package com.oneshield.ui.helper;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.oneshield.logging.Category;
import com.oneshield.test.framework.DragonTestCaseBase;

public class FieldProcessorHelper_Test extends DragonTestCaseBase
{
	public static Category cat = Category.getInstance(FieldProcessorHelper_Test.class);
	
	private static final String EXPECTED_RESULT = "expectedResult";
	
	private HttpSession httpSession = null;
	public FieldProcessorHelper_Test(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setUpX() 
	{
		this.httpSession = mockHttpSession( mockHttpServletRequest() );
	}
	 
	@Test
	public void testExtDateFormat()
	{
		cat.info("testExtDateFormat" + this.getLogFolder() );
		String extDate = FieldProcessorHelper.formatDateForExtStore("20160310000001");
		
		assertEquals("2016-03-10 00:00:01", extDate);
	}
	
	@Test
	public void testValidDateFormat() throws Exception
	{
		cat.info("testValidDateFormat");
		
		JSONObject jsonObj = this.readJsonData(this.testDataInputFolder, "testValidDateFormat.json");
		
		String[] results = formatString(jsonObj);
		
		String expectedResult = getNullableString(jsonObj, EXPECTED_RESULT);
		
		assertEquals(expectedResult, results[0]);
	}
	
	@Test
	public void testProcessFieldDateValid() throws Exception
	{
		cat.info("testProcessFieldDate");
		
		JSONObject jsonObj = this.readJsonData(this.testDataInputFolder, "testProcessFieldDateValid.json");
		
		String[] results = this.processField(jsonObj);
		
		String expectedResult = getNullableString(jsonObj, EXPECTED_RESULT);
		
		assertTrue(results[0],  results[1].equalsIgnoreCase(expectedResult));
	}
	
	@Test
	public void testProcessFieldDateInvalid() throws Exception
	{
		cat.info("testProcessFieldDate");
		
		JSONObject jsonObj = this.readJsonData(this.testDataInputFolder, "testProcessFieldDateInvalid.json");
		
		String[] results = this.processField(jsonObj);
		
		String expectedResult = getNullableString(jsonObj, EXPECTED_RESULT);
		
		assertTrue(results[0],  results[1].equalsIgnoreCase(expectedResult));
	}
	
	@Test
	public void testInvalidDateFormat() throws Exception
	{
		cat.info("testInvalidDateFormat");
		
		JSONObject jsonObj = this.readJsonData(this.testDataInputFolder, "testInvalidDateFormat.json");
		
		String[] results = formatString(jsonObj);
				
		String expectedResult = getNullableString(jsonObj, EXPECTED_RESULT);
				
		assertEquals(expectedResult, results[0]);
	}
	
	@Test
	public void testValidFormatPhone() throws Exception
	{
		cat.info("testValidFormatPhone");
		
		JSONObject jsonObj = this.readJsonData(this.testDataInputFolder, "testValidFormatPhone.json");
		
		String[] results = formatString(jsonObj);
				
		String expectedResult = getNullableString(jsonObj, EXPECTED_RESULT);
				
		assertEquals(expectedResult, results[0]);
	}
	
	
	private String[] formatString(JSONObject jsonObj)
	{
		return FieldProcessorHelper.formatString
		(
			getNullableString(jsonObj,"fieldName"),
			getNullableString(jsonObj,"fieldValue"),
			getNullableString(jsonObj,"localeCode"),
			getNullableString(jsonObj,"pageActionId")
		);
	}
	private String[] processField(JSONObject jsonObj)
	{
		
		return FieldProcessorHelper.processField
		(
			getNullableString(jsonObj,"fieldName"),
			getNullableString(jsonObj,"fieldValue"),
			getNullableString(jsonObj,"localeCode"),
			true,
			true,
			getNullableString(jsonObj,"pageActionId"),
			this.httpSession
		);
	}
}
