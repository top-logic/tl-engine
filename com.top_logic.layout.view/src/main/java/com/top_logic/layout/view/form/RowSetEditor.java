/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * The cell-editing mechanics of a table edited within a form.
 *
 * <p>
 * Maintains one {@link TLObjectOverlay} per edited row and one {@link AttributeFieldModel} per
 * edited cell (created lazily, rendered through {@link FieldControlService}), wires cell
 * validation to the form's {@link FormValidationModel}, and propagates dirty state to the
 * {@link FormControl}. Implements {@link FormParticipant}, so buffered row edits validate, apply
 * and persist together with the form's save.
 * </p>
 *
 * <p>
 * The membership semantics of the row set (which objects are the rows, what adding and removing
 * means, what is written back on commit) are delegated to the configured {@link RowSetBinding}.
 * </p>
 */
public class RowSetEditor implements FormParticipant {

	private final FormControl _form;

	private final RowSetBinding _binding;

	private final List<RowEditModel> _rowModels = new ArrayList<>();

	/** The persistent row objects at edit-session start, for remove detection on commit. */
	private List<TLObject> _originalRows;

	private RowSetFieldModel _fieldModel;

	/** Validation listeners registered per cell, for cleanup on session end. */
	private final List<ConstraintValidationListener> _cellValidationListeners = new ArrayList<>();

	/** The validation model on which cell listeners were registered, for correct cleanup. */
	private FormValidationModel _registeredValidationModel;

	/**
	 * Creates a {@link RowSetEditor}.
	 *
	 * @param form
	 *        The form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics.
	 */
	public RowSetEditor(FormControl form, RowSetBinding binding) {
		_form = form;
		_binding = binding;
	}

	/**
	 * The row-set semantics of this editor.
	 */
	public RowSetBinding getBinding() {
		return _binding;
	}

	/**
	 * Whether an edit session is running.
	 */
	public boolean isEditing() {
		return _fieldModel != null;
	}

	/**
	 * The current row objects of the running edit session (row overlays and transient new
	 * objects), or an empty list if no session is running.
	 */
	public List<TLObject> getCurrentRows() {
		return _fieldModel == null ? new ArrayList<>() : _fieldModel.getCurrentList();
	}

	/**
	 * Starts an edit session: wraps each persistent row in a {@link TLObjectOverlay}, publishes
	 * the overlay list through the binding, registers the overlays with the form's
	 * {@link FormValidationModel}, and registers this editor as {@link FormParticipant}.
	 *
	 * @return The row objects to display (the overlay list).
	 */
	public List<TLObject> beginEditSession() {
		// Clean up previous edit state (e.g. after executeApply resets the session).
		endEditSession();

		TLObject currentObject = _form.getCurrentObject();
		TLObject baseObject =
			currentObject instanceof TLObjectOverlay overlay ? overlay.getBase() : currentObject;

		List<TLObject> persistentRows = _binding.readRows(baseObject);
		_originalRows = new ArrayList<>(persistentRows);

		List<TLObject> overlayList = new ArrayList<>();
		for (TLObject row : persistentRows) {
			TLObjectOverlay rowOverlay = new TLObjectOverlay(row);
			_rowModels.add(RowEditModel.forExisting(rowOverlay));
			overlayList.add(rowOverlay);
		}

		_binding.updateMembership(_form, overlayList);

		_fieldModel = new RowSetFieldModel(overlayList);
		for (RowEditModel row : _rowModels) {
			TLObjectOverlay rowOverlay = row.getRowOverlay();
			if (rowOverlay != null) {
				_fieldModel.addRowOverlay(rowOverlay);
			}
		}

		// Register row overlays with the validation model so field-level constraints (mandatory,
		// range) are evaluated for row objects.
		_registeredValidationModel = _form.getValidationModel();
		if (_registeredValidationModel != null) {
			for (RowEditModel rowModel : _rowModels) {
				TLObjectOverlay rowOverlay = rowModel.getRowOverlay();
				if (rowOverlay != null) {
					_registeredValidationModel.addOverlay(rowOverlay, rowOverlay.getBase());
				}
			}
		}

		_form.registerParticipant(this);

		return overlayList;
	}

