/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Arrays.*;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.basic.config.order.DefaultOrderStrategy;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagUtil;

/**
 * Generic writer for {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationWriter implements AutoCloseable {
	
	private static final XMLOutputFactory OUTPUT_FACTORY;
	static {
		OUTPUT_FACTORY = XMLOutputFactory.newInstance();
	}

	private XMLStreamWriter out;

	private String _ns;

	private String _prefix;

	private boolean _writeNamespace;

	private boolean _writeXMLHeader;

	private boolean _transitiveClose;

	/**
	 * Creates a {@link ConfigurationWriter}.
	 * 
	 * @param writer
	 *        The writer to write the generated XML to.
	 */
	public ConfigurationWriter(Writer writer) throws XMLStreamException {
		this(OUTPUT_FACTORY.createXMLStreamWriter(writer));
	}

	/**
	 * Creates a {@link ConfigurationWriter}.
	 *
	 * @param out
	 *        The {@link XMLStreamWriter} to write to.
	 */
	public ConfigurationWriter(XMLStreamWriter out) {
		this(out, true);
	}

	/**
	 * Creates a {@link ConfigurationWriter}.
	 *
	 * @param out
	 *        The {@link XMLStreamWriter} to write to.
	 * @param transitiveClose
	 *        Whether to close the underlying stream writer, if this writer is closed.
	 */
	public ConfigurationWriter(XMLStreamWriter out, boolean transitiveClose) {
		this.out = out;
		_transitiveClose = transitiveClose;
		setNamespaceWriting(true);
		setXMLHeaderWriting(true);
	}

	/**
	 * {@link XMLStreamWriter} writing the {@link ConfigurationItem}.
	 */
	protected XMLStreamWriter getXMLWriter() {
		return out;
	}

	/**
	 * Flag for suppressing the namespace writing if it's already written to avoid unnecessary
	 * duplicates.
	 */
	public void setNamespaceWriting(boolean writeNamespace) {
		_writeNamespace = writeNamespace;
	}

	/**
	 * Determines whether the XML header of the document must be written.
	 */
	public void setXMLHeaderWriting(boolean writeXMLHeader) {
		_writeXMLHeader = writeXMLHeader;
	}

	/**
	 * Sets the XML namespace to save the configuration in.
	 * 
	 * @param prefix
	 *        The namespace prefix to use.
	 * @param namespace
	 *        The namespace URI to associate the configuration with.
	 */
	public void setNamespace(String prefix, String namespace) throws XMLStreamException {
		_prefix = prefix;
		_ns = namespace;
		out.setPrefix(prefix, namespace);
	}

	/**
	 * Writes the given {@link ConfigurationItem}.
	 * 
	 * @param localName
	 *        Configuration element name that should be used as root element.
	 */
	public final void write(String localName, Class<? extends ConfigurationItem> staticType, ConfigurationItem item)
			throws XMLStreamException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(staticType);
		write(localName, descriptor, item);
	}

	public void write(String localName, ConfigurationDescriptor staticType, ConfigurationItem item) throws XMLStreamException {
		if (_ns != null) {
			out.setPrefix(_prefix, _ns);
		}
		out.setPrefix(ConfigurationSchemaConstants.CONFIG_NS_PREFIX, ConfigurationSchemaConstants.CONFIG_NS);
		if (_writeXMLHeader) {
			out.writeStartDocument();
		}
		
		writeRootElement(localName, staticType, item);

		out.writeEndDocument();
	}

	public void writeRootElement(String localName, ConfigurationDescriptor staticType, ConfigurationItem item)
			throws XMLStreamException {
		writeStartElement(localName);
		writeNamespace();
		
		writeContent(localName, staticType, item);
		out.writeEndElement();
	}

	private void writeNamespace() throws XMLStreamException {
		if (_writeNamespace) {
			if (_ns != null) {
				out.writeNamespace(_prefix, _ns);
			}

			out.writeNamespace(ConfigurationSchemaConstants.CONFIG_NS_PREFIX, ConfigurationSchemaConstants.CONFIG_NS);
		}
	}
	
	protected void writeContent(String tagName, ConfigurationDescriptor staticType, ConfigurationItem item) throws XMLStreamException {
		ConfigurationDescriptor descriptor = item.descriptor();
		Class<?> concreteConfigInterface = descriptor.getConfigurationInterface();
		TagName tagNameAnnotation = concreteConfigInterface.getAnnotation(TagName.class);
		
		// Check, whether the concrete configuration interface cannot be
		// reconstructed from from the content.
		boolean typeAnnotationRequired;

		final PropertyDescriptor implClassProperty;
		if (item instanceof PolymorphicConfiguration<?>) {
			implClassProperty = descriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		} else {
			implClassProperty = null;
		}
		if (tagNameAnnotation != null && tagName.equals(tagNameAnnotation.value())) {
			typeAnnotationRequired = false;
		} else if (implClassProperty != null) {
			if (item.value(implClassProperty) != null) {
				boolean staticPolymorph =
					PolymorphicConfiguration.class.isAssignableFrom(staticType.getConfigurationInterface());
				if (!staticPolymorph) {
					/* Assume the static type is not polymorph and a class is set. If no config
					 * interface is written, then the reader expects a simple ConfigurationItem
					 * which has no property class. */
					typeAnnotationRequired = true;
				} else {
					Class<?> implementationClass = (Class<?>) item.value(implClassProperty);
					try {
						Class<?> expectedConfigurationInterface =
							DefaultConfigConstructorScheme.getFactory(implementationClass).getConfigurationInterface();
						typeAnnotationRequired = !expectedConfigurationInterface.equals(concreteConfigInterface);
					} catch (ConfigurationException ex) {
						throw new XMLStreamException(
							"Cannot serialize configuration, invalid implementation class '"
								+ implementationClass.getName() + "'.", ex);
					}
				}
			} else {
				typeAnnotationRequired = true;
			}
		} else {
			typeAnnotationRequired = item.descriptor() != staticType;
		}
		
		if (typeAnnotationRequired) {
			out.writeAttribute(ConfigurationSchemaConstants.CONFIG_NS, ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR, concreteConfigInterface.getName());
		}
		
		List<PropertyDescriptor> properties =
			new DefaultOrderStrategy.Collector(NoCustomizations.INSTANCE, descriptor) {
				@Override
				protected boolean isHidden(PropertyDescriptor property) {
					// All properties must be dumped, esp. the configuration-interface
					return false;
				}

				@Override
				protected boolean collectUnorderedProperties() {
					// All properties must be dumped, esp. the properties not in @DisplayOrder
					return true;
				}

				@Override
				protected DisplayStrategy getDisplayStrategy(ConfigurationDescriptor desc) {
					DisplayStrategy displayStrategy = super.getDisplayStrategy(desc);
					if (displayStrategy == DisplayStrategy.IGNORE) {
						// All properties must be dumped, esp. the super properties
						displayStrategy = defaultDisplayStrategy();
					}
					return displayStrategy;
				}

			}.collect();

		// Values of properties that are produced by a @Format annotation but not
		// serialized as attribute value because the value contains a special character.
		Map<NamedConstant, String> formattedValues = Collections.emptyMap();

		if (implClassProperty != null) {
			// The implementation class property must be written first to prevent it from being
			// serialized as sub-tag, if another property serialized before cannot be written as
			// attribute.
			formattedValues = writePlain(staticType, item, implClassProperty, formattedValues);
		}
		for (PropertyDescriptor property : properties) {
			if (property == implClassProperty) {
				// Already written as first property.
				continue;
			}
			switch (property.kind()) {
				case LIST:
				case MAP:
				case ARRAY:
				case ITEM: {
					if (property.getValueProvider() != null) {
						formattedValues = writePlain(staticType, item, property, formattedValues);
					}
					break;
				}
				case COMPLEX:
				case PLAIN: {
					formattedValues = writePlain(staticType, item, property, formattedValues);
					break;
				}
				default:
					break;
			}
		}
		for (PropertyDescriptor property : properties) {
			if (isSpecial(property)) {
				continue;
			}
			String specification = formattedValues.get(property.identifier());
			if (specification != null) {
				// Property value contains special characters that cannot be serialized in a
				// attribute
				// value. Use an inner tag instead.
				writeStartElement(property.getPropertyName());

				writeStringAsTagContent(specification);
				out.writeEndElement();
			} else {
				writeNonPlainProperty(item, property, false);
			}
		}
	}

	private void writeStringAsTagContent(String specification) throws XMLStreamException {
		int last = 0, cr;
		while ((cr = specification.indexOf('\r', last)) >= 0) {
			// Note: Produce at least one CData section to mark this value as not
			// pretty-printable because it contains a CR character.
			if (last == 0 || cr > last) {
				String part = specification.substring(last, cr);
				if (last == 0 || nextTagSpecial(part, 0) >= 0) {
					writeCData(part);
				} else {
					out.writeCharacters(part);
				}
			}
			out.writeEntityRef("#" + ((int) specification.charAt(cr)));
			last = cr + 1;
		}
		if (last < specification.length()) {
			String lastPart = specification.substring(last);
			if (nextTagSpecial(lastPart, 0) >= 0) {
				writeCData(lastPart);
			} else {
				out.writeCharacters(lastPart);
			}
		}
	}

	private void writeCData(String part) throws XMLStreamException {
		int endindex = part.indexOf(TagUtil.CDATA_END);
		int start = 0;
		while (endindex >= 0) {
			out.writeCData(part.substring(start, endindex + 2));
			start = endindex + 2;
			endindex = part.indexOf(TagUtil.CDATA_END, start + 1);
		}
		out.writeCData(part.substring(start));
	}

	/**
	 * Writes the value of the given property in the given item.
	 * 
	 * @param item
	 *        The item that holds the value to be written.
	 * @param property
	 *        A {@link PropertyDescriptor} (appropriate for the given {@link ConfigurationItem}) of
	 *        {@link PropertyDescriptor#kind() kind}
	 *        {@link PropertyKind#ITEM},{@link PropertyKind#LIST},{@link PropertyKind#ARRAY},{@link PropertyKind#MAP},
	 *        or {@link PropertyKind#COMPLEX}. Properties of different kind are ignored.
	 * @param writeDefaultValues
	 *        Whether also the default value must be written.
	 */
	public void writeNonPlainProperty(ConfigurationItem item, PropertyDescriptor property, boolean writeDefaultValues)
			throws XMLStreamException {
		switch (property.kind()) {
			case ITEM: {
				if (writeDefaultValues || needsToBeWritten(item, property)) {
					writeItem(item, property);
				}
				break;
			}
			case ARRAY: {
				if (writeDefaultValues || needsToBeWritten(item, property)) {
					writeArray(item, property);
				}
				break;
			}
			case LIST: {
				if (writeDefaultValues || needsToBeWritten(item, property)) {
					writeList(item, property);
				}
				break;
			}
			case MAP: {
				if (writeDefaultValues || needsToBeWritten(item, property)) {
					writeMap(item, property);
				}
				break;
			}
			case COMPLEX: {
				if (writeDefaultValues || needsToBeWritten(item, property)) {
					writeComplex(item, property);
				}
				break;
			}
			default:
				// Unsupported kind. ignore.
				break;
		}
	}

	/**
	 * Check, whether some property of the given item has a value explicitly set.
	 */
	private boolean isModified(ConfigurationItem item) {
		if (item == null) {
			return false;
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (isModified(item, property)) {
				return true;
			}
		}
		return false;
	}

	private boolean isModified(ConfigurationItem item, PropertyDescriptor property) {
		if (item.valueSet(property)) {
			return true;
		}
		if (property.isMandatory()) {
			// Accessing an unset mandatory properties would throw an exception.
			return false;
		}
		if (property.kind() == PropertyKind.ITEM) {
			Object value = item.value(property);
			if (property.isInstanceValued() && !(value instanceof ConfiguredInstance<?>)) {
				/* An instance without configuration; this can not be modified with reflection to
				 * any configuration. */
				return false;
			}
			ConfigurationAccess configAccess = property.getConfigurationAccess();
			ConfigurationItem subitem = configAccess.getConfig(value);
			return isModified(subitem);
		}
		if (property.kind() == PropertyKind.LIST) {
			return isCollectionModified(property, (Collection<?>) item.value(property));
		}
		if (property.kind() == PropertyKind.ARRAY) {
			Object[] valueArray = (Object[]) item.value(property);
			return isCollectionModified(property, asList(valueArray));
		}
		if (property.kind() == PropertyKind.MAP) {
			Map<?, ?> valueMap = (Map<?, ?>) item.value(property);
			return isCollectionModified(property, valueMap.values());
		}
		return false;
	}

	private boolean isCollectionModified(PropertyDescriptor property, Collection<?> values) {
		if (values.isEmpty()) {
			return false;
		}
		ConfigurationAccess configAccess = property.getConfigurationAccess();
		for (Object value : values) {
			if (property.isInstanceValued() && !(value instanceof ConfiguredInstance<?>)) {
				/* An instance without configuration; this can not be modified with reflection to
				 * any configuration. */
				continue;
			}
			ConfigurationItem item = configAccess.getConfig(value);
			if (isModified(item)) {
				return true;
			}
		}
		return false;
	}

	protected void writeComplex(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		Object value = item.value(property);
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider != null) {
			if (valueProvider.isLegalValue(value)) {
				// Has been written as attribute using format.
				return;
			}
		}
		if (value != null) {
			writeStartElement(property.getPropertyName());
			
			// TODO: Type safety must be guaranteed at a higher level during
			// property descriptor creation. The unchecked conversion should be
			// moved to a location from which it is obvious that there might no
			// problem at runtime.
			ConfigurationValueBinding valueBinding = property.getValueBinding();
			valueBinding.saveConfigItem(out, value);
			
			out.writeEndElement();
		}
	}

	/**
	 * Writes a property that can be regularly serialized as attribute.
	 * 
	 * @param staticType
	 *        The {@link ConfigurationDescriptor} that would be resolved from the context, when
	 *        reading back the surrounding item.
	 * @param item
	 *        The underlying {@link ConfigurationItem}.
	 * @param property
	 *        The property whose value should be serialized.
	 * @param formattedValues
	 *        Values of properties that are produced by a {@link Format} annotation but not
	 *        serialized as attribute value because the value contains a special character.
	 * 
	 * @return Whether this property was serialized as inner tag for encoding reasons.
	 */
	protected Map<NamedConstant, String> writePlain(ConfigurationDescriptor staticType, ConfigurationItem item,
			PropertyDescriptor property, Map<NamedConstant, String> formattedValues) throws XMLStreamException {
		if (hasDefaultValue(staticType, item, property)) {
			return formattedValues;
		}
		Object value = item.value(property);
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			if (property.kind() == PropertyKind.PLAIN) {
				throw new XMLStreamException("Plain property without format: " + property);
			}
			return formattedValues;
		}
		if (!valueProvider.isLegalValue(value)) {
			if (property.kind() == PropertyKind.PLAIN) {
				throw new XMLStreamException("Plain property '" + property + "' with format '" + valueProvider
					+ "' that does not accept property value '" + value + "'.");
			}
			return formattedValues;
		}
		String specification = valueProvider.getSpecification(value);
		
		if (nextAttributeSpecial(specification, 0) >= 0) {
			return put(formattedValues, property.identifier(), specification);
		} else {
			out.writeAttribute(property.getPropertyName(), specification);
			return formattedValues;
		}
	}

	private static <K, V> Map<K, V> put(Map<K, V> map, K key, V value) {
		switch (map.size()) {
			case 0:
				return Collections.singletonMap(key, value);
			case 1:
				map = new HashMap<>(map);
				//$FALL-THROUGH$
			default:
				map.put(key, value);
				return map;
		}
	}

	/**
	 * Whether the given value contains special characters so that it should not be used as an
	 * attribute value.
	 */
	public static boolean containsAttributeSpecial(CharSequence attributeValue) {
		return nextAttributeSpecial(attributeValue, 0) >= 0;

	}

	private static int nextAttributeSpecial(CharSequence specification, int start) {
		for (int n = start, cnt = specification.length(); n < cnt; n++) {
			switch (specification.charAt(n)) {
				case '"':
				case '<':
				case '\n':
				case '\r':
					return n;
				default: // Not a special.
			}
		}
		return -1;
	}

	private int nextTagSpecial(String specification, int start) {
		for (int n = start, cnt = specification.length(); n < cnt; n++) {
			switch (specification.charAt(n)) {
				case '<':
				case '\n':
				case '\r':
					return n;
				default: // Not a special.
			}
		}
		return -1;
	}

	private boolean hasDefaultValue(ConfigurationDescriptor staticType, ConfigurationItem item,
			PropertyDescriptor property) {
		if (!isImplementationClassProperty(property)) {
			return !item.valueSet(property);
		}

		if (item.valueSet(property)) {
			if (property.getValueDescriptor() == staticType) {
				return false;
			} else {
				// Potentially ignore the explicitly set value, since a custom tag was choosen.
			}
		}

		if (property.isMandatory() && !item.valueSet(property)) {
			/* Cannot access the value of properties that are mandatory but not set. Therefore,
			 * return that the property has its default value, to prevent the caller from trying to
			 * access the value. */
			return true;
		}

		/* It is wrong to compare the actual value with the default value and return that. The
		 * implementation class property can influence the item type and that influences the default
		 * value of this property. */
		PropertyDescriptor expectedImplClassProperty = staticType.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		if (expectedImplClassProperty == null) {
			// May happen, if a PolymorphicConfiguration is serialized in the context of
			// ConfigurationItem.
			return false;
		}
		return Utils.equals(expectedImplClassProperty.getDefaultValue(), item.value(property));
	}

	private boolean isImplementationClassProperty(PropertyDescriptor property) {
		return property.identifier().equals(getImplementationClassProperty().identifier());
	}

	private PropertyDescriptor getImplementationClassProperty() {
		ConfigurationDescriptor polymorphicConfig = getConfigurationDescriptor(PolymorphicConfiguration.class);
		return polymorphicConfig.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
	}

	private boolean isSpecial(PropertyDescriptor property) {
		return property.getPropertyName().equals(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
	}

	protected void writeItem(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		Object value = item.value(property);
		ConfigurationValueProvider<?> format = property.getValueProvider();
		if (format != null && format.isLegalValue(value)) {
			// Was already serialized as attribute.
			return;
		}
		if (value != null) {
			ConfigurationItem config = property.getConfigurationAccess().getConfig(value);
			writeItemTag(property, config);
		} else {
			String tagName = property.getPropertyName();
			writeStartElement(tagName);
			if (PolymorphicConfiguration.class
				.isAssignableFrom(property.getDefaultDescriptor().getConfigurationInterface())) {
				out.writeAttribute(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, "");
			} else {
				out.writeAttribute(ConfigurationSchemaConstants.CONFIG_NS,
					ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR, "");
			}
			out.writeEndElement();
		}
	}

	private boolean needsToBeWritten(ConfigurationItem item, PropertyDescriptor property) {
		boolean valueSet = item.valueSet(property);
		if (!valueSet && property.isMandatory()) {
			// Accessing an unset mandatory properties would throw an exception.
			return false;
		}
		return valueSet || isModified(item, property);
	}

	private void writeItemTag(PropertyDescriptor property, ConfigurationItem valueConfig) throws XMLStreamException {
		String tagName;
		ConfigurationDescriptor valueDescriptor;

		selectDescriptor:
		{
			if (property.isDefaultContainer()) {
				valueDescriptor = valueConfig.descriptor();
				tagName = property.getElementName(valueDescriptor);
				if (tagName != null) {
					// Custom tag found, use custom expected descriptor.
					break selectDescriptor;
				}
			}

			// The expected value descriptor is the general descriptor of the context property.
			tagName = property.getPropertyName();
			valueDescriptor = property.getDefaultDescriptor();
		}

		writeStartElement(tagName);
		writeContent(tagName, valueDescriptor, valueConfig);
		out.writeEndElement();
	}

	protected void writeMap(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		writeMap(property, (Map<?, ?>) item.value(property));
	}

	/**
	 * Writes the map value for the given property.
	 * 
	 * @param property
	 *        A {@link PropertyDescriptor} of {@link PropertyDescriptor#kind() kind}
	 *        {@link PropertyKind#MAP}.
	 * @param value
	 *        A {@link Map} value for the given property.
	 * 
	 * @see #writeList(PropertyDescriptor, List)
	 * @see #write(String, ConfigurationDescriptor, ConfigurationItem)
	 */
	public void writeMap(PropertyDescriptor property, Map<?, ?> value) throws XMLStreamException {
		ConfigurationValueProvider<?> format = property.getValueProvider();
		if (format != null && format.isLegalValue(value)) {
			// Was already serialized as attribute.
			return;
		}

		if (property.isDefaultContainer()) {
			writeMapContents(property, value);
		} else {
			writeStartElement(property.getPropertyName());
			writeMapContents(property, value);
			out.writeEndElement();
		}
	}

	private void writeMapContents(PropertyDescriptor property, Map<?, ?> value) throws XMLStreamException {
		for (Iterator<? extends Entry<?, ?>> it = value.entrySet().iterator(); it.hasNext(); ) {
			Entry<?, ?> entry = it.next();
			ConfigurationItem entryValue = property.getConfigurationAccess().getConfig(entry.getValue());
			
			String elementTag = getElementTag(property, entryValue);
			writeStartElement(elementTag);
			writeContent(elementTag, property.getElementDescriptor(elementTag), entryValue);
			out.writeEndElement();
		}
	}

	protected void writeArray(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		Object value = item.value(property);

		ConfigurationValueProvider<?> format = property.getValueProvider();
		if (format != null && format.isLegalValue(value)) {
			// Was already serialized as attribute.
			return;
		}

		List<?> listValue = PropertyDescriptorImpl.arrayAsList(value);
		writeList(property, listValue);
	}

	protected void writeList(ConfigurationItem item, PropertyDescriptor property) throws XMLStreamException {
		List<?> value = (List<?>) item.value(property);

		ConfigurationValueProvider<?> format = property.getValueProvider();
		if (format != null && format.isLegalValue(value)) {
			// Was already serialized as attribute.
			return;
		}

		writeList(property, value);
	}

	/**
	 * Writes the list value for the given property.
	 * 
	 * @param property
	 *        A {@link PropertyDescriptor} of {@link PropertyDescriptor#kind() kind}
	 *        {@link PropertyKind#LIST}.
	 * @param value
	 *        A {@link List} value for the given property.
	 * 
	 * @see #writeMap(PropertyDescriptor, Map)
	 * @see #write(String, ConfigurationDescriptor, ConfigurationItem)
	 */
	public void writeList(PropertyDescriptor property, List<?> value) throws XMLStreamException {
		if (property.isDefaultContainer()) {
			writeCollectionContents(property, value);
		} else {
			writeStartElement(property.getPropertyName());
			writeCollectionContents(property, value);
			out.writeEndElement();
		}
	}

	private void writeCollectionContents(PropertyDescriptor property, List<?> value) throws XMLStreamException {
		for (int n = 0, cnt = value.size(); n < cnt; n++) {
			ConfigurationItem entry = property.getConfigurationAccess().getConfig(value.get(n));
			
			String elementTag = getElementTag(property, entry);
			
			writeStartElement(elementTag);
			writeContent(elementTag, property.getElementDescriptor(elementTag), entry);
			out.writeEndElement();
		}
	}

	private void writeStartElement(String elementTag) throws XMLStreamException {
		if (_ns != null) {
			out.writeStartElement(_ns, elementTag);
		} else {
			out.writeStartElement(elementTag);
		}
	}

	protected String getElementTag(PropertyDescriptor property, ConfigurationItem entry) {
		return property.getElementName(entry.descriptor());
	}

	@Override
	public void close() throws XMLStreamException {
		if (_transitiveClose) {
			out.close();
		}
	}
	
}
