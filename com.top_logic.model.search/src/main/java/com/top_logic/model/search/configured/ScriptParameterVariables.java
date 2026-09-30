/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ScriptContextVariablesProvider} exposing the {@link ScriptConfiguration#getParameters()
 * parameters} declared for a {@link ScriptConfiguration} as top-level variables for its
 * {@link ScriptConfiguration#getImplementation() implementation} expression.
 */
public class ScriptParameterVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return getVariablesFromModel(valueModel.getModel());
	}

	/**
	 * The names of the {@link ScriptParameter}s declared on the given {@link ScriptConfiguration}.
	 *
	 * @param model
	 *        The edited configuration item. May be of any type; when it is not a
	 *        {@link ScriptConfiguration}, no variables are found.
	 * @return The parameter names; never <code>null</code>.
	 */
	public List<String> getVariablesFromModel(ConfigurationItem model) {
		if (!(model instanceof ScriptConfiguration)) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		for (ScriptParameter parameter : ((ScriptConfiguration) model).getParameters()) {
			result.add(parameter.getName());
		}
		return result;
	}

}
