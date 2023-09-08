/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} primitive for an integer value.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(JSONInteger.TAG_NAME)
public interface JSONInteger extends JSONValue {

	/**
	 * Tag name for an integer value.
	 */
	public static final String TAG_NAME = "integer";

	/**
	 * Integer value.
	 */
	int getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(int value);
}
