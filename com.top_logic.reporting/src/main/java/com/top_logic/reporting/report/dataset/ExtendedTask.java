/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.dataset;

import java.util.Date;

import org.jfree.data.gantt.Task;
import org.jfree.data.time.TimePeriod;

/**
 * The ExtendedTask extends the normal task through an additional 
 * Object.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExtendedTask extends Task {

    private Object additionalObject;

    /** Creates a {@link ExtendedTask}. */
    public ExtendedTask(String aDescription, Date aStart, Date aEnd, Object anObject) {
        super(aDescription, aStart, aEnd);
        
        this.additionalObject = anObject;
    }

    /** Creates a {@link ExtendedTask}. */
    public ExtendedTask(String aDescription, TimePeriod aDuration, Object anObject) {
        super(aDescription, aDuration);

        this.additionalObject = anObject;
    }

    /** Returns the additional object. */
    public Object getAdditionalObject() {
        return this.additionalObject;
    }
    
    /** Sets the additional object. */
    public void setAdditionalObject(Object aAdditionalObject) {
        this.additionalObject = aAdditionalObject;
    }
    
}

