/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint;

import java.lang.annotation.Annotation;

import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;

/**
 * All information describing a constraint.
 * 
 * <p>
 * A {@link ConstraintSpec} is created by a {@link ConstraintFactory} for an {@link Annotation}
 * annotated as {@link ConstraintAnnotation}.
 * </p>
 * 
 * @see ConstraintFactory
 * @see ConstraintAnnotation
 * @see DefaultConstraintSpec
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConstraintSpec {

	/**
	 * The actual {@link ConstraintAlgorithm} for checking the constraint.
	 * 
	 * <p>
	 * This algorithm may receive parameters form the the {@link ConstraintAnnotation} annotated
	 * {@link Annotation}.
	 * </p>
	 */
	ConstraintAlgorithm getAlgorithm();

	/**
	 * References to other properties that are accessed while evaluating the {@link #getAlgorithm()}.
	 */
	Ref[] getRelated();

	/**
	 * Whether this constraint should produce warnings instead of errors, if the the check fails.
	 */
	boolean asWarning();

}
