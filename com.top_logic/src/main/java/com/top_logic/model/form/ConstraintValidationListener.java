/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Listener for validation state changes on overlays.
 */
public interface ConstraintValidationListener {

	/**
	 * Called when the validation result for an attribute changes.
	 *
	 * @param overlay
	 *        The overlay whose validation changed.
	 * @param attribute
	 *        The attribute whose validation changed.
	 * @param result
	 *        The new validation result.
	 */
	void onValidationChanged(TLFormObjectBase overlay, TLStructuredTypePart attribute, ValidationResult result);
}
