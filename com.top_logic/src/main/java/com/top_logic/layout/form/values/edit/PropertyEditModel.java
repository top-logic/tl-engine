/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;

/**
 * {@link PropertyModel} implementation in the UI edit context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class PropertyEditModel implements PropertyModel<Object> {

	private final PropertyDescriptor _property;

	private final ConfigurationItem _model;

	private final FormField _field;

	private ResKey _message;

	private ResKey _label;

	public PropertyEditModel(ConfigurationItem model, PropertyDescriptor property, FormField field) {
		_model = model;
		_property = property;
		_field = field;
	}

	@Override
	public ResKey getLabel() {
		if (_label == null) {
			_label = Labels.propertyLabelKey(_property);
		}
		return _label;
	}

	@Override
	public Object getValue() {
		if (_property.isMandatory() && !isValueSet()) {
			return null;
		}
		return _model.value(_property);
	}

	@Override
	public boolean isValueSet() {
		return _model.valueSet(_property);
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public void setProblemDescription(ResKey message) {
		_message = message;
	}

	public ResKey getProblemDescription() {
		return _message;
	}

	/**
	 * {@link FormField} to annotate constraints to.
	 * 
	 * @return May be <code>null</code>.
	 */
	public FormField getConstraintField() {
		return _field;
	}

}