/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link ItemEvent} that touches attribute values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ItemChange extends ItemEvent {

	/**
	 * Creates a new {@link ItemChange}.
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param objectId
	 *        See {@link #getObjectId()}
	 */
	public ItemChange(long revision, ObjectBranchId objectId) {
		super(revision, objectId);
	}

	/** Map holding for each changed attribute the value for this attribute. */
	protected final Map<String, Object> valuesByAttribute = new HashMap<>();

	/**
	 * Map with values for each changed {@link MOAttribute} indexed by attribute
	 * name.
	 */
	public Map<String, Object> getValues() {
		return valuesByAttribute;
	}

	/**
	 * Sets the value of the attribute with the given name to the given new value in this event.
	 * 
	 * <p>
	 * If old value and new value are equal, the entry is dropped.
	 * </p>
	 * 
	 * @see #setValue(String, Object, Object, boolean)
	 */
	public final void setValue(String name, Object oldValue, Object newValue) {
		setValue(name, oldValue, newValue, true);
	}

	/**
	 * Sets the value of the attribute with the given name to the given new value in this event.
	 * 
	 * @param name
	 *        The name of the attribute that is modified in this event.
	 * @param oldValue
	 *        The value of the attribute before this event. <code>null</code> for create events. The
	 *        old value is only relevant for displaying event journals. When creating events for
	 *        re-play, the old value is of no relevance and can always be <code>null</code>.
	 * @param newValue
	 *        The new value of the attribute that was assigned in this event.
	 * @param dropEqualValues
	 *        If <code>true</code>, the value is only recorded, when <code>oldValue</code> and
	 *        <code>newValue</code> are not equal.
	 */
	public abstract void setValue(String name, Object oldValue, Object newValue, boolean dropEqualValues);
	
	/**
	 * The old value set in {@link #setValue(String, Object, Object)}, if this event type supports
	 * old values.
	 * 
	 * @param name
	 *        The name of the attribute to get the value from.
	 * @return The old value or <code>null</code>, if this event typed does not support old values.
	 */
	public abstract Object getOldValue(String name);

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof ItemChange)) {
			return false;
		}
		
		ItemChange otherEvent = ((ItemChange) other);
		return equalsItemChange(otherEvent);
	}

	/**
	 * Checks whether the given {@link ItemChange} is equal to this {@link ItemChange}. Properties
	 * of class {@link ItemChange} and super classes are considered.
	 */
	protected final boolean equalsItemChange(ItemChange otherEvent) {
		return super.equalsItemEvent(otherEvent) && 
			this.valuesByAttribute.equals(otherEvent.valuesByAttribute);
	}

	@Override
	protected void appendProperties(StringBuilder result) {
		super.appendProperties(result);
		result.append(", values: ");
		result.append(this.valuesByAttribute);
	}
	
	/**
	 * Creates an {@link Iterable} the delivers the values of this {@link ItemChange} as application
	 * values, i.e. referenced objects are resolved.
	 * 
	 * @param context The context to resolve references.
	 */
	public Iterable<Entry<String, Object>> applicationValues(final ObjectContext context) {
		final MOClass type = (MOClass) getObjectId().getObjectType();
		return new Iterable<>() {

			@Override
			public Iterator<Entry<String, Object>> iterator() {
				final Iterator<Entry<String, Object>> source = valuesByAttribute.entrySet().iterator();
				return new Iterator<>() {

					@Override
					public boolean hasNext() {
						return source.hasNext();
					}

					@Override
					public Entry<String, Object> next() {
						Entry<String, Object> originalValue = source.next();
						MOAttribute attribute = type.getAttributeOrNull(originalValue.getKey());
						if (attribute == null) {
							return originalValue;
						}
						Object cacheValue = originalValue.getValue();
						Object applicationValue =
							DefaultEventWriter.toApplicationValue(context, attribute, cacheValue, getRevision());
						if (applicationValue == cacheValue) {
							return originalValue;
						}
						if (cacheValue != null && applicationValue == null) {
							assert attribute instanceof MOReference;
							throw new KnowledgeBaseRuntimeException("No reference found for key " + cacheValue);
						}
						return new SimpleImmutableEntry<>(attribute.getName(),
							applicationValue);
					}

					@Override
					public void remove() {
						source.remove();
					}
				};
			}

		};
	}

}
