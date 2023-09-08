/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.model.NodeFactory.*;
import static com.top_logic.config.xdiff.ms.MSXDiffSchema.*;

import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.DocumentBuilder;
import com.top_logic.config.xdiff.model.Node;

/**
 * Transformer that builds {@link Document}s from collections of {@link MSXDiff} operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSXDocumentBuilder implements MSXDiffVisitor<Node, Void> {

	private static final Void none = null;

	/**
	 * Singleton {@link MSXDocumentBuilder} instance.
	 */
	public static final MSXDocumentBuilder INSTANCE = new MSXDocumentBuilder();

	private MSXDocumentBuilder() {
		// Singleton constructor.
	}

	/**
	 * Transforms the given {@link MSXDiff} to an XML {@link Node}.
	 */
	public Node toXML(MSXDiff operation) {
		return operation.visit(this, none);
	}

	@Override
	public Node visitNodeDelete(NodeDelete diff, Void arg) {
		return element(null, NODE_DELETE_ELEMENT)
			.setAttributes(
				attr(NODE_DELETE__COMPONENT_ATTRIBUTE, diff.getComponent()),
				attr(NODE_DELETE__XPATH_ATTRIBUTE, diff.getNodeXpath()))
			.setEmpty(true);
	}

	@Override
	public Node visitComplexNodeAdd(ComplexNodeAdd diff, Void arg) {
		return element(null, NODE_ADD_ELEMENT)
			.setAttributes(
				attr(NODE_ADD__COMPONENT_ATTRIBUTE, diff.getComponent()),
				attr(NODE_ADD__XPATH_ATTRIBUTE, diff.getBelowXpath()),
				attr(NODE_ADD__BEFORE_XPATH_ATTRIBUTE, diff.getBeforeXpath()))
			.setChildren(DocumentBuilder.convertFromDOM(diff.getFragment()).getChildren());
	}

	@Override
	public Node visitTextNodeAdd(TextNodeAdd diff, Void arg) {
		return element(null, NODE_ADD_ELEMENT)
			.setAttributes(
				attr(NODE_ADD__COMPONENT_ATTRIBUTE, diff.getComponent()),
				attr(NODE_ADD__XPATH_ATTRIBUTE, diff.getBelowXpath()),
				attr(NODE_ADD__BEFORE_XPATH_ATTRIBUTE, diff.getBeforeXpath()),
				attr(NODE_ADD__NODE_NAME_ATTRIBUTE, diff.getNodeName()),
				attr(NODE_ADD__NODE_TEXT_ATTRIBUTE, diff.getNodeText()))
			.setEmpty(true);
	}

	@Override
	public Node visitNodeValue(NodeValue diff, Void arg) {
		return element(null, NODE_VALUE_ELEMENT)
			.setAttributes(
				attr(NODE_VALUE__COMPONENT_ATTRIBUTE, diff.getComponent()),
				attr(NODE_VALUE__XPATH_ATTRIBUTE, diff.getElementXpath()),
				attr(NODE_VALUE__VALUE_ATTRIBUTE, diff.getNewText()))
			.setEmpty(true);
	}

	@Override
	public Node visitAttributeSet(AttributeSet diff, Void arg) {
		return element(null, ATTRIBUTE_SET_ELEMENT)
			.setAttributes(
				attr(ATTRIBUTE_SET__COMPONENT_ATTRIBUTE, diff.getComponent()),
				attr(ATTRIBUTE_SET__XPATH_ATTRIBUTE, diff.getElementXpath()),
				attr(ATTRIBUTE_SET__NAME_ATTRIBUTE, diff.getAttributeName()),
				attr(ATTRIBUTE_SET__VALUE_ATTRIBUTE, diff.getAttributeValue()))
			.setEmpty(true);
	}

}
