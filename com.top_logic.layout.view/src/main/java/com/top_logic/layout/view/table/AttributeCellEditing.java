/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.AttributeChanges;
import com.top_logic.layout.view.form.AttributeModeTracker;
import com.top_logic.layout.view.form.AttributeModeTracker.AttributeMode;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionCellModel;
import com.top_logic.layout.view.form.CompositionEditing;
import com.top_logic.layout.view.form.DynamicVisibility;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.tool.boundsec.BoundCommandGroup;

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
 * A cell is edited in the mode a form field of the attribute has on the row, see
 * {@link AttributeModeTracker}: only where the model lets the attribute be edited on the row and
 * the current user may write it there. The mode a {@link ModeSelector dynamic visibility} of the
 * attribute computes from the other values of the row is applied to the field of the cell and
 * computed again when one of them changes in the row: the cell is edited, displayed read-only or
 * as an inactive input, marked mandatory, or shows nothing at all.
 * </p>
 *
 * <p>
 * A {@link AttributeOptions#isComposition(TLStructuredTypePart) composition} is edited in a dialog:
 * the cell shows the labels of the parts and a button opening the table of the parts, see
 * {@link CompositionEditing}. Its parts are displayed in that dialog wherever the user may read
 * the composition, and changed only where it can be edited.
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
	 * Whether the row holds the attribute, the current user may read it on the row, and the
	 * attribute can be changed there: its static visibility lets it be edited on the row (see
	 * {@link AttributeModeTracker#isStaticallyEditable(TLObject, TLStructuredTypePart)}) and the user
	 * may write it on the row.
	 *
	 * <p>
	 * An attribute with a {@link ModeSelector dynamic visibility} is edited wherever the user may
	 * read and write it on the row: whether the cell offers its input is decided by the mode the
	 * selector computes, which may change while the row is edited, see
	 * {@link #followRow(Object, BoundFieldModel, AttributeChanges)}.
	 * </p>
	 *
	 * <p>
	 * A composition the user may read always offers its dialog, which displays the parts read-only
	 * where the composition cannot be changed.
	 * </p>
	 *
	 * <p>
	 * A cell that cannot be changed is displayed read-only - a derived attribute, an attribute whose
	 * visibility is read-only, and an attribute the user may not write, whether the refusal depends
	 * on the row or not, see
	 * {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
	 * </p>
	 */
	@Override
	public boolean canEdit(Object row) {
		TLStructuredTypePart part = part(row);
		if (part == null) {
			return false;
		}
		TLObject object = (TLObject) row;
		if (!AttributeModeTracker.isReadable(object, part)) {
			return false;
		}
		// The parts of a composition that cannot be changed are displayed in the dialog.
		return AttributeOptions.isComposition(part) || canChange(object, part);
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form, EditLevel level) {
		TLObject object = (TLObject) row;
		TLStructuredTypePart part = part(row);
		if (AttributeOptions.isComposition(part)) {
			return new CompositionCellModel(object, part, form, level, canChange(object, part));
		}
		return FieldControlService.getInstance().createModel(object, part, form);
	}

	/**
	 * Applies the mode the {@link ModeSelector dynamic visibility} of the attribute computes for
	 * the row to the field of the cell, and applies it again when an attribute it depends on
	 * changes in the row, until the field is disposed.
	 */
	@Override
	public void followRow(Object row, BoundFieldModel model, AttributeChanges rowChanges) {
		TLStructuredTypePart part = part(row);
		if (part == null || !hasDynamicMode(part)) {
			return;
		}
		new CellMode((TLObject) row, model, rowChanges).attach(part);
	}

	/**
	 * Creates the control of the cell, which shows nothing while the field is not
	 * {@link BoundFieldModel#isVisible() shown}.
	 */
	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		TLStructuredTypePart part = part(row);
		ReactControl control;
		if (model instanceof CompositionCellModel composition) {
			control = CompositionEditing.createControl(context, composition, MetaLabelProvider.INSTANCE.getLabel(part));
		} else {
			control = FieldControlService.getInstance().createCellControl(context, part, model);
		}
		Runnable followVisibility = () -> control.setHidden(!model.isVisible());
		followVisibility.run();
		model.addVisibilityListener(followVisibility);
		control.addCleanupAction(() -> model.removeVisibilityListener(followVisibility));
		return control;
	}

	/**
	 * Whether the attribute can be changed on the given object: the model lets it be edited there,
	 * and the current user may write it.
	 *
	 * <p>
	 * For an attribute with a {@link ModeSelector dynamic visibility}, the model decides through the
	 * mode applied to the field, see {@link #followRow(Object, BoundFieldModel, AttributeChanges)}.
	 * </p>
	 */
	private static boolean canChange(TLObject object, TLStructuredTypePart part) {
		return (hasDynamicMode(part) || AttributeModeTracker.isStaticallyEditable(object, part))
			&& AttributeModeTracker.isWritable(object, part);
	}

	/**
	 * The mode of the attribute of a row applied to the field of its cell.
	 */
	private static final class CellMode {

		private final TLObject _row;

		private final BoundFieldModel _model;

		private final AttributeModeTracker _tracker;

		CellMode(TLObject row, BoundFieldModel model, AttributeChanges rowChanges) {
			_row = row;
			_model = model;
			_tracker = new AttributeModeTracker(rowChanges, this::apply);
		}

		/**
		 * Applies the mode of the given attribute and follows it until the field is disposed.
		 */
		void attach(TLStructuredTypePart part) {
			_tracker.attach(part);
			apply();
			_model.addDisposeAction(_tracker::detach);
		}

		/**
		 * Computes the mode of the attribute on the row and applies it to the field: whether it is
		 * edited, presented as an inactive input, marked mandatory and shown at all.
		 */
		private void apply() {
			AttributeMode mode = _tracker.computeMode(_row, true, false);
			mode.applyTo(_model);
			_model.setMandatory(mode.mandatory());
			_model.setVisible(mode.visible());
		}
	}

	/**
	 * Whether the mode of the attribute is computed by a {@link ModeSelector}, see
	 * {@link DynamicVisibility}.
	 */
	private static boolean hasDynamicMode(TLStructuredTypePart part) {
		return DynamicVisibility.modeSelector(part) != null;
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
