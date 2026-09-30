/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.spi.ToolProvider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.TooltipProvider;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation that provides a {@link ToolProvider} for the given type.
 */
@TagName("tooltip")
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
// Generalizations are considered explicitly by code using this annotation.
@AnnotationInheritance(Policy.REDEFINE)
@InApp
public interface TLTooltip extends TLTypeAnnotation {

	/**
	 * The provider computing the tooltip for an instance of the annotated type.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends TooltipProvider> getTooltipProvider();

	/**
	 * @see #getTooltipProvider()
	 */
	void setTooltipProvider(PolymorphicConfiguration<? extends TooltipProvider> value);

}
