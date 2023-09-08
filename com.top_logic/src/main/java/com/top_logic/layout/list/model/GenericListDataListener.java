/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * {@link ListDataListener} that forwards all calls to a single
 * {@link #handleListEvent(ListDataEvent)} method.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericListDataListener implements ListDataListener {

	@Override
	public void contentsChanged(ListDataEvent e) {
		handleListEvent(e);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		handleListEvent(e);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		handleListEvent(e);
	}
	
	public abstract void handleListEvent(ListDataEvent e);
}