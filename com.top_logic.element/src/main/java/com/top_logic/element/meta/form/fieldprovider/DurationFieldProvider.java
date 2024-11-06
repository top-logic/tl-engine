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
import com.top_logic.layout.form.format.DurationFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link TypeSpec#LONG_TYPE} with a
 * time duration format.
 */
public class DurationFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		
		Format format = lookupFormat(editContext);

		ComplexField field =
			FormFactory.newComplexField(fieldName, format, FormFactory.IGNORE_WHITE_SPACE, isMandatory,
				isDisabled, null);

		return field;
	}

	private Format lookupFormat(EditContext editContext) {
		try {
			Format format = DisplayAnnotations.getConfiguredFormat(editContext);
			if (format != null) {
				return format;
			}
		} catch (ConfigurationException ex) {
			Logger.error("Invalid attribute definition for '" + editContext + "'.", ex, DateFieldProvider.class);
		}
		return DurationFormat.INSTANCE;
	}

}
