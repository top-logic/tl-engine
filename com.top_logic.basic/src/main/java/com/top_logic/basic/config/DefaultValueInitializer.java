/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;

/**
 * {@link Initializer} for the default values of {@link ConfigurationItem} properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class DefaultValueInitializer extends Initializer {

	private final PropertyDescriptorImpl _property;

	DefaultValueInitializer(PropertyDescriptorImpl property) {
		_property = property;
	}

	@Override
	public void init(AbstractConfigItem item) {
		if (item.valueSet(_property)) {
			return;
		}
		Object defaultValue = item.directValue(_property);
		if (defaultValue == null) {
			defaultValue = _property.getDefaultValue();
			/* Otherwise container already set during item initialisation, when the builder created
			 * the default value. */
			setContainer(item.getInterface(), defaultValue);
		}
		_property.checkValue(defaultValue);
		item.updateDirectly(_property, defaultValue);
	}

	private void setContainer(ConfigurationItem container, Object part) throws UnreachableAssertion {
		if (part == null) {
			return;
		}
		switch (_property.kind()) {
			case ITEM: {
				ConfigPartUtilInternal.initContainer(part, container);
				return;
			}
			case ARRAY: {
				List<?> defaultList = PropertyDescriptorImpl.arrayAsList(part);
				ConfigPartUtilInternal.initContainers(defaultList, container);
				return;
			}
			case LIST: {
				List<?> defaultList = (List<?>) part;
				ConfigPartUtilInternal.initContainers(defaultList, container);
				return;
			}
			case MAP: {
				Map<?, ?> defaultMap = (Map<?, ?>) part;
				ConfigPartUtilInternal.initContainers(defaultMap.values(), container);
				return;
			}
			case PLAIN:
			case COMPLEX: {
				// Nothing to do in these cases: There is no ConfigItem.
				return;
			}
			case REF: {
				// Nothing to do in these cases: The ConfigItem should not know about this
				// container, as it belongs somewhere else and has its container there.
				return;
			}
			case DERIVED: {
				throw new UnreachableAssertion("Setting a default value for a derived property is not allowed.");
			}
			default: {
				throw new UnreachableAssertion("Unhandled property kind: " + _property.kind());
			}
		}
	}

}
