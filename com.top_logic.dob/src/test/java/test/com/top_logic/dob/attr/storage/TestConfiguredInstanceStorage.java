/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.attr.storage;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.TestConfiguredInstance;
import test.com.top_logic.basic.config.TestConfiguredInstance.A.AConfig;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.storage.ConfiguredInstanceStorage;
import com.top_logic.dob.attr.storage.ConfiguredInstanceStorage.Config;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOClassImpl;

/**
 * Tests {@link ConfiguredInstanceStorage}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestConfiguredInstanceStorage extends BasicTestCase {
	
	public class InstanceClass1 {

	}

	public class InstanceClass2 {

	}

	private MOAttributeImpl newAttribute(Class<?> instanceClass) {
		MOClassImpl type = new MOClassImpl("class");
		MOAttributeImpl attr = new MOAttributeImpl("attr", MOPrimitive.STRING);
		ConfiguredInstanceStorage storage = newStorage(instanceClass);
		attr.setStorage(storage);
		try {
			type.addAttribute(attr);
		} catch (DuplicateAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
		type.freeze();
		return attr;
	}

	private ConfiguredInstanceStorage newStorage(Class<?> instanceClass) {
		Config config = TypedConfiguration.newConfigItem(ConfiguredInstanceStorage.Config.class);
		config.setInstanceClass(instanceClass);
		ConfiguredInstanceStorage storage;
		try {
			storage = new ConfiguredInstanceStorage(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		} catch (ConfigurationException ex1) {
			throw new ConfigurationError("Configuration incorrect.", ex1);
		}
		return storage;
	}

	public void testSameValue() {
		MOAttributeImpl attribute = newAttribute(Object.class);
		AttributeStorage storage = attribute.getStorage();
		
		AConfig newConfigItem = TypedConfiguration.newConfigItem(TestConfiguredInstance.A1.AConfig.class);
		TestConfiguredInstance.A1 a1 =
			new TestConfiguredInstance.A1(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, newConfigItem);
		TestConfiguredInstance.A1 a1Copy =
			new TestConfiguredInstance.A1(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				TypedConfiguration.copy(newConfigItem));

		assertTrue(storage.sameValue(attribute, null, null));
		assertFalse(storage.sameValue(attribute, null, a1));
		assertFalse(storage.sameValue(attribute, a1, null));
		assertTrue(storage.sameValue(attribute, a1, a1));
		assertTrue(storage.sameValue(attribute, a1, a1Copy));
		
		assertTrue(storage.sameValue(attribute, new InstanceClass1(), new InstanceClass1()));
		assertFalse(storage.sameValue(attribute, new InstanceClass1(), new InstanceClass2()));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestConfiguredInstanceStorage}.
	 */
	public static Test suite() {
		return DOBTestSetup.createDOBTestSetup(new TestSuite(TestConfiguredInstanceStorage.class));
	}

}

