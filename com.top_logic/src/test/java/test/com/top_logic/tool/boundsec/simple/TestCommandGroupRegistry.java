/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.simple;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupType;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * {@link TestCase} for {@link CommandGroupRegistry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCommandGroupRegistry extends BasicTestCase {

	public void testRegisterered() {
		checkCommandGroup("TestCommandGroupRegistry_write1", CommandGroupType.WRITE);
		checkCommandGroup("TestCommandGroupRegistry_read1", CommandGroupType.READ);
		checkCommandGroup("TestCommandGroupRegistry_delete1", CommandGroupType.DELETE);
	}

	private void checkCommandGroup(String id, CommandGroupType commandType) {
		BoundCommandGroup group = CommandGroupRegistry.resolve(id);
		assertNotNull(group);
		assertEquals(id, group.getID());
		assertEquals(commandType, group.getCommandType());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCommandGroupRegistry}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestCommandGroupRegistry.class);
		test = ServiceTestSetup.createSetup(test, CommandGroupRegistry.Module.INSTANCE);
		test = TLTestSetup.createTLTestSetup(test);
		return test;
	}

}

