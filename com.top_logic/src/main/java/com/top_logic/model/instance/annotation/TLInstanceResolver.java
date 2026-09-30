/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.annotation;

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
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * Annotation specifying an {@link InstanceResolver} implementation for a certain object type.
 * 
 * <p>
 * An {@link InstanceResolver} is used to create and resolve references to objects from object
 * exports. Typically, an object export does not export all objects that are transitively referenced
 * from the root object of the export. Instead, the export only transitively exports objects that
 * are stored in composition references. Objects in other references that are not also reachable by
 * only composition references are only exported as some IDs. An {@link InstanceResolver} is in
 * charge of creating those IDs and resolving them during a re-import.
 * </p>
 */
@TagName("instance-resolver")
@AnnotationInheritance(Policy.REDEFINE)
@TargetType(value = TLTypeKind.REF)
@InApp
public interface TLInstanceResolver extends TLTypeAnnotation {

	/**
	 * The resolver implementation to use for the annotated type.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends InstanceResolver> getImpl();

}
