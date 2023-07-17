package com.oneshield.keymanager.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.oneshield.common.errorhandling.OneshieldException;
import com.oneshield.common.AbstractTestCase;
import com.oneshield.keymanager.KeyManager;
import com.oneshield.keymanager.KeyStatus;

public class KeyManagerTest extends AbstractTestCase
{
	private static final String KEY_TYPE = "3DES";
	
	/*
	 * rotateKey() method should throw OneShieldException if key type is null
	 */
	@Test(expected = OneshieldException.class)
	public void testRotateKeyForNullKeyType()
	{
		UUID uuid = UUID.randomUUID();
		KeyManager.rotateKey(dataAccessManager, userSessionId, transactionId, null, uuid.toString());
	}
	
	/*
	 * rotateKey() method should throw OneShieldException if key is null
	 */
	@Test(expected = OneshieldException.class)
	public void testRotateKeyForNullKey()
	{
		KeyManager.rotateKey(dataAccessManager, userSessionId, transactionId, KEY_TYPE, null);
	}
	
	/*
	 * 1. Checks whether rotateKey() method returns success if unique key is passed for rotation. Here unique key refers to new key which should not 
	 * 	  be among all the previous keys.
	 * 2. Second test case checks that if we try to rotate key again with same key, it should return validation error.
	 */
	@Test
	public void testRotateKey()
	{
		UUID uuid = UUID.randomUUID();
		KeyStatus keyStatus = KeyManager.rotateKey(dataAccessManager, userSessionId, transactionId, KEY_TYPE, uuid.toString());
		assertNotNull(keyStatus.getCode());
		assertTrue("Success should be returned", KeyStatus.SUCCESS.equals(keyStatus));
		
		keyStatus = KeyManager.rotateKey(dataAccessManager, userSessionId, transactionId, KEY_TYPE, uuid.toString());
		assertNotNull(keyStatus.getCode());
		assertTrue("Validation error should be returned", KeyStatus.VALIDATION_ERROR.equals(keyStatus));
	}
	
	/*
	 * getKey() method should throw OneShieldException if key type is null
	 */
	@Test(expected = OneshieldException.class)
	public void testGetKeyForNullKeyType()
	{
		KeyManager.getKey(dataAccessManager, userSessionId, transactionId, null);
	}
	
	/*
	 * 1. Not null value should be returned when we try to get the key for certain key type which is already put in the secret.
	 * 2. Null value should be returned when try to access key for non-existent key type.
	 */
	@Test
	public void testGetKey()
	{
		String returnedKey = KeyManager.getKey(dataAccessManager, userSessionId, transactionId, KEY_TYPE);
		assertNotNull("Not null value should be returned", returnedKey);
		
		UUID uuid = UUID.randomUUID();
		returnedKey = KeyManager.getKey(dataAccessManager, userSessionId, transactionId, uuid.toString());
		assertNull("Null should be returned", returnedKey);
	}
}
