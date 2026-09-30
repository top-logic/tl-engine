/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.form;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} to annotate the {@link Function} to create
 * {@link FormDefinitionTemplate}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("form-templates")
public interface TLFormTemplates extends TLAttributeAnnotation {

	/**
	 * Function creating a {@link Supplier} creating {@link FormDefinitionTemplate}s to offer to
	 * the user as base templates for the editing the given object.
	 */
	@InstanceFormat
	@Mandatory
	TemplateResolver getFunction();

	/**
	 * Resolves a {@link TLFormTemplates} annotation in the context of a given object.
	 */
	public static Supplier<? extends List<FormDefinitionTemplate>> resolve(TLFormTemplates annotation,
			TLObject obj) {
		if (annotation == null || obj == null) {
			return () -> Collections.emptyList();
		}

		return annotation.getFunction().getTemplates(obj);
	}
}