/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

/**
 * A Filter that will accept only objects not equal to the given one.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class NotEqualsFilter extends EqualsFilter {

    /**
     * Default constructor.
     * 
     * @param    anObject    The object to be used for testing.
     */
    public NotEqualsFilter(Object anObject) {
        super(anObject);
    }

    /**
     * Accept only objects not equal to the internal one.
     */
    @Override
	public boolean accept(Object anObject) {
        return !this.object.equals(anObject);
    }
}
