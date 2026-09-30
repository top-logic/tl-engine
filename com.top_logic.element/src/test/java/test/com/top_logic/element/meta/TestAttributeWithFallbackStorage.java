/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link AttributeWithFallbackStorage} on transient objects.
 *
 * @see "Ticket #29304"
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAttributeWithFallbackStorage extends BasicTestCase {

	/**
	 * Tests that an explicitly set value of a fallback attribute is stored in (and read back from)
	 * the underlying storage attribute when the edited object is transient.
	 */
	public void testTransientFallbackAttribute() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			TLModel model = ModelService.getInstance().getModel();
			TLModule module = model.getModule("TestTypes");
			TLType stringType = model.getModule("tl.core").getType("String");

			TLClass type = TLModelUtil.addClass(module, "FallbackTestType");
			TLModelUtil.addProperty(type, "storage", stringType);
			TLModelUtil.addProperty(type, "fallback", stringType);
			TLClassProperty value = TLModelUtil.addProperty(type, "value", stringType);
			value.setAnnotation(fallbackStorage("storage", "fallback"));

			TLObject obj = TransientObjectFactory.INSTANCE.createObject(type);

			// Without an explicit value, the fallback value is used.
			obj.tUpdateByName("fallback", "fallbackValue");
			assertEquals("fallbackValue", obj.tValueByName("value"));
			assertNull(obj.tValueByName("storage"));

			// An explicitly set value must be found again and is stored in the storage attribute.
			obj.tUpdateByName("value", "explicitValue");
			assertEquals("explicitValue", obj.tValueByName("value"));
			assertEquals("explicitValue", obj.tValueByName("storage"));

			// Resetting the explicit value falls back to the fallback value again.
			obj.tUpdateByName("value", null);
			assertEquals("fallbackValue", obj.tValueByName("value"));
			assertNull(obj.tValueByName("storage"));

			// Do not commit: The model change is only needed to set up the fallback storage and is
			// rolled back to not pollute the shared application model.
		}
	}

	private static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
		AttributeWithFallbackStorage.Config<?> storageConfig =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storageConfig.update(storageConfig.descriptor().getProperty("storage-attribute"), storageAttribute);
		storageConfig.update(storageConfig.descriptor().getProperty("fallback-attribute"), fallbackAttribute);

		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storageConfig);
		return annotation;
	}

	public static Test suite() {
		Test t = new TestSuite(TestAttributeWithFallbackStorage.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}

}
