/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Utility to check HTML input to only contain safe contents.
 * 
 * @see #check(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SafeHTML extends ManagedClass {

	/** Name of a script tag */
	public static final String SCRIPT_TAG = "script";

	/** Attribute containing the type of a {@link #SCRIPT_TAG} */
	public static final String TYPE_ATTR = "type";

	/**
	 * Type of a {@link #SCRIPT_TAG} that marks it to contain TLScript (those tags are rendered to
	 * the UI only for editing them in the HTML editor, not for executing them).
	 */
	public static final String TEXT_TLSSCRIPT_TYPE = "text/tlscript";

	/**
	 * Configuration for {@link SafeHTML} specifying which attribute names, tag names and attribute
	 * values are allowed.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<SafeHTML> {
		/**
		 * Attribute name for tag and attribute tags.
		 */
		public static final String NAME = "name";

		/**
		 * Configuration tag name for each tag.
		 */
		public static final String TAG = "tag";

		/**
		 * Configuration tag name for each attribute.
		 */
		public static final String ATTRIBUTE = "attribute";

		/**
		 * {@link List} of allowed attributes.
		 */
		@ListBinding(tag = ATTRIBUTE, attribute = NAME)
		List<String> getAllowedAttributes();

		/**
		 * {@link List} of allowed tags.
		 */
		@ListBinding(tag = TAG, attribute = NAME)
		List<String> getAllowedTags();

		/**
		 * {@link Map} of {@link AttributeChecker}'s.
		 */
		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		Map<String, NamedPolymorphicConfiguration<AttributeChecker>> getAttributeCheckers();
	}

	private Set<String> _allowedAttributeNames;

	private Set<String> _allowedTagNames;

	private Map<String, AttributeChecker> _attributeChecker;

	private static final Pattern ENTITY_PATTERN = Pattern.compile("&([a-zA-Z]+);");

	/**
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        {@link Config} for {@link SafeHTML}.
	 */
	public SafeHTML(InstantiationContext context, Config config) {
		super(context, config);

		initAllowedAttributes(config);
		initAllowedTags(config);
		initAttributeCheckers(context, config);
	}

	private void initAttributeCheckers(InstantiationContext context, Config config) {
		_attributeChecker = TypedConfiguration.getInstanceMap(context, config.getAttributeCheckers());
	}

	private void initAllowedTags(Config config) {
		_allowedTagNames = new HashSet<>(config.getAllowedTags());
	}

	private void initAllowedAttributes(Config config) {
		_allowedAttributeNames = new HashSet<>(config.getAllowedAttributes());
	}

	/**
	 * Checks the given HTML fragment to contain only safe contents.
	 * 
	 * <p>
	 * HTML content is safe, if it cannot be used to generate a cross-site-scripting attack when
	 * injected into the UI of an arbitrary user. Save content must be a well-formed XML fragment
	 * (XML without necessarily having a single root tag) consisting only of tags, text and
	 * comments. Only certain tag and attribute names are allow in safe content.
	 * </p>
	 * 
	 * @param html
	 *        The HTML string to be checked.
	 * @throws I18NException
	 *         If the given HTML contains content that is not guaranteed to be safe.
	 */
	public void check(String html) throws I18NException {
		try {
			XMLStreamReader in =
				XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(xml(html)));
			try {
				while (in.hasNext()) {
					int event = in.next();
					switch (event) {
						case XMLStreamConstants.START_ELEMENT:
							checkTag(in);
							break;
						case XMLStreamConstants.START_DOCUMENT:
						case XMLStreamConstants.END_DOCUMENT:
						case XMLStreamConstants.END_ELEMENT:
						case XMLStreamConstants.COMMENT:
						case XMLStreamConstants.CHARACTERS:
						case XMLStreamConstants.CDATA:
						case XMLStreamConstants.SPACE:
							// Considered safe.
							break;
						default:
							throw new UnsafeHTMLException(I18NConstants.UNEXPECTED_STRUCTURE.fill(event));
					}
				}
			} finally {
				in.close();
			}
		} catch (XMLStreamException ex) {
			throw new UnsafeHTMLException(I18NConstants.INVALID_INPUT_STRUCTURE.fill(ex.getMessage()));
		}
	}

	/**
	 * Checks the current tag that the given reader has started to read.
	 */
	private void checkTag(XMLStreamReader in) throws I18NException {
		String localName = in.getLocalName();
		if (!isAllowedTagName(localName)) {
			// Allow script tags, if they are of type "text/tlscript". This is required to edit HTML
			// fragments containing those scripts in the UI. Those scripts are not executed by the
			// browser anyway.
			if (SCRIPT_TAG.equals(localName)) {
				String type = in.getAttributeValue(null, TYPE_ATTR);
				if (TEXT_TLSSCRIPT_TYPE.equals(type)) {
					// It's safe to render those scripts (only for editing them in the UI, they are
					// not executed).
					return;
				}
			}
			throw new UnsafeHTMLException(I18NConstants.INVALID_TAG_NAME.fill(localName));
		}
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			String attributeName = in.getAttributeLocalName(n);
			String attributeValue = in.getAttributeValue(n);
			checkAttribute(localName, attributeName, attributeValue);
		}
	}

	/**
	 * Checks the given tag name.
	 * 
	 * @param tag
	 *        The name of the tag.
	 * @throws I18NException
	 *         if the given tag is not allowed.
	 */
	public void checkTag(String tag) throws I18NException {
		if (!isAllowedTagName(tag)) {
			throw new UnsafeHTMLException(I18NConstants.INVALID_TAG_NAME.fill(tag));
		}
	}

	/**
	 * Checks the given attribute name and value.
	 * 
	 * @param tag
	 *        The tag in which the attribute occurs.
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The value of the attribute.
	 * @throws I18NException
	 *         if the given attribute value is not allowed.
	 */
	public void checkAttribute(String tag, String attribute, String value) throws I18NException {
		checkAttributeName(tag, attribute);
		checkAttributeValue(attribute, value);
	}

	/**
	 * Checks the given attribute value.
	 * 
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The value of the attribute.
	 * @throws I18NException
	 *         if the given attribute value is not allowed.
	 * 
	 * @see #checkAttribute(String, String, String)
	 */
	public void checkAttributeValue(String attribute, String value) throws I18NException {
		AttributeChecker attributeChecker = getAttributeChecker(attribute);
		if (attributeChecker != null) {
			attributeChecker.check(value);
		}
	}

	/**
	 * The {@link AttributeChecker} for the given attribute name or <code>null</code>, if no checker
	 * is configured for this attribute.
	 */
	public AttributeChecker getAttributeChecker(String attribute) {
		return _attributeChecker.get(attribute);
	}

	/**
	 * Checks whether the given attribute is allowed.
	 * 
	 * @param tag
	 *        The tag in which the attribute occurs.
	 * @param attribute
	 *        The name of the attribute.
	 * @throws I18NException
	 *         if the given attribute is not allowed.
	 */
	public void checkAttributeName(String tag, String attribute) throws I18NException {
		if (!isAllowedAttributeName(attribute)) {
			throw new UnsafeHTMLException(I18NConstants.INVALID_ATTRIBUTE_NAME.fill(attribute, tag));
		}
	}

	/**
	 * Add a root tag and replace named entities not defined by XML
	 * 
	 * @param html
	 *        the HTML string to convert.
	 * @return The converted XML string.
	 */
	private static String xml(String html) {
		Matcher matcher = ENTITY_PATTERN.matcher(html);

		StringBuilder buffer = new StringBuilder();
		buffer.append("<div>");
		int pos = 0;
		while (matcher.find(pos)) {
			buffer.append(html, pos, matcher.start());
			buffer.append(HTMLEntities.CHAR_BY_BY_NAME.get(matcher.group(1)));
			pos = matcher.end();
		}
		buffer.append(html, pos, html.length());
		buffer.append("</div>");

		return buffer.toString();
	}

	/**
	 * Return the only instance of this class.
	 * 
	 * @return The requested {@link SafeHTML} instance.
	 */
	public static SafeHTML getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * @param tag
	 *        To be checked tag name.
	 * @return TRUE if this tag is allowed, otherwise false.
	 */
	public boolean isAllowedTagName(String tag) {
		return _allowedTagNames.contains(tag);
	}

	/**
	 * @param attribute
	 *        To be checked attribute name.
	 * @return TRUE if this attribute is allowed, otherwise false.
	 */
	public boolean isAllowedAttributeName(String attribute) {
		return _allowedAttributeNames.contains(attribute);
	}

	/**
	 * Singleton holder for the {@link SafeHTML}.
	 */
	public static final class Module extends TypedRuntimeModule<SafeHTML> {

		/**
		 * Singleton {@link SafeHTML.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SafeHTML> getImplementation() {
			return SafeHTML.class;
		}
	}
}
