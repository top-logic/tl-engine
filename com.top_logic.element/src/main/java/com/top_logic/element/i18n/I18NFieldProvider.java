/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link FieldProvider} for {@link I18NField}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class I18NFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isMultiLine = AttributeOperations.isMultiline(editContext.getAnnotation(MultiLine.class));

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

		return createField(editContext, fieldName, isMandatory, isDisabled, isMultiLine, constraint);
	}

	/**
	 * Create the field with the given settings.
	 * 
	 * @param editContext
	 *        The {@link EditContext} for which a {@link FormField}.
	 * @param fieldName
	 *        Never null.
	 * @param constraint
	 *        Null when there is no {@link Constraint}.
	 * 
	 * @return Is not allowed to be null.
	 */
	protected abstract FormMember createField(EditContext editContext, String fieldName, boolean isMandatory,
			boolean immutable, boolean isMultiLine, Constraint constraint);

	/**
	 * Creates an implementation-specific {@link Constraint} for the field created in
	 * {@link #createField(EditContext, String, boolean, boolean, boolean, Constraint)}.
	 * 
	 * @param minLength
	 *        Minimal length the string should have.
	 * @param maxLength
	 *        Maximal length the string should have.
	 * @return String Constraint with the specified length.
	 */
	protected abstract Constraint createLengthConstraint(int minLength, int maxLength);

}
