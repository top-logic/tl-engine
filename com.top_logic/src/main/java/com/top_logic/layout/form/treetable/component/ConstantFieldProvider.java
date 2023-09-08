/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.util.FormFieldConstants;
import com.top_logic.layout.table.model.FieldProvider;

/**
 * The ConstantFieldProvider provides a view-only field to display an arbitrarily value.
 * Use the generated field with {@link SelectControl} for text-only display or with
 * {@link SelectionControl} for decorated resource display of the value.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ConstantFieldProvider implements FieldProvider, FormFieldConstants {

    public static final ConstantFieldProvider INSTANCE = new ConstantFieldProvider();

    @Override
	public FormMember createField(Object aModel, Accessor aAccessor, String aProperty) {
        return createField(aProperty, aAccessor.getValue(aModel, aProperty));
    }

    public FormMember createField(String aName, Object aValue) {
    	List option = CollectionUtil.intoListNotNull(aValue);
    	SelectField field = FormFactory.newSelectField(aName, option, !MULTI_SELECT, option, IMMUTABLE);
    	field.setFrozen(true);
    	return field;
    }

}
