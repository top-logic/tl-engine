/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.UnreachableAssertion;

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

	/**
	 * Appends a CSS class that indicates the maximal number of columns.
	 */
	public String appendColsCSSto(String baseCSS) {
		switch (this) {
			case DEFAULT:
				return baseCSS;
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
				return baseCSS + " " + getColsCSS(getValue().intValue());
		}
		throw new UnreachableAssertion("Uncovered case: " + this);
	}

	/**
	 * Determines the CSS class to display the maximal number of columns.
	 * 
	 * @param maxColCount
	 *        1, 2, 3, 4, or 5.
	 */
	public static String getColsCSS(int maxColCount) {
		switch (maxColCount) {
			case 1:
				return "cols1";
			case 2:
				return "cols2";
			case 3:
				return "cols3";
			case 4:
				return "cols4";
			case 5:
				return "cols5";
			default:
				throw new IllegalArgumentException("Unexpected number of columns. Only 1-5 are allowed.");
		}
	}
}