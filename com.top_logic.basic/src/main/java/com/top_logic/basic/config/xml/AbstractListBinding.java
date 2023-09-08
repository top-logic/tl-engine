/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ListOperation;
import com.top_logic.basic.config.Position;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * {@link ConfigurationValueBinding} for collections of values.
 * 
 * @param <V>
 *        The value type of the property this binding is used for.
 * @param <E>
 *        The element type of the collection entries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListBinding<V, E> implements ConfigurationValueBinding<V> {

	private final String elementName;

	private final String attributeName;

	private final ConfigurationValueProvider<E> valueProvider;

	/**
	 * Creates a {@link ListValueBinding}.
	 * 
	 * @param elementName
	 *        Name of the "list-element" XML element to use, see {@link ListValueBinding}.
	 * @param attributeName
	 *        Name of the "value-attribute" XML attribute to use on "list-element" elements.
	 * @param valueProvider
	 *        The {@link ConfigurationValueProvider} for serializing the list element values.
	 */
	public AbstractListBinding(String elementName, String attributeName, ConfigurationValueProvider<E> valueProvider) {
		this.elementName = elementName;
		this.attributeName = attributeName;
		this.valueProvider = valueProvider;
	}

	@Override
	public V loadConfigItem(XMLStreamReader in, V baseValue)
			throws XMLStreamException, ConfigurationException {
		List<E> result = copyValueToList(baseValue);
		while (true) {
			int tag = in.nextTag();
			if (tag == XMLStreamConstants.END_ELEMENT) {
				break;
			}

			if (!elementName.equals(in.getLocalName())) {
				configurationError(I18NConstants.ERROR_UNEXPECTED_ELEMENT_NAME__EXPECTEDNAME_ACTUALNAME_LOCATION
					.fill(elementName, in.getLocalName(), XMLStreamUtil.atLocation(in)), in.getLocalName());
			}

			String valueText = in.getAttributeValue(null, attributeName);
			E value = valueProvider.getValue(attributeName, valueText);
			ListOperation operation = ConfigurationReader.Handler.getListOperationValue(in, ListOperation.ADD);
			switch (operation) {
				case ADD_OR_UPDATE:
					if (result.contains(value)) {
						handleUpdate(in, result, value, valueText);
					} else {
						Position position = ConfigurationReader.Handler.getPositionValue(in, Position.END);
						handleAdd(in, result, value, valueText, position);
					}
					break;
				case ADD:
					Position position = ConfigurationReader.Handler.getPositionValue(in, Position.END);
					handleAdd(in, result, value, valueText, position);
					break;

				case REMOVE:
					boolean existed = result.remove(value);
					if (!existed) {
						configurationError(I18NConstants.ERROR_VALUE_DOES_NOT_EXIST__VALUE_LOCATION.fill(valueText,
							XMLStreamUtil.atLocation(in)), valueText);
					}
					break;

				case UPDATE:
					if (!result.contains(value)) {
						configurationError(
							I18NConstants.ERROR_UPDATE_OBJECT_NOT_FOUND__VALUE_LOCATION
								.fill(valueText, XMLStreamUtil.atLocation(in)),
							valueText);
					}
					handleUpdate(in, result, value, valueText);
					break;

				default:
					configurationError(
						I18NConstants.ERROR_UNKNOWN_OPERATION__OPERATIONNAME_OPERATION_LOCATION
							.fill(ListOperation.class.getName(), operation, XMLStreamUtil.atLocation(in)),
						valueText);
			}

			XMLStreamUtil.nextEndTag(in);
		}
		return valueFromList(result);
	}

	/**
	 * Adds the new value to the given list depending on the configured position.
	 * 
	 * <p>
	 * If no position has been configured, the new value is added to the end of the list.
	 * </p>
	 * 
	 * @param reader
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param list
	 *        The list in which the new value should be inserted.
	 * @param newValue
	 *        The new value which should be inserted into the list.
	 * @param valueText
	 *        The attribute value text of the new value.
	 * @param position
	 *        The {@link Position} where the new value should be inserted.
	 * @throws ConfigurationException
	 *         If the list already contains the new value.
	 */
	private void handleAdd(XMLStreamReader reader, List<E> list, E newValue, String valueText, Position position)
			throws ConfigurationException {
		switch (position) {
			case BEGIN: {
				list.add(0, newValue);
				break;
			}
			case END: {
				list.add(newValue);
				break;
			}
			case BEFORE: {
				E reference = getReference(reader, list);
				int index = list.indexOf(reference);
				list.add(index, newValue);
				break;
			}
			case AFTER: {
				E reference = getReference(reader, list);
				int index = list.indexOf(reference);
				list.add(index + 1, newValue);
				break;
			}
			default:
				configurationError(
					I18NConstants.ERROR_UNKNOWN_POSITION__POSITIONNAME_POSITION_LOCATION.fill(Position.class.getName(),
						position, XMLStreamUtil.atLocation(reader)),
					valueText);
				break;
		}
	}

	/**
	 * Updates the position of an existing value of the given list.
	 * 
	 * @param reader
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param list
	 *        The list in which the value should be updated.
	 * @param newValue
	 *        The new value.
	 * @param valueText
	 *        The attribute value text of the new value.
	 * @throws ConfigurationException
	 *         If the value does not exist in the list.
	 */
	private void handleUpdate(XMLStreamReader reader, List<E> list, E newValue, String valueText)
			throws ConfigurationException {
		Position movePosition = ConfigurationReader.Handler.getPositionValue(reader, null);
		if (movePosition != null) {
			list.remove(newValue);
			handleAdd(reader, list, newValue, valueText, movePosition);
		} else {
			configurationError(
				I18NConstants.ERROR_MOVE_POSITION_MISSING__VALUE_LOCATION.fill(valueText,
					XMLStreamUtil.atLocation(reader)),
				valueText);
		}
	}

	/**
	 * Parses the configured reference which is needed to handle specific list adds.
	 * 
	 * @see #handleAdd(XMLStreamReader, List, Object, String, Position)
	 * 
	 * @param reader
	 *        {@link XMLStreamReader} that parses the configuration.
	 * @param list
	 *        Current list of configured objects
	 * @return The configured reference object.
	 * @throws ConfigurationException
	 *         If no object was found for the given reference.
	 */
	private E getReference(XMLStreamReader reader, List<E> list) throws ConfigurationException {
		String reference = reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
			ConfigurationSchemaConstants.LIST_REFERENCE_ATTR_NAME);
		E referenceObject = valueProvider.getValue(attributeName, reference);
		if (!list.contains(referenceObject)) {
			configurationError(
				I18NConstants.ERROR_REFERENCE_OBJECT_NOT_FOUND__VALUE_LOCATION.fill(reference,
					XMLStreamUtil.atLocation(reader)),
				reference);
		}
		return referenceObject;
	}

	private void configurationError(ResKey message, String value) throws ConfigurationException {
		throw new ConfigurationException(message, attributeName, value);
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, V value) throws XMLStreamException {
		for (E entry : valueAsList(value)) {
			out.writeEmptyElement(elementName);
			out.writeAttribute(attributeName, valueProvider.getSpecification(entry));
		}
	}

	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return value;
	}

	/**
	 * Creates a result value from the given list of elements.
	 */
	protected abstract V valueFromList(List<E> result);

	/**
	 * Converts the given value to a new modifiable list.
	 */
	protected abstract List<E> copyValueToList(V baseValue);

	/**
	 * Creates a list representation for the given value.
	 */
	protected abstract List<E> valueAsList(V value);

}
