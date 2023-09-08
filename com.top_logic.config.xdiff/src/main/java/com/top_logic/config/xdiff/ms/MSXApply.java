/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;


import static com.top_logic.basic.xml.DOMUtil.*;
import static com.top_logic.config.xdiff.ms.MSXDiffSchema.*;
import static com.top_logic.config.xdiff.util.Utils.*;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.XApplyException;

/**
 * {@link MSXDiffVisitor} that applies the diff to a target {@link Document}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSXApply implements MSXDiffVisitor<Void, Document> {

	private static final Void none = null;

	/**
	 * Singleton {@link MSXApply} instance.
	 */
	private static final MSXApply INSTANCE = new MSXApply();

	/**
	 * Applies the given differences to the given target document.
	 */
	public static void apply(Document diff, Document target) throws XApplyException {
		applyOperations(getSettingsElement(diff), target);
	}

	private static void applyOperations(Node diffFragment, Document target) throws XApplyException {
		MSXApply apply = INSTANCE;
		for (Element operationSpec : elementsNS(diffFragment, null)) {
			MSXDiff operation = MSXDiffParser.parseElement(operationSpec);
			operation.visit(apply, target);
		}
	}

	private MSXApply() {
		// Singleton constructor.
	}

	@Override
	public Void visitNodeDelete(NodeDelete diff, Document target) {
		String nodeXpath = diff.getNodeXpath();
		Node node = XPathUtil.findNode(nodeXpath, target);
		if (node == null) {
			throw new MSXApplyError("Node '" + nodeXpath + "' not found");
		}
		if (!(node instanceof Element)) {
			throw new MSXApplyError("Must only remove element nodes: " + nodeXpath);
		}
		node.getParentNode().removeChild(node);
		return none;
	}

	@Override
	public Void visitComplexNodeAdd(ComplexNodeAdd diff, Document target) {
		Node below = diff.getBelowNode(target);
		if (below == null) {
			throw new MSXApplyError("Node '" + diff.getBelowXpath() + "' not found");
		}
		Node before = diff.getBeforeNode(below);
		if ((before != null) && (before.getParentNode() != below)) {
			throw new MSXApplyError(
				"Node '" + diff.getBeforeXpath() + "' not a child of '" + diff.getBelowXpath() + "'.");
		}

		for (Node node = diff.getFragment().getFirstChild(); node != null; node = node.getNextSibling()) {
			Node newChild = target.importNode(node, true);
			if (newChild instanceof Text) {
				if (isWhiteSpace(newChild.getNodeValue())) {
					continue;
				}
			}
			if (!(newChild instanceof Element)) {
				throw new MSXApplyError("Must only add element nodes: " + diff.getBelowXpath());
			}

			below.insertBefore(newChild, before);
		}
		return none;
	}

	@Override
	public Void visitTextNodeAdd(TextNodeAdd diff, Document target) {
		Element newNode = target.createElementNS(null, diff.getNodeName());

		if (diff.getNodeText() != null) {
			Text content = target.createTextNode(diff.getNodeText());
			newNode.appendChild(content);
		}

		diff.addNode(target, newNode);
		return none;
	}

	@Override
	public Void visitNodeValue(NodeValue diff, Document target) {
		Element element = XPathUtil.findElement(diff.getElementXpath(), target);

		Node next;
		for (Node oldContent = element.getFirstChild(); oldContent != null; oldContent = next) {
			next = oldContent.getNextSibling();
			element.removeChild(oldContent);
		}

		if (diff.getNewText() != null) {
			Text newContent = target.createTextNode(diff.getNewText());
			element.appendChild(newContent);
		}
		return none;
	}

	@Override
	public Void visitAttributeSet(AttributeSet diff, Document target) {
		String xpath = diff.getElementXpath();
		Node node = XPathUtil.findElement(xpath, target);

		Element element = (Element) node;
		element.setAttributeNS(null, diff.getAttributeName(), diff.getAttributeValue());

		return none;
	}

	/**
	 * Finds the global {@link MSXDiffSchema#SETTINGS_ELEMENT} from a document in the
	 * {@link MSXDiffSchema}.
	 */
	public static Element getSettingsElement(Document diff) {
		try {
			return (Element) DOMUtil.selectSingleNode(diff,
				"/" + CONFIGURATION_ELEMENT
					+ "/" + COMPONENTS_ELEMENT
					+ "/" + CONFIG_CUSTOMIZATIONS_ELEMENT
					+ "/" + SETTINGS_ELEMENT);
		} catch (XPathExpressionException ex) {
			throw (AssertionError) new AssertionError("Finding settings element failed.").initCause(ex);
		}
	}

}
