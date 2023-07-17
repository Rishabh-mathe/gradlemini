package com.oneshield.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.oneshield.common.AbstractTestCase;
import com.oneshield.logging.Category;

public class OSPropertiesManagerTest extends AbstractTestCase {
	
	protected static final Category logger = Category.getInstance(OSPropertiesManagerTest.class);	
	@Test
	public void testPropertyLoading()
	{
		OSPropertiesManager osPropertiesManager = OSPropertiesManager.getInstance();
		logger.info("KEY is->" + osPropertiesManager.getEncryptionKey());
		assertEquals(5, osPropertiesManager.getEncryptedPropertiesNames().length);
		//fetch an encrypted property
		assertEquals("dapuser", osPropertiesManager.getProperty("gateway_username"));
		//fetch an plain text property
		assertEquals("super", osPropertiesManager.getProperty("dragon.userid"));
	}

}
