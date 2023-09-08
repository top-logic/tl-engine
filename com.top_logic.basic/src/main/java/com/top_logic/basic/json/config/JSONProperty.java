/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;

/**
 * A json property consists of a key and {@link JSONValue} value.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface JSONProperty extends ConfigurationItem {

	/**
	 * Key to identify the property.
	 */
	public static final String KEY = "key";

	/**
	 * Key for this json property.
	 */
	@Name(KEY)
	String getKey();

	/**
	 * @see #getKey()
	 */
	void setKey(String value);

	/**
	 * {@link JSONValue} value of this property.
	 */
	@DefaultContainer
	JSONValue getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(JSONValue value);
}
