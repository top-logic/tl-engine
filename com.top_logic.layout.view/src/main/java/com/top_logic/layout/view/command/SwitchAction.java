/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ViewAction} that chooses one of several chains of actions by a value computed from the
 * current value of the enclosing chain.
 *
 * <p>
 * The switch value is what the cases decide on: the result of the switch's
 * {@link Config#getValue() value} function, or the chain's current value itself when no such
 * function is configured. A case either names the value it stands for with
 * {@link CaseConfig#getMatch() match} - a TL-Script expression without parameters, compared to the
 * switch value with the equality of TL-Script, so a classifier, a text and a number are all written
 * as the literal they are - or decides with a predicate {@link CaseConfig#getTest() test} called
 * with the switch value and read as a condition in the fuzzy sense of TL-Script. Each case
 * configures exactly one of the two.
 * </p>
 *
 * <p>
 * The cases are compared in configuration order and the first matching one runs; when none matches,
 * the default runs. The chosen chain runs as a nested chain of the enclosing one: it starts with the
 * current value and its last action's result becomes the value the enclosing chain continues with. A
 * case without actions, a default without actions, and a value matching neither a case nor a
 * configured default all pass the value through unchanged. An abort inside a case - a declined
 * {@link ConfirmAction} - aborts the whole command, and a compensation registered inside a case runs
 * on the command's abort like one registered beside the switch.
 * </p>
 *
 * <p>
 * Example: close an open ticket and reopen a closed one, in one command:
 * </p>
 *
 * <pre>
 * &lt;switch value="t -&gt; $t.get(`demo.tickets:Ticket#status`)"&gt;
 *   &lt;case match="`demo.tickets:TicketStatus#closed`"&gt;
 *     &lt;with-transaction&gt;
 *       &lt;execute-script function="t -&gt; $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/&gt;
 *     &lt;/with-transaction&gt;
 *   &lt;/case&gt;
 *   &lt;default&gt;
 *     &lt;with-transaction&gt;
 *       &lt;execute-script function="t -&gt; $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/&gt;
 *     &lt;/with-transaction&gt;
 *   &lt;/default&gt;
 * &lt;/switch&gt;
 * </pre>
 *
 * <p>
 * Example: decide on the chain's own value with a literal and with a predicate:
 * </p>
 *
 * <pre>
 * &lt;switch&gt;
 *   &lt;case test="s -&gt; $s == null"&gt;
 *     &lt;execute-script function="x -&gt; new(`demo.tickets:Ticket`, transient: true)"/&gt;
 *   &lt;/case&gt;
 *   &lt;case match="'closed'"&gt;
 *     &lt;write-channel name="closedTicket"/&gt;
 *   &lt;/case&gt;
 * &lt;/switch&gt;
 * </pre>
 */
@InApp
public class SwitchAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link SwitchAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<SwitchAction>, ActionScript.Inputs {

		/** Configuration tag of a {@link SwitchAction}. */
		String TAG_NAME = "switch";

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		@Override
		@ClassDefault(SwitchAction.class)
		Class<? extends SwitchAction> getImplementationClass();

		/**
		 * TL-Script function computing the value the cases decide on.
		 *
		 * <p>
		 * Called with the {@link #getInputs() inputs} channel values as leading positional
		 * arguments, followed by the chain's current value as the last argument. Without such a
		 * function, the chain's current value is the value the cases decide on.
		 * </p>
		 */
		@Name(VALUE)
		Expr getValue();

		/**
		 * The cases, compared in order; the first matching one runs.
		 *
		 * @implNote Written directly as {@code <case>} children of the {@code <switch>}.
		 */
		@Name(CASES)
		@DefaultContainer
		List<CaseConfig> getCases();

		/**
		 * The actions to run when no case matches; none to pass the chain's value through.
		 */
		@Name(DEFAULT)
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getDefault();
	}

	/**
	 * Configuration of a single {@code <case>} of a {@link SwitchAction}.
	 *
	 * <p>
	 * A case configures exactly one of {@link #getMatch() match} and {@link #getTest() test}.
	 * </p>
	 */
	@TagName(CaseConfig.TAG_NAME)
	public interface CaseConfig extends ConfigurationItem {

		/** Configuration tag of a case. */
		String TAG_NAME = "case";

		/** Configuration name for {@link #getMatch()}. */
		String MATCH = "match";

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/**
		 * TL-Script expression without parameters computing the value this case stands for.
		 *
		 * <p>
		 * The case matches when the switch value is equal to this value in the sense of the
		 * TL-Script comparison, so a classifier is written as {@code `module:Enumeration#literal`},
		 * a text as {@code 'text'} and a number as the number it is. The expression is evaluated
		 * whenever the switch decides, so it may also compute the value from the application's
		 * state.
		 * </p>
		 */
		@Name(MATCH)
		Expr getMatch();

		/**
		 * TL-Script predicate called with the switch value; this case matches when it holds.
		 *
		 * <p>
		 * The result is taken as a condition in the fuzzy sense of TL-Script, so a plain object
		 * stands for a true condition and nothing - {@code null}, an empty list, an empty text - for
		 * a false one. Use it for a case that cannot be written as a single value, such as a range or
		 * a test for emptiness; a case standing for one value is written as
		 * {@link #getMatch() match}.
		 * </p>
		 */
		@Name(TEST)
		Expr getTest();

		/**
		 * The actions to run when this case matches; none to pass the chain's value through.
		 *
		 * @implNote Written directly as children of the {@code <case>}.
		 */
		@Name(ACTIONS)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	private final ActionScript _value;

	private final List<SwitchCase> _cases;

	private final List<ViewAction> _default;

	/**
	 * Creates a new {@link SwitchAction} from configuration.
	 */
	@CalledByReflection
	public SwitchAction(InstantiationContext context, Config config) {
		this(switchValue(config), cases(context, config.getCases()),
			ViewActions.instantiate(context, config.getDefault()));
	}

	/**
	 * Creates a new {@link SwitchAction}.
	 *
	 * @param value
	 *        The function computing the value the cases decide on.
	 * @param cases
	 *        The cases, compared in the given order; the first matching one runs.
	 * @param defaultActions
	 *        The actions to run when no case matches; empty to pass the chain's value through.
	 */
	public SwitchAction(ActionScript value, List<SwitchCase> cases, List<ViewAction> defaultActions) {
		_value = value;
		_cases = cases;
		_default = defaultActions;
	}

	private static ActionScript switchValue(Config config) {
		Expr value = config.getValue();
		if (value == null) {
			return (context, input) -> input;
		}
		return ActionScript.compile(value, config.getInputs());
	}

	private static List<SwitchCase> cases(InstantiationContext context, List<CaseConfig> configs) {
		List<SwitchCase> result = new ArrayList<>();
		for (CaseConfig caseConfig : configs) {
			Expr match = caseConfig.getMatch();
			Expr test = caseConfig.getTest();
			List<ViewAction> actions = ViewActions.instantiate(context, caseConfig.getActions());
			if (match != null) {
				if (test != null) {
					context.error("A case of a switch must not configure both '" + CaseConfig.MATCH + "' and '"
						+ CaseConfig.TEST + "'.");
					continue;
				}
				result.add(SwitchCase.matching(QueryExecutor.compile(match), actions));
			} else if (test != null) {
				result.add(SwitchCase.testing(QueryExecutor.compile(test), actions));
			} else {
				context.error("A case of a switch must configure either '" + CaseConfig.MATCH + "' or '"
					+ CaseConfig.TEST + "'.");
			}
		}
		return result;
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ViewActionChain.nest(context, branch(_value.execute(context, input)), input, continuation);
	}

	private List<ViewAction> branch(Object value) {
		for (SwitchCase candidate : _cases) {
			if (candidate.matcher().test(value)) {
				return candidate.actions();
			}
		}
		return _default;
	}

	@Override
	public boolean appliesFormState() {
		return ViewActions.appliesFormState(_default)
			|| _cases.stream().anyMatch(candidate -> ViewActions.appliesFormState(candidate.actions()));
	}

	/**
	 * A case of a {@link SwitchAction}: the condition the switch value must satisfy for this case to
	 * run, and the actions it runs.
	 *
	 * @param matcher
	 *        Decides whether the switch value selects this case.
	 * @param actions
	 *        The actions to run when it does.
	 */
	public record SwitchCase(Predicate<Object> matcher, List<ViewAction> actions) {

		/**
		 * Creates a {@link SwitchCase} that runs for a switch value equal to the value the given
		 * expression computes.
		 *
		 * @param match
		 *        Computes the value this case stands for, evaluated whenever the switch decides.
		 * @param actions
		 *        The actions to run when the switch value is equal to that value in the sense of the
		 *        TL-Script comparison.
		 */
		public static SwitchCase matching(QueryExecutor match, List<ViewAction> actions) {
			return new SwitchCase(value -> IsEqual.equals(value, match.execute()), actions);
		}

		/**
		 * Creates a {@link SwitchCase} that runs for a switch value the given predicate holds for.
		 *
		 * @param test
		 *        Called with the switch value; its result is read as a condition in the fuzzy sense
		 *        of TL-Script.
		 * @param actions
		 *        The actions to run when the predicate holds.
		 */
		public static SwitchCase testing(QueryExecutor test, List<ViewAction> actions) {
			return new SwitchCase(value -> SearchExpression.isTrue(test.execute(value)), actions);
		}
	}
}
