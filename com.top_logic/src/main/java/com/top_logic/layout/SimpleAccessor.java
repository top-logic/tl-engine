/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * An Accessor that simply returns the base object.
 * 
 * May be used in case some {@link ResourceProvider}, later extracts the desired parts of the object.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SimpleAccessor implements Accessor {

    public static final SimpleAccessor INSTANCE = new SimpleAccessor();

    /** 
     * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object object, String property) {
        return (object);
    }

    /** 
     * @see com.top_logic.layout.Accessor#setValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
	public void setValue(Object object, String property, Object value) {
    }
}
