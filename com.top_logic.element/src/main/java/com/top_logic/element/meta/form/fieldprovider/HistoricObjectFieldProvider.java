/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s pointing to the past.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoricObjectFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isDisabled = true;
		Object theValue = editContext.getCorrectValues();
		List<?> theValueList;
		if (theValue == null) {
			theValueList = Collections.EMPTY_LIST;
		}
		else if (theValue instanceof List<?>) {
			theValueList = (List<?>) theValue;
		}
		else if (theValue instanceof Collection<?>) {
			theValueList = new ArrayList<Object>((Collection<?>) theValue);
		}
		else {
			theValueList = Collections.singletonList(theValue);
		}

		SelectField selectField = FormFactory.newSelectField(fieldName, theValueList, false, isDisabled);
		selectField.setConfigNameMapping(DefaultAttributeFormFactory.ATTRIBUTED_CONFIG_NAME_MAPPING);

		selectField.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
		selectField.setMandatory(editContext.isMandatory());

		return selectField;
	}

}
