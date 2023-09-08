/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of a value provider representing a map.
 * 
 * @see MapDefaultValueProvider
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MapValueProvider implements ValueProvider {

    private final Map<String, Object> _data;

    /**
     * Creates a {@link MapValueProvider} with no values set.
     */
    public MapValueProvider() {
		this(new HashMap<>());
    }

    /**
	 * Creates a {@link MapValueProvider}.
	 * 
	 * @param initialValues
	 *        The initial key value binding. Must not be <code>null</code>. The given {@link Map} is
	 *        stored directly and not copied. That means, changes to it also change this
	 *        {@link MapValueProvider}.
	 */
    public MapValueProvider(Map<String, Object> initialValues) {
		assert initialValues != null : "Initial values must not be null.";
        _data = initialValues;
    }

    @Override
	public Object getValue(String aKey) {
        Object result = _data.get(aKey);
		if (result == null && !_data.containsKey(aKey)) {
			return getDefaultValue(aKey);
		}
		return result;
    }

	/**
	 * The value to report in {@link #getValue(String)}, if no value has been set.
	 * 
	 * @param aKey
	 *        The requested key.
	 * @return Might be <code>null</code>.
	 */
	protected Object getDefaultValue(String aKey) {
		return null;
	}

    @Override
	public void setValue(String aName, Object aValue) {
        _data.put(aName, aValue);
    }
}

