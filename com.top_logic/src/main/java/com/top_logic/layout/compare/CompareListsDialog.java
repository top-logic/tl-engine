/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.messagebox.SimpleTableDialog;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * Dialog comparing two lists.
 * 
 * @see CompareTreesDialog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareListsDialog extends SimpleTableDialog {

	static final Property<TableData> PREPARED_TABLE_DATA = TypedAnnotatable.property(TableData.class, "prepared table data");

	private List<Object> _origList;

	private List<Object> _compareList;

	private FormContext _compareForm;

	private TableConfigurationProvider _tableConfig = TableConfigurationFactory.emptyProvider();

	private final CompareAlgorithm _algorithm;

	/**
	 * Creates a new {@link CompareListsDialog}.
	 * @param width
	 *        See
	 *        {@link SimpleTableDialog#SimpleTableDialog(ResPrefix, DisplayDimension, DisplayDimension)}.
	 * @param height
	 *        See
	 *        {@link SimpleTableDialog#SimpleTableDialog(ResPrefix, DisplayDimension, DisplayDimension)}.
	 * @param origList
	 *        The original list that serves as base list.
	 * @param compareList
	 *        The list to compare with.
	 */
	public CompareListsDialog(DisplayDimension width, DisplayDimension height, List<Object> origList,
			List<Object> compareList, CompareAlgorithm algorithm) {
		super(I18NConstants.COMPARE_DIALOG, width, height);
		_origList = origList;
		_compareList = compareList;
		_algorithm = algorithm;
	}

	/**
	 * Creates a new CompareListsDialog, with height of 80%.
	 * 
	 * @param origList
	 *        The original list that serves as base list.
	 * @param compareList
	 *        The list to compare with.
	 */
	public CompareListsDialog(List<Object> origList, List<Object> compareList, CompareAlgorithm algorithm) {
		this(CompareTreesDialog.EIGHTY_PERCENT, CompareTreesDialog.EIGHTY_PERCENT, origList,
			compareList, algorithm);
	}

	/**
	 * Sets the {@link TableConfigurationProvider} defining the displayed table.
	 */
	public void setTableConfig(TableConfigurationProvider tableConfig) {
		_tableConfig = tableConfig;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.CLOSE, getDiscardClosure()));
	}

	@Override
	protected FormContext createFormContext() {
		FormContext displayForm = super.createFormContext();
		_compareForm = super.createFormContext();
		return displayForm;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		String fieldName = SimpleFormDialog.INPUT_FIELD;
		SelectField displayedSelectField = addNewSelectField(context, fieldName, _origList);
		addNewSelectField(_compareForm, fieldName, _compareList);
		DecorateService.annotate(context, new CompareService<>(_compareForm, _algorithm));

		/* Need to cache compare table data, because it is recomputed on each call. Therefore all
		 * changes on the GUI, e.g. filtering, sorting, disappear on F5. */
		displayedSelectField.set(PREPARED_TABLE_DATA, DecorateService.prepareTableData(displayedSelectField));
	}

	private SelectField addNewSelectField(FormContext context, String fieldName, List<Object> options) {
		SelectField newSelectField = newSelectField(fieldName, options);
		context.addMember(newSelectField);
		return newSelectField;
	}

	private SelectField newSelectField(String fieldName, List<Object> options) {
		final SelectField selectField =
			FormFactory.newSelectField(fieldName, options, FormFactory.MULTIPLE, !FormFactory.IMMUTABLE);
		selectField.setAsSelection(options);
		selectField.setDisabled(true);
		selectField.setTableConfigurationProvider(TableConfigurationFactory.combine(_tableConfig, setResourcesOfSelectField(selectField)));
		return selectField;
	}

	private NoDefaultColumnAdaption setResourcesOfSelectField(final SelectField selectField) {
		return new NoDefaultColumnAdaption() {
			
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				selectField.setResources(table.getResPrefix());
			}

		};
	}

	@Override
	protected ControlProvider getControlProvider() {
		return new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				if (StringServices.isEmpty(style)
					&& SimpleFormDialog.INPUT_FIELD.equals(((FormMember) model).getName())) {
					return createTableControl((SelectField) model);
				}
				return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
			}

			private Control createTableControl(SelectField model) {
				TableData tableData = model.get(PREPARED_TABLE_DATA);
				return new TableControl(tableData, tableData.getViewModel().getTableConfiguration().getTableRenderer());
			}

		};
	}

}

