package com.oneshield.utils.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.oneshield.common.AbstractTestCase;
import com.oneshield.logging.Category;

public class TransitDataEncryptionHelperTest extends AbstractTestCase 
{
	public static final Category cat = Category.getInstance(TransitDataEncryptionHelperTest.class);
	
	@Test
	public void testEncryptionDecryption() {
		
		String plainText = "This is plaintext";
		String encryptedText = TransitDataEncryptionHelper.encryptTransitData(plainText);
		assertEquals(plainText, TransitDataEncryptionHelper.decryptTransitData(encryptedText));		
	}
	
	@Test
	public void testEncryptionDecryption2() {
		
		String text = "This is long Test for testing encryption of with long string of 75 characters.";
		String plainText = new StringBuilder(text).append(text).append(text).append(text).append(text).toString();				
		cat.info("plain text length->" + plainText.length(), Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
		String encryptedText = TransitDataEncryptionHelper.encryptTransitData(plainText);
		cat.info("Encrypted text length->" + encryptedText.length(), Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
		assertEquals(plainText, TransitDataEncryptionHelper.decryptTransitData(encryptedText));		
	}

}
