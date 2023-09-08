/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.Accessor;

/**
 * {@link MarkerFieldProvider} that always returns a boolean field initialized with
 * {@link Boolean#TRUE}.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TrueMarkerFieldProvider extends MarkerFieldProvider {
    /**
     * @see com.top_logic.layout.table.component.MarkerFieldProvider#getInitialValue(java.lang.Object, com.top_logic.layout.Accessor, java.lang.String)
     */
    @Override
	protected Boolean getInitialValue(Object aMolde, Accessor aAnAccessor, String aProperty) {
        return Boolean.TRUE;
    }
}

