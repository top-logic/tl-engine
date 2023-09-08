/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.TypeKind;

import com.top_logic.model.TLType;

/**
 * Meta-annotation for {@link TLAnnotation} types that specify the {@link TypeKind}s this annotation
 * is compatible with.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetType {

	/**
	 * The {@link TLTypeKind}s the annotated annotation is compatible with.
	 * 
	 * <p>
	 * In case {@link TLTypeKind#REF} or {@link TLTypeKind#CUSTOM}, {@link #name()} is used to
	 * define optional names for {@link TLType} that are compatible with the annotated annotation.
	 * </p>
	 */
	TLTypeKind[] value();

	/**
	 * When {@link TLTypeKind#REF} or {@link TLTypeKind#CUSTOM} is specified for {@link #value()},
	 * this property returns optional type names that specify the super types, the annotated
	 * annotation is compatible with.
	 */
	String[] name() default {};

}
