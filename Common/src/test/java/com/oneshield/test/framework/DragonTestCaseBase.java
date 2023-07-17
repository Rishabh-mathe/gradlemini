package com.oneshield.test.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import com.oneshield.common.JvmCache;
import com.oneshield.common.constants.SystemAttribute;
import com.oneshield.common.constants.TransactionConst;
import com.oneshield.common.domain.Request;
import com.oneshield.common.xml.XmlConstants;
import com.oneshield.dataaccess.base.DataAccessManager;
import com.oneshield.dataaccess.base.DataAccessManager_IF;
import com.oneshield.dataaccess.utils.BackgroundLogon;
import com.oneshield.logging.Category;
import com.oneshield.property.OSPropertiesManager;
import com.oneshield.startup.DragonStartupService;
import com.oneshield.statemachine.ActionDescriptor;
import com.oneshield.statemachine.ActionSpec;

/**
 * Base Test class to be extended by other junit test cases.
 * 
 * @author bthavanati
 * 
 */
public abstract class DragonTestCaseBase extends TestCase 
{
	private static final String JUNIT_INPUTS = "junit-inputs";

	private static final String JUNIT_TEST_FOLDER = "junit_test_folder";

	private static final String JUNIT_PROPS_FOLDER = "junit_properties_folder";

	private final static Category cat =
				Category.getInstance("JUnitTest");


	
	protected File testDataInputFolder = null;
	
	private OSPropertiesManager osPropertiesMgr;

	private Connection dbConnection;

	private DataAccessManager_IF dataAccessMgr;

	private ActionDescriptor actionDescriptor = null;

	private ActionSpec actionSpec = null;

	private Request request;
	
	private TestCaseDbConnection osDB = null;

	public DragonTestCaseBase(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception 
	{
		Properties props =  getTestProperties("osjunit");
		
		System.setProperty("jdbc_long_url", props.getProperty("jdbc_long_url"));
	
		System.setProperty("jdbc_driver", props.getProperty("jdbc_driver"));
		
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, org.apache.naming.java.javaURLContextFactory.class.getName());
		
		JvmCache jvmCache = JvmCache.getInstance();
		
		jvmCache.putObject(TransactionConst.DEBUG_KEY,  props.getProperty(TransactionConst.DEBUG_KEY,"true"));
		
		// Connection Pool Creation.
		
		osDB = new TestCaseDbConnection();
		
		// load properties from database
		osPropertiesMgr = OSPropertiesManager.getInstance();
		
		this.testDataInputFolder = this.getTestInputsDataFolder();
		
		cat.info("Test data for ["+this.getClass()+"] is expected to be at "+this.testDataInputFolder.getAbsolutePath());
	

		dbConnection = DataAccessManager.getNewInstance().getConnection();
		// get data access mgr connection using db connection
		dataAccessMgr = DataAccessManager.getNewInstance(dbConnection);
		
		// get sessionid using background login
		actionDescriptor = BackgroundLogon.createUserSession
				(
					dataAccessMgr, 
					"osjunit-test",
					props.getProperty("os_exchangeid"), 
					props.getProperty("entryActionId","2"),
					props.getProperty("os_partner"), 
					props.getProperty("os_user"),
					props.getProperty("os_pwd"), 
					props.getProperty("http_session_id","0")
				);

		actionDescriptor.setObjectId( new Long(actionDescriptor.getUserSessionId()) );
		
		cat.info("logged in as "+props.getProperty("os_user")+", created user session ID "+actionDescriptor.getUserSessionId());
		
		dataAccessMgr.setUserSessionId(actionDescriptor.getUserSessionId());

//		long transactionId = DataAccessUtil.generateTransactionId(dataAccessMgr,
	//			Long.parseLong(actionDescriptor.getUserSessionId()));
		
		//cat.info("created transaction id "+transactionId+", under  user session ID "+actionDescriptor.getUserSessionId());
		
		//actionDescriptor.setTransactionId(Long.valueOf(transactionId));
		actionSpec = new ActionSpec();
		actionSpec.setUserSessionId(actionDescriptor.getUserSessionId());
		//actionSpec.setTransactionId( String.valueOf(transactionId));
		actionSpec.setActionOutcomeId(ActionSpec.ACTION_OUTCOME_OK);

		
		//load caches that load during server startup
		new DragonStartupService().initialized();
		
		request = new Request();
		request.setSessionId(actionDescriptor.getSessionId());
		request.setUserSessionId(actionDescriptor.getUserSessionId());
		//request.setTransactionId( String.valueOf(transactionId)); 
		request.setName("DRAGON_JUNIT_TESTS");
		
		
		
		setUpX();
	}

	
	/**
	 * in case if the individual unit test case needs to do any other setup.
	 */
	protected abstract void setUpX();
	

