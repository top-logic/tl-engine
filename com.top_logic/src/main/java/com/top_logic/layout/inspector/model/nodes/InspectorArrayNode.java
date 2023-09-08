/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;

/**
 * {@link InspectorTreeNode} in case model is actually an array of objects.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorArrayNode extends InspectorTreeNode {

	/**
	 * Maximum number of children to display at once.
	 */
	public static final int CHUNK_SIZE = 50;

	/**
	 * Creates a {@link InspectorArrayNode}.
	 */
	public InspectorArrayNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty field, Object[] userObject) {
		super(model, parent, userObject, field);
	}

	/**
	 * Creates the children of this node.
	 */
	@Override
	public List<InspectorTreeNode> makeChildren() {
		ArrayList<InspectorTreeNode> childList = new ArrayList<>();
		Object[] array = (Object[]) getBusinessObject();
		addChildren(childList, this, array, 0, array.length);
		return childList;
	}

	static void addChildren(ArrayList<? super InspectorTreeNode> childList, InspectorTreeNode parent, Object[] array,
			int start, int end) {
		AbstractTreeTableModel<InspectorTreeNode> model = parent.getModel();
		int numberElements = end - start;
		if (numberElements <= CHUNK_SIZE) {
			for (int position = start; position < end; position++) {
				childList.add(parent.makeValueNode(new InspectorCollectionNode.Index(position), array[position]));
			}
		} else {
			int elementsPerGroup = 1;
			int numberGroups = numberElements;
			while (numberGroups > CHUNK_SIZE) {
				elementsPerGroup *= CHUNK_SIZE;
				numberGroups = numberPartitions(numberGroups, CHUNK_SIZE);
			}
			int childStart = start;
			for (int i = 0; i < numberGroups - 1; i++) {
				int childEnd = childStart + elementsPerGroup;
				childList.add(new InspectorRangeNode(model, parent, array, childStart, childEnd));
				childStart = childEnd;
			}
			childList.add(new InspectorRangeNode(model, parent, array, childStart, end));
		}
	}

	private static int numberPartitions(int elements, int partitionSize) {
		return ((elements - 1) / partitionSize) + 1;
	}

}