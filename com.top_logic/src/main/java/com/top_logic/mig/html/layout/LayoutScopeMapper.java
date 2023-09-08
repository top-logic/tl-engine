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

import com.top_logic.basic.col.ConstantMapMapping;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.editor.commands.LayoutExporter;

/**
 * Rewrites all {@link LayoutReference#getResource()} and {@link ComponentName} scope using the
 * configured mapping.
 * 
 * @see LayoutExporter
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutScopeMapper extends DescendingConfigurationItemVisitor {

	private Function<String, String> _scopeMapping;

	/**
	 * Creates a new {@link LayoutScopeMapper} with the given {@link Map} as scope mapping.
	 * 
	 * @see LayoutScopeMapper#LayoutScopeMapper(Function)
	 */
	public LayoutScopeMapper(Map<String, String> scopeMapping) {
		this(new ConstantMapMapping<>(scopeMapping, null));
	}

	/**
	 * Creates a new {@link LayoutScopeMapper} with the given mapping.
	 * 
	 * @param scopeMapping
	 *        Mapping function for the source scope to the target scope. A return value of
	 *        <code>null</code> means "no replacement".
	 */
	public LayoutScopeMapper(Function<String, String> scopeMapping) {
		_scopeMapping = scopeMapping;
	}

	/**
	 * Rewrites all {@link LayoutReference#getResource()} and {@link ComponentName} scopes in the
	 * given {@link ConfigurationItem}.
	 */
	public void map(ConfigurationItem configuration) {
		handleProperties(configuration);
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		if (isComponentNameProperty(property)) {
			Object value = config.value(property);
			if (value != null) {
				config.update(property, mapName((ComponentName) value));
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
			for (ComponentName name : value) {
				target.add(mapName(name));
			}

			config.update(property, target);
		}
	}

	private ComponentName mapName(ComponentName name) {
		return ComponentName.newName(mapScope(name.scope()), name.localName());
	}

	private boolean isComponentNameProperty(PropertyDescriptor property) {
		return ComponentName.class.isAssignableFrom(property.getType());
	}

	private boolean isComponentCollectionProperty(PropertyDescriptor property) {
		return Collection.class.isAssignableFrom(property.getType())
			&& ComponentName.class.isAssignableFrom(property.getElementType());
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
			List<? extends ConfigurationItem> listValue) {
		if (!listValue.isEmpty()) {
			PropertyDescriptor keyProperty = property.getKeyProperty();

			if (keyProperty == null || !isComponentNameProperty(keyProperty)) {
				handleConfigurations(listValue);
			} else {
				/* The key of this property is a ComponentName. It is not good to change the key of
				 * an indexed property. Better create a copy of the list and store it to have a
				 * clear "key state" */
				ConfigurationItem[] copy = listValue.toArray(new ConfigurationItem[listValue.size()]);
				for (ConfigurationItem value : copy) {
					handleConfiguration(value);
				}
				config.update(property, Arrays.asList(copy));
			}
		}
	}

	private void handleConfigurations(Collection<? extends ConfigurationItem> listValue) {
		for (ConfigurationItem entry : listValue) {
			handleConfiguration(entry);
		}
	}

	private void handleConfiguration(ConfigurationItem entry) {
		if (entry instanceof LayoutReference) {
			mapLayoutReferenceResource(entry);
		}

		handleProperties(entry);
	}

	private void mapLayoutReferenceResource(ConfigurationItem entry) {
		String resource = ((LayoutReference) entry).getResource();
		String mappedResource = mapScope(resource);

		if (mappedResource != null) {
			((LayoutReference) entry).setResource(mappedResource);
		}
	}

	/**
	 * Maps the given layout scope.
	 */
	public String mapScope(String scope) {
		String mappedScope = _scopeMapping.apply(scope);
		if (mappedScope == null) {
			// other scope.
			return scope;
		}
		return mappedScope;
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
				handleConfiguration(configValue);
			}
		} else {
			/* The key of this property is a ComponentName. It is not good to change the key of an
			 * indexed property. Better create a copy of the list and store it to have a clear
			 * "key state" */
			Object copy = Array.newInstance(arrayValue.getClass().getComponentType(), size);
			for (int i = 0; i < size; i++) {
				ConfigurationItem configValue = (ConfigurationItem) arrayValue[i];
				handleConfiguration(configValue);
				Array.set(copy, i, configValue);
			}
			config.update(property, copy);
		}
	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		if (!mapValue.isEmpty()) {
			PropertyDescriptor keyProperty = property.getKeyProperty();
			if (!isComponentNameProperty(keyProperty)) {
				handleConfigurations(mapValue.values());
			} else {
				LinkedHashMap<Object, Object> copy = CollectionUtilShared.linkedMap();
				Mapping<Object, Object> keyMapping = property.getKeyMapping();
				for (ConfigurationItem value : mapValue.values()) {
					handleConfiguration(value);
					copy.put(keyMapping.apply(value), value);
				}
				config.update(property, copy);
			}
		}
	}

	@Override
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationItem itemValue) {
		handleConfiguration(itemValue);
	}

}
