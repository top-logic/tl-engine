/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

/**
 * {@link FieldModel} for fields where the value must come from a defined set of options.
 */
public interface SelectFieldModel extends FieldModel {

	/**
	 * The available options.
	 */
	List<?> getOptions();

	/**
	 * Whether multiple options can be selected simultaneously.
	 */
	boolean isMultiple();

	/**
	 * Updates the available options.
	 *
	 * <p>
	 * Fires {@link OptionsListener#onOptionsChanged(SelectFieldModel, List)}.
	 * </p>
	 */
	void setOptions(List<?> options);

	/**
	 * Adds a listener for options changes.
	 */
	void addOptionsListener(OptionsListener listener);

	/**
	 * Removes a previously added options listener.
	 */
	void removeOptionsListener(OptionsListener listener);
}
