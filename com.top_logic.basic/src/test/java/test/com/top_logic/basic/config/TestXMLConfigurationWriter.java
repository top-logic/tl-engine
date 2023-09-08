/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.config.AbstractConfigurationWriterTest.TagNameTest.TestConfig;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.A.Config;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;

/**
 * Test case for {@link ConfigurationWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestXMLConfigurationWriter extends AbstractConfigurationWriterTest {

	/**
	 * Test that an empty configuration does not create any container tags.
	 */
	public void testEmpty() throws XMLStreamException {
		Config config = TypedConfiguration.newConfigItem(A.Config.class);
		assertTrue(config.getBConfigs().isEmpty());
		assertTrue(config.getIndexed().isEmpty());
		String xml = writeConfigurationItem("a", config.descriptor(), config);
		Document document = DOMUtil.parse(xml);
		assertNull(document.getDocumentElement().getFirstChild());
	}

	public void testNamespace() throws XMLStreamException, ConfigurationException {
		A.Config a = create(A.Config.class);
		a.setP(42);
		B.Config b = create(B.Config.class);
		b.setX(13);
		a.setBConfig(b);
		Map<String, ConfigurationDescriptor> descriptors = getDescriptors();
		StringWriter buffer = new StringWriter();
		Entry<String, ConfigurationDescriptor> entry = getDescriptorBinding(descriptors, a);
		ConfigurationWriter writer = new ConfigurationWriter(buffer);
		writer.setNamespace("x", "http://www.foo.com/ns/xyz");
		String tag = entry.getKey();
		Class<? extends ConfigurationItem> type = (Class<? extends ConfigurationItem>) entry.getValue()
			.getConfigurationInterface();
		writer.write(tag, type, a);
		String xml = buffer.toString();

		assertEqualsXML("<a xmlns='http://www.foo.com/ns/xyz' p='42'><b x='13'/></a>", xml);

		ConfigurationItem aCopy = fromXML(xml);

		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, aCopy));
	}

	public void testWritePropertiesInDisplayOrder() throws XMLStreamException, IOException {
		ConfigWithDisplayAnnotation root = TypedConfiguration.newConfigItem(ConfigWithDisplayAnnotation.class);
		root.setA(TypedConfiguration.newConfigItem(ConfigWithDisplayAnnotation.class));
		root.setB(TypedConfiguration.newConfigItem(ConfigWithDisplayAnnotation.class));
		root.setC(TypedConfiguration.newConfigItem(ConfigWithDisplayAnnotation.class));
		String result;
		try (StringWriter out = new StringWriter()) {
			new ConfigurationWriter(out).write("root", ConfigWithDisplayAnnotation.class, root);
			result = out.getBuffer().toString();
		}

		Document parsed = DOMUtil.parse(result);
		NodeList children = parsed.getDocumentElement().getChildNodes();
		assertEquals("Three properties are set.", 3, children.getLength());
		assertEquals(ConfigWithDisplayAnnotation.C_NAME + " is first in display order.",
			ConfigWithDisplayAnnotation.C_NAME, children.item(0).getLocalName());
		assertEquals(ConfigWithDisplayAnnotation.A_NAME + " is second in display order.",
			ConfigWithDisplayAnnotation.A_NAME, children.item(1).getLocalName());
		assertEquals(ConfigWithDisplayAnnotation.B_NAME + " is not contained in display order, but must be written.",
			ConfigWithDisplayAnnotation.B_NAME, children.item(2).getLocalName());
	}

	public void testImplClassDefaultRead() throws Exception {
		Map<String, ConfigurationDescriptor> descriptor = descriptor("a", WithImplClassDefault.class);

		WithImplClassDefault item = (WithImplClassDefault) fromXML(descriptor, "<a><b y=\"42\"></b></a>");

		assertInstanceof(item.getB(), C.Config.class);
		assertEquals(42, ((C.Config) item.getB()).getX());

		String xml = toXML(descriptor, item);
		assertEqualsXML("<a><b y=\"42\"></b></a>", xml);
	}

	public void testWriteDefaultValueOfDefaultDescriptorWhenSetExplicitly()
			throws Exception {
		Map<String, ConfigurationDescriptor> descriptor = descriptor("a", WithImplClassDefault.class);

		WithImplClassDefault item =
			(WithImplClassDefault) fromXML(descriptor, "<a><b class='" + B.class.getName() + "' w=\"42\"></b></a>");

		assertInstanceof(item.getB(), B.Config.class);
		assertEquals(42, ((B.Config) item.getB()).getW());

		String xml = toXML(descriptor, item);
		assertEqualsXML("<a><b class='" + B.class.getName() + "' w=\"42\"></b></a>", xml);
	}

	public void testImplClassDefaultConstructed() throws XMLStreamException {
		Map<String, ConfigurationDescriptor> descriptor = descriptor("a", WithImplClassDefault.class);

		WithImplClassDefault item = create(WithImplClassDefault.class);
		C.Config c = create(C.Config.class);
		assertEquals(C.class, c.getImplementationClass());
		c.setY(42);
		item.setB(c);

		assertInstanceof(item.getB(), C.Config.class);
		assertEquals(42, ((C.Config) item.getB()).getX());

		String xml = toXML(descriptor, item);
		assertEqualsXML("<a><b y=\"42\"></b></a>", xml);
	}

	private Map<String, ConfigurationDescriptor> descriptor(String tag, Class<WithImplClassDefault> type) {
		return Collections.singletonMap(tag, TypedConfiguration.getConfigurationDescriptor(type));
	}

	public void testNoInterfaceAnnotationOnTagName() throws Exception {
		TestConfig testConfig = TypedConfiguration.newConfigItem(TagNameTest.TestConfig.class);
		testConfig.getConfigs().add(TypedConfiguration.newConfigItem(TagNameTest.NamedConfig.class));
		testConfig.getConfigs().add(TypedConfiguration.newConfigItem(TagNameTest.UnnamedConfig.class));
		testConfig.getPolymorphic()
			.add(TypedConfiguration.newConfigItem(TagNameTest.NamedPolymorphicConfig.class));
		testConfig.getPolymorphic()
			.add(TypedConfiguration.newConfigItem(TagNameTest.UnnamedPolymorphicConfig.class));

		assertEquals(list(TagNameTest.INSTANCE, TagNameTest.INSTANCE),
			TypedConfiguration.getInstanceList(context, testConfig.getPolymorphic()));

		Document reparsedConfig = DOMUtil.parse(serializeItem(testConfig));
		NodeList configs =
			reparsedConfig.getDocumentElement().getElementsByTagName(TestConfig.CONFIG_NAME).item(0).getChildNodes();
		Node namedConfig = configs.item(0);
		assertEquals(TagNameTest.NamedConfig.NAMED_CONFIG_TAG, namedConfig.getLocalName());
		assertNull(namedConfig.getAttributes().getNamedItemNS(ConfigurationSchemaConstants.CONFIG_NS,
			ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR));
		Node unnamedConfig = configs.item(1);
		assertEquals(TestConfig.ENTRY, unnamedConfig.getLocalName());
		assertNotNull(unnamedConfig.getAttributes().getNamedItemNS(ConfigurationSchemaConstants.CONFIG_NS,
			ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR));
		NodeList polymorphicConfigs =
			reparsedConfig.getDocumentElement().getElementsByTagName(TestConfig.POLYMORPHIC_NAME).item(0)
				.getChildNodes();
		Node namedPolyConfig = polymorphicConfigs.item(0);
		assertEquals(TagNameTest.NamedPolymorphicConfig.NAMED_POLYMORPHIC_TAG, namedPolyConfig.getLocalName());
		assertNull("Ticket #23708: No need to write config interface annotation.",
			namedPolyConfig.getAttributes().getNamedItemNS(ConfigurationSchemaConstants.CONFIG_NS,
				ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR));
		Node unnamedPolyConfig = polymorphicConfigs.item(1);
		assertEquals(TestConfig.ENTRY, unnamedPolyConfig.getLocalName());
		assertNotNull(unnamedPolyConfig.getAttributes().getNamedItemNS(ConfigurationSchemaConstants.CONFIG_NS,
			ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR));
	}

	@Override
	protected String prettyPrintSerialized(String serializedA) {
		return XMLPrettyPrinter.prettyPrint(serializedA);
	}

	@Override
	protected String writeConfigurationItem(String localName, ConfigurationDescriptor staticType, ConfigurationItem item) throws XMLStreamException {
		StringWriter buffer = new StringWriter();
		new ConfigurationWriter(buffer).write(localName, staticType, item);
		return buffer.toString();
	}
	
	@Override
	protected ConfigurationItem readConfigItem(String localName,
			ConfigurationDescriptor expectedType, CharacterContent content) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap(localName, expectedType);
		return new ConfigurationReader(context, globalDescriptors).setSource(content).read();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestXMLConfigurationWriter.class));
	}

}

