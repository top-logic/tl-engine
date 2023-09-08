/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Mutable variant of {@link AbstractFlexData}.
 * 
 * @see ImmutableFlexData
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MutableFlexData extends AbstractFlexData {

	private static final int NOT_PERSISTENT = -1;

	/**
	 * {@link AbstractFlexData.AttributeData} holding an additional base value and a change type.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected static class MutableAttributeData extends AttributeData {

		private Object _baseValue;

		private ChangeType _changeType = ChangeType.UNCHANGED;

		/**
		 * Creates a new {@link MutableAttributeData}.
		 */
		public MutableAttributeData(Object value, long revision) {
			super(value, revision);
		}

		/**
		 * Base value for {@link #value()}.
		 */
		public Object baseValue() {
			return _baseValue;
		}

		/**
		 * Setter for {@link #baseValue()}.
		 */
		public void setBaseValue(Object baseValue) {
			_baseValue = baseValue;
		}

		/**
		 * The kind of change to the attribute.
		 */
		public ChangeType changeType() {
			return _changeType;
		}

		/**
		 * Setter for {@link #changeType()}.
		 */
		public void setChangeType(ChangeType changeType) {
			_changeType = changeType;
		}

		@Override
		public String toString() {
			return "(" + value() + ", " + baseValue() + ", " + revision() + ")";
		}

	}

	/**
	 * Enumeration of change type of flex values.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static enum ChangeType {

		/** Marker that the value was unchanged in the {@link FlexData}. */
		UNCHANGED,

		/** Marker that the value was added to the {@link FlexData}. */
		ADD,

		/** Marker that the value was updated in the {@link FlexData}. */
		UPDATE,

		/** Marker that the value was deleted from the {@link FlexData}. */
		DELETE,

		;

	}

	private boolean _containsBinaryData = false;

	/**
	 * Constructs a new {@link MutableFlexData}.
	 */
	protected MutableFlexData() {
		super();
	}

	protected Iterable<Entry<String, ChangeType>> getChanges() {
		return _valueByName.entrySet()
			.stream()
			.filter(MutableFlexData::isChangedAttribute)
			.map(MutableFlexData::mapToChangeTypeEntry)
			.collect(Collectors.toList());
	}

	private static Entry<String, ChangeType> mapToChangeTypeEntry(Entry<String, AttributeData> entry) {
		ChangeType changeType = ((MutableAttributeData) entry.getValue()).changeType();
		return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), changeType);
	}

	private static boolean isChangedAttribute(Entry<String, AttributeData> entry) {
		return ((MutableAttributeData) entry.getValue()).changeType() != ChangeType.UNCHANGED;
	}

	protected void commitLocally(long commitRevision) {
		Iterator<AttributeData> it = _valueByName.values().iterator();
		while (it.hasNext()) {
			MutableAttributeData data = (MutableAttributeData) it.next();
			switch (data.changeType()) {
				case ADD:
				case UPDATE:
					data.setChangeType(ChangeType.UNCHANGED);
					data.setRevision(commitRevision);
					data.setBaseValue(data.value());
					break;
				case DELETE:
					it.remove();
					break;
				case UNCHANGED:
					// No change
					break;
			}
		}
	}

	/**
	 * Delivers the old values of {@link ChangeType#UPDATE updated} attributes.
	 */
	protected Object oldValue(String attributeName) {
		return ((MutableAttributeData) _valueByName.get(attributeName)).baseValue();
	}

	@Override
	public Object setAttributeValue(String attributeName, Object value) {
		Object oldValue;
		MutableAttributeData knownValue = (MutableAttributeData) _valueByName.get(attributeName);
		if (knownValue == null) {
			oldValue = null;
			if (value == null) {
				return null;
			} else {
				MutableAttributeData attributeData = new MutableAttributeData(value, NOT_PERSISTENT);
				attributeData.setChangeType(ChangeType.ADD);
				_valueByName.put(attributeName, attributeData);
			}
		} else {
			oldValue = knownValue.value();
			knownValue.setValue(value);
			if (value == null) {
				switch( knownValue.changeType()) {
					case ADD:
						// Delete of a new attribute. Remove it from storage.
						_valueByName.remove(attributeName);
						break;
					case DELETE:
						// setting null a second time.
						break;
					case UNCHANGED:
						// Deleting an unchanged attribute.
						knownValue.setChangeType(ChangeType.DELETE);
						break;
					case UPDATE:
						// Deleting an changed attribute.
						knownValue.setChangeType(ChangeType.DELETE);
						break;
				}
			} else {
				switch( knownValue.changeType()) {
					case ADD:
						// changed a new attribute; attribute remains new.
						break;
					case DELETE:
						// deleted attribute re-animated. As there is an entry the entry can not be
						// a deleted new attribute, therefore it is an update.
						knownValue.setChangeType(ChangeType.UPDATE);
						break;
					case UPDATE:
						// change of an updated attribute.
						break;
					case UNCHANGED:
						// first change of an persitent attribute.
						knownValue.setChangeType(ChangeType.UPDATE);
						break;
				}
			}
		}
		_containsBinaryData |= value instanceof BinaryData;

		return oldValue;
	}

	public boolean containsBinaryData() {
		return _containsBinaryData;
	}

	@Override
	protected AttributeData newAttributeData(Object value, long revMin) {
		MutableAttributeData data = new MutableAttributeData(value, revMin);
		data.setBaseValue(value);
		return data;
	}

}

