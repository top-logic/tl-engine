/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import org.w3c.dom.Document;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DynamicDelegatingCommandModel;
import com.top_logic.layout.basic.PopupCommand;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.provider.ButtonComponentButtonsControlProvider;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.FilterFormOwner;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.mig.html.HTMLConstants;
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

	private static final String FILTER_OPTIONS = "filterOptions";
	private static final String FILTER_OPTIONS_CONTENT = FILTER_OPTIONS + "Content";
	private static final String FILTER_OPTIONS_BUTTONS = FILTER_OPTIONS + "Buttons";

	private static final String INCLUDE_PARENTS_FIELD_NAME = "includeParents";
	private static final String INCLUDE_CHILDREN_FIELD_NAME = "includeChildren";
	private static final String APPLY_FIELD_NAME = "apply";
	private static final String CANCEL_FIELD_NAME = "cancel";

	private static final ResPrefix RES_PREFIX = TableFilter.RES_PREFIX;

	private static final String FILTER_CONTENT_AREA_CSS_CLASS = PopupFilterDialogBuilder.FILTER_CONTENT_AREA_CSS_CLASS;
	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";
	private static final String FILTER_LIST_CSS_CLASS = "fltList";
	private static final String FILTER_COMMAND_AREA_CSS_CLASS = PopupFilterDialogBuilder.FILTER_COMMAND_AREA_CSS_CLASS;
	private static final String FILTER_BUTTONS_CSS_CLASS = PopupFilterDialogBuilder.FILTER_BUTTONS_CSS_CLASS;

	private static final Document FILTER_OPTIONS_CONTENT_TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+ "<div class='" + FILTER_MENU_CSS_CLASS + " " + FILTER_CONTENT_AREA_CSS_CLASS + "'>"
			+ "<p:field name='" + FILTER_OPTIONS_CONTENT + "'>"
			+ "<t:list>"
			+ "<div class='" + FILTER_LIST_CSS_CLASS + "'>"
			+ "<t:items/>"
			+ "</div>"
			+ "</t:list>"
			+ "</p:field>"
			+ "</div>"
			+ "</t:group>"
	);

	private static final Document FILTER_OPTIONS_BUTTONS_TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+ "<div class='" + FILTER_COMMAND_AREA_CSS_CLASS + "'>"
			+ "<p:field name='" + FILTER_OPTIONS_BUTTONS + "'>"
			+ "<t:list>"
			+ "<div class='" + FILTER_BUTTONS_CSS_CLASS + "'>"
			+ "<t:items />"
			+ "</div>"
			+ "</t:list>"
			+ "</p:field>"
			+ "</div>"
			+ "</t:group>"
	);

	private static final ControlProvider FILTER_OPTIONS_CONTROL_PROVIDER = new DefaultFormFieldControlProvider() {
		@Override
		public Control createControl(Object model, String style) {
			BlockControl block = new BlockControl();
			block.addChild(super.createControl(model, style));
			block.addChild(new LabelControl((FormMember)model));
			block.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
			return block;
		}
	};


	/**
	 * {@link Command} for the apply button.
	 */
	private static class ApplyFilterOptionsExecutable implements Command {

		private final PopupDialogModel dialogModel;

		private final TableViewModel viewModel;
		private final FormGroup optionsGroup;

		public ApplyFilterOptionsExecutable(PopupDialogModel dialogModel, TableViewModel viewModel, FormGroup optionsGroup) {
			this.dialogModel = dialogModel;
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
			dialogModel.setClosed();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * {@link Command} for the cancel button.
	 */
	private static class CancelFilterOptionsExecutable implements Command {

		private final PopupDialogModel dialogModel;

		public CancelFilterOptionsExecutable(PopupDialogModel dialogModel) {
			this.dialogModel = dialogModel;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			dialogModel.setClosed();
			return HandlerResult.DEFAULT_RESULT;
		}
	}


	/**
	 * {@link Command} to open the filter options dialog.
	 */
	public static class OpenFilterOptionsCommand extends PopupCommand {

		private final TableData _table;

		/**
		 * Creates a new {@link OpenFilterOptionsCommand}.
		 */
		public OpenFilterOptionsCommand(TableData table) {
			_table = table;
		}

		@Override
		public HandlerResult showPopup(DisplayContext context, PopupHandler handler) {
			TableData tableData = tableData();
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordOpenTreeFilter(tableData);
			}
			TableViewModel viewModel = tableData.getViewModel();
			if (!viewModel.hasFilterOptions()) {
				HandlerResult result = new HandlerResult();
				result.addErrorMessage(I18NConstants.FILTER_OPTIONS_NO_TREE_MODEL);
				return result;
			}

			LayoutData layout = new DefaultLayoutData(
				DisplayDimension.dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL),
				100,
				DisplayDimension.dim(0, DisplayUnit.PIXEL), 100, Scrolling.NO);
			ResourceText dialogTitle = new ResourceText(I18NConstants.FILTER_OPTIONS_TITLE);
			PopupDialogModel dialogModel = new DefaultPopupDialogModel(dialogTitle, layout, 1);

			PopupDialogControl popup = handler.createPopup(dialogModel);
			createContentForPopup(popup, viewModel);
			return handler.openPopup(popup);
		}

		TableData tableData() {
			return _table;
		}

		@SuppressWarnings("synthetic-access")
		private void createContentForPopup(PopupDialogControl popup, TableViewModel viewModel) {
			FormContext form = new FormContext(FILTER_OPTIONS, RES_PREFIX);

			FormGroup content = new FormGroup(FILTER_OPTIONS_CONTENT, RES_PREFIX);
			boolean includeParentsValue = viewModel.isFiniteTree() && viewModel.isFilterIncludeParents();
			boolean includeParentsImmutable = !viewModel.isFiniteTree();
			BooleanField includeParentsField = FormFactory.newBooleanField(INCLUDE_PARENTS_FIELD_NAME,
				includeParentsValue, includeParentsImmutable);
			includeParentsField.setLabel(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS));
			if (viewModel.isFiniteTree()) {
				includeParentsField.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP));
			} else {
				includeParentsField
					.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP_INFINITE_TREE));
			}
			content.addMember(includeParentsField);
			BooleanField includeChildrenField = FormFactory.newBooleanField(INCLUDE_CHILDREN_FIELD_NAME,
				Boolean.valueOf(viewModel.isFilterIncludeChildren()), false);
			includeChildrenField.setLabel(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_CHILDREN));
			includeChildrenField.setTooltip(translate(I18NConstants.FILTER_OPTIONS_INCLUDE_CHILDREN_TOOLTIP));
			content.addMember(includeChildrenField);
			form.addMember(content);

			PopupDialogModel dialogModel = popup.getPopupDialogModel();
			final ApplyFilterOptionsExecutable applyExecutable =
				new ApplyFilterOptionsExecutable(dialogModel, viewModel, content);
			CancelFilterOptionsExecutable cancelExecutable = new CancelFilterOptionsExecutable(dialogModel);

			FormGroup buttons = new FormGroup(FILTER_OPTIONS_BUTTONS, RES_PREFIX);
			CommandField applyButton = FormFactory.newCommandField(APPLY_FIELD_NAME, applyExecutable);
			applyButton.setLabel(translate(I18NConstants.FILTER_OPTIONS_APPLY));
			buttons.addMember(applyButton);
			CommandField cancelButton = FormFactory.newCommandField(CANCEL_FIELD_NAME, cancelExecutable);
			cancelButton.setLabel(translate(I18NConstants.FILTER_OPTIONS_CANCEL));
			buttons.addMember(cancelButton);
			form.addMember(buttons);

			BlockControl container = new BlockControl();
			container.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
			container.addChild(new FormGroupControl(form, FILTER_OPTIONS_CONTROL_PROVIDER,
				DOMUtil.getFirstElementChild(FILTER_OPTIONS_CONTENT_TEMPLATE.getDocumentElement()), RES_PREFIX));
			container.addChild(new FormGroupControl(form, ButtonComponentButtonsControlProvider.INSTANCE,
				DOMUtil.getFirstElementChild(FILTER_OPTIONS_BUTTONS_TEMPLATE.getDocumentElement()), RES_PREFIX));

			popup.setContent(container);
			popup.setReturnCommand(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext displayContext) {
					return applyExecutable.executeCommand(displayContext);
				}
			});
			FilterFormOwner.register(form, tableData(), popup);
		}

		protected String translate(ResKey i18nKey) {
			return Resources.getInstance().getString(i18nKey);
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
