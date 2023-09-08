/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing;

/** 
 * Basic exception for an indexing engine like Lucene or mindaccess.
 * 
 * If there are and other exceptions needed, please use this as parent class.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class IndexException extends RuntimeException {
    /**
     * Constructor for IndexException.
     */
    public IndexException() {
        super();
    }

    /**
	 * Constructor for {@link IndexException}.
	 */
    public IndexException(String arg0) {
        super(arg0);
    }

    /**
	 * Constructor for {@link IndexException}.
	 */
    public IndexException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
	 * Constructor for {@link IndexException}.
	 */
    public IndexException(Throwable arg0) {
        super(arg0);
    }
}
