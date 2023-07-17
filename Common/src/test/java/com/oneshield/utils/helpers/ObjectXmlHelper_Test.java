package com.oneshield.utils.helpers;

import java.io.File;

import org.junit.Test;
import org.w3c.dom.Document;

import com.oneshield.common.TextFileUtility;
import com.oneshield.common.objects.node.ObjectNode;
import com.oneshield.logging.Category;
import com.oneshield.test.framework.DragonTestCaseBase;


public class ObjectXmlHelper_Test extends DragonTestCaseBase {

	
	
	public static Category cat = Category.getInstance(ObjectXmlHelper_Test.class);
	
	
	public ObjectXmlHelper_Test(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	protected void setUpX() {
		

	}

	@Test
	public void testGetBvFromNode() throws Exception
	{
		Document objDom = this.readXmlData(testDataInputFolder, "getBvFromNode.xml");
		
		ObjectNode objectTree = ObjectXmlHelper.objectTreeFromElement(objDom.getDocumentElement(),true);
		
		//String str = ObjectXmlHelper.objectNodeToXmlText(tree);
	
		//TextFileUtility.writeStringToFile("/logs/getBvFromNodeOut.xml", str);
		
		ObjectNode parNode = ObjectXmlHelper.getNodeByAttribute(objectTree,ObjectNode.TYPE_ID,"310",true,0);
		
		assertNotNull(parNode);
		
		ObjectNode bv = ObjectXmlHelper.getBvFromNode(parNode, "29439902");
	
		assertNotNull(bv);
		
		assertEquals("101", bv.getValue());
	}
}
