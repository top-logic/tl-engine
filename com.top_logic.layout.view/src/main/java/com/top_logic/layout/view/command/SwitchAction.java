/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ViewAction} that chooses one of several chains of actions by the value a TL-Script
 * expression computes from the current value of the enclosing chain.
 *
 * <p>
 * The cases are compared in configuration order and the first matching one runs; when none matches,
 * the default runs. The chosen chain runs as a nested chain of the enclosing one: it starts with
 * the current value and its last action's result becomes the value the enclosing chain continues
 * with. A case without actions, a default without actions, and a value matching neither a case nor
 * a configured default all pass the value through unchanged. An abort inside a case - a declined
 * {@link ConfirmAction} - aborts the whole command, and a compensation registered inside a case
 * runs on the command's abort like one registered beside the switch.
 * </p>
 *
 * <p>
 * A case matches the classifier of an enumeration by its qualified name as well as by its plain
 * name, and any other value by its text. Example: close an open ticket and reopen a closed one, in
 * one command:
 * </p>
 *
 * <pre>
 * &lt;switch test="t -&gt; $t.get(`demo.tickets:Ticket#status`)"&gt;
 *   &lt;case value="demo.tickets:TicketStatus#closed"&gt;
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

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		@Override
		@ClassDefault(SwitchAction.class)
		Class<? extends SwitchAction> getImplementationClass();

		/**
		 * TL-Script expression computing the value the cases are compared against.
		 *
		 * <p>
		 * Called with the {@link #getInputs() inputs} channel values as leading positional
		 * arguments, followed by the chain's current value as the last argument.
		 * </p>
		 */
		@Name(TEST)
		@Mandatory
		Expr getTest();

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
	 */
	@TagName(CaseConfig.TAG_NAME)
	public interface CaseConfig extends ConfigurationItem {

		/** Configuration tag of a case. */
		String TAG_NAME = "case";

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/**
		 * The value this case matches.
		 *
		 * <p>
		 * A classifier of an enumeration is named either qualified, as
		 * {@code module:Enumeration#literal}, or by its plain name; any other value is written as
		 * the text it displays as.
		 * </p>
		 */
		@Name(VALUE)
		@Mandatory
		String getValue();

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

	private final ActionScript _test;

	private final List<SwitchCase> _cases;

	private final List<ViewAction> _default;

	/**
	 * Creates a new {@link SwitchAction} from configuration.
	 */
	@CalledByReflection
	public SwitchAction(InstantiationContext context, Config config) {
		this(ActionScript.compile(config.getTest(), config.getInputs()), cases(context, config.getCases()),
			ViewActions.instantiate(context, config.getDefault()));
	}

	/**
	 * Creates a new {@link SwitchAction}.
	 *
	 * @param test
	 *        The function computing the value the cases are compared against.
	 * @param cases
	 *        The cases, compared in the given order; the first matching one runs.
	 * @param defaultActions
	 *        The actions to run when no case matches; empty to pass the chain's value through.
	 */
	public SwitchAction(ActionScript test, List<SwitchCase> cases, List<ViewAction> defaultActions) {
		_test = test;
		_cases = cases;
		_default = defaultActions;
	}

	private static List<SwitchCase> cases(InstantiationContext context, List<CaseConfig> configs) {
		List<SwitchCase> result = new ArrayList<>();
		for (CaseConfig caseConfig : configs) {
			result.add(new SwitchCase(caseConfig.getValue(), ViewActions.instantiate(context, caseConfig.getActions())));
		}
		return result;
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ViewActionChain.nest(context, branch(_test.execute(context, input)), input, continuation);
	}

	private List<ViewAction> branch(Object value) {
		for (SwitchCase candidate : _cases) {
			if (candidate.matches(value)) {
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
	 * A case of a {@link SwitchAction}: the value it matches and the actions it runs.
	 *
	 * @param value
	 *        The configured value this case matches.
	 * @param actions
	 *        The actions to run when it matches.
	 */
	public record SwitchCase(String value, List<ViewAction> actions) {

		/**
		 * Whether the given result of the switch's test function selects this case.
		 */
		boolean matches(Object testResult) {
			if (testResult == null) {
				return false;
			}
			if (testResult instanceof TLClassifier classifier) {
				return value.equals(TLModelUtil.qualifiedName(classifier)) || value.equals(classifier.getName());
			}
			return value.equals(String.valueOf(testResult));
		}
	}
}
