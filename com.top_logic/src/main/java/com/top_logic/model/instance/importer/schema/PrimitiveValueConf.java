/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.annotation.TagName;

/**
 * A single element of a primitive value collection.
 */
@TagName("element")
public interface PrimitiveValueConf extends ValueConf {
	/**
	 * The serialized value.
	 */
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);
}
