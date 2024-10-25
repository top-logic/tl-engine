/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.access.DeleteConstraint;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation that binds a {@link DeleteConstraint} to a type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete-constraints")
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@AnnotationInheritance(Policy.REDEFINE)
@InApp
public interface TLDeleteConstraints extends TLTypeAnnotation {

	/**
	 * Whether {@link TLDeleteConstraints} annotations of super-types should be overridden.
	 * 
	 * <p>
	 * If set to <code>true</code>, the annotated type specifies its on {@link DeleteConstraint}s
	 * independently of potential generalizations.
	 * </p>
	 */
	boolean getOverride();

	/**
	 * The {@link DeleteConstraint} implementation for the annotated type.
	 * 
	 * <p>
	 * Note: The configurable "default implementation" selected with the entry tag
	 * <code>delete-constraint</code> is contributed by the model-based search module declaring the
	 * tag name <code>delete-constraint</code>. Hard-coded implementations of constraints are
	 * possible by the somewhat special entry tag <code>custom-constraint</code>.
	 * </p>
	 */
	@DefaultContainer
	@EntryTag("custom-constraint")
	List<PolymorphicConfiguration<DeleteConstraint>> getValue();

}
