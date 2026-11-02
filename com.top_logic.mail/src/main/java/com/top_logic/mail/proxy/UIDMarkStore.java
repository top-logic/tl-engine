/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.basic.Logger;
import com.top_logic.basic.db.schema.properties.DBProperties;

/**
 * Persistent storage of a {@link UIDMark} as a cluster-global {@link DBProperties database
 * property}.
 */
public class UIDMarkStore {

	/**
	 * Prefix of the {@link DBProperties} key a {@link UIDMark} is stored under.
	 *
	 * @see #forName(DBProperties, String)
	 */
	public static final String KEY_PREFIX = "mail.uid-mark.";

	private final DBProperties _properties;

	private final String _key;

	/**
	 * Creates a {@link UIDMarkStore}.
	 *
	 * @param properties
	 *        The database properties to store the mark in.
	 * @param key
	 *        The property key of the mark in the {@link DBProperties#GLOBAL_PROPERTY global node}.
	 */
	public UIDMarkStore(DBProperties properties, String key) {
		_properties = properties;
		_key = key;
	}

	/**
	 * Creates a {@link UIDMarkStore} for the mark with the given name.
	 *
	 * @param properties
	 *        The database properties to store the mark in.
	 * @param name
	 *        The name of the mark, e.g. the name of the task reading the folder. The property key
	 *        is the name prefixed with {@link #KEY_PREFIX}.
	 */
	public static UIDMarkStore forName(DBProperties properties, String name) {
		return new UIDMarkStore(properties, KEY_PREFIX + name);
	}

	/**
	 * The property key of the mark.
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Reads the stored mark.
	 *
	 * @return The stored mark, <code>null</code> if no mark is stored or the stored value is
	 *         invalid.
	 */
	public UIDMark load() {
		String value = _properties.getProperty(DBProperties.GLOBAL_PROPERTY, _key);
		try {
			return UIDMark.parse(value);
		} catch (IllegalArgumentException ex) {
			Logger.warn("Ignoring invalid value of database property '" + _key + "'.", ex, UIDMarkStore.class);
			return null;
		}
	}

	/**
	 * Stores the given mark.
	 *
	 * @param mark
	 *        The mark to store, <code>null</code> to remove the stored mark.
	 */
	public void store(UIDMark mark) {
		_properties.setProperty(DBProperties.GLOBAL_PROPERTY, _key, mark == null ? null : mark.format());
	}

}
