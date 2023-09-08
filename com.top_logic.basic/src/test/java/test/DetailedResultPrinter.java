/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test;

import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.textui.ResultPrinter;

/**
 * Extend the textUI resultPrinter to output its result directly.
 * 
 * This way we can more easily locate problems like forgotten
 * System.out.println() statements.
 *
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class DetailedResultPrinter extends ResultPrinter {

    /** Create a new DetailedResultPrinter for the gicen PrintStream */
    public DetailedResultPrinter(PrintStream out) {
        super(out);  
    }

    /** Print the Stack-Trace for t to {@link #getWriter()} */
    @Override
	public void addError(Test test, Throwable t) {
        t.printStackTrace(getWriter());
    }

    /** Print the Stack-Trace for t to {@link #getWriter()} */
    @Override
	public void addFailure(Test test, AssertionFailedError t) {
        t.printStackTrace(getWriter());
    }

    /** Write "End : " + test */
    @Override
	public void endTest(Test test) {
        getWriter().println("End  : " + test);
    }

    /** Write "Start: " + test */
    @Override
	public void startTest(Test test) {
        getWriter().println("Start: " + test);
    }

}
