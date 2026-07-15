/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} adapter for numeric range validation from {@code @TLRange}.
 */
public class RangeConstraintCheck implements ConstraintCheck {

	private final Double _min;

	private final Double _max;

	/**
	 * Creates a {@link RangeConstraintCheck}.
	 */
	public RangeConstraintCheck(Double min, Double max) {
		_min = min;
		_max = max;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a numeric value against the bounds.
	 */
	public ResKey checkValue(Object value) {
		if (value == null) {
			return null;
		}
		double numericValue;
		if (value instanceof Number) {
			numericValue = ((Number) value).doubleValue();
		} else {
			return null;
		}
		if (_min != null && numericValue < _min) {
			return I18NConstants.ERROR_VALUE_BELOW_MINIMUM__MIN_ACTUAL.fill(_min, numericValue);
		}
		if (_max != null && numericValue > _max) {
			return I18NConstants.ERROR_VALUE_ABOVE_MAXIMUM__MAX_ACTUAL.fill(_max, numericValue);
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
