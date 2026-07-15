/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Listener for {@link FormModel} state changes.
 */
public interface FormModelListener {

	/**
	 * Called when the form's state changes (object switched, mode changed, overlay reset).
	 *
	 * @param source
	 *        The form model that changed.
	 */
	void onFormStateChanged(FormModel source);
}
