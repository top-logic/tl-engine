/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import com.top_logic.basic.config.NamedConfigMandatory;

/**
 * Definition of a property in an {@link ObjectSchema}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ObjectSchemaProperty extends NamedConfigMandatory {

	/**
	 * Whether this property must be set.
	 */
	boolean isRequired();

	/**
	 * Setter for {@link #isRequired()}.
	 */
	void setRequired(boolean value);

	/**
	 * The {@link Schema} that a value for this property must satisfy.
	 */
	Schema getSchema();

	/**
	 * Setter for {@link #getSchema()}.
	 */
	void setSchema(Schema value);

}