	protected void tearDown() throws Exception 
	{
		// super.tearDown();
		if (dbConnection != null) 
		{
			dbConnection.close();
		}		
		osDB.unbind();
	}

	public ActionDescriptor getActionDescriptor() 
	{
		return actionDescriptor;
	}

	public Request getRequest() 
	{
		return request;
	}

	public DataAccessManager_IF getDataAccessManager() 
	{
		return dataAccessMgr;
	}

	public ActionSpec getActionSpec() 
	{
		return actionSpec;
	}

	public OSPropertiesManager getPropertiesMgr() 
	{
		return osPropertiesMgr;
	}
	

	private Properties getTestProperties(String name) throws IOException
	{
	
		String configFolder = System.getProperty(JUNIT_PROPS_FOLDER);
		Properties props = new Properties();
		boolean useConfigFolder = false;
		
		if( configFolder == null )
		{
			System.out.println("[" + name + "] Property "+JUNIT_PROPS_FOLDER+" is null, will attempt using class loader");
		}
		else
		{
			File f = new File(configFolder);
			
			if( f == null || !f.exists() )
			{
				System.err.println("[" + name + "] Folder represented by the property "+JUNIT_PROPS_FOLDER+" does not exist");
			}
			else if ( !f.isDirectory() )
			{
				System.err.println("[" + name + "] File represented by the property "+JUNIT_PROPS_FOLDER+" is not a folder");
			} else
			{
				useConfigFolder = true;
			}
		}
		
		if( useConfigFolder )
		{
			String[] tryFullNames = {
					configFolder + System.getProperty("file.separator") + name
			};
			for (String tryName : tryFullNames)
			{
				FileInputStream fis = null;
				
				try
				{
					fis = new FileInputStream(tryName+".properties");
			        props.load(fis);    
				} 
				catch (Throwable th1)
				{
				}
				finally
				{
					try {
						if (fis != null)
					        fis.close();
					} catch (Throwable th)
					{
					}
				}
				
				if (!props.entrySet().isEmpty() ) 
				{
					break;
				}
			}			
			if (props.entrySet().isEmpty())
			{	
				throw new IOException("No properties found for "+name+" in "+JUNIT_PROPS_FOLDER);
			}
		}
		else // use the internal properties files
		{
			System.out.println("Loading "+name + ".properties from class loader");
			try
			{
				props.load(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(name + ".properties"));
			} catch (Throwable th1)
			{
				System.out.println("could not load "+name + ".properties" +" from current thread classloader");
			}
		}
	
		return props;
	}
	
	protected void checkAndDeleteFile(String resultsPath) 
	{
		File file = new File(resultsPath);
		
		if( file.exists()  )
		{
			if( file.isFile())
			{
				cat.debug("checkAndDeleteFile("+resultsPath+") exists and deleting it ");
				file.delete();
			}
			else
			{
				cat.warn("checkAndDeleteFile("+resultsPath+") : IS NOT A FILE ");
			}
		}
		else
		{
			cat.debug("checkAndDeleteFile("+resultsPath+")  file does not exist");
		}
		
	}
	
