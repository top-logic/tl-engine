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
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Fragment;
import com.top_logic.config.xdiff.model.FragmentBase;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;

/**
 * Generation of {@link NodeValue} operations from a {@link XDiff} result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class NodeValueComputation extends AbstractNodeVisitor<Void, ChildrenTraversal> {

	private static final Void none = null;

	String _component;

	final DocumentPosition _insertPosition = new DocumentPosition();

	protected List<MSXDiff> _operations = new ArrayList<>();

	public NodeValueComputation(String component) {
		_component = component;
	}

	public List<MSXDiff> getOperations() {
		return _operations;
	}

	@Override
	public Void visitElement(Element node, ChildrenTraversal arg) {
		if (isDiff(node)) {
			descend(node, arg);
			return none;
		} else if (isXDiffElement(node)) {
			return none;
		} else {
			arg.traverseChild(node);
			Void result = super.visitElement(node, arg);
			return result;
		}
	}

	@Override
	protected Void visitFragmentBase(FragmentBase node, ChildrenTraversal arg) {
		Void result = super.visitFragmentBase(node, arg);

		descendNextLevel(node);

		return result;
	}

	@Override
	public Void visitDocument(com.top_logic.config.xdiff.model.Document node, ChildrenTraversal arg) {
		descendNextLevel(node);
		return none;
	}

	@Override
	public Void visitFragment(Fragment node, ChildrenTraversal arg) {
		descend(node, arg);
		return none;
	}

	private void descendNextLevel(FragmentBase node) {
		ChildrenTraversal childrenTraversal = _insertPosition.childrenTraversal();
		descend(node, childrenTraversal);
		childrenTraversal.stop();
	}

	protected void descend(FragmentBase node, ChildrenTraversal childrenVisit) {
		for (Node child : node.getChildren()) {
			descendLocal(child, childrenVisit);
		}
	}

	private void descendLocal(Node child, ChildrenTraversal arg) {
		child.visit(this, arg);
	}

	@Override
	protected Void visitNode(Node node, ChildrenTraversal arg) {
		return none;
	}

}
