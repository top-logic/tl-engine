/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.ArrayList;
import java.util.Collections;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.decorator.DetailDecorator.Context;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.tree.TreeNodeUnwrappingProvider;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Command}, that opens a detail dialog of comparison change.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OpenChangeDetailDialog implements Command {

	private final CompareInfo _compareInfo;
	private DetailDecorator _decorator;
	private Context _context;
	private TableData _tableData;

	private Object _rowObject;

	/**
	 * Create a new {@link OpenChangeDetailDialog}.
	 */
	public OpenChangeDetailDialog(DetailDecorator decorator, Context context, TableData tableData,
			Object rowObject, CompareInfo compareInfo) {
		_decorator = decorator;
		_context = context;
		_tableData = tableData;
		_rowObject = rowObject;
		_compareInfo = compareInfo;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		_tableData.getSelectionModel().setSelection(Collections.singleton(_rowObject));

		final DialogModel dialogModel = createDialogModel();
		TableConfiguration tableConfiguration = createTreeTableConfiguration();
		TreeTableField treeTableField = createTreeTableField(_compareInfo, tableConfiguration);
		ArrayList<CommandModel> dialogCommands = createDialogCommands(dialogModel, treeTableField);

		return MessageBox.open(context.getWindowScope(), dialogModel,
			new TableControl(treeTableField, DefaultTableRenderer.newInstance()), dialogCommands);
	}

	TableConfiguration createTreeTableConfiguration() {
		NoDefaultColumnAdaption mainConfiguration = new ChangeDetailDialogConfiguration(_decorator, _context);

		TableConfiguration tableConfiguration =
			TableConfigurationFactory.build(mainConfiguration, TreeNodeUnwrappingProvider.INSTANCE);
		return tableConfiguration;
	}

	private TreeTableField createTreeTableField(CompareInfo compareInfo, TableConfiguration tableConfiguration) {
		final DefaultTreeTableModel tableModel = createTableModel(compareInfo, tableConfiguration);
		TreeTableField treeTableField = FormFactory.newTreeTableField("compareDetailTable", ConfigKey.none());
		treeTableField.setTree(tableModel);
		return treeTableField;
	}

	DefaultTreeTableModel createTableModel(CompareInfo compareInfo, TableConfiguration tableConfiguration) {
		final DefaultTreeTableModel tableModel =
			new DefaultTreeTableModel(new ChangeDetailBuilder(compareInfo), new CompareDetailRow(
				TypeBasedLabelProvider.INSTANCE.getLabel(compareInfo.getDisplayedObject()),
				compareInfo),
				Collections.<String> emptyList(), tableConfiguration);
		tableModel.setRootVisible(true);
		tableModel.setExpanded(tableModel.getRoot(), true);
		return tableModel;
	}

	private DefaultDialogModel createDialogModel() {
		DefaultDialogModel dialogModel =
			new DefaultDialogModel(new DefaultLayoutData(DisplayDimension.dim(640, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(530, DisplayUnit.PIXEL), 100, Scrolling.AUTO),
				new ResourceText(I18NConstants.COMPARE_DETAIL_DIALOG_TITLE), true, true, null);
		return dialogModel;
	}

	private ArrayList<CommandModel> createDialogCommands(final DialogModel dialogModel,
			final TreeTableField treeTableField) {
		ArrayList<CommandModel> dialogCommands = new ArrayList<>();
		dialogCommands.add(MessageBox.button(I18NConstants.COMPARE_DETAIL_PREVIOUS_CHANGE,
			new SwitchDetailCommand(treeTableField, _tableData, new SelectPreviousChange(_tableData))));
		CommandModel nextButton = MessageBox.button(I18NConstants.COMPARE_DETAIL_NEXT_CHANGE,
			new SwitchDetailCommand(treeTableField, _tableData, new SelectNextChange(_tableData)));
		dialogCommands.add(nextButton);
		dialogModel.setDefaultCommand(nextButton);
		dialogCommands.add(MessageBox.button(ButtonType.CLOSE,
			(Command) context -> dialogModel.getCloseAction().executeCommand(context)));
		return dialogCommands;
	}

	private final class SwitchDetailCommand implements Command {

		private TreeTableField _detailTreeTableField;

		private TableData _tableData;
		private Command _switchDirectionCommand;

		SwitchDetailCommand(TreeTableField detailTreeTableField, TableData tableData,
				Command switchCommand) {
			_detailTreeTableField = detailTreeTableField;
			_tableData = tableData;
			_switchDirectionCommand = switchCommand;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			int rowIndex = TableUtil.getSingleSelectedRow(_tableData);
			_switchDirectionCommand.executeCommand(context);
			if (rowIndex != TableUtil.getSingleSelectedRow(_tableData)) {
				int nextRowIndex = TableUtil.getSingleSelectedRow(_tableData);
				CompareInfo nextCompareInfo =
					(CompareInfo) _tableData.getViewModel().getValueAt(nextRowIndex, DecorateService.DECORATION_COLUMN);
				DefaultTreeTableModel newTableModel =
					createTableModel(nextCompareInfo, createTreeTableConfiguration());
				_detailTreeTableField.setTree(newTableModel);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private static class SelectNextChange implements Command {

		private TableData _tableData;

		SelectNextChange(TableData tableData) {
			_tableData = tableData;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			TableViewModel viewModel = _tableData.getViewModel();
			int rowIndex = TableUtil.getSingleSelectedRow(_tableData);
			CompareService.selectNextChangeAfter(_tableData, viewModel.getRowObject(rowIndex));
			if (rowIndex == TableUtil.getSingleSelectedRow(_tableData)) {
				CompareService.showNoNextChangeMessage();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	private static class SelectPreviousChange implements Command {

		private TableData _tableData;

		SelectPreviousChange(TableData tableData) {
			_tableData = tableData;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			TableViewModel viewModel = _tableData.getViewModel();
			int rowIndex = TableUtil.getSingleSelectedRow(_tableData);
			CompareService.selectPreviousChangeBefore(_tableData, viewModel.getRowObject(rowIndex));
			if (rowIndex == TableUtil.getSingleSelectedRow(_tableData)) {
				CompareService.showNoPreviousChangeMessage();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

	}
}