/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.config.xdiff.XApplyException;

/**
 * Utilities for XPath queries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XPathUtil {

	/**
	 * Find a node referenced by an xpath from a given context node.
	 */
	public static Element findElement(String xpath, Node context) throws XApplyException {
		Node node = findNode(xpath, context);
		if (!(node instanceof Element)) {
			throw new XApplyException("Not an element node referenced by '" + xpath + "'.");
		}
		return (Element) node;
	}

	/**
	 * Find an element node referenced by an xpath from a given context node.
	 */
	public static Node findNode(String xpath, Node context) throws XApplyException {
		try {
			return (Node) xpath().evaluate(xpath, context, XPathConstants.NODE);
		} catch (XPathExpressionException ex) {
			throw new XApplyException("Node deletion failed.", ex);
		}
	}

	static XPath xpath() {
		return XPathFactory.newInstance().newXPath();
	}

}
