package com.oneshield.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.oneshield.common.errorhandling.OneshieldException;

public class ExceptionUtilTest 
{
	
	private static final String GENERIC_EXCEPTION_MESSAGE = "GENRIC_EXCEPTION";
	private static final String GENERIC_EXCEPTION_MESSAGE_WITH_PARAM = "GENRIC_EXCEPTION with Error ? and ?";
	private static final String GENERIC_EXCEPTION_MESSAGE_WITH_PARAM_SUBSTITUTUED = "GENRIC_EXCEPTION with Error Unwanted and Unexpected";
	private static final List<Object> ERROR_MESSAGE_PARAMS =  Arrays.asList("Unwanted", "Unexpected");
	private static final Long ERROR_CODE = 1234l;
	private static final String FAIL_TEST_MESSAGE = "Unexpected event";
	private static final String SQL_EXCEPTION_MESSAGE = "SQL Exception Occurred";
	
	@Test(expected = OneshieldException.class)
	public void testAssertBool()
	{
		ExceptionUtil.assertBool(ERROR_CODE == ERROR_CODE+1, ERROR_CODE, GENERIC_EXCEPTION_MESSAGE);			
	}
	
	@Test
	public void testAssertBool2()
	{
		try
		{
			ExceptionUtil.assertBool(ERROR_CODE == ERROR_CODE+1, ERROR_CODE, GENERIC_EXCEPTION_MESSAGE);
			fail(FAIL_TEST_MESSAGE);
		}
		catch(OneshieldException e)
		{
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			//notice that no stack trace in log 

		}
	}
	
	@Test
	public void testAssertBool3()
	{
		try
		{
			ExceptionUtil.assertBool(ERROR_CODE == ERROR_CODE+1, ERROR_CODE, GENERIC_EXCEPTION_MESSAGE_WITH_PARAM, ERROR_MESSAGE_PARAMS);	
			fail(FAIL_TEST_MESSAGE);
		}
		catch(OneshieldException e)
		{
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			//make sure parameter substitution occurred
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE_WITH_PARAM_SUBSTITUTUED));			
			//notice that no stack trace in log 

		}
	}
	
	@Test
	public void testAssertBool4()
	{
		try
		{
			ExceptionUtil.assertBool(ERROR_CODE == 1234l, ERROR_CODE, GENERIC_EXCEPTION_MESSAGE);			
		}
		catch(OneshieldException e)
		{
			fail(FAIL_TEST_MESSAGE);
		}
	}
	
	
	@Test
	public void testGenerateExceptionMethod() {
		
		SQLException sqlException = new SQLException(SQL_EXCEPTION_MESSAGE);
		
		try 
		{
			ExceptionUtil.generateException(ERROR_CODE, GENERIC_EXCEPTION_MESSAGE, sqlException, true);
			fail(FAIL_TEST_MESSAGE);
		}
		catch(OneshieldException e) 
		{
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			assertTrue(e.getMessage().contains(SQL_EXCEPTION_MESSAGE));
			//notice there is no stack trace in log
		}
		
	}
	
	@Test
	public void testGenerateExceptionMethod2() {
		
		SQLException sqlException = new SQLException(SQL_EXCEPTION_MESSAGE);
		
		try 
		{
			OneshieldException  e = ExceptionUtil.generateException(ERROR_CODE,GENERIC_EXCEPTION_MESSAGE, sqlException, false);
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			assertTrue(e.getMessage().contains(SQL_EXCEPTION_MESSAGE));
			//notice there is no stack trace in log
		}
		catch(OneshieldException e) 
		{
			fail(FAIL_TEST_MESSAGE);
			//notice there is no stack trace in log
		}
		
	}
}
