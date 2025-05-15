/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.ModeObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;

/**
 * Handles the visibility changes of a non-attribute element.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupModeObserver extends ModeObserver {

	boolean _isVisible = true;

	/**
	 * Creates a {@link GroupModeObserver}.
	 * 
	 * @param updateContainer
	 *        Container to get the values of.
	 * @param modeSelector
	 *        Calculates the actual {@link FormVisibility}.
	 * @param object
	 *        Model to observe.
	 * @param attribute
	 *        The attribute to observe. Maybe <code>null</code>.
	 */
	public GroupModeObserver(AttributeUpdateContainer updateContainer, ModeSelector modeSelector, TLObject object,
			TLStructuredTypePart attribute) {
		super(updateContainer, modeSelector, object, attribute);
	}

	@Override
	public void valueChanged(FormVisibility mode) {
		boolean visibilityChanged = false;
		switch (mode) {
			case HIDDEN:
				if (isVisible())
					visibilityChanged = true;
				_isVisible = false;
				break;
			default:
				if (!isVisible())
					visibilityChanged = true;
				_isVisible = true;
				break;
		}

		if (visibilityChanged) {
			// trigger re-draw
		}
	}

	/**
	 * Whether the element is calculated to be visible or not.
	 */
	public boolean isVisible() {
		return _isVisible;
	}

}
