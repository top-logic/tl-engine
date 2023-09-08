/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the {@link AbstractControlBase#isAttached()
 * attached} state of an control.
 * 
 * @see AbstractControlBase#ATTACHED_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttachedPropertyListener extends PropertyListener {

	/**
	 * Handles change of the {@link AbstractControlBase#isAttached() attached} state of an control.
	 * 
	 * @param sender
	 *        The control whose state had changed.
	 * @param oldValue
	 *        Whether the sender was attached before.
	 * @param newValue
	 *        Whether the sender is now attached.
	 * 
	 * @see AbstractControlBase#isAttached()
	 */
	void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue);

}

