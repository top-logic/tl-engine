/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.knowledge.objects.identifier.ObjectBranchId;


/**
 * {@link ItemEvent} that represents an object or link creation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ItemCreation extends ItemChange {

	/**
	 * Creates a new {@link ItemCreation}.
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param objectId
	 *        See {@link #getObjectId()}
	 */
	public ItemCreation(long revision, ObjectBranchId objectId) {
		super(revision, objectId);
	}

	@Override
	public void setValue(String name, Object oldValue, Object newValue, boolean dropEqualValues) {
		assert oldValue == null : "In create event '" + this + "', the old value should be null. Try to set oldValue '"
			+ oldValue + "', newValue '" + newValue + "'";
		
		if (newValue == null) {
			assert !valuesByAttribute.containsKey(name) : "Attribute name clash '" + name + "' in item creation '"
				+ this + "'. Try to set null as value, but '" + valuesByAttribute.get(name) + "' was set before";
			if (!dropEqualValues) {
				valuesByAttribute.put(name, newValue);
			}
			return;
		} else {
			Object lastNewValue = valuesByAttribute.put(name, newValue);
			assert lastNewValue == null : "Attribute name clash '" + name + "' in item creation '" + this
				+ "'. Try to set value '" + newValue + "', but '" + lastNewValue + "' was set before";
		}
	}

	@Override
	public Object getOldValue(String name) {
		return null;
	}
}
