/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import java.util.List;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * {@link Schema} defining an structured object as value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ObjectSchema extends Schema {

	@Override
	@StringDefault(OpenAPISchemaConstants.SCHEMA_TYPE_OBJECT)
	String getType();

	/**
	 * Allowed properties for the value.
	 */
	@Key(ObjectSchemaProperty.NAME_ATTRIBUTE)
	List<ObjectSchemaProperty> getProperties();

}
