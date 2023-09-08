/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Base class for {@link ConstraintFactory} implementations of multi-constraint annotations.
 * 
 * @see com.top_logic.basic.config.constraint.annotation.Constraints.Factory
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MultiConstraintsFactory<A extends Annotation> implements ConstraintFactory<A> {

	@Override
	public List<ConstraintSpec> createConstraint(A annotation) {
		return createConstraints(constraintAnnotations(annotation));
	}

	/**
	 * Decomposes the given multi-constraint annotation.
	 * 
	 * @param annotation
	 *        The multi-constraint annotation to process.
	 * @return The inner constraint annotations.
	 */
	protected abstract Annotation[] constraintAnnotations(A annotation);

	private List<ConstraintSpec> createConstraints(Annotation[] annotations) {
		ArrayList<ConstraintSpec> specs = new ArrayList<>();
		for (int n = 0, cnt = annotations.length; n < cnt; n++) {
			Annotation annotation = annotations[n];
			ConstraintAnnotation metaAnnotation =
				annotation.annotationType().getAnnotation(ConstraintAnnotation.class);
			if (metaAnnotation == null) {
				throw new ConfigurationError("Not a constraint annotation: " + annotation);
			}
			specs.addAll(factory(metaAnnotation.value(), annotation).createConstraint(annotation));
		}
		return specs;
	}

	@SuppressWarnings("rawtypes")
	private ConstraintFactory<Annotation> factory(Class<? extends ConstraintFactory> factoryClass,
			Annotation annotation) {
		try {
			@SuppressWarnings("unchecked")
			ConstraintFactory<Annotation> factory = ConfigUtil.getInstance(factoryClass);
			return factory;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Cannot get constraint factory for annotation '" + annotation + "'.", ex);
		}
	}

}