/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.form;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.fieldprovider.I18NConstants;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link TLAttributeAnnotation} to annotate a {@link FormTypeResolver} to an attribute storing a
 * {@link FormDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("form-type")
public interface TLFormType extends TLAttributeAnnotation {

	/**
	 * Algorithm computing the type of objects to be displayed in a form.
	 */
	@DefaultContainer
	PolymorphicConfiguration<FormTypeResolver> getFunction();

	/**
	 * Resolves the form type of some context.
	 */
	public static TLStructuredType resolve(TLFormType annotation, TLObject context, ResKey descriptionKey) {
		if (context == null) {
			return null;
		}
		if (annotation == null) {
			throw new TopLogicException(
				I18NConstants.MISSING_TYPE_COMPUTATION__ATTRIBUTE.fill(descriptionKey));
		}
	
		return TypedConfigUtil.createInstance(annotation.getFunction()).getFormType(context);
	}

}