/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.Arrays;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.kbbased.storage.mappings.JavaEnumMapping;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of Java {@link Enum} types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaEnumFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		TLPrimitive type = (TLPrimitive) editContext.getValueType();
		JavaEnumMapping<?, ?> mapping = (JavaEnumMapping<?, ?>) type.getStorageMapping();
		Iterable<?> options = Arrays.asList(mapping.getConfig().getEnum().getEnumConstants());
		return FormFactory.newSelectField(fieldName, options, editContext.isMultiple(), isMandatory, isDisabled,
			isMandatory ? GenericMandatoryConstraint.SINGLETON : null);
	}

}
