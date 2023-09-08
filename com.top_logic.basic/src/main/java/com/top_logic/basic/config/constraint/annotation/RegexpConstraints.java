/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.MultiConstraintsFactory;

/**
 * Multi-constraint annotation for the {@link RegexpConstraint} annotation.
 * 
 * @see RegexpConstraint
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ConstraintAnnotation(RegexpConstraints.Factory.class)
@TagName("reg-exp-constraints")
public @interface RegexpConstraints {

	/**
	 * The multiple {@link RegexpConstraint}s on a single property.
	 */
	RegexpConstraint[] value();

	/**
	 * {@link MultiConstraintsFactory} for {@link RegexpConstraints}.
	 */
	public class Factory extends MultiConstraintsFactory<RegexpConstraints> {

		/**
		 * Singleton {@link RegexpConstraints.Factory} instance.
		 */
		public static final Factory INSTANCE = new Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		protected Annotation[] constraintAnnotations(RegexpConstraints annotation) {
			return annotation.value();
		}

	}

}

