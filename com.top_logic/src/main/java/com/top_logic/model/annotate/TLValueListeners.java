/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
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
public interface TLValueListeners extends TLAttributeAnnotation {

	/**
	 * Listeners to trigger when the {@link FormField} for the annotated attribute changes its
	 * value.
	 */
	@DefaultContainer
	@Options(fun = InAppImplementations.class)
	List<PolymorphicConfiguration<? extends ValueListener>> getListeners();

}

