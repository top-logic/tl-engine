/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.element.layout.formeditor.implementation.FormEditorAttributeAnnotations;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.definition.VisibilityConfig;

/**
 * A definition of an input field.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("field")
@DisplayOrder({
	FieldDefinition.ATTRIBUTE,
	FieldDefinition.VISIBILITY,
	FieldDefinition.ANNOTATIONS
})
public interface FieldDefinition
		extends FormElement<FieldDefinitionTemplateProvider>, VisibilityConfig, AnnotatedConfig<TLAttributeAnnotation>,
		TypeRef, FullQualifiedName {

	/** Configuration name for the value of the {@link #getAttribute()}. */
	String ATTRIBUTE = "attribute";

	/**
	 * Setter for an attribute name.
	 * 
	 * @param name
	 *        Name of the attribute.
	 */
	void setAttribute(String name);

	/**
	 * Returns the name of the attribute.
	 * 
	 * @return The name of the attribute.
	 */
	@Name(ATTRIBUTE)
	@ReadOnly
	String getAttribute();

	@Override
	@Hidden
	String getTypeSpec();

	@Override
	@Options(fun = FormEditorAttributeAnnotations.class, mapping = ItemOptionMapping.class, args = @Ref(FULL_QUALIFIED_NAME))
	Collection<TLAttributeAnnotation> getAnnotations();

	@Override
	@Hidden
	String getFullQualifiedName();

}
