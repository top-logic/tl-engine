/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.kafka.interceptor.TopicViewerInterceptor;
import com.top_logic.kafka.layout.kafka.ProgressTreeTableComponent.TreeTableComponentValueUpdater;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * Identify kafka topics which have changed their message or state.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class KafkaTableUpdater implements TreeTableComponentValueUpdater {

	@Override
	public void update(ProgressTreeTableComponent progressTableComponent) {
		TreeTableModel tableModel = (TreeTableModel) progressTableComponent.getTableModel();

		for (KafkaTopic<?> topic : TopicViewerInterceptor.getTopics()) {
			MutableTLTreeNode<?> root = (MutableTLTreeNode<?>) tableModel.getTreeModel().getRoot();
			MutableTLTreeNode<?> nodeForTopic = findNode(root, topic);

			// the topic is known
			if (tableModel.containsRowObject(nodeForTopic)) {
				// the topic is visible
				int rowIndex = tableModel.getRowOfObject(nodeForTopic);

				if (TableModel.NO_ROW != rowIndex) {
					updateTopic(tableModel, nodeForTopic, topic, rowIndex);
				} else {
					// topic is invisible -> ignore
				}
			} else {
				// add a new topic
				createNode(root, topic);
			}
		}

	}

	private void updateTopic(TableModel model, MutableTLTreeNode<?> node, KafkaTopic<?> topic, int rowIndex) {
		List<KafkaMessage> messages = CollectionUtil.copyOnly(KafkaMessage.class, topic.getMessages());
		boolean hasChanged = false;
		int size = node.getChildCount();
		List<Integer> toRemove = new ArrayList<>();

		for (int pos = size - 1; pos >= 0; pos--) {
			MutableTLTreeNode<?> child = node.getChildAt(pos);
			KafkaMessage bo = (KafkaMessage) child.getBusinessObject();

			if (!messages.contains(bo)) {
				toRemove.add(pos);
				hasChanged = true;
			} else {
				messages.remove(bo);
			}
		}

		for (Integer pos : toRemove) {
			node.removeChild(pos);
		}

		for (KafkaMessage message : messages) {
			node.createChild(IndexPosition.START, message);

			hasChanged = true;
		}

		if (hasChanged) {
			model.updateRows(rowIndex, rowIndex + node.getChildCount() + 1);
		}
	}

	private MutableTLTreeNode<?> findNode(MutableTLTreeNode<?> node, KafkaTopic<?> topic) {
		if (topic.equals(node.getBusinessObject())) {
			return node;
		} else {
			for (MutableTLTreeNode<?> child : node.getChildren()) {
				MutableTLTreeNode<?> theNode = findNode(child, topic);

				if (theNode != null) {
					return theNode;
				}
			}

			return null;
		}
	}

	private MutableTLTreeNode<?> createNode(MutableTLTreeNode<?> parent, Object businessObject) {
		return createNode(parent, IndexPosition.END, businessObject);
	}

	private MutableTLTreeNode<?> createNode(MutableTLTreeNode<?> parent, IndexPosition pos, Object businessObject) {
		return parent.createChild(pos, businessObject);
	}
}
