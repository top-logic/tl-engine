/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ScriptContextVariablesProvider} offering the {@link NamedInputs inputs} of the edited
 * configuration as the variables of its scripts.
 *
 * <p>
 * For a configuration whose scripts see the values of its own inputs and nothing else, e.g. a
 * number field computing the range of its slider from channels.
 * </p>
 */
public class NamedInputsVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		ConfigurationItem config = valueModel.getModel();
		return config instanceof NamedInputs named ? InputRef.names(named.getInputs()) : List.of();
	}

}
