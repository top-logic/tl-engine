/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;

/**
 * Checks, if a Field contains a given String. 
 * 
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ContainsStringConstraint extends AbstractConstraint {

    private String mustContain;
    
    public ContainsStringConstraint(String mustContain) {
        this.mustContain = mustContain;
    }
    
    /**
     * @see com.top_logic.layout.form.Constraint#check(java.lang.Object)
     */
    @Override
	public boolean check(Object someValue) throws CheckException {
        if (someValue instanceof String) {
            if (((String)someValue).indexOf(mustContain) > -1) {
                return true;
            } else {
                throw new CheckException("The Field does not contain the mandatory String '" + mustContain + "'");
            }
        }
        return false;
    }

}
