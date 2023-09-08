/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} notifying about {@link CockpitControl#getMaximizedControl()} changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MaximizedControlListener extends PropertyListener {

	/**
	 * Notifies about a change in the {@link CockpitControl#getMaximizedControl() property}.
	 * 
	 * @param sender
	 *        The changed {@link CockpitControl}.
	 * @param oldValue
	 *        The previously maximized control, or <code>null</code> if nothing was maximized
	 *        before.
	 * @param newValue
	 *        The newly maximized control, or <code>null</code> if noting is maximized now.
	 */
	void notifyMaximizedControlChanged(CockpitControl sender, LayoutControl oldValue, LayoutControl newValue);

}
