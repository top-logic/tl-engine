/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * The ReadOnlyMapAccessor is a MapAccessor which doesn't allow modification
 * of the target object.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class ReadOnlyMapAccessor extends MapAccessor {

    public static final ReadOnlyMapAccessor INSTANCE = new ReadOnlyMapAccessor();
    
    @Override
	public final void setValue(Object aObject, String aProperty, Object aValue) {
        throw new UnsupportedOperationException();
    }

}
