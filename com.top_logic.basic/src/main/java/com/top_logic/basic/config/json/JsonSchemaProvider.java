/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Describes the JSON form of the values of a configuration {@link PropertyDescriptor property} as
 * JSON {@link Schema}.
 *
 * <p>
 * Implemented by a {@link JsonValueBinding}, and optionally by a
 * {@link ConfigurationValueProvider} whose string form needs more explanation than a plain string
 * schema gives - e.g. the form of an identifier and what it must name.
 * {@link JsonConfigSchemaBuilder} uses it for every property whose value binding or format
 * implements it.
 * </p>
 *
 * @see JsonConfigSchemaBuilder
 */
public interface JsonSchemaProvider {

	/**
	 * Builds a JSON {@link Schema} describing the values of the given property.
	 *
	 * <p>
	 * A {@link Schema#getDescription() description} of the result describes the value form. The
	 * builder places the property's own description in front of it.
	 * </p>
	 *
	 * @param property
	 *        The {@link PropertyDescriptor} to build the schema for.
	 * @return The JSON Schema describing the values of the property.
	 */
	Schema buildSchema(PropertyDescriptor property);

}
