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
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.TreeSelectionModel.NodeSelectionState;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
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

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();
	}

	private static final ExecutableState NO_EXEC_FULL = ExecutableState.createDisabledState(I18NConstants.NO_EXEC_FULL);

	private static final ExecutableState NO_EXEC_NONE = ExecutableState.createDisabledState(I18NConstants.NO_EXEC_NONE);

	public static final String SELECT_SUBTREE_ID = "treeTableSelectSubtree";

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

		if (component instanceof TableDataOwner table) {
			SelectionModel selectionModel = table.getTableData().getSelectionModel();
			if (selectionModel instanceof TreeSelectionModel treeSelection) {
				treeSelection.setSelectedSubtree(model, select());
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The selection state to establish.
	 */
	protected boolean select() {
		// Note: Must not be cached, since the label depends on this property and that is accessed
		// by the super-constructor.
		return ((Config) getConfig()).getSelect();
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return (component, model, args) -> executableState(component, model);
	}

	private ExecutableState executableState(LayoutComponent component, Object model) {
		if (model == null || model instanceof Collection<?>) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		if (component instanceof TableDataOwner table) {
			SelectionModel selectionModel = table.getTableData().getSelectionModel();
			if (selectionModel instanceof TreeSelectionModel treeSelection) {
				if (treeSelection.getNodeSelectionState(model) == disabledState()) {
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