	/**
	 * Ends the edit session, unregistering validation listeners, row overlays and this
	 * participant.
	 */
	public void endEditSession() {
		// Remove cell validation listeners and row overlays from the model they were registered on
		// (which may differ from the current model after the form set up a fresh edit session).
		if (_registeredValidationModel != null) {
			for (ConstraintValidationListener listener : _cellValidationListeners) {
				_registeredValidationModel.removeConstraintValidationListener(listener);
			}
			for (RowEditModel row : _rowModels) {
				TLObjectOverlay overlay = row.getRowOverlay();
				if (overlay != null) {
					_registeredValidationModel.removeOverlay(overlay);
				}
			}
			_registeredValidationModel = null;
		}
		_cellValidationListeners.clear();

		if (_fieldModel != null) {
			_form.unregisterParticipant(this);
			_fieldModel = null;
		}
		_rowModels.clear();
		_originalRows = null;
	}

	/**
	 * Adds a new transient row of the given type to the edit session.
	 *
	 * @param type
	 *        The concrete type of the new row; one of the binding's
	 *        {@link RowSetBinding#getCreateTypes()}.
	 * @return The created transient row object, or {@code null} if no session is running.
	 */
	public TLObject addRow(TLClass type) {
		if (_fieldModel == null || type == null) {
			return null;
		}

		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(type);

		// Register with validation model so constraints are evaluated.
		if (_registeredValidationModel != null) {
			_registeredValidationModel.addOverlay(transientObject, null);
		}

		_rowModels.add(RowEditModel.forNew(transientObject));

		List<TLObject> currentList = _fieldModel.getCurrentList();
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		_binding.updateMembership(_form, currentList);
		notifyMembershipChanged();
		_form.updateDirtyState();

		return transientObject;
	}

	/**
	 * Removes a row from the edit session. The remove semantics of the binding are applied on
	 * commit, not here.
	 *
	 * @param rowObject
	 *        The row object (overlay or transient) to remove.
	 * @return Whether the row was part of the session and has been removed.
	 */
	public boolean removeRow(TLObject rowObject) {
		if (_fieldModel == null) {
			return false;
		}

		List<TLObject> currentList = _fieldModel.getCurrentList();
		if (!currentList.remove(rowObject)) {
			return false;
		}
		_fieldModel.setValue(currentList);

		RowEditModel removedRow = findRowModel(rowObject);
		if (removedRow != null) {
			_rowModels.remove(removedRow);
			TLObjectOverlay removedOverlay = removedRow.getRowOverlay();
			if (removedOverlay != null) {
				_fieldModel.removeRowOverlay(removedOverlay);
			}
			if (_registeredValidationModel != null) {
				if (removedOverlay != null) {
					_registeredValidationModel.removeOverlay(removedOverlay);
				} else if (rowObject.tTransient()) {
					_registeredValidationModel.removeOverlay(rowObject);
				}
			}
		}

		_binding.updateMembership(_form, currentList);
		notifyMembershipChanged();
		_form.updateDirtyState();

		return true;
	}

	/**
	 * The editing state of the given row, or {@code null} if the row is not part of the session.
	 */
	public RowEditModel findRowModel(TLObject rowObject) {
		for (RowEditModel row : _rowModels) {
			if (row.getRowObject() == rowObject) {
				return row;
			}
		}
		return null;
	}

	/**
	 * Builds the editable control for a cell: the attribute's field control bound to the cell's
	 * {@link AttributeFieldModel}, which is created once per row and column and wired for
	 * validation and dirty tracking.
	 *
	 * @param context
	 *        The React context for control creation.
	 * @param row
	 *        The row object (overlay or transient).
	 * @param columnName
	 *        The model attribute name of the column.
	 * @return The editable cell control, or {@code null} if the cell cannot be edited (the
	 *         attribute does not resolve or the row is not part of the session).
	 */
	public ReactControl createEditCellControl(ReactContext context, TLObject row, String columnName) {
		TLStructuredType type = row.tType();
		TLStructuredTypePart part = type.getPart(columnName);
		if (part == null) {
			return null;
		}
		RowEditModel rowModel = findRowModel(row);
		if (rowModel == null) {
			return null;
		}
		AttributeFieldModel cellFieldModel = rowModel.getColumnModel(columnName);
		if (cellFieldModel == null) {
			cellFieldModel = new AttributeFieldModel(row, part);
			cellFieldModel.setEditable(true);
			rowModel.putColumnModel(columnName, cellFieldModel);

			// Wire validation from FormValidationModel to this cell.
			wireCellValidation(cellFieldModel, row, part);

			// Add dirty propagation and live validation trigger.
			addCellListener(cellFieldModel, row);
		}
		return FieldControlService.getInstance().createFieldControl(context, part, cellFieldModel);
	}

	// -- FormParticipant --

