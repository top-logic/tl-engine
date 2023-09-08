/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for {@link TLAnnotation}s that defines whether they are inherited and can be
 * redefined on sub types.
 * 
 * @see DefaultStrategy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationInheritance {

	/**
	 * The policy for inheriting an annotated {@link TLAnnotation}.
	 */
	enum Policy {
		/**
		 * The annotated {@link TLAnnotation} is inherited from overridden parts but may be
		 * redefined on overriding parts.
		 */
		INHERIT,

		/**
		 * The annotated {@link TLAnnotation} must be redefined on each overridden part.
		 */
		REDEFINE,

		/**
		 * The annotated {@link TLAnnotation} is inherited but must not be redefined on an
		 * overridden part.
		 */
		FINAL;

		/**
		 * Utility to look up the {@link AnnotationInheritance annotated} {@link Policy} on a given
		 * {@link TLAnnotation} class.
		 *
		 * @param annotationInterface
		 *        The {@link TLAnnotation} class to inspect.
		 * @return The annotated {@link Policy}, or {@link #INHERIT}, if nothing is annotated.
		 */
		public static Policy getInheritancePolicy(Class<?> annotationInterface) {
			AnnotationInheritance metaAnnotation = annotationInterface.getAnnotation(AnnotationInheritance.class);
			if (metaAnnotation == null) {
				return INHERIT;
			}

			return metaAnnotation.value();
		}

	}

	/**
	 * The annotation inheritance {@link Policy} for the annotated {@link TLAnnotation}.
	 */
	public Policy value();

}
