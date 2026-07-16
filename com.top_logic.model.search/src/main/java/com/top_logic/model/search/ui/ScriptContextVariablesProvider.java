/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.List;

import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Provides the names of the variables that a surrounding context makes available at the top level of
 * a TL-Script expression, for use by <code>$</code>-completion.
 *
 * <p>
 * Implementations must have a public no-argument constructor.
 * </p>
 *
 * @see ScriptContextVariables
 */
public interface ScriptContextVariablesProvider {

	/**
	 * The context variable names (without the leading <code>$</code>) for the edited property.
	 *
	 * @param valueModel
	 *        Access to the edited configuration item and property.
	 * @return The variable names; never <code>null</code>.
	 */
	List<String> getVariables(ValueModel valueModel);

}
