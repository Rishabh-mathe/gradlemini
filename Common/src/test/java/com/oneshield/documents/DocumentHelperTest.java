package com.oneshield.documents;

import com.oneshield.common.AbstractTestCase;
import com.oneshield.common.errorhandling.OneshieldException;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class DocumentHelperTest extends AbstractTestCase
{
    private static final Logger log = Logger.getLogger(DocumentHelperTest.class.getSimpleName());


    @Test
    public void maxFileSize_defaultValue()
    {
        assertTrue(DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES > 0);
    }

    // This test should be executed alone (with other tests disabled)
    // This is because DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES class field is statically initialized during the class loading.
    // @Test
    public void maxFileSize_setViaJavaProperty()
    {
        long limit = 1024*1024*5; // 5M
        System.setProperty(DocumentHelper.PROP_FILE_UPLOAD_MAX_FILE_SIZE_BYTES, String.valueOf(limit));
        assertEquals(limit, DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES);
    }

    @Test
    public void validateFileSize_belowLimit() throws FileNotFoundException
    {
        File file = createTempFileOfSize("below_limit.txt", DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES - 1);
        assertTrue(DocumentHelper.isFileSizeValidForUpload(new FileInputStream(file), "below_limit.txt"));
    }

    @Test
    public void validateFileSize_equalsToLimit() throws FileNotFoundException
    {
        File file = createTempFileOfSize("equals_to_limit.txt", DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES);
        assertTrue(DocumentHelper.isFileSizeValidForUpload(new FileInputStream(file), "equals_to_limit.txt"));
    }

    @Test
    public void validateFileSize_aboveLimit() throws FileNotFoundException
    {
        File file = createTempFileOfSize("above_limit.txt", DocumentHelper.MAX_UPLOAD_FILE_SIZE_BYTES + 1);
        assertFalse(DocumentHelper.isFileSizeValidForUpload(new FileInputStream(file), "above_limit.txt"));
    }

    @Test
    public void validateFileType_allowed() throws FileNotFoundException
    {
        File file = getResourceFile("txt_file.txt");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "txt_file.txt"));

        file = getResourceFile("xml_file.xml");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "xml_file.xml"));

        /*file = getResourceFile("xlsx_file.xlsx");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "xlsx_file.xlsx"));*/
    }

    @Test
    public void validateFileType_allowedFakeExtension() throws FileNotFoundException
    {
        File file = getResourceFile("txt_file.7z");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "txt_file.7z"));

        file = getResourceFile("xml_file.7z");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "xml_file.7z"));

        /*file = getResourceFile("xlsx_file.7z");
        assertTrue(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "xlsx_file.7z"));*/
    }

    @Test
    public void validateFileType_notAllowed() throws FileNotFoundException
    {
        File file = getResourceFile("7z_file.7z");
        assertFalse(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "7z_file.7z"));
    }

    @Test
    public void validateFileType_notAllowedFakeExtension() throws FileNotFoundException
    {
        File file = getResourceFile("7z_file.xml");
        assertFalse(DocumentHelper.isFileTypeValidForUpload(new FileInputStream(file), "7z_file.xml"));
    }


    private File getResourceFile(String fileName) {
        URL url = DocumentHelperTest.class.getClassLoader().getResource(fileName);
        Assert.assertNotNull("Resource not found: " + fileName, url);

        Path path = null;
        try {
            path = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return path.toFile();
    }

    private File createTempFileOfSize(String fileName, long size) {
        File file = null;
        try
        {
            file = File.createTempFile(fileName + "_","");

            try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))
            {
                raf.setLength(size);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        log.info("Created file " + file.getName() + " of size " + file.length());
        return file;
    }
    
    
    @Test
    public void stripPunctuationCharactersFromStringTest()
    {
        assertTrue(DocumentHelper.stripPunctuationCharactersFromString("SampleFile%20N}a+m!e").equals("SampleFile20Name"));
    }
    
    @Test
    public void stripFileNameForSpecialCharsTest()
    {
        assertTrue(DocumentHelper.stripPunctuationCharsFromFileNameWithExtension("SampleFile%20N}a+m!e.pdf").equals("SampleFile20Name.pdf"));
    }
    
    @Test
    public void stripFileNameForSpecialCharsTestWithInvalidFileName()
    {
        try
        {
        	DocumentHelper.stripPunctuationCharsFromFileNameWithExtension("%}+!.pdf");
        	fail();
        }catch(OneshieldException ose) 
        {
        	assertTrue(ose.getMessage().contains(DocumentHelper.INVALID_FILE_NAME_ONLY_PUNCTUATION_CHARS));
        }    	
    }
    
    @Test
    public void stripPunctuationCharsTest()
    {
    	assertFalse(DocumentHelper.stripPunctuationChars());
    }

}
