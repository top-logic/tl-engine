/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.basic.html;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML.Config;
import com.top_logic.basic.xml.XMLStreamUtil;

class WhiteListHTMLChecker implements HTMLChecker {

	private Set<String> _allowedAttributeNames;

	private Set<String> _allowedTagNames;

	private Map<String, AttributeChecker> _attributeChecker;

	private static final Pattern ENTITY_PATTERN = Pattern.compile("&([a-zA-Z]+);");

	/**
	 * Creates a {@link WhiteListHTMLChecker}.
	 */
	public WhiteListHTMLChecker(InstantiationContext context, Config config) {
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

	@Override
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
			// Allow script tags, if they are of type "text/tlscript". This is required to edit
			// HTML
			// fragments containing those scripts in the UI. Those scripts are not executed by
			// the
			// browser anyway.
			if (SafeHTML.SCRIPT_TAG.equals(localName)) {
				String type = in.getAttributeValue(null, SafeHTML.TYPE_ATTR);
				if (SafeHTML.TEXT_TLSSCRIPT_TYPE.equals(type)) {
					// It's safe to render those scripts (only for editing them in the UI, they
					// are
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

	@Override
	public void checkTag(String tag) throws I18NException {
		if (!isAllowedTagName(tag)) {
			throw new UnsafeHTMLException(I18NConstants.INVALID_TAG_NAME.fill(tag));
		}
	}

	@Override
	public void checkAttribute(String tag, String attribute, String value) throws I18NException {
		checkAttributeName(tag, attribute);
		checkAttributeValue(attribute, value);
	}

	@Override
	public void checkAttributeValue(String attribute, String value) throws I18NException {
		AttributeChecker attributeChecker = getAttributeChecker(attribute);
		if (attributeChecker != null) {
			attributeChecker.check(value);
		}
	}

	@Override
	public AttributeChecker getAttributeChecker(String attribute) {
		return _attributeChecker.get(attribute);
	}

	@Override
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

	@Override
	public boolean isAllowedTagName(String tag) {
		return _allowedTagNames.contains(tag);
	}

	@Override
	public boolean isAllowedAttributeName(String attribute) {
		return _allowedAttributeNames.contains(attribute);
	}
}