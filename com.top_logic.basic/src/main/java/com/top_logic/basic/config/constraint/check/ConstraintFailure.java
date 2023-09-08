/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.check;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey5;

/**
 * Description of typed configuration property constraint that fails to validate.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstraintFailure {

	private final boolean _warning;

	private final PropertyDescriptor _contextProperty;

	private final ResKey _constraintName;

	private final ConfigurationItem _item;

	/**
	 * Creates a {@link ConstraintFailure}.
	 * 
	 * @param checkedItem
	 *        See {@link #getItem()}.
	 * @param warning
	 *        See {@link #isWarning()}.
	 * @param contextProperty
	 *        See {@link #getContextProperty()}.
	 * @param constraintName
	 *        See {@link #getMessage()}.
	 */
	public ConstraintFailure(ConfigurationItem checkedItem, boolean warning, PropertyDescriptor contextProperty,
			ResKey constraintName) {
		_warning = warning;
		_contextProperty = contextProperty;
		_constraintName = constraintName;
		_item = checkedItem;
	}

	/**
	 * The property for which the message is generated.
	 */
	public PropertyDescriptor getContextProperty() {
		return _contextProperty;
	}

	/**
	 * The failure message.
	 */
	public ResKey getMessage() {
		String interfaceName = _contextProperty.getDescriptor().getConfigurationInterface().getName();
		String propertyName = _contextProperty.getPropertyName();
		Object propertyValue = getItem().value(getContextProperty());
		Location location = getItem().location();
		return getErrorOrWarningKey().fill(interfaceName, propertyName, propertyValue, getConstraintName(), location);
	}

	/** The i18n for the constraint that is violated. */
	public ResKey getConstraintName() {
		return _constraintName;
	}

	private ResKey5 getErrorOrWarningKey() {
		if (isWarning()) {
			return I18NConstants.CONSTRAINT_VIOLATION_WARNING__INTERFACE_PROPERTY_VALUE_CONSTRAINT_LOCATION;
		}
		return I18NConstants.CONSTRAINT_VIOLATION_ERROR__INTERFACE_PROPERTY_VALUE_CONSTRAINT_LOCATION;
	}

	/**
	 * Whether this failure is only a warning.
	 */
	public boolean isWarning() {
		return _warning;
	}

	/**
	 * The checked item.
	 */
	public ConfigurationItem getItem() {
		return _item;
	}

	@Override
	public String toString() {
		String interfaceName = _contextProperty.getDescriptor().getConfigurationInterface().getName();
		String propertyName = _contextProperty.getPropertyName();
		Object propertyValue = getItem().value(getContextProperty());
		Location location = getItem().location();
		return toString(getConstraintName(), interfaceName, propertyName, propertyValue, location);
	}

	private String toString(ResKey constraintName, String interfaceName, String propertyName,
			Object propertyValue, Location location) {
		return (_warning ? "Warning" : "Error") + " in Property '" + interfaceName + "." + propertyName
			+ "'. Value: '" + propertyValue + "'. Constraint: " + constraintName + ". Location: " + location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_contextProperty == null) ? 0 : _contextProperty.hashCode());
		result = prime * result + ((_constraintName == null) ? 0 : _constraintName.hashCode());
		result = prime * result + (_warning ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstraintFailure other = (ConstraintFailure) obj;
		if (_contextProperty == null) {
			if (other._contextProperty != null)
				return false;
		} else if (!_contextProperty.equals(other._contextProperty))
			return false;
		if (_constraintName == null) {
			if (other._constraintName != null)
				return false;
		} else if (!_constraintName.equals(other._constraintName))
			return false;
		if (_warning != other._warning)
			return false;
		return true;
	}

}
