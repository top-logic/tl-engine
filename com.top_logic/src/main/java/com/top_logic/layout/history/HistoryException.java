/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

/**
 * The {@link HistoryException} is thrown by {@link HistoryEntry} iff some undo
 * or redo action does not work doue to the state of the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoryException extends Exception {
	
	public HistoryException(Throwable cause) {
		super(cause);
	}

}
