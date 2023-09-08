/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;

/**
 * Constraint for string based identifiers.
 * 
 * An identifier does not contain any blanks.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class IdentifierConstraint extends StringLengthConstraint {

    public IdentifierConstraint(int minLength) {
        super(minLength);
    }

    public IdentifierConstraint(int minLength, int maxLength) {
        super(minLength, maxLength);
    }

    /** 
     * @see com.top_logic.layout.form.constraints.StringLengthConstraint#checkString(java.lang.String)
     */
    @Override
	protected boolean checkString(String aValue) throws CheckException {
        boolean theResult = super.checkString(aValue);

        if (aValue == null) {
			// Note: An specialized constraint must not imply a mandatory constraint.
            return (theResult);
        }
        else {
            return (theResult && (aValue.indexOf(' ') < 0));
        }
    }
}
