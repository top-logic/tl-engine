/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.Date;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Date}.
 * 
 * @see DateFieldProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTimeFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {

		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		DateTimeField field = new DateTimeField(fieldName, null, isDisabled);

		// Note: Setting the parser validates the field. Since it has no value yet, it it is better
		// to first set the parser before making the field mandatory, to avoid unnecessary error
		// handling.
		com.top_logic.layout.table.filter.DateFieldProvider.setShortcutParser(field.getDayField());

		field.setMandatory(isMandatory);
		return field;
	}

}
