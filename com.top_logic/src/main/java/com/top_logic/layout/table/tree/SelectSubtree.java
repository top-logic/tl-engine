/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.TreeSelectionModel.NodeSelectionState;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} selecting all nodes in the subtree.
 */
public class SelectSubtree extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link SelectSubtree}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Whether the subtree is selected or deselected.
		 */
		boolean getSelect();

		/**
		 * The number of tree levels to process.
		 * 
		 * <p>
		 * A value of <code>0</code> means "all levels".
		 * </p>
		 */
		int getLevels();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();
	}

	private static final ExecutableState NO_EXEC_FULL = ExecutableState.createDisabledState(I18NConstants.NO_EXEC_FULL);

	private static final ExecutableState NO_EXEC_NONE = ExecutableState.createDisabledState(I18NConstants.NO_EXEC_NONE);

	/**
	 * Well-known ID for the sub-tree select command in the {@link CommandHandlerFactory}.
	 */
	public static final String SELECT_SUBTREE_ID = "treeTableSelectSubtree";

	/**
	 * Well-known ID for the sub-tree deselect command in the {@link CommandHandlerFactory}.
	 */
	public static final String DESELECT_SUBTREE_ID = "treeTableDeselectSubtree";

	/**
	 * Creates a {@link SelectSubtree}.
	 */
	public SelectSubtree(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		Object directTarget = someArguments.get(ContextMenuUtil.DIRECT_TARGET);

		if (component instanceof TableDataOwner table) {
			SelectionModel<Object> selectionModel = table.getTableData().getSelectionModel();
			if (selectionModel instanceof TreeSelectionModel<Object> treeSelection) {
				int levels = levels();
				if (levels == 0) {
					treeSelection.setSelectedSubtree(directTarget, select());
				} else {
					selectLevels(treeSelection, directTarget, levels);
				}
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void selectLevels(TreeSelectionModel<Object> treeSelection, Object directTarget, int levels) {
		Object update = treeSelection.startBulkUpdate();
		try {
			selectLevel(treeSelection, directTarget, levels);
		} finally {
			treeSelection.completeBulkUpdate(update);
		}
	}

	private void selectLevel(TreeSelectionModel<Object> treeSelection, Object directTarget, int levels) {
		treeSelection.setSelected(directTarget, select());

		if (levels > 0) {
			int childLevels = levels - 1;

			for (Object child : treeSelection.getTreeModel().getChildren(directTarget)) {
				selectLevel(treeSelection, child, childLevels);
			}
		}
	}

	/**
	 * The selection state to establish.
	 */
	protected boolean select() {
		// Note: Must not be cached, since the label depends on this property and that is accessed
		// by the super-constructor.
		return config().getSelect();
	}

	/**
	 * The number of selection levels.
	 */
	protected int levels() {
		return config().getLevels();
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return (component, model, args) -> executableState(component, args.get(ContextMenuUtil.DIRECT_TARGET));
	}

	private ExecutableState executableState(LayoutComponent component, Object directTarget) {
		if (directTarget == null || directTarget instanceof Collection<?>) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		if (component instanceof TableDataOwner table) {
			SelectionModel<Object> selectionModel = table.getTableData().getSelectionModel();
			if (selectionModel instanceof TreeSelectionModel<Object> treeSelection) {
				if (levels() == 0 && treeSelection.getNodeSelectionState(directTarget) == disabledState()) {
					return select() ? NO_EXEC_FULL : NO_EXEC_NONE;
				}

				return ExecutableState.EXECUTABLE;
			}
		}

		return ExecutableState.NOT_EXEC_HIDDEN;
	}

	private NodeSelectionState disabledState() {
		return select() ? NodeSelectionState.FULL : NodeSelectionState.NONE;
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return select() ? I18NConstants.SELECT_SUBTREE : I18NConstants.DESELECT_SUBTREE;
	}

}
