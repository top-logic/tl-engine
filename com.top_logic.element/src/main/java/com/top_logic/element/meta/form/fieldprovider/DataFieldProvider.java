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
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.control.ImageUploadControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.BinaryDisplay;
import com.top_logic.model.annotate.ui.BinaryDisplay.BinaryPresentation;
import com.top_logic.model.annotate.ui.TLAcceptedTypes;
import com.top_logic.model.annotate.util.TLAnnotations;

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
		boolean isMultiple = editContext.isMultiple();

		BinaryPresentation presentation =
			TLAnnotations.getBinaryPresentation(editContext.getAnnotation(BinaryDisplay.class), isMultiple);

		DataField field = FormFactory.newDataField(fieldName);
		field.setMandatory(isMandatory);
		field.setImmutable(isDisabled);

		TLAcceptedTypes acceptedTypes = editContext.getAnnotation(TLAcceptedTypes.class);
		String accType = acceptedTypes != null ? acceptedTypes.getValue() : "";
		String imgType = "image/*";
		if (presentation == null && accType.equals(imgType)) {
			presentation = BinaryPresentation.IMAGE_UPLOAD;
		}

		switch (presentation) {
			case DATA_ITEM:
				field.setControlProvider((model, style) -> new DataItemControl((DataField) model));
				break;
			case IMAGE_UPLOAD:
				if (acceptedTypes != null && !accType.equals(imgType)) {
					acceptedTypes.setValue("image/*");
				}
				field.setControlProvider((model, style) -> new ImageUploadControl((DataField) model));
				break;
			default:
				throw BinaryPresentation.noSuchBinary(presentation);
		}

		setAcceptedFileTypes(field, acceptedTypes);

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
