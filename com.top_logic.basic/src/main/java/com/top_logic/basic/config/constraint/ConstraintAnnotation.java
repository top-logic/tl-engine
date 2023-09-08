/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;

/**
 * Meta-annotation that makes an annotation interface defining a typed configuration property
 * constraint.
 * 
 * <p>
 * Note: A {@link ConstraintAnnotation} must also be annotated as {@link Retention}
 * {@link RetentionPolicy#RUNTIME} and {@link Target} {@link ElementType#METHOD}.
 * </p>
 * 
 * @see #value()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ConstraintAnnotation {

	/**
	 * The factory class that transforms the an annotation instance into a
	 * {@link ConstraintAlgorithm} implementation.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends ConstraintFactory> value();

}
