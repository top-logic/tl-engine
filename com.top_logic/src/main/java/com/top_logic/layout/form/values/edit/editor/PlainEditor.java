/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.Format;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.NumberOfRows;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * {@link Editor} displaying a plain text input field that allows editing the property value using
 * the properties XML format.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainEditor extends AbstractEditor {

	/**
	 * Singleton {@link PlainEditor} instance.
	 */
	public static final PlainEditor INSTANCE = new PlainEditor();

	/**
	 * Creates a {@link PlainEditor}.
	 */
	protected PlainEditor() {
		// Singleton constructor.
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		PropertyDescriptor property = model.getProperty();
		@SuppressWarnings("unchecked")
		final ConfigurationValueProvider<Object> provider =
			(ConfigurationValueProvider<Object>) getValueProvider(property);
		Format format = new ConfigurationFormatAdapter(provider);

		// Allow entering raw configuration values.
		ComplexField field = complex(container, fieldName, format);

		Mapping<Object, Object> uiConversion = Identity.INSTANCE;
		Mapping<Object, Object> storageConversion = Converters.valueConverter(property);

		if (provider instanceof ValueBindingAdapter) {
			field.setControlProvider(MultiLineText.INSTANCE);
		}
		
		NumberOfRows annotation = editorFactory.getAnnotation(property, NumberOfRows.class);

		if (annotation != null) {
			field.set(NumberOfRows.NUMBER_OF_ROWS, new Pair<>(annotation.min(), annotation.max()));
		}

		if (editorFactory.getAnnotation(property, Encrypted.class) != null) {
			field.setControlProvider(PasswordInputControlProvider.INSTANCE);
		}

		init(editorFactory, model, field, uiConversion, storageConversion);

		return field;
	}

	private ConfigurationValueProvider<?> getValueProvider(final PropertyDescriptor property) {
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			ConfigurationValueBinding<?> xmlBinding = property.getValueBinding();
			if (xmlBinding != null) {
				return toValueProvider(xmlBinding);
			}
			// Allow entering a raw class name.
			if (property.isInstanceValued()) {
				valueProvider = new AbstractConfigurationValueProvider<>(property.getType()) {
					@Override
					protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue)
							throws ConfigurationException {
						return ConfigUtil.getInstanceWithInstanceDefault(property.getType(), propertyName,
							propertyValue, null);
					}

					@Override
					protected String getSpecificationNonNull(Object configValue) {
						return configValue.getClass().getName();
					}
				};
			} else {
				valueProvider =
					new AbstractConfigurationValueProvider<PolymorphicConfiguration<?>>(
						PolymorphicConfiguration.class) {
						@Override
						protected PolymorphicConfiguration<?> getValueNonEmpty(String propertyName,
								CharSequence propertyValue)
								throws ConfigurationException {
							Class<?> implClass =
								ConfigUtil.getClassForName(property.getElementType(), propertyName,
									propertyValue, null);

							@SuppressWarnings("unchecked")
							PolymorphicConfiguration<Object> config =
								TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
							config.setImplementationClass(implClass);
							return config;
						}

						@Override
						protected String getSpecificationNonNull(PolymorphicConfiguration<?> configValue) {
							return configValue.getImplementationClass().getName();
						}
					};
			}
		}
		return valueProvider;
	}

	private <T> ConfigurationValueProvider<T> toValueProvider(ConfigurationValueBinding<T> xmlBinding) {
		return new ValueBindingAdapter<>(Object.class, xmlBinding);
	}

	/**
	 * {@link ConfigurationValueProvider} that allows to edit XML fragments from a
	 * {@link ConfigurationValueBinding}.
	 */
	private static final class ValueBindingAdapter<T> extends AbstractConfigurationValueProvider<T> {
		private static final String FRAGMENT_TAG = "FRAGMENT";

		private static final String FRAGMENT_START = "<" + FRAGMENT_TAG + ">";

		private static final String FRAGMENT_STOP = "</" + FRAGMENT_TAG + ">";

		private final ConfigurationValueBinding<T> _xmlBinding;
	
		/** 
		 * Creates a {@link ValueBindingAdapter}.
		 */
		public ValueBindingAdapter(Class<?> type, ConfigurationValueBinding<T> xmlBinding) {
			super(type);
			_xmlBinding = xmlBinding;
		}
	
		@Override
		protected T getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			try {
				return fromXML(_xmlBinding, propertyValue);
			} catch (XMLStreamException ex) {
				throw new ConfigurationException(
					"Cannot parse value " + XMLStreamUtil.atLocation(ex) + ": " + ex.getMessage());
			}
		}
	
		@Override
		protected String getSpecificationNonNull(T configValue) {
			try {
				return toXML(_xmlBinding, configValue);
			} catch (XMLStreamException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public boolean isLegalValue(Object value) {
			return _xmlBinding.isLegalValue(value);
		}

		@Override
		public T defaultValue() {
			return _xmlBinding.defaultValue();
		}
	
		static <T> String toXML(ConfigurationValueBinding<? super T> xmlBinding, T configValue)
				throws XMLStreamException {
			StringWriter buffer = new StringWriter();
			XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
			out.writeStartDocument();
			out.writeStartElement(FRAGMENT_TAG);
			out.flush();
			xmlBinding.saveConfigItem(out, configValue);
			out.flush();
			out.writeEndElement();
			out.writeEndDocument();
			out.close();
	
			String rawXml = buffer.getBuffer().toString();
			Config config = XMLPrettyPrinter.newConfiguration();
			config.setEmptyTags(tag -> !FRAGMENT_TAG.equals(tag));
			config.setIndentChar('\t');
			String xml = XMLPrettyPrinter.prettyPrint(config, rawXml);

			int start = xml.indexOf(FRAGMENT_START);
			int stop = xml.lastIndexOf(FRAGMENT_STOP);
			assert start >= 0;
			assert stop >= 0;
			String fragmentXml = xml.substring(start + FRAGMENT_START.length(), stop);
			return fragmentXml.replaceAll("(?m)^\t", "").trim();
		}
	
		static <T> T fromXML(ConfigurationValueBinding<T> xmlBinding, CharSequence propertyValue)
				throws XMLStreamException, ConfigurationException {
			String input = FRAGMENT_START + propertyValue + FRAGMENT_STOP;
			XMLStreamReader in =
				XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(input));
			int start = in.nextTag();
			assert start == XMLStreamConstants.START_ELEMENT;
			T result = xmlBinding.loadConfigItem(in, null);
			int stop = in.getEventType();
			assert stop == XMLStreamConstants.END_ELEMENT;
			assert FRAGMENT_TAG.equals(in.getLocalName());
			return result;
		}
	}

}
