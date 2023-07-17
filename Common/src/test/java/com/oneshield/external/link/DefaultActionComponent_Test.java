package com.oneshield.external.link;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.junit.Test;
import org.mockito.Mockito;

import com.oneshield.logging.Category;
import com.oneshield.test.framework.DragonTestCaseBase;

public class DefaultActionComponent_Test extends DragonTestCaseBase 
{
	public static Category cat = Category.getInstance(DefaultActionComponent_Test.class);
	
	static ArrayList<String> httpParams = new ArrayList<String>()
	{
		private static final long serialVersionUID = 1L;

		{	
			add("MT_LOCALE_ID");
			add("OBJ_ID");
			add("PARTNER");
			add("PWD");
			add("SKIP_COOKIE");
			add("TX_NAME");
			add("USER_ID");
		}
	};

	private DefaultActionComponent target = null;
	
	public DefaultActionComponent_Test(String name) 
	{
		super(name);
	}

	@Override
	protected void setUpX() 
	{
		this.target = new DefaultActionComponent(); 
		
		
	}

	@Test
	public void testLoginPage() throws Exception
	{
		JSONObject paramsObj = this.readJsonData(this.testDataInputFolder, "loginComponentTestData.json");
		
		String resultsPath = this.getLogFolder() + "/LoginTest.json";
		
		HttpServletRequest httpRequest = this.mockHttpServletRequest();
		
		HttpServletResponse httpResponse = this.mockHttpServletResponse();
		
		this.setHttpRequestTestParams( paramsObj, httpRequest, httpParams );
		
		Mockito.when(httpRequest.getParameter("OBJ_ID")).thenReturn(this.getActionDescriptor().getUserSessionId());
		
		PrintWriter writer = new PrintWriter(resultsPath);
	        
	    Mockito.when(httpResponse.getWriter()).thenReturn(writer);
		
	    target.setExchangeId("1218");
		
	   // target.setEntryPointAction(entryPointAction);
		
	    target.setHttpRequest(httpRequest);
		
	    target.setHttpResponse(httpResponse);
		
	    target.setDataAccessManager( this.getDataAccessManager() );
		
	    target.setIpAddress("0");
		
	    target.setHttpSessionId("0");
	    
		target.processExternalLink();
		
		assertEquals(true, true);
		
	}
}
