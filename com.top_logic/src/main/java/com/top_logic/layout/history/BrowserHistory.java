/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

/**
 * The {@link BrowserHistory} represents the browser side history, i.e. each
 * time the user does an action which can be undone then an {@link HistoryEntry}
 * is added to the history and the user gets the possibility to undo this action
 * by pushing the "browser back" button.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BrowserHistory {

	/**
	 * Adds the given {@link HistoryEntry} to the history. If the user pushes
	 * the "browser back" button directly after the given entry was added then
	 * this action will be undone.
	 * 
	 * @param entry
	 *        the entry which represents the action done by the user. must not
	 *        be <code>null</code>
	 */
	void addHistory(HistoryEntry entry);

	/**
	 * Informs the {@link BrowserHistory} that it is now impossible to undo the
	 * actions added until now, i.e. if the user pushes "browser back" then no
	 * action will be undone. A new "stack" of history is started. Actions added
	 * from now on can be undone until this point in history but not further.
	 * 
	 * @see #pop()
	 */
	void push();

	/**
	 * Informs the {@link BrowserHistory} that the formerly added "stack" can be
	 * removed, i.e. the {@link HistoryEntry entries} done before push can be
	 * undone.
	 * 
	 * It is now impossible to undo or redo entries added between the last push
	 * and now.
	 * 
	 *@see #push()
	 */
	void pop();

}
