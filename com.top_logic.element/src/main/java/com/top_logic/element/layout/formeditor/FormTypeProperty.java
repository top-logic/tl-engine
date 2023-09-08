/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * Annotation for a {@link PropertyDescriptor} of type {@link FormDefinition} that contains the name
 * of another {@link PropertyDescriptor} (in the same configuration) that holds the
 * {@link TLStructuredType} for which the form is edited.
 * 
 * <p>
 * Editing a {@link FormDefinition} requires a {@link TLStructuredType} whose attributes are shown
 * in the form editor. To allow more than one {@link FormDefinition} property in a single
 * configuration, each {@link FormDefinition} property must be linked to its individual type
 * property. Normally, the type property (providing the {@link TLStructuredType}) is a
 * {@link Derived} property that provides the type from its editing context.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("form-type-property")
public @interface FormTypeProperty {

	/**
	 * Name of the {@link PropertyDescriptor} providing the {@link TLStructuredType} for which a
	 * {@link FormDefinition} is edited in the annotated property.
	 */
	String value();

}

