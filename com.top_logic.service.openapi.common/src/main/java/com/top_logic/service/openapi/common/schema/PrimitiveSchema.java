/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

/**
 * {@link Schema} representing a primitive value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PrimitiveSchema extends Schema {

	@Override
	String getType();

	/**
	 * Setter for {@link #getType()}.
	 */
	void setType(String type);

	/**
	 * The format that the primitive value must satisfy.
	 */
	String getFormat();

	/**
	 * Setter for {@link #getFormat()}.
	 */
	void setFormat(String value);

}
