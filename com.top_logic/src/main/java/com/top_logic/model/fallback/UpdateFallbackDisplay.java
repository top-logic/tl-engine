/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.ActivePropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;

/**
 * {@link ValueListener} updating CSS classes and placeholder/value properties on fields that
 * display attributes with fallback values.
 */
public class UpdateFallbackDisplay implements ValueListener, ActivePropertyListener {

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
	 * Singleton {@link UpdateFallbackDisplay} instance.
	 */
	public static final UpdateFallbackDisplay INSTANCE = new UpdateFallbackDisplay();

	private UpdateFallbackDisplay() {
		// Singleton constructor.
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		boolean displaysFallback = field.isActive() ? Utils.isEmpty(newValue) : Utils.isEmpty(field.getPlaceholder());
		if (displaysFallback) {
			field.removeCssClass(CSS_EXPLICIT);
			field.addCssClass(CSS_FALLBACK);
		} else {
			field.removeCssClass(CSS_FALLBACK);
			field.addCssClass(CSS_EXPLICIT);
		}
	}

	@Override
	public Bubble handleActiveChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (sender instanceof FormField field) {
			if (newValue.booleanValue()) {
				// Field became active again.
				Object placeholder = field.getPlaceholder();
				if (Utils.isEmpty(placeholder)) {
					// The field was displaying the fallback value.
					Object value = field.getValue();
					field.setPlaceholder(value);

					// Make field show the placeholder value again.
					field.initializeField(null);
				}
			} else {
				// Field was deactivated.
				Object value = field.getValue();
				if (Utils.isEmpty(value)) {
					// Field has no value, so the placeholder value is the value of the displayed
					// attribute.
					Object placeholder = field.getPlaceholder();
					// Mark field displaying default value.
					field.setPlaceholder(null);

					// Use the placeholder (fallback value) as real value in read-only mode (which
					// does not display a placeholder). Note: This change must be done last since it
					// triggers the CSS update.
					field.initializeField(placeholder);
				}
			}
		}
		return Bubble.BUBBLE;
	}

}
