/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandCommandHandler;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test case for {@link CommandCommandHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestCommandCommandHandler extends BasicTestCase {

	public void testCreate() {
		final BooleanFlag called = new BooleanFlag(false);

		Command command = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				called.set(true);
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		CommandCommandHandler commandHandler = CommandCommandHandler.newHandler(command);

		commandHandler.handleCommand(DummyDisplayContext.newInstance(), null, null, Collections.<String, Object> emptyMap());

		assertTrue(called.get());
	}

	public void testCliqueOverride() {
		String applyGroup = CommandHandlerFactory.getInstance().getCliqueGroup("apply");
		assertEquals("additional-edit", applyGroup);
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestCommandCommandHandler.class, CommandHandlerFactory.Module.INSTANCE));
	}

}
