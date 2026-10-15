/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionStep;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.DeleteObjectAction;
import com.top_logic.layout.view.command.ExecuteScriptAction;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.IfAction;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.layout.view.command.WriteChannelAction;

/**
 * Tests the {@link ActionArguments} an action chain runs with: their unchanged flow through the
 * chain, the follow-up actions of an action with a result ({@link WithResultActions}), the
 * selection of the processed value ({@link com.top_logic.layout.view.command.WithValueArgument}),
 * and the {@link ActionScope} the actions are instantiated in.
 */
public class TestActionArguments extends TestCase {

	/** Name of the main argument of the chains under test. */
	private static final String MAIN = "main";

	/** Name of a further argument of the chains under test. */
	private static final String OTHER = "other";

	/** The value of the {@link #MAIN} argument. */
	private static final String MAIN_VALUE = "m";

	/** The value of the {@link #OTHER} argument. */
	private static final String OTHER_VALUE = "o";

	private final List<String> _log = new ArrayList<>();

	private final List<ActionArguments> _seen = new ArrayList<>();

	private final List<Object> _values = new ArrayList<>();

	private final List<Object> _completions = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests that the result of an action does not reach the actions following it.
	 */
	public void testArgumentsFlowPastResult() {
		run(List.of(result("first", "produced"), seeArguments(), seeValue()));

		assertEquals(1, _seen.size());
		assertEquals(List.of(MAIN), _seen.get(0).names());
		assertEquals(List.of(MAIN_VALUE), _values);
		assertEquals("The completion value is the result of the last action.", List.of(MAIN_VALUE), _completions);
	}

	/**
	 * Tests that the follow-up actions see the arguments of the chain and the result under the name
	 * {@link WithResultActions#RESULT}, and that the chain continues with its unchanged arguments.
	 */
	public void testFollowUpsSeeResult() {
		run(List.of(step(result("first", "produced"), WithResultActions.RESULT, seeArguments(), seeValue()),
			seeArguments(), seeValue()));

		ActionArguments inner = _seen.get(0);
		assertEquals(List.of(MAIN, WithResultActions.RESULT), inner.names());
		assertEquals(MAIN_VALUE, inner.get(MAIN));
		assertEquals("produced", inner.get(WithResultActions.RESULT));
		assertEquals("The result is the default value of the follow-up actions.", "produced", _values.get(0));

		ActionArguments outer = _seen.get(1);
		assertEquals(List.of(MAIN), outer.names());
		assertEquals("Behind the follow-up actions, the main argument is the default again.", MAIN_VALUE,
			_values.get(1));
	}

	/**
	 * Tests that the follow-up actions see the result under the name given with
	 * {@link WithResultActions#AS}.
	 */
	public void testFollowUpsSeeResultUnderGivenName() {
		run(List.of(step(result("first", "produced"), "created", seeArguments())));

		assertEquals(List.of(MAIN, "created"), _seen.get(0).names());
		assertEquals("produced", _seen.get(0).get("created"));
		assertEquals("created", _seen.get(0).defaultName());
	}

	/**
	 * Tests that a result named like an argument of the chain shadows that argument.
	 */
	public void testInnerShadowsOuter() {
		run(List.of(step(result("first", "inner"), MAIN, seeArguments(), seeValue()), seeValue()));

		assertEquals(List.of(MAIN), _seen.get(0).names());
		assertEquals("inner", _seen.get(0).get(MAIN));
		assertEquals(List.of("inner", MAIN_VALUE), _values);
	}

	/**
	 * Tests that an action processes the argument selected by name, and the default argument
	 * without a selection.
	 */
	public void testValueSelectsArgument() {
		ActionArguments arguments = ActionArguments.of(MAIN, MAIN_VALUE).with(OTHER, OTHER_VALUE).withDefault(MAIN);

		ViewActionChain.run(context(), List.of(
			new ActionStep(seeValue(), OTHER, WithResultActions.RESULT, List.of()),
			seeValue()), arguments, _completions::add);

		assertEquals(List.of(OTHER_VALUE, MAIN_VALUE), _values);
	}

