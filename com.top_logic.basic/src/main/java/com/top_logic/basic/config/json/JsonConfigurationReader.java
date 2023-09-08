/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.AbstractConfigurationReader;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.rw.ReaderR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonToken;

/**
 * Reader instantiating {@link ConfigurationItem}s from Json definition files.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JsonConfigurationReader extends AbstractConfigurationReader {

	private ConfigurationDescriptor _rootDescriptor;

	private Content _currentSource;

	private JsonReader _input;

	private boolean _unexpectedEntriesAsWarn = false;

	/**
	 * Creates a new {@link JsonConfigurationReader}.
	 * 
	 * @param rootDescriptor
	 *        {@link ConfigurationDescriptor} of the serialized item.
	 */
	public JsonConfigurationReader(InstantiationContext context, ConfigurationDescriptor rootDescriptor) {
		super(context);
		_rootDescriptor = rootDescriptor;
	}

	/**
	 * Whether elements that are not expected (values for non existing properties) are treated as
	 * warning or error
	 *
	 * @param value
	 *        If <code>true</code>, then values for non existing properties are not treated as error
	 *        but as warning.
	 */
	public void treatUnexpectedEntriesAsWarn(boolean value) {
		_unexpectedEntriesAsWarn = value;

	}

	@Override
	protected ConfigurationItem readInternal(Content source, ConfigurationItem currentResult)
			throws ConfigurationException {
		try {
			return readInternal0(source, currentResult);
		} catch (IOException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_READING_CONTENT__CONTENT_NAME.fill(source.getName()),
				ex);
		}
	}

	private ConfigurationItem readInternal0(Content source, ConfigurationItem currentResult)
			throws IOException, ConfigurationException {
		_currentSource = source;
		_input = toReader(source);
		return nextItem(_rootDescriptor, currentResult);
	}

	private ConfigBuilder nextItem(ConfigurationDescriptor expectedDescriptor, ConfigurationItem baseItem)
			throws IOException, ConfigurationException {
		JsonToken currentState = _input.peek();
		switch (currentState) {
			case BEGIN_ARRAY:
				return readPolymorphicItem(expectedDescriptor, baseItem);
			case BEGIN_OBJECT:
				return readMonomorphicItem(expectedDescriptor, baseItem);
			default:
				throw new IOException(
					"Unexpected input state " + currentState + " when reading item" + inSourceAtPath() + ".");
		}
	}

	private ConfigBuilder readPolymorphicItem(ConfigurationDescriptor staticType, ConfigurationItem baseItem)
			throws IOException, ConfigurationException {
		_input.beginArray();
		Class<?> polymorphicType =
			ConfigUtil.getClassForNameMandatory(Object.class, "polymorphicTypeDefinition", _input.nextString());

		ConfigBuilder result;
		if (ConfigurationItem.class.isAssignableFrom(polymorphicType)) {
			result = TypedConfiguration.createConfigBuilder(polymorphicType);
		} else {
			Class<?> expectedConfigType = staticType.getConfigurationInterface();
			if (PolymorphicConfiguration.class.isAssignableFrom(expectedConfigType)) {
				Factory factory = DefaultConfigConstructorScheme.getFactory(polymorphicType);
				if (!expectedConfigType.isAssignableFrom(factory.getConfigurationInterface())) {
					throw new IOException(
						"Configured implementation type '" + polymorphicType + "' is not a sub type of the expected type '"
							+ expectedConfigType + "'" + inSourceAtPath() + ".");
				}
				result = createBuilderForImplementationClass(staticType, polymorphicType);
			} else {
				throw new IOException("Polymorphic item definition, but polymorphic type '" + polymorphicType
					+ "' is not a configuration" + inSourceAtPath() + ".");
			}
		}
		fillBuilder(result, baseItem);
		_input.endArray();
		return result;
	}

	private void fillBuilder(ConfigBuilder result, ConfigurationItem baseItem)
			throws IOException, ConfigurationException {
		_input.beginObject();
		if (baseItem != null) {
			moveValues(baseItem, result);
		}
		ConfigurationDescriptor descriptor = result.descriptor();
		while (_input.hasNext()) {
			String key = _input.nextName();
			PropertyDescriptor property = descriptor.getProperty(key);
			if (property == null) {
				String msg = "No property with name '" + key + "' found " + inSourceAtPath() + " (see also "
					+ descriptor.getConfigurationInterface().getName() + ").";
				if (_unexpectedEntriesAsWarn) {
					_context.info(msg, Protocol.WARN);
				} else {
					_context.error(msg);
				}
				_input.skipValue();
			} else {
				updateProperty(result, property);
			}
		}
		_input.endObject();
	}

	private void updateProperty(ConfigBuilder result, PropertyDescriptor property)
			throws IOException, ConfigurationException {
		Object currentValue;
		if (result.valueSet(property)) {
			currentValue = result.value(property);
		} else {
			currentValue = null;
		}
		JsonValueBinding valueBinding = fetchValueBinding(property);
		if (valueBinding != null) {
			Object updatedValue = valueBinding.loadConfigItem(property, _input, currentValue);
			result.update(property, updatedValue);
			return;
		}

		switch (property.kind()) {
			case ARRAY:
				updateArrayValue(result, property, currentValue);
				break;
			case COMPLEX:
				throw new IOException("No Json binding found for complex property '" + property + "'.");
			case DERIVED:
				throw new IOException("Update for derived property '" + property + "'" + inSourceAtPath() + ".");
			case ITEM:
				updateItemValue(result, property, currentValue);
				break;
			case LIST:
				updateListValue(result, property, (List<?>) currentValue);
				break;
			case MAP:
				updateMapValue(result, property, (Map<?, ?>) currentValue);
				break;
			case PLAIN:
				updatePlainValue(result, property);
				break;
			case REF:
				throw new IOException("Update for reference property '" + property + "'" + inSourceAtPath() + ".");
			default:
				throw PropertyKind.noSuchPropertyKind(property.kind());
		}
	}

	private JsonValueBinding fetchValueBinding(PropertyDescriptor property) throws ConfigurationException {
		JsonValueBinding valueBinding;
		JsonBinding annotation = property.getAnnotation(JsonBinding.class);
		if (annotation == null) {
			valueBinding = null;
		} else {
			valueBinding = ConfigUtil.getInstance(annotation.value());
		}
		return valueBinding;
	}

	private void updateItemValue(ConfigBuilder result, PropertyDescriptor property, Object currentValue)
			throws ConfigurationException, IOException {
		if (property.getValueProvider() != null && _input.peek() == JsonToken.STRING) {
			updatePlainValue(result, property);
		} else {
			result.update(property, nextElement(property, currentValue));
		}
	}

	private void updatePlainValue(ConfigBuilder result, PropertyDescriptor property)
			throws IOException, ConfigurationException {
		Object propertyValue;
		switch(_input.peek()) {
			case BEGIN_ARRAY:
			case BEGIN_OBJECT:
			case END_ARRAY:
			case END_DOCUMENT:
			case END_OBJECT:
			case NAME:
				throw new IOException("Unexpected token '" + _input.peek() + "' for plain property '" + property + "'"
					+ inSourceAtPath() + ".");
			case BOOLEAN:
				Class<?> elementType = property.getElementType();
				if (elementType != Boolean.class && elementType != boolean.class) {
					throw new IOException("Found boolean value for property '" + property + "' with element type '"
						+ elementType + "'" + inSourceAtPath() + ".");
				}
				propertyValue = Boolean.valueOf(_input.nextBoolean());
				break;
			case NULL:
				_input.nextNull();
				propertyValue = null;
				break;
			case NUMBER:
			case STRING:
				String serializedValue = _input.nextString();
				ConfigurationValueProvider valueProvider = property.getValueProvider();
				propertyValue = valueProvider.getValue(property.getPropertyName(), serializedValue);
				break;
			default:
				throw new UnreachableAssertion("Unknown token '" + _input.peek() + "'" + inSourceAtPath()+".");
		}
		result.update(property, propertyValue);
	}

	private void updateMapValue(ConfigBuilder result, PropertyDescriptor property, Map<?, ?> currentValue)
			throws IOException, ConfigurationException {
		_input.beginObject();
		if (_input.hasNext()) {
			if (currentValue == null) {
				currentValue = Collections.emptyMap();
			}
			PropertyDescriptor keyProperty = property.getKeyProperty();
			ConfigurationValueProvider keyValueProvider = keyProperty.getValueProvider();
			// preserve order of values.
			Map<Object, Object> baseMap = new LinkedHashMap<>(currentValue);
			do {
				String keyString = _input.nextName();
				Object key = keyValueProvider.getValue(keyProperty.getPropertyName(), keyString);
				Object elementForUpdate = currentValue.get(key);
				baseMap.put(key, nextElement(property, elementForUpdate, keyProperty, key));
			} while (_input.hasNext());
			result.update(property, baseMap);
		} else {
			// Nothing to to here.
		}
		_input.endObject();
	}

	private void updateArrayValue(ConfigBuilder result, PropertyDescriptor property, Object currentValue)
			throws IOException, ConfigurationException {
		Class<?> arrayType = property.getElementType();
		List<?> listValue = readListValue(property);
		Object newValue;
		if (currentValue == null) {
			newValue = listValue.toArray((Object[])Array.newInstance(arrayType, listValue.size()));
		} else if (listValue.isEmpty()) {
			newValue = currentValue;
		} else {
			int currentSize = Array.getLength(currentValue);
			newValue = Array.newInstance(arrayType, currentSize + listValue.size());
			for (int i = 0; i < currentSize; i++) {
				Array.set(arrayType, i, Array.get(arrayType, i));
			}
			for (int i = 0; i < listValue.size(); i++) {
				Array.set(arrayType, i + currentSize, listValue.get(i));
			}
		}
		result.update(property, newValue);
	}

	private void updateListValue(ConfigBuilder result, PropertyDescriptor property, List<?> currentValue)
			throws IOException, ConfigurationException {
		List<?> listValue = readListValue(property);
		List<?> newValue;
		if (currentValue == null) {
			newValue = listValue;
		} else if (listValue.isEmpty()) {
			newValue = currentValue;
		} else {
			ArrayList<Object> tmp = new ArrayList<>(currentValue);
			tmp.addAll(listValue);
			newValue = tmp;
		}
		result.update(property, newValue);
	}

	private List<?> readListValue(PropertyDescriptor property) throws IOException, ConfigurationException {
		_input.beginArray();
		List<?> result;
		if (_input.hasNext()) {
			ArrayList<Object> tmp = new ArrayList<>();
			do {
				tmp.add(nextElement(property, null));
			} while (_input.hasNext());
			result = tmp;
		} else {
			result = Collections.emptyList();
		}

		_input.endArray();
		return result;
	}

	private Object nextElement(PropertyDescriptor property, Object elementForUpdate)
			throws IOException, ConfigurationException {
		return nextElement(property, elementForUpdate, null, null);
	}

	private Object nextElement(PropertyDescriptor property, Object elementForUpdate, PropertyDescriptor addProp,
			Object addValue)
			throws IOException, ConfigurationException {
		ConfigurationItem configForUpdate;
		if (elementForUpdate instanceof ConfigurationItem) {
			configForUpdate = (ConfigurationItem) elementForUpdate;
		} else {
			configForUpdate = null;
		}
		ConfigurationItem nextItem = nextItem(property.getDefaultDescriptor(), configForUpdate);
		if (nextItem == null) {
			return null;
		}
		if (addProp != null) {
			nextItem.update(addProp, addValue);
		}
		Object element;
		if (property.isInstanceValued()) {
			element = _context.getInstance((PolymorphicConfiguration<?>) nextItem);
		} else {
			element = nextItem;
		}
		return element;
	}

	private ConfigBuilder createBuilderForImplementationClass(ConfigurationDescriptor expectedDescriptor,
			Class<?> implementationClass) throws ConfigurationException {
		ConfigBuilder result =
			TypedConfiguration.createConfigBuilderForImplementationClass(implementationClass, expectedDescriptor);

		// install implementation class as it was set explicit
		result.update(classProperty(result.descriptor()), implementationClass);
		return result;

	}

	private PropertyDescriptor classProperty(ConfigurationDescriptor staticType) {
		return staticType.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
	}

	private ConfigBuilder readMonomorphicItem(ConfigurationDescriptor descriptor, ConfigurationItem baseItem)
			throws IOException, ConfigurationException {
		ConfigBuilder result = TypedConfiguration.createConfigBuilder(descriptor);
		fillBuilder(result, baseItem);
		return result;
	}

	private String inSourceAtPath() {
		return inSource() + JsonUtilities.atLocationString(_input);
	}
	private String inSource() {
		return " in " + _currentSource.getName();
	}

	private JsonReader toReader(Content source) throws IOException {
		Reader baseReader;
		if (source instanceof BinaryContent) {
			baseReader =
				new InputStreamReader(((BinaryContent) source).getStream(), JsonUtilities.DEFAULT_JSON_ENCODING);
		} else if (source instanceof CharacterContent) {
			baseReader = ((CharacterContent) source).getReader();
		} else {
			throw new AssertionError("Content must be either binary or character content.");
		}
		JsonReader jsonReader;
		if (_expander == null) {
			jsonReader = new JsonReader(new ReaderR(baseReader));
		} else {
			jsonReader = new AliasedJsonReader(new ReaderR(baseReader), _expander);
		}
		return jsonReader;
	}

}

