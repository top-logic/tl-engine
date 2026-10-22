/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ScriptContextVariablesProvider} offering the variables of an {@link ActionScript}: the
 * arguments defined at the position of the edited action, followed by the action's own
 * {@link NamedInputs inputs}.
 *
 * @see ActionScript#variables(ActionScope, List, List)
 */
public class ActionScriptVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return variables(valueModel.getModel(), List.of());
	}

	/**
	 * The variables of a script of the given action.
	 *
	 * @param action
	 *        The configuration of the action holding the script.
	 * @param additional
	 *        The names of values the action adds for the script.
	 */
	public static List<String> variables(ConfigurationItem action, List<String> additional) {
		List<InputRef> inputs = action instanceof NamedInputs named ? named.getInputs() : List.of();
		return ActionScript.variables(ActionScopes.scopeAt(action), inputs, additional);
	}
}
