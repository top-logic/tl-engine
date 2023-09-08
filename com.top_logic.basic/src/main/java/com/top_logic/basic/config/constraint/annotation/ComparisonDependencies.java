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
 * Annotation declaring multiple {@link ComparisonDependency} annotations on a single property.
 * 
 * @see ComparisonDependency
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ConstraintAnnotation(ComparisonDependencies.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("comparison-dependencies")
public @interface ComparisonDependencies {

	/**
	 * The multiple {@link ComparisonDependency} annotations to declare on a single property.
	 */
	ComparisonDependency[] value();

	/**
	 * {@link ConstraintFactory} for {@link ComparisonDependencies}.
	 */
	public class Factory extends MultiConstraintsFactory<ComparisonDependencies> {

		/**
		 * Singleton {@link ComparisonDependencies.Factory} instance.
		 */
		public static final ComparisonDependencies.Factory INSTANCE = new ComparisonDependencies.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		protected Annotation[] constraintAnnotations(ComparisonDependencies annotation) {
			return annotation.value();
		}

	}

}
