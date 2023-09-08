/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;
import com.top_logic.basic.util.Utils;

/**
 * {@link ConfigurationItem} which has a programmatic default.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class MutableConfigItem extends AbstractConfigItem {

	protected final Location location;

	public MutableConfigItem(AbstractConfigurationDescriptor type, Map<NamedConstant, Object> values,
			Location location) {
		super(type, values);

		this.location = location;
	}

	@Override
	public Location location() {
		return location;
	}
	
	@Override
	protected final Object internalValue(PropertyDescriptorImpl property) {
		Object result = directValue(property);
		if (result != null || directlySet(property)) {
			return result;
		}
		Object value = lookupDefault(property);

		return initValue(property, value);
	}

	private Object initValue(PropertyDescriptorImpl property, Object value) {
		switch (property.kind()) {
			case ARRAY: {
				return initArray(property, value);
			}
			case LIST: {
				Collection<?> items = (Collection<?>) value;
				return initCollection(property, items);
			}
			case MAP: {
				Map<?, ?> itemMap = (Map<?, ?>) value;
				ConfigPartUtilInternal.initContainers(itemMap.values(), getInterface());
				Object valueContainer = wrapWithPropertyMap(property, itemMap);
				updateDirectly(property, valueContainer);
				return valueContainer;
			}
			case ITEM: {
				ConfigPartUtilInternal.initContainer(value, getInterface());
				break;
			}
			default:
				break;
		}

		return value;
	}

	private Object initArray(PropertyDescriptorImpl property, Object arrayValue) {
		ConfigPartUtilInternal.initContainerArray((Object[]) arrayValue, getInterface());
		/* array is copied and the copied value is stored. Therefore it is safe to return the given
		 * arrayValue. */
		updateDirectly(property, arrayValue);
		return arrayValue;
	}

	private Object initCollection(PropertyDescriptorImpl property, Collection<?> items) {
		ConfigPartUtilInternal.initContainers(items, getInterface());
		Object valueContainer = wrapWithPropertyList(property, items);
		updateDirectly(property, valueContainer);
		return valueContainer;
	}

	protected Object lookupDefault(PropertyDescriptor property) {
		return property.getDefaultValue();
	}

	@Override
	protected final Object internalUpdate(PropertyDescriptorImpl property, Object newValue) {
		newValue = property.normalize(newValue);
		property.checkValue(newValue);
		
		if (property.canHaveSetter()) {
			markSet(property);
		}

		switch (property.kind()) {
			case ITEM: {
				Object oldValue = updateDirectly(property, newValue);
				if (Utils.equals(newValue, oldValue)) {
					return oldValue;
				}
				if (oldValue != null) {
					ConfigPartUtilInternal.clearContainer(oldValue);
				}
				if (newValue != null) {
					ConfigPartUtilInternal.initContainer(newValue, getInterface());
				}
				onChange(property).update(property, oldValue, newValue);
				return oldValue;
			}
			case ARRAY: {
				Object oldValue = updateDirectly(property, newValue);
				Object[] newArray = (Object[]) newValue;
				Object[] oldArray = (Object[]) oldValue;
				if (Arrays.equals(newArray, oldArray)) {
					return oldValue;
				}
				if (oldValue != null) {
					ConfigPartUtilInternal.clearContainerArray(oldArray);
				}
				if (newValue != null) {
					ConfigPartUtilInternal.initContainerArray(newArray, getInterface());
				}
				onChange(property).update(property, oldArray, newArray);
				return oldArray;
			}
			case LIST: {
				@SuppressWarnings("unchecked")
				PropertyList<Object> propertyList = (PropertyList<Object>) internalValue(property);
				Object before = propertyList.isEmpty() ? Collections.emptyList() : new ArrayList<>(propertyList);
				if (!before.equals(newValue)) {
					propertyList.clear();
					propertyList.addAll((Collection<?>) newValue);
				}
				return before;
			}
			case MAP: {
				@SuppressWarnings("unchecked")
				PropertyMap<Object, Object> propertyMap = (PropertyMap<Object, Object>) internalValue(property);
				Object before =
					propertyMap.isEmpty() ? 
						Collections.<Object, Object> emptyMap() : 
						new HashMap<>(propertyMap);
				if (!before.equals(newValue)) {
					propertyMap.clear();
					propertyMap.putAll((Map<?, ?>) newValue);
				}
				return before;
			}
			case DERIVED: {
				throw new UnsupportedOperationException("Derived property '" + property + "' cannot be set.");
			}
			default:
				Object oldValue = updateDirectly(property, newValue);
				if (Utils.equals(newValue, oldValue)) {
					return oldValue;
				}
				onChange(property).update(property, oldValue, newValue);
				return oldValue;

		}
	}

}
