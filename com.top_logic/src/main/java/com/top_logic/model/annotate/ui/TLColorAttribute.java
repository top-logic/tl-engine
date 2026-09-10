/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.PartNamesOptionProvider;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * The attribute holding the color an instance of the annotated type is displayed with.
 *
 * <p>
 * The annotated attribute holds either a color value or an enumeration literal, whose own color
 * annotation then decides. An object of a type without this annotation has no color.
 * </p>
 *
 * <p>
 * Specializations of the annotated type inherit the annotation.
 * </p>
 *
 * @see TLColor
 *
 * @implNote {@link ValueColorProvider#colorOf(Object)} reads the annotated attribute and, for an
 *           attribute value that is a {@link TLClassifier}, continues with its {@link TLColor}.
 */
@TagName(TLColorAttribute.TAG_NAME)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@AnnotationInheritance(Policy.INHERIT)
@InApp
public interface TLColorAttribute extends TLTypeAnnotation {

	/** Name of the tag defining a {@link TLColorAttribute} annotation. */
	String TAG_NAME = "color-attribute";

	/** Configuration name of {@link #getName()}. */
	String NAME = "name";

	/**
	 * Name of the attribute of the annotated type that holds the color of an instance.
	 */
	@Name(NAME)
	@Mandatory
	@Label("Attribute")
	@Options(fun = PartNamesOptionProvider.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

}