	private JSONObject readJsonData(BufferedReader br) throws Exception
	{
		JSONObject jsonObj = null;
		try
		{
			StringBuffer buff = new StringBuffer();
			String lineRead = null;
			while( (lineRead = br.readLine() ) != null )
			{
				 buff.append(lineRead);
			}
			
			jsonObj = JSONObject.fromObject(buff.toString());
		}
		finally
		{
			br.close();
		}
	
		return jsonObj;
	}
	protected JSONObject readJsonData(String completePath) throws Exception
	{
		String completePathWithExn = completePath.toUpperCase().endsWith(".JSON") ? completePath : completePath +".json";
		
		File f = new File(completePathWithExn);
		
		return readJsonData(f);
	}
	
	protected JSONObject readJsonData(File completePath) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(completePath));
		
		return readJsonData(br);
	}
	
	protected JSONObject readJsonData(File parent, String child) throws Exception
	{
		String childWithExn = child.toUpperCase().endsWith(".JSON") ? child : child +".json";
		
		BufferedReader br = new BufferedReader(new FileReader(new File( parent,childWithExn)));
		
		return readJsonData(br);
	}
	
	protected Document readXmlData( File parent, String child) throws Exception
	{
		String childWithExn = child.toUpperCase().endsWith(".XML") ? child : child +".xml";
		
		FileInputStream fin = new FileInputStream( new File(parent, childWithExn) );
		
		return readXmlData(fin);
	}
	
	protected Document readXmlData(String completePath) throws Exception
	{
		String completePathWithExn = completePath.toUpperCase().endsWith(".XML") ? completePath : completePath +".xml";
		
		FileInputStream fin = new FileInputStream( completePathWithExn );
		
		return readXmlData(fin);
	}
	
	protected Document readXmlData(File completePath) throws Exception
	{
		FileInputStream fin = new FileInputStream( completePath );
		
		return readXmlData(fin);
	}
	
	private Document readXmlData(FileInputStream fin) throws Exception 
	{
		return getDOMBuilder().parse(fin);
	}

	protected String getTestParamValue( JSONObject testObject, String key )
	{
		return  getNullableString(testObject, key);
	}
	
	protected String[] getTestParamValues( JSONObject testObject, String key )
	{
		String val = getNullableString(testObject,key);
		
		String[] retArr = null;
		
		if( val != null )
		{
			retArr = val.split(",");
		}
		else 
		{
			retArr = new String[1];
			retArr[0] = "";
		}
		return retArr;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setHttpRequestTestParams(JSONObject testObject, HttpServletRequest httpRequest,ArrayList<String> keys)
	{
		for(String key : keys )
		{
			Mockito.when(httpRequest.getParameter(key)).thenReturn(getTestParamValue(testObject,key));
			
			Mockito.when(httpRequest.getParameterValues(key)).thenReturn( getTestParamValues(testObject,key));
		}
		
		Mockito.when( httpRequest.getParameterNames() ).thenReturn(  new IteratorEnumeration(keys.iterator()));
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setHttpRequestTestHeaders(JSONObject jsonObject, HttpServletRequest httpRequest) 
	{
		if( jsonObject != null )
		{	
			Iterator<String> iter = jsonObject.keys();
		
			while( iter.hasNext())
			{	
				String key = iter.next();
			
				Mockito.when( httpRequest.getHeader(key) ).thenReturn(  getTestParamValue( jsonObject,key) );
				
				if( "Content-Type".equals(key)) /* This is important to set, otherwise the servlet request parameter names are wiped out when it is checking for contentType() !!!*/
				{	
					Mockito.when( httpRequest.getContentType() ).thenReturn(  getTestParamValue( jsonObject,key) );
				
				}
			}
		}
		
		Mockito.when( httpRequest.getHeaderNames() ).thenReturn(  new IteratorEnumeration(jsonObject.keys()));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setHttpSessionTestAttributes(HttpSession httpSession, HashMap<String, String> httpSessionAttrs) 
	{
		for(String key : httpSessionAttrs.keySet() )
		{
			Mockito.when(httpSession.getAttribute(key)).thenReturn(httpSessionAttrs.get(key));
		}
		
		Mockito.when( httpSession.getAttributeNames() ).thenReturn(  new IteratorEnumeration(httpSessionAttrs.keySet().iterator()));
		
	}
	
	protected String getNullableString(JSONObject testObject, String key)
	{
		 return testObject.containsKey(key) ? testObject.getString(key) : null ;
	}

	
	protected HttpServletRequest mockHttpServletRequest()
	{
		return Mockito.mock(HttpServletRequest.class);
	}
	
	protected HttpServletResponse mockHttpServletResponse() 
	{
		return Mockito.mock(HttpServletResponse.class);
	}
	

	protected HttpSession mockHttpSession(HttpServletRequest httpRequest)
	{
		HttpSession httpSession =  Mockito.mock(HttpSession.class);
		
		Mockito.when(httpRequest.getSession()).thenReturn(httpSession);  
		
		Mockito.when(httpRequest.getSession(Mockito.anyBoolean())).thenReturn(httpSession);  
		
		Mockito.when(httpSession.getId()).thenReturn("testHttpsessionId");  
		
		return httpSession;
	}
	
	protected ServletContext mockServletContext(HttpServlet servlet)
	{
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		
		Mockito.when(servlet.getServletContext()).thenReturn(servletContext); 
		
		
		
		
		return servletContext;
	}

	protected File getTestInputsDataFolder() throws Exception
	{
		File testDataFolder = null;
		
		String strTestDataFolder = null;
	    
	    try
	    {
	    	strTestDataFolder = this.osPropertiesMgr.getProperty(JUNIT_TEST_FOLDER, null);
	    } 
	    catch (Exception ex)
	    {
	        cat.warn("getJunitTestDataFolder() : exception[suppressed] getting Property junit_test_folder :" + ex.toString());
	       
	    }
	    
	    if( strTestDataFolder == null )
	    {
		   cat.info("system attribute "+JUNIT_TEST_FOLDER+" value is not set, so trying to find the folder 'inputs' in the current classpath");
	    	try
		    {
		    	strTestDataFolder = this.getClass().getClassLoader().getResource(JUNIT_INPUTS).getFile().toString();
		    }
		    catch(Exception ex)
		    {
		    	ex.printStackTrace();
		    	throw new Exception("Exception while reading the folder inputs from folder "+JUNIT_INPUTS+" in classpath. This must be fixed for test cases to work");
		    }
	    }
	    
	    cat.info("\n>>>>>>>>>>> getJunitTestDataFolder() root folder of the test inputs is "+strTestDataFolder+ " <<<<<<<<<<<<<<<<<<<<<\n");
	    
	    File rootTestFolder = new File(strTestDataFolder);
	    
	    if(! rootTestFolder.isDirectory() )
	    {
	    	throw new Exception(strTestDataFolder+ " does not represent a folder ");
	    }
	    
	    if(! rootTestFolder.canRead()  )
	    {
	    	throw new Exception(strTestDataFolder+ " folder does not have read permissions ");
	    }
	    
		String subFolderPath = this.getClass().getName().replace(".", "/");
		
		testDataFolder = new File(rootTestFolder, subFolderPath);
		
		if(! testDataFolder.isDirectory() )
	    {
	    	throw new Exception(testDataFolder+ " does not represent a folder ");
	    }
	    
	    if(! testDataFolder.canRead()  )
	    {
	    	throw new Exception(testDataFolder+ " folder does not have read permissions ");
	    }
		
		return testDataFolder;
	}
	
	protected String getLogFolder()
	{
		String logFolder = null;
		
	    try
	    {
	    	logFolder = this.osPropertiesMgr.getProperty(SystemAttribute.core_mt_log_folder, "/logs");
	    } 
	    catch (Exception ex)
	    {
	        cat.warn("Exception[suppressed] getting Property "+SystemAttribute.core_mt_log_folder+":" + ex.toString());
	        logFolder = "/logs";
	    }
	 	
		return logFolder;
	}
	
	private static DocumentBuilder getDOMBuilder() throws ParserConfigurationException
	{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
				ClassLoader.getSystemClassLoader());
			
		    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			factory.setFeature(XmlConstants.DISALLOW_DTD_FEATURE, true);
			factory.setFeature(XmlConstants.EXTERNAL_PARAMETER_ENTITIES, false);
			factory.setFeature(XmlConstants.LOAD_EXTERNAL_DTD, false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			
			return factory.newDocumentBuilder();
	}	
	/**
	 * To bridge the case where an Enumeration is still used in place of an Iterator
	 * @author bthavanati
	 *
	 */
	class IteratorEnumeration<E> implements Enumeration<E>
	{
	    private final Iterator<E> iterator;

	    public IteratorEnumeration(Iterator<E> iterator)
	    {
	        this.iterator = iterator;
	    }

	    public E nextElement() 
	    {
	        return iterator.next();
	    }

	    public boolean hasMoreElements() 
	    {
	        return iterator.hasNext();
	    }

	}
	class TestCaseDbConnection 
	{
		//private static final String url = "jdbc:oracle:thin:GCNA_API/GCNA_API@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=vip-oscdevdb2)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=DEVGCNA)))";

		private static final String JNDI_USER_TRANSACTION = "java:comp/UserTransaction";

		private static final String JNDI_JAVA_COMP_SUBCONTEXT = "java:comp";

		private static final String JNDI_JDBC_SUBCONTEXT = "jdbc";

		private static final String JNDI_OS_DATASOURCE = "jdbc/Oneshield Datasource";

		private DataSource ds;

		private Context context;

		public TestCaseDbConnection() throws Exception
		{
			super();
					
			System.setProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver");
			ds = setupDataSource(System.getProperty("jdbc_long_url"));
			context = getInitialContext();
			context.createSubcontext(JNDI_JDBC_SUBCONTEXT);
			context.bind(JNDI_OS_DATASOURCE, ds);
			
			context.createSubcontext(JNDI_JAVA_COMP_SUBCONTEXT);
			
			UserTransaction userTxn = Mockito.mock(UserTransaction.class);
			
			context.bind(JNDI_USER_TRANSACTION,userTxn);
		}

		public Connection getConnection() throws Exception 
		{
			DataSource dataSource = (DataSource) context.lookup(JNDI_OS_DATASOURCE);
			return dataSource.getConnection();
		}

		protected InitialContext getInitialContext() throws NamingException {
			/*Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.INITIAL_CONTEXT_FACTORY, org.apache.naming.java.javaURLContextFactory.class.getName());
			System.setProperty(Context.URL_PKG_PREFIXES,"org.apache.naming");
			InitialContext ctx = new InitialContext(environment);
			return ctx;*/
			
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
	                "org.apache.naming.java.javaURLContextFactory");
	            System.setProperty(Context.URL_PKG_PREFIXES, 
	                "org.apache.naming");            
	            InitialContext ic = new InitialContext();
	            
	            return ic;
		}

		public DataSource setupDataSource(String connectionURI) throws Exception {
			ObjectPool connectionPool = new GenericObjectPool(null);

			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionURI, null);

			new PoolableConnectionFactory(connectionFactory,
					connectionPool, null, null, false, true);

			PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

			return dataSource;
		}
		
		public void unbind() throws Exception 
		{
			context.unbind(JNDI_OS_DATASOURCE);
			context.destroySubcontext(JNDI_JDBC_SUBCONTEXT);
			context.unbind(JNDI_USER_TRANSACTION);
			context.destroySubcontext(JNDI_JAVA_COMP_SUBCONTEXT);	
		}
	}
}
