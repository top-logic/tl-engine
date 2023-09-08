/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Map;

import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link ItemEvent} that represents an object or link deletion.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemDeletion extends ItemChange {

	/**
	 * Creates a {@link ItemDeletion}.
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param objectId
	 *        See {@link #getObjectId()}
	 */
	public ItemDeletion(long revision, ObjectBranchId objectId) {
		super(revision, objectId);
	}

	@Override
	public EventKind getKind() {
		return EventKind.delete;
	}

	/**
	 * Service method to set old value.
	 * 
	 * <p>
	 * Delegates to {@link #setValue(String, Object, Object)} with null as new value.
	 * </p>
	 */
	public final void setValue(String name, Object oldValue) {
		setValue(name, oldValue, null);
	}

	/**
	 * Returns the attribute values of the deleted at deletion time.
	 * 
	 * @see com.top_logic.knowledge.event.ItemChange#getValues()
	 */
	@Override
	public Map<String, Object> getValues() {
		return super.getValues();
	}

	@Override
	public void setValue(String name, Object oldValue, Object newValue, boolean dropEqualValues) {
		assert newValue == null : "In delete event '" + this + "', the new value should be null. Try to set oldValue '"
			+ oldValue + "', newValue '" + newValue + "'";
		if (oldValue == null) {
			assert !valuesByAttribute.containsKey(name) : "Attribute name clash '" + name + "' in item deletion '"
				+ this + "'. Try to set null as value, but '" + valuesByAttribute.get(name) + "' was set before";
			if (!dropEqualValues) {
				valuesByAttribute.put(name, oldValue);
			}
			return;
		} else {
			Object lastOldValue = valuesByAttribute.put(name, oldValue);
			assert lastOldValue == null : "Attribute name clash '" + name + "' in item deletion '" + this
				+ "'. Try to set value '" + oldValue + "', but '" + lastOldValue + "' was set before";
		}
	}

	@Override
	public Object getOldValue(String name) {
		return valuesByAttribute.get(name);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof ItemDeletion)) {
			return false;
		}
		
		ItemDeletion otherEvent = ((ItemDeletion) other);
		return equalsItemChange(otherEvent);
	}

	@Override
	public <R,A> R visitItemEvent(ItemEventVisitor<R,A> v, A arg) {
		return v.visitDelete(this, arg);
	}

}
