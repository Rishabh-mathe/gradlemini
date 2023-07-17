package com.oneshield.common.xml.transform;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class DragonXmlTransformer_Test {

	@Test
	public void testEncodeXmlSpecialCharacters() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Method m = DragonXmlTransformer.class
				.getDeclaredMethod("encodeXmlSpecialCharacters", String.class);
		m.setAccessible(true);

		assertTrue("2020".equals((String) m.invoke(null, "20?20")));
	}
}
