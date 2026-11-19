/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import static test.com.top_logic.layout.view.command.OperationModes.*;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.designer.OpenDesignerCommand;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that {@link OpenDesignerCommand} is available in development only, for every user: in a
 * production install it is hidden and refuses to open the designer, for administrators as well.
 */
public class TestOpenDesignerCommand extends AbstractModelAccessTest {

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	/** In production, the designer is hidden for an administrator and cannot be opened. */
	public void testProductionHidesForRoot() {
		becomeUser(_root);
		assertTrue("Precondition: the user is an administrator.", TLContext.isAdmin());

		inMode(OperationMode.PRODUCTION, this::assertRefused);
	}

	/** In production, the designer is hidden for a user without administrator rights. */
	public void testProductionHidesForUser() {
		becomeUser(_roleless);
		assertFalse("Precondition: the user is no administrator.", TLContext.isAdmin());

		inMode(OperationMode.PRODUCTION, this::assertRefused);
	}

	/** In development, the designer is executable for a logged-in user. */
	public void testDevelopmentOffersToUser() {
		assertOffered(_roleless);
		assertOffered(_root);
	}

	private void assertOffered(Person user) {
		becomeUser(user);
		inMode(OperationMode.DEVELOPMENT, () -> {
			ExecutableState state = rule(createCommand()).isExecutable(null);
			assertTrue("Executable for " + user.getName() + ".", state.isExecutable());
		});
	}

	private void assertRefused() {
		OpenDesignerCommand command = createCommand();
		ExecutableState state = rule(command).isExecutable(null);
		assertTrue("The command is hidden.", state.isHidden());
		assertFalse(state.isExecutable());

		ViewCommandModel model = new ViewCommandModel(null, command,
			TypedConfiguration.newConfigItem(OpenDesignerCommand.Config.class), null, rule(command));
		try {
			model.execute(null);
			fail("The command model must refuse to open the designer.");
		} catch (TopLogicException ex) {
			// Expected: the rule of the command refuses it.
		}

		try {
			command.execute(newContext(), null);
			fail("The command must refuse to open the designer when called directly.");
		} catch (TopLogicException ex) {
			// Expected: the command checks its rule itself.
		}
	}

	/** The rule deciding over the command where it is placed without configured rules. */
	private static ViewExecutabilityRule rule(OpenDesignerCommand command) {
		return ViewExecutabilityRules.withIntrinsicRule(null, command, ViewExecutabilityRule.ALWAYS_EXECUTABLE);
	}

	private static OpenDesignerCommand createCommand() {
		OpenDesignerCommand.Config config = TypedConfiguration.newConfigItem(OpenDesignerCommand.Config.class);
		return (OpenDesignerCommand) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static ReactContext newContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Test suite with the users of {@link AbstractModelAccessTest} and the
	 * {@link OperationModeService} the command's rule asks.
	 */
	public static Test suite() {
		return suiteWith(TestOpenDesignerCommand.class, OperationModeService.Module.INSTANCE);
	}
}
