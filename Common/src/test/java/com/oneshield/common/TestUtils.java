package com.oneshield.common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.oneshield.common.errorhandling.ErrorConst;
import com.oneshield.common.errorhandling.OneshieldException;
import com.oneshield.dataaccess.base.DataAccessManager;
import com.oneshield.dataaccess.base.DataAccessManager_IF;
import com.oneshield.logging.Category;

public class TestUtils {

	private static final Category cat = Category.getInstance(TestUtils.class);

	private static final String DB_DESCRIPTOR = "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=OSICORE52DEVDB1.oneshield.com)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=DCORE52)))";
	public static final String DB_URL = "jdbc:oracle:thin:@" + DB_DESCRIPTOR;
	public static final String JDBC_LONG_URL = "jdbc:oracle:thin:CORE_API/CORE_API@" + DB_DESCRIPTOR;
	public static final String USER_NAME = "CORE_API";
	public static final String PASS_WORD = "CORE_API";

	public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";
	public static final String JDBC_DRIVER_NAME = "jdbc_driver";
	public static final String JDBC_LONG_URL_NAME = "jdbc_long_url";

	public static final String POLICY_TAG_NAME = "Policy";
	public static final String POLICY_NUMBER_TAG_NAME = "PolicyNumber";

	public static final boolean AUTO_COMMIT_TRUE = true;
	public static final boolean AUTO_COMMIT_FALSE = false;

	public static final String JUNIT_LOG_CAT_NAME = "JUnitTest";

	public static final String REMOTE_HOST_KEY = "REMOTE_HOST";
	public static final String HTTP_SESSION_ID_KEY = "HTTP_SESSION_ID";
	public static final String REMOTE_HOST_TEST_VALUE = "0";
	public static final String HTTP_SESSION_TEST_VALUE = "0";

	public static final String SQL_TXT_CREATE_USER_SESSION_ID = "{call pkg_os_wf.sp_user_session_create(?,?,?,?,?,?,?,?,?,?)}";
	public static final String SQL_TXT_CREATE_TRANSACTION_ID = "{call ? := pkg_os_wf_session.fn_transaction_get(?,?,?,?)}";

	private DataSource ds;
	private Context context;
	private static final String JNDI_JAVA_COMP_SUBCONTEXT = "java:comp";
	private static final String JNDI_JDBC_SUBCONTEXT = "jdbc";
	private static final String JNDI_OS_DATASOURCE = "jdbc/Oneshield Datasource";
	private static final Long UNKNOWN_ERR_CODE = Long.valueOf(ErrorConst.CODE_UNDEFINED);
	private static final String ERR_MSG_DATA_ACCESS_MGR_NULL = "DataAccessManager is null ";
	private static final String ERR_MSG_CONNECTION_NULL = "Connection is null ";
	private static final String ERR_MSG_USER_SESSION_NULL = "userSessionId is null";

