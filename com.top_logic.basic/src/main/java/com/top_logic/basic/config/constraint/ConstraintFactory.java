/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Algorithm creating {@link ConstraintSpec}s from {@link Annotation}s marked as
 * {@link ConstraintAnnotation}.
 * 
 * @see ConstraintAnnotation
 * @see MultiConstraintsFactory
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConstraintFactory<A extends Annotation> {

	/**
	 * Creates the operative constraint implementations from a {@link ConstraintAnnotation}
	 * annotated {@link Annotation}.
	 * 
	 * @param annotation
	 *        The concrete annotation instance to translate.
	 * @return {@link ConstraintSpec}s describing the constraints to create.
	 */
	List<ConstraintSpec> createConstraint(A annotation);

}
