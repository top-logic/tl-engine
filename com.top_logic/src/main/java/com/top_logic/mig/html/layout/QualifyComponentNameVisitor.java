/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * {@link DescendingConfigurationItemVisitor} that qualifies each {@link ComponentName} in the tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QualifyComponentNameVisitor extends DescendingConfigurationItemVisitor {

	private final Function<ComponentName, ComponentName> _nameMapping;

	/**
	 * Creates a new {@link QualifyComponentNameVisitor}.
	 * 
	 * @param nameMapping
	 *        Mapping from local {@link ComponentName}s to qualified names.
	 */
	public QualifyComponentNameVisitor(Function<ComponentName, ComponentName> nameMapping) {
		_nameMapping = nameMapping;
	}

	/**
	 * Qualifies the {@link ComponentName}s in the given {@link ConfigurationItem}.
	 * 
	 * @param config
	 *        The configuration which needs qualified component names.
	 */
	public void qualify(ConfigurationItem config) {
		handleProperties(config);
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property, List<? extends ConfigurationItem> listValue) {
		if (listValue.isEmpty()) {
			return;
		}
		PropertyDescriptor keyProperty = property.getKeyProperty();
		if (keyProperty == null || !isComponentNameProperty(keyProperty)) {
			for (ConfigurationItem value : listValue) {
				qualify(value);
			}
		} else {
			/* The key of this property is a ComponentName. It is not good to change the key of an
			 * indexed property. Better create a copy of the list and store it to have a clear
			 * "key state" */
			ConfigurationItem[] copy = listValue.toArray(new ConfigurationItem[listValue.size()]);
			for (ConfigurationItem value : copy) {
				qualify(value);
			}
			config.update(property, Arrays.asList(copy));
		}

	}

	@Override
	protected void handleArrayProperty(ConfigurationItem config, PropertyDescriptor property, Object[] arrayValue) {
		int size = arrayValue.length;
		if (size == 0) {
			return;
		}

		PropertyDescriptor keyProperty = property.getKeyProperty();
		if (keyProperty == null || !isComponentNameProperty(keyProperty)) {
			for (int i = 0; i < size; i++) {
				ConfigurationItem configValue = (ConfigurationItem) arrayValue[i];
				qualify(configValue);
			}
		} else {
			/* The key of this property is a ComponentName. It is not good to change the key of an
			 * indexed property. Better create a copy of the list and store it to have a clear
			 * "key state" */
			Object copy = Array.newInstance(arrayValue.getClass().getComponentType(), size);
			for (int i = 0; i < size; i++) {
				ConfigurationItem configValue = (ConfigurationItem) arrayValue[i];
				qualify(configValue);
				Array.set(copy, i, configValue);
			}
			config.update(property, copy);
		}

	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		if (mapValue.isEmpty()){
			return;
		}
		PropertyDescriptor keyProperty = property.getKeyProperty();
		if (!isComponentNameProperty(keyProperty)) {
			for (ConfigurationItem value : mapValue.values()) {
				qualify(value);
			}
		} else {
			LinkedHashMap<Object, Object> copy = CollectionUtilShared.linkedMap();
			Mapping<Object, Object> keyMapping = property.getKeyMapping();
			for (ConfigurationItem value : mapValue.values()) {
				qualify(value);
				copy.put(keyMapping.apply(value), value);
			}
			config.update(property, copy);
		}
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		if (isComponentNameProperty(property)) {
			Object value = config.value(property);
			if (value == null) {
				return;
			}
			ComponentName name = (ComponentName) value;
			if (name.isLocalName()) {
				ComponentName qualifiedName = _nameMapping.apply(name);
				config.update(property, qualifiedName);
			}
		} else if (isComponentCollectionProperty(property)) {
			@SuppressWarnings("unchecked")
			Collection<ComponentName> value = (Collection<ComponentName>) config.value(property);
			if (value == null || value.isEmpty()) {
				return;
			}
			Collection<ComponentName> target;
			if (value instanceof Set) {
				target = CollectionUtilShared.newLinkedSet(value.size());
			} else if (value instanceof List) {
				target = new ArrayList<>(value.size());
			} else {
				Class<?> sourceType = value.getClass();
				try {
					@SuppressWarnings("unchecked")
					Collection<ComponentName> sourceClassCopy =
						(Collection<ComponentName>) ConfigUtil.newInstance(sourceType);
					target = sourceClassCopy;
				} catch (ConfigurationException ex) {
					// Don't know the concrete copy class;
					for (ComponentName name : value) {
						if (name.isLocalName()) {
							throw new IllegalArgumentException(
								"Don't know the copy class of type '" + sourceType + "' for property " + property);
						}
					}
					return;
				}
			}
			boolean changed = false;
			for (ComponentName name : value) {
				ComponentName qualifiedName;
				if (name.isLocalName()) {
					qualifiedName = _nameMapping.apply(name);
					changed = true;
				} else {
					qualifiedName = name;
				}
				target.add(qualifiedName);
			}
			if (changed) {
				config.update(property, target);
			}
		}
	}

	private static boolean isComponentNameProperty(PropertyDescriptor property) {
		return ComponentName.class.isAssignableFrom(property.getType());
	}

	private static boolean isComponentCollectionProperty(PropertyDescriptor property) {
		return Collection.class.isAssignableFrom(property.getType())
			&& ComponentName.class.isAssignableFrom(property.getElementType());
	}

}

