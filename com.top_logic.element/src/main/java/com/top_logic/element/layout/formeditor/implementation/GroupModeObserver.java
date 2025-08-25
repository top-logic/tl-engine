/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.ModeObserver;
import com.top_logic.layout.form.model.VisibilityModel;
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

	VisibilityModel _visibilityModel;

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
			TLStructuredTypePart attribute, VisibilityModel visibilityModel) {
		super(updateContainer, modeSelector, object, attribute);

		_visibilityModel = visibilityModel;
	}

	@Override
	public void valueChanged(FormVisibility mode) {
		boolean isVisible = !FormVisibility.HIDDEN.equals(mode);
		_visibilityModel.setVisible(isVisible);
	}
}
