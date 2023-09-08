/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;

/**
 * Possible formats of request parameters.
 * 
 * @see ConcreteRequestParameter.Config#getFormat()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ParameterFormat implements ExternallyNamed {

	/**
	 * An arbitrary string.
	 */
	STRING("string"),

	/**
	 * An integer value with 32 bit.
	 */
	INTEGER("integer"),

	/**
	 * An integer value with 64 bit.
	 */
	LONG("long"),

	/**
	 * An floating point value with 32 bit.
	 */
	FLOAT("float"),

	/**
	 * An floating point value with 64 bit.
	 */
	DOUBLE("double"),

	/**
	 * A boolean value.
	 */
	BOOLEAN("boolean"),

	/**
	 * A date value.
	 */
	DATE("date"),

	/**
	 * A date value with time.
	 */
	@Label("Date with time")
	DATE_TIME("date-time"),

	/**
	 * A JSON object.
	 */
	OBJECT("object");

	private final String _externalName;

	/**
	 * This constructor creates a new ParameterFormat.
	 * 
	 */
	private ParameterFormat(String externalName) {
		_externalName = externalName;
	}

	/**
	 * @see com.top_logic.basic.config.ExternallyNamed#getExternalName()
	 */
	@Override
	public String getExternalName() {
		return _externalName;
	}
}
