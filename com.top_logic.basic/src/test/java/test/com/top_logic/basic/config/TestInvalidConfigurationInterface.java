/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for error reporting upon invalid configuration interface declarations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestInvalidConfigurationInterface extends TestCase {

	@NoImplementationClassGeneration
	public interface SetterWithoutGetter extends ConfigurationItem {
		int getFoo();

		void setBar(int value);
	}

	@NoImplementationClassGeneration
	public interface SetterWithoutGetterBase extends ConfigurationItem {
		void setBar(int value);
	}

	@NoImplementationClassGeneration
	public interface SetterWithoutGetterInherited extends SetterWithoutGetterBase {
		int getBar();
	}

	public void testSetterWithoutGetter() {
		try {
			TypedConfiguration.getConfigurationDescriptor(SetterWithoutGetter.class);
			fail("Must not load invalid configuration interface.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			assertContains("must have a getter", ex.getMessage());
			assertContainsOnce(SetterWithoutGetter.class.getName(), ex.getMessage());
		}
	}

	public void testSetterWithoutGetterInherited() {
		try {
			TypedConfiguration.getConfigurationDescriptor(SetterWithoutGetterInherited.class);
			fail("Must not load invalid configuration interface.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			assertContains("must have a getter", ex.getMessage());
			assertContainsOnce(SetterWithoutGetter.class.getName(), ex.getMessage());
		}
	}

	private void assertContainsOnce(String name, String message) {
		assertEquals("Duplicate occurrence of <<<" + name + ">>> in <<<" + message + ">>>.", message.length() - name.length(), message.replace(name, "").length());
	}

}
