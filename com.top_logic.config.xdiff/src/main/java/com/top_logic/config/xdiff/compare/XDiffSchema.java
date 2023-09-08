/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import static com.top_logic.config.xdiff.compare.XDiffSchemaConstants.*;
import static com.top_logic.config.xdiff.model.NodeFactory.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.config.xdiff.model.Attribute;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;

/**
 * Factory to create XML difference descriptions in the {@link XDiffSchemaConstants} language.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XDiffSchema {

	/**
	 * Creates a synthetic root node, if the root nodes for the case if the analyzed documents
	 * differ.
	 */
	public static Node syntheticRoot(List<Node> nodes) {
		Element element = element(DIFF_ELEMENT_NAME);
		element.setChildren(nodes);
		return element;
	}

	/**
	 * {@link #deleteNode(Node)} followed by an {@link #insertNode(Node)}
	 */
	public static Node deleteInsert(Node oldNode, Node newNode) {
		return fragment(deleteNode(oldNode), insertNode(newNode));
	}

	/**
	 * The insertion of all given nodes.
	 */
	public static Node insertNodes(List<Node> nodes) {
		Element element = element(INSERT_ELEMENT_NAME);
		element.setChildren(nodes);
		return element;
	}

	/**
	 * Insertion of the given node.
	 */
	public static Node insertNode(Node node) {
		return insertNodes(Collections.singletonList(node));
	}

	/**
	 * Deletion of the given node.
	 */
	public static Node deleteNode(Node node) {
		return deleteNodes(Collections.singletonList(node));
	}

	/**
	 * Deletion of all the given nodes.
	 */
	public static Node deleteNodes(List<Node> nodes) {
		Element element = element(DELETE_ELEMENT_NAME);
		element.setChildren(nodes);
		return element;
	}

	/**
	 * Update of an element by removing and adding attributes.
	 * 
	 * <p>
	 * Note: An attribute that exists on a node and shall be changed must be first deleted and then
	 * be added again with its new value.
	 * </p>
	 */
	public static Element attributeUpdate(List<Attribute> removedAttributes, List<Attribute> addedAttributes) {
		Element operation = attributesOperation();
	
		if ((!removedAttributes.isEmpty())) {
			operation.addChild(removeAttributes(removedAttributes));
		}
	
		if ((!addedAttributes.isEmpty())) {
			operation.addChild(addAttributes(addedAttributes));
		}
		return operation;
	}

	private static Element attributesOperation() {
		return element(ATTRIBUTES_ELEMENT_NAME);
	}

	private static Node removeAttributes(List<Attribute> deletedAttributes) {
		Element element = element(REMOVE_ELEMENT_NAME);
		element.setAttributes(deletedAttributes);
		return element;
	}

	private static Node addAttributes(List<Attribute> insertedAttributes) {
		Element element = element(ADD_ELEMENT_NAME);
		element.setAttributes(insertedAttributes);
		return element;
	}

	/**
	 * Whether the given element is from the namespace {@link XDiffSchemaConstants#XDIFF_NS}.
	 */
	public static boolean isXDiffElement(Element node) {
		return XDIFF_NS.equals(node.getNamespace());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#DIFF_ELEMENT}.
	 */
	public static boolean isDiff(Element node) {
		return DIFF_ELEMENT_NAME.equals(node.getQName());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#DELETE_ELEMENT}.
	 */
	public static boolean isDelete(Element node) {
		return DELETE_ELEMENT_NAME.equals(node.getQName());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#INSERT_ELEMENT}.
	 */
	public static boolean isInsert(Element node) {
		return INSERT_ELEMENT_NAME.equals(node.getQName());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#ATTRIBUTES_ELEMENT}.
	 */
	public static boolean isAttributes(Element node) {
		return ATTRIBUTES_ELEMENT_NAME.equals(node.getQName());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#ADD_ELEMENT}.
	 */
	public static boolean isAttributeAdd(Element node) {
		return ADD_ELEMENT_NAME.equals(node.getQName());
	}

	/**
	 * Whether the given element is a {@link XDiffSchemaConstants#REMOVE_ELEMENT}.
	 */
	public static boolean isAttributeRemove(Element node) {
		return REMOVE_ELEMENT_NAME.equals(node.getQName());
	}

}
