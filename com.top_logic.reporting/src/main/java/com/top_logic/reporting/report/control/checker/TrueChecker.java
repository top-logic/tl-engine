/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.checker;

/**
 * The TrueChecker returns always <code>true</code>.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TrueChecker implements Checker {

	/** Please use this static instance. */
	public static final Checker INSTANCE = new TrueChecker();

    /** Returns always <code>true</code>.  */
    @Override
	public boolean check(Object anObject) {
        return true;
    }

}

