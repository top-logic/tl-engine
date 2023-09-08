/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;

/**
 * The FieldProvider provides {@link FormMember}s based on an object and a property key.
 * 
 * It must not be assumed that successive calls to {@link #createField(Object, Accessor, String)}
 * result in the same object, but rather that each call will provide a new object.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface FieldProvider {
    
    /**
	 * Create a form member to be used as value in a table cell; the name of the
	 * created form member must be equal to the name returned by {@link #getFieldName(Object, Accessor, String)}
	 */
    public FormMember createField(Object aModel, Accessor anAccessor, String aProperty);
    
    /**
     * Return the name of the {@link FormMember} that would be created 
     * via {@link #createField(Object, Accessor, String)}
     */
	default String getFieldName(Object aModel, Accessor anAccessor, String aProperty) {
		return aProperty;
	}
}

