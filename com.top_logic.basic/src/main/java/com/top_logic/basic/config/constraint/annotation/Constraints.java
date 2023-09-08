/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.MultiConstraintsFactory;

/**
 * Multi-constraint annotation for the generic {@link Constraint} annotation.
 * 
 * @see Constraint
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ConstraintAnnotation(Constraints.Factory.class)
@TagName("constraints")
public @interface Constraints {

	/**
	 * The multiple {@link Constraint}s on a single property.
	 */
	Constraint[] value();

	/**
	 * {@link ConstraintFactory} for {@link Constraints}.
	 */
	public class Factory extends MultiConstraintsFactory<Constraints> {

		/**
		 * Singleton {@link Constraints.Factory} instance.
		 */
		public static final Constraints.Factory INSTANCE = new Constraints.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		protected Annotation[] constraintAnnotations(Constraints annotation) {
			return annotation.value();
		}

	}

}
