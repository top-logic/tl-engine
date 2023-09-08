/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Check if a String contains only digits
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class DigitsOnlyConstraint extends AbstractStringConstraint {

	/** The only instance. */
	public static final DigitsOnlyConstraint INSTANCE = new DigitsOnlyConstraint();
	
	/** 
	 * Create a new DigitsOnlyConstraint
	 * 
	 */
	private DigitsOnlyConstraint() {
		super();
	}

	/** 
	 * @see com.top_logic.layout.form.constraints.AbstractStringConstraint#checkString(java.lang.String)
	 */
	@Override
	protected boolean checkString(String value) throws CheckException {
		if (!StringServices.isEmpty(value)) {
			int theLen = value.length();
			for (int i=0; i<theLen; i++) {
				if (!Character.isDigit(value.charAt(i))) {
					throw new CheckException(Resources.getInstance().getString(I18NConstants.NOT_ONLY_DIGITS));
				}
			}
		}
		
		return true;
	}

}
