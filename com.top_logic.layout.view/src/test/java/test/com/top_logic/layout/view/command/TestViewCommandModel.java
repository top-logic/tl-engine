/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Tests for {@link ViewCommandModel}.
 */
public class TestViewCommandModel extends TestCase {

	/** The command a {@link ReactButtonControl} receives when clicked. */
	private static final String CLICK = "click";

	/** The tooltip configured for a command whose own tooltip a test inspects. */
	private static final ResKey CONFIGURED_TOOLTIP = ResKey.text("The configured tooltip.");

	/** Reason of {@link #SWITCHABLE_REASON} for any input but {@link #REASON_B_INPUT}. */
	private static final ResKey REASON_A = ResKey.text("Disabled for reason A.");

	/** Reason of {@link #SWITCHABLE_REASON} for the input {@link #REASON_B_INPUT}. */
	private static final ResKey REASON_B = ResKey.text("Disabled for reason B.");

	/** The input {@link #SWITCHABLE_REASON} answers with {@link #REASON_B}. */
	private static final String REASON_B_INPUT = "reasonB";

	/**
	 * Rule that disables the command for every input, but with a reason depending on the input.
	 */
	private static final ViewExecutabilityRule SWITCHABLE_REASON =
		input -> ExecutableState.createDisabledState(REASON_B_INPUT.equals(input) ? REASON_B : REASON_A);

