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
 * Annotation declaring multiple {@link Bound} annotations on a single property.
 * 
 * @see Bound
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ConstraintAnnotation(Bounds.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("bounds")
public @interface Bounds {

	/**
	 * The multiple {@link Bound} annotations to declare on a single property.
	 */
	Bound[] value();

	/**
	 * {@link ConstraintFactory} for {@link Bounds}.
	 */
	public class Factory extends MultiConstraintsFactory<Bounds> {

		/**
		 * Singleton {@link Bounds.Factory} instance.
		 */
		public static final Bounds.Factory INSTANCE = new Bounds.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		protected Annotation[] constraintAnnotations(Bounds annotation) {
			return annotation.value();
		}

	}

}
