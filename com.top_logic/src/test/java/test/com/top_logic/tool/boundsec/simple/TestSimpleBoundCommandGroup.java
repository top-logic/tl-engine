/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.simple;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.I18NConstants;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup.ValueProvider;

/**
 * Test case for {@link SimpleBoundCommandGroup}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSimpleBoundCommandGroup extends BasicTestCase {

	public void testValueProvider() throws ConfigurationException {
		ValueProvider provider = SimpleBoundCommandGroup.ValueProvider.INSTANCE;

		assertSame(SimpleBoundCommandGroup.CREATE, provider.getValue("group", SimpleBoundCommandGroup.CREATE_NAME));
		assertEquals(SimpleBoundCommandGroup.CREATE_NAME, provider.getSpecification(SimpleBoundCommandGroup.CREATE));
	}

	public void testValueProviderErrorOnMissingGroup() {
		String nonExistingCommandGroupName = "notExistingCommandGroup";
		assertNull("Test needs a an undefined command group.",
			CommandGroupRegistry.resolve(nonExistingCommandGroupName));
		ValueProvider provider = SimpleBoundCommandGroup.ValueProvider.INSTANCE;

		try {
			provider.getValue("somePropertyName", nonExistingCommandGroupName);
			fail("Configuration failure: Group " + nonExistingCommandGroupName + " not registered.");
		} catch (ConfigurationException ex) {
			// Expected: Command group does not exist!
			assertEquals(I18NConstants.ERROR_COMMAND_GROUP_NOT_REGISTERED__COMMAND_GROUP, ex.getErrorKey().plain());
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSimpleBoundCommandGroup}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestSimpleBoundCommandGroup.class);
		test = ServiceTestSetup.createSetup(test, CommandGroupRegistry.Module.INSTANCE);
		test = TLTestSetup.createTLTestSetup(test);
		return test;
	}

}
