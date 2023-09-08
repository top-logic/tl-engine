/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.table.control.IndexViewportState;
import com.top_logic.layout.table.control.IndexViewportStateParser;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link TreeControl} used for display of trees as fixed tree table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class StructureEditTreeControl extends TreeControl {

	private static class TableScrollUpdateAction extends ControlCommand {

		public static final String COMMAND_NAME = "updateScrollPosition";

		public static final StructureEditTreeControl.TableScrollUpdateAction INSTANCE = new TableScrollUpdateAction();

		/**
		 * Singleton constructor.
		 */
		protected TableScrollUpdateAction() {
			super(TableScrollUpdateAction.COMMAND_NAME);
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			StructureEditTreeControl treeControl = (StructureEditTreeControl) control;
			IndexViewportState tableViewportState = IndexViewportStateParser.getIndexViewportState(arguments);
			treeControl.setTableViewportState(tableViewportState);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SCROLL_UPDATE;
		}
	}
	
	private ScrollPositionModel _scrollPositionModel;
	
	/**
	 * Create a new {@link StructureEditTreeControl}.
	 */
	public StructureEditTreeControl(TreeData data, ScrollPositionModel scrollPositionModel) {
		super(data, createCommandMap(TREE_COMMANDS, TableScrollUpdateAction.INSTANCE));
		_scrollPositionModel = scrollPositionModel;
	}
	
	/**
	 * @see #getTableViewportState()
	 */
	public void setTableViewportState(IndexViewportState tableViewportState) {
		_scrollPositionModel.setScrollPosition(tableViewportState);
	}

	/**
	 * {@link IndexViewportState scroll position} of this tree, if it is rendered as tree
	 *         table.
	 */
	public IndexViewportState getTableViewportState() {
		return _scrollPositionModel.getScrollPosition();
	}
	
}