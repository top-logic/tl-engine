/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.DefaultConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;

/**
 * Annotation of a {@link ConstraintAlgorithm constraint checking algorithm} to a configuration
 * property.
 * 
 * <p>
 * If multiple independent constraints should be declared on a single property, these annotations
 * can be nested within a {@link Constraints} annotation.
 * </p>
 * 
 * <p>
 * If the annotated property overrides a property of a super-interface, all constraints declared on
 * the overridden property are also active, unless {@link OverrideConstraints} is also given.
 * </p>
 * 
 * <p>
 * If the {@link ConstraintAlgorithm} implementation needs parameterization, a custom constraint
 * annotation type can be declared. This custom constraint annotation takes the parameters for the
 * checking algorithm. To be picked up in the constraint creation process, the custom annotation
 * must itself be annotated with {@link ConstraintAnnotation}. Note: A custom constraint annotation
 * can only be declared once at a single property.
 * </p>
 * 
 * @see ConstraintAlgorithm
 * @see Constraints
 * @see ConstraintAnnotation
 * @see OverrideConstraints
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Constraints.class)
@ConstraintAnnotation(Constraint.Factory.class)
@TagName("constraint")
public @interface Constraint {

	/**
	 * Implementation of the constraint checking algorithm.
	 */
	Class<? extends ConstraintAlgorithm> value();

	/**
	 * References to other configuration properties that are related to the annotated configuration
	 * property.
	 */
	Ref[] args() default {};
	
	/**
	 * Whether a potential constraint violation should be treated as warning (instead of as error).
	 */
	boolean asWarning() default false;

	/**
	 * {@link ConstraintFactory} for the {@link Constraint} annotation.
	 */
	public class Factory implements ConstraintFactory<Constraint> {

		/**
		 * Singleton {@link Constraint.Factory} instance.
		 */
		public static final Constraint.Factory INSTANCE = new Constraint.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(final Constraint annotation) {
			return Collections.<ConstraintSpec> singletonList(
				new DefaultConstraintSpec(
					algorithm(annotation),
					annotation.args(),
					annotation.asWarning()));
		}

		private ConstraintAlgorithm algorithm(final Constraint annotation) {
			try {
				return ConfigUtil.getInstance(annotation.value());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError("Cannot instantiate annotated constraint implementation.", ex);
			}
		}
	}
}
