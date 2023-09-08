/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.ExtIDAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Test for storage of {@link ExtID}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM internally.")
public class TestExtIDPersistency extends AbstractDBKnowledgeBaseTest {

	private static final String EXT_ID_ROW_ATTR = "extRowAttr";

	private static final String EXT_ID_FLEX_ATTR = "extFlexAttr";

	private static final String TYPE_WITH_EXT_ID = "TestType";

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = (DBKnowledgeBaseTestSetup) super.createSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(TYPE_WITH_EXT_ID);
				{
					type.setSuperclass((MOClass) typeRepository.getType(KnowledgeBaseTestScenarioConstants.B_NAME));
					type.addAttribute(newExternalAttribute(EXT_ID_ROW_ATTR));
				}
				typeRepository.addMetaObject(type);
			}

			private MOAttribute newExternalAttribute(String attrName) {
				ExtIDAttribute.Config extAttrConfig = TypedConfiguration.newConfigItem(ExtIDAttribute.Config.class);
				ConfigurationDescriptor descriptor = extAttrConfig.descriptor();
				extAttrConfig.update(descriptor.getProperty(AttributeConfig.ATTRIBUTE_NAME_KEY), attrName);
				extAttrConfig.update(descriptor.getProperty(DOXMLConstants.MANDATORY_ATTRIBUTE), false);
				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(extAttrConfig);
			}
		});
		return setup;
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestFlexAttr() throws DataObjectException {
		testExtId(EXT_ID_FLEX_ATTR);
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestRowAttr() throws DataObjectException {
		testExtId(EXT_ID_ROW_ATTR);
	}

	private void testExtId(String attrName) throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newItem();
		assertNull(x.getAttributeValue(attrName));
		ObjectKey xId = x.tId();
		x = null;
		KnowledgeObject y = newItem();
		y.setAttributeValue(attrName, new ExtID(15, 654));
		assertEquals(new ExtID(15, 654), y.getAttributeValue(attrName));
		ObjectKey yId = y.tId();
		y = null;
		tx.commit();

		// ensure objects are refetched
		provokeOutOfMemory();

		assertNull(kb().resolveCachedObjectKey(xId));
		x = (KnowledgeObject) kb().resolveObjectKey(xId);
		assertNull(kb().resolveCachedObjectKey(yId));
		y = (KnowledgeObject) kb().resolveObjectKey(yId);
		

		assertNull(x.getAttributeValue(attrName));
		assertEquals(new ExtID(15, 654), y.getAttributeValue(attrName));
	}

	private KnowledgeObject newItem() throws DataObjectException {
		KnowledgeObject item = kb().createKnowledgeObject(TYPE_WITH_EXT_ID);
		item.setAttributeValue(A1_NAME, "a1");
		return item;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestExtIDPersistency}.
	 */
	public static Test suite() {
		return AbstractDBKnowledgeBaseTest.suite(TestExtIDPersistency.class);
	}

}
