/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import static com.top_logic.basic.util.ResKey.*;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;

/**
 * Test case for {@link com.top_logic.basic.util.ResKey.ValueBinding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResKeyXMLBinding extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {
		ResKey getText();

		void setText(ResKey value);
	}

	public void testFormat() throws ConfigurationException, XMLStreamException {
		A item = read("<a text='my.key'/>");
		assertEquals(ResKey.forTest("my.key"), item.getText());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" text=\"my.key\"></a>",
			write(item));
	}

	public void testFormatElement() throws ConfigurationException {
		A item = read("<a><text key='my.key'/></a>");
		assertEquals(ResKey.forTest("my.key"), item.getText());
	}

	public void testFormatAsTextContent() throws ConfigurationException {
		A item = read("<a><text>my.key</text></a>");
		assertEquals(ResKey.forTest("my.key"), item.getText());
	}

	public void testFormatLiteral() throws ConfigurationException, XMLStreamException {
		A item = read("<a text='#(\"foo\"@en, \"bar\"@de)'/>");
		assertEquals(literal(langString(en(), "foo"), langString(de(), "bar")), item.getText());

		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><text><en>foo</en><de>bar</de></text></a>",
			write(item));
	}

	public void testLiteralWithKey() throws ConfigurationException, XMLStreamException {
		A item = TypedConfiguration.newConfigItem(A.class);
		Builder builder = ResKey.builder("my.key");
		builder.add(en(), "foo");
		builder.add(de(), "bar");
		item.setText(builder.build());

		String xml = write(item);
		assertEquals(
			"<?xml version=\"1.0\" ?><a xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><text key=\"my.key\"><en>foo</en><de>bar</de></text></a>",
			xml);

		ConfigurationItem copy = read(xml);
		assertEquals(item, copy);
		assertEquals("my.key", item.getText().getKey());
	}

	private Locale en() {
		return new Locale("en");
	}

	private Locale de() {
		return new Locale("de");
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

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestResKeyXMLBinding.class);
	}

}
