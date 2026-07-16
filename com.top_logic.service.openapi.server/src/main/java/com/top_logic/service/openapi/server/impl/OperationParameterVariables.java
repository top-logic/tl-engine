/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.service.openapi.server.conf.Operation;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * {@link ScriptContextVariablesProvider} exposing the parameters of the enclosing
 * {@link Operation} as top-level variables for the operation script of
 * {@link ServiceMethodBuilderByExpression}.
 */
public class OperationParameterVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return getVariablesFromModel(valueModel.getModel());
	}

	/**
	 * The parameter names of the {@link Operation} owning the given implementation config.
	 *
	 * <p>
	 * The implementation config is a {@link ConfigPart} whose {@link ConfigPart#container() container}
	 * is the {@link Operation} it is configured on.
	 * </p>
	 *
	 * @param model
	 *        The {@link ServiceMethodBuilderByExpression.Config} being edited.
	 */
	public List<String> getVariablesFromModel(ConfigurationItem model) {
		List<String> result = new ArrayList<>();
		if (model instanceof ConfigPart) {
			ConfigurationItem container = ((ConfigPart) model).container();
			if (container instanceof Operation) {
				for (RequestParameter.Config<?> parameter : ((Operation) container).getParameters()) {
					result.add(parameter.getName());
				}
			}
		}
		return result;
	}

}
