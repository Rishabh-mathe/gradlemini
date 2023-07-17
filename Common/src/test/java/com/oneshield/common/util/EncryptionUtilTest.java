package com.oneshield.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;
import com.oneshield.common.AbstractTestCase;
import com.oneshield.common.constants.SystemAttribute;
import com.oneshield.logging.Category;

import junit.framework.Assert;

import static com.oneshield.common.constants.SystemAttribute.*;



/*
 * Test class to test methods under 
 * <code>com.oneshield.common.util.EncryptionUtil.java<code>
 */
public class EncryptionUtilTest extends AbstractTestCase 
{
	
	private static final String PLAIN_TEXT = "MyTestString";
	private static final String SECRET_KEY = "MyTestKey";
	private static final String DATA_AND_IV_SEPERATOR= "OneShield";
	//output from EncryptText.jar
	private static final String DECRYPTED_VAL_AES_CBC = "uOULBi6KJS3fPHB7whYAew==OneShieldFkO1gtlSFq9V4W8VaJ2/Gg==";
	//output from EncryptPlainText_AES-GCM.jar
	private static final String DECRYPTED_VAL_AES_GCM = "aTGd2Q3OepnM2Om5etxX9cE7+iO32CEamoZYEg==OneShieldVKYZ7Kws2sBpBnyh";

	private static final String ORIGINAL_VALUE = "This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit keyABCDEFGHIJ1234567890@#$%^&*(";
	private static final String ORIGINAL_LARGE_VALUE = "This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit keyABCDEFGHIJ1234567890@#$%^&*(. This is very large value.";
	private static final String SECRET_KEY_256_BITS = "OneShield1AES256OneShield1AES256";
	protected static final Category logger = Category.getInstance(EncryptionUtilTest.class);

	@Test
	public void testEncryptionUsingAESCBC() 
	{		
		System.setProperty(SYSTEM_ATTR_ENCRYPTION_ALGORITHM, SYSTEM_ATTR_ENCRYPTION_ALGORITHM_AES_CBC);
		String encryptedString = EncryptionUtil.encryptStringUsingAES(PLAIN_TEXT, SECRET_KEY);		
		int ind = encryptedString.indexOf(DATA_AND_IV_SEPERATOR);  		
		assertEquals(PLAIN_TEXT, EncryptionUtil.decryptString(encryptedString.substring(0,ind), encryptedString.substring(ind+9), SECRET_KEY));
	}
	
	@Test
	public void testEncryptionUsingAESGCM() 
	{
		System.setProperty(SYSTEM_ATTR_ENCRYPTION_ALGORITHM, SYSTEM_ATTR_ENCRYPTION_ALGORITHM_AES_GCM);
		String encryptedString = EncryptionUtil.encryptStringUsingAES(PLAIN_TEXT, SECRET_KEY);		
		int ind = encryptedString.indexOf(DATA_AND_IV_SEPERATOR);  		
		assertEquals(PLAIN_TEXT, EncryptionUtil.decryptString(encryptedString.substring(0,ind), encryptedString.substring(ind+9), SECRET_KEY));
	}
	
	@Test
	public void testDecryptionUsingAESCBC() 
	{
		System.setProperty(SYSTEM_ATTR_ENCRYPTION_ALGORITHM, SYSTEM_ATTR_ENCRYPTION_ALGORITHM_AES_CBC);
		int ind = DECRYPTED_VAL_AES_CBC.indexOf(DATA_AND_IV_SEPERATOR);  		
		assertEquals(PLAIN_TEXT, EncryptionUtil.decryptString(DECRYPTED_VAL_AES_CBC.substring(0,ind), DECRYPTED_VAL_AES_CBC.substring(ind+9), SECRET_KEY));
	}
	
	@Test
	public void testDecryptionUsingAESGCM() 
	{
		System.setProperty(SYSTEM_ATTR_ENCRYPTION_ALGORITHM, SYSTEM_ATTR_ENCRYPTION_ALGORITHM_AES_GCM);
		int ind = DECRYPTED_VAL_AES_GCM.indexOf(DATA_AND_IV_SEPERATOR);  		
		assertEquals(PLAIN_TEXT, EncryptionUtil.decryptString(DECRYPTED_VAL_AES_GCM.substring(0,ind), DECRYPTED_VAL_AES_GCM.substring(ind+9), SECRET_KEY));
	}
	