	@Override
	public boolean validate() {
		if (_fieldModel == null) {
			return true;
		}

		// Reveal all fields so that model-level errors (mandatory, range constraints from
		// FormValidationModel) become visible via hasError().
		revealAll();

		boolean valid = !_fieldModel.hasError();
		for (RowEditModel row : _rowModels) {
			for (AttributeFieldModel colModel : row.getColumnModels().values()) {
				if (colModel.hasError()) {
					valid = false;
				}
			}
		}
		return valid;
	}

	@Override
	public void applyState() {
		if (_fieldModel == null) {
			return;
		}
		for (RowEditModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null && overlay.isDirty()) {
				overlay.apply();
			}
		}
	}

	@Override
	public void persist(Transaction tx) {
		if (_fieldModel == null) {
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();

		// Persist new transient objects and unwrap overlays to their base objects.
		List<TLObject> persistedRows = new ArrayList<>();
		for (TLObject obj : currentList) {
			if (obj instanceof TLObjectOverlay overlay) {
				persistedRows.add(overlay.getBase());
			} else if (obj.tTransient()) {
				persistedRows.add(persistTransientObject(obj));
			} else {
				persistedRows.add(obj);
			}
		}

		_binding.commit(tx, _form, persistedRows, _originalRows);
	}

	@Override
	public void cancel() {
		_rowModels.clear();
		_fieldModel = null;
	}

	@Override
	public void revealAll() {
		if (_fieldModel != null) {
			_fieldModel.setRevealed(true);
		}
		for (RowEditModel row : _rowModels) {
			for (AttributeFieldModel colModel : row.getColumnModels().values()) {
				colModel.setRevealed(true);
			}
		}
	}

	@Override
	public boolean isDirty() {
		return _fieldModel != null && _fieldModel.isDirty();
	}

	// -- Internals --

	/**
	 * Notifies the {@link FormValidationModel} that the bound attribute's value has changed, so
	 * that constraints on the attribute (e.g. min count) are re-evaluated.
	 */
	private void notifyMembershipChanged() {
		TLStructuredTypePart boundPart = _binding.getBoundPart();
		TLObjectOverlay overlay = _form.getOverlay();
		if (_registeredValidationModel != null && overlay != null && boundPart != null) {
			_registeredValidationModel.onValueChanged(overlay, boundPart);
		}
	}

	/**
	 * Wires a {@link ConstraintValidationListener} that propagates validation results from the
	 * {@link FormValidationModel} to the cell's {@link AttributeFieldModel}.
	 */
	private void wireCellValidation(AttributeFieldModel model, TLObject rowObject, TLStructuredTypePart part) {
		FormValidationModel validationModel = _registeredValidationModel;
		if (validationModel == null) {
			return;
		}

		// Apply initial validation state.
		model.applyValidationResult(validationModel.getValidation(rowObject, part));

		// Listen for future changes on this specific (object, attribute).
		ConstraintValidationListener listener = (overlay, attr, result) -> {
			if (overlay == rowObject && attr.equals(part)) {
				model.applyValidationResult(result);
			}
		};
		validationModel.addConstraintValidationListener(listener);
		_cellValidationListeners.add(listener);
	}

	/**
	 * Adds a listener to a cell field model that propagates dirty state, reveals the field after
	 * user interaction, and triggers live constraint re-evaluation via the
	 * {@link FormValidationModel}.
	 */
	private void addCellListener(AttributeFieldModel cellFieldModel, TLObject rowObject) {
		cellFieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_form.updateDirtyState();

				// Reveal validation errors after user interaction.
				cellFieldModel.setRevealed(true);

				// Trigger live constraint re-evaluation.
				FormValidationModel validationModel = _registeredValidationModel;
				if (validationModel != null) {
					validationModel.onValueChanged(rowObject, cellFieldModel.getPart());
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// No-op.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// No-op.
			}
		});
	}

	/**
	 * Persists a transient row by creating a new persistent KB object of the same type and copying
	 * all attribute values.
	 */
	private static TLObject persistTransientObject(TLObject transientObj) {
		TLClass type = (TLClass) transientObj.tType();
		TLObject persisted = DynamicModelService.getFactoryFor(type.getModule().getName())
			.createObject(type, null);

		// Copy attribute values.
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part instanceof TLReference reference) {
				if (reference.isBackwards()) {
					continue;
				}
			}
			try {
				Object value = transientObj.tValue(part);
				if (value != null) {
					persisted.tUpdate(part, value);
				}
			} catch (RuntimeException ex) {
				// Skip attributes that cannot be copied (e.g. derived attributes).
			}
		}
		return persisted;
	}

}
