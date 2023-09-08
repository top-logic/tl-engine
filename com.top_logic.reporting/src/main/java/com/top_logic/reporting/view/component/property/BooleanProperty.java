/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link PrimitiveFilterProperty} for booleans.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class BooleanProperty extends PrimitiveFilterProperty {

	/**
	 * Creates a new {@link BooleanProperty}.
	 */
	public BooleanProperty(String name, Boolean initialValue, BoundComponent aComponent) {
		super(name, initialValue, aComponent);
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		return FormFactory.newBooleanField(getName(), getInitialValue(), false, false, null);
	}

}
