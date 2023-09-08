/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that is informed, if the layouting information in a {@link Box}
 * hierarchy has changed.
 * 
 * @see Box#LAYOUT_CHANGE
 * @see #layoutChanged(Box)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BoxListener extends PropertyListener {

	/**
	 * The layout of the given {@link Box} has changed.
	 * 
	 * @param changed
	 *        The changed box.
	 * @return Whether propagation of this event to the parent box should be suppressed.
	 */
	Bubble layoutChanged(Box changed);

}
