/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;


/**
 * Utilities for {@link SortConfig}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SortConfigFactory {

	/**
	 * Creates a {@link SortConfig}.
	 * 
	 * @see #ascending(String)
	 * @see #descending(String)
	 */
	public static SortConfig sortConfig(String columnName, boolean ascending) {
		KeyValueBuffer<String, String> initialValues = new KeyValueBuffer<>(2);
		initialValues.put(SortConfig.COLUMN_PROPERTY, columnName);
		initialValues.put(SortConfig.ASCENDING_PROPERTY, Boolean.toString(ascending));
		try {
			return TypedConfiguration.newConfigItem(SortConfig.class, initialValues);
		} catch (ConfigurationException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Creates an {@link SortConfig#getAscending() ascending} {@link SortConfig}.
	 * 
	 * @param columnName
	 *        The name of the column.
	 */
	public static SortConfig ascending(String columnName) {
		return sortConfig(columnName, true);
	}

	/**
	 * Creates an {@link SortConfig#getAscending() descending} {@link SortConfig}.
	 * 
	 * @param columnName
	 *        The name of the column.
	 */
	public static SortConfig descending(String columnName) {
		return sortConfig(columnName, false);
	}

}