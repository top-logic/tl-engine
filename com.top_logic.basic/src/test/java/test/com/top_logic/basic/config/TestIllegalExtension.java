/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Tests illegal subclassing of ConfigurationItems 
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestIllegalExtension extends TestCase {
	
	public interface Config extends ConfigurationItem {
		
		@Name("identifer")
		String getString();
	}
	
	@NoImplementationClassGeneration
	public interface IllegalRenaming extends Config {
		
		@Override
		@Name("id")
		String getString();
	}
	
	public void testIllegalRenaming() {
		try {
			TypedConfiguration.newConfigItem(IllegalRenaming.class);
			fail("Must not be able to rename properties.");
		} catch(RuntimeException ex) {
			// expected
			BasicTestCase.assertContains("Has already a configuration name", ex.getLocalizedMessage());
		}
	}
	
	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestIllegalExtension.class);
	}

}

