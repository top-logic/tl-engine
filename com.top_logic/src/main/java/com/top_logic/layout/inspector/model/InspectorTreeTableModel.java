/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.inspector.InspectorComponent;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link DefaultTreeTableModel} for {@link InspectorComponent}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InspectorTreeTableModel extends IndexedTreeTableModel<InspectorTreeNode> {

	private Filter<? super InspectorTreeNode> _filter;

	/**
	 * Creates a new {@link InspectorTreeTableModel}.
	 */
	public InspectorTreeTableModel(TreeBuilder<InspectorTreeNode> builder, InspectorModel model,
			List<String> columnNames, TableConfiguration config) {
		super(builder, model.getInspected(), columnNames, config);
		_filter = model.getFilter();
		setRootVisible(true);
		getRoot().setExpanded(true);
	}

	/**
	 * The currently active filter to apply when creating children nodes.
	 * 
	 * @see InspectorTreeNode#filter()
	 */
	public Filter<? super InspectorTreeNode> getNodeFilter() {
		return _filter;
	}

}

