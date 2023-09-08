/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.Set;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * {@link ConfigEquality} that compares the {@link ConfigurationItem}s by value, but ignores
 * {@link PropertyKind#DERIVED} properties.
 * <p>
 * In contrast to {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED} this {@link ConfigEquality}
 * compares all synthetic {@link ComponentName}s as equal.
 * </p>
 * 
 * @see ConfigEquality#INSTANCE_ALL_BUT_DERIVED
 * @see LayoutConstants#isSyntheticName(ComponentName)
 */
public class ComponentConfigEquality extends ConfigEquality {

	/**
	 * Singleton {@link ComponentConfigEquality} instance.
	 */
	public static final ComponentConfigEquality INSTANCE = new ComponentConfigEquality();

	/** 
	 * Creates a {@link ComponentConfigEquality}.
	 */
	protected ComponentConfigEquality() {
		super(CompareMode.VALUE, CompareMode.VALUE, CompareMode.IGNORE, CompareMode.IGNORE);
	}

	@Override
	protected boolean equalsPropertyValue(Object valueLeft, Object valueRight, PropertyDescriptor property,
			Set<Set<ConfigurationItem>> stack) {
		switch (property.kind()) {
			case PLAIN:
				if (valueLeft instanceof ComponentName) {
					if (!(valueRight instanceof ComponentName)) {
						return false;
					}
					if (LayoutConstants.isSyntheticName((ComponentName) valueLeft)) {
						return LayoutConstants.isSyntheticName((ComponentName) valueRight);
					} else {
						return valueLeft.equals(valueRight);
					}
				} else {
					return super.equalsPropertyValue(valueLeft, valueRight, property, stack);
				}
			default:
				return super.equalsPropertyValue(valueLeft, valueRight, property, stack);
		}
	}

	@Override
	protected int hashCodePropertyValue(Object propertyValue, PropertyDescriptor property,
			Set<ConfigurationItem> stack) {
		switch (property.kind()) {
			case PLAIN:
				if (propertyValue instanceof ComponentName) {
					if (LayoutConstants.isSyntheticName((ComponentName) propertyValue)) {
						return 156;
					} else {
						return propertyValue.hashCode();
					}
				} else {
					return super.hashCodePropertyValue(propertyValue, property, stack);
				}
			default:
				return super.hashCodePropertyValue(propertyValue, property, stack);
		}
	}
}