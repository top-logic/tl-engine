/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.io.File;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.knowledge.gui.layout.upload.FileNameConstraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.TLAcceptedTypes;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link File}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DataFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		DataField field = FormFactory.newDataField(fieldName);

		field.setMandatory(isMandatory);
		field.setImmutable(isDisabled);
		setAcceptedFileTypes(field, editContext.getAnnotation(TLAcceptedTypes.class));

		return field;
	}

	/**
	 * Sets accepted file types for file upload dialogs.
	 * 
	 * @param field
	 *        The {@link DataField}.
	 * @param annotation
	 *        The {@link TLAcceptedTypes} annotation.
	 */
	private void setAcceptedFileTypes(DataField field, TLAcceptedTypes annotation) {
		if (annotation != null) {
			String acceptedFileTypes = annotation.getValue();

			Set<String> acceptedTypesSet = StringServices.toSet(acceptedFileTypes, ',');
			AcceptedTypesChecker checker = new AcceptedTypesChecker(acceptedTypesSet);
			FileNameConstraint constraint = new FileNameConstraint(checker);
			field.setFileNameConstraint(constraint);
			field.setAcceptedTypes(checker.getAcceptedTypesAsString(true));
		}
	}
}
