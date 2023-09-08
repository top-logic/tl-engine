/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the read only state of a {@link DataField}.
 * 
 * @see DataField#READ_ONLY_PROPERTY
 * 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReadOnlyListener extends PropertyListener {

	/**
	 * Handles change of the read only state of the given {@link DataField}.
	 * 
	 * @param sender
	 *        {@link DataField} whose read only state changed.
	 * @param wasReadOnly
	 *        Whether the {@link DataField} was read only before.
	 * @param isReadOnly
	 *        Whether the {@link DataField} is now read only.
	 * @return Whether this event shall bubble.
	 * 
	 * @see DataField#isReadOnly()
	 */
	Bubble handleReadOnlyChanged(DataField sender, Boolean wasReadOnly, Boolean isReadOnly);

}

