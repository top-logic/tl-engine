/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.request;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;

/**
 * {@link ScriptContextVariablesProvider} exposing the {@link MethodDefinition#getParameters()
 * parameters} of the enclosing {@link MethodDefinition} as top-level variables for an
 * {@link com.top_logic.model.search.expr.config.dom.Expr} nested somewhere below it (e.g. the
 * {@link JSONRequestBody.Config#getJson()} expression).
 *
 * <p>
 * At runtime, {@link JSONRequestBody#createRequestModifier(com.top_logic.service.openapi.client.registry.impl.call.MethodSpec)}
 * binds exactly these parameter names around the configured expression (see
 * {@code JSONRequestBody.compileWithMethodParameters}), so they are the correct set of implicit
 * variables to offer for <code>$</code>-completion.
 * </p>
 */
public class MethodParameterVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return getVariablesFromModel(valueModel.getModel());
	}

	/**
	 * The parameter names of the {@link MethodDefinition} that (indirectly) contains the given
	 * configuration item.
	 *
	 * <p>
	 * The given model is expected to be nested somewhere below a {@link MethodDefinition} (e.g. as
	 * an entry of its {@link MethodDefinition#getCallBuilders()}, or nested further below such an
	 * entry). The {@link MethodDefinition} is found by walking up the {@link ConfigPart#container()
	 * container} chain. This works for any {@link ConfigurationItem} nested under a
	 * {@link MethodDefinition}, not just {@link JSONRequestBody.Config}.
	 * </p>
	 *
	 * @param model
	 *        The edited configuration item. May be of any type; when no enclosing
	 *        {@link MethodDefinition} is found, no variables are found.
	 * @return The parameter names; never <code>null</code>.
	 */
	public List<String> getVariablesFromModel(ConfigurationItem model) {
		ConfigurationItem current = model;
		while (current instanceof ConfigPart) {
			current = ((ConfigPart) current).container();
			if (current instanceof MethodDefinition) {
				return new ArrayList<>(((MethodDefinition) current).getParameterNames());
			}
		}
		return List.of();
	}

}
