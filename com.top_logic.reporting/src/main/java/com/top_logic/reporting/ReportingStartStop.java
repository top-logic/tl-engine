/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting;

import javax.servlet.ServletContext;

import com.top_logic.element.ElementStartStop;

/**
 * Setup Reporting relavnt classes here.
 * 
 * As Reporting has no real WebApp this class is not actually 
 * used as SatrtStopListener, but its functions clarify what
 * must be set up. 
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ReportingStartStop extends ElementStartStop {
    
    /** 
     * Setup Classes needed for the reporting project.
     */
    public static void setupReporting() throws Exception {
    }

    /** Overridden for Reporting Specific setup */
    @Override
	protected void initSubClassHook(ServletContext aContext) throws Exception {
        setupReporting(); // Need this before Layout is loaded ...
        super.initSubClassHook(aContext);
    }
    
}

