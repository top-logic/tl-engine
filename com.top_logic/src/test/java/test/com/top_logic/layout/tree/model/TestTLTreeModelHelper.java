/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Factory for an infinite tree model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestTLTreeModelHelper {

	/**
	 * Creates a full tree of infinite depth with <code>numberOfChildren</code> children for each
	 * node.
	 * 
	 * the business objects of node <i>x</i> {@link String} is
	 * <ol>
	 * <li><code>rootName</code> when <i>x</i> is the root node, or
	 * <li><i>i</i> when <i>x</i> is the <i>i</i>^th child of the root node, or
	 * <li><i>y.i</i> when <i>x</i> is the <i>i</i>^th child of non root node and <code>y</code> is
	 * its business object.
	 * </ol>
	 * 
	 * @param numberOfChildren
	 *        the number of children for each node
	 * @param rootName
	 *        the business object of the root node
	 */
	public static DefaultMutableTLTreeModel createInfiniteTree(final int numberOfChildren, final String rootName) {
		return new DefaultMutableTLTreeModel(new AbstractTLTreeNodeBuilder() {

			@Override
			public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
				AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model = node.getModel();
				final Object userObject = node.getBusinessObject();
				if (!(userObject instanceof String)) {
					return Collections.emptyList();
				}
				String s = (String) userObject;
				ArrayList<DefaultMutableTLTreeNode> result = new ArrayList<>();
				if (s == rootName) {
					for (int index = 0; index < numberOfChildren; index++) {
						result.add(createNode(model, node, Integer.toString(index)));
					}
				} else {
					for (int index = 0; index < numberOfChildren; index++) {
						result.add(createNode(model, node, s + "." + Integer.toString(index)));
					}
				}
				return result;
			}

			@Override
			public boolean isFinite() {
				return false;
			}

		}, rootName);
	}

}
