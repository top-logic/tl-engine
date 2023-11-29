/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.html.template;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.html.template.I18NConstants;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.MapWithProperties;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests evaluating {@link HTMLTemplateFragment}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@SuppressWarnings("javadoc")
public class TestHTMLTemplates extends TestCase {

	public void testTemplateExpressions() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();

		properties.put("id", "c4711");
		properties.put("isActive", false);

		String html = html("<div id=\"{id}\" class=\"tabIcon {isActive ? 'active'}\"></div>", properties);
		assertEquals("<div id=\"c4711\" class=\"tabIcon\"></div>", html);

		String html2 = html(
			"<div id=\"{id}\" class=\"{!isActive ? 'disabledTabIcon' : 'tabIcon'}{isActive ? 'active' : 'inactive'}\"></div>",
			properties);
		assertEquals("<div id=\"c4711\" class=\"disabledTabIcon inactive\"></div>", html2);
	}

	public void testAttributeSuppression() throws IOException, ConfigurationException {
		String template = "<input name=\"foo\" size=\"{value > 0 ? value}\"/>";
		assertEquals("<input name=\"foo\"/>",
			html(template, WithProperties.fromMap(Collections.singletonMap("value", 0))));

		assertEquals("<input name=\"foo\" size=\"42\"/>",
			html(template, WithProperties.fromMap(Collections.singletonMap("value", 42))));

		assertEquals("<input name=\"foo\"/>",
			html("<input name=\"foo\" size=\"{value}\"/>",
				WithProperties.fromMap(Collections.singletonMap("value", null))));
	}

	public void testTemplateExpressionConcat() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();

		properties.put("isActive", true);
		properties.put("activeClass", "active");
		properties.put("visibleClass", "visible");

		String html = html("<div class=\"{isActive ? activeClass} {isActive ? visibleClass}\"></div>", properties);

		assertEquals("<div class=\"active visible\"></div>", html);
	}

	public void testIfThenExpressions() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();

		properties.put("isBorderBox", true);
		properties.put("width", 50);
		properties.put("padding", 20);
		properties.put("border", 10);

		String html = html("<div style=\"{isBorderBox ? width + padding + border + 'px'}\"></div>", properties);

		assertEquals("<div style=\"80px\"></div>", html);

	}

	public void testIfThenElseExpressions() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		properties.put("a", false);
		properties.put("b", false);
		properties.put("withAdditional", false);

		String template =
			"<div class=\"{a ? (withAdditional ? 'aWith': 'a') : (b ? (withAdditional ? 'bWith':'b'):'none')}\"></div>";
		assertEquals("<div class=\"none\"></div>", html(template, properties));
		properties.put("b", true);
		assertEquals("<div class=\"b\"></div>", html(template, properties));
		properties.put("withAdditional", true);
		assertEquals("<div class=\"bWith\"></div>", html(template, properties));
		properties.put("a", true);
		assertEquals("<div class=\"aWith\"></div>", html(template, properties));
		properties.put("withAdditional", false);
		assertEquals("<div class=\"a\"></div>", html(template, properties));
	}

	public void testConditionalTag() throws IOException, ConfigurationException {
		String template =
			"<a><b a1=\"v1\" tl:if=\"test\" a2=\"v2\"/><b a1=\"v1\" tl:if=\"!test\" a2=\"v2\">some contents</b></a>";
		assertEquals(
			"<a><b a1=\"v1\" a2=\"v2\"/></a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("test", Boolean.TRUE))));
		assertEquals(
			"<a><b a1=\"v1\" a2=\"v2\">some contents</b></a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("test", Boolean.FALSE))));
	}

	public void testConditionTag() throws IOException, ConfigurationException {
		String template =
			"<a>" + "<tl:if test=\"test\"><b a1=\"v1\" a2=\"v2\"></b></tl:if>"
				+ "<tl:if test=\"!test\"><b a1=\"v1\" a2=\"v2\">some contents</b></tl:if>"
				+ "<tl:if test=\"test\">plain content</tl:if>" + "</a>";
		assertEquals(
			"<a><b a1=\"v1\" a2=\"v2\"></b>plain content</a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("test", Boolean.TRUE))));
		assertEquals(
			"<a><b a1=\"v1\" a2=\"v2\">some contents</b></a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("test", Boolean.FALSE))));
	}

	public void testChooseTag() throws IOException, ConfigurationException {
		String template =
			"<a>\n" +
				"    <tl:choose>\n" +
				"        <tl:when test=\"test1\"><b1></b1></tl:when>\n" +
				"        <tl:when test=\"test2\"><b2></b2></tl:when>\n" +
				"        <tl:otherwise><b3></b3></tl:otherwise>\n" +
				"    </tl:choose>\n" +
				"</a>\n";
		assertEquals("<a><b1></b1></a>", html(template,
			WithProperties.fromMap(
				new MapBuilder<String, Object>().put("test1", Boolean.TRUE).put("test2", Boolean.TRUE).toMap())));
		assertEquals("<a><b2></b2></a>", html(template,
			WithProperties.fromMap(
				new MapBuilder<String, Object>().put("test1", Boolean.FALSE).put("test2", Boolean.TRUE).toMap())));
		assertEquals("<a><b3></b3></a>", html(template,
			WithProperties.fromMap(
				new MapBuilder<String, Object>().put("test1", Boolean.FALSE).put("test2", Boolean.FALSE).toMap())));
	}

	public void testTrivialChooseTag() throws IOException, ConfigurationException {
		String template =
			"<a>" +
				"<tl:choose>" +
				"<tl:otherwise><b></b></tl:otherwise>" +
				"</tl:choose>" +
				"</a>";
		assertEquals("<a><b></b></a>", html(template, WithProperties.fromMap(Collections.emptyMap())));
	}

	public void testEmptyChooseTag() throws IOException, ConfigurationException {
		String template =
			"<a>" +
				"<tl:choose>" +
				"</tl:choose>" +
				"</a>";
		assertEquals("<a></a>", html(template, WithProperties.fromMap(Collections.emptyMap())));
	}

	public void testForeachTag() throws IOException, ConfigurationException {
		String template =
			"<a><tl:foreach elements=\"x : values\"><b>{x}</b></tl:foreach></a>";
		assertEquals(
			"<a><b>1</b><b>2</b><b>3</b></a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("values", Arrays.asList(1, 2, 3)))));
	}

	public void testTagLoop() throws IOException, ConfigurationException {
		String template =
			"<a><b tl:foreach=\"x : values\">{x}</b></a>";
		assertEquals(
			"<a><b>1</b><b>2</b><b>3</b></a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("values", Arrays.asList(1, 2, 3)))));
	}

	public void testWithTag() throws IOException, ConfigurationException {
		String template =
			"<a><tl:with def=\"x : 2 * value + 1\">{x} + {x}</tl:with></a>";
		assertEquals(
			"<a>5 + 5</a>",
			html(template, WithProperties.fromMap(Collections.singletonMap("value", 2))));
	}

	public void testTemplateBooleanExpressions() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		properties.put("isTrue", true);
		properties.put("isFalse", false);

		assertEquals("<div></div>", html("<div class=\"{isFalse ? 'foo'}\"></div>", properties));
		assertEquals("<div class=\"foo\"></div>", html("<div class=\"{!isFalse ? 'foo'}\"></div>", properties));
		assertEquals("<div></div>", html("<div class=\"{!isFalse && !isTrue ? 'foo'}\"></div>", properties));
		assertEquals("<div class=\"foo\"></div>",
			html("<div class=\"{!isFalse && isTrue ? 'foo'}\"></div>", properties));
		assertEquals("<div></div>", html("<div class=\"{isFalse || !isTrue ? 'foo'}\"></div>", properties));
		assertEquals("<div class=\"foo\"></div>",
			html("<div class=\"{isFalse || isTrue ? 'foo'}\"></div>", properties));
	}

	public void testOrAsChain() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		assertEquals("<span>last</span>", html("<span>{null||false||'last'}</span>", properties));
		assertEquals("<span>first</span>", html("<span>{'first'||'foobar'}</span>", properties));
	}

	public void testComputeLong() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		long a = 42;
		properties.put("a", a);
		long b = 13;
		properties.put("b", b);

		assertEquals("<span>" + (-a) + "</span>", html("<span>{-a}</span>", properties));
		assertEquals("<span>" + (a + b) + "</span>", html("<span>{a + b}</span>", properties));
		assertEquals("<span>" + (a - b) + "</span>", html("<span>{a - b}</span>", properties));
		assertEquals("<span>" + (a * b) + "</span>", html("<span>{a * b}</span>", properties));
		assertEquals("<span>" + (a / b) + "</span>", html("<span>{a / b}</span>", properties));
		assertEquals("<span>" + (a % b) + "</span>", html("<span>{a % b}</span>", properties));
	}

	public void testComputeDouble() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		double a = 42.1;
		properties.put("a", a);
		double b = 13.2;
		properties.put("b", b);

		assertEquals("<span>" + (-a) + "</span>", html("<span>{-a}</span>", properties));
		assertEquals("<span>" + render(a + b) + "</span>", html("<span>{a + b}</span>", properties));
		assertEquals("<span>" + render(a - b) + "</span>", html("<span>{a - b}</span>", properties));
		assertEquals("<span>" + render(a * b) + "</span>", html("<span>{a * b}</span>", properties));
		assertEquals("<span>" + render(a / b) + "</span>", html("<span>{a / b}</span>", properties));
		assertEquals("<span>" + render(a % b) + "</span>", html("<span>{a % b}</span>", properties));
	}

	private String render(double value) {
		return Double.toString(value);
	}

	public void testStringConcat() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		String a = "Hello";
		properties.put("a", a);
		String b = "World";
		properties.put("b", b);

		assertEquals("<span>" + (a + b) + "</span>", html("<span>{a + b}</span>", properties));
		assertEquals("<span>" + ("foobar" + b) + "</span>", html("<span>{'foobar' + b}</span>", properties));
	}

	public void testStringLongConcat() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		String a = "Hello";
		properties.put("a", a);
		long b = 42;
		properties.put("b", b);

		assertEquals("<span>" + (a + b) + "</span>", html("<span>{a + b}</span>", properties));
	}

	public void testLongStringConcat() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		long a = 13;
		properties.put("a", a);
		String b = "World";
		properties.put("b", b);

		assertEquals("<span>" + (a + b) + "</span>", html("<span>{a + b}</span>", properties));
	}

	public void testCompare() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		long a = 13;
		properties.put("a", a);
		long b = 42;
		properties.put("b", b);

		assertEquals("<span>t</span>", html("<span>{a == a ? 't' : 'f'}</span>", properties));
		assertEquals("<span>f</span>", html("<span>{a == b ? 't' : 'f'}</span>", properties));
		assertEquals("<span>f</span>", html("<span>{a != a ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{a != b ? 't' : 'f'}</span>", properties));
		assertEquals("<span>f</span>", html("<span>{a > b ? 't' : 'f'}</span>", properties));
		assertEquals("<span>f</span>", html("<span>{a >= b ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{a < b ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{a <= b ? 't' : 'f'}</span>", properties));
	}

	public void testLiterals() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		long a = 13;
		properties.put("a", a);

		assertEquals("<span>t</span>", html("<span>{a == 13 ? 't' : 'f'}</span>", properties));
		assertEquals("<span>f</span>", html("<span>{true == false ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{1 == 1.0 ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{1 != 1.1 ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{null != a ? 't' : 'f'}</span>", properties));
		assertEquals("<span>t</span>", html("<span>{'x' < 'y' ? 't' : 'f'}</span>", properties));
	}

	/**
	 * White space is collapsed, except for text inside the <tl:text> tag.
	 */
	public void testText() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();

		assertEquals("<span> some \t  raw  \t text </span>",
			html("<span>  \tsome\t  <tl:text>\t  raw  \t</tl:text>\t  text  \t</span>", properties));
		assertEquals("<a><b></b><b></b></a>",
			html("    \t<a>\n    \t\t<b></b>\n    \t    <b></b>\n    </a>\n    ", properties));
	}

	public void testEntities() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();

		assertEquals("<span>\u00C4</span>", html("<span>&Auml;</span>", properties));
		assertEquals("<span>\u00C4</span>", html("<span>&#x00C4;</span>", properties));
		assertEquals("<span>\u00C4</span>", html("<span>&#196;</span>", properties));
		assertEquals("<span id=\"\u00C4\"></span>", html("<span id=\"&Auml;\"></span>", properties));
		assertEquals("<span id=\"\u00C4\"></span>", html("<span id=\"&#x00C4;\"></span>", properties));
		assertEquals("<span id=\"\u00C4\"></span>", html("<span id=\"&#196;\"></span>", properties));
	}

	public void testParentesis() throws IOException, ConfigurationException {
		assertEquals("<span>18</span>", html("<span>{(1+5)*3}</span>", new MapWithProperties()));
	}

	public void testInvalidNesting() throws ConfigurationException {
		try {
			parse("text <a name=\"xxx\"><span>foobar</div>!</a>");
			fail("Expected parsing failuer.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.html.template.I18NConstants.ERROR_INVALID_NESTING__EXPECTED_FOUND_LINE_COL,
				ex.getErrorKey().plain());
			assertEquals("span", ex.getErrorKey().arguments()[0]);
			assertEquals("div", ex.getErrorKey().arguments()[1]);
			assertEquals(1, ex.getErrorKey().arguments()[2]);
			assertEquals(32, ex.getErrorKey().arguments()[3]);
		}
	}

	public void testQuotingInvalidAttributeContent() throws IOException, ConfigurationException {
		assertEquals("text <a name=\"&lt;span>test&lt;/span>\">foobar!</a>",
			html("text <a name=\"<span>test</span>\">foobar!</a>", new MapWithProperties()));
	}

	public void testQuotingExpressions() throws IOException, ConfigurationException {
		assertEquals(
			"<div data-value=\"{&quot;name&quot;: &quot;value&quot;; &quot;foo&quot;: 42 }\">{hello world}</div>",
			html("<div data-value=\"\\{\\\"name\\\": \\\"value\\\"; \\\"foo\\\": 42 \\}\">\\{hello world\\}</div>",
				new MapWithProperties()));
	}

	public void testQuotingSpecials() throws IOException, ConfigurationException {
		assertEquals("<div data-value=\"&lt;span>&amp;nbsp;&lt;/span>\"></div>",
			html("<div data-value=\"<span>\\&nbsp;</span>\"></div>",
				new MapWithProperties()));
		assertEquals("<div data-value=\"&lt;span>&amp;nbsp;&lt;/span>\"></div>",
			html("<div data-value=\"&lt;span>&amp;nbsp;&lt;/span>\"></div>",
				new MapWithProperties()));
	}

	public void testDuplicateAttributeDetection() throws ConfigurationException {
		try {
			parse(
				"<div" + "\n" +
					"    class=\"foo\"" + "\n" +
					"    style=\"color: red;\" " + "\n" +
					"    class=\"bar\">content</div>");
			fail("Parse error expected.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_DUPLICATE_ATTRIBUTE__NAME_LINE_COL, ex.getErrorKey().plain());
			assertEquals("class", ex.getErrorKey().arguments()[0]);
			assertEquals(4, ex.getErrorKey().arguments()[1]);
			assertEquals(5, ex.getErrorKey().arguments()[2]);
		}
	}

	public void testEmptyNonVoidElementDetection() throws ConfigurationException {
		try {
			parse("<div/>");
			fail("Parse error expected.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.NO_VALID_VOID_ELEMENT__NAME_LINE_COL, ex.getErrorKey().plain());
			assertEquals("div", ex.getErrorKey().arguments()[0]);
			assertEquals(1, ex.getErrorKey().arguments()[1]);
			assertEquals(1, ex.getErrorKey().arguments()[2]);
		}
	}

	public void testVariables() throws ConfigurationException {
		HTMLTemplate template = parse("<svg xmlns=\"http://www.w3.org/2000/svg\">"
			+ "<g style=\"stroke-width:10.5309; {hasWarning ? '' : 'display: none;'}\"></g></svg>");
		assertTrue(template.getVariables().contains("hasWarning"));
	}

	private String html(String template, WithProperties properties) throws IOException, ConfigurationException {
		return render(parse(template), properties);
	}

	private HTMLTemplate parse(String template) throws ConfigurationException {
		return HTMLTemplateUtils.parse("test", template);
	}

	private String render(HTMLTemplateFragment template, WithProperties properties) throws IOException {
		TagWriter writer = new TagWriter();

		template.write(displayContext(), writer, properties);

		return writer.toString();
	}

	private DisplayContext displayContext() {
		return null;
	}

}
