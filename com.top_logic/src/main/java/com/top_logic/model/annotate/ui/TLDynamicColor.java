/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * The algorithm computing the color an instance of the annotated type is displayed with.
 *
 * <p>
 * A color tells the state of an object apart at a glance: the color computed for an object is what
 * the object is displayed as a pill in, wherever it appears as a value. An object of a type without
 * this annotation, and an object the algorithm answers no color for, is displayed in the default
 * way.
 * </p>
 *
 * <p>
 * Specializations of the annotated type inherit the annotation.
 * </p>
 *
 * @see TLColor
 *
 * @implNote {@link ValueColorProvider#colorOf(Object)} of the configured provider answers the color
 *           of an instance, see {@link AnnotationValueColorProvider}.
 */
@TagName(TLDynamicColor.TAG_NAME)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@AnnotationInheritance(Policy.INHERIT)
@InApp
public interface TLDynamicColor extends TLTypeAnnotation {

	/** Name of the tag defining a {@link TLDynamicColor} annotation. */
	String TAG_NAME = "dynamic-color";

	/**
	 * The provider computing the color of an instance of the annotated type.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends ValueColorProvider> getColorProvider();

	/**
	 * @see #getColorProvider()
	 */
	void setColorProvider(PolymorphicConfiguration<? extends ValueColorProvider> value);

}
