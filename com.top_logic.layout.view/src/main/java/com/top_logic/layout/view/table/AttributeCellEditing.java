/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionCellModel;
import com.top_logic.layout.view.form.CompositionEditing;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

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
 *
 * <p>
 * Only a cell the current user may write on its row is edited, as a form field is.
 * </p>
 *
 * <p>
 * A {@link AttributeOptions#isComposition(TLStructuredTypePart) composition} is edited in a dialog:
 * the cell shows the labels of the parts and a button opening the table of the parts, see
 * {@link CompositionEditing}. Its parts are displayed in that dialog wherever the user may read
 * the composition, and changed only where the user may write it.
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

	/**
	 * Whether the row holds the attribute and the current user may read and write it on the row - or
	 * only read it, for a composition.
	 *
	 * <p>
	 * A cell the user may not write is displayed read-only, whether the refusal depends on the row
	 * or not, see {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
	 * </p>
	 */
	@Override
	public boolean canEdit(Object row) {
		TLStructuredTypePart part = part(row);
		if (part == null) {
			return false;
		}
		TLObject object = (TLObject) row;
		if (!ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.READ, object, part).isExecutable()) {
			return false;
		}
		// The parts of a composition the user may only read are displayed in the dialog.
		return AttributeOptions.isComposition(part) || canWrite(object, part);
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form) {
		TLObject object = (TLObject) row;
		TLStructuredTypePart part = part(row);
		if (AttributeOptions.isComposition(part)) {
			return new CompositionCellModel(object, part, form, canWrite(object, part));
		}
		return FieldControlService.getInstance().createModel(object, part, form);
	}

	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		TLStructuredTypePart part = part(row);
		if (model instanceof CompositionCellModel composition) {
			return CompositionEditing.createControl(context, composition, MetaLabelProvider.INSTANCE.getLabel(part));
		}
		return FieldControlService.getInstance().createCellControl(context, part, model);
	}

	private static boolean canWrite(TLObject object, TLStructuredTypePart part) {
		return ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.WRITE, object, part).isExecutable();
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
