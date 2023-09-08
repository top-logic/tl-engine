/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.config.misc.PropertyValueImpl;
import com.top_logic.util.error.TopLogicException;

/**
 * Default {@link ValueModel} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultValueModel implements ValueModel {
	private final PropertyDescriptor _property;

	private final ConfigurationItem _container;

	/**
	 * Creates a {@link ValueModel} from a resolved dot-separated path starting at the given base
	 * object.
	 *
	 * @param base
	 *        The base object to start the path resolution from.
	 * @param path
	 *        A dot-separated list of property names.
	 * @return A {@link ValueModel} of the storage location pointed to by the given path.
	 */
	public static ValueModel resolvePath(ConfigurationItem base, String path) {
		String[] names = path.split("\\.");
		ConfigurationItem current = base;
		for (int n = 0, cnt = names.length - 1; n < cnt; n++) {
			PropertyDescriptor property = current.descriptor().getProperty(names[n]);
			current = (ConfigurationItem) current.value(property);
		}

		return new DefaultValueModel(current, current.descriptor().getProperty(names[names.length - 1]));
	}

	/**
	 * Creates a {@link DefaultValueModel}.
	 *
	 * @param container
	 *        The object that declares the given property.
	 * @param property
	 *        The property to access.
	 */
	public DefaultValueModel(ConfigurationItem container, PropertyDescriptor property) {
		_container = container;
		_property = property;
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public ConfigurationItem getModel() {
		return _container;
	}

	@Override
	public void setValue(Object newValue) {
		getModel().update(getProperty(), newValue);
	}

	@Override
	public Object getValue() {
		return getModel().value(getProperty());
	}

	@Override
	public PropertyValue getPropertyValue() {
		return new PropertyValueImpl(getModel(), getProperty());
	}

	@Override
	public void clearValue() {
		getModel().reset(getProperty());
	}

	@Override
	public boolean addValue(Object newElement) {
		switch (_property.kind()) {
			case MAP: {
				Mapping<Object, Object> keyMapping = _property.getKeyMapping();
				Object key = keyMapping.map(newElement);
				Map<Object, Object> mapValue = mapValue();
				if (mapValue == null) {
					_container.update(_property, Collections.singletonMap(key, newElement));
				} else {
					if (mapValue.containsKey(key)) {
						throw errorKeyViolation(_property.getKeyProperty(), key);
					}
					mapValue.put(key, newElement);
				}
				break;
			}

			case LIST: {
				Collection<Object> collectionValue = collectionValue();
				if (collectionValue == null) {
					_container.update(_property, Collections.singletonList(newElement));
				} else {
					Mapping<Object, Object> keyMapping = _property.getKeyMapping();
					if (keyMapping != null) {
						Object key = keyMapping.map(newElement);
						boolean containsKey = collectionValue.stream()
							.map(keyMapping)
							.anyMatch(key::equals);
						if (containsKey) {
							throw errorKeyViolation(_property.getKeyProperty(), key);
						}
					}
					collectionValue.add(newElement);
				}
				break;
			}

			case ITEM: {
				_container.update(_property, newElement);
				break;
			}

			default:
				throw new IllegalStateException(
					"Unsupported property '" + _property.getPropertyName() + "' of kind " + _property.kind()
						+ ".");
		}
		return true;
	}

	private Collection<Object> collectionValue() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Collection<Object> collectionValue = (Collection) _container.value(_property);
		return collectionValue;
	}

	private Map<Object, Object> mapValue() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<Object, Object> mapValue = (Map) _container.value(_property);
		return mapValue;
	}

	@Override
	public void removeValue(Object oldElement) {
		switch (_property.kind()) {
			case MAP: {
				Object collection = _container.value(_property);
				Mapping<Object, Object> keyMapping = _property.getKeyMapping();
				Object key = keyMapping.map(oldElement);
				@SuppressWarnings("unchecked")
				Map<Object, Object> mapValue = (Map<Object, Object>) collection;

				if (mapValue.get(key) == oldElement) {
					mapValue.remove(key);
				}
				break;
			}

			case LIST: {
				Object collection = _container.value(_property);
				@SuppressWarnings("unchecked")
				Collection<Object> collectionValue = (Collection<Object>) collection;
				collectionValue.remove(oldElement);
				break;
			}

			case ITEM: {
				if (_container.value(_property) == oldElement) {
					_container.update(_property, null);
				}
				break;
			}

			default:
				throw new IllegalStateException(
					"Unsupported property '" + _property.getPropertyName() + "' of kind " + _property.kind()
						+ ".");
		}
	}

	private TopLogicException errorKeyViolation(PropertyDescriptor property, Object key) {
		throw new TopLogicException(
			I18NConstants.SELECTED_OPTION_ALREADY_ADDED__PROPERTY_OPTION.fill(Labels.propertyLabelKey(property), key));
	}

}