/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLRange;

/**
 * {@link InstanceCheck} that tests the value of a {@link Number} attribute to be from a given
 * range.
 * 
 * @see TLRange
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NumberRangeCheck extends AbstractInstanceCheck {

	private final Double _minimum;

	private final Double _maximum;

	/**
	 * Creates a {@link NumberRangeCheck}.
	 */
	public NumberRangeCheck(TLStructuredTypePart attribute, Double minimum, Double maximum) {
		super(attribute);
		_minimum = minimum;
		_maximum = maximum;
	}

	@Override
	protected void internalCheck(Sink<ResKey> problems, TLObject object) {
		Object value = getValue(object);
		if (value instanceof Number) {
			double doubleValue = ((Number) value).doubleValue();
			if (_minimum != null) {
				if (doubleValue < _minimum.doubleValue()) {
					problems.add(
						withLocation(object, I18NConstants.ERROR_NUMBER_TO_SMALL__VALUE_MIN.fill(value, _minimum)));
				}
			}
			if (_maximum != null) {
				if (doubleValue > _maximum.doubleValue()) {
					problems.add(
						withLocation(object, I18NConstants.ERROR_NUMBER_TO_HIGH__VALUE_MAX.fill(value, _maximum)));
				}
			}
		}
	}

}
