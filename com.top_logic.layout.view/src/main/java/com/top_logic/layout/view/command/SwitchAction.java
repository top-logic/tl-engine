/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.MandatoryIfUnset;
import com.top_logic.basic.config.constraint.impl.OnlySetIfUnset;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ViewAction} that chooses one of several chains of actions by a value computed from the
 * arguments of the enclosing chain.
 *
 * <p>
 * The switch value is what the cases decide on: the result of the switch's
 * {@link Config#getValue() value} script, or the chain's default argument itself when no such
 * script is configured. A case either names the value it stands for with
 * {@link CaseConfig#getMatch() match} - a TL-Script expression compared to the switch value with
 * the equality of TL-Script, so a classifier, a text and a number are all written as the literal
 * they are - or decides with a condition {@link CaseConfig#getTest() test} read in the fuzzy sense
 * of TL-Script. Each case configures exactly one of the two. The scripts of the cases see the
 * switch value as a variable named by {@link Config#getAs() as}, {@value Config#DEFAULT_AS} by
 * default, in addition to the arguments of the chain.
 * </p>
 *
 * <p>
 * The cases are compared in configuration order and the first matching one runs; when none matches,
 * the default runs. The chosen chain runs as a nested chain of the enclosing one, with the same
 * arguments; the result of its last action is the result of this action. A case without actions, a
 * default without actions, and a value matching neither a case nor a configured default do
 * nothing. An abort inside a case - a declined {@link ConfirmAction} - aborts the whole command, and
 * a compensation registered inside a case runs on the command's abort like one registered beside
 * the switch.
 * </p>
 *
 * <p>
 * Example: close an open ticket and reopen a closed one, in one command with the input channel
 * {@code ticket}:
 * </p>
 *
 * <pre>
 * &lt;switch value="$ticket.get(`demo.tickets:Ticket#status`)"&gt;
 *   &lt;case match="`demo.tickets:TicketStatus#closed`"&gt;
 *     &lt;with-transaction&gt;
 *       &lt;execute-script function="$ticket.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/&gt;
 *     &lt;/with-transaction&gt;
 *   &lt;/case&gt;
 *   &lt;default&gt;
 *     &lt;with-transaction&gt;
 *       &lt;execute-script function="$ticket.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/&gt;
 *     &lt;/with-transaction&gt;
 *   &lt;/default&gt;
 * &lt;/switch&gt;
 * </pre>
 *
 * <p>
 * Example: decide on the chain's default argument with a condition and with a literal:
 * </p>
 *
 * <pre>
 * &lt;switch&gt;
 *   &lt;case test="$value == null"&gt;
 *     &lt;execute-script function="new(`demo.tickets:Ticket`, transient: true)"/&gt;
 *   &lt;/case&gt;
 *   &lt;case match="'closed'"&gt;
 *     &lt;write-channel name="closedTicket"/&gt;
 *   &lt;/case&gt;
 * &lt;/switch&gt;
 * </pre>
 */
@InApp
public class SwitchAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link SwitchAction}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-signpost")
	public interface Config extends PolymorphicConfiguration<SwitchAction>, NamedInputs {

		/** Configuration tag of a {@link SwitchAction}. */
		String TAG_NAME = "switch";

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getAs()}. */
		String AS = "as";

		/** Name of the switch value in the scripts of the cases when no {@link #getAs() name} is given. */
		String DEFAULT_AS = "value";

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		@Override
		@ClassDefault(SwitchAction.class)
		Class<? extends SwitchAction> getImplementationClass();

		/**
		 * TL-Script computing the value the cases decide on.
		 *
		 * <p>
		 * Uses the arguments of the chain and the values of the {@link #getInputs() inputs} as
		 * variables named after them, without declaring them. Without such a script, the chain's
		 * default argument is the value the cases decide on.
		 * </p>
		 */
		@Name(VALUE)
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getValue();

		/**
		 * The name of the variable holding the switch value in the scripts of the cases.
		 *
		 * <p>
		 * Without a name, the variable is called {@value #DEFAULT_AS}. It shadows an argument of the
		 * chain with the same name.
		 * </p>
		 */
		@Name(AS)
		@Nullable
		String getAs();

		/**
		 * The cases, compared in order; the first matching one runs.
		 *
		 * @implNote Written directly as {@code <case>} children of the {@code <switch>}.
		 */
		@Name(CASES)
		@DefaultContainer
		@TreeProperty
		List<CaseConfig> getCases();

		/**
		 * The actions to run when no case matches; without actions, nothing happens.
		 */
		@Name(DEFAULT)
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewAction>> getDefault();

		/**
		 * The name of the switch value in the scripts of the cases: the {@link #getAs() given
		 * name}, or {@link #DEFAULT_AS} without one.
		 */
		static String switchValueName(Config config) {
			String as = config.getAs();
			return as == null || as.isBlank() ? DEFAULT_AS : as.trim();
		}
	}

	/**
	 * Configuration of a single {@code <case>} of a {@link SwitchAction}.
	 *
	 * <p>
	 * A case configures exactly one of {@link #getMatch() match} and {@link #getTest() test}.
	 * </p>
	 */
	@TagName(CaseConfig.TAG_NAME)
	public interface CaseConfig extends ConfigPart {

		/** Configuration tag of a case. */
		String TAG_NAME = "case";

		/** Configuration name for {@link #getMatch()}. */
		String MATCH = "match";

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/**
		 * TL-Script expression computing the value this case stands for.
		 *
		 * <p>
		 * The case matches when the switch value is equal to this value in the sense of the
		 * TL-Script comparison, so a classifier is written as {@code `module:Enumeration#literal`},
		 * a text as {@code 'text'} and a number as the number it is. The expression is evaluated
		 * whenever the switch decides, so it may also compute the value from the arguments of the
		 * chain and the switch value, which it sees as variables.
		 * </p>
		 */
		@Name(MATCH)
		@Constraint(value = OnlySetIfUnset.class, args = @Ref(TEST))
		@Constraint(value = MandatoryIfUnset.class, args = @Ref(TEST))
		@ScriptContextVariables(CaseVariables.class)
		Expr getMatch();

		/**
		 * TL-Script condition; this case matches when it holds.
		 *
		 * <p>
		 * Sees the switch value and the arguments of the chain as variables. The result is taken as
		 * a condition in the fuzzy sense of TL-Script, so a plain object stands for a true condition
		 * and nothing - {@code null}, an empty list, an empty text - for a false one. Use it for a
		 * case that cannot be written as a single value, such as a range or a test for emptiness; a
		 * case standing for one value is written as {@link #getMatch() match}.
		 * </p>
		 */
		@Name(TEST)
		@Constraint(value = OnlySetIfUnset.class, args = @Ref(MATCH))
		@ScriptContextVariables(CaseVariables.class)
		Expr getTest();

		/**
		 * The actions to run when this case matches; without actions, nothing happens.
		 *
		 * @implNote Written directly as children of the {@code <case>}.
		 */
		@Name(ACTIONS)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	/**
	 * {@link ScriptContextVariablesProvider} for the scripts of a {@link CaseConfig}: the variables
	 * of the enclosing switch, followed by the switch value.
	 */
	public static class CaseVariables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			ConfigurationItem caseConfig = valueModel.getModel();
			if (caseConfig instanceof ConfigPart part && part.container() instanceof Config switchConfig) {
				return ActionScriptVariables.variables(switchConfig, List.of(Config.switchValueName(switchConfig)));
			}
			return List.of();
		}
	}

	private final ActionScript _value;

	private final List<SwitchCase> _cases;

	private final List<ViewAction> _default;

	/**
	 * Creates a new {@link SwitchAction} from configuration.
	 */
	@CalledByReflection
	public SwitchAction(InstantiationContext context, Config config) {
		this(switchValue(context, config), cases(context, config),
			ViewActions.instantiate(context, config.getDefault()));
	}

	/**
	 * Creates a new {@link SwitchAction}.
	 *
	 * @param value
	 *        The script computing the value the cases decide on.
	 * @param cases
	 *        The cases, compared in the given order; the first matching one runs.
	 * @param defaultActions
	 *        The actions to run when no case matches; empty for nothing to happen.
	 */
	public SwitchAction(ActionScript value, List<SwitchCase> cases, List<ViewAction> defaultActions) {
		_value = value;
		_cases = cases;
		_default = defaultActions;
	}

	private static ActionScript switchValue(InstantiationContext context, Config config) {
		Expr value = config.getValue();
		if (value == null) {
			return (context1, arguments, additional) -> arguments.value();
		}
		return ActionScript.compile(value, ActionScope.of(context), config.getInputs());
	}

	private static List<SwitchCase> cases(InstantiationContext context, Config config) {
		ActionScope scope = ActionScope.of(context);
		List<InputRef> inputs = config.getInputs();
		String switchValue = Config.switchValueName(config);
		List<SwitchCase> result = new ArrayList<>();
		for (CaseConfig caseConfig : config.getCases()) {
			Expr match = caseConfig.getMatch();
			List<ViewAction> actions = ViewActions.instantiate(context, caseConfig.getActions());
			if (match != null) {
				result.add(SwitchCase.matching(ActionScript.compile(match, scope, inputs, switchValue), actions));
			} else {
				result.add(SwitchCase.testing(ActionScript.compile(caseConfig.getTest(), scope, inputs, switchValue),
					actions));
			}
		}
		return result;
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		Object value = _value.execute(context, arguments);
		ViewActionChain.nest(context, branch(context, arguments, value), arguments, continuation);
	}

	private List<ViewAction> branch(ReactContext context, ActionArguments arguments, Object value) {
		for (SwitchCase candidate : _cases) {
			if (candidate.matcher().matches(context, arguments, value)) {
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
	 * Destructive as soon as one of the cases or the default is, see {@link #appliesFormState()}.
	 */
	@Override
	public ButtonTone getTone() {
		return ViewActions.tone(Stream.concat(_default.stream(),
			_cases.stream().flatMap(candidate -> candidate.actions().stream())).toList());
	}

	/**
	 * Decides whether a case of a {@link SwitchAction} runs.
	 */
	@FunctionalInterface
	public interface CaseMatcher {

		/**
		 * Whether the case runs.
		 *
		 * @param context
		 *        The context the switch executes in.
		 * @param arguments
		 *        The arguments of the chain.
		 * @param value
		 *        The switch value.
		 */
		boolean matches(ReactContext context, ActionArguments arguments, Object value);
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
	public record SwitchCase(CaseMatcher matcher, List<ViewAction> actions) {

		/**
		 * Creates a {@link SwitchCase} that runs for a switch value equal to the value the given
		 * script computes.
		 *
		 * @param match
		 *        Computes the value this case stands for, evaluated whenever the switch decides with
		 *        the switch value as additional value.
		 * @param actions
		 *        The actions to run when the switch value is equal to that value in the sense of the
		 *        TL-Script comparison.
		 */
		public static SwitchCase matching(ActionScript match, List<ViewAction> actions) {
			return new SwitchCase(
				(context, arguments, value) -> IsEqual.equals(value, match.execute(context, arguments, value)), actions);
		}

		/**
		 * Creates a {@link SwitchCase} that runs when the given condition holds.
		 *
		 * @param test
		 *        Evaluated with the switch value as additional value; its result is read as a
		 *        condition in the fuzzy sense of TL-Script.
		 * @param actions
		 *        The actions to run when the condition holds.
		 */
		public static SwitchCase testing(ActionScript test, List<ViewAction> actions) {
			return new SwitchCase(
				(context, arguments, value) -> SearchExpression.isTrue(test.execute(context, arguments, value)),
				actions);
		}
	}
}
