/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DateFormat;
import java.util.Locale;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Enumeration for {@link DateFormat} styles.
 * 
 * @see DateFormat#getDateInstance(int)
 */
public enum DateStyle implements ExternallyNamed {

	/**
	 * @see DateFormat#FULL
	 */
	FULL("full", DateFormat.FULL),

	/**
	 * @see DateFormat#LONG
	 */
	LONG("long", DateFormat.LONG),

	/**
	 * @see DateFormat#MEDIUM
	 */
	MEDIUM("medium", DateFormat.MEDIUM),

	/**
	 * @see DateFormat#SHORT
	 */
	SHORT("short", DateFormat.SHORT);

	private final String _name;

	private final int _intValue;

	private DateStyle(String name, int intValue) {
		_name = name;
		_intValue = intValue;
	}

	@Override
	public String getExternalName() {
		return _name;
	}

	/**
	 * A value compatible to {@link DateFormat#getDateInstance(int, Locale)}.
	 */
	public int intValue() {
		return _intValue;
	}

}