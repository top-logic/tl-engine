/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.text.NumberFormat;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.util.Resources;

/**
 * The NumbersOnlyConstraint checks if the String represents a Number, e.g. 0.45 or 10.0, etc.
 * 
 * TODO KHA/TBE extract some common regexConstraint ?
 *      Perhaps you want to use a AbstractFloatConstraint ?
 *      Or just use a {@link ComplexField} with some {@link NumberFormat}
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class NumbersOnlyConstraint extends AbstractStringConstraint {

	/** The only instance. */
	public static final NumbersOnlyConstraint INSTANCE = new NumbersOnlyConstraint();
	
	private static final Pattern regex = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

	/** 
	 * Creates a {@link NumbersOnlyConstraint}.
	 * 
	 */
	public NumbersOnlyConstraint() {
		super();
	}
	/**
	 * @see com.top_logic.layout.form.constraints.AbstractStringConstraint#checkString(java.lang.String)
	 */
	@Override
	protected boolean checkString(String aValue) throws CheckException {
		if (!StringServices.isEmpty(aValue)) {
			if (!regex.matcher(aValue).matches()) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.ONLY_NUMBERS_ALLOWED));
			}
		}
		return true;
	}
}

