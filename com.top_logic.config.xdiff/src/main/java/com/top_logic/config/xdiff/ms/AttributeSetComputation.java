/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.compare.XDiffSchema.*;
import static com.top_logic.config.xdiff.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.model.AbstractNodeVisitor;
import com.top_logic.config.xdiff.model.Attribute;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Fragment;
import com.top_logic.config.xdiff.model.FragmentBase;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.QNameOrder;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;

/**
 * Generation of {@link AttributeSet} operations from a {@link XDiff} result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class AttributeSetComputation extends AbstractNodeVisitor<Void, ChildrenTraversal> {

	private static final Void none = null;

	private String _component;

	private final DocumentPosition _position = new DocumentPosition();

	private List<MSXDiff> _sets = new ArrayList<>();

	public AttributeSetComputation(String component) {
		_component = component;
	}

	public List<MSXDiff> getAttributeSets() {
		return _sets;
	}

	@Override
	public Void visitElement(Element node, ChildrenTraversal arg) {
		if (isAttributes(node)) {
			String xPath = _position.getXPath();
			Element removeElement = null;
			Element addElement = null;
			for (Node operation : node.getChildren()) {
				if (operation instanceof Element) {
					Element operationElement = (Element) operation;
					if (isAttributeRemove(operationElement)) {
						if (removeElement != null) {
							throw new IllegalArgumentException(
								"No duplicate attribute remove operation allowed.");
						}
						removeElement = operationElement;

						if (addElement != null) {
							throw new IllegalArgumentException(
								"Attribute add operation must not preceede attribute remove operation.");
						}
					} else if (isAttributeAdd(operationElement)) {
						if (addElement != null) {
							throw new IllegalArgumentException(
								"No duplicate attribute add operation allowed.");
						}
						addElement = operationElement;

						if (removeElement != null) {
							// Check that no attributes are removed that are not also added, since
							// attribute removal is not supported.
							if (!isSubset(QNameOrder.INSTANCE,
									removeElement.getOrderedAttributes(), addElement.getOrderedAttributes())) {

								throw new MSXApplyError("Attribute removal is not supported.");
							}
						}
						for (Attribute attr : operationElement.getOrderedAttributes()) {
							if (attr.getNamespace() != null) {
								throw new MSXApplyError("Setting attributes with namespace is not supported.");
							}
							String name = attr.getLocalName();
							String value = attr.getValue();
							_sets.add(new AttributeSet(_component, xPath, name, value));
						}
					}
				}
			}

			if (addElement == null && removeElement != null && removeElement.getAttributeCount() > 0) {
				throw new MSXApplyError("Attribute removal is not supported.");
			}

			return none;
		} else if (isInsert(node)) {
			descendLocal(node, arg);
			return none;
		} else if (isDiff(node)) {
			descendLocal(node, arg);
			return none;
		} else if (isXDiffElement(node)) {
			return none;
		}

		return super.visitElement(node, arg);
	}

	@Override
	public Void visitDocument(Document node, ChildrenTraversal arg) {
		descend(node);
		return none;
	}

	@Override
	public Void visitFragment(Fragment node, ChildrenTraversal arg) {
		descend(node);
		return none;
	}

	@Override
	protected Void visitFragmentBase(FragmentBase node, ChildrenTraversal arg) {
		Void result = super.visitFragmentBase(node, arg);

		descend(node);

		return result;
	}

	private void descend(FragmentBase node) {
		ChildrenTraversal childrenTraversal = _position.childrenTraversal();
		descendLocal(node, childrenTraversal);
		childrenTraversal.stop();
	}

	private void descendLocal(FragmentBase node, ChildrenTraversal childrenTraversal) {
		for (Node child : node.getChildren()) {
			child.visit(this, childrenTraversal);
		}
	}

	@Override
	protected Void visitNode(Node node, ChildrenTraversal arg) {
		arg.traverseChild(node);
		return none;
	}

}
