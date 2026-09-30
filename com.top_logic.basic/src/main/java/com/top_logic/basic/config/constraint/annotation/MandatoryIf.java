/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.DefaultConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Declares that the annotated property must be set as soon as another property has a certain value.
 *
 * <p>
 * For a property that is required by one setting and meaningless under another: the bounds of a
 * value that is displayed as a slider are needed exactly while the display is the slider, and are
 * left out for a display that has no bounds to draw.
 * </p>
 *
 * <p>
 * The {@link #value() required value} is the value as it is written in a configuration - the
 * external name of an enumeration literal, {@code true} for a boolean - so that the annotation
 * names the value the same way the configuration it guards does. It is compared with the value the
 * {@link #other() other property} effectively has, its default included, so a property left at a
 * default that demands the annotated one is reported just as an explicitly written value is.
 * </p>
 *
 * @see com.top_logic.basic.config.constraint.impl.MandatoryIfUnset
 *
 * @implNote Comparing the configuration specification rather than the value itself is what makes
 *           the annotation work for every property type: an annotation can carry a literal only as
 *           a constant, and the specification of a value is the one text form every configuration
 *           value has, see {@link ConfigurationValueProvider#getSpecification(Object)}.
 */
@ConstraintAnnotation(MandatoryIf.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("mandatory-if")
public @interface MandatoryIf {

	/**
	 * Reference to the property whose value decides whether the annotated property must be set.
	 */
	Ref other();

	/**
	 * The value of the {@link #other() other property} that makes the annotated property mandatory,
	 * written as it would be in a configuration.
	 */
	String value();

	/**
	 * Whether a potential constraint violation should be treated as warning (instead of as error).
	 */
	boolean asWarning() default false;

	/**
	 * {@link ConstraintFactory} for {@link MandatoryIf}.
	 */
	public class Factory implements ConstraintFactory<MandatoryIf> {

		/**
		 * Singleton {@link MandatoryIf.Factory} instance.
		 */
		public static final MandatoryIf.Factory INSTANCE = new MandatoryIf.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(MandatoryIf annotation) {
			return Collections.<ConstraintSpec> singletonList(
				new DefaultConstraintSpec(
					new Constraint(annotation.value()),
					new Ref[] { annotation.other() },
					annotation.asWarning()));
		}

		/**
		 * The algorithm checking a {@link MandatoryIf} annotation.
		 */
		private static class Constraint extends GenericPropertyConstraint {

			private final String _requiredValue;

			Constraint(String requiredValue) {
				_requiredValue = requiredValue;
			}

			@Override
			public void check(PropertyModel<?>... models) {
				PropertyModel<?> self = models[0];
				if (self.isValueSet()) {
					return;
				}

				PropertyModel<?> other = models[1];
				if (!_requiredValue.equals(specification(other))) {
					return;
				}

				self.setProblemDescription(
					I18NConstants.MUST_BE_SET_IF_OTHER_HAS_VALUE__OTHER_VALUE.fill(
						other.getLabel(), _requiredValue));
			}

			/**
			 * The value of the given property as it would be written in a configuration, or
			 * {@code null} for a value that has no such form.
			 */
			private static String specification(PropertyModel<?> model) {
				Object value = model.getValue();
				if (value == null) {
					return null;
				}

				PropertyDescriptor property = model.getProperty();
				@SuppressWarnings("unchecked")
				ConfigurationValueProvider<Object> valueProvider = property.getValueProvider();
				if (valueProvider == null) {
					return value.toString();
				}
				return valueProvider.getSpecification(value);
			}

			/**
			 * Only the annotated property is at fault: the other one may hold its value with every
			 * right.
			 */
			@Override
			public boolean isChecked(int index) {
				return index == 0;
			}

			@Override
			public Class<?>[] signature() {
				return null;
			}

		}

	}

}
