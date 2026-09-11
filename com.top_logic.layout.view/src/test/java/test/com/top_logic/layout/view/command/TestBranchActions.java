/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ExecuteScriptAction;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.IfAction;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.SwitchAction;
import com.top_logic.layout.view.command.SwitchAction.CaseConfig;
import com.top_logic.layout.view.command.SwitchAction.SwitchCase;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Tests for {@link IfAction} and {@link SwitchAction}, the branches of an action chain.
 */
public class TestBranchActions extends TestCase {

	private static final String VIEW = "test-branch-actions.view.xml";

	/** Condition that holds for every value. */
	private static final ActionScript TRUE = (context, input) -> Boolean.TRUE;

	/** Condition that holds for no value. */
	private static final ActionScript FALSE = (context, input) -> Boolean.FALSE;

	/**
	 * Function handing the chain's current value on as the switch value, as a {@code <switch>}
	 * without a value function does.
	 */
	private static final ActionScript VALUE = (context, input) -> input;

	private final List<String> _log = new ArrayList<>();

	private final List<Object> _completions = new ArrayList<>();

	/**
	 * Tests that the {@code then} branch runs when the condition holds.
	 */
	public void testThenBranchRuns() {
		IfAction branch = new IfAction(TRUE, List.of(record("then")), List.of(record("else")));

		run(List.of(branch, record("after")), "in");

		assertEquals(List.of("then", "after"), _log);
	}

	/**
	 * Tests that the {@code else} branch runs when the condition does not hold.
	 */
	public void testElseBranchRuns() {
		IfAction branch = new IfAction(FALSE, List.of(record("then")), List.of(record("else")));

		run(List.of(branch, record("after")), "in");

		assertEquals(List.of("else", "after"), _log);
	}

