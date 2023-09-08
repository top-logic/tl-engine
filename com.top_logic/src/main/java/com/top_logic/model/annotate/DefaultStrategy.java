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

import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Meta-annotation for {@link TLAnnotation} types that specifies whether the annotation has some
 * default if not specified directly at some attribute.
 * 
 * <p>
 * The strategy used by default (if the annotation is not annotated with {@link DefaultStrategy}) is
 * {@link Strategy#VALUE_TYPE}.
 * </p>
 * 
 * @see AnnotationInheritance
 * @see #value()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultStrategy {

	/**
	 * The default strategy to use for the annotated {@link TLAnnotation}.
	 */
	Strategy value();

	/**
	 * Default strategy for {@link TLAnnotation}s.
	 * 
	 * <p>
	 * The default, if no {@link DefaultStrategy} annotation is present ist {@link #VALUE_TYPE}.
	 * </p>
	 * 
	 * @see DefaultStrategy#value()
	 */
	enum Strategy {
		/**
		 * There is no default.
		 */
		NONE,

		/**
		 * The default is taken from the {@link TLStructuredTypePart#getType() value type} of the
		 * attribute.
		 */
		VALUE_TYPE,

		/**
		 * The default is taken from the {@link TLStructuredTypePart#getType() value type} of the
		 * attribute or the first of its primary generalizations defining the annotated annotation.
		 * 
		 * @see TLClass#getGeneralizations()
		 * @see TLModelUtil#getPrimaryGeneralization(TLClass)
		 */
		PRIMARY_GENERALIZATION;
	}

}
