/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} primitive for a float value.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(JSONFloat.TAG_NAME)
public interface JSONFloat extends JSONValue {

	/**
	 * Tag name for the float value.
	 */
	public static final String TAG_NAME = "float";

	/**
	 * Float value.
	 */
	float getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(float value);

}