	/**
	 * Tests that the default value is the innermost result, and the main argument of the trigger
	 * outside of all follow-up actions.
	 */
	public void testDefaultIsInnermostResult() {
		ViewAction inner = step(result("inner", "r2"), "b", seeValue());
		run(List.of(step(result("outer", "r1"), "a", inner, seeValue()), seeValue()));

		assertEquals(List.of("r2", "r1", MAIN_VALUE), _values);
	}

	/**
	 * Tests that the step resumes the chain with the result of its action, after its follow-up
	 * actions ran.
	 */
	public void testStepResultIsResultOfAction() {
		run(List.of(step(result("first", "produced"), WithResultActions.RESULT, result("follow-up", "other"))));

		assertEquals(List.of("first", "follow-up"), _log);
		assertEquals(List.of("produced"), _completions);
	}

	/**
	 * Tests that an abort inside the follow-up actions ends the chain and unwinds the compensations
	 * registered inside the follow-up actions, by the action itself, and before it, newest first.
	 */
	public void testAbortInFollowUpsUnwindsCompensations() {
		run(List.of(compensate("before"),
			step(compensate("action"), WithResultActions.RESULT, compensate("follow-up"), abort()),
			record("after")));

		assertEquals(List.of("follow-up compensated", "action compensated", "before compensated"), _log);
		assertEquals("An aborted chain settles without a value.", Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a compensation registered inside the follow-up actions runs when the chain is
	 * aborted after the step.
	 */
	public void testCompensationInFollowUpsRunsOnLaterAbort() {
		run(List.of(compensate("before"), step(record("action"), WithResultActions.RESULT, compensate("follow-up")),
			abort()));

		assertEquals(List.of("action", "follow-up compensated", "before compensated"), _log);
	}

	/**
	 * Tests that the follow-up actions run once a suspended action resumes, with its late result.
	 */
	public void testFollowUpsAfterSuspendedAction() {
		Suspend suspend = new Suspend();
		run(List.of(step(suspend, WithResultActions.RESULT, seeValue()), seeValue()));

		assertEquals(List.of(), _values);
		assertEquals(List.of(), _completions);

		suspend.resume("late");

		assertEquals(List.of("late", MAIN_VALUE), _values);
		assertEquals(List.of(MAIN_VALUE), _completions);
	}

	/**
	 * Tests that an action sees its destructive follow-up actions in the tone of what follows it.
	 */
	public void testFollowUpsDecideToneOfAction() {
		ButtonTone[] tone = new ButtonTone[1];
		ViewAction probe = new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				tone[0] = continuation.tone();
				continuation.resume(input);
			}
		};
		ViewAction destructive = new ViewAction() {
			@Override
			public Object execute(ReactContext context, Object input) {
				return input;
			}

			@Override
			public ButtonTone getTone() {
				return ButtonTone.DANGER;
			}
		};

		ViewAction step = step(probe, WithResultActions.RESULT, destructive);
		run(List.of(step));

		assertEquals(ButtonTone.DANGER, tone[0]);
		assertEquals("A step is destructive when one of its follow-up actions is.", ButtonTone.DANGER,
			step.getTone());
	}

	/**
	 * Tests that the branches of an {@link IfAction} run with the arguments of the chain.
	 */
	public void testIfPassesArguments() {
		ActionArguments arguments = ActionArguments.of(MAIN, MAIN_VALUE).with(OTHER, OTHER_VALUE);
		IfAction branch = new IfAction((context, input) -> Boolean.TRUE, List.of(seeArguments()), List.of());

		ViewActionChain.run(context(), List.of(branch, seeArguments()), arguments, _completions::add);

		assertSame(arguments, _seen.get(0));
		assertSame(arguments, _seen.get(1));
	}