	/**
	 * Tests that the chain's value passes through a branch that is not configured.
	 */
	public void testMissingBranchPassesValueThrough() {
		IfAction branch = new IfAction(FALSE, List.of(result("then", "produced")), List.of());
		Object[] seen = new Object[1];

		run(List.of(branch, (context, input) -> seen[0] = input), "in");

		assertEquals("The value of the chain reaches the action after the branch.", "in", seen[0]);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that the result of the branch's last action becomes the value the chain continues with.
	 */
	public void testBranchResultContinuesChain() {
		IfAction branch = new IfAction(TRUE, List.of(result("then", "produced")), List.of());
		Object[] seen = new Object[1];

		run(List.of(branch, (context, input) -> seen[0] = input), "in");

		assertEquals("produced", seen[0]);
		assertEquals("The chain settles with the value the branch produced.", List.of("produced"), _completions);
	}

	/**
	 * Tests that an action inside a branch may resume the chain later, its result reaching the
	 * action that follows the branch.
	 */
	public void testAsynchronousActionInsideBranch() {
		Suspend suspend = new Suspend();
		IfAction branch = new IfAction(TRUE, List.of(suspend, record("then")), List.of());
		Object[] seen = new Object[1];

		run(List.of(branch, (context, input) -> seen[0] = input), "in");

		assertEquals("The chain waits inside the branch.", List.of(), _log);
		assertEquals(List.of(), _completions);

		suspend.resume("late");

		assertEquals(List.of("then"), _log);
		assertEquals("The value the suspended action resumed with reaches the branch's next action.",
			"late", seen[0]);
		assertEquals(List.of("late"), _completions);
	}

	/**
	 * Tests that an abort inside a branch ends the command and runs the compensation registered
	 * before the branch.
	 */
	public void testAbortInsideBranchEndsCommand() {
		IfAction branch = new IfAction(TRUE, List.of(abort()), List.of());

		run(List.of(compensate("outer"), branch, record("after")), "in");

		assertEquals("The command ends in the branch, the outer compensation runs.",
			List.of("outer compensated"), _log);
		assertEquals("An aborted chain settles without a value.", Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a compensation registered inside a branch runs when the enclosing chain aborts
	 * afterwards, before the compensations of the actions preceding the branch.
	 */
	public void testCompensationInsideBranchRunsOnLaterAbort() {
		IfAction branch = new IfAction(TRUE, List.of(compensate("inner")), List.of());

		run(List.of(compensate("outer"), branch, abort()), "in");

		assertEquals(List.of("inner compensated", "outer compensated"), _log);
	}

	/**
	 * Tests that the actions of the matching case run.
	 */
	public void testSwitchRunsMatchingCase() {
		SwitchAction branch = new SwitchAction(VALUE,
			List.of(new SwitchCase(is("open"), List.of(record("open"))),
				new SwitchCase(is("closed"), List.of(record("closed")))),
			List.of(record("default")));

		run(List.of(branch, record("after")), "closed");

		assertEquals(List.of("closed", "after"), _log);
	}

	/**
	 * Tests that a case configured with a value compares it to the switch value with the equality of
	 * TL-Script, which sees a number as equal to a number of another Java type and a text as equal
	 * to a text.
	 */
	public void testSwitchMatchesCaseValue() {
		SwitchAction branch = new SwitchAction(VALUE,
			List.of(SwitchCase.matching(literal(Long.valueOf(42)), List.of(record("number"))),
				SwitchCase.matching(literal("closed"), List.of(record("closed")))),
			List.of(record("default")));

		run(List.of(branch), Integer.valueOf(42));
		assertEquals("The number of the case matches an equal number of another Java type.",
			List.of("number"), _log);

		_log.clear();
		run(List.of(branch), "closed");
		assertEquals(List.of("closed"), _log);

		_log.clear();
		run(List.of(branch), "open");
		assertEquals(List.of("default"), _log);
	}

	/**
	 * Tests that a case configured with a predicate runs when the predicate holds for the switch
	 * value, reading its result in the fuzzy sense of TL-Script.
	 */
	public void testSwitchTestsCasePredicate() {
		SwitchAction branch = new SwitchAction(VALUE,
			List.of(SwitchCase.testing(script(value -> "open".equals(value) ? "yes" : null),
				List.of(record("open")))),
			List.of(record("default")));

		run(List.of(branch), "open");
		assertEquals("The predicate is called with the switch value.", List.of("open"), _log);

		_log.clear();
		run(List.of(branch), "closed");
		assertEquals("Nothing stands for a condition that does not hold.", List.of("default"), _log);
	}

	/**
	 * Tests that the default runs when no case matches.
	 */
	public void testSwitchRunsDefault() {
		SwitchAction branch = new SwitchAction(VALUE,
			List.of(new SwitchCase(is("open"), List.of(record("open")))), List.of(record("default")));

		run(List.of(branch, record("after")), "closed");

		assertEquals(List.of("default", "after"), _log);
	}

	/**
	 * Tests that the chain's value passes through a switch that has neither a matching case nor a
	 * default.
	 */
	public void testSwitchWithoutMatchPassesValueThrough() {
		SwitchAction branch = new SwitchAction(VALUE,
			List.of(new SwitchCase(is("open"), List.of(result("open", "produced")))), List.of());
		Object[] seen = new Object[1];

		run(List.of(branch, (context, input) -> seen[0] = input), "closed");

		assertEquals(List.of(), _log);
		assertEquals("closed", seen[0]);
	}

	/**
	 * Tests that a case configuring neither a value nor a predicate is reported as a configuration
	 * error.
	 */
	public void testSwitchCaseWithoutCondition() throws Exception {
		assertCaseError(newCase(null, null));
	}

	/**
	 * Tests that a case configuring both a value and a predicate is reported as a configuration
	 * error.
	 */
	public void testSwitchCaseWithBothConditions() throws Exception {
		assertCaseError(newCase(expr("'open'"), expr("s -> $s == 'open'")));
	}

	private void assertCaseError(CaseConfig caseConfig) {
		SwitchAction.Config config = TypedConfiguration.newConfigItem(SwitchAction.Config.class);
		config.update(config.descriptor().getProperty(SwitchAction.Config.CASES), List.of(caseConfig));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestBranchActions.class);
		context.getInstance(config);

		assertTrue("The case is rejected at instantiation.", context.hasErrors());
	}

	private static CaseConfig newCase(Expr match, Expr test) {
		CaseConfig result = TypedConfiguration.newConfigItem(CaseConfig.class);
		ConfigurationDescriptor descriptor = result.descriptor();
		if (match != null) {
			result.update(descriptor.getProperty(CaseConfig.MATCH), match);
		}
		if (test != null) {
			result.update(descriptor.getProperty(CaseConfig.TEST), test);
		}
		return result;
	}

	private static Expr expr(String source) throws Exception {
		return ExprFormat.INSTANCE.getValue(TestBranchActions.class.getSimpleName(), source);
	}

	/** Matcher of a case selected by a switch value equal to the given one. */
	private static Predicate<Object> is(Object expected) {
		return value -> expected.equals(value);
	}

	/** A compiled script delivering the given value, as a {@code match} expression does. */
	private static QueryExecutor literal(Object value) {
		return script(ignored -> value);
	}

	/** A compiled script computing its result from its single argument. */
	private static QueryExecutor script(Function<Object, Object> function) {
		return new QueryExecutor() {
			@Override
			protected Object internalExecuteWith(EvalContext definitions, Args args) {
				return function.apply(args.hasValue() ? args.value() : null);
			}

			@Override
			public SearchExpression getSearch() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected KnowledgeBase getKnowledgeBase() {
				return null;
			}

			@Override
			protected TLModel getTLModel() {
				return null;
			}

			@Override
			protected void internalDisableSecurity() {
				// Nothing to switch off, the script accesses no data.
			}
		};
	}

	/**
	 * Tests that a branch applies the form state as soon as one of its actions does.
	 */
	public void testAppliesFormState() {
		ViewAction applying = new FormApplying();

		assertFalse(new IfAction(TRUE, List.of(record("then")), List.of(record("else"))).appliesFormState());
		assertTrue(new IfAction(TRUE, List.of(applying), List.of()).appliesFormState());
		assertTrue("The branch that is not taken decides the button's state as well.",
			new IfAction(TRUE, List.of(), List.of(applying)).appliesFormState());

		assertFalse(new SwitchAction(VALUE, List.of(new SwitchCase(is("open"), List.of(record("open")))),
			List.of(record("default"))).appliesFormState());
		assertTrue(new SwitchAction(VALUE, List.of(new SwitchCase(is("open"), List.of(applying))), List.of())
			.appliesFormState());
		assertTrue(new SwitchAction(VALUE, List.of(), List.of(applying)).appliesFormState());
	}

	/**
	 * Tests that a command chain with {@code <if>} and {@code <switch>} branches parses and
	 * instantiates.
	 */
	public void testBranchConfiguration() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestBranchActions.class);
		PanelElement.Config panel = (PanelElement.Config) parse(context).getContent();
		Map<String, PolymorphicConfiguration<? extends ViewCommand>> commands = new HashMap<>();
		for (PolymorphicConfiguration<? extends ViewCommand> command : panel.getCommands()) {
			commands.put(((ViewCommand.Config) command).getName(), command);
		}

		GenericViewCommand.Config decide = (GenericViewCommand.Config) commands.get("decide");
		IfAction.Config ifConfig = (IfAction.Config) decide.getActions().get(0);
		assertNotNull("The condition is parsed.", ifConfig.getTest());
		assertEquals("branchInput", ifConfig.getInputs().get(0).getChannelName());
		assertEquals(1, ifConfig.getThen().size());
		assertTrue(ifConfig.getThen().get(0) instanceof ExecuteScriptAction.Config);
		assertEquals(1, ifConfig.getElse().size());

		GenericViewCommand.Config select = (GenericViewCommand.Config) commands.get("select");
		SwitchAction.Config switchConfig = (SwitchAction.Config) select.getActions().get(0);
		assertNotNull("The value function is parsed.", switchConfig.getValue());
		assertEquals(2, switchConfig.getCases().size());
		assertNotNull("The value of the first case is parsed.", switchConfig.getCases().get(0).getMatch());
		assertNull(switchConfig.getCases().get(0).getTest());
		assertEquals(1, switchConfig.getCases().get(0).getActions().size());
		assertNotNull("The predicate of the second case is parsed.", switchConfig.getCases().get(1).getTest());
		assertNull(switchConfig.getCases().get(1).getMatch());
		assertEquals(1, switchConfig.getDefault().size());

		GenericViewCommand.Config classify = (GenericViewCommand.Config) commands.get("classify");
		SwitchAction.Config valueLess = (SwitchAction.Config) classify.getActions().get(0);
		assertNull("Without a value function, the chain's value is the switch value.", valueLess.getValue());
		assertEquals(1, valueLess.getCases().size());

		assertTrue("The chain instantiates.", context.getInstance(decide) instanceof GenericViewCommand);
		assertTrue("The chain instantiates.", context.getInstance(select) instanceof GenericViewCommand);
		assertTrue("The chain instantiates.", context.getInstance(classify) instanceof GenericViewCommand);
		context.checkErrors();
	}

	private ViewElement.Config parse(DefaultInstantiationContext context) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestBranchActions.class, VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	private void run(List<ViewAction> actions, Object input) {
		ViewActionChain.run(new DefaultViewContext(null), actions, input, _completions::add);
	}

	/** An action that logs its name and hands the chain's value on. */
	private ViewAction record(String name) {
		return (context, input) -> {
			_log.add(name);
			return input;
		};
	}

	/** An action that logs its name and hands a value of its own on. */
	private ViewAction result(String name, Object result) {
		return (context, input) -> {
			_log.add(name);
			return result;
		};
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

	/** An action that applies the values entered into the enclosing form. */
	private static class FormApplying implements ViewAction {

		@Override
		public Object execute(ReactContext context, Object input) {
			return input;
		}

		@Override
		public boolean appliesFormState() {
			return true;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestBranchActions.class, TypeIndex.Module.INSTANCE);
	}
}
