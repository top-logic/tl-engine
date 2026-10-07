/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.conf.Operation;
import com.top_logic.service.openapi.server.conf.ParametersConfig;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.ParameterReference;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
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
	 * The variable names bound around the operation script.
	 *
	 * <p>
	 * The implementation config is a {@link ConfigPart}; walking up its
	 * {@link ConfigPart#container() container} chain reaches the enclosing {@link Operation} and its
	 * {@link PathItem}. Both contribute request parameters that the runtime binds around the operation
	 * script (see {@code OpenApiServer.buildHandlers}), so both are offered by completion. A
	 * {@link ParameterReference} contributes the variables of the referenced global parameter.
	 * </p>
	 *
	 * @param model
	 *        The {@link ServiceMethodBuilderByExpression.Config} being edited.
	 */
	public List<String> getVariablesFromModel(ConfigurationItem model) {
		List<String> result = new ArrayList<>();
		ConfigurationItem current = model;
		while (current instanceof ConfigPart) {
			current = ((ConfigPart) current).container();
			if (current instanceof ParametersConfig) {
				Map<String, ReferencedParameter> globalParameters = globalParameters(current);
				for (RequestParameter.Config<?> parameter : ((ParametersConfig) current).getParameters()) {
					ConcreteRequestParameter.Config<?> resolved = parameter.resolveParameter(globalParameters);
					if (resolved == null) {
						result.add(parameter.getName());
					} else {
						result.addAll(resolved.scriptVariableNames());
					}
				}
			}
		}
		return result;
	}

	private static Map<String, ReferencedParameter> globalParameters(ConfigurationItem part) {
		if (part instanceof OpenAPIServerPart) {
			OpenApiServer.Config<?> server = ((OpenAPIServerPart) part).getServerConfiguration();
			if (server != null) {
				return server.getGlobalParameters();
			}
		}
		return Collections.emptyMap();
	}

}
