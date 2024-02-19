/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers.toggle;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link StateHandler} linking the button state to a component property.
 * 
 * <p>
 * The state is session-local and private to a single button.
 * </p>
 */
@InApp
@Label("Local state")
public class LocalStateHandler implements StateHandler {

	/**
	 * The property to store the button state to.
	 * 
	 * <p>
	 * Note: This is not a constant, because a component may have multiple toggle buttons with
	 * different state.
	 * </p>
	 */
	private final Property<Boolean> _property = TypedAnnotatable.property(Boolean.class, "buttonState", Boolean.FALSE);

	@Override
	public boolean getState(LayoutComponent component, Object model) {
		return component.get(_property).booleanValue();
	}

	@Override
	public void setState(LayoutComponent component, Object model, boolean state) {
		component.set(_property, Boolean.valueOf(state));
	}

}
