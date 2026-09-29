/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Resolves the {@link ScriptContextVariables} annotation of an edited property to the context
 * variable names provided by its {@link ScriptContextVariablesProvider}.
 */
public class ScriptContextVariablesResolver {

	/**
	 * The context variable names for the property represented by the given {@link ValueModel}.
	 *
	 * @param valueModel
	 *        The value model of the edited property, or <code>null</code>.
	 * @return The variable names (without <code>$</code>); empty if there is no value model, no
	 *         annotation, or the provider fails.
	 */
	public static List<String> resolve(ValueModel valueModel) {
		if (valueModel == null) {
			return Collections.emptyList();
		}
		PropertyDescriptor property = valueModel.getProperty();
		if (property == null) {
			return Collections.emptyList();
		}
		ScriptContextVariables annotation = property.getAnnotation(ScriptContextVariables.class);
		if (annotation == null) {
			return Collections.emptyList();
		}
		try {
			ScriptContextVariablesProvider provider = annotation.value().getConstructor().newInstance();
			List<String> variables = provider.getVariables(valueModel);
			return variables == null ? Collections.emptyList() : variables;
		} catch (Exception ex) {
			Logger.error("Failed to compute TL-Script context variables for '" + property.getPropertyName() + "'.",
				ex, ScriptContextVariablesResolver.class);
			return Collections.emptyList();
		}
	}

}
