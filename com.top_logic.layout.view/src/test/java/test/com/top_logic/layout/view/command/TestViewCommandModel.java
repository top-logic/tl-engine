/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link ViewCommandModel}.
 */
public class TestViewCommandModel extends TestCase {

	/**
	 * Tests that the model reactively updates executability when the input channel value changes.
	 */
	public void testReactiveExecutability() {
		ViewChannel channel = new DefaultViewChannel("test");

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE, null);
		model.attach();

		// Initially null input -> disabled
		assertFalse("Should be disabled with null input",
			model.getExecutableState().isExecutable());

		// Set value -> enabled
		channel.set("someValue");
		assertTrue("Should be enabled with non-null input",
			model.getExecutableState().isExecutable());

		// Set back to null -> disabled
		channel.set(null);
		assertFalse("Should be disabled again",
			model.getExecutableState().isExecutable());
	}

	/**
	 * Tests that a model without an input channel always resolves {@code null} input.
	 */
	public void testNoInputChannel() {
		// null input channel -> always resolves null
		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), null,
			input -> ExecutableState.EXECUTABLE, null);
		model.attach();

		assertNull("Should resolve null without channel", model.resolveInput());
		assertTrue("Should be executable", model.getExecutableState().isExecutable());
	}

	/**
	 * Tests that the state change listener is notified when executability changes.
	 */
	public void testStateChangeListener() {
		ViewChannel channel = new DefaultViewChannel("test");
		int[] callCount = {0};

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE, null);
		model.setStateChangeListener(() -> callCount[0]++);
		model.attach();

		// Initial attach fires listener because initial state was EXECUTABLE, but after
		// evaluation with null input it changes to NO_EXEC_NO_MODEL
		int initialCalls = callCount[0];

		channel.set("value");
		assertEquals("Listener should fire on state change", initialCalls + 1, callCount[0]);

		// Setting same value shouldn't fire (channel dedup)
		channel.set("value");
		assertEquals("No change, no fire", initialCalls + 1, callCount[0]);

		// Setting different value but same executability (still executable) shouldn't fire
		channel.set("otherValue");
		assertEquals("Still executable, no fire", initialCalls + 1, callCount[0]);
	}

	/**
	 * Tests that detaching stops the model from receiving channel updates.
	 */
	public void testDetach() {
		ViewChannel channel = new DefaultViewChannel("test");

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE, null);
		model.attach();

		// Set value -> enabled
		channel.set("value");
		assertTrue(model.getExecutableState().isExecutable());

		model.detach();

		// After detach, channel changes shouldn't update the model
		channel.set(null);
		// Model still shows old state because it's detached
		assertTrue("Should still show old state after detach",
			model.getExecutableState().isExecutable());
	}

	private ViewCommand.Config createMinimalConfig() {
		return TypedConfiguration.newConfigItem(ViewCommand.Config.class);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewCommandModel.class, TypeIndex.Module.INSTANCE);
	}
}
