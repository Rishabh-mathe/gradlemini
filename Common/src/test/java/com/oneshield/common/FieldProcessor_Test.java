package com.oneshield.common;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.oneshield.common.cache.FieldMetaDataCache;

public class FieldProcessor_Test {

	@Test
	public void testIsStringLengthValid_Positive() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(8);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("aas", 2, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(true);
		}
		else
		{
			assertTrue(false);
		}
	}
	
	@Test
	public void testIsStringLengthValid_Negative() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(8);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("a", 2, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(false);
		}
		else
		{
			assertTrue(true);
		}
	}
	
	@Test
	public void testIsIntegerLengthValid_Positive() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(2);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("1233", 2, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(true);
		}
		else
		{
			assertTrue(false);
		}
	}
	
	@Test
	public void testIsIntegerLengthValid_Negative() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(2);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("1", 2, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(false);
		}
		else
		{
			assertTrue(true);
		}
	}
	@Test
	public void testIsFloatLengthValid_Positive() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(3);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("2.23", 2, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(true);
		}
		else
		{
			assertTrue(false);
		}
	}
	
	@Test
	public void testIsFloatLengthValid_Negative() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(3);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 2, 0);
		String formatedValue = fieldProcessor.isLengthValid("1.1", 4, 7);
		String validOutput = "NO_LENGTH_VALUE_ERROR";
		if(validOutput.equals(formatedValue))
		{
			assertTrue(false);
		}
		else
		{
			assertTrue(true);
		}
	}
	
	@Test
	public void testIsLengthValid_MessagePositive() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(3);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 3, 0);
		fieldProcessor.maxlengthValidationError = "The floating point number {1} is too high, the maximum value is {0}.";
		String formatedValue = fieldProcessor.isLengthValid("1.13455555555", 4, 7);
		if(formatedValue.contains("1.13455555555"))
		{
			assertTrue(true);
		}
		else
		{
			assertTrue(false);
		}
	}
	
	@Test
	public void testIsLengthValid_Message_Negative() 
	{
		FieldMetaDataCache fieldMetaDataCache = FieldMetaDataCache.getInstance();
		BigDecimal logicalDataType = new BigDecimal(3);
		FieldProcessor fieldProcessor = FieldMetaDataCache.lookup(fieldMetaDataCache, "en_US", logicalDataType, null, 3, 0);
		fieldProcessor.minlengthValidationError = "The floating point number" +"{1}"+ "is too low, the minimum value is {0}.";
		String formatedValue = fieldProcessor.isLengthValid("0.1", 4, 7);
		if(formatedValue.contains("0.1"))
		{
			assertTrue(true);
		}
		else
		{
			assertTrue(false);
		}
	}

}
