/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.annotation;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * Annotation specifying an {@link InstanceResolver} implementation for a certain object type.
 */
@AnnotationInheritance(Policy.REDEFINE)
@TargetType(value = TLTypeKind.REF)
public interface TLInstanceResolver extends TLTypeAnnotation {

	/**
	 * The resolver implementation to use for the annotated type.
	 */
	@DefaultContainer
	PolymorphicConfiguration<? extends InstanceResolver> getImpl();

}
