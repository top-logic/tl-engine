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

import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;

/**
 * Declares a static bound on a property.
 * 
 * <p>
 * If multiple independent {@link Bound}s should be declared on a single property, these annotations
 * can be nested within a {@link Bounds} annotation.
 * </p>
 * 
 * <p>
 * Dynamic bounds can be realized through a {@link ComparisonDependency}.
 * </p>
 * 
 * @see #comparison()
 * @see #value()
 * @see ComparisonDependency
 * @see Bounds
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ConstraintAnnotation(Bound.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Bounds.class)
@TagName("bound")
public @interface Bound {

	/**
	 * The type of comparison to perform.
	 */
	Comparision comparison();

	/**
	 * The value to compare the value of the annotated property with.
	 */
	double value();

	/**
	 * Whether a potential constraint violation should be treated as warning (instead of as error).
	 */
	boolean asWarning() default false;

	/**
	 * {@link ConstraintFactory} for {@link Bound}.
	 */
	public class Factory implements ConstraintFactory<Bound> {

		/**
		 * Singleton {@link Bound.Factory} instance.
		 */
		public static final Bound.Factory INSTANCE = new Bound.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(Bound annotation) {
			return Collections.<ConstraintSpec> singletonList(
				new Constraint(
					annotation.comparison(),
					annotation.value(),
					annotation.asWarning()));
		}

		private static class Constraint extends ValueConstraint<Number> implements ConstraintSpec {

			private static final Ref[] NONE = {};

			private final Comparision _comparision;

			private final double _compareValue;

			private final boolean _asWarning;

			public Constraint(Comparision comparision, double value, boolean asWarning) {
				super(Number.class);
				_comparision = comparision;
				_compareValue = value;
				_asWarning = asWarning;
			}

			@Override
			public ConstraintAlgorithm getAlgorithm() {
				return this;
			}

			@Override
			public Ref[] getRelated() {
				return NONE;
			}

			@Override
			public boolean asWarning() {
				return _asWarning;
			}

			@Override
			protected void checkValue(PropertyModel<Number> propertyModel) {
				Number actualValue = propertyModel.getValue();
				if (actualValue == null) {
					return;
				}

				double actualDouble = actualValue.doubleValue();
				if (_comparision.compare(actualDouble, _compareValue)) {
					return;
				}

				propertyModel.setProblemDescription(
					I18NConstants.BOUND_CONSTRAINT_VIOLATED__VALUE_BOUND_OPERATION.fill(
						actualValue, _compareValue, _comparision));
			}

		}

	}

}
