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

	private final String _cssExplicit;

	private final String _cssFallback;

	/**
	 * Creates an {@link UpdateFallbackDisplay}.
	 */
	public UpdateFallbackDisplay(String cssExplicit, String cssFallback) {
		_cssExplicit = cssExplicit;
		_cssFallback = cssFallback;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		boolean displaysFallback = field.isActive() ? Utils.isEmpty(newValue) : Utils.isEmpty(field.getPlaceholder());
		if (displaysFallback) {
			field.removeCssClass(_cssExplicit);
			field.addCssClass(_cssFallback);
		} else {
			field.removeCssClass(_cssFallback);
			field.addCssClass(_cssExplicit);
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
