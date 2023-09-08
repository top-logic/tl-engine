/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Map;

import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * {@link RewritingEventVisitor} rewriting the values of {@link ItemChange} and old values of
 * {@link ItemUpdate}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractValueConverter extends RewritingEventVisitor {

	/**
	 * Translates the values of the event using {@link #translateValues(ItemChange, Map)}.
	 * 
	 * @see #visitItemEvent(ItemEvent, Void)
	 */
	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		translateValues(event, event.getValues());
		return super.visitItemChange(event, arg);
	}

	/**
	 * Translates the old values of the event using {@link #translateValues(ItemChange, Map)}.
	 * 
	 * @see #visitItemChange(ItemChange, Void)
	 */
	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		final Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			translateValues(event, oldValues);
		}
		return super.visitUpdate(event, arg);
	}

	/**
	 * Translates the values of an {@link ItemChange} (and the {@link ItemUpdate#getOldValues() old
	 * values} of an {@link ItemUpdate} if non <code>null</code>).
	 */
	protected abstract void translateValues(ItemChange event, Map<String, Object> values);

}
