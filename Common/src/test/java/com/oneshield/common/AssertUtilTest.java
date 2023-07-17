package com.oneshield.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

import com.oneshield.common.errorhandling.ErrorConst;
import com.oneshield.common.errorhandling.OneshieldException;

public class AssertUtilTest {
	
	private static final String SQL_EXCEPTION_MESSAGE = "SQL Exception Occurred";
	private static final String GENERIC_EXCEPTION_MESSAGE = "GENRIC_EXCEPTION";
	private static final String TEST_EXCEPTION_MESSAGE = "Test Exception";
	private static final String FAIL_TEST_MESSAGE = "Unexpected event";
	private static final Long ERROR_CODE = 1234l;
	
	@Test
	public void testGenerateExceptionMethod() {
		
		OneshieldException oneshieldException = new OneshieldException(ERROR_CODE, TEST_EXCEPTION_MESSAGE, null/* causing exception*/);
		
		try 
		{
			AssertUtil.generateException(GENERIC_EXCEPTION_MESSAGE, oneshieldException);
			fail(FAIL_TEST_MESSAGE);
		}
		catch(OneshieldException e) 
		{
			//AssertUtil no more creating new exception, code should remain as in original exception
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			assertTrue(e.getMessage().contains(TEST_EXCEPTION_MESSAGE));
			//notice a stack trace in log as AssertUtil always prints it.
		}
		
	}
	
	@Test
	public void testGenerateExceptionMethod2() {
		
		SQLException sqlException = new SQLException(SQL_EXCEPTION_MESSAGE);
		
		try 
		{
			AssertUtil.generateException(ERROR_CODE, GENERIC_EXCEPTION_MESSAGE, GENERIC_EXCEPTION_MESSAGE, sqlException, null /* category*/);
			fail(FAIL_TEST_MESSAGE);
		}
		catch(OneshieldException e) 
		{
			assertEquals(ERROR_CODE.intValue(), e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			assertTrue(e.getMessage().contains(SQL_EXCEPTION_MESSAGE));
			//notice a stack trace in log as AssertUtil always prints it.
		}
		
	}
	
	@Test
	public void testGenerateExceptionMethod3() {
		
		SQLException sqlException = new SQLException(SQL_EXCEPTION_MESSAGE);
		
		try 
		{
			OneshieldException  e = AssertUtil.generateException(GENERIC_EXCEPTION_MESSAGE, sqlException, false);
			assertEquals(ErrorConst.USER_DEFINED_ERROR, e.getCode());
			//make sure old and new messages are there
			assertTrue(e.getMessage().contains(GENERIC_EXCEPTION_MESSAGE));
			assertTrue(e.getMessage().contains(SQL_EXCEPTION_MESSAGE));
			//notice a stack trace in log as AssertUtil always prints it.
		}
		catch(OneshieldException e) 
		{
			fail(FAIL_TEST_MESSAGE);
		}
		
	}

}
