/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * The follow-up actions of an action with a result, see {@link WithResultActions}.
 *
 * <p>
 * The actions run once the action producing the result has completed, including a commit the
 * action does itself. They run with the arguments of the chain, extended by the result under the
 * name {@link #getAs()}. The result is their default value: an action processing a single value
 * works on it unless it selects another argument.
 * </p>
 */
public interface WithResult extends ConfigPart, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getAs()}. */
	String AS = "as";

	/** Configuration name for {@link #getActions()}. */
	String ACTIONS = "actions";

	/**
	 * The name under which the follow-up actions see the result.
	 *
	 * <p>
	 * Without a name, the result is called {@value WithResultActions#RESULT}. The name shadows an
	 * equal name of the arguments of the chain.
	 * </p>
	 */
	@Name(AS)
	@Nullable
	String getAs();

	/**
	 * The actions to run after the action producing the result, with its result in addition to the
	 * arguments of the chain.
	 *
	 * <p>
	 * An abort in one of these actions aborts the whole chain; changes the action committed stay.
	 * The chain goes on with the actions following the action producing the result once these
	 * actions are done.
	 * </p>
	 *
	 * @implNote Written directly as children of the element.
	 */
	@Name(ACTIONS)
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	@TreeProperty
	List<PolymorphicConfiguration<? extends ViewAction>> getActions();

	/**
	 * The name of the result for the actions of the given configuration: the {@link #getAs() given
	 * name}, or {@value WithResultActions#RESULT} without one.
	 */
	static String resultName(WithResult config) {
		String as = config.getAs();
		return as == null || as.isBlank() ? WithResultActions.RESULT : as.trim();
	}

	/**
	 * The follow-up actions run with the arguments at the position of the action producing the
	 * result, extended by the {@link #resultName(WithResult) result}.
	 */
	@Override
	default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
		if (ActionScopes.contains(getActions(), part)) {
			return outer.with(resultName(this));
		}
		return outer;
	}

}
