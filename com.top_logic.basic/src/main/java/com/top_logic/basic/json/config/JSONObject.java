/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import java.util.Map;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.TagName;

/**
 * A json object consists of several properties. Json properties are written in key/value pairs.
 * Keys must be {@link String}'s and values are from {@link JSONValue} type.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(JSONObject.TAG_NAME)
public interface JSONObject extends JSONValue {

	/**
	 * Tag name for a json object.
	 */
	public static final String TAG_NAME = "struct";

	/**
	 * A map of properties for this json object.
	 */
	@DefaultContainer
	@Key(JSONProperty.KEY)
	Map<String, JSONProperty> getProperties();

}
