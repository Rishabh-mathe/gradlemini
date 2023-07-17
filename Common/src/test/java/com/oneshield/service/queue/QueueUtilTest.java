package com.oneshield.service.queue;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.oneshield.common.AbstractTestCase;
import com.oneshield.common.constants.SystemAttribute;
import com.oneshield.common.util.EncryptionUtil;
import com.oneshield.keymanager.KeyConstants;
import com.oneshield.keymanager.helper.KeyHelper;

/**
 * Test class to test the ActiveMQ credential lookup test
 * There are two tests. run both independently, running both in one go will result in failure of second test 
 * as OSPropertiesManager is loaded only once.
 * @author smongia
 *
 */
public class QueueUtilTest extends AbstractTestCase
{
	
	private static final String CONSUMER_ACTIVEMQ_UNAME_IN_PROPS = "admin";
	private static final String CONSUMER_ACTIVEMQ_PASS_IN_PROPS = "manager";
	private static final String PRODUCER_ACTIVEMQ_UNAME_IN_PROPS = "admin";
	private static final String PRODUCER_ACTIVEMQ_PASS_IN_PROPS = "manager";
	private static final String CONSUMER_ACTIVEMQ_UNAME_IN_SYS_ATTR = "ConsumerUserName";
	private static final String CONSUMER_ACTIVEMQ_PASS_IN_SYS_ATTR = "ConsumerPass";
	private static final String PRODUCER_ACTIVEMQ_UNAME_IN_SYS_ATTR = "ProducerUserName";
	private static final String PRODUCER_ACTIVEMQ_PASS_IN_SYS_ATTR = "ProducerPass";


	@Test
	public void testActiveMQCredentialsLoadingFromPropertiesFile() 
	{
		// Not defining anything in system properties, fetching from queue properties file
		assertEquals(CONSUMER_ACTIVEMQ_UNAME_IN_PROPS, QueueUtil.getConsumerActiveMQUserName());
		assertEquals(CONSUMER_ACTIVEMQ_PASS_IN_PROPS, QueueUtil.getConsumerActiveMQPass());
		assertEquals(PRODUCER_ACTIVEMQ_UNAME_IN_PROPS, QueueUtil.getProducerActiveMQUserName());
		assertEquals(PRODUCER_ACTIVEMQ_PASS_IN_PROPS, QueueUtil.getProducerActiveMQPass());
	}
	
	@Test
	public void testActiveMQCredentialsLoadingFromSystemPropertiesUnEncrypted() 
	{
		// set values in System attributes
		System.setProperty(SystemAttribute.ACTIVEMQ_CONSUMER_USERNAME, CONSUMER_ACTIVEMQ_UNAME_IN_SYS_ATTR);
		System.setProperty(SystemAttribute.ACTIVEMQ_CONSUMER_PASS, CONSUMER_ACTIVEMQ_PASS_IN_SYS_ATTR);
		System.setProperty(SystemAttribute.ACTIVEMQ_PRODUCER_USERNAME, PRODUCER_ACTIVEMQ_UNAME_IN_SYS_ATTR);
		System.setProperty(SystemAttribute.ACTIVEMQ_PRODUCER_PASS, PRODUCER_ACTIVEMQ_PASS_IN_SYS_ATTR);
		
		assertEquals(CONSUMER_ACTIVEMQ_UNAME_IN_SYS_ATTR, QueueUtil.getConsumerActiveMQUserName());
		assertEquals(CONSUMER_ACTIVEMQ_PASS_IN_SYS_ATTR, QueueUtil.getConsumerActiveMQPass());
		assertEquals(PRODUCER_ACTIVEMQ_UNAME_IN_SYS_ATTR, QueueUtil.getProducerActiveMQUserName());
		assertEquals(PRODUCER_ACTIVEMQ_PASS_IN_SYS_ATTR, QueueUtil.getProducerActiveMQPass());
	}
}
