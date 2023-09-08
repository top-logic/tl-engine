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
import com.top_logic.model.annotate.TLSize;

/**
 * {@link InstanceCheck} that limits the size of a string attribute.
 * 
 * @see TLSize
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringSizeCheck extends AbstractInstanceCheck {

	private final long _lowerBound;

	private final long _upperBound;

	/**
	 * Creates a {@link StringSizeCheck}.
	 */
	public StringSizeCheck(TLStructuredTypePart attribute, long lowerBound, long upperBound) {
		super(attribute);
		_lowerBound = lowerBound;
		_upperBound = upperBound;
	}

	@Override
	protected void internalCheck(Sink<ResKey> problems, TLObject object) {
		Object value = nonNull(getValue(object));
		if (value instanceof CharSequence) {
			int length = ((CharSequence) value).length();
			if (length < _lowerBound) {
				problems.add(
					withLocation(object, I18NConstants.ERROR_STRING_TO_SMALL__LENGTH_MIN.fill(length, _lowerBound)));
			}
			if (length > _upperBound) {
				problems.add(
					withLocation(object, I18NConstants.ERROR_STRING_TO_LARGE__LENGTH_MAX.fill(length, _upperBound)));
			}
		}
	}

	private static Object nonNull(Object value) {
		return value == null ? "" : value;
	}

}
