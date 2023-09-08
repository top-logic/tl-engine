/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.NumberFormat;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ExternallyNamed;

/**
 * Type of result a {@link NumberFormat} should produce.
 * 
 * @see DecimalFormatDefinition.Config#getResultType()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum NumberFormatResult implements ExternallyNamed {

	/**
	 * Any {@link Number} depending on the actual user input.
	 */
	NUMBER("number"),

	/**
	 * Always a {@link Double}.
	 */
	DOUBLE("double"),

	/**
	 * Always a {@link Long}, parsing should fail, if any fractional digits have been entered.
	 */
	LONG("long"),

	;

	private final String _name;

	private NumberFormatResult(String name) {
		_name = name;
	}

	@Override
	public String getExternalName() {
		return _name;
	}

	/**
	 * Wraps the given format to ensure the given result type.
	 */
	public NumberFormat adapt(NumberFormat format) {
		switch (this) {
			case NUMBER:
				return format;
			case DOUBLE:
				return DoubleFormat.newInstance(format);
			case LONG:
				return LongFormat.newInstance(format);
			default:
				throw new UnreachableAssertion("No such result type: " + this);
		}
	}

}
