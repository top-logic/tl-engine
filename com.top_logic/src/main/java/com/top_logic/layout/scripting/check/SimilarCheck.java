/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ValueCheck} that checks a value against an expected value.
 * 
 * <p>
 * In contrast to {@link EqualsCheck}, a fuzzy matching is used that ignores some (implementation)
 * details.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimilarCheck extends ValueCheck<SimilarCheck.SimilarCheckConfig> {

	/**
	 * Configuration of {@link ValueCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface SimilarCheckConfig extends ValueCheck.ValueCheckConfig {
		/**
		 * Description of the expected valued.
		 */
		ModelName getExpectedValue();

		/** @see #getExpectedValue() */
		void setExpectedValue(ModelName value);
	}

	private static final double EPSILON = 0.000001D;

	/**
	 * Create a {@link SimilarCheck} from configuration.
	 * 
	 * @param context
	 *        The that allows instantiation of related implementations.
	 * @param config
	 *        The configuration of this instance.
	 */
	public SimilarCheck(InstantiationContext context, SimilarCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object actual) {
		Object expected = context.resolve(_config.getExpectedValue());

		if (actual instanceof Set<?>) {
			expected = toSet(expected);
		}
		else if (actual instanceof List<?>) {
			expected = toList(expected);
		}
		else if (expected instanceof Set<?>) {
			actual = toSet(actual);
		}
		else if (expected instanceof List<?>) {
			actual = toList(actual);
		}
		else if (isNumber(actual) && isNumber(expected)) {
			Number expectedNumber = (Number) expected;
			Number actualNumber = (Number) actual;
			if (isFloatingPoint(actual) || isFloatingPoint(expected)) {
				compareAsDouble(expectedNumber, actualNumber);
			} else {
				compareAsLong(expectedNumber, actualNumber);
			}
		}
		else {
			ApplicationAssertions.assertEquals(_config, "Values do not match.", expected, actual);
		}

		ApplicationAssertions.assertEquals(_config, "Values do not match.", expected, actual);
	}

	private boolean isNumber(Object actual) {
		return actual instanceof Number;
	}

	private boolean isFloatingPoint(Object actual) {
		return actual instanceof Float || actual instanceof Double;
	}

	private void compareAsDouble(Number expected, Number actual) {
		double expectedDouble = expected.doubleValue();
		double actualDouble = actual.doubleValue();
		if (Math.abs(actualDouble - expectedDouble) > EPSILON) {
			ApplicationAssertions.fail(_config, "Floating point values do not match, expected " + expectedDouble
				+ ", got " + actualDouble);
		}
	}

	private void compareAsLong(Number expected, Number actual) {
		long expectedLong = expected.longValue();
		long actualLong = actual.longValue();
		if (actualLong != expectedLong) {
			ApplicationAssertions.fail(_config, "Integer values do not match, expected " + expectedLong + ", got "
				+ actualLong);
		}
	}

	private List<?> toList(Object value) {
		if (value instanceof List<?>) {
			return (List<?>) value;
		} else if (value instanceof Collection<?>) {
			return new ArrayList<Object>((Collection<?>) value);
		} else {
			return CollectionUtil.singletonOrEmptyList(value);
		}
	}

	private Set<?> toSet(Object value) {
		if (value instanceof Set<?>) {
			return (Set<?>) value;
		} else if (value instanceof Collection<?>) {
			return new HashSet<Object>((Collection<?>) value);
		} else {
			return CollectionUtilShared.singletonOrEmptySet(value);
		}
	}

}

