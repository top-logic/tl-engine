/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Local {@link FlexData} implementation for {@link AbstractFlexDataManager}.
 * 
 * @since 5.8.0
 * 
 *        com.top_logic.knowledge.service.db2.AbstractFlexDataManager
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFlexData implements FlexData {

	/**
	 * Data holder for an attribute value together with the revision of the last update.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected static class AttributeData {

		private Object _value;

		private long _revision;

		/**
		 * Creates a new {@link AttributeData}.
		 */
		public AttributeData(Object value, long revision) {
			setValue(value);
			setRevision(revision);
		}

		/**
		 * The actual value for the attribute.
		 */
		public Object value() {
			return _value;
		}

		/**
		 * Setter for {@link #value()}.
		 */
		public void setValue(Object value) {
			_value = value;
		}

		/**
		 * Last modification revision of the value.
		 */
		public long revision() {
			return _revision;
		}

		/**
		 * This method sets the revision.
		 *
		 * @param revision
		 *        The revision to set.
		 */
		public void setRevision(long revision) {
			_revision = revision;
		}

		@Override
		public String toString() {
			return "(" + value() + ", " + revision() + ")";
		}

	}

	/**
	 * Creates a mutable copy of the source {@link FlexData}.
	 * 
	 * @param source
	 *        An immutable {@link FlexData}. Data are copied.
	 */
	public static FlexData createMutableCopy(FlexData source) {
		MutableFlexData mutableData = new MutableFlexData();
		for (String attributeName : source.getAttributes()) {
			try {
				mutableData.initAttributeValue(attributeName, source.getAttributeValue(attributeName),
					source.lastModified(attributeName));
			} catch (NoSuchAttributeException ex) {
				String error = "Source NamedValues has '" + attributeName + "' as attribute.";
				throw new IllegalArgumentException(error, ex);
			}
		}
		return mutableData;
	}

	/**
	 * Creates an immutable copy of the source {@link FlexData}.
	 * 
	 * @param source
	 *        Some {@link FlexData} .
	 */
	public static FlexData makeImmutable(FlexData source) {
		if (source == NoFlexData.INSTANCE) {
			return source;
		} else if (source instanceof ImmutableFlexData) {
			return source;
		} else {
			ImmutableFlexData immutableData = new ImmutableFlexData();
			for (String attributeName : source.getAttributes()) {
				try {
					immutableData.initAttributeValue(attributeName, source.getAttributeValue(attributeName),
						source.lastModified(attributeName));
				} catch (NoSuchAttributeException ex) {
					String error = "Source NamedValues has '" + attributeName + "' as attribute.";
					throw new IllegalArgumentException(error, ex);
				}
			}
			return immutableData;
		}
	}

	/** Storage map holding the actual data */
	protected final Map<String, AttributeData> _valueByName;

	private final Set<String> _atributeNames;
	
	/**
	 * Constructs a new {@link AbstractFlexData}.
	 */
	protected AbstractFlexData() {
		_valueByName = new HashMap<>();
		_atributeNames = Collections.unmodifiableSet(_valueByName.keySet());
	}

	@Override
	public Collection<String> getAttributes() {
		return _atributeNames;
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return _valueByName.containsKey(attributeName);
	}

	@Override
	public Object getAttributeValue(String attributeName) {
		AttributeData attributeData = _valueByName.get(attributeName);
		if (attributeData == null) {
			return null;
		}
		return attributeData.value();
	}

	@Override
	public long lastModified(String attributeName) {
		AttributeData attributeData = _valueByName.get(attributeName);
		if (attributeData != null) {
			return attributeData.revision();
		} else {
			return -1;
		}
	}

	void initAttributeValue(String attributeName, Object value, long revMin) {
		_valueByName.put(attributeName, newAttributeData(value, revMin));
	}

	/**
	 * Creates a new {@link AttributeData storage object} for the given attribute.
	 */
	protected AttributeData newAttributeData(Object value, long revMin) {
		return new AttributeData(value, revMin);
	}

}
