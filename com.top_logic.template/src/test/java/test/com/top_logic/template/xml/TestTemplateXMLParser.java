/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml;

import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.template.expander.TemplateExpander;
import com.top_logic.template.xml.TemplateSchema;
import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateByteArraySource;

/**
 * Test case for {@link TemplateXMLParser}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTemplateXMLParser extends TestCase {
	
	private static final String NS = " xmlns:t=" + quote(TemplateSchema.TEMPLATE_NS) + " xmlns:a=" + quote(TemplateSchema.ANNOTATION_NS);

	private static final String HEAD = "<t:head><t:settings output-format='text' /><t:parameters>" +
		"<t:parameter name='bool' type='boolean' />" +
		"<t:parameter name='str' type='string' />" +
		"<t:parameter name='strWithDefault' type='string' default='str-default-value' />" +
		"<t:parameter name='values' type='string' multiple='true' />" +
		"</t:parameters></t:head>";

	public void testEmptyInput() {
		BasicTestCase.executeInDefaultLocale(Locale.UK, () -> {
			try {
				doTest("", "");
			} catch (RuntimeException ex) {
				if (ex.getMessage().contains("Premature end of file.")) {
					return null;
				} else {
					throw (AssertionFailedError) new AssertionFailedError(
						"Expanding empty string failed with unexpected message!").initCause(ex);
				}
			} catch (IOException ex) {
				throw new IOError(ex);
			} catch (XMLStreamException ex) {
				throw new RuntimeException(ex);
			}
			fail("Expanding empty string should fail but did not!");
			return null;
		});

	}

	public void testExpandToEmptyFile() throws IOException, XMLStreamException {
		doTest(encloseInBoilerplate(""), "");
	}

	public void testSingleNode() throws IOException, XMLStreamException {
		String xml = "<r/>";
		doTest(encloseInBoilerplate(xml), "<r/>");
	}

	public void testForeach() throws IOException, XMLStreamException {
		String xml = "<r><t:foreach var='n' values='$values'>text</t:foreach></r>";
		doTest(encloseInBoilerplate(xml), "<r>texttexttext</r>");
	}
	
	public void testIf() throws IOException, XMLStreamException {
		String xml = "<r><t:if condition='$bool'><t:then>text</t:then></t:if></r>";
		doTest(encloseInBoilerplate(xml), "<r>text</r>");
	}
	
	public void testElse() throws IOException, XMLStreamException {
		String xml = "<r><t:if condition='$bool'><t:else>text</t:else></t:if></r>";
		doTest(encloseInBoilerplate(xml), "<r></r>");
	}
	
	public void testIfElse() throws IOException, XMLStreamException {
		String xml = "<t:if condition='$bool'> <t:then><foo/></t:then> <t:else><bar/></t:else> </t:if>";
		doTest(encloseInBoilerplate(xml), "<foo/>");
	}

	public void testValue() throws IOException, XMLStreamException {
		String xml = "<r><t:value expr='$str'/></r>";
		doTest(encloseInBoilerplate(xml), "<r>hello</r>");
	}
	
	public void testDefaultValue() throws IOException, XMLStreamException {
		String xml = "<r><t:value expr='$strWithDefault'/></r>";
		doTest(encloseInBoilerplate(xml), "<r>str-default-value</r>");
	}

	public void testDefine() throws IOException, XMLStreamException {
		String xml = "<t:define var='v' expr='$str'/><r><t:value expr='v'/></r>";
		doTest(encloseInBoilerplate(xml), "<r>hello</r>");
	}
	
	/**
	 * Test if a namespace declaration is copied to the template expansion result.
	 */
	public void testNamespaceDeclaration() throws IOException, XMLStreamException {
		String content = "<r xmlns:example=\"Example-Namespace\"/>";
		String expanded = expand(encloseInBoilerplate(content));
		if (expanded.equals(content)) {
			return;
		} else if (expanded.equals("<r/>")) {
			fail("Namespace declarations are not copied to expansion result!");
		} else {
			assertEquals(content, expanded);
		}
	}

	/**
	 * Test if a namespace declaration for the default/empty namespace is copied correctly to the
	 * template expansion result.
	 */
	public void testDefaultNamespaceDeclaration() throws IOException, XMLStreamException {
		String content = "<r xmlns=\"Example-Namespace\"/>";
		String expanded = expand(encloseInBoilerplate(content));
		if (expanded.equals(content)) {
			return;
		} else if (expanded.equals("<r/>")) {
			fail("Namespace declarations are not copied to expansion result!");
		} else {
			assertEquals(content, expanded);
		}
	}

	/**
	 * Test if comments are copied to the template expansion result.
	 */
	public void testComment() throws IOException, XMLStreamException {
		String xml = encloseInBoilerplate("<!-- A --><!-- B --><r><!-- C --></r><!-- D --><!-- E -->");
		String expanded = expand(xml);
		String expected = "<!-- A --><!-- B --><r><!-- C --></r><!-- D --><!-- E -->";
		if (expanded.equals(expected)) {
			return;
		} else if (expanded.equals("<r/>") || expanded.equals("<r></r>")) {
			fail("XML comments are not copied to expansion result!");
		} else {
			assertEquals(expected, expanded);
		}
	}

	private static final String encloseInBoilerplate(String templateBody) {
		return "<t:template" + NS + ">" + HEAD + "<t:body>" + templateBody + "</t:body></t:template>";
	}

	void doTest(String xml, String expected) throws IOException, XMLStreamException {
		assertEquals(expected, expand(xml));
	}

	private String expand(String xml) throws IOException, XMLStreamException {
		TemplateByteArraySource templateSource =
			new TemplateByteArraySource(xml.getBytes());
		return TemplateExpander.expandXmlTemplate(templateSource, createVariableValues());
	}

	private Map<String, Object> createVariableValues() {
		Map<String, Object> variableValues = new HashMap<>();
		variableValues.put("bool", true);
		variableValues.put("str", "hello");
		variableValues.put("values", Arrays.asList("entry1", "entry2", "entry3"));
		return variableValues;
	}

	private static String quote(String value) {
		return '"' + value + '"';
	}

}
