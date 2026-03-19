/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.constraint;

import java.util.Collection;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} adapter for mandatory attribute validation.
 */
public class MandatoryConstraintCheck implements ConstraintCheck {

	/** Singleton instance. */
	public static final MandatoryConstraintCheck INSTANCE = new MandatoryConstraintCheck();

	private MandatoryConstraintCheck() {
		// Singleton
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a value for emptiness.
	 */
	public ResKey checkValue(Object value) {
		if (value == null) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		if (value instanceof String && ((String) value).trim().isEmpty()) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
