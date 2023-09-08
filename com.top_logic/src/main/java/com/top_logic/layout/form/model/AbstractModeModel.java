/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.col.InlineList;

/**
 * Base class for {@link ModeModel} implementations implementing listener
 * handling.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractModeModel implements ModeModel {

	/**
	 * {@link InlineList} of {@link ModeModelListener}s.
	 */
	private Object listeners = InlineList.newInlineList();
	
	/**
	 * Sends an event to all registered {@link ModeModelListener}s.
	 * 
	 * @param oldMode
	 *        the mode before the change.
	 * @param newMode
	 *        the mode after the change.
	 */
	protected void fireModeChange(int oldMode, int newMode) {
		Object[] listenerArray = InlineList.toArray(listeners);
		for (int n = 0, cnt = listenerArray.length; n < cnt; n++) {
			((ModeModelListener) listenerArray[n]).handleModeChange(this, oldMode, newMode);
		}
	}
	
	/**
	 * Whether some listeners are currently registered.
	 */
	protected boolean hasModeModelListeners() {
		return ! InlineList.isEmpty(listeners);
	}

	@Override
	public void addModeModelListener(ModeModelListener listener) {
		if (! InlineList.contains(listeners, listener)) {
			this.listeners = InlineList.add(ModeModelListener.class, listeners, listener);
		}
	}

	@Override
	public void removeModeModelListener(ModeModelListener listener) {
		this.listeners = InlineList.remove(listeners, listener);
	}

}
