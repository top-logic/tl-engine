/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ConstraintAnnotation} that creates regular expression constraints for {@link String}
 * -valued properties.
 * 
 * @see #value()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RegexpConstraints.class)
@ConstraintAnnotation(RegexpConstraint.Factory.class)
@TagName("reg-exp-constraint")
public @interface RegexpConstraint {

	/**
	 * The regular expression that must match the value.
	 * 
	 * @see Pattern
	 */
	String value();

	/**
	 * Whether a constraint failure should be treated as warning instead of error.
	 */
	boolean asWarning() default false;

	/**
	 * The {@link RegexpErrorKey} that delivers the error message when the pattern in
	 * {@link #value()} does not match.
	 */
	Class<? extends RegexpErrorKey> errorKey() default RegexpErrorKey.class;

	/**
	 * {@link ConstraintFactory} for {@link RegexpConstraint} annotations.
	 */
	public class Factory implements ConstraintFactory<RegexpConstraint>, RegexpErrorKey {

		/**
		 * Singleton {@link RegexpConstraint.Factory} instance.
		 */
		public static final RegexpConstraint.Factory INSTANCE = new RegexpConstraint.Factory();

		private static final Ref[] NO_REFS = {};

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(RegexpConstraint annotation) {
			Pattern pattern = Pattern.compile(annotation.value());
			RegexpErrorKey errorKey = resolveErrorKey(annotation);
			return Collections.<ConstraintSpec> singletonList(new DefaultConstraintSpec(
				new Algorithm(pattern, errorKey), NO_REFS, annotation.asWarning()));
		}

		private RegexpErrorKey resolveErrorKey(RegexpConstraint annotation) {
			RegexpErrorKey errorKey;
			if (annotation.errorKey() != RegexpErrorKey.class) {
				try {
					errorKey = ConfigUtil.getInstance(annotation.errorKey());
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			} else {
				errorKey = this;
			}
			return errorKey;
		}

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.ERROR_REGEXP_CONSTRAINT_VIOLATED__PATTERN_INPUT.fill(pattern, input);
		}

	}

	/**
	 * {@link ConstraintAlgorithm} implementing {@link RegexpConstraint} annotations.
	 */
	public class Algorithm extends ValueConstraint<String> {

		private final Pattern _pattern;

		private final RegexpErrorKey _errorKey;

		/**
		 * Creates a {@link Algorithm}.
		 * 
		 * @param pattern
		 *        The regular expression pattern to match against.
		 * @param errorKey
		 *        The message to display when pattern is not matched. The message is filled with the
		 *        string representation of given pattern and the checked value.
		 */
		public Algorithm(Pattern pattern, RegexpErrorKey errorKey) {
			super(String.class);
			_pattern = pattern;
			_errorKey = errorKey;
		}

		@Override
		protected void checkValue(PropertyModel<String> propertyModel) {
			String value = propertyModel.getValue();
			if (value == null) {
				// No value of nullable field.
				return;
			}
			Matcher m = _pattern.matcher(value);
			if (!m.matches()) {
				propertyModel.setProblemDescription(_errorKey.getKey(_pattern.pattern(), value));
			}
		}
	}

}
