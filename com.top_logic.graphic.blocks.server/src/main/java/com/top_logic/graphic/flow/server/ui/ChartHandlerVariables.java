/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ScriptContextVariablesProvider} exposing the names of the {@link ScriptFlowChartBuilder.Config#getHandlers()
 * handlers} declared alongside the {@link ScriptFlowChartBuilder.Config#getCreateChart() create chart script} as
 * top-level variables of that script.
 */
public class ChartHandlerVariables implements ScriptContextVariablesProvider {

	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return getVariablesFromModel(valueModel.getModel());
	}

	/**
	 * The handler names declared in the given model's {@link ScriptFlowChartBuilder.Config#getHandlers()
	 * handlers} map.
	 *
	 * <p>
	 * The {@link ScriptFlowChartBuilder} constructor binds each handler as an implicit variable
	 * around the {@link ScriptFlowChartBuilder.Config#getCreateChart() create chart script}, keyed by
	 * its map key (see {@link ScriptFlowChartBuilder.Config.SyntaxCheck}, which performs the same
	 * binding for validation purposes).
	 * </p>
	 *
	 * @param model
	 *        The configuration item being edited.
	 * @return The declared handler names; empty if the model is not a
	 *         {@link ScriptFlowChartBuilder.Config}.
	 */
	public List<String> getVariablesFromModel(ConfigurationItem model) {
		if (model instanceof ScriptFlowChartBuilder.Config<?>) {
			return new ArrayList<>(((ScriptFlowChartBuilder.Config<?>) model).getHandlers().keySet());
		}
		return new ArrayList<>();
	}

}
