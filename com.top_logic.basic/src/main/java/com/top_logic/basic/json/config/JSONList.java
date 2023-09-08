/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import java.util.List;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link JSONValue} for a list container.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(JSONList.TAG_NAME)
public interface JSONList extends JSONValue {

	/**
	 * Tag name for the list container.
	 */
	public static final String TAG_NAME = "list";

	/**
	 * List of json objects.
	 */
	@DefaultContainer
	List<JSONValue> getContent();
}
