/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.annotation;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.instance.importer.resolver.ValueResolver;

/**
 * Annotating associating a {@link ValueResolver} implementation with a primitive type.
 */
@TagName("value-resolver")
@AnnotationInheritance(Policy.REDEFINE)
@TargetType(value = TLTypeKind.CUSTOM)
public interface TLValueResolver extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * The {@link ValueResolver} implementation to use for exporting and importing values of the
	 * annotated type.
	 */
	@Mandatory
	@DefaultContainer
	PolymorphicConfiguration<? extends ValueResolver> getImpl();

}
