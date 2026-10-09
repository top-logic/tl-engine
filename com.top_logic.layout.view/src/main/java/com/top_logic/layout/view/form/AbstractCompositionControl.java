/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.table.CellEditing;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Base class for form controls that display and edit a set of row objects inline.
 *
 * <p>
 * The editing itself - row overlays, per-cell field models, validation, participation in the
 * form's save - is a {@link RowSetEditSession}; this control displays the rows of a session and
 * offers to change them.
 * </p>
 *
 * <p>
 * A control created for a form ({@link #AbstractCompositionControl(ReactContext, FormControl,
 * RowSetBinding, String)}) edits rows of the form object and owns its sessions: it implements
 * {@link FormModelListener} to react to form state changes and starts a session whenever the form
 * enters edit mode, ending it when the form leaves edit mode or the control is disposed. A control
 * created for a running session
 * ({@link #AbstractCompositionControl(ReactContext, RowSetEditSession, String)}) only displays that
 * session, which outlives the control.
 * </p>
 *
 * <p>
 * The membership semantics of the row set (which objects are the rows, what adding and removing
 * means, what is written back on commit) are delegated to a {@link RowSetBinding}. An
 * {@link AttributeRowSetBinding} derives them from a composition or plain reference of the form
 * object; a {@link QueryRowSetBinding} computes the rows from a query and takes them from explicit
 * configuration.
 * </p>
 *
 * <p>
 * Subclasses render the row objects: {@link #buildContent(List, boolean)} builds the presentation
 * for a row list, {@link #refreshRows()} updates it after {@link #addRow()} or
 * {@link #removeRow(TLObject)} changed the list.
 * </p>
 */
public abstract class AbstractCompositionControl extends ReactControl implements FormModelListener {

	private final FormControl _formControl;

	private final RowSetBinding _binding;

	/**
	 * Whether this control starts and ends its sessions itself, following the edit mode of its
	 * form.
	 */
	private final boolean _ownsSession;

	/** The session displayed, {@code null} while no session runs. */
	private RowSetEditSession _session;

	private final RowSetEditSession.Listener _sessionListener = new RowSetEditSession.Listener() {
		@Override
		public void onRowsChanged(RowSetEditSession session) {
			refreshRows();
		}

		@Override
		public void onRowSetValidationChanged(RowSetEditSession session) {
			updateCompositionErrorDisplay();
		}
	};

	/**
	 * Creates a new {@link AbstractCompositionControl} over the given row-set binding of the form
	 * object.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics (row objects, create types, remove mode, commit).
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, FormControl formControl,
			RowSetBinding binding, String reactModule) {
		super(context, null, reactModule);
		_formControl = formControl;
		_binding = binding;
		_ownsSession = true;

		formControl.addFormModelListener(this);
	}

	/**
	 * Creates a new {@link AbstractCompositionControl} editing a composition or plain reference
	 * attribute of the form object.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param compositionAttributeName
	 *        The name of the reference attribute on the parent object holding the rows.
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, String reactModule) {
		this(context, formControl, new AttributeRowSetBinding(compositionAttributeName), reactModule);
	}

	/**
	 * Creates a new {@link AbstractCompositionControl} displaying the given running session.
	 *
	 * <p>
	 * The control neither starts nor ends the session: the session keeps its state when the
	 * control is disposed.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param session
	 *        The session to display.
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, RowSetEditSession session, String reactModule) {
		super(context, null, reactModule);
		_formControl = session.owner().form();
		_binding = session.binding();
		_ownsSession = false;
		_session = session;
	}

	/**
	 * Initializes the presentation.
	 *
	 * <p>
	 * Must be called after construction. A control displaying a given session builds the content
	 * of that session, editable when the session {@link RowSetEditSession#isEditable() may change}
	 * its rows. A control owning its sessions resolves the row-set binding against the form's
	 * current object and builds the initial content; when the binding is not available (e.g. the
	 * current object's type does not declare the bound attribute), builds empty view-mode content.
	 * </p>
	 *
	 * <p>
	 * When the form is already in edit mode (e.g. the control is created lazily within a tab of a
	 * form that is opened in edit mode), the edit session of this control starts right away, as it
	 * does for a control that observes the form entering edit mode.
	 * </p>
	 */
	public void init() {
		if (!_ownsSession) {
			_session.addListener(_sessionListener);
			buildContent(_session.currentRows(), _session.isRunning() && _session.isEditable());
			updateCompositionErrorDisplay();
			return;
		}
		TLObject currentObject = _formControl.getCurrentObject();
		if (!_binding.resolve(currentObject)) {
			buildContent(List.of(), false);
			return;
		}
		if (_formControl.isEditMode()) {
			enterEditMode();
		} else {
			buildContent(_binding.readRows(currentObject), false);
		}
	}

	/**
	 * Builds the presentation for the given row objects.
	 *
	 * @param rows
	 *        The row objects to display: overlays and transient objects in edit mode, persistent
	 *        objects in view mode.
	 * @param editMode
	 *        Whether the form is in edit mode.
	 */
	protected abstract void buildContent(List<? extends TLObject> rows, boolean editMode);

	/**
	 * Updates the presentation after the row list changed through {@link #addRow()} or
	 * {@link #removeRow(TLObject)}. The current list is available from the
	 * {@link #fieldModel()}.
	 */
	protected abstract void refreshRows();

	/**
	 * The parent form control.
	 */
	protected final FormControl formControl() {
		return _formControl;
	}

	/**
	 * The object holding the displayed rows: the form's current object, or the owner of the
	 * displayed session.
	 */
	protected final TLObject ownerObject() {
		return _ownsSession ? _formControl.getCurrentObject() : _session.owner().object();
	}

	/**
	 * The row-set semantics of this control.
	 */
	protected final RowSetBinding binding() {
		return _binding;
	}

	/**
	 * The displayed session, or {@code null} while no session runs.
	 */
	public final RowSetEditSession session() {
		return _session;
	}

	/**
	 * The name of the bound reference attribute, or {@code null} for bindings without a bound
	 * attribute (e.g. a query binding).
	 */
	protected final String compositionAttributeName() {
		return _binding instanceof AttributeRowSetBinding attributeBinding
			? attributeBinding.getAttributeName()
			: null;
	}

	/**
	 * The bound reference of the row-set binding, or {@code null} before {@link #init()}, when the
	 * current object's type does not declare the attribute, or for a binding without a bound
	 * attribute.
	 */
	protected final TLStructuredTypePart compositionPart() {
		return _binding.getBoundPart();
	}

	/**
	 * The field model holding the edited row list, or {@code null} outside edit mode.
	 */
	protected final CompositionFieldModel fieldModel() {
		return _session == null ? null : _session.fieldModel();
	}

	/**
	 * Whether an edit session is running.
	 */
	protected final boolean isEditing() {
		return fieldModel() != null;
	}

	// -- FormModelListener --

	@Override
	public void onFormStateChanged(FormModel source) {
		TLObject currentObject = source.getCurrentObject();

		if (!_binding.resolve(currentObject)) {
			endSession();
			return;
		}

		if (source.isEditMode()) {
			enterEditMode();
		} else {
			exitEditMode(currentObject);
		}
	}

	private void enterEditMode() {
		// End a previous session (e.g. after executeApply resets the form's edit session).
		endSession();

		_session = new RowSetEditSession(RowSetOwner.ofForm(_formControl), _binding);
		_session.addListener(_sessionListener);
		_session.start();

		// Rebuild the presentation in edit mode.
		buildContent(_session.currentRows(), true);
	}

	/**
	 * Reflects the current validation error of the row-set {@link #fieldModel()} in the
	 * presentation.
	 *
	 * <p>
	 * Called whenever the bound attribute's validation state changes and when the edit session
	 * ends (with a then-cleared field model). Subclasses that render an error location override
	 * this.
	 * </p>
	 */
	protected void updateCompositionErrorDisplay() {
		// Hook for subclasses.
	}

	private void exitEditMode(TLObject currentObject) {
		endSession();

		// Rebuild the presentation in view mode.
		List<TLObject> rows = _binding.readRows(currentObject);
		buildContent(rows, false);
	}

	/**
	 * Ends the session this control owns, discarding all its state; stops displaying a session it
	 * does not own.
	 */
	private void endSession() {
		RowSetEditSession session = _session;
		if (session == null) {
			return;
		}
		session.removeListener(_sessionListener);
		if (_ownsSession) {
			_session = null;
			session.end();
		}
		// Clear any displayed composition error.
		updateCompositionErrorDisplay();
	}

	// -- Row Manipulation --

	/**
	 * Adds a new, empty row to the row set.
	 *
	 * @return The created transient row object, or {@code null} if no edit session is running or the
	 *         binding offers no row creation.
	 * @see RowSetEditSession#addRow(Consumer)
	 */
	public TLObject addRow() {
		return addRow(null);
	}

	/**
	 * Adds a new row to the row set, optionally initialized by the given function.
	 *
	 * @param initializer
	 *        Initializes attribute values of the created transient object before it is added to the
	 *        list, or {@code null} for an empty row.
	 * @return The created transient row object, or {@code null} if no edit session is running or the
	 *         binding offers no row creation.
	 * @see RowSetEditSession#addRow(Consumer)
	 */
	public TLObject addRow(Consumer<TLObject> initializer) {
		return _session == null ? null : _session.addRow(initializer);
	}

	/**
	 * Removes the given row from the row set. The binding's remove semantics are applied on commit,
	 * not here.
	 *
	 * @param rowObject
	 *        The row object (overlay or transient) to remove.
	 */
	public void removeRow(TLObject rowObject) {
		if (_session != null) {
			_session.removeRow(rowObject);
		}
	}

	// -- Cell Model Support --

	/**
	 * Builds the editable control for a cell: the control the column's {@link CellEditing} says the
	 * value is entered with, bound to the field model of that cell, see
	 * {@link RowSetEditSession#cellModel(TLObject, ColumnSetup)}.
	 *
	 * @param context
	 *        The React context for control creation.
	 * @param row
	 *        The row object (overlay or transient).
	 * @param column
	 *        The column whose cell is edited.
	 * @return The editable cell control, or {@code null} if the cell cannot be edited (the column
	 *         offers no edit on this row, or the row is not part of the session).
	 */
	protected final ReactControl buildEditCellControl(ReactContext context, TLObject row, ColumnSetup column) {
		if (_session == null) {
			return null;
		}
		BoundFieldModel cellFieldModel = _session.cellModel(row, column);
		if (cellFieldModel == null) {
			return null;
		}
		return column.editing().createControl(context, row, cellFieldModel);
	}

	/**
	 * The row model tracking the given row object, or {@code null} if unknown.
	 */
	protected final CompositionRowModel findRowModel(TLObject rowObject) {
		return _session == null ? null : _session.findRowModel(rowObject);
	}

	@Override
	protected void onCleanup() {
		if (_ownsSession) {
			_formControl.removeFormModelListener(this);
		}
		endSession();
		super.onCleanup();
	}
}
