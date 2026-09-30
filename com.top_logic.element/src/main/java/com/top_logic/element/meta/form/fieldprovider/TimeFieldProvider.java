/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.text.Format;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link TypeSpec#TIME_TYPE}.
 * 
 * @see DateTimeFieldProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {

		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		ComplexField field =
			FormFactory.newTimeField(fieldName, null, FormFactory.IGNORE_WHITE_SPACE, isMandatory, isDisabled, null);
		setConfiguredFormat(field, editContext);
		return field;
	}

	private void setConfiguredFormat(ComplexField field, EditContext editContext) {
		Format format;
		try {
			format = DisplayAnnotations.getConfiguredFormat(editContext);
		} catch (ConfigurationException ex) {
			Logger.error("Invalid attribute definition for '" + editContext + "'.", ex, TimeFieldProvider.class);
			format = null;
		}
		if (format != null) {
			field.setFormatAndParser(format);
		}
	}

}
