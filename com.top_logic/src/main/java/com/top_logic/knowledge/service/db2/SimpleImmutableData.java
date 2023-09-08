/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.AbstractMap.SimpleImmutableEntry;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Immutable implementation of {@link NamedValues} with exactly one value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class SimpleImmutableData extends SimpleImmutableEntry<String, Object> implements NamedValues {

	SimpleImmutableData(String key, Object value) {
		super(key, value);
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] { getKey() };
	}

	@Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
		if (hasAttribute(attrName)) {
			return getValue();
		}
		throw new NoSuchAttributeException("No attribute " + attrName);
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		throw new IllegalStateException("Immutable object cannot be modified.");
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return attributeName.equals(getKey());
	}

}

