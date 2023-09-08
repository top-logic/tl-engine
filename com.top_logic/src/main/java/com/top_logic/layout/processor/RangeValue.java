/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.processor.Expansion.Buffer;

/**
 * {@link ParameterValue} that expands to a document range.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RangeValue extends ParameterValue {

	public static ParameterValue createArgumentValue(String paramName, Element parentElement) {
		if (hasAttributes(parentElement) || !onlyText(parentElement)) {
			return new RangeValue(paramName, parentElement, false);
		} else {
			return new StringValue(paramName, textContent(parentElement));
		}
	}

	public static ParameterValue createDefaultValue(String paramName, Element parentElement) {
		if (onlyText(parentElement)) {
			return new StringValue(paramName, textContent(parentElement));
		} else {
			return new RangeValue(paramName, parentElement, true);
		}
	}

	private static String textContent(Element parentElement) {
		Node firstChild = parentElement.getFirstChild();
		if (firstChild == null) {
			return "";
		} else if (firstChild.getNextSibling() == null) {
			if (isTextNode(firstChild)) {
				return firstChild.getTextContent();
			} else {
				return "";
			}
		} else {
			StringBuilder buffer = new StringBuilder();
			for (Node child : DOMUtil.children(parentElement)) {
				if (isTextNode(child)) {
					buffer.append(child.getTextContent());
				}
			}
			return buffer.toString();
		}
	}

	private static boolean onlyText(Element parentElement) {
		for (Node child : DOMUtil.children(parentElement)) {
			if (!isTextNode(child)) {
				return false;
			}
			if (ConfigurationWriter.containsAttributeSpecial(child.getTextContent())) {
				return false;
			}
		}
		return true;
	}

	private static boolean isTextNode(Node child) {
		short nodeType = child.getNodeType();
		return nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE;
	}

	private static boolean hasAttributes(Element parentElement) {
		return parentElement.getAttributes().getLength() > 0;
	}

	private Element _rangeElement;

	private boolean _contentOnly;

	/**
	 * Creates a {@link RangeValue}.
	 * 
	 * @param paramName
	 *        See {@link #getParamName()}.
	 * @param rangeElement
	 *        The nodes that are copied to the destination upon expansion.
	 * @param contentOnly
	 *        Whether only the contents of the given range should be copied.
	 */
	private RangeValue(String paramName, Element rangeElement, boolean contentOnly) {
		super(paramName);
		_rangeElement = rangeElement;
		_contentOnly = contentOnly;
	}

	@Override
	protected List<? extends Node> internalExpand(Buffer out) throws ElementInAttributeContextError {
		return out.appendRange(_rangeElement, _contentOnly, expansion());
	}

}