	/**
	 * Tests that an action with a result accepts follow-up actions as children next to a further
	 * property element, and that every action is instantiated in the scope of its position.
	 */
	public void testParseFollowUps() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestActionArguments.class);
		GenericViewCommand.Config config = parse(context,
			"<generic-command input='draft'>"
				+ "<execute-script function='x -> $x' as='created'>"
				+ "<write-channel name='selection'/>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "<with-transaction>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</with-transaction>"
				+ "</execute-script>"
				+ "<execute-script function='a -> x -> $x'>"
				+ "<inputs><input channel='a'/></inputs>"
				+ "<write-channel name='selection'/>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</execute-script>"
				+ "<write-channel name='other' value='draft'/>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</generic-command>");

		ExecuteScriptAction.Config named = (ExecuteScriptAction.Config) config.getActions().get(0);
		assertEquals("created", named.getAs());
		assertEquals(3, named.getActions().size());
		assertTrue(named.getActions().get(0) instanceof WriteChannelAction.Config);

		ExecuteScriptAction.Config withInputs = (ExecuteScriptAction.Config) config.getActions().get(1);
		assertNull(withInputs.getAs());
		assertEquals(1, withInputs.getInputs().size());
		assertEquals("a", withInputs.getInputs().get(0).getChannelName());
		assertEquals(2, withInputs.getActions().size());

		assertEquals("draft", ((WriteChannelAction.Config) config.getActions().get(2)).getValue());

		assertTrue(context.getInstance(config) instanceof GenericViewCommand);
		context.checkErrors();

		assertEquals(4, ArgumentsProbe.SCOPES.size());
		assertScope(List.of("draft", "created"), "created", ArgumentsProbe.SCOPES.get(0));
		assertScope(List.of("draft", "created"), "created", ArgumentsProbe.SCOPES.get(1));
		assertScope(List.of("draft", WithResultActions.RESULT), WithResultActions.RESULT,
			ArgumentsProbe.SCOPES.get(2));
		assertScope(List.of("draft"), "draft", ArgumentsProbe.SCOPES.get(3));
	}

	/**
	 * Tests that selecting an argument that is not defined at the position of the action is a
	 * configuration error.
	 */
	public void testUnknownValueIsError() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestActionArguments.class);
		GenericViewCommand.Config config = parse(context,
			"<generic-command input='draft'>"
				+ "<" + DeleteObjectAction.Config.TAG_NAME + " value='created'/>"
				+ "</generic-command>");

		context.getInstance(config);

		assertTrue("The argument 'created' is not defined.", context.hasErrors());
	}

	private static void assertScope(List<String> names, String defaultName, ActionScope scope) {
		assertEquals(names, scope.names());
		assertEquals(defaultName, scope.defaultName());
	}

	private static GenericViewCommand.Config parse(DefaultInstantiationContext context, String xml)
			throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(GenericViewCommand.Config.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("generic-command", descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		GenericViewCommand.Config config = (GenericViewCommand.Config) reader.read();
		context.checkErrors();
		return config;
	}

	private void run(List<ViewAction> actions) {
		ViewActionChain.run(context(), actions, ActionArguments.of(MAIN, MAIN_VALUE), _completions::add);
	}

	private static DefaultViewContext context() {
		return new DefaultViewContext(null);
	}

	/** A step running the given action, followed by the given actions over its result. */
	private static ViewAction step(ViewAction action, String resultName, ViewAction... followUps) {
		return new ActionStep(action, null, resultName, List.of(followUps));
	}

	/** An action recording the arguments it runs with, resuming with the default value. */
	private ViewAction seeArguments() {
		return new ArgumentsViewAction() {
			@Override
			public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
				_seen.add(arguments);
				continuation.resume(arguments.value());
			}
		};
	}

	/** An action recording the value it processes, resuming with it. */
	private ViewAction seeValue() {
		return (context, input) -> {
			_values.add(input);
			return input;
		};
	}

	/** An action that logs its name and resumes with the given result. */
	private ViewAction result(String name, Object result) {
		return (context, input) -> {
			_log.add(name);
			return result;
		};
	}

	/** An action that logs its name and resumes without a result. */
	private ViewAction record(String name) {
		return result(name, null);
	}

	/** An action that registers a compensation logging its name. */
	private ViewAction compensate(String name) {
		return new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				continuation.onAbort(() -> _log.add(name + " compensated"));
				continuation.resume(input);
			}
		};
	}

	/** An action that aborts the chain. */
	private static ViewAction abort() {
		return new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				continuation.abort();
			}
		};
	}

	/** An action that holds its continuation until {@link #resume(Object)} is called. */
	private static class Suspend extends InterruptibleViewAction {

		private Continuation _continuation;

		@Override
		public void execute(ReactContext context, Object input, Continuation continuation) {
			_continuation = continuation;
		}

		void resume(Object value) {
			_continuation.resume(value);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestActionArguments.class, TypeIndex.Module.INSTANCE);
	}
}
