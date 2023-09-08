/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;


/**
 * Super class of SQL parameter ({@link SQLParameter} and {@link SQLSetParameter}).
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSQLParameter extends AbstractSQLExpression {

	private String name;

	private Conversion conversion;

	AbstractSQLParameter(Conversion conversion, String name) {
		assert name != null;

		this.conversion = conversion;
		this.name = name;
	}

	/**
	 * the name of this parameter
	 */
	public String getName() {
		return name;
	}

	/** @see #getName() */
	public void setName(String name) {
		assert name != null;
		this.name = name;
	}

	/**
	 * A Mapping that transforms the object which is stored into this parameter to a value actually
	 * needed, e.g. if this parameter is a parameter representing the branch of some object, but the
	 * actual argument is the item itself, then the conversion is a mapping which maps the item to
	 * its branch.
	 */
	public Conversion getConversion() {
		return conversion;
	}

	/** @see #getConversion() */
	public void setConversion(Conversion conversion) {
		this.conversion = conversion;
	}

}

