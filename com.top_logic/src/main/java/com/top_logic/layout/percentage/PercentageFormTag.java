/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.ComplexField;

/**
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class PercentageFormTag extends PercentageTag {

	private String name;
	
	@Override
	protected double getPercentageValue() {
		FormComponent formComponent = (FormComponent) getComponent();
		ComplexField field = (ComplexField) formComponent.getFormContext().getField(getName());
		
		Object value = field.getValue();
		if (value != null) {
			return ((Number) value).doubleValue();
		}
		
		return 0;
	}

	@Override
	public boolean isVisible() {
		FormComponent formComponent = (FormComponent) getComponent();
		ComplexField field = (ComplexField) formComponent.getFormContext().getField(getName());
		
		return field.isVisible();
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		
		this.name = null;
	}
	
	/**
	 * Returns the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param fieldName The name to set.
	 */
	public void setName(String fieldName) {
		this.name = fieldName;
	}

}
