/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DynamicDelegatingCommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * Util class to write the table filter options dialog.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class FilterOptionsUtil {

	private static final ResKey FILTER_OPTIONS_TOOLTIP = DefaultTableRenderer.GENERAL_PREFIX.key("filterOptions");

	private static final String INCLUDE_PARENTS_FIELD_NAME = "includeParents";
	private static final String INCLUDE_CHILDREN_FIELD_NAME = "includeChildren";

	/**
	 * {@link Command} for the apply button.
	 */
	private static class ApplyFilterOptionsExecutable implements Command {

		private final TableViewModel viewModel;
		private final FormGroup optionsGroup;

		public ApplyFilterOptionsExecutable(TableViewModel viewModel, FormGroup optionsGroup) {
			this.viewModel = viewModel;
			this.optionsGroup = optionsGroup;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			FormField field = optionsGroup.getField(INCLUDE_PARENTS_FIELD_NAME);
			boolean includeParents = Utils.getbooleanValue(field.getValue());
			field = optionsGroup.getField(INCLUDE_CHILDREN_FIELD_NAME);
			boolean includeChildren = Utils.getbooleanValue(field.getValue());
			viewModel.setFilterOptions(includeParents, includeChildren);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * {@link AbstractTemplateDialog} to edit filter options for tree tables.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class FilterOptionsDialog extends AbstractTemplateDialog {

		private TableViewModel _viewModel;

		/**
		 * Creates a {@link FilterOptionsDialog}.
		 */
		public FilterOptionsDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height,
				TableViewModel viewModel) {
			super(dialogTitle, width, height);
			_viewModel = viewModel;
		}

		@Override
		protected TagTemplate getTemplate() {
			return div(
				fieldBoxInputFirst(INCLUDE_PARENTS_FIELD_NAME), fieldBoxInputFirst(INCLUDE_CHILDREN_FIELD_NAME));
		}

		@Override
		protected void fillFormContext(FormContext context) {
			boolean includeParentsValue = _viewModel.isFiniteTree() && _viewModel.isFilterIncludeParents();
			boolean includeParentsImmutable = !_viewModel.isFiniteTree();
			BooleanField includeParentsField = FormFactory.newBooleanField(INCLUDE_PARENTS_FIELD_NAME,
				includeParentsValue, includeParentsImmutable);
			includeParentsField.setLabel(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS));
			if (_viewModel.isFiniteTree()) {
				includeParentsField.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP));
			} else {
				includeParentsField
					.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP_INFINITE_TREE));
			}
			context.addMember(includeParentsField);

			BooleanField includeChildrenField = FormFactory.newBooleanField(INCLUDE_CHILDREN_FIELD_NAME,
				Boolean.valueOf(_viewModel.isFilterIncludeChildren()), false);
			includeChildrenField.setLabel(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_CHILDREN));
			includeChildrenField.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_CHILDREN_TOOLTIP));
			context.addMember(includeChildrenField);
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			CommandModel ok = MessageBox.button(ButtonType.OK,
				new Command.CommandChain(
					new ApplyFilterOptionsExecutable(_viewModel, getFormContext()),
					getDiscardClosure()));
			buttons.add(ok);

			addCancel(buttons);
		}

		private String translate(ResKey i18nKey) {
			return Resources.getInstance().getString(i18nKey);
		}

	}

	/**
	 * {@link Command} to open the filter options dialog.
	 */
	public static class OpenFilterOptionsCommand implements Command {

		private final TableData _table;

		/**
		 * Creates a new {@link OpenFilterOptionsCommand}.
		 */
		public OpenFilterOptionsCommand(TableData table) {
			_table = table;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			TableData tableData = _table;
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordOpenTreeFilter(tableData);
			}
			TableViewModel viewModel = tableData.getViewModel();
			if (!viewModel.hasFilterOptions()) {
				HandlerResult result = new HandlerResult();
				result.addErrorMessage(I18NConstants.FILTER_OPTIONS_NO_TREE_MODEL);
				return result;
			}

			DisplayDimension width =
				DisplayDimension.dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL);
			DisplayDimension height =
				DisplayDimension.dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_HEIGHT),
					DisplayUnit.PIXEL);
			return new FilterOptionsDialog(I18NConstants.FILTER_OPTIONS_TITLE, width, height, viewModel).open(context);
		}

	}

	/**
	 * The opener button for filter options dialog.
	 */
	public static CommandModel createFilterOptions(final TableData table) {
		OpenFilterOptionsCommand openFilterOptionsCommand = new OpenFilterOptionsCommand(table);

		ExecutabilityModel exectutable = new AbstractExecutabilityModel() {
			@Override
			protected ExecutableState calculateExecutability() {
				if (table.getViewModel().hasFilterOptions()) {
					return ExecutableState.EXECUTABLE;
				} else {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
			}
		};

		CommandModel commandModel = new OpenFilterOptionsCommandModel(openFilterOptionsCommand, exectutable);
		commandModel.setExecutable();
		TableButtons.initButton(commandModel, table, Icons.FILTER_OPTIONS, null, FILTER_OPTIONS_TOOLTIP);
		return commandModel;
	}

	/**
	 * {@link CommandModel} for {@link OpenFilterOptionsCommand}.
	 * 
	 * <p>
	 * This is an extra class to be able to identify it during application testing.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class OpenFilterOptionsCommandModel extends DynamicDelegatingCommandModel {

		OpenFilterOptionsCommandModel(OpenFilterOptionsCommand aCommand, ExecutabilityModel anExec) {
			super(aCommand, anExec);
		}

	}

}
