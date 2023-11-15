/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic.fragments;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.basic.fragments.Fragments.Attribute;
import com.top_logic.layout.basic.fragments.Fragments.Attributes;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;

/**
 * Test case for {@link Fragments}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFragments extends TestCase {

	public void testEmptyFragment() throws IOException {
		assertRender("", Fragments.empty());
	}

	public void testTextFragment() throws IOException {
		assertRender("", Fragments.text(null));
		assertRender("", Fragments.text(""));
		assertRender("&lt;bar/&gt;", Fragments.text("<bar/>"));
	}

	public void testRenderedFragment() throws IOException {
		assertRender("", Fragments.htmlSource((String) null));
		assertRender("", Fragments.htmlSource(""));
		assertRender("<bar/>", Fragments.htmlSource("<bar/>"));
		assertRender("<b>bold</b>", Fragments.htmlSource(ResKey.text("<b>bold</b>")));
	}

	public void testMessageFragment() throws IOException {
		assertRender("", Fragments.message(null));
		assertRender("&lt;bar/&gt;", Fragments.message(Resources.encodeLiteralText("<bar/>")));
	}

	public void testNbspFragment() throws IOException {
		assertRender(HTMLConstants.NBSP, Fragments.nbsp());
	}

	public void testConcat() throws IOException {
		assertRender("", Fragments.concat());
		assertRender("", Fragments.concat((HTMLFragment[]) null));
		assertRender("", Fragments.concat(new HTMLFragment[0]));
		assertRender("foo", Fragments.concat(Fragments.text("foo")));
		assertRender("foobar", Fragments.concat(Fragments.text("foo"), Fragments.text("bar")));

		assertRender("", Fragments.concat((List<HTMLFragment>) null));
		assertRender("", Fragments.concat(Collections.emptyList()));
		assertRender("foo", Fragments.concat(Collections.singletonList(Fragments.text("foo"))));
		assertRender("foobar", Fragments.concat(BasicTestCase.list(Fragments.text("foo"), Fragments.text("bar"))));
	}

	public void testPlainTag() throws IOException {
		assertRender("<foo>bar</foo>", Fragments.tag("foo", Fragments.text("bar")));
	}

	public void testPlainTagNoContent() throws IOException {
		assertRender("<foo></foo>", Fragments.tag("foo"));
	}

	public void testCssTag() throws IOException {
		assertRender("<foo class=\"bar\">baz</foo>", Fragments.tag("foo", "bar", Fragments.text("baz")));
	}

	public void testSingleAttributeTag() throws IOException {
		assertRender("<foo class=\"bar\">baz</foo>", Fragments.tag("foo", Fragments.attribute("class", "bar"), Fragments.text("baz")));
	}
	
	public void testSingleAttributeTagNoContent() throws IOException {
		assertRender("<foo class=\"bar\"></foo>", Fragments.tag("foo", Fragments.attribute("class", "bar")));
	}

	public void testDynamicAttribute() throws IOException {
		assertRender("<foo class=\"foobar\"></foo>",
			Fragments.tag("foo", Fragments.attribute("class", (context, out) -> {
				out.append("foo");
				out.append("bar");
			})));
	}

	public void testGenericTag() throws IOException {
		assertRender("<foo id=\"xxx\" class=\"bar\">baz</foo>", Fragments.tag("foo", Fragments.attributes(Fragments.attribute("id", "xxx"), Fragments.attribute("class", "bar")), Fragments.text("baz")));
	}
	
	public void testNoContentTag() throws IOException {
		assertRender("<foo id=\"xxx\" class=\"bar\"></foo>",
			Fragments.tag("foo",
				Fragments.attribute("id", "xxx"),
				Fragments.attribute("class", "bar")));
	}

	public void testNoContentTagAttributes() throws IOException {
		assertRender("<foo id=\"xxx\" class=\"bar\"></foo>",
			Fragments.tag("foo",
				Fragments.attributes(
					Fragments.attribute("id", "xxx"),
					Fragments.attribute("class", "bar"))));
	}

	public void testEmptyTag() throws IOException {
		assertRender("<foo id=\"xxx\" class=\"bar\"/>",
			Fragments.tagEmpty("foo",
				Fragments.attribute("id", "xxx"),
				Fragments.attribute("class", "bar")));
	}

	public void testEmptyCssTag() throws IOException {
		assertRender("<foo class=\"foobar\"/>",
			Fragments.tagEmpty("foo", "foobar"));
	}

	public void testPlainEmptyTag() throws IOException {
		assertRender("<foo/>",
			Fragments.tagEmpty("foo"));
	}

	public void testHtml() throws IOException {
		assertRender(
			"<html><head></head><body><div id=\"c0\" class=\"foobar\">Hello world!</div></body></html>",
			html(head(), body(div(attributes(id("c0"), css("foobar")), text("Hello world!")))));
	}

	public void testConditionalTag() throws IOException {
		assertRender("<br id=\"c1\" class=\"is-control foo\"/>",
			Fragments.conditional(VisibilityModel.AlwaysVisible.INSTANCE, Fragments.br("foo")));
		assertRender("<div id=\"c1\" class=\"is-control\">content</div>",
			Fragments.conditional(VisibilityModel.AlwaysVisible.INSTANCE, Fragments.div(Fragments.text("content"))));
	}

	public void testConditionalTagInvisible() throws IOException {
		VisibilityModel.Default invisible = new VisibilityModel.Default(false);
		assertRender("<br id=\"c1\" class=\"is-control\" style=\"display:none;\"/>",
			Fragments.conditional(invisible, Fragments.br("foo")));
		assertRender("<div id=\"c1\" class=\"is-control\" style=\"display:none;\"></div>",
			Fragments.conditional(invisible, Fragments.div(Fragments.text("content"))));
	}

	public void testRendered() throws IOException {
		assertRender("<div>content</div>",
			Fragments.div(Fragments.rendered(new Renderer<>() {
				@Override
				public void write(DisplayContext context, TagWriter out, String value) throws IOException {
					out.writeText("con");
					out.writeText(value);
				}

			}, "tent")));
	}

	public void testParametrization() throws IOException {
		Property<HTMLFragment> x = Fragments.templateProperty("x");
		Property<HTMLFragment> y = Fragments.templateProperty("x");

		HTMLFragment template = Fragments.tag("container",
			Fragments.tagEmpty("before"),
			Fragments.templateVar(x),
			Fragments.tagEmpty("middle"),
			Fragments.templateVar(y),
			Fragments.tagEmpty("after"));

		AbstractDisplayContext context = assertRender(
			"<container><before/><argument1/><middle/><argument2/><after/></container>",
			Fragments.templateBind(template,
				Fragments.templateArg(x, Fragments.tagEmpty("argument1")),
				Fragments.templateArg(y, Fragments.tagEmpty("argument2"))));

		assertNull("Variables have been unbound after rendering.", context.get(x));
	}

	public void testParametrizationSingleArg() throws IOException {
		Property<HTMLFragment> x = Fragments.templateProperty("x");

		HTMLFragment template = Fragments.tag("container",
			Fragments.tagEmpty("before"),
			Fragments.templateVar(x),
			Fragments.tagEmpty("after"));

		AbstractDisplayContext context = assertRender(
			"<container><before/><argument/><after/></container>",
			Fragments.templateBind(template, x, Fragments.tagEmpty("argument")));

		assertNull("Variables have been unbound after rendering.", context.get(x));
	}

	public void testParametrizationWithNull() throws IOException {
		Property<HTMLFragment> x = TypedAnnotatable.property(HTMLFragment.class, "x");

		HTMLFragment template = Fragments.tag("container",
			Fragments.tagEmpty("before"),
			Fragments.templateVar(x),
			Fragments.tagEmpty("after"));

		AbstractDisplayContext context = assertRender(
			"<container><before/><after/></container>",
			Fragments.templateBind(template, Fragments.templateArg(x, null)));

		assertNull("Variables have been unbound after rendering.", context.get(x));
	}

	public void testParametrizationUnbound() throws IOException {
		Property<HTMLFragment> x = TypedAnnotatable.property(HTMLFragment.class, "withSameName");
		Property<HTMLFragment> y = TypedAnnotatable.property(HTMLFragment.class, "withSameName");

		try {
			assertRender(
				"<container><before/><after/></container>",
				Fragments.templateBind(
					Fragments.tag("container",
						Fragments.tagEmpty("before"),
						Fragments.templateVar(x),
						Fragments.tagEmpty("after")),
					Fragments.templateArg(y, Fragments.tagEmpty("argument"))));
			fail("Must not accept unbound variables.");
		} catch (IllegalStateException ex) {
			// Expected.
			assertTrue(ex.getMessage().contains("not bound"));
		}
	}

	public void testMemberAspects() throws IOException {
		StringField field = FormFactory.newStringField("foo");
		field.setLabel("foo-label");
		field.setTooltip("foo-tooltip");
		field.setTooltipCaption("foo-tooltip-caption");
		ControlProvider cp = new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new SimpleConstantControl<>(style == null ? "style:value" : "style:" + style,
					ResourceRenderer.newResourceRenderer(DefaultLabelProvider.INSTANCE));
			}
		};
		assertRender(
			"<span id=\"c1\" class=\"is-control\">style:" + FormTemplateConstants.STYLE_LABEL_VALUE + "</span>",
			Fragments.label(cp, field));
		assertRender(
			"<span id=\"c1\" class=\"is-control\">style:" + FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE
				+ "</span>",
			Fragments.labelWithColon(cp, field));
		assertRender(
			"<span id=\"c1\" class=\"is-control\">style:value</span>",
			Fragments.value(cp, field));
		assertRender(
			"<span id=\"c1\" class=\"is-control\">style:" + FormTemplateConstants.STYLE_ERROR_VALUE
				+ "</span>",
			Fragments.error(cp, field));
		assertRender(
			"<span id=\"c1\" class=\"is-control\"><span id=\"c2\" class=\"is-control\">style:value</span><span id=\"c3\" class=\"is-control\">style:"
				+ FormTemplateConstants.STYLE_ERROR_VALUE + "</span></span>",
			Fragments.valueWithError(cp, field));
		assertRender(
			"<span id=\"c1\" class=\"is-control\"><span id=\"c2\" class=\"is-control\">style:"
				+ FormTemplateConstants.STYLE_ERROR_VALUE
				+ "</span><span id=\"c3\" class=\"is-control\">style:value</span></span>",
			Fragments.errorWithValue(cp, field));
	}

	public void testAllHtmlConstants() throws IllegalAccessException, SecurityException, IllegalArgumentException,
			InvocationTargetException, IOException {
		for (Field field : HTMLConstants.class.getDeclaredFields()) {
			if (field.isSynthetic()) {
				continue;
			}
			if (!Modifier.isPublic(field.getModifiers())) {
				continue;
			}
			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			String fieldName = field.getName();
			if (fieldName.endsWith("_ATTR")) {
				if (fieldName.startsWith("TL_")) {
					continue;
				}
				String methodName =
					CodeUtil.toLowerCaseStart(
						CodeUtil.toCamelCase(
							fieldName.substring(0, fieldName.length() - "_ATTR".length()).toLowerCase()));

				String attrName = (String) field.get(null);
				if (HTMLConstants.CLASS_ATTR.equals(attrName)) {
					methodName = "css";
				}
				if (HTMLConstants.FOR_ATTR.equals(attrName)) {
					methodName = "forAttr";
				}

				try {
					Method factory = Fragments.class.getMethod(methodName, String.class);
					Attribute attr = (Attribute) factory.invoke(null, "foo");
					assertRender("<br " + attrName + "=\"foo\"/>", Fragments.br(attr));
				} catch (NoSuchMethodException ex) {
					BasicTestCase.fail("Missing attribute method '" + methodName + "(String)'.", ex);
				}

				continue;
			}
			if (fieldName.startsWith("DOCTYPE_")) {
				continue;
			}
			if (fieldName.endsWith("_NS")) {
				continue;
			}
			if (fieldName.endsWith("_ARROW")) {
				continue;
			}
			if (fieldName.endsWith("_PROPERTY")) {
				continue;
			}
			if (fieldName.endsWith("_VALUE")) {
				continue;
			}
			if (fieldName.startsWith("TEXT_ALIGN_")) {
				continue;
			}
			if (fieldName.startsWith("CONTENT_TYPE_")) {
				continue;
			}
			if (fieldName.equals("TYPE_ICO")) {
				continue;
			}
			if (fieldName.equals("TYPE_GIF")) {
				continue;
			}
			if (fieldName.equals("TYPE_PNG")) {
				continue;
			}
			if (fieldName.equals("TYPE_JPEG")) {
				continue;
			}
			if (fieldName.equals("TYPE_SVG")) {
				continue;
			}
			if (field.getType() != String.class) {
				continue;
			}
			// Note: Do not add any other quirks exceptions here, if adding new constants adhere to
			// the existing conventions!

			String tagName = (String) field.get(null);
			if (tagName.startsWith(HTMLConstants.DATA_ATTRIBUTE_PREFIX)) {
				continue;
			}
			if (tagName.equals(HTMLConstants.NBSP)) {
				continue;
			}
			if (tagName.equals(HTMLConstants.SUM)) {
				continue;
			}
			if (tagName.equals(HTMLConstants.NL)) {
				continue;
			}
			if (tagName.equals(HTMLConstants.HREF_EMPTY_LINK)) {
				continue;
			}
			if (tagName.equals(HTMLConstants.SCROLLING)) {
				continue;
			}
			// Note: Do not add any other quirks exceptions here, if adding new constants adhere to
			// the existing conventions!

			String methodName = tagName;

			HTMLFragment[] content = { Fragments.text("content") };

			boolean isVoid = HTMLUtil.isVoidElement(tagName);
			try {
				if (isVoid) {
					Method factory = Fragments.class.getMethod(methodName);
					HTMLFragment fragment = (HTMLFragment) factory.invoke(null);
					assertRender("<" + tagName + "/>", fragment);
				} else {
					Method factory = Fragments.class.getMethod(methodName, HTMLFragment[].class);
					HTMLFragment fragment = (HTMLFragment) factory.invoke(null, new Object[] { content });
					assertRender(fieldName, "<" + tagName + ">content</" + tagName + ">", fragment);
				}

				if (isVoid) {
					Method factory = Fragments.class.getMethod(methodName, String.class);
					HTMLFragment fragment = (HTMLFragment) factory.invoke(null, "foobar");
					assertRender("<" + tagName + " class=\"foobar\"/>", fragment);
				} else {
					Method factory = Fragments.class.getMethod(methodName, String.class, HTMLFragment[].class);
					HTMLFragment fragment = (HTMLFragment) factory.invoke(null, new Object[] { "foobar", content });
					assertRender("<" + tagName + " class=\"foobar\">content</" + tagName + ">", fragment);
				}

				if (isVoid) {
					Method factory = Fragments.class.getMethod(methodName, Attribute[].class);
					HTMLFragment fragment =
						(HTMLFragment) factory.invoke(null,
							new Object[] { new Attribute[] { Fragments.attribute("id", "c0"),
								Fragments.attribute("class", "foobar") } });
					assertRender("<" + tagName + " id=\"c0\" class=\"foobar\"/>", fragment);
				} else {
					Method factory = Fragments.class.getMethod(methodName, Attributes.class, HTMLFragment[].class);
					HTMLFragment fragment =
						(HTMLFragment) factory.invoke(null,
							new Object[] {
								Fragments.attributes(
									Fragments.attribute("id", "c0"),
									Fragments.attribute("class", "foobar")),
								content });
					assertRender("<" + tagName + " id=\"c0\" class=\"foobar\">content</" + tagName + ">", fragment);
				}
			} catch (NoSuchMethodException ex) {
				BasicTestCase.fail("Missing factory method " + methodName + "() in Fragments.", ex);
			} catch (IllegalArgumentException ex) {
				throw (AssertionFailedError) new AssertionFailedError(
					"Wrong factory method " + methodName + "() in Fragments.").initCause(ex);
			}

		}
	}

	private AbstractDisplayContext assertRender(String expectedResult, HTMLFragment fragment) throws IOException {
		return assertRender(null, expectedResult, fragment);
	}

	private AbstractDisplayContext assertRender(String message, String expectedResult, HTMLFragment fragment)
			throws IOException {
		AbstractDisplayContext context = DummyDisplayContext.forScope(new DummyControlScope());
		TagWriter out = new TagWriter();

		out.beginTag("foo");
		fragment.write(context, out);
		out.endTag("foo");

		assertEquals(message, "<foo>" + expectedResult + "</foo>", out.toString());

		return context;
	}

	public static Test suite() {
		return KBSetup
			.getSingleKBTest(ServiceTestSetup.createSetup(TestFragments.class, LabelProviderService.Module.INSTANCE));
	}
}
