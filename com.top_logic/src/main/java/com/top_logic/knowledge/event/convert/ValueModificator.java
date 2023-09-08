/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * A {@link ValueModificator} rewrites the values of some attribute of some attribute in a constant
 * way.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueModificator extends RewritingEventVisitor {
	
	/**
	 * The name of the type to look for the attribute
	 */
	private final String _typeName;

	/**
	 * The name of the attribute to change
	 */
	private final String _attribute;

	/**
	 * The rule used to change the value
	 */
	private final Mapping<Object, ?> _valueMapping;

	/**
	 * Creates a {@link ValueModificator}.
	 * 
	 * @param type
	 *        the type whose attribute value must be changed
	 * @param attribute
	 *        the attribute whose value must be changed
	 * @param valueMapping
	 *        the rule how to change the attribute value
	 */
	public ValueModificator(String type, String attribute, Mapping<Object, ?> valueMapping) {
		this._typeName = type;
		this._attribute = attribute;
		this._valueMapping = valueMapping;
	}
	
	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		update(event, event.getValues());
		return super.visitItemChange(event, arg);
	}

	/**
	 * Updates the given value for the given event. Checks whether the type is correct and adopts
	 * the value of the attribute.
	 * 
	 * @param event
	 *        the occurred event to identify the object.
	 * @param values
	 *        the values of the object to adopt
	 */
	protected void update(ItemEvent event, Map<String, Object> values) {
		final String typeName = event.getObjectType().getName();
		if(_typeName.equals(typeName)) {
			if (values.containsKey(_attribute)) {
				final Object oldValue = values.get(_attribute);
				values.put(_attribute, _valueMapping.map(oldValue));
			}
		}
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		update(event, event.getOldValues());
		return super.visitUpdate(event, arg);
	}

}

