/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.command.PersistTransientAction;
import com.top_logic.layout.view.table.CellEditing;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The editing of a set of row objects, taking part in the edit session of a form.
 *
 * <p>
 * When {@link #start() started}, the session wraps every existing row object in a
 * {@link TLObjectOverlay} and buffers the row list in a {@link CompositionFieldModel}. Rows are
 * added as transient objects and removed from the list, and the cells of the rows are edited
 * through field models created once per row and column. The session registers itself as
 * {@link FormParticipant} of the {@link RowSetOwner#form() form}: the form validates it, and on
 * save, the session persists its new rows, applies the changes of its row overlays, and applies
 * the remove semantics of the {@link RowSetBinding} to the rows taken out of the row set.
 * </p>
 *
 * <p>
 * The rows belong to a {@link RowSetOwner}: the object of the form, or a row of a row set the form
 * edits. The membership semantics of the row set (which objects are the rows, what adding and
 * removing means, what is written back on commit) come from the {@link RowSetBinding}.
 * </p>
 *
 * <p>
 * The session lives independently of what displays it: a display observes it through a
 * {@link Listener} and may be created and disposed while the session runs. The session ends with
 * {@link #end()}.
 * </p>
 */
public class RowSetEditSession implements FormParticipant {

	/**
	 * Observer of a {@link RowSetEditSession}, typically the display of its rows.
	 */
	public interface Listener {

		/**
		 * Called after a row was added to or removed from the row set.
		 *
		 * @param session
		 *        The session whose row set changed, see {@link RowSetEditSession#currentRows()}.
		 */
		void onRowsChanged(RowSetEditSession session);

		/**
		 * Called after the validation state of the row set as a whole changed, see
		 * {@link RowSetEditSession#fieldModel()}, and when the session ends.
		 *
		 * @param session
		 *        The session whose validation state changed.
		 */
		void onRowSetValidationChanged(RowSetEditSession session);

		/**
		 * Called after the session {@link RowSetEditSession#end() ended}.
		 *
		 * @param session
		 *        The session that ended.
		 */
		default void onEnded(RowSetEditSession session) {
			// Nothing to do by default.
		}

	}

	private final RowSetOwner _owner;

	private final RowSetBinding _binding;

	private final List<Listener> _listeners = new CopyOnWriteArrayList<>();

	private boolean _editable = true;

	/** Whether the session was {@link #start() started} and has not {@link #end() ended} yet. */
	private boolean _started;

	private CompositionFieldModel _fieldModel;

	private final List<CompositionRowModel> _rowModels = new ArrayList<>();

	/** The persistent objects at session start, for orphan detection on save. */
	private List<TLObject> _originalPersistentObjects;

	/** Validation listeners registered by this session, for cleanup on end. */
	private final List<ConstraintValidationListener> _validationListeners = new ArrayList<>();

	/** The validation model on which the listeners were registered, for correct cleanup. */
	private FormValidationModel _validationModel;

	/**
	 * Creates a {@link RowSetEditSession}.
	 *
	 * @param owner
	 *        The object holding the rows and the form the editing takes part in.
	 * @param binding
	 *        The row-set semantics, {@link RowSetBinding#resolve(TLObject) resolved} against the
	 *        owner.
	 */
	public RowSetEditSession(RowSetOwner owner, RowSetBinding binding) {
		_owner = owner;
		_binding = binding;
	}

	/**
	 * The object holding the rows and the form the editing takes part in.
	 */
	public RowSetOwner owner() {
		return _owner;
	}

	/**
	 * The row-set semantics of this session.
	 */
	public RowSetBinding binding() {
		return _binding;
	}

	/**
	 * Adds an observer of this session.
	 */
	public void addListener(Listener listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes an observer added by {@link #addListener(Listener)}.
	 */
	public void removeListener(Listener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Whether the rows may be changed: rows added and removed, and their cells edited.
	 *
	 * <p>
	 * A session that may not change its rows still collects them for display.
	 * </p>
	 */
	public boolean isEditable() {
		return _editable;
	}

	/**
	 * Sets whether the rows may be {@link #isEditable() changed}.
	 */
	public void setEditable(boolean editable) {
		_editable = editable;
	}

	/**
	 * Whether the session is {@link #start() started} and neither {@link #end() ended} nor
	 * {@link #cancel() cancelled}.
	 */
	public boolean isRunning() {
		return _fieldModel != null;
	}

	/**
	 * The field model holding the edited row list, or {@code null} while the session is not
	 * {@link #isRunning() running}.
	 */
	public CompositionFieldModel fieldModel() {
		return _fieldModel;
	}

	/**
	 * The current rows: overlays of the existing rows and the transient new rows, empty while the
	 * session is not {@link #isRunning() running}.
	 */
	public List<TLObject> currentRows() {
		return _fieldModel == null ? List.of() : _fieldModel.getCurrentList();
	}

	/**
	 * The per-row models tracking overlays and cell field models.
	 */
	public List<CompositionRowModel> rowModels() {
		return _rowModels;
	}

	/**
	 * The validation model in effect for this session, or {@code null}.
	 */
	public FormValidationModel validationModel() {
		return _validationModel;
	}

	/**
	 * Starts editing the rows the owner holds and takes part in the edit session of the form.
	 *
	 * <p>
	 * Requires the {@link RowSetOwner#object() editing buffer} of the owner, i.e. a form in edit
	 * mode.
	 * </p>
	 */
	public void start() {
		end();

		// Read the persistent row objects.
		List<TLObject> persistentObjects = _binding.readRows(_owner.base());
		_originalPersistentObjects = new ArrayList<>(persistentObjects);

		// Create overlays for each existing row object.
		List<TLObject> overlayList = new ArrayList<>();
		for (TLObject row : persistentObjects) {
			TLObjectOverlay rowOverlay = new TLObjectOverlay(row);
			_rowModels.add(CompositionRowModel.forExisting(rowOverlay));
			overlayList.add(rowOverlay);
		}

		// Publish the overlay list to the owner through the binding.
		_binding.updateMembership(_owner, overlayList);

		// Create the row-list field model.
		_fieldModel = new CompositionFieldModel(overlayList);
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay rowOverlay = row.getRowOverlay();
			if (rowOverlay != null) {
				_fieldModel.addRowOverlay(rowOverlay);
			}
		}

		// Register row overlays with the validation model so field-level
		// constraints (mandatory, range) are evaluated for row objects.
		FormControl form = _owner.form();
		_validationModel = form.getValidationModel();
		if (_validationModel != null) {
			for (CompositionRowModel rowModel : _rowModels) {
				TLObjectOverlay rowOverlay = rowModel.getRowOverlay();
				if (rowOverlay != null) {
					_validationModel.addOverlay(rowOverlay, rowOverlay.getBase());
				}
			}
		}

		form.registerParticipant(this);
		_started = true;

		// Reflect constraints of the bound attribute itself in the field model and display.
		wireRowSetValidation();
	}

	/**
	 * Reflects constraints of the bound attribute itself (e.g. a required minimum number of
	 * entries) in the row-set {@link #fieldModel()} and notifies the listeners to display them.
	 *
	 * <p>
	 * Without this wiring, such a constraint would only block saving without any visible location
	 * of the problem: the bound attribute is displayed as a table and has no regular field chrome
	 * showing validation errors.
	 * </p>
	 */
	private void wireRowSetValidation() {
		FormValidationModel validationModel = _validationModel;
		TLObject owner = _owner.object();
		TLStructuredTypePart boundPart = _binding.getBoundPart();
		if (validationModel == null || owner == null || _fieldModel == null || boundPart == null) {
			return;
		}

		_fieldModel.applyValidationResult(validationModel.getValidation(owner, boundPart));

		ConstraintValidationListener listener = (changedOverlay, attr, result) -> {
			if (changedOverlay == owner && attr.equals(boundPart)) {
				_fieldModel.applyValidationResult(result);
			}
		};
		validationModel.addConstraintValidationListener(listener);
		_validationListeners.add(listener);

		FormControl form = _owner.form();
		_fieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				// Value changes are reported through Listener.onRowsChanged().
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not displayed.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				fireValidationChanged();
				form.fireValidityChanged();
			}
		});

		fireValidationChanged();
	}

	/**
	 * Ends the session: discards all its state - validation listeners, row overlays, the field
	 * model - and withdraws its participation in the form.
	 *
	 * <p>
	 * The {@link Listener}s are notified when a started session ends, also after the form
	 * {@link #cancel() cancelled} it.
	 * </p>
	 */
	public void end() {
		// Remove validation listeners and row overlays from the model they were registered on
		// (which may differ from the current model after the form set up a fresh edit session).
		if (_validationModel != null) {
			for (ConstraintValidationListener listener : _validationListeners) {
				_validationModel.removeConstraintValidationListener(listener);
			}
			for (CompositionRowModel row : _rowModels) {
				TLObjectOverlay overlay = row.getRowOverlay();
				if (overlay != null) {
					_validationModel.removeOverlay(overlay);
				}
			}
			_validationModel = null;
		}
		_validationListeners.clear();

		boolean wasStarted = _started;
		_started = false;
		if (_fieldModel != null) {
			_owner.form().unregisterParticipant(this);
			_fieldModel = null;
		}
		for (CompositionRowModel row : _rowModels) {
			disposeCells(row);
		}
		_rowModels.clear();
		_originalPersistentObjects = null;

		// Clear any displayed row-set error (the field model is now gone).
		fireValidationChanged();

		if (wasStarted) {
			for (Listener listener : _listeners) {
				listener.onEnded(this);
			}
		}
	}

	/**
	 * Releases the field models of the cells of the given row; cells displayed again get new ones.
	 */
	private static void disposeCells(CompositionRowModel row) {
		for (BoundFieldModel cell : row.getColumnModels().values()) {
			cell.dispose();
		}
		row.getColumnModels().clear();
	}

	// -- FormParticipant --

	@Override
	public boolean validate() {
		if (_fieldModel == null) {
			return true;
		}

		boolean valid = !_fieldModel.hasError();
		for (CompositionRowModel row : _rowModels) {
			for (BoundFieldModel colModel : row.getColumnModels().values()) {
				if (colModel.hasError()) {
					valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * Checks the write right of every changed row and the right to create every added row.
	 *
	 * <p>
	 * A row added to the rows of a persistent owner is created when the form is saved: the user
	 * needs the right to create an object of its type in the context of the owner and, for rows of
	 * an attribute of the owner, the right to write that attribute. Rows of a transient owner
	 * become persistent together with it, where that creation is checked.
	 * </p>
	 */
	@Override
	public void checkApplyState() {
		if (_fieldModel == null) {
			return;
		}
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null && overlay.isDirty()) {
				overlay.checkApply();
			}
		}
		if (!_owner.isTransient()) {
			checkCreateRights();
		}
	}

	private void checkCreateRights() {
		TLObject parent = _owner.base();
		TLStructuredTypePart part = parent != null ? _binding.getBoundPart() : null;
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		for (TLObject row : _fieldModel.getCurrentList()) {
			if (row instanceof TLObjectOverlay || !row.tTransient()) {
				continue;
			}
			TLClass type = (TLClass) row.tType();
			boolean allowed = part == null
				? rights.isAllowedCreate(user, type, (TLObject) null)
				: rights.isAllowedCreate(user, parent, part, type);
			if (!allowed) {
				throw new TopLogicException(
					com.top_logic.element.model.copy.I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE.fill(type));
			}
		}
	}

	@Override
	public void applyState() {
		if (_fieldModel == null) {
			return;
		}
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null && overlay.isDirty()) {
				overlay.apply();
			}
		}

		if (_owner.isTransient()) {
			// A transient owner keeps transient row objects: write the current row list into the
			// editing buffer of the owner so applying it transfers the rows to the owner. The whole
			// transient tree becomes persistent in one piece when the owner is made persistent
			// (e.g. by a create dialog's or a new-entry form's submit chain).
			List<TLObject> bases = new ArrayList<>();
			for (TLObject obj : _fieldModel.getCurrentList()) {
				bases.add(obj instanceof TLObjectOverlay overlay ? overlay.getBase() : obj);
			}
			_binding.updateMembership(_owner, bases);
		}
	}

	@Override
	public void persist(Transaction tx) {
		if (_fieldModel == null) {
			return;
		}
		if (_owner.isTransient()) {
			// Rows of a transient owner are not persisted individually - they are transferred to
			// the owner by applyState() and become persistent together with it.
			return;
		}

		// Persist new transient objects and build the persisted row list.
		List<TLObject> persistedList = new ArrayList<>();
		for (TLObject obj : _fieldModel.getCurrentList()) {
			if (obj instanceof TLObjectOverlay overlay) {
				persistedList.add(overlay.getBase());
			} else if (obj.tTransient()) {
				persistedList.add(persist(obj));
			} else {
				persistedList.add(obj);
			}
		}

		// Write the row set back and apply the binding's remove semantics to orphaned objects.
		_binding.commit(tx, _owner, persistedList, _originalPersistentObjects);
	}

	/**
	 * Creates the persistent object of a row added in this session, together with the parts of its
	 * compositions, see {@link PersistTransientAction#persistentCopy(TLObject, TLObject, TLReference)}.
	 */
	private TLObject persist(TLObject transientRow) {
		TLStructuredTypePart boundPart = _binding.getBoundPart();
		return PersistTransientAction.persistentCopy(transientRow, _owner.base(),
			boundPart instanceof TLReference reference ? reference : null);
	}

	@Override
	public void cancel() {
		// The rows are kept until the session ends, which releases what their cells hold.
		_fieldModel = null;
	}

	@Override
	public void revealAll() {
		if (_fieldModel != null) {
			_fieldModel.setRevealed(true);
		}
		for (CompositionRowModel row : _rowModels) {
			for (BoundFieldModel colModel : row.getColumnModels().values()) {
				colModel.setRevealed(true);
			}
		}
	}

	@Override
	public boolean isDirty() {
		return _fieldModel != null && _fieldModel.isDirty();
	}

	// -- Snapshot --

	/**
	 * The current state of the rows, to be {@link #restore(Snapshot) restored} later: which rows
	 * there are, the changes buffered for the existing rows, and the values of the new rows.
	 *
	 * @return The snapshot, or {@code null} if the session is not running.
	 */
	public Snapshot snapshot() {
		if (_fieldModel == null) {
			return null;
		}
		Map<TLObjectOverlay, TLObjectOverlay.Snapshot> overlays = new IdentityHashMap<>();
		Map<TLObject, Map<TLStructuredTypePart, Object>> values = new IdentityHashMap<>();
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null) {
				overlays.put(overlay, overlay.snapshot());
			} else {
				values.put(row.getRowObject(), storedValues(row.getRowObject()));
			}
		}
		return new Snapshot(new ArrayList<>(_fieldModel.getCurrentList()), new ArrayList<>(_rowModels), overlays,
			values);
	}

	/**
	 * Returns the rows to the state of the given snapshot, discarding all changes made since: rows
	 * added since are dropped, rows removed since are back, and the existing and new rows hold the
	 * values they held when the snapshot was taken.
	 *
	 * <p>
	 * The cells of the rows show the restored values, and the listeners are notified of the
	 * changed rows. Does nothing for a session that is not running.
	 * </p>
	 *
	 * @param snapshot
	 *        A snapshot taken from this session by {@link #snapshot()} while it runs.
	 */
	public void restore(Snapshot snapshot) {
		if (_fieldModel == null || snapshot == null) {
			return;
		}
		Set<CompositionRowModel> restoredRows = Collections.newSetFromMap(new IdentityHashMap<>());
		restoredRows.addAll(snapshot._rowModels);
		Set<CompositionRowModel> currentRows = Collections.newSetFromMap(new IdentityHashMap<>());
		currentRows.addAll(_rowModels);

		// Rows added since the snapshot leave the edit.
		for (CompositionRowModel row : _rowModels) {
			if (!restoredRows.contains(row)) {
				disposeCells(row);
				unregisterRow(row);
			}
		}
		// Rows removed since the snapshot return to the edit.
		for (CompositionRowModel row : snapshot._rowModels) {
			if (!currentRows.contains(row)) {
				registerRow(row);
			}
		}
		_rowModels.clear();
		_rowModels.addAll(snapshot._rowModels);

		for (Map.Entry<TLObjectOverlay, TLObjectOverlay.Snapshot> entry : snapshot._overlays.entrySet()) {
			entry.getKey().restore(entry.getValue());
		}
		for (Map.Entry<TLObject, Map<TLStructuredTypePart, Object>> entry : snapshot._values.entrySet()) {
			restoreValues(entry.getKey(), entry.getValue());
		}

		List<TLObject> rows = new ArrayList<>(snapshot._rows);
		_fieldModel.setValue(rows);
		for (CompositionRowModel row : _rowModels) {
			row.refreshColumnModels();
		}
		if (_validationModel != null) {
			for (TLObject row : rows) {
				revalidate(row);
			}
		}
		membershipChanged(rows);
	}

	private void unregisterRow(CompositionRowModel row) {
		TLObjectOverlay overlay = row.getRowOverlay();
		if (overlay != null) {
			_fieldModel.removeRowOverlay(overlay);
		}
		if (_validationModel != null) {
			_validationModel.removeOverlay(row.getRowObject());
		}
	}

	private void registerRow(CompositionRowModel row) {
		TLObjectOverlay overlay = row.getRowOverlay();
		if (overlay != null) {
			_fieldModel.addRowOverlay(overlay);
		}
		if (_validationModel != null) {
			_validationModel.addOverlay(row.getRowObject(), overlay != null ? overlay.getBase() : null);
		}
	}

	/**
	 * The values a new row holds in its attributes that are not computed.
	 */
	private static Map<TLStructuredTypePart, Object> storedValues(TLObject row) {
		Map<TLStructuredTypePart, Object> result = new HashMap<>();
		for (TLStructuredTypePart part : storedParts(row)) {
			result.put(part, TLObjectOverlay.copyValue(row.tValue(part)));
		}
		return result;
	}

	private static void restoreValues(TLObject row, Map<TLStructuredTypePart, Object> values) {
		for (TLStructuredTypePart part : storedParts(row)) {
			Object value = values.get(part);
			if (!Objects.equals(row.tValue(part), value)) {
				row.tUpdate(part, TLObjectOverlay.copyValue(value));
			}
		}
	}

	/**
	 * The attributes of the given row that hold a value of their own: neither derived, nor
	 * abstract, nor the backwards direction of a reference.
	 */
	private static List<TLStructuredTypePart> storedParts(TLObject row) {
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLStructuredTypePart part : row.tType().getAllParts()) {
			if (part.isDerived() || part.isAbstract()) {
				continue;
			}
			if (part instanceof TLReference reference && reference.isBackwards()) {
				continue;
			}
			result.add(part);
		}
		return result;
	}

	/**
	 * Re-runs the checks of all attributes of the given row.
	 */
	private void revalidate(TLObject row) {
		for (TLStructuredTypePart part : row.tType().getAllParts()) {
			_validationModel.onValueChanged(row, part);
		}
	}

	/**
	 * Checks the rows and reveals what is wrong with them.
	 *
	 * <p>
	 * The rows are valid when no attribute of a row and no cell reports an error, and the row set
	 * as a whole satisfies the constraints of the bound attribute.
	 * </p>
	 *
	 * @return Whether the rows are valid.
	 */
	public boolean checkValid() {
		if (_fieldModel == null) {
			return true;
		}
		boolean valid = true;
		if (_validationModel != null) {
			for (TLObject row : _fieldModel.getCurrentList()) {
				revalidate(row);
				for (TLStructuredTypePart part : row.tType().getAllParts()) {
					if (!_validationModel.getValidation(row, part).isValid()) {
						valid = false;
					}
				}
			}
		}
		revealAll();
		return validate() && valid;
	}

	/**
	 * The state of the rows of a {@link RowSetEditSession} at some point in time.
	 *
	 * @see RowSetEditSession#snapshot()
	 * @see RowSetEditSession#restore(Snapshot)
	 */
	public static final class Snapshot {

		final List<TLObject> _rows;

		final List<CompositionRowModel> _rowModels;

		final Map<TLObjectOverlay, TLObjectOverlay.Snapshot> _overlays;

		final Map<TLObject, Map<TLStructuredTypePart, Object>> _values;

		Snapshot(List<TLObject> rows, List<CompositionRowModel> rowModels,
				Map<TLObjectOverlay, TLObjectOverlay.Snapshot> overlays,
				Map<TLObject, Map<TLStructuredTypePart, Object>> values) {
			_rows = rows;
			_rowModels = rowModels;
			_overlays = overlays;
			_values = values;
		}

	}

	// -- Row Manipulation --

	/**
	 * Adds a new row to the row set, optionally initialized by the given function.
	 *
	 * <p>
	 * Creates a transient object of the binding's create type within the owner and appends it to
	 * the current list. Publishes the membership change and notifies the listeners.
	 * </p>
	 *
	 * @param initializer
	 *        Initializes attribute values of the created transient object before it is added to the
	 *        list, or {@code null} for an empty row.
	 * @return The created transient row object, or {@code null} if the session is not running, its
	 *         rows may not be changed, or the binding offers no row creation.
	 */
	public TLObject addRow(Consumer<TLObject> initializer) {
		if (_fieldModel == null || !_editable) {
			return null;
		}

		List<TLClass> createTypes = _binding.getCreateTypes();
		if (createTypes.isEmpty()) {
			return null;
		}
		TLClass targetType = createTypes.get(0);

		// Create the transient object within the owner, so that it navigates to its owner (e.g. in
		// an options expression) before it is stored.
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(targetType, _owner.base());
		if (initializer != null) {
			initializer.accept(transientObject);
		}

		// Register with validation model so constraints are evaluated.
		if (_validationModel != null) {
			_validationModel.addOverlay(transientObject, null);
		}

		_rowModels.add(CompositionRowModel.forNew(transientObject));

		// Replace the list instead of mutating it in place, so dirty tracking (comparison against
		// the initial snapshot) and value-change events observe the membership change.
		List<TLObject> currentList = new ArrayList<>(_fieldModel.getCurrentList());
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		membershipChanged(currentList);
		return transientObject;
	}

	/**
	 * Removes the given row from the row set. The binding's remove semantics are applied on commit,
	 * not here.
	 *
	 * @param rowObject
	 *        The row object (overlay or transient) to remove.
	 */
	public void removeRow(TLObject rowObject) {
		if (_fieldModel == null) {
			return;
		}
		int index = _fieldModel.getCurrentList().indexOf(rowObject);
		if (index >= 0) {
			deleteRow(rowObject, index);
		}
	}

	/**
	 * Removes a row from the row set.
	 *
	 * @param rowObject
	 *        The row to remove from the current list.
	 * @param rowIndex
	 *        The row's index in {@link #rowModels()}.
	 */
	public void deleteRow(TLObject rowObject, int rowIndex) {
		if (_fieldModel == null || !_editable) {
			return;
		}

		// Replace the list instead of mutating it in place, so dirty tracking (comparison against
		// the initial snapshot) and value-change events observe the membership change.
		List<TLObject> currentList = new ArrayList<>(_fieldModel.getCurrentList());
		currentList.remove(rowObject);
		_fieldModel.setValue(currentList);

		// Remove row model and unregister from validation model.
		if (rowIndex >= 0 && rowIndex < _rowModels.size()) {
			CompositionRowModel removedRow = _rowModels.remove(rowIndex);
			// A removed row takes no part in the save, nor do edits within its cells.
			disposeCells(removedRow);
			TLObjectOverlay removedOverlay = removedRow.getRowOverlay();
			if (removedOverlay != null) {
				_fieldModel.removeRowOverlay(removedOverlay);
			}
			if (_validationModel != null) {
				if (removedOverlay != null) {
					_validationModel.removeOverlay(removedOverlay);
				} else if (rowObject.tTransient()) {
					_validationModel.removeOverlay(rowObject);
				}
			}
		}

		membershipChanged(currentList);
	}

	/**
	 * Publishes a changed row list to the owner, the validation and the form's dirty state, and
	 * notifies the listeners.
	 */
	private void membershipChanged(List<TLObject> currentList) {
		_binding.updateMembership(_owner, currentList);

		// Notify the validation model that the bound attribute changed, so that constraints on the
		// attribute (e.g. min count) are re-evaluated.
		TLObject owner = _owner.object();
		TLStructuredTypePart boundPart = _binding.getBoundPart();
		if (_validationModel != null && owner != null && boundPart != null) {
			_validationModel.onValueChanged(owner, boundPart);
		}

		_owner.form().updateDirtyState();

		for (Listener listener : _listeners) {
			listener.onRowsChanged(this);
		}
	}

	private void fireValidationChanged() {
		for (Listener listener : _listeners) {
			listener.onRowSetValidationChanged(this);
		}
	}

	// -- Cell Model Support --

	/**
	 * The field model holding the edited value of a cell: created by the column's
	 * {@link CellEditing} once per row and column, and wired for validation and dirty tracking.
	 *
	 * @param row
	 *        The row object (overlay or transient).
	 * @param column
	 *        The column whose cell is edited.
	 * @return The field model of the cell, or {@code null} if the cell cannot be edited (the rows
	 *         may not be changed, the column offers no edit on this row, or the row is not part of
	 *         the session).
	 */
	public BoundFieldModel cellModel(TLObject row, ColumnSetup column) {
		CellEditing editing = column.editing();
		if (!_editable || editing == null || !editing.canEdit(row)) {
			return null;
		}
		CompositionRowModel rowModel = findRowModel(row);
		if (rowModel == null) {
			return null;
		}
		String columnName = column.name();
		BoundFieldModel cellFieldModel = rowModel.getColumnModel(columnName);
		if (cellFieldModel == null) {
			cellFieldModel = editing.createModel(row, _owner.form());
			cellFieldModel.setEditable(true);
			rowModel.putColumnModel(columnName, cellFieldModel);

			wireCell(cellFieldModel, row);
		}
		return cellFieldModel;
	}

	/**
	 * Wires a freshly created cell model into the form: the validation of the attribute it edits,
	 * dirty propagation and the live re-evaluation of constraints.
	 *
	 * <p>
	 * Constraints are declared on attributes, so a cell holding a value no attribute holds has none
	 * to show.
	 * </p>
	 */
	private void wireCell(BoundFieldModel cellFieldModel, TLObject row) {
		TLStructuredTypePart part =
			cellFieldModel instanceof AttributeFieldModel attributeModel ? attributeModel.getPart() : null;
		if (part != null) {
			wireCellValidation(cellFieldModel, row, part);
		}
		addCellListener(cellFieldModel, row, part);
	}

	/**
	 * Wires a {@link ConstraintValidationListener} that propagates validation results from the
	 * {@link FormValidationModel} to the cell's field model.
	 */
	private void wireCellValidation(BoundFieldModel model, TLObject rowObject, TLStructuredTypePart part) {
		FormValidationModel validationModel = _validationModel;
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
		_validationListeners.add(listener);
	}

	/**
	 * Adds a listener to a cell field model that propagates dirty state, reveals the field after
	 * user interaction, and triggers live constraint re-evaluation via the
	 * {@link FormValidationModel}.
	 *
	 * @param cellFieldModel
	 *        The field model holding the cell's edited value.
	 * @param rowObject
	 *        The row the cell belongs to.
	 * @param part
	 *        The attribute the cell writes to, or {@code null} for a cell holding a value no
	 *        attribute holds - there is then no constraint to re-evaluate.
	 */
	private void addCellListener(BoundFieldModel cellFieldModel, TLObject rowObject, TLStructuredTypePart part) {
		FormControl form = _owner.form();
		cellFieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				form.updateDirtyState();

				// Reveal validation errors after user interaction.
				cellFieldModel.setRevealed(true);

				// Trigger live constraint re-evaluation.
				FormValidationModel validationModel = _validationModel;
				if (validationModel != null && part != null) {
					validationModel.onValueChanged(rowObject, part);
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// No-op.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// The cell's displayed validation changed, so commands gated on visible errors
				// must re-evaluate - after the new result was applied, not while it is computed.
				form.fireValidityChanged();
			}
		});
	}

	/**
	 * The row model tracking the given row object, or {@code null} if unknown.
	 */
	public CompositionRowModel findRowModel(TLObject rowObject) {
		for (CompositionRowModel row : _rowModels) {
			if (row.getRowObject() == rowObject) {
				return row;
			}
		}
		return null;
	}

}
