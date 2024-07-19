/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;

/**
 * {@link ValueListener} updating CSS classes on fields that display attributes with fallback
 * values.
 */
public class UpdateFallbackCssClass implements ValueListener {

	/**
	 * CSS class for a value that can have a fallback value.
	 */
	public static final String CSS_WITH_FALLBACK = "tl-with-fallback";

	/**
	 * CSS class for a fallback value being displayed.
	 */
	public static final String CSS_FALLBACK = "tl-fallback";

	/**
	 * CSS class for an explicit value that overrides a fallback value.
	 */
	public static final String CSS_EXPLICIT = "tl-explicit";

	/**
	 * Singleton {@link UpdateFallbackCssClass} instance.
	 */
	public static final UpdateFallbackCssClass INSTANCE = new UpdateFallbackCssClass();

	private UpdateFallbackCssClass() {
		// Singleton constructor.
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (Utils.isEmpty(newValue)) {
			field.removeCssClass(CSS_EXPLICIT);
			field.addCssClass(CSS_FALLBACK);
		} else {
			field.removeCssClass(CSS_FALLBACK);
			field.addCssClass(CSS_EXPLICIT);
		}
	}

}
