/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Base class for {@link ListDataListener} that are not interested on the concrete event type.
 * 
 * @see #listChanged(ListDataEvent)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ListDataAdapter implements ListDataListener {

	@Override
	public void intervalRemoved(ListDataEvent e) {
		listChanged(e);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		listChanged(e);
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		listChanged(e);
	}

	/**
	 * Called for all {@link ListDataEvent}s.
	 * 
	 * @param e
	 *        The event that was sent.
	 */
	protected void listChanged(ListDataEvent e) {
		// Ignore.
	}

}
