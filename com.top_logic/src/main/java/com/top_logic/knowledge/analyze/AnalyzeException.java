/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

/**
 * Exception thrown when analyzing document or information fails.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class AnalyzeException extends RuntimeException {

    /**
     * Constructor for AnalyzeException.
     */
    public AnalyzeException() {
        super();
    }

    /**
     * Constructor for AnalyzeException.
     */
    public AnalyzeException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor for AnalyzeException.
     */
    public AnalyzeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor for AnalyzeException.
     */
    public AnalyzeException(Throwable arg0) {
        super(arg0);
    }

}

