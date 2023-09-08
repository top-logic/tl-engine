/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.TestFormat.FormatTooSpecific;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Test case for {@link Format} annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFormatAndBinding extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {

		@Format(V.ValueFormat.class)
		@Binding(V.ValueBinding.class)
		V getValue();

	}

	public static class V {

		private final String _x;

		private final String _y;

		public V(String x, String y) {
			_x = x;
			_y = y;
		}

		public String getX() {
			return _x;
		}

		public String getY() {
			return _y;
		}

		public static class ValueFormat extends AbstractConfigurationValueProvider<V> {

			public ValueFormat() {
				super(V.class);
			}

			@Override
			protected V getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String value = propertyValue.toString();
				int sepIndex = value.indexOf(':');

				return new V(value.substring(0, sepIndex), value.substring(sepIndex + 1));
			}

			@Override
			protected String getSpecificationNonNull(V configValue) {
				return configValue.getX() + ":" + configValue.getY();
			}

			@Override
			public boolean isLegalValue(Object value) {
				return value == null || (value instanceof V && ((V) value).getX().indexOf(':') < 0);
			}
		}

		public static class ValueBinding extends AbstractConfigurationValueBinding<V> {

			@Override
			public V loadConfigItem(XMLStreamReader in, V baseValue) throws XMLStreamException, ConfigurationException {
				in.nextTag();
				String x = XMLStreamUtil.nextText(in);
				in.nextTag();
				String y = XMLStreamUtil.nextText(in);
				in.nextTag();
				return new V(x, y);
			}

			@Override
			public void saveConfigItem(XMLStreamWriter out, V item) throws XMLStreamException {
				out.writeStartElement("x");
				out.writeCharacters(item.getX());
				out.writeEndElement();

				out.writeStartElement("y");
				out.writeCharacters(item.getY());
				out.writeEndElement();
			}

		}
	}

	public void testDetectFormatTypeIncompatibility() throws XMLStreamException {
		FormatTooSpecific config;
		try {
			config = TypedConfiguration.newConfigItem(FormatTooSpecific.class);

			// If configuration would be instantiated, a legal configuration could not be
			// serialized:
			config.setFoo(new FormatTooSpecific.Foo());
			new ConfigurationWriter(new StringWriter()).write("config", FormatTooSpecific.class, config);

			// The prove that the invalid format annotation can lead to problems does not work. This
			// should never be reached.
			fail("Something is really wrong, if this is reached.");
		} catch (IllegalArgumentException ex) {
			// Expected. Invalid format annotation.
			BasicTestCase.assertContains(FormatTooSpecific.BarFormat.class.getName(), ex.getMessage());
		}
	}

	public void testReadFormat() throws ConfigurationException, XMLStreamException {
		A item = read("<a value='foo:bar'/>");
		assertEquals("foo", item.getValue().getX());
		assertEquals("bar", item.getValue().getY());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" value=\"foo:bar\"></a>",
			write(item));
	}

	public void testReadBindingSimple() throws ConfigurationException, XMLStreamException {
		A item = read("<a><value><x>foo</x><y>bar</y></value></a>");
		assertEquals("foo", item.getValue().getX());
		assertEquals("bar", item.getValue().getY());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" value=\"foo:bar\"></a>",
			write(item));
	}

	public void testReadBindingComplex() throws ConfigurationException, XMLStreamException {
		A item = read("<a><value><x>foo:bar</x><y>baz</y></value></a>");
		assertEquals("foo:bar", item.getValue().getX());
		assertEquals("baz", item.getValue().getY());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><value><x>foo:bar</x><y>baz</y></value></a>",
			write(item));
	}

	private String write(A item) throws XMLStreamException {
		StringWriter buffer = new StringWriter();
		new ConfigurationWriter(buffer).write("a", A.class, item);
		return buffer.toString();
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

}
