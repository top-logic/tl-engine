/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The {@link DOMHelper} is a helper class to create DOM {@link Element}s for
 * {@link FormPatternConstants} and {@link FormTemplateConstants}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DOMHelper implements FormPatternConstants, FormTemplateConstants {

	/**
	 * This method returns an {@link Element} with namespace
	 * {@link FormPatternConstants#PATTERN_NS}, qualified name
	 * {@link FormPatternConstants#FIELD_PATTERN_ELEMENT} and attribute
	 * {@link FormPatternConstants#NAME_FIELD_ATTRIBUTE}.
	 * 
	 * @param aDocument
	 *            the {@link Document} to create the element for.
	 * @param aFieldName
	 *            the value of the attribute
	 */
	public static Element createPFieldNodeNS(Document aDocument, String aFieldName) {
		Element fieldNode = aDocument.createElementNS(PATTERN_NS, FIELD_PATTERN_ELEMENT);
		fieldNode.setAttributeNS(null, NAME_FIELD_ATTRIBUTE, aFieldName);
		return fieldNode;
	}

	/**
	 * This method returns {@link #createPFieldNodeNS(Document, String)}
	 * together with a style attribute.
	 */
	public static Element createStyledPFieldNodeNS(Document aDocument, String aFieldName, String aStyle) {
		Element fieldNode = createPFieldNodeNS(aDocument, aFieldName);
		fieldNode.setAttributeNS(null, STYLE_TEMPLATE_ATTRIBUTE, aStyle);
		return fieldNode;
	}

	/**
	 * This method creates an {@link Element} with qualified name
	 * {@link FormPatternConstants#SELF_PATTERN_ELEMENT}.
	 * 
	 * @param aDocument
	 *            the {@link Document} to build the {@link Element} for.
	 */
	public static Element createPSelfNodeNS(Document aDocument) {
		return aDocument.createElementNS(PATTERN_NS, SELF_PATTERN_ELEMENT);
	}

	/**
	 * This method creates en {@link Element} with namespace
	 * {@link FormTemplateConstants#TEMPLATE_NS}, qualified name
	 * {@link FormTemplateConstants#TEXT_TEMPLATE_ELEMENT} and attribute
	 * {@link FormTemplateConstants#KEY_TEXT_ATTRIBUTE}.
	 * 
	 * @param aDocument
	 *            the {@link Document} to build the {@link Element} for.
	 * @param aKey
	 *            the value of the attribute
	 */
	public static Element createTTextNode(Document aDocument, String aKey) {
		Element textNode = aDocument.createElementNS(TEMPLATE_NS, TEXT_TEMPLATE_ELEMENT);
		textNode.setAttribute(KEY_TEXT_ATTRIBUTE, aKey);
		return textNode;
	}

}