	public TestUtils() 
	{
		try 
		{
			System.setProperty(JDBC_DRIVER_NAME, ORACLE_DRIVER_CLASS);
			System.setProperty(JDBC_LONG_URL_NAME, JDBC_LONG_URL);
			ds = setupDataSource(JDBC_LONG_URL);
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

			context = new InitialContext();
			context.createSubcontext(JNDI_JDBC_SUBCONTEXT);
			context.bind(JNDI_OS_DATASOURCE, ds);
			context.createSubcontext(JNDI_JAVA_COMP_SUBCONTEXT);
		} 
		catch (Exception ex) 
		{
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE, "error initializing TestUtils", ex);
		}
	}

	private DataSource setupDataSource(String connectionURI) 
	{
		ObjectPool connectionPool = new GenericObjectPool(null);

		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionURI, null);

		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);

		return new PoolingDataSource(connectionPool);
	}

	public DataAccessManager_IF createDataAccessManager() 
	{
		DataAccessManager_IF dataAccessManager = this.createdFixedConnectionDataAccessManager(ORACLE_DRIVER_CLASS,
				DB_URL, USER_NAME, PASS_WORD, false);
		dataAccessManager.setAutoCommit(false);

		return dataAccessManager;
	}

	public String createSession(DataAccessManager_IF dataAccessManager) 
	{
		String sessionId = null;

		System.setProperty("oneshield.log.config.dir", "C:\\logs");

		sessionId = this.createDragonUserSessionId(dataAccessManager, REMOTE_HOST_TEST_VALUE, "1218", null, "0", "u",
				"u", HTTP_SESSION_TEST_VALUE);

		return sessionId;
	}

	public DataAccessManager_IF createdFixedConnectionDataAccessManager(String driver, String dbUrl, String userName,
			String password, boolean autoCommit) 
	{
		String exceptionMessage = "createdFixedConnectionDataAccessManager method failed";
		String exceptionDetail = "driver = " + driver + "   dbUrl = " + dbUrl + "   autoCommit = " + autoCommit;

		this.assertDataAccessManagerCreateParameters(exceptionMessage, driver, dbUrl, userName, password, cat);

		Connection connection = null;
		DataAccessManager dataccessMngr = null;

		try 
		{
			connection = DataAccessManager.getJdbcConnection(driver, dbUrl, userName, password, autoCommit);

			ExceptionUtil.assertBool(connection != null, UNKNOWN_ERR_CODE, ERR_MSG_CONNECTION_NULL + exceptionDetail);
		} 
		catch (OneshieldException ex) 
		{
			throw ex;
		} 
		catch (Exception ex) {
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE, exceptionMessage, ex);
		}

		try 
		{
			dataccessMngr = DataAccessManager.getNewInstance(connection);
			ExceptionUtil.assertBool(dataccessMngr != null, UNKNOWN_ERR_CODE,
					ERR_MSG_DATA_ACCESS_MGR_NULL + exceptionDetail);
			ExceptionUtil.assertBool(dataccessMngr.isInitialized(), UNKNOWN_ERR_CODE,
					"DataAccessManager is initialized - construction failure:   " + exceptionDetail);
			dataccessMngr.isInitialized();
		} 
		catch (OneshieldException ex) 
		{
			throw ex;
		} 
		catch (Exception ex) 
		{
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE, exceptionMessage, ex);
		}

		return dataccessMngr;
	}

	public void assertDataAccessManagerCreateParameters(String exceptionMessage, String driver, String dbUrl,
			String userName, String password, Category logCategory)
	{
		StringBuilder exceptionDetails = new StringBuilder();

		boolean throwException = false;

		if (driver == null || driver.length() <= 0) 
		{
			exceptionDetails.append("driver is null   ");

			throwException = true;
		}

		if (dbUrl == null || dbUrl.length() <= 0) 
		{
			exceptionDetails.append("dbUrl is null   ");

			throwException = true;
		}

		if (userName == null || userName.length() <= 0) 
		{
			exceptionDetails.append("userName is null   ");

			throwException = true;
		}

		if (password == null || password.length() <= 0) 
		{
			exceptionDetails.append("password is null   ");

			throwException = true;
		}

		if (throwException) 
		{
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE,
					exceptionMessage + exceptionDetails.toString(), null);
		}
	}

	/**
	 * The Data AccessManager needs to be set to "Fixed" or "Thread" connection
	 * mode.
	 */
	public String createDragonUserSessionId(DataAccessManager_IF daMngr, String ipAddress, String exchangeId,
			String entryPointAction, String partnername, String username, String password, String httpSessionId) 
	{

		String userSessionId = null;
		Connection connection = null;
		CallableStatement callableStatement = null;

		try 
		{
			ExceptionUtil.assertBool(daMngr != null, UNKNOWN_ERR_CODE, ERR_MSG_DATA_ACCESS_MGR_NULL);
			connection = daMngr.getConnection();
			ExceptionUtil.assertBool(connection != null, UNKNOWN_ERR_CODE, ERR_MSG_CONNECTION_NULL);
			callableStatement = connection.prepareCall(SQL_TXT_CREATE_USER_SESSION_ID);
			ExceptionUtil.assertBool(callableStatement != null, UNKNOWN_ERR_CODE,
					"connection.prepareCall failed - sql = " + SQL_TXT_CREATE_USER_SESSION_ID);

			callableStatement.setString(1, ipAddress);

			callableStatement.setString(2, exchangeId);

			callableStatement.setString(3, entryPointAction);

			callableStatement.setString(4, partnername);

			callableStatement.setString(5, username);

			callableStatement.setString(6, password);

			callableStatement.setString(7, httpSessionId);

			// Session Id
			callableStatement.registerOutParameter(8, oracle.jdbc.OracleTypes.VARCHAR);

			// New Action
			callableStatement.registerOutParameter(9, oracle.jdbc.OracleTypes.VARCHAR);

			// Workflow context
			callableStatement.registerOutParameter(10, oracle.jdbc.OracleTypes.VARCHAR);

			callableStatement.execute();

			userSessionId = callableStatement.getString(8);

			ExceptionUtil.assertBool(userSessionId != null, UNKNOWN_ERR_CODE, ERR_MSG_USER_SESSION_NULL);
		} 
		catch (OneshieldException ex) {
			throw ex;
		} 
		catch (Exception ex) {
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE, ex.getMessage(), ex);
		} 
		finally 
		{
			try 
			{
				if (callableStatement != null) {
					callableStatement.close();
				}
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return userSessionId;
	}

	public String createDragonTransactionId(DataAccessManager_IF daMngr, String userSessionId) {
		Connection connection = null;
		CallableStatement callableStatement = null;
		String transactionId = null;

		try 
		{
			ExceptionUtil.assertBool(daMngr != null, UNKNOWN_ERR_CODE, ERR_MSG_DATA_ACCESS_MGR_NULL);

			ExceptionUtil.assertBool(userSessionId != null, UNKNOWN_ERR_CODE, ERR_MSG_USER_SESSION_NULL);

			connection = daMngr.getConnection();

			ExceptionUtil.assertBool(connection != null, UNKNOWN_ERR_CODE, ERR_MSG_CONNECTION_NULL);

			callableStatement = connection.prepareCall(SQL_TXT_CREATE_TRANSACTION_ID);

			ExceptionUtil.assertBool(callableStatement != null, UNKNOWN_ERR_CODE,
					"connection.prepareCall failed - sql = " + SQL_TXT_CREATE_TRANSACTION_ID);

			callableStatement.registerOutParameter(1, Types.NUMERIC);

			callableStatement.setLong(2, Long.parseLong(userSessionId));

			callableStatement.setNull(3, oracle.jdbc.OracleTypes.BIGINT);

			callableStatement.setNull(4, oracle.jdbc.OracleTypes.BIGINT);

			callableStatement.setNull(5, oracle.jdbc.OracleTypes.BIGINT);

			callableStatement.execute();

			long transactionIdL = callableStatement.getLong(1);

			ExceptionUtil.assertBool(transactionIdL >= 1, UNKNOWN_ERR_CODE,
					"transactionIdL is equal to or less than 0");

			transactionId = Long.toString(transactionIdL);

			ExceptionUtil.assertBool(transactionId != null && transactionId.length() > 0, UNKNOWN_ERR_CODE,
					ERR_MSG_USER_SESSION_NULL);

		} 
		catch (OneshieldException oex) {
			throw oex;
		} 
		catch (Exception ex) {
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE,
					"createDragonTransactionId method failed", ex);
		} 
		finally 
		{
			try
			{
				if (callableStatement != null) {
					callableStatement.close();
				}
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return transactionId;
	}

	public static void assertBasicParameters(DataAccessManager_IF daMnger, String exceptionMsg, Long sessionId,
			Long transactionId, Long objectId, Category logCategory) 
	{
		assertBasicParameters(daMnger, exceptionMsg, sessionId, transactionId, objectId, null, logCategory);
	}

	public static void assertBasicParameters(DataAccessManager_IF daMnger, String exceptionMsg, Long sessionId,
			Long transactionId, Long objectId, Long attributeId, Category logCategory) 
	{
		StringBuilder exceptionDetails = new StringBuilder();
		boolean foundError = false;
		boolean throwException = false;

		ExceptionUtil.assertBool(daMnger != null, UNKNOWN_ERR_CODE, "DataAccessManager is null");

		if (sessionId != null) 
		{
			foundError = checkAttributeValue(sessionId);

			if (foundError) {
				exceptionDetails.append("sessionId = " + sessionId + "   ");

				throwException = true;
			}
		}

		if (transactionId != null) 
		{
			foundError = checkAttributeValue(transactionId);

			if (foundError) {
				exceptionDetails.append("transactionId = " + transactionId + "   ");

				throwException = true;
			}
		}

		if (objectId != null) 
		{
			foundError = checkAttributeValue(objectId);

			if (foundError) {
				exceptionDetails.append("objectId = " + objectId + "   ");

				throwException = true;
			}
		}

		if (attributeId != null) 
		{
			foundError = checkAttributeValue(attributeId);

			if (foundError) {
				exceptionDetails.append("attributeId = " + attributeId + "   ");

				throwException = true;
			}
		}

		if (throwException) 
		{
			ExceptionUtil.generateAndThrowOneShieldException(UNKNOWN_ERR_CODE, exceptionDetails.toString(), null);
		}
	}

	private static boolean checkAttributeValue(Long attributeObject) 
	{
		boolean result = false;

		if (attributeObject != null && attributeObject.longValue() == 0) 
		{
			result = true;
		}

		return result;
	}

	public static Long covertToLong(String attributeValue) 
	{
		if (attributeValue == null) 
		{
			return null;
		} 
		else 
		{
			return Long.parseLong(attributeValue.trim());
		}

	}

	public static Integer covertToInteger(String attributeValue) 
	{
		if (attributeValue == null) 
		{
			return null;
		} 
		else 
		{
			return Integer.parseInt(attributeValue.trim());
		}

	}

	/**
	 * This method is used to cleanup JDBC Resources. This is used for the pattern
	 * where a Connection, Statement, and ResultSet are used. It will close or free
	 * these resources based whether or not they are null.
	 * 
	 * @param resultSet         - close resultSet if not null
	 * @param statement         - close statement if not null - this is an interface
	 *                          so you can pass any of the JDBC Statement types.
	 * @param dataAccessManager - used for freeing a connection
	 * @param connection        - Free Connection if not null
	 */
	public void cleanupJdbcResources(String message, ResultSet resultSet, Statement statement,
			DataAccessManager_IF dataAccessManager, Connection connection) 
	{
		if (resultSet != null) 
		{
			try 
			{
				resultSet.close();
			} 
			catch (Exception ex) {
				cat.debug(message + " could not close a ResultSet", ex, Category.NO_SESSION_ID,
						Category.NO_TRANSACTION_ID);
			}
		}

		if (statement != null) 
		{
			try 
			{
				statement.close();
			} 
			catch (Exception ex) 
			{
				cat.debug(message + " could not close a Statement", ex, Category.NO_SESSION_ID,
						Category.NO_TRANSACTION_ID);
			}
		}

		if (dataAccessManager != null && connection != null) 
		{
			try 
			{
				// cat.debug("cleanupJdbcResources free connection called",
				// Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);

				// dataAccessManager.freeConnection(connection);
			} 
			catch (Exception ex) {
				// cat.debug(message + " could not free a Connection", ex,
				// Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			}
		}
	}

}
