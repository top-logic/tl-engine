/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * Observer of {@link ModeModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModeModelListener {

	/**
	 * Informs this listener about a mode change.
	 * 
	 * @param sender
	 *        The changed model.
	 * @param oldMode
	 *        The mode before the change.
	 * @param newMode
	 *        The mode after the change.
	 */
	void handleModeChange(Object sender, int oldMode, int newMode);
	
}
