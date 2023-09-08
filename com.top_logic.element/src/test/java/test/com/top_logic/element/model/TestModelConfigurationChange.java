/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.util.model.ModelService;

/**
 * Test that applying a changed configuration to an exisint in-app model works as expected.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelConfigurationChange extends BasicTestCase {

	public void testModelUpdate() throws ModuleException {
		{
			TLModule module = DynamicModelService.getInstance().getModel().getModule("TestModelConfigurationChange");

			// Check that everything is as described in TestModelConfigurationChange.model.xml
			TLClass A = (TLClass) module.getType("A");
			TLClass B = (TLClass) module.getType("B");
			TLStructuredTypePart ref = A.getPart("ab");
			assertNotNull(ref);
			assertEquals(B, ref.getType());

			// Make an dynamic change to the model that the test can be sure that the exiting in-app
			// model is updated and the DB was not cleared in between.
			try (Transaction tx = module.tHandle().getKnowledgeBase().beginTransaction()) {
				TLI18NKey i18n = TypedConfiguration.newConfigItem(TLI18NKey.class);
				i18n.setValue(ResKey.forTest("dynamic.change"));
				A.setAnnotation(i18n);

				tx.commit();
			}
		}

		// Re-boot with a configuration extension modifying the model with
		// TestModelConfigurationChange.extension.model.xml
		XMLPropertiesConfig newConfig = XMLProperties.Module.INSTANCE.config().clone();
		newConfig.pushAdditionalContent(null,
			ClassRelativeBinaryContent.withSuffix(TestModelConfigurationChange.class, "properties2.config.xml"));
		XMLProperties.restartXMLProperties(newConfig);

		// Re-test the model, create new interaction, since the re-boot has dropped the current one.
		ThreadContextManager.inSystemInteraction(TestModelConfigurationChange.class, () -> {
			TLModule module = DynamicModelService.getInstance().getModel().getModule("TestModelConfigurationChange");

			TLClass A = (TLClass) module.getType("A");
			TLClass B = (TLClass) module.getType("B");
			TLClass C = (TLClass) module.getType("C");
			TLStructuredTypePart ref = A.getPart("ab");
			assertNotNull(B);
			assertNotNull(C);
			assertNotNull(ref);
			assertEquals(C, ref.getType());

			TLI18NKey i18N = A.getAnnotation(TLI18NKey.class);
			assertNotNull(i18N);
			assertEquals("dynamic.change", i18N.getValue().getKey());
		});
	}

	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestModelConfigurationChange.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		String customConfig = CustomPropertiesDecorator.createFileName(TestModelConfigurationChange.class);
		Test test = new CustomPropertiesSetup(kbTest, customConfig, true);
		return TestUtils.doNotMerge(TLTestSetup.createTLTestSetup(test));
	}

}
