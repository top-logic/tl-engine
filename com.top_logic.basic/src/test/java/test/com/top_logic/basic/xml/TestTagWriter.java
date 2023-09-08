/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.character.NonClosingProxyWriter;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.TagWriter.State;


/**
 * Testcase for the {@link com.top_logic.basic.xml.TagWriter}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestTagWriter extends TestCase {
    
	private static final String SCRIPT_TAG = "stag";

	private static final String SCRIPT_ATTRIBUTE = "sattr";

	private static final String SCRIPT_ATTR_START = "<" + SCRIPT_TAG + " " + SCRIPT_ATTRIBUTE + "=\"";

	private static final String SCRIPT_ATTR_STOP = "\"/>";

	private static final String SCRIPT_START = "<script type=\"text/javascript\">\n// <![CDATA[\n";

	private static final String SCRIPT_STOP = "\n// ]]>\n</script>";

	private static final String QUOTED_SCRIPT_STOP = "\n// ]]" + "]]><![CDATA[" + ">\n</script>";

	private String encoding = "utf-8";

    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestTagWriter (String name) {
        super (name);
    }
    
	public void testNoIndentByDefault() {
    	TagWriter tw = new TagWriter();
    	
    	tw.beginTag("a");
    	tw.beginTag("b");
    	tw.endTag("b");
    	tw.endTag("a");
    	
    	assertEquals("<a><b></b></a>", tw.toString());
    }
    
	public void testNoIndentByDefaultAttributes() {
        TagWriter tw = new TagWriter();
        
        tw.beginBeginTag("a");
        tw.endBeginTag();
        tw.beginBeginTag("b");
        tw.endEmptyTag();
        tw.endTag("a");
        
        assertEquals("<a><b/></a>", tw.toString());
    }

	@SuppressWarnings("deprecation")
	public void testWriteContent() throws IOException {
    	TagWriter tw = new TagWriter();
    	
    	tw.beginBeginTag("a");
    	tw.endBeginTag();
    	tw.writeContent("<b>this is dangerous</b>");
    	tw.endTag("a");
    	
    	assertEquals("<a><b>this is dangerous</b></a>", tw.toString());
    }

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ELEMENT_CONTENT}. Test for method: {@link TagWriter#write(String, int, int)}
	 */
	public void testTextWriterRegion() throws IOException {
		TagWriter out = new TagWriter();
		out.write("123456789", 3, 4);
		out.flush();
		out.close();
		assertEquals("4567", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ELEMENT_CONTENT}. Test for method: {@link TagWriter#write(String)}
	 */
	public void testTextWriterFull() throws IOException {
		TagWriter out = new TagWriter();
		out.write("123456789");
		out.flush();
		out.close();
		assertEquals("123456789", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ELEMENT_CONTENT}. Test if text is encoded.
	 */
	public void testTextWriterEncoding() throws IOException {
		TagWriter out = new TagWriter();
		out.write("<&>");
		out.flush();
		out.close();
		assertEquals("&lt;&amp;&gt;", out.toString());
	}

	public void testAttributeQuoting() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("foo");
		out.writeAttribute("a", "\"x\"");
		out.writeAttribute("b", "'x'");
		out.writeAttribute("c", "<");
		out.writeAttribute("d", ">");
		out.writeAttribute("e", "&");
		out.endEmptyTag();
		out.flush();
		out.close();
		assertEquals("<foo a=\"&quot;x&quot;\" b=\"'x'\" c=\"&lt;\" d=\">\" e=\"&amp;\"/>", out.toString());
	}

	public void testAttributeWithLinefeed() throws IOException {
		assertAttributeQuoting("AAA\nZZZ", "AAA&#10;ZZZ");
	}

	public void testAttributeWithCarriageReturn() throws IOException {
		assertAttributeQuoting("AAA\rZZZ", "AAA&#13;ZZZ");
	}

	public void testAttributeWithTab() throws IOException {
		assertAttributeQuoting("AAA\tZZZ", "AAA&#9;ZZZ");
	}
	private void assertAttributeQuoting(String originalValue, String quotedValue) throws IOException {
		var tag = "test-tag";
		var attributeName = "test-attribute";
		var expectedString = "<" + tag + " " + attributeName + "=\"" + quotedValue + "\"/>";
		var actualString = writeEmptyTagWithAttribute(tag, attributeName, originalValue);
		assertEquals(expectedString, actualString);
	}

	private String writeEmptyTagWithAttribute(String tag, String attributeName, String attributeValue)
			throws IOException {
		try (var out = new TagWriter()) {
			out.emptyTag(tag, attributeName, attributeValue);
			out.flush();
			return out.toString();
		}
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ATTRIBUTE}. Test for method: {@link TagWriter#write(String, int, int)}
	 */
	public void testAttributeWriterRegion() throws IOException {
		doTestAttributeWriter("1", out -> {
			try {
				out.append('1');
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("23", out -> {
			try {
				out.append("23");
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("4", out -> {
			try {
				out.write('4');
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("56", out -> {
			try {
				out.write("56");
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("78", out -> {
			try {
				out.write("---78----", 3, 2);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("a", out -> out.writeText('a'));

		doTestAttributeWriter("bc", out -> out.writeText("bc"));
		doTestAttributeWriter("bc&amp;de", out -> {
			try {
				out.writeContent("bc&amp;de");
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("def", out -> {
			try {
				out.writeText("---def----".toCharArray(), 3, 3);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("'abc'", out -> {
			try {
				out.writeJsLiteral("abc");
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});

		doTestAttributeWriter("'x'", out -> {
			try {
				out.beginJsString();
				out.writeJsStringContent('x');
				out.endJsString();
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});
	}

	private void doTestAttributeWriter(String expected, Consumer<TagWriter> code) throws IOException {
		doTestAttributeWriterRegular(expected, code);
		doTestAttributeWriterBoolean(expected, code);
		doTestAttributeWriterEndAllRegular(expected, code);
		doTestAttributeWriterEndAllBoolean(expected, code);
	}

	private void doTestAttributeWriterRegular(String expected, Consumer<TagWriter> code) throws IOException {
		String result;
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attribute");
			code.accept(out);
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");
			out.flush();
			result = out.toString();
		}
		doCheckAttributeWriter(expected, result);
	}

	private void doTestAttributeWriterBoolean(String expected, Consumer<TagWriter> code) throws IOException {
		String result;
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attribute");
			code.accept(out);
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");
			out.flush();
			result = out.toString();
		}
		doCheckAttributeWriterBoolean(expected, result);
	}

	private void doTestAttributeWriterEndAllRegular(String expected, Consumer<TagWriter> code) throws IOException {
		String result;
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attribute");
			code.accept(out);
			out.endAll();
			out.flush();
			out.close();
			result = out.toString();
		}
		doCheckAttributeWriter(expected, result);
	}

	private void doCheckAttributeWriter(String expected, String result) {
		assertEquals("<tag attribute=\"" + expected + "\"></tag>", result);
	}

	private void doTestAttributeWriterEndAllBoolean(String expected, Consumer<TagWriter> code) throws IOException {
		String result;
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attribute");
			code.accept(out);
			out.endAll();
			out.flush();
			out.close();
			result = out.toString();
		}
		doCheckAttributeWriterBoolean(expected, result);
	}

	private void doCheckAttributeWriterBoolean(String expected, String result) {
		if (expected.isEmpty()) {
			assertEquals("<tag></tag>", result);
		} else {
			doCheckAttributeWriter(expected, result);
		}
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ATTRIBUTE}. Test for method: {@link TagWriter#write(String)}
	 */
	public void testAttributeWriterFull() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attribute");
		out.write("123456789");
		out.endAll();
		out.flush();
		out.close();
		assertEquals("<tag attribute=\"123456789\"></tag>", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#ATTRIBUTE}. Test if text is encoded.
	 */
	public void testAttributeWriterEncoding() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attribute");
		out.write("<&>'\"");
		out.endAll();
		out.flush();
		out.close();
		assertEquals("<tag attribute=\"&lt;&amp;>'&quot;\"></tag>", out.toString());
	}

	public void testCDATA() throws IOException {
		TagWriter out = new TagWriter();
		beginCDATA(out);
		out.writeCDATAContent("foo");
		out.writeCDATAContent("bar");
		out.writeCDATAContent("");
		endCDATA(out);
		assertCDATAContents(out.toString(), "foobar");
		assertEquals("<a><![CDATA[foobar]]></a>", out.toString());
	}

	public void testCDATAEscape() throws IOException {
		TagWriter out = new TagWriter();
		beginCDATA(out);
		out.writeCDATAContent("foo");
		out.writeCDATAContent("]]>]]>]]>");
		out.writeCDATAContent("bar");
		endCDATA(out);
		assertCDATAContents(out.toString(), "foo]]>]]>]]>bar");
		String escape = "]]><![CDATA[";
		assertEquals("<a><![CDATA[foo]]" + escape + ">]]" + escape + ">]]" + escape + ">bar]]></a>", out.toString());
	}

	public void testCDATAEscapeChunkBorders() throws IOException {
		TagWriter out = new TagWriter();
		beginCDATA(out);
		out.writeCDATAContent("foo");
		out.writeCDATAContent("]");
		out.writeCDATAContent("]>]]");
		out.writeCDATAContent(">]]>");
		out.writeCDATAContent("bar");
		endCDATA(out);
		assertCDATAContents(out.toString(), "foo]]>]]>]]>bar");
		String escape = "]]><![CDATA[";
		// Note: This result reflects the existing situation, even if the quoting strategy applied
		// is not optimal.
		assertEquals("<a><![CDATA[foo]" + escape + "]>]]" + escape + ">]]" + escape + ">bar]]></a>", out.toString());
	}

	public void testCDATAEscapeSingleChar() throws IOException {
		TagWriter out = new TagWriter();
		beginCDATA(out);
		out.append("foo");
		out.append(']');
		out.append(']');
		out.append('>');
		out.append(']');
		out.append("]]>]]" + "]>" + "]]>]]", 5, 5 + 2);
		out.append("]]>");
		out.append("bar");
		endCDATA(out);
		assertCDATAContents(out.toString(), "foo]]>]]>]]>bar");
		String escape = "]]><![CDATA[";
		// Note: This result reflects the existing situation
		assertEquals("<a><![CDATA[foo]]" + escape + ">]" + escape + "]>]]" + escape + ">bar]]></a>",
			out.toString());
	}

	public void testCDATAEscapeWriteAPI() throws IOException {
		TagWriter out = new TagWriter();
		beginCDATA(out);
		out.write("foo");
		out.write(']');
		out.write(']');
		out.write('>');
		out.write(']');
		out.write("]]>]]" + "]>" + "]]>]]", 5, 2);
		out.write("]]>");
		out.write("bar");
		endCDATA(out);
		assertCDATAContents(out.toString(), "foo]]>]]>]]>bar");
		String escape = "]]><![CDATA[";
		// Note: This result reflects the existing situation.
		assertEquals("<a><![CDATA[foo]]" + escape + ">]" + escape + "]>]]" + escape + ">bar]]></a>",
			out.toString());
	}
	
	public void testCDATAQuoting() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("a");
		out.beginQuotedXML();
		out.beginBeginTag("b");
		out.writeAttribute("attr", "\"<&>\"");
		out.endBeginTag();
		out.writeText("<&>");
		out.endTag("b");
		out.endQuotedXML();
		out.endTag("a");

		checkCDATAQuoting(out);
	}

	public void testCDATAQuotingGenericWrite() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("a");
		out.beginQuotedXML();
		out.beginBeginTag("b");
		out.beginAttribute("attr");
		out.write("\"<&>\"");
		out.endAttribute();
		out.endBeginTag();
		out.write("<&>");
		out.endTag("b");
		out.endQuotedXML();
		out.endTag("a");

		checkCDATAQuoting(out);
	}

	private void checkCDATAQuoting(TagWriter out) {
		assertCDATAContents(out.toString(), "<b attr=\"&quot;&lt;&amp;>&quot;\">&lt;&amp;&gt;</b>");
		Document outer = DOMUtil.parse(out.toString());
		Document inner = DOMUtil.parse(outer.getDocumentElement().getTextContent());
		assertEquals("\"<&>\"", inner.getDocumentElement().getAttribute("attr"));
		assertEquals("<&>", inner.getDocumentElement().getTextContent());
	}

	public void testCDATAQuotingOfCDATA() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("a");
		out.beginQuotedXML();
		out.beginTag("b");
		out.beginCData();
		out.append("<<<>>>");
		out.append("<![CDATA[nested]]>");
		out.writeContent("<![CDATA[legacy]]>");
		out.endCData();
		out.endTag("b");
		out.endQuotedXML();
		out.endTag("a");

		checkCDATAQuotingOfCDATA(out);
	}

	private void checkCDATAQuotingOfCDATA(TagWriter out) {
		assertCDATAContents(out.toString(), "<b><![CDATA[<<<>>><![CDATA[nested]]><![CDATA[legacy]]>]]></b>");
	}

	public void testCDATAInAttribute() throws IOException {
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			out.beginTag("a");
			out.beginCData();
			try (TagWriter inner = new TagWriter(new NonClosingProxyWriter(out))) {
				inner.beginBeginTag("b");
				inner.beginAttribute("x");
				try (TagWriter attr = new TagWriter(new NonClosingProxyWriter(inner))) {
					attr.beginTag("c");
					attr.beginCData();
					attr.write("value ]]>");
					attr.endCData();
					attr.endTag("c");
				}
				inner.endAttribute();
				inner.endBeginTag();
				inner.endTag("b");
			}
			out.endCData();
			out.endTag("a");
		}

		String xml = buffer.toString();

		Element a = DOMUtil.parse(xml).getDocumentElement();
		assertEquals("a", a.getTagName());
		assertEquals("<b x=\"&lt;c>&lt;![CDATA[value ]]]]&gt;&lt;![CDATA[>]]&gt;&lt;/c>\"></b>",
			a.getFirstChild().getTextContent());
		assertNull(a.getFirstChild().getNextSibling());

		Element b = DOMUtil.parse(a.getFirstChild().getTextContent()).getDocumentElement();
		assertEquals("b", b.getTagName());
		assertEquals("<c><![CDATA[value ]]]]><![CDATA[>]]></c>", b.getAttribute("x"));

		Element c = DOMUtil.parse(b.getAttribute("x")).getDocumentElement();
		assertEquals("c", c.getTagName());
		assertEquals("value ]]>", c.getTextContent());
	}

	private void beginCDATA(TagWriter out) throws IOException {
		out.beginTag("a");
		out.beginCData();
	}

	private void endCDATA(TagWriter out) throws IOException {
		out.endCData();
		out.endTag("a");
	}

	private void assertCDATAContents(String xml, String expected) {
		Document document;
		try {
			document = DOMUtil.parse(xml);
		} catch (IllegalArgumentException ex) {
			throw (AssertionError) new AssertionError("Cannot parse '" + xml + "', expecting '" + expected
				+ "' as CDATA contents.").initCause(ex);
		}
		assertEquals(expected, document.getDocumentElement().getTextContent());
	}

	public void testNoCssClassAttribute() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

	public void testNoCssClassAttributeForNull() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.append(null);
		tagWriter.writeAttributeText(null);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

	public void testNoCssClassAttributeForEmptyString() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.append("");
		tagWriter.writeAttributeText("");
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

	public void testCssClassCharSequence() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.write(" \t");
		tagWriter.append("foo");
		tagWriter.append(null);
		tagWriter.append("");
		tagWriter.append(" \t");
		tagWriter.writeAttributeText(" bar \t");
		tagWriter.writeAttributeText(null);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag class=\"foo bar\"></tag>", tagWriter.toString());
	}

	public void testCssClassWriteContentEmpty() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.writeContent("");
		tagWriter.writeContent(" \t");
		tagWriter.writeContent(null);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

	public void testCssClassWriteContent() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.writeContent("");
		tagWriter.writeContent("foo");
		tagWriter.writeContent("");
		tagWriter.writeContent("bar");
		tagWriter.writeContent(null);
		tagWriter.writeContent("baz");
		tagWriter.writeContent(null);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag class=\"foo bar baz\"></tag>", tagWriter.toString());
	}

	public void testCssClassCharArray() throws IOException {
		char[] classes = { 'f', 'o', 'o', 'b', 'a', 'r' };
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.write(classes, 0, 3);
		tagWriter.writeAttributeText(classes, 3, 3);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag class=\"foo bar\"></tag>", tagWriter.toString());
	}

	public void testCssClassForEmptyCharArray() throws IOException {
		char[] classes = {};
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.write(classes, 0, 0);
		tagWriter.writeAttributeText(classes, 3, 0);
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

	public void testCssClassWithExplicitSeparator() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.append("foo");
		tagWriter.append(" ");
		tagWriter.append("bar");
		tagWriter.append(' ');
		tagWriter.append("bazz");
		tagWriter.endCssClasses();
		tagWriter.endBeginTag();
		tagWriter.endTag("tag");
		tagWriter.close();
		assertEquals("<tag class=\"foo bar bazz\"></tag>", tagWriter.toString());
	}

	public void testNoSingleCharInCssClass() throws IOException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.append('f');
			tagWriter.append('o');
			tagWriter.append('o');
			tagWriter.endCssClasses();
			tagWriter.endBeginTag();
			tagWriter.endTag("tag");
			tagWriter.close();
		} catch (IllegalStateException ex) {
			BasicTestCase.assertContains("single char", ex.getMessage());
		}
	}

	public void testNoMixedClassAndAttributeBeginAndEnd() throws IOException {
		TagWriter tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.endAttribute();
			fail("Must not allow mixed attribute and css class begin and end!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No attribute end in state " + State.CLASS_ATTRIBUTE_START, ex.getMessage());
		}

		tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginAttribute("class");
			tagWriter.endCssClasses();
			fail("Must not allow mixed attribute and css class begin and end!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No class attribute end in state " + State.ATTRIBUTE_START, ex.getMessage());
		}
	}

	public void testNoDoubleCssClassStart() throws IOException {
		TagWriter tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.beginCssClasses();
			fail("Must not allow begin of css class twice without calling end in between!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No class attribute in state " + State.CLASS_ATTRIBUTE_START, ex.getMessage());
		}
	}

	public void testNoDoubleCssClassEnd() throws IOException {
		TagWriter tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.endCssClasses();
			tagWriter.endCssClasses();
			fail("Must not allow end of css class twice without calling begin in between!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No class attribute end in state " + State.START_TAG, ex.getMessage());
		}
	}

	public void testNoCssClassBeginInAttributeWrite() throws IOException {
		TagWriter tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginAttribute("foo");
			tagWriter.beginCssClasses();
			fail("Must not allow begin of css class during attribute write!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No class attribute in state " + State.ATTRIBUTE_START, ex.getMessage());
		}

		tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginAttribute("foo");
			tagWriter.writeAttributeText("bar");
			tagWriter.beginCssClasses();
			fail("Must not allow begin of css class during attribute write!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No class attribute in state " + State.ATTRIBUTE, ex.getMessage());
		}
	}

	public void testNoAtributeBeginInCssClassWrite() throws IOException {
		TagWriter tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.beginAttribute("foo");
			fail("Must not allow begin of css class during attribute write!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No attribute in state " + State.CLASS_ATTRIBUTE_START, ex.getMessage());
		}
		tagWriter = new TagWriter();
		try {
			tagWriter.beginBeginTag("tag");
			tagWriter.beginCssClasses();
			tagWriter.writeAttributeText("bar");
			tagWriter.beginAttribute("foo");
			fail("Must not allow begin of css class during attribute write!");
		} catch (AssertionError ex) {
			tagWriter.close();
			assertEquals("No attribute in state " + State.CLASS_ATTRIBUTE, ex.getMessage());
		}
	}

	public void testPretty() throws IOException {
		String nl = "\n";
		TagWriter out = new TagWriter();
		out.setNewLine(nl);
		out.beginBeginTag("x");
		out.writeIndent();
		out.writeAttribute("y", "v1");
		out.writeIndent();
		out.writeAttribute("z", "v2");
		out.decreaseIndent();
		out.writeIndent();
		out.endBeginTag();
		out.increaseIndent();
		{
			out.writeIndent();
			out.beginComment();
			out.writeIndent();
			out.writeCommentContent("comment");
			out.writeIndent();
			out.endComment();
		}
		out.decreaseIndent();
		out.writeIndent();
		out.endTag("x");

		assertEquals(
			"<x" + nl +
			"   y=\"v1\"" + nl +
			"   z=\"v2\"" + nl +
			">" + nl +
			"  <!-- " + nl +
			"  comment" + nl +
			"   -->" + nl +
			"</x>", out.toString());
	}

	public void testCustomIndent() throws IOException {
		String nl = "\n";
		TagWriter out = new TagWriter();
		out.setNewLine(nl);
		out.beginTag("x");
		out.nl();
		out.indented().write("foo");
		out.nl();
		out.indented().write("bar");
		out.endTag("x");

		assertEquals(
			"<x>" + nl +
			"  foo" + nl +
			"  bar</x>", out.toString());
	}

	public void testScriptIndent() throws IOException {
		String nl = "\n";
		TagWriter out = new TagWriter();
		out.setNewLine(nl);
		out.beginScript();
		out.indented().write("var x = 1;");
		out.nl();
		out.indented().write("x++;");
		out.nl();
		out.endScript();

		assertEquals(
			SCRIPT_START +
				"  var x = 1;" + nl +
				"  x++;" + nl +
				SCRIPT_STOP, out.toString());
	}

	public void testPrinter() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("x");
		out.write("y");
		out.getPrinter().print(13);
		out.getPrinter().print("&");
		out.write("z");
		out.endTag("x");

		assertEquals("<x>y13&amp;z</x>", out.toString());
	}

	@SuppressWarnings("deprecation")
	public void testContentWriter() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("x");
		out.write("y");
		StringServices.append(out.contentWriter(), 13);
		out.contentWriter().write("&amp;");
		out.write("z");
		out.endTag("x");

		assertEquals("<x>y13&amp;z</x>", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#COMMENT_CONTENT}. Test for method: {@link TagWriter#write(String, int, int)}
	 */
	public void testCommentWriterRegion() throws IOException {
		TagWriter out = new TagWriter();
		out.beginComment();
		out.write("123456789", 3, 4);
		out.endComment();
		out.endAll();
		out.flush();
		out.close();
		assertEquals("<!-- 4567 -->", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#COMMENT_CONTENT}. Test for method: {@link TagWriter#write(String)}
	 */
	public void testCommentWriterFull() throws IOException {
		TagWriter out = new TagWriter();
		out.beginComment();
		out.write("123456789");
		out.endComment();
		out.endAll();
		out.flush();
		out.close();
		assertEquals("<!-- 123456789 -->", out.toString());
	}

	/**
	 * Tests for using the {@link TagWriter} as {@link Writer} when in {@link State}
	 * {@link State#COMMENT_CONTENT}. Test if text is encoded.
	 */
	public void testCommentWriterEncoding() throws IOException {
		TagWriter out = new TagWriter();
		out.beginComment();
		out.write("<&>'\"---");
		out.endComment();
		out.endAll();
		out.flush();
		out.close();
		assertEquals("<!-- <&>'\"- -->", out.toString());
	}

	@SuppressWarnings("deprecation")
	public void testWriteContentInAttributes() throws IOException {
    	TagWriter tw = new TagWriter();
    	
    	tw.beginBeginTag("a");
    	tw.writeContent("v1='this is'");
    	tw.writeContent("v2='dangerous'");
    	tw.endBeginTag();
    	tw.endTag("a");
    	
    	assertEquals("<a v1='this is' v2='dangerous'></a>", tw.toString());
    }
    
    /** Exepected Output to be created by writeTags(). */
    private final static String EXPECTED_TAGS =
        "\n"
      + "<gruml bool=\"true\" char=\"x\" int=\"99\" String=\"gnirtS\">\n"
      + "</gruml>\n"
      + "<grunt>\n"
      + "</grunt>\n"
      + "<a x=\"y\">\n"
      + "  <b><u>\n"
      + "      <empty/>\n"
      + "      <c y=\"x\"/>Just indented Text\n"
      + "      <em>Emphasis on this content</em>\n"
      + "      <hr class=\"inner\"/>\n"
      + "      <br/></u>\n"
      + "  </b>\n"
      + "</a>";
      
    /** 
     * helper function to create some tags using a TagWriter
     */
	@SuppressWarnings("deprecation")
	private void writeTags(TagWriter tw) throws Exception {
    	tw.setIndent(true);
        tw.beginBeginTag("gruml");
    	tw.setIndent(false);
        tw.writeAttribute("bool", true);
        tw.writeAttribute("char", 'x');
        tw.writeAttribute("int", 99);
        tw.writeAttribute("String", "gnirtS");
        tw.endBeginTag();
    	tw.setIndent(true);
        tw.endTag("gruml");
        tw.beginBeginTag("grunt");
    	tw.setIndent(false);
		tw.writeAttribute("char", (CharSequence) null);
        tw.endBeginTag();
    	tw.setIndent(true);
        tw.endTag("grunt");
        tw.beginBeginTag("a");
    	tw.setIndent(false);
        tw.writeAttribute("x", "y");
        tw.endBeginTag();
    	tw.setIndent(true);
        tw.beginTag("b");
    	tw.setIndent(false);
        tw.beginTag("u");
    	tw.setIndent(true);
        tw.beginBeginTag("empty");
    	tw.setIndent(false);
        tw.endEmptyTag();
        tw.writeIndent();
        tw.emptyTag("c", "y", "x");
        tw.writeContent("Just indented Text");
    	tw.setIndent(true);
        tw.beginTag("em");
    	tw.setIndent(false);
        tw.writeText("Emphasis on this content");
		tw.endTag("em");
    	tw.setIndent(true);
        tw.beginBeginTag("hr");
    	tw.setIndent(false);
        tw.writeAttribute("class", "inner");
        tw.endEmptyTag();
        tw.writeIndent();
        tw.emptyTag("br");
        tw.endTag("u");
    	tw.setIndent(true);
        tw.endTag("b");
        tw.endTag("a");
    	tw.setIndent(false);
    }
    
    /** 
     * Test default operation on StringWriter 
     */
    public void testNormal() throws Exception {
        // TagWriter tws = new TagWriter(System.out);
        // writeTags(tws);
        TagWriter tw = new TagWriter();
        writeTags(tw);
        assertEquals(EXPECTED_TAGS , tw.toString());
    }

    /** 
     * Test operation with Writer.
     */
    public void testWriter() throws Exception {
        File tmp = BasicTestCase.createTestFile("TestTagWriter", ".dat");
		FileOutputStream out = new FileOutputStream(tmp);
		try {
			TagWriter tw = new TagWriter(new OutputStreamWriter(out, encoding));
			tw.writeXMLHeader(encoding);
			writeTags(tw);
			tw.flush();
		} finally {
			out.close();
		}
        // System.out.println(tw.toString());
        assertTrue(tmp.delete());
    }
    
    /** 
     * Test That IOExceptions are thrown when needed.
     */
    public void testIOExceptions() throws Exception {
        File tmp = BasicTestCase.createTestFile("TestTagWriter", ".dat");
        FileWriter fw = new FileWriter(tmp);
        TagWriter  tw = new TagWriter(fw);
        fw.close();
        assertTrue(tmp.delete());
        
        /* As check errror does a flush we do not want this any more
        try {
        	tw.writeIndent();
            fail("Expected IOException here");
        }
        catch (IOException expected) { / * expected * / }
        */
        try {
            tw.flush();
            fail("Expected IOException here");
        }
        catch (IOException expected) { /* expected */ }

        tw.close();
    }
   
    /** 
     * Test operation with a PrintWriter.
     */
    public void testPrintriter() throws Exception {
        File        tmp = BasicTestCase.createTestFile("TestTagPrintWriter", ".txt");

		TagWriter tw;
		FileOutputStream out = new FileOutputStream(tmp);
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, encoding));
			tw = new TagWriter(pw);
			tw.writeXMLHeader(encoding);
			writeTags(tw);
			tw.flush();
		} finally {
			out.close();
		}
        // System.out.println(tw.toString());
        assertTrue(tmp.delete());
        assertNotNull(tw.toString());
    }

    /** 
     * Test operation with Stream.
     */
    public void testStream() throws Exception {
        File tmp = BasicTestCase.createTestFile("TestTagWriter", ".dat");
		TagWriter tw = TagWriter.newTagWriter(tmp, encoding);
        writeTags(tw);
        // System.out.println(tw.toString());
        tw.close();
        assertTrue(tmp.delete());
    }

    /** 
     * Tag Writer must coplian on all sort of illegal tags.
     */
	public void testExceptions() {
        try  {
            TagWriter tw = new TagWriter();
            tw.endTag("dontCare");
            fail("Expected TagException here");
        }
        catch (AssertionError expected)  {
        	// Expected.
        }
        try  {
            TagWriter tw = new TagWriter();
            tw.beginTag("this");
            tw.beginTag("other");
            tw.endTag  ("that");
            fail("Expected TagException here");
        }
        catch (AssertionError expected)  {
        	// Expected.
        }
        
        try  {
            TagWriter tw = new TagWriter();
            tw.endBeginTag();
            fail("Expected TagException here");
        }
        catch (AssertionError expected)  {
        	// Expected.
        }
        
            try {
                TagWriter tw = new TagWriter();
                tw.beginTag("this");
                tw.beginTag("other");
                tw.endTag  ("that");
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }
        
            try  {
                TagWriter tw = new TagWriter();
                tw.beginBeginTag("this");
                tw.beginBeginTag("other");
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.beginBeginTag("this");
                tw.beginBeginTag("other");
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.writeAttribute("must not","val");
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.writeAttribute("must not",'x');
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.writeAttribute("must not",false);
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.writeAttribute("must not",99);
                fail("Expected TagException here");
            }
            catch (AssertionError expected)  {
            	// Expected.
            }

            try  {
                TagWriter tw = new TagWriter();
                tw.endEmptyTag();
            }
            catch (AssertionError expected)  {
            	// Expected.
            }
    }

	/** Test for {@link TagWriter#endAll()} in state {@link State#ELEMENT_CONTENT}. */
	public void testEndAll() throws IOException {
		TagWriter out = new TagWriter();
		try {
			out.beginTag("a");
			int depth = out.getDepth();
			try {
				out.beginTag("b");
				out.beginTag("c");
				throw new Exception();
			} catch (Exception ex) {
				out.endAll(depth);
			}
			out.endTag("a");
		} finally {
			out.close();
		}

		assertEquals("<a><b><c></c></b></a>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#START_TAG}. */
	public void testEndAllInBeginTag() throws IOException {
		TagWriter out = new TagWriter();
		try {
			out.beginBeginTag("a");
			int depth = out.getDepth();
			try {
				out.writeAttribute("x", 1);
				
				// Control is aborted abnormally here.
				throw new Exception();
			} catch (Exception ex) {
				out.endAll(depth);
			}
			out.endTag("a");
		} finally {
			out.close();
		}

		assertEquals("<a x=\"1\"></a>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#ATTRIBUTE}. */
	public void testCloseAllInAttribute() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginAttribute("attribute");
		tagWriter.writeAttributeText("Attribute Value");
		tagWriter.endAll();
		tagWriter.close();
		assertEquals("<tag attribute=\"Attribute Value\"></tag>", tagWriter.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#COMMENT_CONTENT}. */
	public void testCloseAllInComment() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginTag("tag");
		tagWriter.beginComment();
		tagWriter.writeCommentContent("comment");
		tagWriter.endAll();
		tagWriter.close();
		assertEquals("<tag><!-- comment --></tag>", tagWriter.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#JS_ATTRIBUTE_STRING}. */
	public void testCloseAllInJsAttributeString() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		out.beginJsString();
		out.write("foo");
		out.endAll();
		out.close();
		assertEquals("<tag attr=\"'foo'\"></tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#JS_ELEMENT_STRING}. */
	public void testCloseAllInJsElementString() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("tag");
		out.beginJsString();
		out.write("foo");
		out.endAll();
		out.close();
		assertEquals("<tag>'foo'</tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#JS_CDATA_STRING}. */
	public void testCloseAllInJsCDATAString() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("tag");
		out.beginCData();
		out.beginJsString();
		out.write("foo");
		out.endAll();
		out.close();
		assertEquals("<tag><![CDATA['foo']]></tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in state {@link State#CDATA_CONTENT}. */
	public void testCloseAllInCDATA() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("tag");
		out.beginCData();
		out.write("foo");
		out.endAll();
		out.close();
		assertEquals("<tag><![CDATA[foo]]></tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in quoted XML. */
	public void testCloseAllInQuotedXML() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("tag");
		out.beginQuotedXML();
		out.beginTag("a");
		out.beginTag("b");
		out.endAll();
		out.close();
		assertEquals("<tag><![CDATA[<a><b></b></a>]]></tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in {@link State#SCRIPT}. */
	public void testCloseAllInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("tag");
		out.beginScript();
		out.write("foo();");
		out.endAll();
		out.close();
		assertEquals("<tag>" + SCRIPT_START + "foo();" + SCRIPT_STOP + "</tag>", out.toString());
	}

	/** Test for {@link TagWriter#endAll()} in {@link State#CLASS_ATTRIBUTE}. */
	public void testCloseAllInCssClasses() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.writeAttributeText("Attribute Value");
		tagWriter.endAll();
		tagWriter.close();
		assertEquals("<tag class=\"Attribute Value\"></tag>", tagWriter.toString());
	}

	/** Test for {@link TagWriter#endAll()} in {@link State#CLASS_ATTRIBUTE_START}. */
	public void testCloseAllInCssClassStart() throws IOException {
		TagWriter tagWriter = new TagWriter();
		tagWriter.beginBeginTag("tag");
		tagWriter.beginCssClasses();
		tagWriter.endAll();
		tagWriter.close();
		assertEquals("<tag></tag>", tagWriter.toString());
	}

    public void testGetStack() {
    	TagWriter out = new TagWriter();
    	out.beginTag("first");
    	out.beginTag("second");
    	
		Stack<String> stack = out.getStack();
    	assertEquals("second",stack.pop());
    	assertEquals("first", stack.pop());
    	assertTrue(stack.isEmpty());
    	
    	// test that the original stack is not touched.
    	stack = out.getStack();
    	assertTrue(stack.contains("first"));
    	assertTrue(stack.contains("second"));
    	
    	out.endTag("second");
    	stack = out.getStack();
    	assertEquals("first",stack.pop());
    }
    
	public void testToManyIndents() {
    	String tag = "span";
    	
    	TagWriter out = new TagWriter();
    	try {
    		for (int index = 0; index < 100; index++) {
    			out.beginTag(tag);
    		}
    	} catch (IndexOutOfBoundsException ex) {
    		BasicTestCase.fail("#2987: To many indents causes Exception", ex);
    	}
    	out.writeText("content");
    	for (int index = 0; index < 100; index++) {
    		out.endTag(tag);
    	}
    }

	public void testWriteCommentPlain() {
		StringWriter internalWriter = new StringWriter();
		TagWriter writer = new TagWriter(internalWriter);
		writer.writeCommentPlain("123");
		assertEquals("<!--123-->", internalWriter.getBuffer().toString());
	}

	public void testWriteCommentPlain_SinlgeMinuses() {
		StringWriter internalWriter = new StringWriter();
		TagWriter writer = new TagWriter(internalWriter);
		writer.writeCommentPlain("1-2-3");
		assertEquals("<!--1-2-3-->", internalWriter.getBuffer().toString());
	}

	public void testWriteCommentPlain_FailOnStartMinus() {
		StringWriter internalWriter = new StringWriter();
		TagWriter writer = new TagWriter(internalWriter);
		try {
			writer.writeCommentPlain("--123");
		} catch (AssertionError ex) {
			return;
		}
		fail("TagWriter did not throw an error on illegal input!");
	}

	public void testWriteCommentPlain_FailOnEndMinus() {
		StringWriter internalWriter = new StringWriter();
		TagWriter writer = new TagWriter(internalWriter);
		try {
			writer.writeCommentPlain("123-");
		} catch (AssertionError ex) {
			return;
		}
		fail("TagWriter did not throw an error on illegal input!");
	}

	public void testWriteCommentPlain_FailOnMinusMinus() {
		StringWriter internalWriter = new StringWriter();
		TagWriter writer = new TagWriter(internalWriter);
		try {
			writer.writeCommentPlain("1--2");
		} catch (AssertionError ex) {
			return;
		}
		fail("TagWriter did not throw an error on illegal input!");
	}

	public void testNonPrintableText() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("foo");
		out.write("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008a");
		out.write("\u000B\u000Cb");
		out.write("\u000E\u000Fc");
		out.write("\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001Fd");
		out.endTag("foo");

		assertEquals("<foo>abcd</foo>", out.toString());
	}

	public void testNonPrintableAttribute() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("foo");
		out.beginAttribute("bar");
		out.write("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008a");
		out.write("\u000B\u000Cb");
		out.write("\u000E\u000Fc");
		out.write("\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001Fd");
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<foo bar=\"abcd\"/>", out.toString());
	}

	public void testNonPrintableJSText() throws IOException {
		TagWriter out = new TagWriter();
		out.beginTag("foo");
		out.beginJsString();
		out.write("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008");
		out.write("\u0009\n");
		out.write("\u000B\u000C");
		out.write("\r");
		out.write("\u000E\u000F");
		out.write("\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F");
		out.endJsString();
		out.endTag("foo");

		assertEquals(
			"<foo>'"+ 
			"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\u0008\\t\\n\\u000B\\u000C\\r\\u000E\\u000F" + 
			"\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001A\\u001B\\u001C\\u001D\\u001E\\u001F" + 
			"'</foo>",
			out.toString());
	}

	public void testNonPrintableJSScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.beginJsString();
		out.write("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008");
		out.write("\u0009\n");
		out.write("\u000B\u000C");
		out.write("\r");
		out.write("\u000E\u000F");
		out.write("\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F");
		out.endJsString();
		out.endScript();
		
		assertEquals(
			SCRIPT_START + "'"+ 
			"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\u0008\\t\\n\\u000B\\u000C\\r\\u000E\\u000F" + 
			"\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001A\\u001B\\u001C\\u001D\\u001E\\u001F" + 
			"'" + SCRIPT_STOP,
			out.toString());
	}
	
	public void testScriptEmpty() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.endScript();

		assertEquals(SCRIPT_START + SCRIPT_STOP, out.toString());
	}

	public void testWriteScriptInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = true && true ? ");
		out.writeJsLiteral("<'&'foo>");
		out.writeScript(" : ".toCharArray(), 0, 3);
		out.writeJsLiteral(null);
		out.writeScript(';');
		out.endScript();

		assertEquals(
			SCRIPT_START + "var x = true && true ? " + "'\\u003C\\'&\\'foo\\u003E'" + " : " + "null" + ";"
				+ SCRIPT_STOP,
			out.toString());
	}

	public void testWriteScriptWithSingleClosingBrace() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = foo[");
		out.write("bar[");
		out.write("4");
		// Must not be quoted, even in CDATA, even if "almost" forming the CDATA end marker. By
		// removing the white space, the script can no longer be rendered with XHTML/HTML4
		// compatibility.
		out.write("]]");
		out.write(" > ");
		out.write("array");
		out.write('[');
		out.write("2");
		// Must not be quoted, even in CDATA
		out.write(']');
		out.write(";");
		out.endScript();

		assertEquals(SCRIPT_START + "var x = foo[bar[4" + "]] > " + "array[2];" + SCRIPT_STOP, out.toString());
	}

	public void testWriteScriptInAttribute() throws IOException {
		TagWriter out = new TagWriter();
		beginScriptAttr(out);
		out.writeScript("var x = true && true ? ");
		out.writeJsLiteral("<'&'foo>");
		out.writeScript(" : ");
		out.writeJsLiteral(null);
		out.writeScript(";");
		endScriptAttr(out);

		assertEquals(SCRIPT_ATTR_START + "var x = true &amp;&amp; true ? " + "'&lt;\\'&amp;\\'foo&gt;'" + " : " + "null" + ";" + SCRIPT_ATTR_STOP, out.toString());
	}
	
	public void testScriptStringInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = ");
		out.writeJsLiteral("&\\\r\n'");
		out.writeScript(";");
		out.endScript();
		
		assertEquals(SCRIPT_START + "var x = '" + "&\\\\" + "\\r" + "\\n" + "\\'" + "';" + SCRIPT_STOP, out.toString());
	}

	public void testScriptStringInAttribute() throws IOException {
		TagWriter out = new TagWriter();
		beginScriptAttr(out);
		out.writeScript("var x = ");
		out.writeJsLiteral("&\\\r\n'");
		out.writeScript(";");
		endScriptAttr(out);

		assertEquals(SCRIPT_ATTR_START + "var x = '" + "&amp;\\\\" + "\\r" + "\\n" + "\\'" + "';" + SCRIPT_ATTR_STOP, out.toString());
	}
	
	public void testScriptStringIncrementalInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = ");
		out.beginJsString();
		out.writeJsStringContent('\'');
		out.writeJsStringContent('&');
		out.writeJsStringContent("'&");
		out.writeJsStringContent("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		out.endScript();

		assertEquals(SCRIPT_START + "var x = '\\'&\\'&\\'&';" + SCRIPT_STOP, out.toString());
	}

	public void testScriptStringIncrementalInAttribute() throws IOException {
		TagWriter out = new TagWriter();
		beginScriptAttr(out);
		out.writeScript("var x = ");
		out.beginJsString();
		out.writeJsStringContent('\'');
		out.writeJsStringContent('&');
		out.writeJsStringContent("'&");
		out.writeJsStringContent("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		endScriptAttr(out);

		assertEquals(SCRIPT_ATTR_START + "var x = '\\'&amp;\\'&amp;\\'&amp;';" + SCRIPT_ATTR_STOP, out.toString());
	}

	public void testScriptStringIncrementalAppendInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = ");
		out.beginJsString();
		out.append('\'');
		out.append('&');
		out.append("'&");
		out.append("'&", 0, 2);
		out.endJsString();
		out.writeScript(";");
		out.endScript();

		assertEquals(SCRIPT_START + "var x = '\\'&\\'&\\'&';" + SCRIPT_STOP, out.toString());
	}

	public void testScriptStringIncrementalAppendInAttribute() throws IOException {
		TagWriter out = new TagWriter();
		beginScriptAttr(out);
		out.writeScript("var x = ");
		out.beginJsString();
		out.append('\'');
		out.append('&');
		out.append("'&");
		out.append("'&", 0, 2);
		out.endJsString();
		out.writeScript(";");
		endScriptAttr(out);
		
		assertEquals(SCRIPT_ATTR_START + "var x = '\\'&amp;\\'&amp;\\'&amp;';" + SCRIPT_ATTR_STOP, out.toString());
	}
	
	public void testScriptStringIncrementalWriteInScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginScript();
		out.writeScript("var x = ");
		out.beginJsString();
		out.write('\'');
		out.write('&');
		out.write("'&");
		out.write("'&", 0, 2);
		out.write("'&".toCharArray());
		out.write("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		out.endScript();

		assertEquals(SCRIPT_START + "var x = '\\'&\\'&\\'&\\'&\\'&';" + SCRIPT_STOP, out.toString());
	}

	public void testScriptStringIncrementalWriteInAttribute() throws IOException {
		TagWriter out = new TagWriter();
		beginScriptAttr(out);
		out.writeScript("var x = ");
		out.beginJsString();
		out.write('\'');
		out.write('&');
		out.write("'&");
		out.write("'&", 0, 2);
		out.write("'&".toCharArray());
		out.write("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		endScriptAttr(out);
		
		assertEquals(SCRIPT_ATTR_START + "var x = '\\'&amp;\\'&amp;\\'&amp;\\'&amp;\\'&amp;';" + SCRIPT_ATTR_STOP, out.toString());
	}
	
	public void testQuotedScript() throws IOException {
		TagWriter out = new TagWriter();
		out.beginQuotedXML();
		out.beginScript();
		out.writeScript("var x = true && ");
		out.beginJsString();
		out.write('\'');
		out.write('&');
		out.write("'&");
		out.write("'&", 0, 2);
		out.write("'&".toCharArray());
		out.write("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		out.endScript();
		out.endQuotedXML();

		assertEquals("<![CDATA[" + SCRIPT_START + "var x = true && '\\'&\\'&\\'&\\'&\\'&';" + QUOTED_SCRIPT_STOP + "]]>",
			out.toString());

		// Other way of checking the unwrap process - which of cause is much more complicated to
		// debug in case of an error:
		String html = DOMUtil.parse("<r>" + out.toString() + "</r>").getDocumentElement().getTextContent();
		String script = DOMUtil.parse(html).getDocumentElement().getTextContent();
		String scriptComment = "\n// \n";
		assertEquals(scriptComment + "var x = true && '\\'&\\'&\\'&\\'&\\'&';" + scriptComment, script);
	}

	public void testQuotedScriptAttribute() throws IOException {
		TagWriter out = new TagWriter();
		out.beginQuotedXML();
		beginScriptAttr(out);
		out.writeScript("var x = true && ");
		out.beginJsString();
		out.write('\'');
		out.write('&');
		out.write("'&");
		out.write("'&", 0, 2);
		out.write("'&".toCharArray());
		out.write("'&".toCharArray(), 0, 2);
		out.endJsString();
		out.writeScript(";");
		endScriptAttr(out);
		out.endQuotedXML();
		
		assertEquals("<![CDATA[" + SCRIPT_ATTR_START + "var x = true &amp;&amp; '\\'&amp;\\'&amp;\\'&amp;\\'&amp;\\'&amp;';" + SCRIPT_ATTR_STOP + "]]>",
			out.toString());

		// Other way of checking the unwrap process - which of cause is much more complicated to
		// debug in case of an error:
		String html = DOMUtil.parse("<r>" + out.toString() + "</r>").getDocumentElement().getTextContent();
		String script = DOMUtil.parse(html).getDocumentElement().getAttribute(SCRIPT_ATTRIBUTE);
		assertEquals("var x = true && '\\'&\\'&\\'&\\'&\\'&';", script);
	}
	
	private void beginScriptAttr(TagWriter out) {
		out.beginBeginTag(SCRIPT_TAG);
		out.beginAttribute(SCRIPT_ATTRIBUTE);
	}

	private void endScriptAttr(TagWriter out) {
		out.endAttribute();
		out.endEmptyTag();
	}

	public void testJsString() throws IOException {
		TagWriter out = new TagWriter();

		out.append("foo(");
		out.writeJsString("<foo's argument> & more");
		out.append(");");

		assertEquals("foo(" + TagUtil.JS_STRING_QUOTE_ATTR + "&lt;foo" + TagUtil.JS_APOS_QUOTE_ATTR
			+ "s argument&gt; &amp; more"
			+ TagUtil.JS_STRING_QUOTE_ATTR + ");", out.toString());
	}

	public void testJsStringElement() throws IOException {
		TagWriter out = new TagWriter();

		out.beginTag("tag");
		writeJs(out);
		out.endTag("tag");

		assertEquals("<tag>" + expectedJsContent() + "</tag>", out.toString());
	}

	public void testJsStringAttribute() throws IOException {
		TagWriter out = new TagWriter();

		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		writeJs(out);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"" + expectedJsContent() + "\"/>", out.toString());
	}

	private void writeJs(TagWriter out) throws IOException {
		out.append("foo(");
		out.writeJsString("<foo's argument>");
		out.append("+");
		{
			out.beginJsString();
			out.writeJsStringContent('&');
			out.writeJsStringContent("<more>");
			out.endJsString();
		}
		out.append("+");
		{
			out.beginJsString();
			out.append('&');
			out.append("\"...\"");
			out.endJsString();
		}
		out.append(");");
	}

	private String expectedJsContent() {
		String jsContent = "foo(" +
			TagUtil.JS_STRING_QUOTE_ATTR + "&lt;foo" + TagUtil.JS_APOS_QUOTE_ATTR + "s argument&gt;"
				+ TagUtil.JS_STRING_QUOTE_ATTR + "+" +
			TagUtil.JS_STRING_QUOTE_ATTR + "&amp;&lt;more&gt;" + TagUtil.JS_STRING_QUOTE_ATTR + "+" +
			TagUtil.JS_STRING_QUOTE_ATTR + "&amp;&quot;...&quot;" + TagUtil.JS_STRING_QUOTE_ATTR +
			");";
		return jsContent;
	}

	public void testJsStringElementAppendable() throws IOException {
		TagWriter out = new TagWriter();

		out.beginTag("tag");
		writeJsAppendable(out);
		out.endTag("tag");

		assertEquals("<tag>" + expectedJsContent() + "</tag>", out.toString());
	}

	public void testJsStringAttributeAppendable() throws IOException {
		TagWriter out = new TagWriter();

		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		writeJsAppendable(out);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"" + expectedJsContent() + "\"/>", out.toString());
	}

	public void testJsStringDirect() throws IOException {
		StringBuilder out = new StringBuilder();

		writeJsAppendable(out);

		assertEquals(expectedJsDirectContent(), out.toString());
	}

	private String expectedJsDirectContent() {
		String jsContent = "foo(" +
			"'\\u003Cfoo\\'s argument\\u003E'" + "+" +
			"'&\\u003Cmore\\u003E'" + "+" +
			"'&\"...\"'" +
			");";
		return jsContent;
	}

	private void writeJsAppendable(Appendable out) throws IOException {
		out.append("foo(");
		TagUtil.writeJsString(out, "<foo's argument>");
		out.append("+");
		{
			TagUtil.beginJsString(out);
			TagUtil.writeJsStringContent(out, '&');
			TagUtil.writeJsStringContent(out, "<more>");
			TagUtil.writeJsStringContent(out, null);
			TagUtil.endJsString(out);
		}
		out.append("+");
		{
			TagUtil.beginJsString(out);
			out.append('&');
			out.append("\"...\"");
			TagUtil.endJsString(out);
		}
		out.append(");");
	}

	public void testWriteInt() throws IOException {
		TagWriter out = new TagWriter();
		out.writeInt(42);
		assertEquals("42", out.toString());
	}

	public void testWriteLong() throws IOException {
		TagWriter out = new TagWriter();
		out.writeLong(1234567890987654321L);
		assertEquals("1234567890987654321", out.toString());
	}

	public void testWriteLongAttr() {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.writeAttribute("attr", 1234567890987654321L);
		out.endEmptyTag();
		assertEquals("<tag attr=\"1234567890987654321\"/>", out.toString());
	}

	public void testWriteFloat() throws IOException {
		TagWriter out = new TagWriter();
		out.writeFloat(42.13f);
		assertEquals("42.13", out.toString());
	}

	public void testWriteIntFloat() throws IOException {
		TagWriter out = new TagWriter();
		out.writeFloat(42f);
		assertEquals("42", out.toString());
	}

	public void testWriteJsLiteralNull() throws IOException {
		TagWriter out = new TagWriter();
		out.writeJsLiteral(null);
		assertEquals("null", out.toString());
	}

	public void testWriteJsLiteralNullString() {
		StringBuilder out = new StringBuilder();
		TagUtil.writeJsString(out, null);
		assertEquals("null", out.toString());
	}

	public void testWriteJsLiteralString() throws IOException {
		TagWriter out = new TagWriter();
		out.writeJsLiteral("'foo'");
		assertEquals(TagUtil.JS_STRING_QUOTE_ATTR + TagUtil.JS_APOS_QUOTE_ATTR + "foo" + TagUtil.JS_APOS_QUOTE_ATTR
			+ TagUtil.JS_STRING_QUOTE_ATTR, out.toString());
	}

	public void testWriteJsLiteralNumberInt() throws IOException {
		TagWriter out = new TagWriter();
		out.writeJsLiteral(42);
		assertEquals("42", out.toString());
	}

	public void testWriteJsLiteralNumberFloat() throws IOException {
		TagWriter out = new TagWriter();
		out.writeJsLiteral(42.13f);
		assertEquals("42.13", out.toString());
	}

	public void testWriteJsLiteralNumberIntAttr() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		out.writeJsLiteral(42);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"42\"/>", out.toString());
	}

	public void testWriteJsLiteralNumberFloatAttr() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		out.writeJsLiteral(42.13f);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"42.13\"/>", out.toString());
	}

	public void testWriteJsLiteralTrue() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		out.writeJsLiteral(true);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"true\"/>", out.toString());
	}

	public void testWriteJsLiteralFalse() throws IOException {
		TagWriter out = new TagWriter();
		out.beginBeginTag("tag");
		out.beginAttribute("attr");
		out.writeJsLiteral(false);
		out.endAttribute();
		out.endEmptyTag();

		assertEquals("<tag attr=\"false\"/>", out.toString());
	}

	public void testStreamingAttributeInEmptyTag() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.writeAttributeText("value");
			out.endAttribute();
			out.endEmptyTag();

			assertEquals("<tag attr=\"value\"/>", out.toString());
		}
	}

	public void testStreamingAttributeNoValueInEmptyTag() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.endAttribute();
			out.endEmptyTag();

			assertEquals("<tag/>", out.toString());
		}
	}

	public void testStreamingAttribute() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.writeAttributeText("value");
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag attr=\"value\"></tag>", out.toString());
		}
	}

	public void testStreamingAttributeChar() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.append('x');
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag attr=\"x\"></tag>", out.toString());
		}
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.writeAttributeText('x');
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag attr=\"x\"></tag>", out.toString());
		}
	}

	public void testStreamingAttributeNoValue() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag></tag>", out.toString());
		}
	}

	public void testStreamingAttributeNullValue() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.append(null);
			out.writeAttributeText(null);
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag></tag>", out.toString());
		}
	}

	public void testStreamingAttributeEmptyStringValue() throws IOException {
		try (TagWriter out = new TagWriter()) {
			out.beginBeginTag("tag");
			out.beginAttribute("attr");
			out.append("");
			out.writeAttributeText("");
			out.endAttribute();
			out.endBeginTag();
			out.endTag("tag");

			assertEquals("<tag attr=\"\"></tag>", out.toString());
		}
	}


    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestTagWriter.class);
        // TestSuite suite = new TestSuite ();
        // suite.addTest (new TestTagWriter("testThis"));
        return suite;
    }

    /**
	 * Main function for direct testing.
	 */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (TestTagWriter.suite ());
    }
}
