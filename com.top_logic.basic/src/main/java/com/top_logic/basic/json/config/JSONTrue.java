/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} primitive for the boolean true.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 * 
 * @see JSONBoolean
 */
@TagName(JSONTrue.TAG_NAME)
public interface JSONTrue extends JSONValue {

	/**
	 * Tag name for the boolean true value.
	 */
	public static final String TAG_NAME = "true";

}
