/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.Collection;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueCount;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} limiting the number of values of a multi-valued attribute, as
 * {@link TLValueCount} demands.
 */
public class ValueCountConstraintCheck implements ConstraintCheck {

	private final int _max;

	/**
	 * Creates a {@link ValueCountConstraintCheck}.
	 *
	 * @param max
	 *        The maximum number of values.
	 */
	public ValueCountConstraintCheck(int max) {
		_max = max;
	}

	/**
	 * The check for the given attribute, or {@code null} if it is not limited by a
	 * {@link TLValueCount} with a {@link TLValueCount#getMax() maximum}.
	 */
	public static ValueCountConstraintCheck forAttribute(TLStructuredTypePart attribute) {
		return forAnnotation(attribute.getAnnotation(TLValueCount.class));
	}

	/**
	 * The check for the given annotation, or {@code null} if the annotation is missing or states
	 * no {@link TLValueCount#getMax() maximum}.
	 */
	public static ValueCountConstraintCheck forAnnotation(TLValueCount annotation) {
		Integer max = maxCount(annotation);
		return max == null ? null : new ValueCountConstraintCheck(max.intValue());
	}

	/**
	 * The maximum number of values the given annotation allows, or {@code null} if it does not
	 * limit the number.
	 */
	public static Integer maxCount(TLValueCount annotation) {
		return annotation == null ? null : annotation.getMax();
	}

	/**
	 * The maximum number of values.
	 */
	public int getMax() {
		return _max;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks the number of values of the given attribute value.
	 *
	 * @param value
	 *        The value of a multi-valued attribute. A value that is not a {@link Collection} counts
	 *        as one value at most and is never rejected.
	 * @return The problem, or {@code null} if the value holds no more values than allowed.
	 */
	public ResKey checkValue(Object value) {
		if (value instanceof Collection<?> values) {
			int count = values.size();
			if (count > _max) {
				return I18NConstants.ERROR_TOO_MANY_VALUES__MAX_ACTUAL.fill(_max, count);
			}
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
