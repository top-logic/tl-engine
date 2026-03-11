/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

/**
 * Listener for {@link SelectFieldModel#getOptions() options} changes.
 */
public interface OptionsListener {

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

