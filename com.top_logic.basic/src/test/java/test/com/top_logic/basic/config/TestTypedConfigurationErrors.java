/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for error detection in broken {@link ConfigurationItem} definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationErrors extends TestCase {

	@NoImplementationClassGeneration
	public interface A extends ConfigurationItem {
		void setFoo(String value);
	}

	public void testPropertyWithoutGetter() {
		try {
			TypedConfiguration.newConfigItem(A.class);
			fail("Must not instantiate.");
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("must have a getter", ex.getMessage());
		} catch (AssertionError ex) {
			BasicTestCase.assertContains("If there is no getter a super property must exists", ex.getMessage());
		}
	}

}
