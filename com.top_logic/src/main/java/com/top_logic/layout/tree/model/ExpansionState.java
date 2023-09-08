/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Externalized expansion state of a {@link TreeUIModel}.
 * 
 * <p>
 * An externalized expansion state is independent of the concrete node implementation object. The
 * externalized expansion state is expressed in paths of
 * {@link TreeUIModel#getBusinessObject(Object) business objects} from the a top-level node in the
 * tree to an expanded node.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpansionState<N> {

	/**
	 * Creates an {@link ExpansionState} of the given node.
	 * 
	 * @param treeModel
	 *        The {@link TreeUIModel} of the given node. Must not be <code>null</code>.
	 * @param node
	 *        The top-level tree node for which the expansion state should be externalized. Must not
	 *        be <code>null</code>.
	 * 
	 * @return The externalized expansion state. Never <code>null</code>.
	 */
	public static <N> ExpansionState<N> createExpansionState(TreeUIModel<N> treeModel, N node) {
		return new ExpansionState<>(treeModel, node, null);
	}

	private final Object _userObject;

	private final Map<Object, ExpansionState<N>> _expansionByUserObject;

	private ExpansionState(TreeUIModel<N> treeModel, N node, Object userObject) {
		_userObject = userObject;
		_expansionByUserObject = initExpansionStateMap(treeModel, node);
	}

	private Map<Object, ExpansionState<N>> initExpansionStateMap(TreeUIModel<N> treeModel, N node) {
		Map<Object, ExpansionState<N>> expansionByUserObject = null;
		for (N childNode : treeModel.getChildren(node)) {
			if (treeModel.isExpanded(childNode)) {
				Object childUserObject = treeModel.getUserObject(childNode);
				if (childUserObject != null) {
					if (expansionByUserObject == null) {
						expansionByUserObject = new HashMap<>();
					}
					ExpansionState<N> expansion = new ExpansionState<>(treeModel, childNode, childUserObject);
					expansionByUserObject.put(expansion._userObject, expansion);
				}
			}
		}
		return expansionByUserObject;
	}

	/**
	 * Apply this externalized {@link ExpansionState} to the given node.
	 * 
	 * @param treeModel
	 *        The {@link TreeUIModel} of the given node. Must not be <code>null</code>.
	 * @param node
	 *        The top-level node to which this {@link ExpansionState} should be applied to. Must not
	 *        be <code>null</code>.
	 */
	public void apply(TreeUIModel<N> treeModel, N node) {
		treeModel.setExpanded(node, true);

		if (_expansionByUserObject != null) {
			for (N childNode : treeModel.getChildren(node)) {
				ExpansionState<N> expansion = _expansionByUserObject.get(treeModel.getUserObject(childNode));
				if (expansion != null) {
					expansion.apply(treeModel, childNode);
				}
			}
		}
	}

}