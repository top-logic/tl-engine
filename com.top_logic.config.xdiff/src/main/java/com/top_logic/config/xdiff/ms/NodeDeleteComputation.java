/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.compare.XDiffSchema.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.model.AbstractNodeVisitor;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Fragment;
import com.top_logic.config.xdiff.model.FragmentBase;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;

/**
 * Generation of {@link NodeDelete} operations from a {@link XDiff} result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class NodeDeleteComputation extends AbstractNodeVisitor<Void, ChildrenTraversal> {

	private static final Void none = null;

	private String _component;

	private final DocumentPosition _deletePosition = new DocumentPosition();

	private List<MSXDiff> _deletes = new ArrayList<>();

	public NodeDeleteComputation(String component) {
		_component = component;
	}

	public List<MSXDiff> getDeletes() {
		return _deletes;
	}

	@Override
	public Void visitElement(Element node, ChildrenTraversal arg) {
		if (isDelete(node)) {
			for (Node deleted : node.getChildren()) {
				// Text delete and add is realized through node value operations.
				if (deleted.getNodeType() != NodeType.TEXT) {
					arg.traverseChild(deleted);
					_deletes.add(
						new NodeDelete(_component, _deletePosition.getXPath()));
				}
			}
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
		ChildrenTraversal childrenTraversal = _deletePosition.childrenTraversal();
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
