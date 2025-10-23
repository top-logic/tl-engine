/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.meta.form.fieldprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableListControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link RowObjectCreator} to create row for new objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CompositionRowCreator implements RowObjectCreator {

	private final EditContext _context;

	private final FormContainer _contentGroup;

	private final TLObject _owner;

	private final boolean _multiple;

	private final ResKey _labelKey;

	private Set<TLClass> _rowTypes;

	/**
	 * Creates a new {@link CompositionRowCreator}.
	 * @param owner
	 *        The owner of the newly created object.
	 * @param valueType
	 *        The type of objects being created.
	 * @param multiple
	 *        Whether multiple objects can be created.
	 * @param labelKey
	 *        The label of the edit context.
	 */
	CompositionRowCreator(EditContext context, FormContainer contentGroup, TLObject owner,
			TLClass valueType, boolean multiple, ResKey labelKey) {
		_context = context;
		_contentGroup = contentGroup;
		_owner = owner;
		_rowTypes = TLModelUtil.getConcreteReflexiveTransitiveSpecializations(valueType);
		_multiple = multiple;
		_labelKey = labelKey;
	}

	@Override
	public Object createNewRow(Control aControl) {
		TLClass valueType;
		Set<TLClass> rowTypes = _rowTypes;
		if (rowTypes.size() == 1) {
			valueType = rowTypes.iterator().next();

			return createRow(aControl, valueType);
		} else {
			SimpleFormDialog dialog =
				new SimpleFormDialog(I18NConstants.CREATE_COMPOSITION_ROW, DisplayDimension.px(350),
					DisplayDimension.px(280)) {
					private SelectField _selectField;

					@Override
					protected void fillFormContext(FormContext context) {
						_selectField = FormFactory.newSelectField(INPUT_FIELD, rowTypes);
						_selectField.setMandatory(true);
						context.addMember(_selectField);
					}

					@Override
					protected void fillButtons(List<CommandModel> buttons) {
						addCancel(buttons);
						buttons.add(MessageBox.button(ButtonType.OK, context -> {
							TLClass selection = (TLClass) _selectField.getSingleSelection();
							TLObject newRow = createRow(aControl, selection);
							((TableListControl) aControl).addNewRow(newRow);
							getDialogModel().getCloseAction().executeCommand(context);
							return HandlerResult.DEFAULT_RESULT;
						}));
					}
				};
			dialog.open(DefaultDisplayContext.getDisplayContext());
			return null;
		}
	}

	final TLObject createRow(Control aControl, TLClass valueType) {
		TableField tableField = (TableField) ((TableControl) aControl).getModel();
		TLObject newRow = CompositionFieldProvider.mkCreateContext(_context, _contentGroup, _owner, valueType, CompositionFieldProvider.lookupConstructor(_owner));
		addValue(tableField, newRow);
		return newRow;
	}


	private void addValue(TableField table, TLObject row) {
		Collection<?> currentValue = (Collection<?>) table.getValue();
		ArrayList<Object> value;
		if (currentValue == null) {
			value = new ArrayList<>();
		} else {
			value = new ArrayList<>(currentValue);
		}
		value.add(row);
		table.setValue(value);
	}

	@Override
	public ResKey allowCreateNewRow(int row, TableData data, Control control) {
		if (!_multiple && !data.getViewModel().getAllRows().isEmpty()) {
			return I18NConstants.NEW_ROW_DISABLED_NOT_MULTIPLE__ATTRIBUTE.fill(_labelKey);
		}
		return RowObjectCreator.super.allowCreateNewRow(row, data, control);
	}
}