	@Test
	public void testAES256EncryptionDecryptionWithHex() {

		try {

			String encryptedValue = EncryptionUtil.encryptAES256ReturnHexString(ORIGINAL_VALUE, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			String decryptedValue = EncryptionUtil.decryptAES256(encryptedValue, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			assertEquals(ORIGINAL_VALUE, decryptedValue);

		} catch (Exception e) {
			logger.error("Exception in testAES256EncryptionDecryptionWithHex", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}
	}

	/**
	 * Stress test to verify that encrypted value for a given input is always
	 * different no matter how many times you do it. Stress test to verify that
	 * decrypted value for a given input is always matches with original input no
	 * matter how many times you do it.
	 */
	@Test
	public void stressTestAES256EncryptionDecryptionWithHex() 
	{
		try {

			String previousIterationEncryptedValue = null;
			String previousIterationDecryptedValue = null;
			logger.info("Start stress test", Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);	
			for (int i = 0; i < 1000; i++) {
				String encryptedValue = EncryptionUtil.encryptAES256ReturnHexString(ORIGINAL_VALUE, SECRET_KEY_256_BITS,
						Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
				if (previousIterationEncryptedValue != null) {
					assertNotSame(previousIterationEncryptedValue, encryptedValue);
				}
				previousIterationEncryptedValue = encryptedValue;
				String decryptedValue = EncryptionUtil.decryptAES256(encryptedValue, SECRET_KEY_256_BITS,
						Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
				if (previousIterationDecryptedValue != null) {
					assertEquals(previousIterationDecryptedValue, decryptedValue);
				}
				previousIterationDecryptedValue = decryptedValue;
				assertEquals(ORIGINAL_VALUE, decryptedValue);
			}

			logger.info("End stress test", Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);			

		} catch (Exception e) {
			logger.error("Exception in stressTestAES256EncryptionDecryptionWithHex", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}
	}

	/**
	 * This method tests the encryption of Strings in raw_strings.txt file sitting under resources
	 */
	@Test
	public void testAES256EncryptValuesFromInputFile() 
	{		
		logger.info("Start encryption test on inputs from file", Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);	
		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(getResourceFilePath("raw_strings.txt"))) {

			stream.forEach(EncryptionUtilTest::testAES256EncryptionDecryptionWithHex);

		} catch (IOException e) {
			logger.error("Exception in testAES256EncryptValuesFromInputFile", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}
		logger.info("End encryption test on inputs from file", Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);	
	}
	
	private static void testAES256EncryptionDecryptionWithHex(String input) 
	{
		try {
			String encryptedValue = EncryptionUtil.encryptAES256ReturnHexString(input, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID,SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);			
			String decryptedValue = EncryptionUtil.decryptAES256(encryptedValue, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			logger.info("Input is## " + input + " encryptedValue is## " + encryptedValue + " ## and decrypted value is ## " + decryptedValue, Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			assertEquals(input, decryptedValue);
		} catch (Exception e) {
			logger.error("Exception in testAES256EncryptionDecryptionWithHex", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}
	}
	
	private Path getResourceFilePath(String fileName) 
	{
		URL url = EncryptionUtilTest.class.getClassLoader().getResource(fileName);
		Assert.assertNotNull("Resource not found: " + fileName, url);

		Path path = null;
		try {
			path = Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			logger.error("Exception in getResourceFilePath", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
		}
		return path;
	}
	
	@Test
	public void testAES256EncryptionDecryptionWithHexOnLargeString() 
	{
		try {

			String encryptedValue = EncryptionUtil.encryptAES256ReturnHexString(ORIGINAL_LARGE_VALUE, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			String decryptedValue = EncryptionUtil.decryptAES256(encryptedValue, SECRET_KEY_256_BITS,
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			assertEquals(ORIGINAL_LARGE_VALUE, decryptedValue);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in testAES256EncryptionDecryptionWithHexOnLargeString", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}
	}
	
	//Unable to run this as this needs DataSource initialized which is not initialized in parent test.	
	@Test
	public void testEncryptBvValueUsingAES256() {
		try {

			String encryptedValue = EncryptionUtil.encryptBvValueUsingAES256(ORIGINAL_VALUE, 
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			String decryptedValue = EncryptionUtil.decryptBvValueUsingAES256(encryptedValue, 
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256);
			assertEquals(ORIGINAL_VALUE, decryptedValue);

		} catch (Exception e) {
			logger.error("Exception in testAES256EncryptionDecryptionWithHex", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}

	}
	
	@Test
	public void testEncryptBvValueUsingAES256RDS() {
		try {
			// Encrypt then decrypt to verify the RDS version of AES256 works round trip.
			String encryptedValue = EncryptionUtil.encryptBvValueUsingAES256(ORIGINAL_VALUE, 
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256_RDS);
			String decryptedValue = EncryptionUtil.decryptBvValueUsingAES256(encryptedValue, 
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256_RDS);
			assertEquals(ORIGINAL_VALUE, decryptedValue);

		} catch (Exception e) {
			logger.error("Exception in testAES256EncryptionDecryptionWithHex", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}

	}
	
	@Test
	public void testEncryptBvValueRDSDbCheck() {
		try {
			// Below encrypted hex string was encrypted on DB side with same key with initial string also below in the assertEquals 
			String decryptedValue = EncryptionUtil.decryptBvValueUsingAES256(
					"D6A54117FF5542F457BFB17A7D7211764380CE78A4C7E762F4FE87E6CAACB7000B79BA3E6D08041C63FB728614A895279CC62F182CF687ECFCD771C8AFA07432BB59D2122535FFD6FD0E2A6EBEFD938E610F6C0BC3AFCFC34E91CBFDB21CB4404D674775D42C86F059445BDC5A7D507AE91F8B68D775ED36ECCA707CE482BF2BE332385A7E65F6773A7E5A6042F97F3BB882DAA310C43F8020F9B7785DBED7928F3C4867723DB776C38A25BAAFD4EBC83224D3B9D3B9FEEF23A704215ECEA44A1F17590DD14AC82B355142FFCD2691D600826F8DC5A710240F0DBB059641A3E8E5851836751C655CB8010A69E60D12FA", 
					Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID, SystemAttribute.BV_ENCRYPTION_ALGORITHM_AES256_RDS);
			assertEquals("This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit key.This is my test value to see encryption with AES256 with 256 bit key123456789012345", decryptedValue);

		} catch (Exception e) {
			logger.error("Exception in testEncryptBvValueRDSDbCheck", e,Category.NO_SESSION_ID, Category.NO_TRANSACTION_ID);
			fail();
		}

	}

}
