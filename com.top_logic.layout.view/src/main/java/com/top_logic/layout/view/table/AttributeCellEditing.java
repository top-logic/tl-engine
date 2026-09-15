/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link CellEditing} writing the edited value to a model attribute of the row.
 *
 * <p>
 * The attribute is resolved at the row, not at the row type the table declares, so a row of a
 * specialized type is edited through the attribute that type holds. A row whose type does not hold
 * the attribute at all shows its cell but does not edit it.
 * </p>
 *
 * <p>
 * The field and the control editing it are the ones a form builds for that attribute, so a cell is
 * edited exactly as the same attribute is in a form.
 * </p>
 */
public class AttributeCellEditing implements CellEditing {

	private final String _attribute;

	/**
	 * Creates an {@link AttributeCellEditing}.
	 *
	 * @param attribute
	 *        The name of the attribute holding the cell values.
	 */
	public AttributeCellEditing(String attribute) {
		_attribute = attribute;
	}

	@Override
	public boolean canEdit(Object row) {
		return part(row) != null;
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form) {
		return FieldControlService.getInstance().createModel((TLObject) row, part(row), form);
	}

	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		return FieldControlService.getInstance().createFieldControl(context, part(row), model);
	}

	/**
	 * The attribute holding the given row's cell value, or {@code null} if the row does not hold
	 * one of that name.
	 */
	private TLStructuredTypePart part(Object row) {
		if (!(row instanceof TLObject object)) {
			return null;
		}
		TLStructuredType type = object.tType();
		return type == null ? null : type.getPart(_attribute);
	}

}
