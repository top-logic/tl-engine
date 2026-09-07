/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.script;

import java.util.List;

import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ScriptContextVariablesProvider} exposing the implicit parameters that
 * {@link ConsumerProcessorByExpression} binds around {@link ConsumerProcessorByExpression.Config#getProcessor()}.
 */
public class ConsumerProcessorVariables implements ScriptContextVariablesProvider {

	/**
	 * The names of the implicit parameters bound around the processor script.
	 *
	 * <p>
	 * These are constant: {@link ConsumerProcessorByExpression} always binds the same four
	 * names around {@link ConsumerProcessorByExpression.Config#getProcessor()}, regardless of the
	 * edited configuration.
	 * </p>
	 *
	 * @param valueModel
	 *        Ignored, since the variables are constant.
	 */
	@Override
	public List<String> getVariables(ValueModel valueModel) {
		return List.of(
			ConsumerProcessorByExpression.MESSAGE,
			ConsumerProcessorByExpression.KEY,
			ConsumerProcessorByExpression.HEADERS,
			ConsumerProcessorByExpression.TOPIC);
	}

}
