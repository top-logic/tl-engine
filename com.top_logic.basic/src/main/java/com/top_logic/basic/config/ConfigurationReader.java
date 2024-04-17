/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.vars.IVariableExpander;
import com.top_logic.basic.xml.AliasedXMLReader;
import com.top_logic.basic.xml.LogWithStreamLocation;
import com.top_logic.basic.xml.XMLContent;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Reader instantiating {@link ConfigurationItem}s from XML definition files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationReader extends AbstractConfigurationReader {

	static final Object INVALID_VALUE = new Object();

	/**
	 * Attribute containing the name of the file that is currently read.
	 */
	public static final String DEFINITION_FILE_ANNOTATION_ATTR = "definition-file";

	/** value used to split the annotated source files. */
	public static final String DEFINITION_FILES_SEPARATOR = ",";

	public static final String ANNOTATION_NS = "http://www.top-logic.com/ns/layout/annotation/1.0";

	/**
	 * Parser of {@link ConfigurationItem}s from {@link XMLStreamReader}s.
	 */
	public static class Handler {

		private static final Object NO_KEY = new Object();

		private final Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName;
		private final InstantiationContext context;
		
		/**
		 * Stack of tuples (current file location including include information read from XML
		 * annotations, the last include information read from XML annotations prepended to all
		 * previously read include informations). If
		 * {@link ConfigurationReader#DEFINITION_FILE_ANNOTATION_ATTR} is read, two new entries are
		 * added to this stack. The first one (at peek-position 1) is the new concatenated list of
		 * definition annotations read so far. The second one (at peek-position 0) is the new
		 * location information constructed from the XML reader's system ID and the definition chain
		 * added before. This information is constructed once to prevent multiple identical string
		 * concatenations otherwise stored in the location information added to the resulting config
		 * items.
		 */
		private Stack<String> _definitions = new ArrayStack<>();

		public Handler(InstantiationContext context, Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName) {
			this.context = context;
			this.globalDescriptorsByLocalName = globalDescriptorsByLocalName;
		}
		
		public ConfigurationItem parse(XMLStreamReader reader, ConfigurationItem baseConfig) {
//			int documentEvent = reader.next();
//			if (documentEvent != XMLStreamConstants.START_DOCUMENT) {
//				throw new XMLStreamException("Start of document expected, but got event '" + documentEvent + "' at " + location(reader));
//			}
			
			int event;
			try {
				event = reader.nextTag();
			} catch (XMLStreamException ex) {
				errorReadingFailed(reader, ex);
				return null;
			}
			if (event != XMLStreamConstants.START_ELEMENT) {
				context.error("Configuration root element expected, but got event '" + event + "' " + atLocation(reader));
				return null;
			}
			
			ConfigurationItem result = parseContents(reader, baseConfig);

			if (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
				context.error("End of document expected, but got event '" + event + "' at " + atLocation(reader));
				return null;
			}
			ensureEndOfFile(reader);
			return result;
		}

		/**
		 * Expects the reader to be located at the closing root tag.
		 */
		private void ensureEndOfFile(XMLStreamReader reader) {
			try {
				reader.next();
				XMLStreamUtil.skipWhitespaceAndComments(reader);
				int eventType = reader.getEventType();
				if (eventType == XMLStreamConstants.START_ELEMENT) {
					context.error("Document has multiple root tags. '"
						+ XMLStreamUtil.getEventName(eventType) + "' " + atLocation(reader));
					return;
				}
				if (eventType != XMLStreamConstants.END_DOCUMENT) {
					context.error("Expected the end of the document, but gut event '"
						+ XMLStreamUtil.getEventName(eventType) + "' " + atLocation(reader));
				}
			} catch (XMLStreamException ex) {
				errorReadingFailed(reader, ex);
			}
		}

		private void errorReadingFailed(XMLStreamReader reader, Throwable error) {
			String message = "Reading configuration failed " + atLocation(reader) + ". Cause: " + error.getMessage();
			context.error(message, error);
		}

		public ConfigurationItem parseContents(XMLStreamReader reader, ConfigurationItem baseConfig) {
			_definitions.push(null);
			_definitions.push(reader.getLocation().getSystemId());
			try {
				ConfigurationDescriptor rootDescriptor;
				if (baseConfig != null) {
					return readConfigurationItem(reader, baseConfig, null);
				} else {
					String localName = reader.getLocalName();
					rootDescriptor = readConfigInterfaceAsDescriptor(reader);
					if (rootDescriptor == null) {
						rootDescriptor = this.globalDescriptorsByLocalName.get(localName);
					}
					if (rootDescriptor == null) {
						context.error("Unexpected element '" + localName + "' " + atLocation(reader)
							+ ", expected one of '" + globalDescriptorsByLocalName.keySet() + "'.");
						return null;
					}
					return readConfigurationItem(reader, rootDescriptor, rootDescriptor, null);
				}
			} catch (ConfigurationException ex) {
				errorReadingFailed(reader, ex);
				return null;
			} catch (XMLStreamException ex) {
				errorReadingFailed(reader, ex);
				return null;
			}
		}

		<T> T parseValue(XMLStreamReader reader, final ConfigurationValueProvider<T> valueProvider,
				PropertyDescriptor property, CharSequence value) throws ConfigurationException {
			final T reference;
			try {
				reference = valueProvider.getValue(property.getPropertyName(), value);
			} catch (Throwable ex) {
				throw new ConfigurationException(messageInvalidValue(reader, property, value, ex), ex);
			}
			return reference;
		}

		private String atLocation(XMLStreamReader reader) {
			String location = XMLStreamUtil.atLocation(reader);
			if (!_definitions.isEmpty()) {
				String definition = _definitions.peek(1);
				if (definition != null) {
					location += " (Definition: " + definition + ")";
				}
			}
			return location;
		}

		private ConfigurationItem readNextElement(ConfigBuilder valueBuilder, XMLStreamReader reader,
				ConfigurationItem baseConfig, PropertyDescriptor owningProperty)
				throws XMLStreamException {
			String annotatedDefinition = reader.getAttributeValue(ANNOTATION_NS, DEFINITION_FILE_ANNOTATION_ATTR);
			if (StringServices.isEmpty(annotatedDefinition)) {
				return readContents(valueBuilder, reader, baseConfig, owningProperty);
			} else {
				String lastDefinition = _definitions.peek(1);

				String definition;
				if (lastDefinition == null) {
					definition = annotatedDefinition;
				} else {
					definition = lastDefinition + DEFINITION_FILES_SEPARATOR + annotatedDefinition;
				}
				_definitions.push(definition);
				_definitions.push(reader.getLocation().getSystemId() + "(" + definition + ")");
				try {
					return readContents(valueBuilder, reader, baseConfig, owningProperty);
				} finally {
					_definitions.pop();
					_definitions.pop();
				}
			}
		}

		private ConfigurationItem readContents(ConfigBuilder valueBuilder, XMLStreamReader reader,
				ConfigurationItem baseConfig, PropertyDescriptor owningProperty) throws XMLStreamException {
			initLocation(valueBuilder, reader);

			ConfigurationDescriptor descriptor = valueBuilder.descriptor();
			
			if (isOverride(reader)) {
				baseConfig = null;
			}
			boolean attributeRead = false;
			Set<String> processedProperties = new HashSet<>();
			// Read attribute properties.
			for (int n = 0, cnt = reader.getAttributeCount(); n < cnt; n++) {
				if (XMLStreamUtil.getAttributeNamespace(reader, n) != null) {
					continue;
				}
				
				attributeRead = true;

				String configName = reader.getAttributeLocalName(n);
				String configValue = reader.getAttributeValue(n);
				PropertyDescriptor property = descriptor.getProperty(configName);
				if (property == null) {
					errorNoSuchProperty(reader, descriptor, configName);
					continue;
				}
				
				Object value;
				try {
					resolveValue:
					switch (property.kind()) {
						case ITEM: {
							ConfigurationValueProvider<?> format = property.getValueProvider();
							if (format != null) {
								value = parseValue(reader, format, property, configValue);
								break;
							}

							ConfigurationDescriptor expectedDescriptor = property.getValueDescriptor();
							if (expectedDescriptor == null) {
								throw new ConfigurationException("Property '" + property
									+ "' without configuration descriptor " + atLocation(reader)
									+ ". The property must potentially be annotated @InstanceFormat.");
							}
							if (!isPolymorphicConfiguration(reader, expectedDescriptor)) {
								throw new ConfigurationException("Property '" + property
									+ "' is an item property which must be defined in separate tag "
									+ atLocation(reader) + ".");
							}
							Class<?> implementationClass =
								getConfiguredImplementationClass(reader, expectedDescriptor, configValue);
							if (implementationClass == null) {
								// Allow overriding inline item configuration by setting
								// implementation class to null.
								value = null;
								break resolveValue;
							}
							ConfigBuilder newConfig =
								createBuilderForImplementationClass(reader, expectedDescriptor, implementationClass);
							ConfigurationItem contentBaseConfig =
								getBaseConfigurationForProperty(reader, property, baseConfig);
							if (contentBaseConfig != null) {
								moveValues(contentBaseConfig, newConfig);
							}
							value = newConfig;
							break;
						}
						case DERIVED:
							errorDerived(reader, property);
							continue;
						default: {
							ConfigurationValueProvider<?> format = property.getValueProvider();
							if (format != null) {
								value = parseValue(reader, format, property, configValue);
								break;
							}

							switch (property.kind()) {
								case ARRAY: {
									Class<?> instanceType = property.getInstanceType();
									if (instanceType != null) {
										List<Object> values =
											readCollectionValues(configName, configValue, property, instanceType);
										value = listAsGenericArray(property, values);
									} else {
										errorNoFormat(reader, property);
										continue;
									}
									break;
								}

								case LIST: {
									Class<?> instanceType = property.getInstanceType();
									if (instanceType != null) {
										value = readCollectionValues(configName, configValue, property, instanceType);
									} else {
										errorNoFormat(reader, property);
										continue;
									}
									break;
								}

								default: {
									errorNoFormat(reader, property);
									continue;
								}
							}
						}
					}
					if (PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME.equals(property.getPropertyName())
						&& isPolymorphicConfiguration(reader, descriptor)) {
						if (value == null) {
							// Setting a class attribute to "" means setting the whole property to
							// null.
							XMLStreamUtil.skipToMatchingEndTag(reader);
							return null;
						}
						// class attribute may be read before
						valueBuilder.update(property, value);
					} else {
						initValue(reader, valueBuilder, property, value, processedProperties);
					}
				} catch (ConfigurationException ex) {
					errorInvalidAttributeConfiguration(reader, property, ex, configValue);
					continue;
				} catch (RuntimeException ex) {
					errorInvalidAttributeConfiguration(reader, property, ex, configValue);
					continue;
				}
			}
			
			// Read element properties.
			ListHandler listHandler = null;
			Set<Object> listKeys = null;
			Map<Object, Object> mapHandler = null;
			Set<Object> mapKeys = null;
			int contentEvent;
			readContent:
			while ((contentEvent = reader.next()) != XMLStreamConstants.END_ELEMENT) {
				StringBuilder buffer = null;

				formattedContent:
				while (true) {
					switch (contentEvent) {
						case XMLStreamConstants.START_ELEMENT:
							// Read regular element property content.
							if (buffer != null) {
								StringBuilder error = new StringBuilder();
								error.append("Must not mix formatted content with element content ");
								appendAtLocation(error, reader);
								error.append(".");
								context.error(error.toString());
							}
							break formattedContent;
							
						case XMLStreamConstants.END_ELEMENT:
							// Finished reading formatted value.
							if (buffer != null) {
								if (attributeRead) {
									StringBuilder error = new StringBuilder();
									error.append("Must not mix formatted content with attribute content ");
									appendAtLocation(error, reader);
									error.append(".");
									context.error(error.toString());
								} else {
									if (owningProperty == null) {
										StringBuilder error = new StringBuilder();
										error.append(
											"Formatted value in top-level configuration not supported ");
										appendAtLocation(error, reader);
										error.append(".");
										context.error(error.toString());
									} else {
										ConfigurationValueProvider format = owningProperty.getValueProvider();
										if (format == null) {
											StringBuilder error = new StringBuilder();
											error.append(
												"Property '" + owningProperty.getPropertyName()
													+ "' has no format and must therefore not be configured with a plain value '"
													+ buffer.toString() + "' ");
											appendAtLocation(error, reader);
											error.append(".");
											context.error(error.toString());
										} else {
											try {
												Object value = parseValue(reader, format, owningProperty, buffer);
												return (ConfigurationItem) value;
											} catch (ConfigurationException ex) {
												StringBuilder error = new StringBuilder();
												error.append(
													"For property '" + owningProperty.getPropertyName() + "' value '"
														+ buffer.toString() + "' with invalid format ("
														+ ex.getMessage()
														+ ") was used ");
												appendAtLocation(error, reader);
												error.append(".");
												context.error(error.toString(), ex);
											}
										}
									}
								}
							}
							break readContent;

						case XMLStreamConstants.SPACE:
						case XMLStreamConstants.CHARACTERS:
						case XMLStreamConstants.CDATA:
							if (buffer == null) {
								if (reader.isWhiteSpace()) {
									break;
								}
								buffer = new StringBuilder(reader.getText());
							} else {
								buffer.append(reader.getText());
							}
							break;

						case XMLStreamConstants.COMMENT:
							// Skip.
							break;

						case XMLStreamConstants.END_DOCUMENT: {
							StringBuilder error = new StringBuilder();
							error.append("Unexpected end of document ");
							appendAtLocation(error, reader);
							error.append(".");
							context.error(error.toString());
							
							// Do not try to fetch the next token.
							return valueBuilder;
						}

						default: {
							StringBuilder error = new StringBuilder();
							error.append("Unexpected event ");
							error.append(contentEvent);
							error.append(" ");
							appendAtLocation(error, reader);
							error.append(".");
							context.error(error.toString());
							break;
						}
					}

					contentEvent = reader.next();
				}

				String localName = reader.getLocalName();
				PropertyDescriptor property = descriptor.getProperty(localName);
				if (property == null) {
					PropertyDescriptor defaultContainer = descriptor.getDefaultContainer();
					if (defaultContainer != null) {
						ConfigurationDescriptor contentDescriptor = defaultContainer.getElementDescriptor(localName);
						if (contentDescriptor != null) {
							switch (defaultContainer.kind()) {
								case ITEM: {
									Object value =
										readItemConfig(reader, baseConfig, defaultContainer, contentDescriptor,
											contentDescriptor);
									initValue(reader, valueBuilder, defaultContainer, value, processedProperties);
									break;
								}
								case ARRAY:
								case LIST: {
									if (listHandler == null) {
										listHandler = createListHandler(baseConfig, defaultContainer, false);
										listKeys = new HashSet<>();
									}
									readListEntry(reader, defaultContainer, defaultContainer.getKeyProperty(),
										listHandler, listKeys);
									break;
								}
								case MAP: {
									if (mapHandler == null) {
										mapHandler = createMapHandler(baseConfig, defaultContainer, false);
										mapKeys = new HashSet<>();
									}
									readMapEntry(reader, defaultContainer, defaultContainer.getKeyProperty(),
										mapHandler, mapKeys);
									break;
								}
								default: {
									throw new UnreachableAssertion("Default container property '" + defaultContainer
										+ "' cannot be of type: " + defaultContainer.kind());
								}
							}
							continue;
						}
					}

					errorNoSuchProperty(reader, descriptor, localName);

					contentEvent = XMLStreamUtil.skipUpToMatchingEndTag(reader);
					continue;
				}
				
				Object value = readElementValue(property, reader, baseConfig);
				if (value != INVALID_VALUE) {
					initValue(reader, valueBuilder, property, value, processedProperties);
				}
			}
			
			if (listHandler != null) {
				PropertyDescriptor containerProperty = descriptor.getDefaultContainer();
				Object containerList;
				if (containerProperty.kind() == PropertyKind.ARRAY) {
					containerList = listAsGenericArray(containerProperty, listHandler.toList());
				} else {
					containerList = listHandler.toList();
				}
				initValue(reader, valueBuilder, containerProperty, containerList,
					processedProperties);
			} else if (mapHandler != null) {
				initValue(reader, valueBuilder, descriptor.getDefaultContainer(), mapHandler, processedProperties);
			}

			if (contentEvent != XMLStreamConstants.END_ELEMENT) {
				throw new XMLStreamException(
					"End element expected but got event '" + contentEvent + "' " + atLocation(reader));
			}

			return valueBuilder;
		}

		private void errorNoSuchProperty(XMLStreamReader reader, ConfigurationDescriptor descriptor,
				String propertyName) {
			StringBuilder error = new StringBuilder();
			error.append("Configuration descriptor '");
			appendConfigInterface(error, descriptor);
			error.append("' has no property '");
			error.append(propertyName);
			error.append("', valid properties are: ");
			error.append(Arrays.asList(descriptor.getPropertiesOrdered()).stream().map(p -> p.getPropertyName())
				.collect(Collectors.toList()));
			appendAtLocation(error, reader);
			context.error(error.toString());
		}

		private void appendConfigInterface(StringBuilder error, ConfigurationDescriptor descriptor) {
			error.append(descriptor.getConfigurationInterface().getName());
		}

		private List<Object> readCollectionValues(String configName, String configValue, PropertyDescriptor property,
				Class<?> instanceType) throws ConfigurationException {
			List<String> classNames = StringServices.toList(configValue, ',');
			List<Object> configurationsOrInstances =
				new ArrayList<>(classNames.size());
			for (String className : classNames) {
				Class<?> instanceClass = ConfigUtil.getClassForNameMandatory(instanceType, configName, className);
				Factory factory = DefaultConfigConstructorScheme.getFactory(instanceClass);
				if (property.isInstanceValued()) {
					Object instance = factory.createDefaultInstance(context);
					if (instance != null) {
						configurationsOrInstances.add(instance);
					}
				} else {
					@SuppressWarnings("unchecked")
					Class<? extends PolymorphicConfiguration<Object>> configurationType =
						(Class<? extends PolymorphicConfiguration<Object>>) factory
							.getConfigurationInterface();
					PolymorphicConfiguration<Object> configuration =
						TypedConfiguration.newConfigItem(configurationType);
					configuration.setImplementationClass(instanceClass);
					configurationsOrInstances.add(configuration);
				}
			}
			return configurationsOrInstances;
		}

		private void errorNoFormat(XMLStreamReader reader, PropertyDescriptor property) {
			StringBuilder error = new StringBuilder();
			error.append("Inline configuration ");
			appendForProperty(error, property);
			error.append(" without specified format.");
			context.error(error.toString());
		}

		private boolean initValue(XMLStreamReader reader, ConfigBuilder valueBuilder, PropertyDescriptor property,
				Object value, Set<String> processedProperties) {
			if (processedProperties.contains(property.getPropertyName())) {
				StringBuilder error = new StringBuilder();
				error.append("Duplicate initialisation value '");
				error.append(value);
				error.append("'");
				appendForProperty(error, property);
				appendAtLocation(error, reader);
				error.append(".");
				context.error(error.toString());
				return false;
			}
			processedProperties.add(property.getPropertyName());
			try {
				valueBuilder.update(property, value);
				return true;
			} catch (IllegalArgumentException ex) {
				StringBuilder error = new StringBuilder();
				error.append("Illegal value '");
				error.append(value);
				error.append("'");
				appendForProperty(error, property);
				appendAtLocation(error, reader);
				error.append(".");
				context.error(error.toString(), ex);
				return false;
			}
		}

		private Object readElementValue(PropertyDescriptor property, XMLStreamReader reader,
				ConfigurationItem baseConfig) throws XMLStreamException {
			switch (property.kind()) {
			case PLAIN: {
				checkNoAttributes(property, reader);
				
				// Simple property configured as element instead of attribute value.
				ConfigurationValueProvider valueProvider = property.getValueProvider();
				
				String valueSpec = reader.getElementText();
					try {
						return valueProvider.getValue(property.getPropertyName(), valueSpec);
					} catch (ConfigurationException ex) {
						errorInvalidPlainValue(reader, property, ex, valueSpec);
						return INVALID_VALUE;
					} catch (RuntimeException ex) {
						errorInvalidPlainValue(reader, property, ex, valueSpec);
						return INVALID_VALUE;
					}
			}
			case ITEM: {
					return readItemConfig(reader, baseConfig, property, property.getValueDescriptor(),
						property.getDefaultDescriptor());
			}
			case ARRAY: {
					List<?> oldListValue =
						PropertyDescriptorImpl.arrayAsList(valueToAdapt(isOverride(reader), property, baseConfig));
					return handleCollection(property, reader, oldListValue, true);
			}
			case LIST: {
				List<?> oldListValue = valueToAdapt(isOverride(reader), property, baseConfig);
				return handleCollection(property, reader, oldListValue, false);
			}
			case MAP: {
				return handleMap(property, reader, baseConfig);
			}
			case COMPLEX: {
					@SuppressWarnings("unchecked")
					ConfigurationValueBinding<Object> valueBinding = property.getValueBinding();
				
					Object baseValue = valueToAdapt(isOverride(reader), property, baseConfig);
					try {
						return valueBinding.loadConfigItem(reader, baseValue);
					} catch (ConfigurationException ex) {
						throw errorInvalidComplexValue(reader, property, ex);
					} catch (RuntimeException ex) {
						throw errorInvalidComplexValue(reader, property, ex);
					}
				}
			case DERIVED: {
					errorDerived(reader, property);
					return INVALID_VALUE;
			}
			case REF: {
					StringBuilder error = new StringBuilder();
					error.append("Reading property ");
					appendForProperty(error, property);
					error.append(" of kind '");
					error.append(PropertyKind.REF);
					error.append("' is not supported ");
					appendAtLocation(error, reader);
					error.append(".");
					throw new UnsupportedOperationException(error.toString());
			}
			default: {
				throw new UnreachableAssertion("No such property kind: " + property.kind());
			}
			}
		}

		private Object handleCollection(PropertyDescriptor property, XMLStreamReader reader, List<?> oldListValue,
				boolean isArray)
				throws XMLStreamException {
			checkOnlySizeAttribute(property, reader);
		
			PropertyDescriptor listKeyProperty = property.getKeyProperty();
			ListHandler handler = ListHandler.createListHandler(context, property, oldListValue);
			Set<Object> listKeys = new HashSet<>();
		
			return handlePotentialTextContent(property, reader, new StructureHandler() {
				@Override
				public void readEntry(XMLStreamReader entryReader, PropertyDescriptor entryProperty)
						throws XMLStreamException {
					readListEntry(entryReader, entryProperty, listKeyProperty, handler, listKeys);
				}
		
				@Override
				public Object getResult() {
					return isArray ? listAsGenericArray(property, handler.toList()) : handler.toList();
				}
			});
		}

		private Object handleMap(PropertyDescriptor property, XMLStreamReader reader, ConfigurationItem baseConfig)
				throws XMLStreamException {
			checkOnlySizeAttribute(property, reader);

			Map<Object, Object> mapValue = createMapHandler(baseConfig, property, isOverride(reader));
			Set<Object> mapKeys = new HashSet<>();
			PropertyDescriptor mapKeyProperty = property.getKeyProperty();

			return handlePotentialTextContent(property, reader, new StructureHandler() {
				@Override
				public void readEntry(XMLStreamReader entryReader, PropertyDescriptor entryProperty)
						throws XMLStreamException {
					readMapEntry(entryReader, entryProperty, mapKeyProperty, mapValue, mapKeys);
				}

				@Override
				public Object getResult() {
					return mapValue;
				}
			});
		}

		/**
		 * Callback for XML structure.
		 */
		private interface StructureHandler {

			/**
			 * Reads a tag.
			 */
			void readEntry(XMLStreamReader entryReader, PropertyDescriptor entryProperty) throws XMLStreamException;

			/**
			 * Produces result of all read elements.
			 */
			Object getResult();

		}

		private Object handlePotentialTextContent(PropertyDescriptor property, XMLStreamReader reader,
				StructureHandler handler) throws XMLStreamException {
			int entryEvent;
			boolean entry = false;
			StringBuilder buffer = null;
			while (true) {
				entryEvent = reader.next();
				switch (entryEvent) {
					case XMLStreamConstants.SPACE:
					case XMLStreamConstants.PROCESSING_INSTRUCTION:
					case XMLStreamConstants.COMMENT:
						// Ignore.
						break;

					case XMLStreamConstants.CHARACTERS:
						if (buffer == null && onlyWhiteSpace(reader)) {
							break;
						}
						//$FALL-THROUGH$
					case XMLStreamConstants.CDATA:
						if (entry) {
							throw errorNoMixedContent(property, reader);
						}
						if (buffer == null) {
							buffer = new StringBuilder();
						}
						buffer.append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
						break;

					case XMLStreamConstants.START_ELEMENT:
						if (buffer != null) {
							throw errorNoMixedContent(property, reader);
						}
						entry = true;
						handler.readEntry(reader, property);
						break;

					case XMLStreamConstants.END_ELEMENT:
						if (buffer != null) {
							@SuppressWarnings("rawtypes")
							ConfigurationValueProvider format = property.getValueProvider();
							try {
								Object value = format.getValue(property.getPropertyName(), buffer);
								return value;
							} catch (ConfigurationException ex) {
								throw new XMLStreamException(
									"Unable to parse formatted list value of property '" + property.getPropertyName()
										+ "' " + atLocation(reader),
									ex);
							}
						}
						return handler.getResult();

					default:
						throw new XMLStreamException(
							"End element expected but got event '" + entryEvent + "' " + atLocation(reader));
				}
			}
		}

		private XMLStreamException errorNoMixedContent(PropertyDescriptor property, XMLStreamReader reader)
				throws XMLStreamException {
			throw new XMLStreamException(
				"Must not mix text and element content within list property '" +
					property.getPropertyName() + "' " + atLocation(reader));
		}

		private static boolean onlyWhiteSpace(XMLStreamReader reader) {
			char[] buffer = reader.getTextCharacters();
			for (int n = reader.getTextStart(), stop = n + reader.getTextLength(); n < stop; n++) {
				if (!Character.isWhitespace(buffer[n])) {
					return false;
				}
			}
			return true;
		}

		private void errorDerived(XMLStreamReader reader, PropertyDescriptor property) {
			StringBuilder error = new StringBuilder();
			error.append("Derived property ");
			appendProperty(error, property);
			error.append(" cannot be configured");
			appendAtLocation(error, reader);
			context.error(error.toString());
		}

		private Object readItemConfig(XMLStreamReader reader, ConfigurationItem baseConfig, PropertyDescriptor property,
				ConfigurationDescriptor elementDescriptor, ConfigurationDescriptor defaultDescriptor)
				throws XMLStreamException {
			final ConfigurationItem contentBaseConfig = getBaseConfigurationForProperty(reader, property, baseConfig);
			try {
				return readItemConfig(reader, property, elementDescriptor, defaultDescriptor, contentBaseConfig);
			} catch (ConfigurationException ex) {
				errorInvalidItemConfig(reader, property, ex);
				return INVALID_VALUE;
			} catch (RuntimeException ex) {
				errorInvalidItemConfig(reader, property, ex);
				return INVALID_VALUE;
			}
		}

		private Map<Object, Object> createMapHandler(ConfigurationItem baseConfig, PropertyDescriptor property,
				boolean override) {
			Map<Object, Object> oldMapValue = valueToAdapt(override, property, baseConfig);

			Map<Object, Object> mapValue;
			if (oldMapValue == null) {
				mapValue = new LinkedHashMap<>();
			} else {
				mapValue = new LinkedHashMap<>(oldMapValue);
			}
			return mapValue;
		}

		private ListHandler createListHandler(ConfigurationItem baseConfig, PropertyDescriptor property,
				boolean override) {
			Object oldValue = valueToAdapt(override, property, baseConfig);
			List<?> oldListValue;
			if (property.kind() == PropertyKind.ARRAY) {
				oldListValue = PropertyDescriptorImpl.arrayAsList(oldValue);
			} else {
				oldListValue = (List<?>) oldValue;
			}
			ListHandler handler = ListHandler.createListHandler(context, property, oldListValue);
			return handler;
		}

		private void readMapEntry(XMLStreamReader reader, PropertyDescriptor property,
				PropertyDescriptor mapKeyProperty, Map<Object, Object> mapValue, Set<Object> mapKeys)
				throws XMLStreamException {
			try {
				ConfigurationDescriptor elementDescriptor = readElementDescriptor(property, reader);
				PropertyDescriptor keyProperty =
					elementDescriptor.getProperty(mapKeyProperty.getPropertyName());
				final MapOperation mapOperation = getMapOperation(reader);
				Object mapKey;
				switch (mapOperation) {
					case ADD_OR_UPDATE: {
						Object reference = getKeyProperty(reader, keyProperty, elementDescriptor);
						final Object oldValue = mapValue.get(reference);
						if (oldValue == null) {
							mapKey = handleMapAdd(reader, elementDescriptor, mapValue, keyProperty, property);
						} else {
							mapKey = handleMapUpdate(reader, elementDescriptor, mapValue, keyProperty, property);
						}
						break;
					}
					case ADD: {
						mapKey = handleMapAdd(reader, elementDescriptor, mapValue, keyProperty, property);
						break;
					}
					case REMOVE: {
						mapKey = readKeyProperty(reader, keyProperty, elementDescriptor);
						mapValue.remove(mapKey);

						/* May removed to insert at a different position */
						checkDuplicateKey(reader, mapKeys, keyProperty, mapKey);
						mapKeys.remove(mapKey);
						mapKey = NO_KEY;
								
						// Skip section
						XMLStreamUtil.skipUpToMatchingEndTag(reader);
						break;
					}
					case UPDATE: {
						mapKey = handleMapUpdate(reader, elementDescriptor, mapValue, keyProperty, property);
						break;
					}
					default: {
						mapKey = NO_KEY;
						assert false : "Unknown map operation: " + mapOperation;
					}
				}
				checkDuplicateKey(reader, mapKeys, keyProperty, mapKey);
			} catch (ConfigurationException ex) {
				errorInvalidMapEntry(reader, property, ex);
			} catch (RuntimeException ex) {
				errorInvalidMapEntry(reader, property, ex);
			}
		}

		private void checkDuplicateKey(XMLStreamReader reader, Set<Object> allKeys, PropertyDescriptor keyProperty,
				Object key) {
			if (key == NO_KEY) {
				return;
			}
			boolean newKey = allKeys.add(key);
			if (newKey) {
				// new key, no problem
				return;
			}
			errorDuplicateKey(reader, keyProperty, key);
		}

		private void readListEntry(XMLStreamReader reader, PropertyDescriptor property,
				PropertyDescriptor declaredKeyProperty, ListHandler handler, Set<Object> listKeys)
				throws XMLStreamException {
			try {
				ConfigurationDescriptor elementDescriptor = readElementDescriptor(property, reader);
				PropertyDescriptor keyProperty;
				if (declaredKeyProperty != null) {
					keyProperty = elementDescriptor.getProperty(declaredKeyProperty.getPropertyName());
				} else {
					keyProperty = null;
				}
				ListOperation listOperation = getListOperation(reader);
				Object listKey;
				switch (listOperation) {
					case ADD_OR_UPDATE: {
						if (keyProperty == null) {
							listKey = handleListAdd(reader, elementDescriptor, keyProperty, property, handler);
							break;
						}
						Object keyPropValue = getKeyProperty(reader, keyProperty, elementDescriptor);
						final ConfigurationItem configToUpdate = handler.resolveReferenceOrNull(keyPropValue);
						if (configToUpdate != null) {
							handleListUpdate(reader, elementDescriptor, keyProperty, property, handler, configToUpdate);
							listKey = keyPropValue;
						} else {
							listKey = handleListAdd(reader, elementDescriptor, keyProperty, property, handler);
						}
						break;
					}
					case ADD: {
						listKey = handleListAdd(reader, elementDescriptor, keyProperty, property, handler);
						break;
					}
					case REMOVE: {
						listKey = readKeyProperty(reader, keyProperty, elementDescriptor);
						handler.remove(listKey);

						/* May removed to insert at a different position */
						checkDuplicateKey(reader, listKeys, keyProperty, listKey);
						listKeys.remove(listKey);
						listKey = NO_KEY;

						// skip section
						XMLStreamUtil.skipUpToMatchingEndTag(reader);
						break;
					}
					case UPDATE: {
						listKey = handleListUpdate(reader, elementDescriptor, keyProperty, property, handler);
						break;
					}
					default: {
						listKey = NO_KEY;
						assert false : "Unknown " + ListOperation.class.getName() + ": " + listOperation;
					}
				}
				checkDuplicateKey(reader, listKeys, keyProperty, listKey);
			} catch (ConfigurationException ex) {
				errorInvalidListEntry(reader, property, ex);
			} catch (RuntimeException ex) {
				errorInvalidListEntry(reader, property, ex);
			}
		}

		private void appendAtLocation(StringBuilder out, XMLStreamReader reader) {
			appendSpace(out);
			out.append(atLocation(reader));
		}

		private void appendSpace(StringBuilder out) {
			removeDot(out);
			if (out.charAt(out.length() - 1) != ' ') {
				out.append(' ');
			}
		}

		private void appendException(StringBuilder out, Throwable ex) {
			appendColon(out);
			out.append(ex.getClass().getName());
			String message = ex.getMessage();
			if (!StringServices.isEmpty(message)) {
				appendColon(out);
				out.append(message);
			}
		}

		private void appendColon(StringBuilder out) {
			removeDot(out);
			out.append(": ");
		}

		private void removeDot(StringBuilder out) {
			int lastIndex = out.length() - 1;
			if (out.charAt(lastIndex) == '.') {
				out.deleteCharAt(lastIndex);
			}
		}

		private void appendForProperty(StringBuilder out, PropertyDescriptor property) {
			appendSpace(out);
			out.append("for property ");
			appendProperty(out, property);
		}

		private void appendProperty(StringBuilder out, PropertyDescriptor property) {
			out.append("'");
			out.append(property.getPropertyName());
			out.append("' (");
			out.append(property);
			out.append(")");
		}

		private void errorInvalidAttributeConfiguration(XMLStreamReader reader, PropertyDescriptor property,
				Exception ex, String attributeValue) {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid value '");
			invalidConfigError.append(attributeValue);
			invalidConfigError.append("'");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			context.error(invalidConfigError.toString(), ex);
		}

		private XMLStreamException errorInvalidComplexValue(XMLStreamReader reader, PropertyDescriptor property,
				Exception ex) {
			// No recovery possible, because an arbitrary number of
			// events may have been consumed from the reader.
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Parsing failed ");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			return new XMLStreamException(invalidConfigError.toString(), ex);
		}

		private void errorDuplicateKey(XMLStreamReader reader, PropertyDescriptor property, Object key) {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid indexed property entry ");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendColon(invalidConfigError);
			invalidConfigError.append("Multiple entries with key");
			appendColon(invalidConfigError);
			invalidConfigError.append(key);
			context.error(invalidConfigError.toString());
		}

		private void errorInvalidMapEntry(XMLStreamReader reader, PropertyDescriptor property, Exception ex)
				throws XMLStreamException {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid map property entry ");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			context.error(invalidConfigError.toString(), ex);

			// No additional events have been from the reader:
			// Reading can proceed.
			XMLStreamUtil.skipUpToMatchingEndTag(reader);
		}

		private void errorInvalidListEntry(XMLStreamReader reader, PropertyDescriptor property, Exception ex)
				throws XMLStreamException {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid list property entry ");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			context.error(invalidConfigError.toString(), ex);

			// No additional events have been from the reader.
			XMLStreamUtil.skipUpToMatchingEndTag(reader);
		}

		private void errorInvalidPlainValue(XMLStreamReader reader, PropertyDescriptor property, Exception ex,
				String valueSpec) {
			context.error(messageInvalidValue(reader, property, valueSpec, ex), ex);
		}

		private String messageInvalidValue(XMLStreamReader reader, PropertyDescriptor property, CharSequence value,
				Throwable ex) {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid value '");
			invalidConfigError.append(value);
			invalidConfigError.append("'");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			return invalidConfigError.toString();
		}

		private ConfigurationDescriptor readElementDescriptor(PropertyDescriptor property, XMLStreamReader reader)
				throws ConfigurationException {
			ConfigurationDescriptor descriptorFromTag = getElementDescriptor(property, reader);
			ConfigurationDescriptor descriptorFromAttribute = readConfigInterfaceAsDescriptor(reader);
			if (descriptorFromAttribute == null) {
				return descriptorFromTag;
			}
			if (descriptorFromAttribute.isSubDescriptorOf(descriptorFromTag)) {
				return descriptorFromAttribute;
			}
			StringBuilder error = new StringBuilder();
			error.append("Explicit requested config ");
			appendConfigInterface(error, descriptorFromAttribute);
			error.append(" is not a subtype of ");
			appendConfigInterface(error, descriptorFromTag);
			error.append(".");
			error.append(" The latter is requested by the tag name or the property content type. Property ");
			appendProperty(error, property);
			appendAtLocation(error, reader);
			error.append(".");
			throw new ConfigurationException(error.toString());
		}

		private ConfigurationItem getBaseConfigurationForProperty(XMLStreamReader reader, PropertyDescriptor property,
				ConfigurationItem baseConfig) {
			Object originalValue = valueToAdapt(isOverride(reader), property, baseConfig);
			return getConfig(property, originalValue);
		}

		private ConfigurationItem readItemConfig(XMLStreamReader reader, PropertyDescriptor property,
				ConfigurationDescriptor elementDescriptor, ConfigurationDescriptor defaultDescriptor,
				final ConfigurationItem baseConfig) throws ConfigurationException, XMLStreamException {
			if (baseConfig != null) {
				return readConfigurationItem(reader, baseConfig, property);
			} else {
				return readConfigurationItem(reader, elementDescriptor, defaultDescriptor, property);
			}
		}

		private void errorInvalidItemConfig(XMLStreamReader reader, PropertyDescriptor property, Exception ex)
				throws XMLStreamException {
			StringBuilder invalidConfigError = new StringBuilder();
			invalidConfigError.append("Invalid contents ");
			appendForProperty(invalidConfigError, property);
			appendAtLocation(invalidConfigError, reader);
			appendException(invalidConfigError, ex);
			invalidConfigError.append(".");
			context.error(invalidConfigError.toString(), ex);
			// No additional events have been from the reader.
			XMLStreamUtil.skipUpToMatchingEndTag(reader);
		}

		private ConfigurationDescriptor getElementDescriptor(PropertyDescriptor property, XMLStreamReader reader)
				throws ConfigurationException {
			ConfigurationDescriptor elementDescriptor = property.getElementDescriptor(reader.getLocalName());
			if (elementDescriptor == null) {
				StringBuilder error = new StringBuilder();
				error.append("Unexpected list element '");
				error.append(reader.getLocalName());
				error.append("', expected one of '");
				error.append(property.getElementNames());
				error.append("'");
				appendForProperty(error, property);
				appendAtLocation(error, reader);
				error.append(".");
				throw new ConfigurationException(error.toString());
			}
			return elementDescriptor;
		}

		private Object handleMapUpdate(XMLStreamReader reader, ConfigurationDescriptor elementDescriptor,
				Map<Object, Object> mapValue, PropertyDescriptor keyProperty, PropertyDescriptor owningProperty)
				throws ConfigurationException, XMLStreamException {
			final Object reference = readKeyProperty(reader, keyProperty, elementDescriptor);
			final Object oldValue = mapValue.get(reference);
			if (oldValue == null) {
				StringBuilder error = new StringBuilder();
				error.append("No value to update for value with key '");
				error.append(reference);
				error.append("'");
				appendForProperty(error, owningProperty);
				appendAtLocation(error, reader);
				error.append(".");
				throw new ConfigurationException(error.toString());
			}
			ConfigurationItem baseConfiguration = getConfig(owningProperty, oldValue);
			ConfigurationItem entryValue =
				readConfigItemUpdate(reader, elementDescriptor, baseConfiguration, owningProperty);
			Object key = entryValue.value(keyProperty);
			mapValue.put(key, entryValue);
			return key;
		}

		private ConfigurationItem getConfig(PropertyDescriptor owningProperty, Object value) {
			if (value instanceof ConfigurationItem) {
				return (ConfigurationItem) value;
			}
			ConfigurationAccess configurationAccess = owningProperty.getConfigurationAccess();
			return configurationAccess.getConfig(value);
		}

		private Object handleMapAdd(XMLStreamReader reader, ConfigurationDescriptor elementDescriptor,
				Map<Object, Object> mapValue, PropertyDescriptor keyProperty,
				PropertyDescriptor owningProperty) throws ConfigurationException, XMLStreamException {
			ConfigurationItem entryValue =
				readConfigurationItem(reader, elementDescriptor, elementDescriptor, owningProperty);
			Object key;
			if (entryValue != null) {
				key = entryValue.value(keyProperty);
				Object oldValue = mapValue.put(key, entryValue);
				if (oldValue != null) {
					errorDuplicateKey(reader, keyProperty, entryValue.value(keyProperty));
				}
			} else {
				key = NO_KEY;
			}
			return key;
		}

		private Object handleListUpdate(XMLStreamReader reader, ConfigurationDescriptor elementDescriptor,
				PropertyDescriptor keyProperty, PropertyDescriptor owningProperty, ListHandler handler)
				throws ConfigurationException, XMLStreamException {
			final Object keyPropValue = readKeyProperty(reader, keyProperty, elementDescriptor);
			final ConfigurationItem configToUpdate = handler.resolveReference(keyPropValue);
			handleListUpdate(reader, elementDescriptor, keyProperty, owningProperty, handler, configToUpdate);
			return keyPropValue;
		}

		private void handleListUpdate(XMLStreamReader reader, ConfigurationDescriptor elementDescriptor,
				PropertyDescriptor keyProperty, PropertyDescriptor owningProperty, ListHandler handler,
				final ConfigurationItem configToUpdate) throws ConfigurationException, XMLStreamException {
			Position movePosition = getPosition(reader, null);
			if (movePosition == null) {
				ConfigurationItem newConfig =
					readConfigItemUpdate(reader, elementDescriptor, configToUpdate, owningProperty);
				handler.update(newConfig);
			} else {
				switch (movePosition) {
					case BEGIN: {
						ConfigurationItem newConfig =
							readConfigItemUpdate(reader, elementDescriptor, configToUpdate, owningProperty);
						handler.moveToStart(newConfig);
						break;
					}
					case END: {
						ConfigurationItem newConfig =
							readConfigItemUpdate(reader, elementDescriptor, configToUpdate, owningProperty);
						handler.moveToEnd(newConfig);
						break;
					}
					case BEFORE: {
						String externalReference = getEncodedReferenceKey(reader);
						final Object reference = parseKeyProperty(reader, keyProperty, externalReference);
						ConfigurationItem newConfig =
							readConfigItemUpdate(reader, elementDescriptor, configToUpdate, owningProperty);
						handler.moveBefore(newConfig, reference);
						break;
					}
					case AFTER: {
						String externalReference = getEncodedReferenceKey(reader);
						final Object reference = parseKeyProperty(reader, keyProperty, externalReference);
						ConfigurationItem newConfig =
							readConfigItemUpdate(reader, elementDescriptor, configToUpdate, owningProperty);
						handler.moveAfter(newConfig, reference);
						break;
					}
					default:
						assert false : "Unknown " + Position.class.getName() + ": " + movePosition;
						break;
				}
			}
		}

		private Object readKeyProperty(XMLStreamReader reader, final PropertyDescriptor keyProperty,
				ConfigurationDescriptor elementDescriptor)
				throws ConfigurationException {
			return getKeyProperty(reader, keyProperty, elementDescriptor);
		}

		private Object handleListAdd(XMLStreamReader reader, ConfigurationDescriptor elementDescriptor,
				final PropertyDescriptor keyProperty, PropertyDescriptor owningProperty, ListHandler handler)
				throws ConfigurationException, XMLStreamException {
			Position insertPosition = getPosition(reader, Position.END);
			ConfigurationItem newConfig;
			switch(insertPosition) {
				case BEGIN:{
					newConfig = readConfigurationItem(reader, elementDescriptor, elementDescriptor, owningProperty);
					handler.prepend(newConfig);
					break;
				}
				case END: {
					newConfig = readConfigurationItem(reader, elementDescriptor, elementDescriptor, owningProperty);
					handler.append(newConfig);
					break;
				}
				case BEFORE: {
					String encodedExternalKeyProperty = getEncodedReferenceKey(reader);
					final Object reference = parseKeyProperty(reader, keyProperty, encodedExternalKeyProperty);
					newConfig = readConfigurationItem(reader, elementDescriptor, elementDescriptor, owningProperty);
					handler.insertBefore(reference, newConfig);
					break;
				}
				case AFTER: {
					String encodedExternalKeyProperty = getEncodedReferenceKey(reader);
					final Object reference = parseKeyProperty(reader, keyProperty, encodedExternalKeyProperty);
					newConfig = readConfigurationItem(reader, elementDescriptor, elementDescriptor, owningProperty);
					handler.insertAfter(reference, newConfig);
					break;
				}
				default:
					assert false : "Unknown " + Position.class.getName() + ": " + insertPosition;
					newConfig = null;
					break;
			}
			if (keyProperty != null && newConfig != null) {
				return newConfig.value(keyProperty);
			}
			return NO_KEY;
		}

		private String getEncodedReferenceKey(XMLStreamReader reader) {
			return reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS, ConfigurationSchemaConstants.LIST_REFERENCE_ATTR_NAME);
		}

		private Object getKeyProperty(XMLStreamReader reader, PropertyDescriptor keyProperty,
				ConfigurationDescriptor elementDescriptor) throws ConfigurationException {
			String encodedKeyProperty = reader.getAttributeValue(null, keyProperty.getPropertyName());
			if (!StringServices.isEmpty(encodedKeyProperty)) {
				return parseKeyProperty(reader, keyProperty, encodedKeyProperty);
			}
			if (ConfigurationItem.CONFIGURATION_INTERFACE_NAME.equals(keyProperty.getPropertyName())) {
				String annotatedType = readConfigurationInterface(reader);
				if (annotatedType != null) {
					return parseKeyProperty(reader, keyProperty, annotatedType);
				}

				return elementDescriptor.getConfigurationInterface();
			}
			return keyProperty.getDefaultValue();
		}

		private Object parseKeyProperty(XMLStreamReader reader, PropertyDescriptor keyProperty, String value) throws ConfigurationException {
			if (keyProperty == null) {
				throw new ConfigurationException("No key property given " + atLocation(reader) + ".");
			}
			if (value == null) {
				return keyProperty.getDefaultValue();
			}
			final ConfigurationValueProvider valueProvider = keyProperty.getValueProvider();

			final Object reference = parseValue(reader, valueProvider, keyProperty, value);
			if (reference == null) {
				throw new ConfigurationException("Property resolves null for '" + value
					+ "', but null is not an allowed reference " + atLocation(reader) + ".");
			}
			return reference;
		}

		private ConfigurationItem readConfigurationItem(XMLStreamReader reader,
				ConfigurationDescriptor elementDescriptor, ConfigurationDescriptor defaultDescriptor,
				PropertyDescriptor owningProperty)
				throws ConfigurationException, XMLStreamException {
			ConfigBuilder valueBuilder = createConfigBuilder(reader, elementDescriptor, defaultDescriptor, owningProperty, false);
			if (valueBuilder == null) {
				return null;
			}
			return readNextElement(valueBuilder, reader, null, owningProperty);
		}

		private ConfigurationItem readConfigurationItem(XMLStreamReader reader, ConfigurationItem baseConfig,
				PropertyDescriptor owningProperty) throws ConfigurationException, XMLStreamException {
			ConfigBuilder valueBuilder =
				createConfigBuilder(reader, baseConfig.descriptor(), baseConfig.descriptor(), owningProperty, true);
			if (valueBuilder == null) {
				return null;
			}
			moveValues(baseConfig, valueBuilder);
			return readNextElement(valueBuilder, reader, valueBuilder, owningProperty);
		}

		/**
		 * Reads an map or list entry update.
		 * <p>
		 * The values in the given "baseConfig" are not deep-copied but flat-copied, as it is not
		 * necessary to deep-copy the old map or list entry: They won't be used anymore anyway.
		 * </p>
		 */
		private ConfigurationItem readConfigItemUpdate(XMLStreamReader reader,
				ConfigurationDescriptor elementDescriptor,
				ConfigurationItem baseConfig, PropertyDescriptor owningProperty)
				throws ConfigurationException, XMLStreamException {
			ConfigBuilder newBuilder;
			if (isOverride(reader)) {
				newBuilder = createConfigBuilder(reader, elementDescriptor, elementDescriptor, owningProperty, false);
			} else {
				newBuilder = createConfigBuilderForUpdate(reader, baseConfig, elementDescriptor, owningProperty);
			}
			if (newBuilder == null) {
				return null;
			}
			return readNextElement(newBuilder, reader, newBuilder, owningProperty);
		}

		/**
		 * For "changing" the type of a {@link ConfigBuilder} by creating a new one and copying the
		 * values.
		 * <p>
		 * The type ({@link ConfigurationDescriptor}) of a {@link ConfigBuilder} cannot be changed.
		 * Therefore, this method creates a new one and (flat-)copies the values from the old to the
		 * new {@link ConfigBuilder}.
		 * </p>
		 */
		private ConfigBuilder createConfigBuilderForUpdate(XMLStreamReader reader, ConfigurationItem baseConfig,
				ConfigurationDescriptor newType, PropertyDescriptor owningProperty)
				throws ConfigurationException, XMLStreamException {

			ConfigurationDescriptor type;
			ConfigurationDescriptor oldType = baseConfig.descriptor();
			if (newType.getConfigurationInterface().isAssignableFrom(oldType.getConfigurationInterface())) {
				type = oldType;
			} else {
				type = newType;
			}
			ConfigBuilder newBuilder = createConfigBuilder(reader, type, type, owningProperty, false);
			if (newBuilder != null) {
				// The new config builder is only created to change the descriptor.
				// Therefore, the values don't have to be deep-copied.
				moveValues(baseConfig, newBuilder);
			}
			return newBuilder;
		}

		/**
		 * Returns the value to adopt or <code>null</code> if the old value does not matter
		 * 
		 * @param <T>
		 *        the type of the value to adopt
		 * @param override
		 *        whether the container tag is annotated to override the base value.
		 * @param property
		 *        the property whose value is requested
		 * @param baseConfig
		 *        the {@link ConfigurationItem} to resolve old value from
		 */
		private static <T> T valueToAdapt(boolean override, PropertyDescriptor property, ConfigurationItem baseConfig) {
			if (override) {
				return null;
			} else {
				return valueToAdapt(property, baseConfig);
			}
		}

		private static <T> T valueToAdapt(PropertyDescriptor property, ConfigurationItem baseConfig) {
			if (baseConfig == null) {
				return null;
			}
			if (!baseConfig.descriptor().hasProperty(property.getPropertyName())) {
				return null;
			}
			return (T) baseConfig.value(property);
		}

		private MapOperation getMapOperation(XMLStreamReader reader) {
			try {
				return getMapOperationValue(reader);
			} catch (ConfigurationException ex) {
				context.error("Attribute value must be boolean", ex);
				return ConfigurationSchemaConstants.MAP_OPERATION_DEFAULT;
			}
		}

		/**
		 * Parses the configured value of the map operation.
		 * 
		 * @param reader
		 *        {@link XMLStreamReader} that parses the configuration.
		 * @return The configured value for the list operation or
		 *         {@link ConfigurationSchemaConstants#MAP_OPERATION_DEFAULT} if not value was
		 *         configured.
		 */
		public static MapOperation getMapOperationValue(XMLStreamReader reader) throws ConfigurationException {
			final String attributeValue = reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
				ConfigurationSchemaConstants.MAP_OPERATION);
			return ConfigUtil.getEnumValue(ConfigurationSchemaConstants.MAP_OPERATION, attributeValue,
				MapOperation.class, ConfigurationSchemaConstants.MAP_OPERATION_DEFAULT);
		}

		private ListOperation getListOperation(XMLStreamReader reader) {
			try {
				return getListOperationValue(reader, ConfigurationSchemaConstants.LIST_OPERATION_DEFAULT);
			} catch (ConfigurationException ex) {
				context.error("Could not read list operation attribute value", ex);
				return ConfigurationSchemaConstants.LIST_OPERATION_DEFAULT;
			}
		}

		/**
		 * Parses the configured value of the list operation.
		 * 
		 * @param reader
		 *        {@link XMLStreamReader} that parses the configuration.
		 * @param defaultOperation
		 *        The default {@link ListOperation} to use if not operation was configured.
		 * @return The configured value for the list operation or
		 *         {@link ConfigurationSchemaConstants#LIST_OPERATION_DEFAULT} if not value was
		 *         configured.
		 */
		public static ListOperation getListOperationValue(XMLStreamReader reader, ListOperation defaultOperation) throws ConfigurationException {
			final String attributeValue = reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
				ConfigurationSchemaConstants.LIST_OPERATION);
			return ConfigUtil.getEnumValue(ConfigurationSchemaConstants.LIST_OPERATION, attributeValue,
				ListOperation.class, defaultOperation);

		}

		private Position getPosition(XMLStreamReader reader, Position defaultPosition) {
			try {
				return getPositionValue(reader, defaultPosition);
			} catch (ConfigurationException ex) {
				context.error("Could not read position attribute value", ex);
				return defaultPosition;
			}
		}

		/**
		 * Parses the configured value of the position.
		 * 
		 * @param reader
		 *        {@link XMLStreamReader} that parses the configuration.
		 * @param defaultPosition
		 *        Fallback position if no position was configured.
		 * @return The position value or the given default position if no position was configured.
		 */
		public static Position getPositionValue(XMLStreamReader reader, Position defaultPosition)
				throws ConfigurationException {
			final String attributeValue = reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
				ConfigurationSchemaConstants.LIST_POSITION);
			return ConfigUtil.getEnumValue(ConfigurationSchemaConstants.LIST_POSITION, attributeValue,
				Position.class, defaultPosition);
		}

		private boolean isOverride(XMLStreamReader reader) {
			final String attributeValue = reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
					ConfigurationSchemaConstants.CONFIG_OVERLOADING_OVERRIDE);
			try {
				return ConfigUtil.getBooleanValue(ConfigurationSchemaConstants.CONFIG_OVERLOADING_OVERRIDE,
						attributeValue, ConfigurationSchemaConstants.OVERRIDE_DEFAULT);
			} catch (ConfigurationException ex) {
				context.error("Attribute value must be boolean", ex);
				return ConfigurationSchemaConstants.OVERRIDE_DEFAULT;
			}
		}

		private void checkOnlySizeAttribute(PropertyDescriptor property, XMLStreamReader reader) {
			for (int n = 0, cnt = reader.getAttributeCount(); n < cnt; n++) {
				String ns = XMLStreamUtil.getAttributeNamespace(reader, n);
				if (ConfigurationSchemaConstants.CONFIG_NS.equals(ns)) {
					continue;
				}
				if (ANNOTATION_NS.equals(ns)) {
					continue;
				}
				String attributeName = reader.getAttributeLocalName(n);
				if (attributeName.equals("size")) {
					// Legacy handling of size annotations.
					continue;
				}
				context.error("No attribute '" + attributeName + "' expected for list elements of list property '"
					+ property.getPropertyName() + "' " + atLocation(reader) + ".");
			}
		}

		private void checkNoAttributes(PropertyDescriptor property, XMLStreamReader reader) {
			for (int n = 0, cnt = reader.getAttributeCount(); n < cnt; n++) {
				String attributeName = reader.getAttributeLocalName(n);
				context.error("No attribute '" + attributeName + "' expected for plain property '"
					+ property.getPropertyName() + "' " + atLocation(reader) + ".");
			}
		}

		private void initLocation(ConfigBuilder builder, XMLStreamReader reader) {
			Location location = reader.getLocation();

			String definition = _definitions.peek();
			int line = location.getLineNumber();
			int column = location.getColumnNumber();
			
			builder.initLocation(definition, line, column);
		}

		private ConfigBuilder newBuilder(ConfigurationDescriptor descriptor) {
			return TypedConfiguration.createConfigBuilder(descriptor);
		}

		private ConfigBuilder createConfigBuilder(XMLStreamReader reader, ConfigurationDescriptor expectedDescriptor,
				ConfigurationDescriptor defaultDescriptor, PropertyDescriptor owningProperty, boolean fallbackItem)
				throws ConfigurationException, XMLStreamException {
			String explicitConfigInterfaceValue = readConfigurationInterface(reader);
			if (explicitConfigInterfaceValue != null
				&& explicitConfigInterfaceValue.isEmpty()) {
				// An explicitly set empty configuration interface is the
				// marker for a null value (that must be explicitly encoded, if the property
				// has an item default).
				XMLStreamUtil.skipUpToMatchingEndTag(reader);
				return null;
			}

			// Check, whether there is an annotation of the
			// configuration interface type.
			ConfigurationDescriptor concreteConfigDescriptor = readConfigInterfaceAsDescriptor(reader);
			if (concreteConfigDescriptor == null) {
				if (expectedDescriptor == null) {
					String message =
						"Property '" + owningProperty + "' cannot be parsed, as its element type is unknown "
							+ atLocation(reader) + "."
						+ " Possible causes: Missing '@InstanceFormat' annotation,"
						+ " missing 'extends ConfigurationItem', wrong property type, etc.";
					throw new ConfigurationException(message);
				}
				// Check, whether an implementation class is given in a
				// polymorphic configuration. From the implementation class, the
				// config interface can be inferred.
				if (isPolymorphicConfiguration(reader, expectedDescriptor)) {
					PropertyDescriptor classProperty = 						classProperty(reader, expectedDescriptor);
					assert classProperty != null : "Class property is defined by " + PolymorphicConfiguration.class;
					
					String classPropertyConfigName = classProperty.getPropertyName();
					String concreteImplClassName = reader.getAttributeValue(null, classPropertyConfigName);
					if (StringServices.isEmpty(concreteImplClassName)) {
						if (fallbackItem) {
							/* There is a fallback item. In this item the implementation class were
							 * set before and the configuration descriptor must also not be changed.
							 * Otherwise, an previously explicit stated implementation class would
							 * be "forgotten". */
							return newBuilder(expectedDescriptor);
						}
						return newBuilderForDefaultImplementationClass(reader, defaultDescriptor);
					} else {
						return createBuilderForImplementationClass(reader, expectedDescriptor, concreteImplClassName);
					}
				} else {
					// Fallback to the expected descriptor.
					return newBuilder(defaultDescriptor);
				}
			} else {
				return newBuilder(concreteConfigDescriptor);
			}

		}

		private ConfigBuilder createBuilderForImplementationClass(XMLStreamReader reader,
				ConfigurationDescriptor expectedDescriptor, String implementationClassName)
				throws ConfigurationException {
			Class<?> implementationClass =
				getConfiguredImplementationClass(reader, expectedDescriptor, implementationClassName);
			if (implementationClass == null) {
				throw new ConfigurationException("No implementation class set " + atLocation(reader) + ".");
			}
			return createBuilderForImplementationClass(reader, expectedDescriptor, implementationClass);
		}

		private ConfigBuilder newBuilderForDefaultImplementationClass(XMLStreamReader reader, ConfigurationDescriptor defaultDescriptor)
				throws ConfigurationException {
			Class<?> implementationClass = (Class<?>) classProperty(reader, defaultDescriptor).getDefaultValue();
			Class<?> implementationConfig = getImplementationConfig(implementationClass);

			ConfigurationDescriptor concreteDescriptor =
				getConcreteDescritptor(defaultDescriptor, implementationConfig);

			return newBuilder(concreteDescriptor);
		}

		private Class<?> getImplementationConfig(Class<?> implementationClass) throws ConfigurationException {
			Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
			return factory.getConfigurationInterface();
		}

		private ConfigurationDescriptor getConcreteDescritptor(ConfigurationDescriptor expectedDescriptor,
				Class<?> implementationConfig) {
			if (implementationConfig.isAssignableFrom(expectedDescriptor.getConfigurationInterface())) {
				// The expected descriptor is more specific - more than its default implementation
				// class requires.
				return expectedDescriptor;
			} else {
				return TypedConfiguration.getConfigurationDescriptor(implementationConfig);
			}
		}

		private Class<?> getConfiguredImplementationClass(XMLStreamReader reader,
				ConfigurationDescriptor expectedDescriptor, String implementationClassName) throws ConfigurationException {
			PropertyDescriptor classProperty = classProperty(reader, expectedDescriptor);
			ConfigurationValueProvider<Class<?>> classValueProvider = classProperty.getValueProvider();
			if (classValueProvider == null) {
				throw new AssertionError("The '" + classProperty.getPropertyName() + "' property of a '"
					+ PolymorphicConfiguration.class + "' interface requires a value provider.");
			}
			return parseValue(reader, classValueProvider, classProperty, implementationClassName);
		}

		private ConfigBuilder createBuilderForImplementationClass(XMLStreamReader reader,
				ConfigurationDescriptor expectedDescriptor, Class<?> implementationClass)
				throws ConfigurationException {
			checkConcreteImplementationClass(reader, implementationClass);

			ConfigBuilder result =
				TypedConfiguration.createConfigBuilderForImplementationClass(implementationClass, expectedDescriptor);

			// install implementation class as it was set explicit
			result.update(classProperty(reader, result.descriptor()), implementationClass);
			return result;
		}

		private PropertyDescriptor classProperty(XMLStreamReader reader, ConfigurationDescriptor descriptor) throws ConfigurationException {
			if (!isPolymorphicConfiguration(reader, descriptor)) {
				throw new ConfigurationException("Descriptor '" + descriptor
					+ "' is not for a polymorphic configuration " + atLocation(reader) + ".");
			}
			PropertyDescriptor classProperty =
				descriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
			assert classProperty != null : PolymorphicConfiguration.class.getName() + " has property "
				+ PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME;
			return classProperty;
		}

		private void checkConcreteImplementationClass(XMLStreamReader reader, Class<?> implementationClass)
				throws ConfigurationException {
			if (Modifier.isAbstract(implementationClass.getModifiers())) {
				String isAbstract = reader.getAttributeValue(
					ConfigurationSchemaConstants.CONFIG_NS,
					ConfigurationSchemaConstants.CONFIG_ABSTRACT);
				if (!Boolean.parseBoolean(isAbstract)) {
					Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
					if (!factory.isConcrete()) {
						/* Factory can not be used to instantiate new objects */
						StringBuilder error = new StringBuilder();
						error.append("Configuration of an class '");
						error.append(implementationClass.getName());
						error
							.append("' which is abstract, but the configuration was not declared abstract ");
						error.append(atLocation(reader));
						throw new ConfigurationException(error.toString());
					}
				}
			}
		}

		private boolean isPolymorphicConfiguration(XMLStreamReader reader, ConfigurationDescriptor descriptor) throws ConfigurationException {
			if (descriptor == null) {
				String message = "No configuration descriptor found " + atLocation(reader) + ".";
				context.error(message);

				return false;
			}
			return PolymorphicConfiguration.class.isAssignableFrom(descriptor.getConfigurationInterface());
		}

		/**
		 * null, if no config-interface is specified.
		 */
		private ConfigurationDescriptor readConfigInterfaceAsDescriptor(XMLStreamReader reader)
				throws ConfigurationException {
			Class<?> configInterface = readConfigInterfaceAsClass(reader);
			if (configInterface == null) {
				return null;
			}
			return TypedConfiguration.getConfigurationDescriptor(configInterface);
		}

		/**
		 * null, if no config-interface is specified.
		 */
		private Class<?> readConfigInterfaceAsClass(XMLStreamReader reader) throws ConfigurationException {
			String configInterfaceName = readConfigurationInterface(reader);
			if (StringServices.isEmpty(configInterfaceName)) {
				return null;
			}
			try {
				return Class.forName(configInterfaceName);
			} catch (ClassNotFoundException ex) {
				throw new ConfigurationException("Cannot resolve concrete configuration interface '"
					+ configInterfaceName + "' " + atLocation(reader) + ".");
			}
		}

		private String readConfigurationInterface(XMLStreamReader reader) {
			return reader.getAttributeValue(ConfigurationSchemaConstants.CONFIG_NS,
				ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR);
		}
	}

	private final LogWithStreamLocation _protocol;

	private final Map<String, ConfigurationDescriptor> _globalDescriptorsByName;

	/**
	 * Creates a {@link ConfigurationReader}, which has to be configured, before {@link #read()} or
	 * {@link #readConfigBuilder()} can finally be called.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * </p>
	 * 
	 * @param context
	 *        Is used for the actual instantiation of the {@link ConfigurationItem}, as well as
	 *        error reporting. Is not allowed to be <code>null</code>.
	 * @param globalDescriptorsByName
	 *        The {@link ConfigurationDescriptor}s which can appear as xml root nodes, with the
	 *        local tag names, which identify them. Is not allowed to be <code>null</code>. Is not
	 *        allowed to be empty. Is not allowed to contain <code>null</code> as key or value.
	 */
	public ConfigurationReader(InstantiationContext context,
			Map<String, ConfigurationDescriptor> globalDescriptorsByName) {
		super(context);
		check(context, globalDescriptorsByName);
		_protocol = new LogWithStreamLocation(context);
		_context = new InstantiationContextAdaptor(_protocol, context);
		_globalDescriptorsByName = globalDescriptorsByName;
	}

	static Object listAsGenericArray(PropertyDescriptor property, List<?> list) {
		return list.toArray();
	}

	private void check(InstantiationContext context, Map<String, ConfigurationDescriptor> globalDescriptorsByName) {
		if (context == null) {
			throw new NullPointerException("Context is not allowed to be null.");
		}
		if (globalDescriptorsByName == null) {
			throw new NullPointerException("Global descriptor map is not allowed to be null.");
		}
		if (globalDescriptorsByName.containsKey(null)) {
			throw new NullPointerException("Global descriptor map is not allowed to contain null as key.");
		}
		if (globalDescriptorsByName.containsValue(null)) {
			throw new NullPointerException("Global descriptor map is not allowed to contain null as value.");
		}
	}

	@Override
	protected ConfigurationItem readInternal(Content source, ConfigurationItem baseConfig)
			throws ConfigurationException {

		String name = source.toString();
		try {
			try (XMLContent content = XMLContent.open(source)) {
				return parseAndClose(name, baseConfig, content.reader());
			}
		} catch (IOException ex) {
			String message =
				"Could not read configuration '" + name + "', as opening the stream failed: " + ex.getMessage();
			_context.error(message, ex);
			throw new ConfigurationException(message, ex);
		} catch (XMLStreamException ex) {
			throw error(name, ex);
		}
	}

	private ConfigurationItem parseAndClose(String name, ConfigurationItem baseConfig, XMLStreamReader xmlReader) {
		try {
			if (_expander != null) {
				xmlReader = wrapAliasReader(xmlReader, name, _expander);
			}
			Handler handler = new Handler(_context, _globalDescriptorsByName);
			XMLStreamReader before = _protocol.setStream(xmlReader);
			try {
				return handler.parse(xmlReader, baseConfig);
			} finally {
				_protocol.setStream(before);
			}
		} finally {
			close(xmlReader);
		}
	}

	/**
	 * Not used due to Workaround for Unicode bug in Java 6 and 7.
	 */
	private ConfigurationException error(String name, XMLStreamException ex) throws ConfigurationException {
		String message = "Could not read configuration '" + name + "' " + XMLStreamUtil.atLocation(ex) + ".";
		_context.error(message, ex);
		throw new ConfigurationException(message, ex);
	}

	private XMLStreamReader wrapAliasReader(XMLStreamReader xmlReader, String name, IVariableExpander expander) {
		if (!AliasManager.Module.INSTANCE.isActive()) {
			// when no aliasManager is active there is no reason to create an
			// AliasedXMLReader. May happen during creating of scripted tests.
			_context.info("AliasManager module is not active. Aliases will not be resolved.");
			return xmlReader;
		}
		return AliasedXMLReader.createAliasedXMLReader(xmlReader, name, expander);
	}

	private void close(Closeable stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (RuntimeException ex) {
			_context.error("Failed to close: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			_context.error("Failed to close: " + ex.getMessage(), ex);
		}
	}

	private void close(XMLStreamReader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (RuntimeException ex) {
			_context.error("Failed to close the XMLStreamReader: " + ex.getMessage(), ex);
		} catch (XMLStreamException ex) {
			_context.error("Failed to close the XMLStreamReader: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates a {@link ConfigurationItem} from the given config {@link Content} and returns it.
	 */
	public static ConfigurationItem readContent(InstantiationContext context,
			Map<String, ConfigurationDescriptor> globalDescriptorsByName, Content source)
			throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(context, globalDescriptorsByName);
		reader.setSource(source);
		return reader.read();
	}

	/**
	 * Creates a {@link ConfigurationItem} from the given configuration file and returns it.
	 * 
	 * @param protocol
	 *        {@link Protocol} to log configuration failures.
	 */
	public static ConfigurationItem readFile(Protocol protocol,
			Map<String, ConfigurationDescriptor> globalDescriptorsByName, File configFile)
			throws ConfigurationException {
		return readContent(protocol, globalDescriptorsByName, content(configFile));
	}

	/**
	 * Creates a {@link ConfigurationItem} from the given configuration {@link Content} and returns
	 * it.
	 * 
	 * @param protocol
	 *        {@link Protocol} to log configuration failures.
	 */
	public static ConfigurationItem readContent(Protocol protocol,
			Map<String, ConfigurationDescriptor> globalDescriptorsByName, Content content)
			throws ConfigurationException {
		InstantiationContext context = new DefaultInstantiationContext(protocol);
		return readContent(context, globalDescriptorsByName, content);
	}

	private static BinaryContent content(File configFile) {
		return FileBasedBinaryContent.createBinaryContent(configFile);
	}

}
