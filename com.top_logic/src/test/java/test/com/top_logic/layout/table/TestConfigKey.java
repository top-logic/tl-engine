/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import junit.framework.TestCase;

import com.top_logic.layout.table.ConfigKey;

/**
 * Test of {@link ConfigKey}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigKey extends TestCase {

	public void testDerivedKey() {
		assertEquals(null, ConfigKey.derived(ConfigKey.none(), "AnyUnusedSuffix").get());
		assertEquals(null, ConfigKey.derived(new TestingConfigKey(null), "AnyUnusedSuffix").get());
		assertEquals("concatenatedValue", ConfigKey.derived(new TestingConfigKey("concatenated"), "Value").get());
		assertEquals("emptySuffix", ConfigKey.derived(new TestingConfigKey("emptySuffix"), "").get());
	}

}

