/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.ConfiguredStringMapping;
import com.top_logic.basic.col.ConfiguredStringMapping.Config;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test for {@link ConfiguredStringMapping}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestConfiguredStringMapping extends BasicTestCase {

	public void testDefaultValue() {
		Config config = TypedConfiguration.newConfigItem(ConfiguredStringMapping.Config.class);
		Mapping<String, String> mapping = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertNull(mapping.map("any input"));

		config.setDefaultValue("defaultValue");
		mapping = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertEquals("defaultValue", mapping.map("any input"));

		config.setNoDefaultValue(true);
		mapping = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertEquals("any input", mapping.map("any input"));
	}

	public void testEmptyConfiguration() {
		Config config = TypedConfiguration.newConfigItem(ConfiguredStringMapping.Config.class);
		Mapping<String, String> mapping = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertNull(mapping.map("any input"));
	}

	public void testSimpleConfiguredStringMapping() {
		Config config = TypedConfiguration.newConfigItem(ConfiguredStringMapping.Config.class);
		Map<String, String> entrySet = new HashMap<>();
		entrySet.put("a-key", "a-value");
		entrySet.put("b-key", "b-value");
		config.setEntrySet(entrySet);
		Mapping<String, String> mapping = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertEquals("a-value", mapping.map("a-key"));
		assertEquals("b-value", mapping.map("b-key"));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestConfiguredStringMapping}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestConfiguredStringMapping.class);
	}

}

