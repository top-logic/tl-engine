/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} primitive for null.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(JSONNull.TAG_NAME)
public interface JSONNull extends JSONValue {

	/**
	 * Tag name for null.
	 */
	public static final String TAG_NAME = "null";

}
