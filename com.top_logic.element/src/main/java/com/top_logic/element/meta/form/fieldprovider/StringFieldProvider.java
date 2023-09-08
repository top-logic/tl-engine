/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.format.IdentityFormat;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.constraints.ListConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLCollectionSeparator;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link String}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		boolean multiple = editContext.isMultiple();

		Generator options = editContext.getOptions();
		if (options != null) {
			Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON : null;

			OptionModel<?> optionModel = options.generate(editContext);
			SelectField result = FormFactory.newSelectField(fieldName, optionModel, multiple,
				isMandatory, isDisabled, mandatoryChecker);
			return result;
		}

		Constraint constraint;
		if (!editContext.isDerived()) {
			TLSize size = editContext.getAnnotation(TLSize.class);
			int minLength = AttributeOperations.getLowerBound(size);
			if (isMandatory) {
				minLength = Math.max(minLength, 1);
			}
			int maxLength = AttributeOperations.getUpperBound(size);
			constraint = createLengthConstraint(minLength, maxLength);
		} else {
			constraint = null;
		}

		if (multiple) {
			if (constraint != null) {
				constraint = new ListConstraint(constraint);
			}
			return FormFactory.newComplexField(fieldName,
				LongFieldProvider.collectionFormat(IdentityFormat.INSTANCE, editContext.isOrdered(),
					editContext.getAnnotation(TLCollectionSeparator.class)),
				false, isMandatory, isDisabled, constraint);
		} else {
			return FormFactory.newStringField(fieldName, StringField.EMPTY_STRING_VALUE, isMandatory, isDisabled,
				constraint);
		}
	}

	/**
	 * @param minLength
	 *        Minimal length the string should have.
	 * @param maxLength
	 *        Maximal length the string should have.
	 * @return String Constraint with the specified length.
	 */
	protected Constraint createLengthConstraint(int minLength, int maxLength) {
		return new StringLengthConstraint(minLength, maxLength);
	}

	@Override
	public boolean renderWholeLine(EditContext editContext) {
		return AttributeOperations.isMultiline(editContext.getAnnotation(MultiLine.class));
	}

}
