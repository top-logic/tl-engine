/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.meta.form.fieldprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.model.copy.CopyConstructor;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * {@link RowObjectCreator} to copy a row.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CompositionRowCopier implements RowObjectCreator {

	private final EditContext _context;

	private final FormContainer _contentGroup;

	private final TLObject _owner;

	private final boolean _multiple;

	private final ResKey _labelKey;


	/**
	 * Creates a new {@link CompositionRowCopier}.
	 * @param owner
	 *        The owner of the newly created object.
	 * @param multiple
	 *        Whether multiple objects can be created.
	 * @param labelKey
	 *        The label of the edit context.
	 */
	CompositionRowCopier(EditContext context, FormContainer contentGroup, TLObject owner, boolean multiple,
			ResKey labelKey) {
		_context = context;
		_contentGroup = contentGroup;
		_owner = owner;
		_multiple = multiple;
		_labelKey = labelKey;
	}

	@Override
	public Object createNewRow(Control aControl) {
		TableControl table = (TableControl) aControl;
		TableField tableField = (TableField) ((TableControl) aControl).getModel();

		int selectedRow = table.getSelectedRow();
		Object rowToCopy = tableField.getViewModel().getRowObject(selectedRow);

		TLFormObject elementToCopy = (TLFormObject) rowToCopy;

		return createCopy(aControl, elementToCopy);
	}

	private Object createCopy(Control aControl, TLFormObject formObj) {
		TableField tableField = (TableField) ((TableControl) aControl).getModel();

		CopyConstructor constructor = new CopyConstructor() {

			Map<TLObject, TLObject> _origByCopy = new HashMap<>();
			{
				_origByCopy.put(_owner, _owner);
			}

			@Override
			public TLObject allocate(TLObject orig, TLReference reference,
					TLObject copyContext) {

				TLObject existing = _origByCopy.get(orig);
				if (existing != null) {
					return existing;
				}

				TLObject copiedContainer = _origByCopy.get(copyContext);

				/* Do not use default copy mechanism to ensure that the EditContext is informed
				 * about the created elements. */
				TLObject result =
					CompositionFieldProvider.mkCreateContext(_context, _contentGroup, copiedContainer,
						orig.tType(), CompositionFieldProvider.lookupConstructor(_owner));

				// Keep new object, in case an inner allocation
				// has also happened.
				_origByCopy.put(orig, result);
				return result;
			}
		};
		CopyOperation copyBack = CopyOperation.initial()
			.setTransient(true)
			.setConstructor(constructor);
		copyBack.setContext(_owner, null);
		TLObject newRow = (TLObject) copyBack.copyReference(formObj);
		copyBack.finish();

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
		if (row < 0) {
			return com.top_logic.layout.table.I18NConstants.NO_ROW_SELECTED;
		}
		return RowObjectCreator.super.allowCreateNewRow(row, data, control);
	}
}
