/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.StringArrayJsonBinding;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * An object representing a Server Variable for server URL template substitution.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#serverVariableObject"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ServerVariableObject.NAME_ATTRIBUTE,
	ServerVariableObject.DEFAULT,
	ServerVariableObject.ENUM,
})
public interface ServerVariableObject extends NamedConfigMandatory {

	/** Configuration name for the value of {@link #getDefault()}. */
	String DEFAULT = "default";

	/** Configuration name for the value of {@link #getEnum()}. */
	String ENUM = "enum";

	/**
	 * Name of the variable.
	 * 
	 * @see com.top_logic.basic.config.NamedConfigMandatory#getName()
	 */
	@Override
	String getName();

	/**
	 * The default value to use for substitution, which SHALL be sent if an alternate value is not
	 * supplied. Note this behavior is different than the Schema Object's treatment of default
	 * values, because in those cases parameter values are optional. If the enum is defined, the
	 * value MUST exist in the enum's values.
	 */
	@Mandatory
	@Name(DEFAULT)
	String getDefault();

	/**
	 * Setter for {@link #getDefault()}.
	 */
	void setDefault(String value);

	/**
	 * An enumeration of string values to be used if the substitution options are from a limited
	 * set. The array MUST NOT be empty.
	 */
	@Name(ENUM)
	@JsonBinding(StringArrayJsonBinding.class)
	String[] getEnum();

	/**
	 * Setter for {@link #getEnum()}.
	 */
	void setEnum(String[] value);

}

