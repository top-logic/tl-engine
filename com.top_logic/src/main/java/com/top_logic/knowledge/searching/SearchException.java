/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

/** 
 * Basic exception within the search engine.
 * 
 * If there are and other exceptions needed, please use this as parent class.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchException extends RuntimeException {

    /**
     * Constructor for SearchException.
     */
    public SearchException() {
        super();
    }

    /**
     * Constructor for SearchException.
     */
    public SearchException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor for SearchException.
     */
    public SearchException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor for SearchException.
     */
    public SearchException(Throwable arg0) {
        super(arg0);
    }
}
