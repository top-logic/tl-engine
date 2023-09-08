/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Map;

/**
 * A {@link MapValueProvider} where the default value is set in the constructor.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class MapDefaultValueProvider extends MapValueProvider {

	private final Object _defaultValue;

	/**
	 * Creates a {@link MapDefaultValueProvider} with the given default value.
	 * 
	 * @param defaultValue
	 *        Is allowed to be <code>null</code>.
	 */
	public MapDefaultValueProvider(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	/**
	 * Creates a {@link MapDefaultValueProvider} with the given default value.
	 * 
	 * @param defaultValue
	 *        Is allowed to be <code>null</code>.
	 * @param initialValues
	 *        See: {@link MapValueProvider#MapValueProvider(Map)}
	 */
	public MapDefaultValueProvider(Map<String, Object> initialValues, Object defaultValue) {
		super(initialValues);
		_defaultValue = defaultValue;
	}

	@Override
	protected Object getDefaultValue(String key) {
		return _defaultValue;
	}

}
