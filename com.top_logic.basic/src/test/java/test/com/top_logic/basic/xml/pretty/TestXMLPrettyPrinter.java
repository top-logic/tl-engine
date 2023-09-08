/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml.pretty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.xml.AbstractXMLFixtures;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;

/**
 * Test case for {@link XMLPrettyPrinter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXMLPrettyPrinter extends BasicTestCase {

	private Config _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = XMLPrettyPrinter.newConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		_config = null;
		super.tearDown();
	}

	public void testXMLPrettyPrinterConfiguration() throws AssertionFailedError, IOException, ConfigurationException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		ConfigurationDescriptor prettyPrinterDescr =
			TypedConfiguration.getConfigurationDescriptor(XMLPrettyPrinter.Config.class);
		Map<String, ConfigurationDescriptor> globalConfigs = Collections.singletonMap("root", prettyPrinterDescr);
		ConfigurationReader reader = new ConfigurationReader(context, globalConfigs);
		reader.setSource(new ClassRelativeBinaryContent(TestXMLPrettyPrinter.class, "testPrettyPrinterConfiguration.xml"));
		_config = (Config) reader.read();

		assertPretty("testPrettyPrinterConfiguration.xml");
	}

	public void testAdditionalNamespaces() throws IOException {
		_config
			.setAdditionalNamespaces(Collections.singletonMap("additionalPrefix", "http://example.com/additionalNS"));
		assertPretty("testAdditionalNamespace.xml", "testAdditionalNamespace.in.xml");
	}

	public void testNoPretty() throws IOException {
		_config.setNoIndent(true);
		assertPretty("testNoIndent.xml", "testNoIndent.in.xml");
	}

	public void testSpaceIndent() throws IOException {
		_config.setIndentChar(' ');
		_config.setIndentStep(2);
		assertPretty("testSpaceIndent.xml", "testSpaceIndent.in.xml");
	}

	public void testNoAttribute() throws IOException {
		_config.setNoAttributeIndent(true);
		assertPretty("testNoAttributeIndent.xml", "testNoIndent.in.xml");
	}

	public void testNoXMLHeader() throws IOException {
		_config.setXMLHeader(false);
		assertPretty("testNoXMLHeader.xml", "testNoXMLHeader.in.xml");
	}

	public void testNoAttributeNoXMLHeader() throws IOException {
		_config.setXMLHeader(false);
		_config.setNoAttributeIndent(true);
		assertPretty("testNoXMLHeaderNoAttributeIndent.xml", "testNoIndent.in.xml");
	}

	public void testStructure() throws IOException {
		assertPretty("testStructure.xml");
	}

	public void testIdAttribute() throws IOException {
		assertPretty("testIdAttribute.xml");
	}

	public void testOneNamespace() throws IOException {
		assertPretty("testOneNamespace.xml");
	}

	public void testMultipleNamespace() throws IOException {
		assertPretty("testMultipleNamespace.xml");
	}

	public void testTextContent() throws IOException {
		assertPretty("testTextContent.xml", "testTextContent.in.xml");
	}

	public void testTextContentFull() throws IOException {
		_config.setCompactSingleLineText(false);
		assertPretty("testTextContentFull.xml", "testTextContentFull.in.xml");
	}

	public void testComment() throws IOException {
		assertPretty("testComment.xml", "testComment.in.xml");
	}

	public void testCData() throws IOException {
		assertPretty("testCData.xml");
	}

	public void testSections() throws IOException {
		assertPretty("testSections.xml", "testSections.in.xml");
	}

	public void testNoNamespace() throws IOException, ParserConfigurationException {
		assertPrettyNoNamespace("testNoNamespace.xml", "testNoNamespace.in.xml");
	}

	public void testEmptyAttributeFilter() throws IOException {
		_config.setEmptyTags(new SetFilter(new HashSet<>(Arrays.asList("c"))));
		assertPretty("testEmptyAttributeFilter.xml", "testEmptyAttributeFilter.in.xml");
	}

	public void testTextStructure() throws IOException {
		_config.setStructuredText(true);
		assertPretty("testTextStructure.xml", "testTextStructure.in.xml");
	}

	public void testDefaultNamespace() throws IOException {
		assertPretty("testDefaultNamespace.xml", "testDefaultNamespace.in.xml");
	}

	public void testQuoting() throws IOException {
		assertPretty("testQuoting.xml", "testQuoting.in.xml");
	}

	public void testDumpEncoding() {
		Document document = DOMUtil.newDocument();
		Element r = document.createElement("ä");
		// To minimise output
		_config.setXMLHeader(false);

		_config.setEncoding(StringServices.UTF8);
		assertEquals("<ä/>", XMLPrettyPrinter.prettyPrint(_config, r));
		_config.setEncoding("utf-16");
		assertEquals("<ä/>", XMLPrettyPrinter.prettyPrint(_config, r));
	}

	public void testNamespaceAssignment() throws IOException {
		Document document = DOMUtil.newDocument();
		Element r = document.createElementNS("foo", "x:a");
		document.appendChild(r);
		r.appendChild(document.createElementNS("bar", "ns1:b"));
		r.appendChild(document.createElementNS("bar", "b"));
		r.appendChild(document.createElementNS("foobar", "b"));
		assertPretty("testNamespaceAssignment.xml", document);
	}

	public void testAttributePrefixAssignment() throws IOException {
		Document document = DOMUtil.newDocument();
		Element r = document.createElementNS("http://foo", "a");
		document.appendChild(r);
		r.setAttributeNS("http://foo", "b", "value");
		assertPretty("testAttributePrefixAssignment.xml", document);
	}

	private void assertPretty(String fixtureName) throws AssertionFailedError, IOException {
		assertPretty(fixtureName, fixtureName);
	}

	private void assertPretty(String expectedName, String inputName) throws AssertionFailedError, IOException {
		assertPretty(DOMUtil.getDocumentBuilder(), expectedName, inputName);
	}

	private void assertPrettyNoNamespace(String expectedName, String inputName)
			throws AssertionFailedError, IOException, ParserConfigurationException {
		DocumentBuilder builder = DOMUtil.newDocumentBuilder();
		assertPretty(builder, expectedName, inputName);
	}

	private void assertPretty(DocumentBuilder builder, String expectedName, String inputName)
			throws AssertionFailedError, IOException, UnsupportedEncodingException {
		Document document = AbstractXMLFixtures.getDocument(TestXMLPrettyPrinter.class, builder, inputName);
		assertPretty(expectedName, document);
	}

	private void assertPretty(String expectedName, Document document) throws IOException, UnsupportedEncodingException {
		String result = toString(document);
		String orig =
			StreamUtilities.readAllFromStream(AbstractXMLFixtures.getStream(TestXMLPrettyPrinter.class, expectedName),
				_config.getEncoding());

		assertEquals(orig, result);
	}

	private String toString(Document document) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (XMLPrettyPrinter printer = new XMLPrettyPrinter(out, _config)) {
			printer.write(document);
		}
		return new String(out.toByteArray(), _config.getEncoding());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestXMLPrettyPrinter}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestXMLPrettyPrinter.class);
	}

}
