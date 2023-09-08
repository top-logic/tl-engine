/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.checker;

/**
 * The Checker checks a arbitrary object and a positive or negative answer.
 * 
 * All classes which implement this interface must have an default 
 * constructor without parameters. The checker are created over reflection.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface Checker {

    /** This method checks the given object and returns <code>true</code> if
     *  and only if the object passed the check, <code>false</code> otherwise.
     * 
     * @param anObject An arbitrary object. <code>null</code> permitted.
     */
    public boolean check(Object anObject);
    
}

