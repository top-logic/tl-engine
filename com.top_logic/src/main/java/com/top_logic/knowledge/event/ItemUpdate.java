/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.util.Utils;

/**
 * {@link ItemEvent} that represents the update of object or link attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemUpdate extends ItemChange {

	private final Map<String, Object> oldValuesByAttribute;

	/**
	 * Creates an {@link ItemUpdate}.
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param objectId
	 *        See {@link #getObjectId()}
	 * @param keepOldValues
	 *        Whether {@link #getOldValues() old values} should be recorded in
	 *        calls to {@link #setValue(String, Object, Object)}.
	 */
	public ItemUpdate(long revision, ObjectBranchId objectId, boolean keepOldValues) {
		super(revision, objectId);
		this.oldValuesByAttribute = keepOldValues ? new HashMap<>() : null;
	}

	private boolean keepOldValues() {
		return oldValuesByAttribute != null;
	}

	@Override
	public EventKind getKind() {
		return EventKind.update;
	}

	@Override
	public <R,A> R visitItemEvent(ItemEventVisitor<R,A> v, A arg) {
		return v.visitUpdate(this, arg);
	}

	@Override
	public void setValue(String name, Object oldValue, Object newValue, boolean dropEqualValues) {
		// Drop overrides with the same value. 
		if (dropEqualValues && Utils.equals(newValue, oldValue)) {
			return;
		}
		
		Object lastNewValue = valuesByAttribute.put(name, newValue);
		assert lastNewValue == null : "Multiple non-null updates for the same attribute.";
		
		if (keepOldValues()) {
			// Preserve old value of first set. This is necessary, because
			// flexible attribute updates produce two adds (first one resets the
			// attribute to null, the second one sets the attribute to the new
			// value).
			if (! oldValuesByAttribute.containsKey(name)) {
				oldValuesByAttribute.put(name, oldValue);
			} else {
				assert newValue != null : "More than one null update.";
			}
		}
	}

	@Override
	public Object getOldValue(String name) {
		if (keepOldValues()) {
			return oldValuesByAttribute.get(name);
		} else {
			return null;
		}
	}

	/**
	 * The old values of attributes just before this event.
	 * 
	 * <p>
	 * The values are index by their attribute name.
	 * </p>
	 * 
	 * <p>
	 * The old values are only relevant when displaying event journals. See
	 * {@link #setValue(String, Object, Object)}.
	 * </p>
	 * 
	 * @return The old values indexed by attribute name, or <code>null</code>,
	 *         if old values are not available.
	 */
	public Map<String, Object> getOldValues() {
		return oldValuesByAttribute;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof ItemUpdate)) {
			return false;
		}
		
		ItemUpdate otherEvent = ((ItemUpdate) other);
		return equalsItemUpdate(otherEvent);
	}

	private final boolean equalsItemUpdate(ItemUpdate otherEvent) {
		return super.equalsItemChange(otherEvent) && 
			Utils.equals(this.oldValuesByAttribute, otherEvent.oldValuesByAttribute);
	}
	
	@Override
	protected void appendProperties(StringBuilder result) {
		super.appendProperties(result);
		if (keepOldValues()) {
			result.append(", oldValues: ");
			result.append(this.oldValuesByAttribute);
		}
	}

}
