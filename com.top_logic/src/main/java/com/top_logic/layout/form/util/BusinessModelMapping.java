/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.table.CellObject;

/**
 * {@link Mapping} mapping view related objects to their business objects.
 * 
 * @see FormFieldValueMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BusinessModelMapping implements Mapping<Object, Object> {

	/**
	 * Singleton {@link BusinessModelMapping} instance.
	 */
	public static final BusinessModelMapping INSTANCE = new BusinessModelMapping();

	/**
	 * Creates a {@link BusinessModelMapping}.
	 */
	protected BusinessModelMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Object map(Object input) {
		if (input instanceof SelectField) {
			SelectField select = (SelectField) input;
			if (select.isMultiple()) {
				return SelectFieldUtils.getSelectionListSorted(select);
			} else {
				return select.getSingleSelection();
			}
		}
		else if (input instanceof FormField) {
            return ((FormField) input).getValue();
        }
        else if (input instanceof FormMember) {
			return ((FormMember) input).get(FormMember.BUSINESS_MODEL_PROPERTY);
        }
        else if (input instanceof CellObject) {
        	return ((CellObject) input).getValue();
        }
        else if (input instanceof SimpleConstantControl<?>) {
            return ((SimpleConstantControl<?>) input).getModel();
        }
        else {
            return input;
        }
	}

}
