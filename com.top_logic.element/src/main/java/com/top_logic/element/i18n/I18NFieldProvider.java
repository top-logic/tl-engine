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
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link FieldProvider} for {@link I18NField}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class I18NFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isMultiLine = AttributeOperations.isMultiline(editContext.getAnnotation(MultiLine.class));

		return createField(editContext, fieldName, isMandatory, isDisabled, isMultiLine);
	}

	/**
	 * Create the field with the given settings.
	 * 
	 * @param editContext
	 *        The {@link EditContext} for which a {@link FormMember} is created.
	 * @param fieldName
	 *        The name of the created {@link FormMember}.
	 * @return Member to edit the I18N value.
	 */
	protected abstract FormMember createField(EditContext editContext, String fieldName, boolean isMandatory,
			boolean immutable, boolean isMultiLine);

}
