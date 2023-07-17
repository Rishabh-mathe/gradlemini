
package com.oneshield.common;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.oneshield.common.domain.Request;
import com.oneshield.dataaccess.base.DataAccessManager_IF;
import com.oneshield.logging.Category;

public class AbstractTestCase
{
    protected static final Category cat = Category.getInstance(TestUtils.JUNIT_LOG_CAT_NAME);
    
    protected static TestUtils utils;
    
    protected static DataAccessManager_IF dataAccessManager;
    
    protected String userSessionId;
    
    protected String transactionId;
    
    @BeforeClass
    public static final void initSuper() throws Exception
    {   
        utils = new TestUtils();
        // dataAccessManager = DataAccessManager.getNewInstance();
        dataAccessManager = utils.createDataAccessManager();
    }
    
    @Before
    public final void setUpSuper() throws Exception
    {
        Request req = new Request();
        req.setUserSessionId("0");
        req.setName("JUnitTest");
        dataAccessManager.enableThreadConnection(req, "JUnitTest");
        dataAccessManager.getConnection().setAutoCommit(false);
        userSessionId = utils.createSession(dataAccessManager);
        transactionId = utils.createDragonTransactionId(dataAccessManager, userSessionId);
        dataAccessManager.setUserSessionId(userSessionId);
    }
    
    @After
    public final void tearDownSuper()
    {
        try
        {
            //dataAccessManager.getConnection().rollback();
            dataAccessManager.getConnection().commit();
            //dataAccessManager.disableThreadConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @AfterClass
    public static final void destroySuper()
    {
        // dataAccessManager.closeFixedConnection();
        // dataAccessManager = null;
    }

}
