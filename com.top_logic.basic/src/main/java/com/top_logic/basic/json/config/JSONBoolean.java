/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} for a primitive boolean value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(JSONBoolean.TAG_NAME)
public interface JSONBoolean extends JSONValue {

	/**
	 * Tag name for a boolean value.
	 * 
	 * @see #getValue()
	 */
	String TAG_NAME = "boolean";

	/**
	 * The boolean value.
	 */
	boolean getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(boolean value);
}
