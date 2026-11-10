/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.html.i18n;

import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;

/**
 * Test case for reading and writing {@link HtmlResKey} values in XML configurations, especially
 * for {@link DefaultHtmlResKey.ValueFormat}.
 */
@SuppressWarnings("javadoc")
public class TestHtmlResKeyFormat extends AbstractTypedConfigurationTestCase {

	public interface HtmlResKeyItem extends ConfigurationItem {

		@Nullable
		HtmlResKey getContent();

		void setContent(HtmlResKey value);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return createDescriptorMap(HtmlResKeyItem.class);
	}

	/**
	 * An empty attribute value, as written for an empty {@link ResKey} before the property type was
	 * changed to {@link HtmlResKey}, is read as <code>null</code>.
	 */
	public void testReadEmptyAttribute() throws Exception {
		HtmlResKeyItem item = read("<HtmlResKeyItem content=''/>");
		assertNull(item.getContent());
	}

	/**
	 * A resource key given as attribute is read as {@link DefaultHtmlResKey} of that key.
	 */
	public void testReadKeyAttribute() throws Exception {
		HtmlResKeyItem item = read("<HtmlResKeyItem content='my.html.key'/>");
		assertEquals(new DefaultHtmlResKey(ResKey.internalCreate("my.html.key")), item.getContent());
	}

	/**
	 * A resource key given as element is still read.
	 */
	public void testReadKeyElement() throws Exception {
		HtmlResKeyItem item = read("<HtmlResKeyItem><content key='my.html.key'/></HtmlResKeyItem>");
		assertEquals(new DefaultHtmlResKey(ResKey.internalCreate("my.html.key")), item.getContent());
	}

	/**
	 * Translations given as element are still read.
	 */
	public void testReadLiteralElement() throws Exception {
		HtmlResKeyItem item = read("<HtmlResKeyItem><content>"
			+ "<en><![CDATA[<b>Hello</b>]]></en>"
			+ "<de><![CDATA[<b>Hallo</b>]]></de>"
			+ "</content></HtmlResKeyItem>");
		ResKey content = ((DefaultHtmlResKey) item.getContent()).content();
		assertTrue(content.isLiteral());
		assertEquals("<b>Hello</b>", translation(content, Locale.ENGLISH));
		assertEquals("<b>Hallo</b>", translation(content, Locale.GERMAN));
	}

	/**
	 * A literal key cannot be represented as attribute and is written as element.
	 */
	public void testWriteLiteral() throws Exception {
		HtmlResKeyItem item = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);
		ResKey literal = ResKey.builder()
			.add(Locale.ENGLISH, "<b>Hello</b>")
			.add(Locale.GERMAN, "<b>Hallo</b>")
			.build();
		item.setContent(new DefaultHtmlResKey(literal));

		String xml = toXML(item);
		assertFalse("Literal must not be written as attribute: " + xml, xml.contains("content="));

		HtmlResKeyItem copy = (HtmlResKeyItem) fromXML(xml);
		ResKey content = ((DefaultHtmlResKey) copy.getContent()).content();
		assertTrue(content.isLiteral());
		assertEquals("<b>Hello</b>", translation(content, Locale.ENGLISH));
		assertEquals("<b>Hallo</b>", translation(content, Locale.GERMAN));
	}

	/**
	 * A plain resource key survives writing and reading.
	 */
	public void testWriteKey() throws Exception {
		HtmlResKeyItem item = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);
		DefaultHtmlResKey key = new DefaultHtmlResKey(ResKey.internalCreate("my.html.key"));
		item.setContent(key);

		HtmlResKeyItem copy = (HtmlResKeyItem) fromXML(toXML(item));
		assertEquals(key, copy.getContent());
	}

	/**
	 * A missing value survives writing and reading.
	 */
	public void testWriteNull() throws Exception {
		HtmlResKeyItem item = TypedConfiguration.newConfigItem(HtmlResKeyItem.class);

		HtmlResKeyItem copy = (HtmlResKeyItem) fromXML(toXML(item));
		assertNull(copy.getContent());
	}

	private static String translation(ResKey literal, Locale locale) {
		return ((ResKey.LiteralKey) literal).getTranslationWithoutFallbacks(locale);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestHtmlResKeyFormat.class));
	}

}
