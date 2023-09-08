/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.attribute;

import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;

/**
 * Removes the value of a given attribute of an event matching a given filter.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveAttributeValue extends RewritingEventVisitor {

	private final String _attributeName;

	private final Filter<? super ItemChange> _eventFilter;

	/**
	 * Creates a new {@link RemoveAttributeValue}.
	 * 
	 * @param eventFilter
	 *        Filter of the events to remove attribute from.
	 * @param attributeName
	 *        The name of the attribute to remove value for.
	 */
	public RemoveAttributeValue(Filter<? super ItemChange> eventFilter, String attributeName) {
		_eventFilter = eventFilter;
		_attributeName = attributeName;
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		if (correctEvent(event)) {
			removeAttribute(event.getValues());
		}
		return super.visitItemChange(event, arg);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		if (correctEvent(event)) {
			Map<String, Object> oldValues = event.getOldValues();
			if (oldValues != null) {
				removeAttribute(oldValues);
			}
		}
		return super.visitUpdate(event, arg);
	}

	private void removeAttribute(Map<String, Object> values) {
		values.remove(_attributeName);
	}

	private boolean correctEvent(ItemChange event) {
		return _eventFilter.accept(event);
	}

	/**
	 * Removes the value of a given attribute of a given type.
	 * 
	 * <p>
	 * In each event the value of the given attribute is removed from the values map, i.e. it is
	 * like the attribute was never set.
	 * </p>
	 */
	public static RemoveAttributeValue removeAllAttributeValues(String koType, String attributeName) {
		return new RemoveAttributeValue(new SameKOTypeFilter(koType), attributeName);
	}

}

