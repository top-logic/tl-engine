/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.compare.XDiffSchema.*;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.Text;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;

/**
 * {@link NodeValueComputation} that creates {@link NodeValue} operations for nodes with newly
 * created or changed text contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class NodeValueSetComputation extends NodeValueComputation {

	private static final Void none = null;

	private Set<String> updatedPaths = new HashSet<>();

	public NodeValueSetComputation(String component) {
		super(component);
	}

	public Set<String> getUpdatedPaths() {
		return updatedPaths;
	}

	@Override
	public Void visitElement(Element node, ChildrenTraversal arg) {
		if (isInsert(node)) {
			for (Node inserted : node.getChildren()) {
				// Text delete and add is realized through node value operations.
				if (inserted.getNodeType() == NodeType.TEXT) {
					String xpath = _insertPosition.getXPath();
					updatedPaths.add(xpath);
					_operations.add(new NodeValue(_component, xpath, ((Text) inserted).getContents()));
					return none;
				}
			}
			descend(node, arg);
			return none;
		} else {
			return super.visitElement(node, arg);
		}
	}

}
