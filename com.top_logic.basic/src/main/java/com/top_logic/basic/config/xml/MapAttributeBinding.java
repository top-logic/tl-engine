/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.MapOperation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * {@link ConfigurationValueBinding} that binds a XML structure given as follows to 
 * a {@link Map}. 
 * 
 * <xmp>
 * <value-element>
 *    <entry-element key-attribute="key1-value" value-attribute="config-value1">
 *    <entry-element key-attribute="key2-value" value-attribute="config-value2">
 *    ...
 * </value-element>
 * </xmp>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapAttributeBinding<K, V> implements ConfigurationValueBinding<Map<K, V>> {

    /** Name of the XML entity used as entry-element */
	private final String entryElement;
    /** Name of the XML attribute used as key-attribute */
	private final String keyAttribute;
    /** Name of the XML attribute used as value-attribute */
	private final String valueAttribute;
	
	/** Use to create the key-values from Objects and vice versa */
	private final ConfigurationValueProvider<V> valueProvider;
	private final ConfigurationValueProvider<K> keyProvider;

	private MapAttributeBinding(String anEntryElement, String aKeyAttribute, ConfigurationValueProvider<K> keyProvider, String aValueAttribute, ConfigurationValueProvider<V> valueProvider) {
	    
	    assert !aKeyAttribute.equals(aValueAttribute) : "key and value attribute must be distinct";
	    
		this.entryElement   = anEntryElement;
		this.keyAttribute   = aKeyAttribute;
		this.valueAttribute = aValueAttribute;
		
		this.keyProvider = keyProvider;
		this.valueProvider = valueProvider;
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, Map<K, V> map) throws XMLStreamException {

		for (Entry<K,V> entry : map.entrySet()) {
			out.writeStartElement(entryElement);
			out.writeAttribute(keyAttribute  , keyProvider.getSpecification(entry.getKey()));
			out.writeAttribute(valueAttribute, valueProvider.getSpecification(entry.getValue()));
			out.writeEndElement();
		}
	}

	@Override
	public Map<K, V> loadConfigItem(XMLStreamReader in, Map<K, V> baseValue) throws XMLStreamException, ConfigurationException {
		int startEvent = in.getEventType();
		if (startEvent != XMLStreamConstants.START_ELEMENT) {
			configurationError(I18NConstants.ERROR_EXPECTED_START_ELEMENT_ACTUALELEMENT_LOCATION.fill(startEvent,
				XMLStreamUtil.atLocation(in)));
		}
		
		Map<K, V> result;
		if (baseValue == null) {
			result = new LinkedHashMap<>();
		} else {
			result = new LinkedHashMap<>(baseValue);
		}
		int eventType;
		while ((eventType = in.nextTag()) == XMLStreamConstants.START_ELEMENT) {
			String localName = in.getLocalName();
			if (! entryElement.equals(localName)) {
				configurationError(I18NConstants.ERROR_UNEXPECTED_ELEMENT_NAME__EXPECTEDNAME_ACTUALNAME_LOCATION
					.fill(entryElement, localName, XMLStreamUtil.atLocation(in)));
			}
			
			K key = null;
			V value = null;
			for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
				String attributeName = in.getAttributeLocalName(n);
				if (XMLStreamUtil.getAttributeNamespace(in, n) != null) {
					// Attribute from foreign name space.
					continue;
				}
				if (keyAttribute.equals(attributeName)) {
					String keySpec = in.getAttributeValue(n);
					key = keyProvider.getValue(keyAttribute, keySpec);
				}
				else if (valueAttribute.equals(attributeName)) {
					String valueSpec = in.getAttributeValue(n);
					value = valueProvider.getValue(valueAttribute, valueSpec);
				}
				else {
					configurationError(I18NConstants.ERROR_UNEXPECTED_ARGUMENT__ARGUMENT_LOCATION.fill(attributeName,
						XMLStreamUtil.atLocation(in)));
				}
			}
			
			if (key == null) {
				configurationError(I18NConstants.ERROR_EXPECTED_ARGUMENT__ARGUMENT_LOCATION.fill(keyAttribute,
					XMLStreamUtil.atLocation(in)));
			}
			
			if (value == null) {
				configurationError(I18NConstants.ERROR_EXPECTED_ARGUMENT__ARGUMENT_LOCATION.fill(valueAttribute,
					XMLStreamUtil.atLocation(in)));
			}
			
			applyConfigOperation(in, result, key, value);
			
			int endEvent = in.nextTag();
			if (endEvent != XMLStreamConstants.END_ELEMENT) {
				configurationError(I18NConstants.ERROR_EXPECTED_END_ELEMENT_ACTUALELEMENT_LOCATION.fill(endEvent,
					XMLStreamUtil.atLocation(in)));
			}
		}
		
		if (eventType != XMLStreamConstants.END_ELEMENT) {
			configurationError(I18NConstants.ERROR_EXPECTED_END_ELEMENT_ACTUALELEMENT_LOCATION.fill(eventType,
				XMLStreamUtil.atLocation(in)));
		}
		
		if (result.isEmpty()) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(result);
		}

	}

	/**
	 * Applies the configured operation for a key value pair to the given map.
	 * 
	 * @param in
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param result
	 *        The map to which the configured object operation should be applied.
	 * @param key
	 * 		  The key of the configured object.
	 * @param value
	 * 		  The value of the configured object.
	 */
	private void applyConfigOperation(XMLStreamReader in, Map<K, V> result, K key, V value)
			throws ConfigurationException {
		MapOperation operation = ConfigurationReader.Handler.getMapOperationValue(in);
		switch (operation) {
			case ADD_OR_UPDATE:
				if (result.containsKey(key)) {
					handleUpdate(in, result, key, value);
				} else {
					handleAdd(in, result, key, value);
				}
				break;
			case ADD:
				handleAdd(in, result, key, value);
				break;

			case REMOVE:
				if (!result.containsKey(key)) {
					configurationError(I18NConstants.ERROR_VALUE_DOES_NOT_EXIST__VALUE_LOCATION.fill(key,
						XMLStreamUtil.atLocation(in)));

				}
				result.remove(key);
				break;

			case UPDATE:
				handleUpdate(in, result, key, value);
				break;

			default:
				configurationError(I18NConstants.ERROR_UNKNOWN_OPERATION__OPERATIONNAME_OPERATION_LOCATION
					.fill(MapOperation.class.getName(), operation, XMLStreamUtil.atLocation(in)));
		}
	}
	
	/**
	 * Adds the new key value pair to the given map.
	 * 
	 * @param reader
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param map
	 *        The map in which the new key value pair should be inserted.
	 * @param key
	 *        The new key which should be inserted into the list.
	 * @param value
	 *        The new value which should be inserted into the list.
	 * @throws ConfigurationException
	 *         If the map already contains the new key.
	 */
	private void handleAdd(XMLStreamReader reader, Map<K, V> map, K key, V value) throws ConfigurationException {
		if (map.containsKey(key)) {
			configurationError(
				I18NConstants.ERROR_VALUE_ALREADY_EXISTS__VALUE_LOCATION.fill(key, XMLStreamUtil.atLocation(reader)));
		}
		map.put(key, value);
	}

	/**
	 * Updates the given key with the new value.
	 * 
	 * @param reader
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param map
	 *        The map in which the key should be updated.
	 * @param key
	 *        The key that should be updated.
	 * @param value
	 *        The new value of the key.
	 * @throws ConfigurationException
	 *         If the map does not contains the given key.
	 */
	private void handleUpdate(XMLStreamReader reader, Map<K, V> map, K key, V value) throws ConfigurationException {
		V oldValue = map.get(key);
		if (oldValue == null) {
			configurationError(
				I18NConstants.ERROR_UPDATE_OBJECT_NOT_FOUND__VALUE_LOCATION.fill(key,
					XMLStreamUtil.atLocation(reader)));
		}
		map.put(key, value);
	}

	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}
	
	@Override
	public Map<K, V> defaultValue() {
		return Collections.emptyMap();
	}
	
	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return Collections.emptyMap();
		}
		return value;
	}
	
	private void configurationError(ResKey message) throws ConfigurationException {
		throw new ConfigurationException(message, keyAttribute, valueAttribute);
	}

    public static <K, V> MapAttributeBinding<K, V> createMapAttributeBinding(String entryElement, String keyAttribute, ConfigurationValueProvider<K> keyProvider, 
			String valueAttribute, ConfigurationValueProvider<V> valueProvider) {
		return new MapAttributeBinding<>(entryElement, keyAttribute, keyProvider, valueAttribute, valueProvider);
	}
}