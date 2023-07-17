package com.oneshield.common.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.oneshield.common.TestUtils;
import com.oneshield.logging.Category;

public class PgpEncryptUtilTest {

	String inFileName = "TestFile.txt";
	String encryptOutputFileName = inFileName + ".pgp";
	String decryptOutputFileName = inFileName + "_decrypted";
	String publicKeyFileName = "Publickey.gpg";
	String privateKeyFileName = "Privatekey.gpg";	
	
	protected static final Category cat = Category.getInstance(TestUtils.JUNIT_LOG_CAT_NAME);
	
	char[] chars = "test".toCharArray();
	
	@Test
	public void test()
	{
		this.testEncryptFile();
		this.testDecryptFile();
		verifyEncryptDecrypt();
	}
	
	private void testEncryptFile()
	{
		File archFile = null;
		
		PGPEncryptUtilRequest request = new PGPEncryptUtilRequestBuilder()
				//.setArchiveName(getResourceFileAbsolutePath(encryptOutputFileName))
				.setArchiveName(encryptOutputFileName)
				.setUnencryptedFileName(getResourceFileAbsolutePath(inFileName))
				.setPublicKeyFileName(getResourceFileAbsolutePath(publicKeyFileName))
				.setArmor(true)
				.setIntegrityCheck(true)
				.build();
		
		archFile = PGPEncryptUtil.encryptFile(request);
		
		assertNotNull(archFile);
		
		String fileName = archFile.getAbsolutePath();
		
		assertNotNull(archFile);
		
		System.out.print("Created encrypted file: " + fileName);

	}

	private void testDecryptFile()
	{
		PGPEncryptUtilRequest request = new PGPEncryptUtilRequestBuilder()
				.setArchiveName(encryptOutputFileName)
				.setUnencryptedFileName(decryptOutputFileName)
				.setPrivateKeyFileName(getResourceFileAbsolutePath(privateKeyFileName))
				.setPrivateKeyPassword(chars)
				.build();
		
		try 
		{
			PGPEncryptUtil.decryptFile(request);
		} 
		catch (NoSuchProviderException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		File archFile = new File(decryptOutputFileName);
		
		assertNotNull(archFile);
		
		String fileName = archFile.getAbsolutePath();
		
		assertNotNull(archFile);
		
		System.out.print("Created decrypted file: " + fileName);

	}
	
	private void verifyEncryptDecrypt()
	{
		
		try 
		{
			assertTrue(FileUtils.contentEquals(new File(getResourceFileAbsolutePath(inFileName)), new File(decryptOutputFileName)));
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Get Absolute path of file as need by PGPEncryptUtil
	 * @param fileName
	 * @return
	 */
	private String getResourceFileAbsolutePath(String fileName)
	{
	    	
	        URL url = PgpEncryptUtilTest.class.getClassLoader().getResource("resources/" + fileName);
	        Assert.assertNotNull("Resource not found: " + fileName, url);
	        
	        Path path = null;
	        try
	        {
	            path = Paths.get(url.toURI());
	        } 
	        catch (URISyntaxException e)
	        {
	            e.printStackTrace();
	        }
	        return path != null ? path.toString() : null;
	}
	
	@After
	public void tearDown() 
	{
		try 
		{
			
			File f = new File(encryptOutputFileName);
			if(f.exists())
				Files.delete(f.toPath());
	
			f = new File(decryptOutputFileName);
			if(f.exists())
				Files.delete(f.toPath());
		
		} 
		catch(IOException ioe) 
		{
			// do nothing
		}
	}

}
