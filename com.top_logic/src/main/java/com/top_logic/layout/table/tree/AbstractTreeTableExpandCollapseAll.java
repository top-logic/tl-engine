/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.table.TableModelUtils;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.tree.component.AbstractExpandCollapseAll;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * Abstract command that expands or collapses all nodes in a tree table.
 * 
 * @implNote Subclasses have to call {@link #prepare(TreeTableData)} in their
 *           {@link #prepare(LayoutComponent, Object, Map)} implementation.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public abstract class AbstractTreeTableExpandCollapseAll extends AbstractExpandCollapseAll {

	/** {@link TypedConfiguration} constructor for {@link AbstractTreeTableExpandCollapseAll}. */
	public AbstractTreeTableExpandCollapseAll(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Has to be called by subclasses in their {@link #prepare(LayoutComponent, Object, Map)}
	 * implementation.
	 */
	protected CommandStep prepare(TreeTableData data) {
		TreeUIModel<?> treeModel = data.getTree();
		if (treeModel == null) {
			return new Hide();
		}
		boolean expand = expand();
		if (expand && !treeModel.isFinite()) {
			return new Hide();
		}
		return Success.toSuccess(ignored -> {
			if (hasSelection(data)) {
				expandAndScrollToSelection(data, expand);
			} else {
				expandAndRestoreViewportState(data, expand);
			}
		});
	}

	private boolean hasSelection(TreeTableData data) {
		return !data.getSelectionModel().getSelection().isEmpty();
	}

	private void expandAndScrollToSelection(TreeTableData data, boolean expand) {
		TreeUIModelUtil.setExpandedAll(data.getTree(), expand);
		TableModelUtils.scrollToSelectedRow(data);
	}

	private void expandAndRestoreViewportState(TreeTableData data, boolean expand) {
		expandAndRestoreViewportState(data, expand, getClientViewportState(data));
	}

	private ViewportState getClientViewportState(TreeTableData data) {
		return data.getViewModel().getClientDisplayData().getViewportState();
	}

	private void expandAndRestoreViewportState(TreeTableData data, boolean expand, ViewportState state) {
		Object rowObjectAnchor = data.getViewModel().getRowObject(state.getRowAnchor().getIndex());
		TreeUIModelUtil.setExpandedAll(data.getTree(), expand);
		int newRowIndex = data.getViewModel().getRowOfObject(rowObjectAnchor);
		state.setRowAnchor(RowIndexAnchor.create(newRowIndex, state.getRowAnchor().getIndexPixelOffset()));
	}

}
