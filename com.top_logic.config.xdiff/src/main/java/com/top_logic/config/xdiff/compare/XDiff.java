/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import static com.top_logic.config.xdiff.compare.XDiffSchema.*;
import static com.top_logic.config.xdiff.model.NodeFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.config.xdiff.model.Attribute;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QNameOrder;

/**
 * Algorithm to compute the structured difference of two XML documents in the {@link XDiffSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XDiff {
	
	private final MinimumEditPath _pathComputation;

	private final MatchDecision _matchAlgorithm;

	/**
	 * Computes differences between source and target documents using the
	 * {@link DefaultMatchDecision}.
	 * 
	 * @see #diff(MatchDecision, Document, Document)
	 */
	public static Node diff(Document source, Document target) {
		return diff(DefaultMatchDecision.INSTANCE, source, target);
	}

	/**
	 * Computes differences between source and target documents in the {@link XDiffSchema}.
	 * 
	 * @param matchDecision
	 *        The algorithm that decides about matching nodes.
	 * @param source
	 *        The source document.
	 * @param target
	 *        the target document.
	 * @return The difference that transforms the source into the target document.
	 */
	public static Node diff(MatchDecision matchDecision, Document source, Document target) {
		return new XDiff(matchDecision).matchNodes(source, target);
	}

	/**
	 * Creates a {@link XDiff}.
	 * 
	 * @param matchAlgorithm
	 *        The algorithm that decides, whether nodes can be transformed into each other with less
	 *        cost than deleting one and inserting the other.
	 */
	private XDiff(MatchDecision matchAlgorithm) {
		_matchAlgorithm = matchAlgorithm;
		_pathComputation = new MinimumEditPath(matchAlgorithm);
	}

	/**
	 * Computes the difference between the given nodes.
	 * 
	 * @param oldNode
	 *        The old node.
	 * @param newNode
	 *        The new node.
	 * @return The computed difference in the {@link XDiffSchemaConstants} language.
	 */
	public Node matchNodes(Node oldNode, Node newNode) {
		if (oldNode.equalsNode(newNode)) {
			return oldNode;
		}
		
		switch (oldNode.getNodeType()) {
			case DOCUMENT:
				List<Node> diff = diff(oldNode.getChildren(), newNode.getChildren());
				if (diff.size() > 1) {
					return document(syntheticRoot(diff));
				} else {
					return document(diff);
				}
			case ELEMENT:
				if (newNode.getNodeType() == NodeType.ELEMENT) {
					Element oldElement = (Element) oldNode;
					Element newElement = (Element) newNode;
					if (_matchAlgorithm.canMatch(oldElement, newElement)) {
						return matchElements(oldElement, newElement);
					}
				}
				break;
			case FRAGMENT:
				throw new IllegalArgumentException("Fragment nodes cannot be in the input of the difference.");
			default:
				// Fall through.
		}

		return deleteInsert(oldNode, newNode);
	}

	private Node matchElements(Element oldElement, Element newElement) {
		// Nodes can be transformed into each other.
		AttributesMatching matching =
			matchAttributes(oldElement.getOrderedAttributes(), newElement.getOrderedAttributes());
		
		Element result = element(oldElement.getNamespace(), oldElement.getLocalName());
		result.setAttributes(matching.matchingAttributes);
		
		List<Attribute> removedAttributes = matching.removedAttributes;
		List<Attribute> addedAttributes = matching.addedAttributes;
		if (!removedAttributes.isEmpty() || !addedAttributes.isEmpty()) {
			result.addChild(attributeUpdate(removedAttributes, addedAttributes));
		}
		
		result.addChildren(diff(oldElement.getChildren(), newElement.getChildren()));
		
		return result;
	}

	private List<Node> diff(List<Node> oldNodes, List<Node> newNodes) {
		if (oldNodes.isEmpty()) {
			if (newNodes.isEmpty()) {
				return Collections.emptyList(); 
			} else {
				return Collections.singletonList(insertNodes(newNodes));
			}
		} else {
			if (newNodes.isEmpty()) {
				return Collections.singletonList(deleteNodes(oldNodes));
			} else {
				return diffNonEmpty(oldNodes, newNodes);
			}
		}
	}

	private List<Node> diffNonEmpty(List<Node> oldNodes, List<Node> newNodes) {
		List<EditOperation> operations = _pathComputation.compute(oldNodes, newNodes);
		
		int diffSize = operations.size();
		int oldIndex = 0;
		int newIndex = 0;
		ArrayList<Node> diff = new ArrayList<>(diffSize);
		for (int n = 0; n < diffSize; n++) {
			switch (operations.get(n)) {
				case DELETE:
					diff.add(deleteNode(oldNodes.get(oldIndex++)));
					break;
				case INSERT:
					diff.add(insertNode(newNodes.get(newIndex++)));
					break;
				case MATCH:
					Node oldNode = oldNodes.get(oldIndex++);
					Node newNode = newNodes.get(newIndex++);
					diff.add(matchNodes(oldNode, newNode));
					break;
			}
		}
		
		return diff;
	}

	private AttributesMatching matchAttributes(List<Attribute> oldAttributes, List<Attribute> newAttributes) {
		AttributesMatching matching = new AttributesMatching();
		
		int n1 = 0, n2 = 0;
		int cnt1 = oldAttributes.size(), cnt2 = newAttributes.size();
		while (n1 < cnt1 && n2 < cnt2) {
			Attribute attribute1 = oldAttributes.get(n1);
			Attribute attribute2 = newAttributes.get(n2);
			int comparision = QNameOrder.INSTANCE.compare(attribute1, attribute2);
			if (comparision == 0) {
				n1++;
				n2++;
				if (attribute1.getValue().equals(attribute2.getValue())) {
					matching.matchingAttributes.add(attribute1);
				} else {
					matching.removedAttributes.add(attribute1);
					matching.addedAttributes.add(attribute2);
				}
			}
			else if (comparision < 0) {
				n1++;
				matching.removedAttributes.add(attribute1);
			}
			else {
				n2++;
				matching.addedAttributes.add(attribute2);
			}
		}
		
		while (n1 < cnt1) {
			Attribute attribute1 = oldAttributes.get(n1++);
			matching.removedAttributes.add(attribute1);
		}
		
		while (n2 < cnt2) {
			Attribute attribute2 = newAttributes.get(n2++);
			matching.addedAttributes.add(attribute2);
		}
		
		return matching;
	}

	private static class AttributesMatching {

		public final List<Attribute> matchingAttributes = new ArrayList<>();

		public final List<Attribute> removedAttributes = new ArrayList<>();

		public final List<Attribute> addedAttributes = new ArrayList<>();

		public AttributesMatching() {
			// Default constructor.
		}

	}

}
