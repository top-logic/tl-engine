/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;

/**
 * Dummy {@link HistoryEntry} that is used as safety element in {@link HistoryControl}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class LoginEntry implements HistoryEntry {
	
	/** Sole instance of {@link LoginEntry}. */
	static final LoginEntry INSTANCE = new LoginEntry();
	
	private LoginEntry() {
		// singleton instance
	}
	
	@Override
	public void undo(DisplayContext context) throws HistoryException {
		// is not undone
		assert false : "SafetyElement must not be undone";
	}

	@Override
	public void redo(DisplayContext context) throws HistoryException {
		// is not redone
		assert false : "SafetyElement must not be redone";
	}

	@Override
	public <T extends Appendable> T appendTitle(DisplayContext context, T out) throws IOException {
		out.append("Login");
		return out;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