	/**
	 * Tests that the model reactively updates executability when the input channel value changes.
	 */
	public void testReactiveExecutability() {
		ViewChannel channel = new DefaultViewChannel("test");

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);

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
			ViewExecutabilityRule.ALWAYS_EXECUTABLE);
		model.attach(null);

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
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.addStateChangeListener(() -> callCount[0]++);
		model.attach(null);

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
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);

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

	/**
	 * Tests that executeCommand() does not call the command when not executable.
	 */
	public void testExecuteCommandNotExecutable() {
		ViewChannel channel = new DefaultViewChannel("test");
		boolean[] commandCalled = {false};

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				commandCalled[0] = true;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);

		// Channel is null -> not executable -> command should not be called
		HandlerResult result = model.executeCommand(null);
		assertFalse("Command should not be called when not executable", commandCalled[0]);
		assertFalse("A refused command is reported as failure.", result.isSuccess());
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey(), result.getErrorMessage());
	}

	/**
	 * Tests that executeCommand() decides by the input as it is when the command runs, not only by
	 * the state last evaluated: a model that was never attached still reports the initial
	 * executable state, but is refused for the input its rule rejects.
	 */
	public void testExecuteCommandDecidesByTheCurrentInput() {
		ViewChannel channel = new DefaultViewChannel("test");
		boolean[] commandCalled = { false };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				commandCalled[0] = true;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);

		assertTrue("The state of a model never attached is not evaluated.",
			model.getExecutableState().isExecutable());

		HandlerResult result = model.executeCommand(null);
		assertFalse("The rule rejects the current input, so the command must not run.", commandCalled[0]);
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey(), result.getErrorMessage());
	}

	/**
	 * Tests that a command its rule rejects is refused with the rule's reason, both when executed
	 * for an input the caller supplies and in the state the model reports.
	 */
	public void testRejectingRuleReportsItsReason() {
		ViewChannel channel = new DefaultViewChannel("test");
		boolean[] commandCalled = { false };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				commandCalled[0] = true;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, SWITCHABLE_REASON);
		model.attach(null);

		assertEquals("The state carries the rule's reason.", REASON_A,
			model.getExecutableState().getI18NReasonKey());

		HandlerResult result = model.execute(null, REASON_B_INPUT);
		assertFalse("The rejected command must not run.", commandCalled[0]);
		assertFalse(result.isSuccess());
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE, result.getErrorTitle());
		assertEquals("The refusal reports the reason for the input executed.", REASON_B, result.getErrorMessage());
	}

	/**
	 * Tests that clicking a button whose model rejects the command reports the rule's reason, not
	 * the generic one of the disabled button.
	 */
	public void testButtonReportsTheReasonOfItsModel() {
		ViewChannel channel = new DefaultViewChannel("test");
		boolean[] commandCalled = { false };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				commandCalled[0] = true;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, SWITCHABLE_REASON);
		model.attach(null);
		ReactButtonControl button = new ReactButtonControl(createReactContext(), model);

		HandlerResult result = button.executeClientCommand(CLICK, Map.of());

		assertFalse("The rejected command must not run.", commandCalled[0]);
		assertFalse(result.isSuccess());
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals("The refusal reports the rule's reason.", REASON_A, result.getErrorMessage());
	}

	/**
	 * Tests that a button whose model accepts the command runs it.
	 */
	public void testButtonRunsAcceptedCommand() {
		ViewChannel channel = new DefaultViewChannel("test");
		channel.set("someValue");
		Object[] receivedInput = { null };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				receivedInput[0] = input;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);
		ReactButtonControl button = new ReactButtonControl(createReactContext(), model);

		HandlerResult result = button.executeClientCommand(CLICK, Map.of());

		assertTrue(result.isSuccess());
		assertEquals("someValue", receivedInput[0]);
	}

	private static ReactContext createReactContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Tests that executeCommand() calls through to the command when executable.
	 */
	public void testExecuteCommandExecutable() {
		ViewChannel channel = new DefaultViewChannel("test");
		channel.set("someValue");
		Object[] receivedInput = {null};

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> {
				receivedInput[0] = input;
				return HandlerResult.DEFAULT_RESULT;
			},
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);

		// Channel has value -> executable -> command should be called
		HandlerResult result = model.executeCommand(null);
		assertEquals("Command should receive channel value", "someValue", receivedInput[0]);
		assertSame(HandlerResult.DEFAULT_RESULT, result);
	}

	/**
	 * Tests that the tooltip explains why the command is disabled, and falls back to the configured
	 * tooltip while it is executable.
	 */
	public void testDisabledReasonAsTooltip() {
		ViewChannel channel = new DefaultViewChannel("test");

		ViewCommand.Config config = createMinimalConfig();
		config.update(config.descriptor().getProperty(ViewCommand.Config.TOOLTIP), CONFIGURED_TOOLTIP);

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			config, channel, NullInputDisabled.INSTANCE);
		model.attach(null);

		assertEquals("Disabled command explains its reason.",
			Resources.getInstance().getString(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey()),
			model.getTooltip());

		channel.set("someValue");
		assertEquals("Executable command shows its configured tooltip.",
			Resources.getInstance().getString(CONFIGURED_TOOLTIP), model.getTooltip());

		channel.set(null);
		assertEquals("Disabled command explains its reason again.",
			Resources.getInstance().getString(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey()),
			model.getTooltip());
	}

	/**
	 * Tests that a command without a configured tooltip has none while it is executable.
	 */
	public void testNoTooltipConfigured() {
		ViewChannel channel = new DefaultViewChannel("test");
		channel.set("someValue");

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, NullInputDisabled.INSTANCE);
		model.attach(null);

		assertNull("No tooltip configured, none shown.", model.getTooltip());
	}

	/**
	 * Tests that a switch between two disabled states with different reasons is reported.
	 */
	public void testChangedDisabledReason() {
		ViewChannel channel = new DefaultViewChannel("test");
		int[] callCount = { 0 };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, SWITCHABLE_REASON);
		model.addStateChangeListener(() -> callCount[0]++);
		model.attach(null);

		assertEquals(REASON_A, model.getExecutableState().getI18NReasonKey());
		int initialCalls = callCount[0];

		channel.set(REASON_B_INPUT);
		assertEquals("Changed reason must be reported.", initialCalls + 1, callCount[0]);
		assertEquals(REASON_B, model.getExecutableState().getI18NReasonKey());
		assertEquals(Resources.getInstance().getString(REASON_B), model.getTooltip());

		channel.set("anythingElse");
		assertEquals("Back to the first reason.", initialCalls + 2, callCount[0]);
		assertEquals(REASON_A, model.getExecutableState().getI18NReasonKey());
	}

	/**
	 * Tests that an unchanged state is not reported, even when the input changes.
	 */
	public void testUnchangedStateNotReported() {
		ViewChannel channel = new DefaultViewChannel("test");
		int[] callCount = { 0 };

		ViewCommandModel model = new ViewCommandModel(
			(context, input) -> HandlerResult.DEFAULT_RESULT,
			createMinimalConfig(), channel, SWITCHABLE_REASON);
		model.addStateChangeListener(() -> callCount[0]++);
		model.attach(null);

		int initialCalls = callCount[0];

		channel.set("someInput");
		assertEquals("Same reason as before, nothing to report.", initialCalls, callCount[0]);

		model.revalidate();
		assertEquals("Re-evaluating the same state reports nothing.", initialCalls, callCount[0]);
	}

	private ViewCommand.Config createMinimalConfig() {
		return TypedConfiguration.newConfigItem(ViewCommand.Config.class);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the {@link ResourcesModule} the
	 * resolved labels come from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestViewCommandModel.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
