/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.util.List;

import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration of an action with a result that runs follow-up actions over that result.
 *
 * <p>
 * The arguments of an action chain flow unchanged through the chain; the result of an action does
 * not reach the actions following it. To work with the result, the actions are nested into the
 * action producing it. They run after it with the arguments of the chain, extended by the result
 * under the name {@link #RESULT} or the name given by {@link #getAs() as}. The result is their
 * default value: an action processing a single value works on it unless it selects another
 * argument.
 * </p>
 *
 * <p>
 * Example: persist a draft, show the created object and close the dialog:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="$draft.copy(transient: false)" as="created"&gt;
 *   &lt;write-channel name="selection"/&gt;
 *   &lt;close-dialog/&gt;
 * &lt;/execute-script&gt;
 * </pre>
 *
 * @implNote Offered by the configurations of actions with a useful result. The follow-up actions are
 *           run by the chain step {@link ViewActions#instantiate(com.top_logic.basic.config.InstantiationContext, List, ActionScope)}
 *           wraps the action into, not by the action itself.
 */
public interface WithResultActions extends ConfigPart, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getAs()}. */
	String AS = "as";

	/** Configuration name for {@link #getActions()}. */
	String ACTIONS = "actions";

	/**
	 * Name of the result of an action for its follow-up actions when no {@link #getAs() name} is
	 * given.
	 */
	String RESULT = "result";

	/**
	 * The name under which the follow-up actions see the result of this action.
	 *
	 * <p>
	 * Without a name, the result is called {@value #RESULT}. The name shadows an equal name of the
	 * arguments of the chain.
	 * </p>
	 */
	@Name(AS)
	@Nullable
	String getAs();

	/**
	 * The actions to run after this action, with its result in addition to the arguments of the
	 * chain.
	 *
	 * <p>
	 * An abort in one of these actions aborts the whole chain. The chain goes on with the actions
	 * following this action once the follow-up actions are done.
	 * </p>
	 *
	 * @implNote Written directly as children of the action's element.
	 */
	@Name(ACTIONS)
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends ViewAction>> getActions();

	/**
	 * The name of the result for the follow-up actions of the given action: the
	 * {@link #getAs() given name}, or
	 * {@link #RESULT} without one.
	 */
	static String resultName(WithResultActions config) {
		String as = config.getAs();
		return as == null || as.isBlank() ? RESULT : as.trim();
	}

	/**
	 * The follow-up actions run with the arguments at the position of this action, extended by the
	 * {@link #resultName(WithResultActions) result}.
	 */
	@Override
	default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
		if (ActionScopes.contains(getActions(), part)) {
			return outer.with(resultName(this));
		}
		return outer;
	}

}
