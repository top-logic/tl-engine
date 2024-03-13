/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.db.schema.properties.DBPropertiesTableSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.TestFlexVersionedDataManager;
import test.com.top_logic.knowledge.service.encryption.pbe.PasswordBasedEncryptionSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.db2.SerializingTransformer.Config;
import com.top_logic.knowledge.service.encryption.SecurityService;
import com.top_logic.knowledge.service.encryption.data.EncryptedFlexDataManager;

/**
 * Test case for {@link EncryptedFlexDataManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestEncryptedFlexDataManagerVersioned extends TestFlexVersionedDataManager {

	@Override
	protected FlexDataManager createFlexDataManager() {
		return new EncryptedFlexDataManager(TypedConfiguration.newConfigItem(Config.class),
			super.createFlexDataManager());
	}

	public static Test suite() {
		return TestEncryptedFlexDataManagerVersioned.encryptedSuite(TestEncryptedFlexDataManagerVersioned.class);
	}

	protected static Test encryptedSuite(Class<? extends AbstractDBKnowledgeBaseTest> testClass) {
		return AbstractDBKnowledgeBaseTest.suite(testClass, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test test = suite;
				test = PasswordBasedEncryptionSetup.setup(test);
				test = ServiceTestSetup.createSetup(test, SecurityService.Module.INSTANCE);
				test = DBPropertiesTableSetup.setup(test);
				return test;
			}
		});
	}

}
