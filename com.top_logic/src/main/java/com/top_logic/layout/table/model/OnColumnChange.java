/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * Callback interface that is informed about column changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OnColumnChange {

	/**
	 * Guard implementation that ignores events.
	 */
	OnColumnChange IGNORE_CHANGE = new OnColumnChange() {
		@Override
		public void handleColumnsChanged() {
			// Ignore.
		}
	};

	/**
	 * The set of columns has changed.
	 */
	public void handleColumnsChanged();

}
