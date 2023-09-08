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
import com.top_logic.basic.config.constraint.DefaultConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;

/**
 * Declares a dependency between two properties.
 * 
 * <p>
 * A static bound on a property can be realized through a {@link Bound} annotation.
 * </p>
 * 
 * @see #comparison()
 * @see #other()
 * @see Bound
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ConstraintAnnotation(ComparisonDependency.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ComparisonDependencies.class)
@TagName("comparison-dependency")
public @interface ComparisonDependency {

	/**
	 * The type of comparison to perform.
	 */
	Comparision comparison();

	/**
	 * Reference to the other property compare the value of the annotated property with.
	 */
	Ref other();

	/**
	 * Whether violations of this dependency should also be reported on the {@link #other()}
	 * property.
	 */
	boolean symmetric() default true;

	/**
	 * Whether a potential constraint violation should be treated as warning (instead of as error).
	 */
	boolean asWarning() default false;

	/**
	 * {@link ConstraintFactory} for {@link ComparisonDependency}.
	 */
	public class Factory implements ConstraintFactory<ComparisonDependency> {

		/**
		 * Singleton {@link ComparisonDependency.Factory} instance.
		 */
		public static final ComparisonDependency.Factory INSTANCE = new ComparisonDependency.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(final ComparisonDependency annotation) {
			return Collections.<ConstraintSpec> singletonList(
				new DefaultConstraintSpec(
					new Constraint(annotation.comparison(), annotation.symmetric()),
					new Ref[] { annotation.other() },
					annotation.asWarning()));
		}

		@SuppressWarnings("rawtypes")
		private static class Constraint extends ValueDependency<Comparable> {

			private final Comparision _comparision;

			private final boolean _symmetric;

			public Constraint(Comparision comparision, boolean symmetric) {
				super(Comparable.class);
				_comparision = comparision;
				_symmetric = symmetric;
			}

			@Override
			public boolean isChecked(int index) {
				return _symmetric || index == 0;
			}

			@Override
			protected void checkValue(PropertyModel<Comparable> baseModel, PropertyModel<Comparable> otherModel) {
				Comparable actualValue = baseModel.getValue();
				if (actualValue == null) {
					return;
				}

				Comparable otherValue = otherModel.getValue();
				if (otherValue == null) {
					return;
				}

				@SuppressWarnings("unchecked")
				boolean result = _comparision.compare(actualValue, otherValue);
				if (result) {
					return;
				}

				baseModel.setProblemDescription(
					I18NConstants.COMPARISON_DEPENDENCY_VIOLATED__VALUE_OTHER_BOUND_OPERATION.fill(
						actualValue, otherModel.getLabel(), otherValue, _comparision));

				if (_symmetric) {
					otherModel.setProblemDescription(
						I18NConstants.COMPARISON_DEPENDENCY_VIOLATED__VALUE_OTHER_BOUND_OPERATION.fill(
							otherValue, baseModel.getLabel(), actualValue, _comparision.swap()));
				}
			}

		}

	}

}
