/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;

/**
 * Adds a {@link ValidationListener} to a {@link LayoutComponent} whenever an external
 * {@link AbstractControlBase#ATTACHED_PROPERTY} event is observed.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DynamicValidationObserver implements AttachedPropertyListener {

	private final ValidationListener _validation;

	private final LayoutComponent _component;

	/**
	 * Creates a {@link DynamicValidationObserver}.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} to observe.
	 * @param validation
	 *        The {@link ValidationListener} to dynamically attach.
	 */
	public DynamicValidationObserver(LayoutComponent component, ValidationListener validation) {
		_component = component;
		_validation = validation;
	}

	@Override
	public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
		if (newValue) {
			_component.addValidationListener(_validation);
		} else {
			_component.removeValidationListener(_validation);
		}
	}
}