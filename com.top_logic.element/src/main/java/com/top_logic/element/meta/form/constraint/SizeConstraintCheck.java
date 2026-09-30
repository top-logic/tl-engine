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
 * {@link ConstraintCheck} adapter for string length validation from {@code @TLSize}.
 */
public class SizeConstraintCheck implements ConstraintCheck {

	private final int _minLength;

	private final int _maxLength;

	/**
	 * Creates a {@link SizeConstraintCheck}.
	 */
	public SizeConstraintCheck(int minLength, int maxLength) {
		_minLength = minLength;
		_maxLength = maxLength;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a value against the length bounds.
	 */
	public ResKey checkValue(Object value) {
		// Treat null as empty string, consistent with the storage-level StringSizeCheck.
		String str = value == null ? "" : value.toString();
		if (str.length() < _minLength) {
			return I18NConstants.ERROR_STRING_TOO_SHORT__MIN_ACTUAL.fill(_minLength, str.length());
		}
		if (str.length() > _maxLength) {
			return I18NConstants.ERROR_STRING_TOO_LONG__MAX_ACTUAL.fill(_maxLength, str.length());
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
