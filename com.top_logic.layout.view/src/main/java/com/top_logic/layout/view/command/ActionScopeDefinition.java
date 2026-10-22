/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import com.top_logic.basic.config.ConfigurationItem;

/**
 * A configuration that defines the names of the {@link ActionArguments arguments} the actions and
 * commands it contains run with: a trigger supplying a value, a command with inputs, an action with
 * a result for its follow-up actions.
 *
 * <p>
 * The definition is the one source of these names. The runtime instantiates the contained
 * actions in the {@link ActionScope} it computes, and the script editor offers the same names for
 * completion (see {@link ActionScopes#scopeAt(ConfigurationItem)}), so that both cannot diverge.
 * </p>
 *
 * <p>
 * Implemented by configuration interfaces, which override {@link #actionScope(ConfigurationItem,
 * ActionScope)} with a default method of their own, since an abstract method would be taken as a
 * configuration property.
 * </p>
 */
public interface ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * The scope of the actions in the given part of this configuration.
	 *
	 * @param part
	 *        A configuration directly contained in this one, e.g. a command in a trigger property
	 *        or an action in a list of follow-up actions.
	 * @param outer
	 *        The scope at the position of this configuration.
	 * @return The scope the given part is instantiated in; {@code outer} for a part this
	 *         configuration defines no names for, which is the default.
	 */
	default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
		return outer;
	}

}
