/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.model.AbstractFieldProvider;

/**
 * {@link AbstractFieldProvider} creating {@link BooleanField}s for markers.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public abstract class MarkerFieldProvider extends AbstractFieldProvider {

    /**
     * @see com.top_logic.layout.table.model.FieldProvider#createField(java.lang.Object, com.top_logic.layout.Accessor, java.lang.String)
     */
    @Override
	public FormMember createField(Object aMolde, Accessor aAnAccessor, String aProperty) {
        return FormFactory.newBooleanField(aProperty, this.getInitialValue(aMolde, aAnAccessor, aProperty), false);
    }

    protected abstract Boolean getInitialValue(Object aMolde, Accessor aAnAccessor, String aProperty);

}

