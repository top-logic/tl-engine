/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import java.util.Date;

import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link PrimitiveFilterProperty} for dates.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class DateProperty extends PrimitiveFilterProperty {

	private boolean _isMandatory;

	/**
	 * Creates a new {@link DateProperty}.
	 */
	public DateProperty(String name, Date initialValue, BoundComponent aComponent) {
		this(name, initialValue, aComponent, true);
	}

	/**
	 * Creates a new {@link DateProperty}.
	 */
	public DateProperty(String name, Date initialValue, BoundComponent aComponent, boolean mandatory) {
		super(name, initialValue, aComponent);
		_isMandatory = mandatory;
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		return FormFactory.newDateField(getName(), getInitialValue(), true, _isMandatory, false, null);
	}

}
