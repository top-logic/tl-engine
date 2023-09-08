/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.log.I18NConstants;

/**
 * Location within a resource.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Location {

	/**
	 * Result of {@link #getLine()}, if line information is not available.
	 */
	public static final int NO_LINE = -1;

	/**
	 * Result of {@link #getColumn()}, if column information is not available.
	 */
	int NO_COLUMN = -1;

	/**
	 * The name of the resource this {@link Location} points into, <code>null</code> if not
	 * available.
	 */
	String getResource();

	/**
	 * The line number this {@link Location} points to, {@link #NO_LINE} if not available.
	 */
	int getLine();

	/**
	 * The column number this {@link Location} points to, {@link #NO_COLUMN} if not available.
	 */
	int getColumn();

	/**
	 * Wraps the given message with this location information.
	 */
	default ResKey withLocation(ResKey errorKey) {
		if (StringServices.isEmpty(getResource())) {
			return I18NConstants.AT_LOCATION__LINE_COL_DETAIL.fill(getLine(), getColumn(), errorKey);
		} else {
			return I18NConstants.AT_LOCATION__FILE_LINE_COL_DETAIL.fill(getResource(), getLine(), getColumn(),
				errorKey);
		}
	}

	/**
	 * Whether the given key is already wrapped with {@link #withLocation(ResKey) location
	 * information}.
	 */
	static boolean hasLocation(ResKey key) {
		ResKey plainKey = key.plain();
		return plainKey == I18NConstants.AT_LOCATION__LINE_COL_DETAIL
			|| plainKey == I18NConstants.AT_LOCATION__FILE_LINE_COL_DETAIL;
	}

	/**
	 * The detail key of a key wrapped with {@link #withLocation(ResKey)}.
	 */
	static ResKey detail(ResKey key) {
		ResKey plainKey = key.plain();
		if (plainKey == I18NConstants.AT_LOCATION__LINE_COL_DETAIL) {
			return (ResKey) key.arguments()[2];
		} else if (plainKey == I18NConstants.AT_LOCATION__FILE_LINE_COL_DETAIL) {
			return (ResKey) key.arguments()[3];
		} else {
			return key;
		}
	}
}
