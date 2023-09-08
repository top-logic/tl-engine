/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

/**
 * The maximal number of columns for a form (element).
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public enum Columns {
	/**
	 * Maximal number of columns: default (for forms: in properties, for form elements: in form).
	 */
	DEFAULT(null),
	/** Maximal number of columns: 1. */
	ONE(1),
	/** Maximal number of columns: 2. */
	TWO(2),
	/** Maximal number of columns: 3. */
	THREE(3),
	/** Maximal number of columns: 4. */
	FOUR(4),
	/** Maximal number of columns: 5. */
	FIVE(5);
	
	private final Integer _number;

	private Columns(Integer number) {
		_number = number;
	}

	/**
	 * Returns the Integer value of the column.
	 * 
	 * @return Value as Integer.
	 */
	public Integer getValue() {
		return _number;
	}
}