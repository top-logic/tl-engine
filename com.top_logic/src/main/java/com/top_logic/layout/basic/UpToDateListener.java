/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the "up to date" state of an control, i.e.
 * whether the control has updates for the GUI or not.
 * 
 * @see AbstractControlBase#UP_TO_DATE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UpToDateListener extends PropertyListener {

	/**
	 * Whether the control is "up to date".
	 * 
	 * @param sender
	 *        The sending {@link AbstractConstantControlBase}.
	 * @param newValue
	 *        <code>true</code> iff the sender has no updates for the GUI.
	 */
	void isUpToDate(AbstractControlBase sender, Boolean oldValue, Boolean newValue);

}

