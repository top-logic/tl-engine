/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

/**
 * Listener that is informed about model changes.
 * 
 * @see ModelScope#addModelListener(ModelListener)
 */
public interface ModelListener {

	/**
	 * Processes the given model update.
	 * 
	 * @param change
	 *        The description of the change to the application model.
	 */
	void notifyChange(ModelChangeEvent change);

}
