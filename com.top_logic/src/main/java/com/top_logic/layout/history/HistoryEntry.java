/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;

/**
 * A history entry represents some action which was executed on the server. It
 * provides methods to {@link #undo(DisplayContext) undo} and
 * {@link #redo(DisplayContext) redo} its represented action.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface HistoryEntry {

	/**
	 * Reverts the action represented by this {@link HistoryEntry}.
	 * 
	 * @param context
	 *        the {@link DisplayContext} wrapping informations about the world
	 *        during calling the method.
	 * 
	 * @throws HistoryException
	 *         if the state of the application does not allow to undo the
	 *         represented action.
	 */
	void undo(DisplayContext context) throws HistoryException;

	/**
	 * Repeats the represented action after it was reverted by
	 * {@link #undo(DisplayContext)}.
	 * 
	 * @param context
	 *        the {@link DisplayContext} wrapping informations about the world
	 *        during calling the method.
	 * 
	 * @throws HistoryException
	 *         if the state of the application does not allow to redo the
	 *         represented action.
	 */
	void redo(DisplayContext context) throws HistoryException;

	/**
	 * Appends a title to the given {@link Appendable} which will be shown in
	 * the browser in the history bar which informs the user about the action
	 * this entry is representing.
	 * 
	 * @param <T>
	 *        the concrete sub type of the {@link Appendable} <code>out</code>.
	 * @param context
	 *        the context to get informations about the world during execute
	 *        this method, e.g. {@link DisplayContext#getResources() resources}.
	 * @param out
	 *        the {@link Appendable} to append the title to
	 * 
	 * @return a representation of the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the {@link Appendable} throws some
	 */
	<T extends Appendable> T appendTitle(DisplayContext context, T out) throws IOException;

}
