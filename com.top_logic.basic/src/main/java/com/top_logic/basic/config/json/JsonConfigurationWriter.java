/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Arrays.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.basic.config.order.DefaultOrderStrategy;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.util.Utils;
import com.top_logic.common.json.adapt.WriterW;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Generic writer for {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JsonConfigurationWriter {
	
	private JsonWriter _out;

	/**
	 * Creates a {@link JsonConfigurationWriter}.
	 * 
	 * @param writer
	 *        The writer to write the generated JSON to.
	 */
	public JsonConfigurationWriter(Writer writer) {
		this(new JsonWriter(new WriterW(writer)));
	}

	/**
	 * Creates a new {@link JsonConfigurationWriter}.
	 */
	public JsonConfigurationWriter(JsonWriter jsonWriter) {
		_out = jsonWriter;
	}

	/**
	 * Pretty prints the output with indent "tab".
	 */
	public JsonConfigurationWriter prettyPrint() {
		return indent("\t");
	}

	/**
	 * Pretty prints the output with given indent.
	 * 
	 * @see #prettyPrint()
	 */
	public JsonConfigurationWriter indent(String value) {
		_out.setIndent(value);
		return this;
	}

	/**
	 * Writes the given {@link ConfigurationItem}.
	 */
	public void write(ConfigurationItem item) throws IOException {
		write(item.descriptor(), item);
	}

	/**
	 * Writes the given {@link ConfigurationItem} with context informations when the actually
	 * expected type would be the given static type.
	 */
	public void write(Class<? extends ConfigurationItem> staticType, ConfigurationItem item)
			throws IOException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(staticType);
		write(descriptor, item);
	}

	/**
	 * Writes the given {@link ConfigurationItem} with context informations when the actually
	 * expected type would be the given static type.
	 */
	public void write(ConfigurationDescriptor staticType, ConfigurationItem item) throws IOException {
		writeContent(staticType, item, Collections.emptySet());
	}

	/**
	 * Writes the given {@link ConfigurationItem} with context informations when the actually
	 * expected type would be the given static type.
	 * 
	 * @param staticType
	 *        The expected output type.
	 * @param item
	 *        The item to write.
	 * @param skippedProperties
	 *        Names of the properties which must not be written.
	 */
	protected void writeContent(ConfigurationDescriptor staticType, ConfigurationItem item,
			Set<String> skippedProperties) throws IOException {
		ConfigurationDescriptor descriptor = item.descriptor();
		
		final PropertyDescriptor implClassProperty;
		if (item instanceof PolymorphicConfiguration<?>) {
			implClassProperty = descriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		} else {
			implClassProperty = null;
		}
		// Check, whether the concrete configuration interface cannot be
		// reconstructed from from the content.
		Class<?> annotationClass;
		if (descriptor == staticType) {
			annotationClass = null;
		} else if (implClassProperty != null) {
			Class<?> implementationClass = (Class<?>) item.value(implClassProperty);
			if (implementationClass != null) {
				boolean staticPolymorph =
					PolymorphicConfiguration.class.isAssignableFrom(staticType.getConfigurationInterface());
				if (!staticPolymorph) {
					/* Assume the static type is not polymorph and a class is set. If no config
					 * interface is written, then the reader expects a simple ConfigurationItem
					 * which has no property class. */

					annotationClass = descriptor.getConfigurationInterface();
				} else {
					try {
						Class<?> expectedConfigurationInterface =
							DefaultConfigConstructorScheme.getFactory(implementationClass).getConfigurationInterface();
						if (!expectedConfigurationInterface.equals(descriptor.getConfigurationInterface())) {
							annotationClass = descriptor.getConfigurationInterface();
						} else {
							annotationClass = implementationClass;
						}
					} catch (ConfigurationException ex) {
						throw new IOException(
							"Cannot serialize configuration, invalid implementation class '"
								+ implementationClass.getName() + "'.",
							ex);
					}
				}
			} else {
				annotationClass = descriptor.getConfigurationInterface();
			}
		} else {
			annotationClass = descriptor.getConfigurationInterface();
		}
		
		if (annotationClass != null) {
			_out.beginArray();
			_out.value(annotationClass.getName());
		}
		_out.beginObject();
		
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

		if (implClassProperty != null && !implClassAnnotated(annotationClass)) {
			// The implementation class property must be written first to prevent it from being
			// serialized as sub-tag, if another property serialized before cannot be written as
			// attribute.
			writePlain(staticType, item, implClassProperty);
		}
		for (PropertyDescriptor property : properties) {
			if (skippedProperties.contains(property.getPropertyName())) {
				continue;
			}
			if (property == implClassProperty) {
				// Already written as first property.
				continue;
			}
			JsonValueBinding valueBinding = fetchValueBinding(property);
			if (valueBinding != null) {
				Object value = item.value(property);
				if (value != null) {
					writeName(property);
					valueBinding.saveConfigItem(property, _out, value);
					System.out.println();
				}
				continue;
			}

			switch (property.kind()) {
				case ITEM: {
					if (property.getValueProvider() != null) {
						writePlain(staticType, item, property);
					}
					break;
				}
				case COMPLEX:
				case PLAIN: {
					writePlain(staticType, item, property);
					break;
				}
				default:
					break;
			}
			writeNonPlainProperty(item, property, false);
		}
		
		_out.endObject();
		if (annotationClass != null) {
			_out.endArray();
		}
	}

	private boolean implClassAnnotated(Class<?> annotationClass) {
		return annotationClass != null && !ConfigurationItem.class.isAssignableFrom(annotationClass);
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
			throws IOException {
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

	/**
	 * Write the value for a property of kind {@link PropertyKind#COMPLEX}.
	 * 
	 * @param item
	 *        Holder whose property value must be written.
	 * @param property
	 *        Property of kind {@link PropertyKind#COMPLEX} whose value must be written.
	 * 
	 * @see #writeNonPlainProperty(ConfigurationItem, PropertyDescriptor, boolean)
	 */
	protected void writeComplex(ConfigurationItem item, PropertyDescriptor property) throws IOException {
		Object value = item.value(property);
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider != null) {
			if (valueProvider.isLegalValue(value)) {
				// Has been written as attribute using format.
				return;
			}
		}
		if (value != null) {
			// Type safety must be guaranteed at a higher level during
			// property descriptor creation. The unchecked conversion should be
			// moved to a location from which it is obvious that there might no
			// problem at runtime.
			JsonValueBinding jsonValueBinding = fetchValueBinding(property);
			if (jsonValueBinding == null) {
				throw new RuntimeException("No JsonBinding given.");
			}
			writeName(property);
			jsonValueBinding.saveConfigItem(property, _out, value);
		}
	}

	private JsonValueBinding fetchValueBinding(PropertyDescriptor property) throws ConfigurationError {
		JsonValueBinding valueBinding;
		JsonBinding annotation = property.getAnnotation(JsonBinding.class);
		if (annotation == null) {
			valueBinding = null;
		} else {
			try {
				valueBinding = ConfigUtil.getInstance(annotation.value());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
		return valueBinding;
	}

	private void writeName(PropertyDescriptor property) throws IOException {
		_out.name(property.getPropertyName());
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
	 */
	protected void writePlain(ConfigurationDescriptor staticType, ConfigurationItem item,
			PropertyDescriptor property) throws IOException {
		if (hasDefaultValue(staticType, item, property)) {
			return;
		}
		Object value = item.value(property);
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			if (property.kind() == PropertyKind.PLAIN) {
				throw new IOException("Plain property without format: " + property);
			}
			return;
		}
		if (!valueProvider.isLegalValue(value)) {
			if (property.kind() == PropertyKind.PLAIN) {
				throw new IOException("Plain property '" + property + "' with format '" + valueProvider
					+ "' that does not accept property value '" + value + "'.");
			}
			return;
		}
		String specification = valueProvider.getSpecification(value);
		
		writeName(property);
		if (value == null) {
			_out.nullValue();
		} else {
			Class<? extends Object> valueType = value.getClass();
			if (Boolean.class.isAssignableFrom(valueType)) {
				_out.value((Boolean) value);
			} else if (Number.class.isAssignableFrom(valueType)) {
				_out.value((Number) value);
			} else {
				_out.value(specification);
			}
		}
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

	/**
	 * Write the value for a property of kind {@link PropertyKind#ITEM}.
	 * 
	 * @param item
	 *        Holder whose property value must be written.
	 * @param property
	 *        Property of kind {@link PropertyKind#ITEM} whose value must be written.
	 * 
	 * @see #writeNonPlainProperty(ConfigurationItem, PropertyDescriptor, boolean)
	 */
	protected void writeItem(ConfigurationItem item, PropertyDescriptor property) throws IOException {
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
			writeName(property);
			_out.nullValue();
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

	private void writeItemTag(PropertyDescriptor property, ConfigurationItem valueConfig) throws IOException {
		writeName(property);
		writeContent(property.getDefaultDescriptor(), valueConfig, Collections.emptySet());
	}

	/**
	 * Write the value for a property of kind {@link PropertyKind#MAP}.
	 * 
	 * @param item
	 *        Holder whose property value must be written.
	 * @param property
	 *        Property of kind {@link PropertyKind#MAP} whose value must be written.
	 * 
	 * @see #writeNonPlainProperty(ConfigurationItem, PropertyDescriptor, boolean)
	 */
	protected void writeMap(ConfigurationItem item, PropertyDescriptor property) throws IOException {
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
	 * @see #write(ConfigurationDescriptor, ConfigurationItem)
	 */
	public void writeMap(PropertyDescriptor property, Map<?, ?> value) throws IOException {
		if (property.isDefaultContainer()) {
			writeMapContents(property, value);
		} else {
			writeName(property);
			_out.beginObject();
			writeMapContents(property, value);
			_out.endObject();
		}
	}

	private void writeMapContents(PropertyDescriptor property, Map<?, ?> value) throws IOException {
		PropertyDescriptor keyProperty = property.getKeyProperty();
		if (keyProperty == null) {
			throw new IllegalArgumentException("Property '" + property + "' has no key property.");
		}
		ConfigurationValueProvider keyValueProvider = keyProperty.getValueProvider();
		ConfigurationDescriptor staticType = property.getDefaultDescriptor();
		for (Iterator<? extends Entry<?, ?>> it = value.entrySet().iterator(); it.hasNext(); ) {
			Entry<?, ?> entry = it.next();
			ConfigurationItem entryValue = property.getConfigurationAccess().getConfig(entry.getValue());
			
			_out.name(keyValueProvider.getSpecification(entry.getKey()));
			writeContent(staticType, entryValue, Collections.singleton(keyProperty.getPropertyName()));
		}
	}

	/**
	 * Write the value for a property of kind {@link PropertyKind#ARRAY}.
	 * 
	 * @param item
	 *        Holder whose property value must be written.
	 * @param property
	 *        Property of kind {@link PropertyKind#ARRAY} whose value must be written.
	 * 
	 * @see #writeNonPlainProperty(ConfigurationItem, PropertyDescriptor, boolean)
	 */
	protected void writeArray(ConfigurationItem item, PropertyDescriptor property) throws IOException {
		List<?> value = PropertyDescriptorImpl.arrayAsList(item.value(property));
		writeList(property, value);
	}

	/**
	 * Write the value for a property of kind {@link PropertyKind#LIST}.
	 * 
	 * @param item
	 *        Holder whose property value must be written.
	 * @param property
	 *        Property of kind {@link PropertyKind#LIST} whose value must be written.
	 * 
	 * @see #writeNonPlainProperty(ConfigurationItem, PropertyDescriptor, boolean)
	 */
	protected void writeList(ConfigurationItem item, PropertyDescriptor property) throws IOException {
		List<?> value = (List<?>) item.value(property);
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
	 * @see #write(ConfigurationDescriptor, ConfigurationItem)
	 */
	public void writeList(PropertyDescriptor property, List<?> value) throws IOException {
		if (property.isDefaultContainer()) {
			writeCollectionContents(property, value);
		} else {
			writeName(property);
			_out.beginArray();
			writeCollectionContents(property, value);
			_out.endArray();
		}
	}

	private void writeCollectionContents(PropertyDescriptor property, List<?> value) throws IOException {
		ConfigurationDescriptor staticType = property.getDefaultDescriptor();
		for (int n = 0, cnt = value.size(); n < cnt; n++) {
			ConfigurationItem entry = property.getConfigurationAccess().getConfig(value.get(n));
			
			writeContent(staticType, entry, Collections.emptySet());
		}
	}
	
}
