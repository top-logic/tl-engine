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
	 * Listener for {@link SelectFieldModel#getOptions() options} changes.
	 */
	interface SelectOptionsListener {

		/**
		 * Called when the available options change.
		 *
		 * @param source
		 *        The model whose options changed.
		 * @param newOptions
		 *        The new options list.
		 */
		void onOptionsChanged(SelectFieldModel source, List<?> newOptions);
	}

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
	 * Fires {@link SelectOptionsListener#onOptionsChanged(SelectFieldModel, List)}.
	 * </p>
	 */
	void setOptions(List<?> options);

	/**
	 * Adds a listener for options changes.
	 */
	void addOptionsListener(SelectOptionsListener listener);

	/**
	 * Removes a previously added options listener.
	 */
	void removeOptionsListener(SelectOptionsListener listener);
}
