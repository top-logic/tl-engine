/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;

/**
 * {@link TLAttributeAnnotation} to annotate {@link ValueListener value listeners} to the field
 * created for an attribute.
 */
@InApp
@TagName("value-listeners")
// Listeners are collected explicitly by code over the whole inheritance hierarchy. 
@AnnotationInheritance(Policy.REDEFINE)
@DisplayOrder({
	TLValueListeners.LISTENERS_ATTRIBUTE,
	TLValueListeners.IGNORE_INHERITED_ATTRIBUTE,
})
public interface TLValueListeners extends TLAttributeAnnotation {

	/**
	 * Configuration name for the property {@link #getListeners()}.
	 */
	String LISTENERS_ATTRIBUTE = "listeners";

	/**
	 * Configuration name for the property {@link #isIgnoreInherited()}.
	 */
	String IGNORE_INHERITED_ATTRIBUTE = "ignore-inherited";

	/**
	 * Listeners to trigger when the {@link FormField} for the annotated attribute changes its
	 * value.
	 */
	@DefaultContainer
	@Options(fun = InAppImplementations.class)
	@Name(LISTENERS_ATTRIBUTE)
	List<PolymorphicConfiguration<? extends ValueListener>> getListeners();

	/**
	 * Whether listeners of inherited attributes should be ignored.
	 */
	@Name(IGNORE_INHERITED_ATTRIBUTE)
	boolean isIgnoreInherited();

}

