/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.error.TopLogicException;

/**
 * Session-scoped form control managing the editing lifecycle (view/edit mode, locking, KB
 * transactions).
 *
 * <p>
 * Implements {@link FormModel} so that field controls can observe form state changes via
 * {@link FormModelListener}. Every state transition (enter/exit edit mode, apply, input change)
 * follows the same pattern: update internal state, then {@link #fireFormStateChanged()}.
 * </p>
 *
 * <p>
 * The control provides four commands: edit, apply, save, and cancel. In view mode, the current
 * object is presented read-only. Entering edit mode creates a {@link TLObjectOverlay}, acquires a
 * lock on the object via the configured {@link LockHandler}, and fires a state change so that
 * listening field controls can switch to editable state.
 * </p>
 */
public class FormControl extends ReactControl implements FormModel, ModelListener {

	/** State key for the current edit mode. */
	private static final String EDIT_MODE = "editMode";

	/** State key for the dirty flag. */
	private static final String DIRTY = "dirty";

	/** State key for the overall form validity. */
	private static final String VALID = "valid";

	/** State key for the no-model placeholder message. */
	private static final String NO_MODEL_MESSAGE = "noModelMessage";

	private TLObject _currentObject;

	private TLObjectOverlay _overlay;

	private boolean _editMode;

	private final LockHandler _lockHandler;

	private ViewChannel _inputChannel;

	private ViewChannel _editModeChannel;

	private ViewChannel _dirtyChannel;

	private FormValidationModel _validationModel;

	private final List<FormModelListener> _formModelListeners = new ArrayList<>();

	private final List<AbstractFieldModel> _fieldModels = new ArrayList<>();

	private final List<FormParticipant> _participants = new ArrayList<>();

	private final ViewChannel.ChannelListener _inputListener = this::handleInputChanged;

	private final ViewChannel.ChannelListener _editModeListener = this::handleEditModeChannelChanged;

	/**
	 * Guard flag to prevent re-entrant loops when publishing to and reacting from the edit mode
	 * channel.
	 */
	private boolean _updatingEditMode;

	private final String _noModelMessage;

	private ModelScope _modelScope;

	/**
	 * Creates a new {@link FormControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param initialObject
	 *        The initial object to display, may be {@code null}.
	 * @param noModelMessage
	 *        The message to display when no object is available.
	 * @param lockHandler
	 *        The {@link LockHandler} for acquiring/releasing locks during editing.
	 */
	public FormControl(ReactContext context, TLObject initialObject, String noModelMessage, LockHandler lockHandler) {
		super(context, initialObject, "TLFormLayout");
		_currentObject = initialObject;
		_noModelMessage = noModelMessage;
		_lockHandler = lockHandler;
		_editMode = false;
		putState(EDIT_MODE, Boolean.FALSE);
		putState(DIRTY, Boolean.FALSE);
		updateNoModelMessage();
	}

	@Override
	public TLObject getCurrentObject() {
		if (_editMode && _overlay != null) {
			return _overlay;
		}
		return _currentObject;
	}

	/**
	 * The current overlay, or {@code null} if not in edit mode.
	 */
	public TLObjectOverlay getOverlay() {
		return _overlay;
	}

	/**
	 * The current validation model, or {@code null} if not in edit mode.
	 */
	public FormValidationModel getValidationModel() {
		return _validationModel;
	}

	@Override
	public boolean isEditMode() {
		return _editMode;
	}

	@Override
	public void addFormModelListener(FormModelListener listener) {
		_formModelListeners.add(listener);
	}

	@Override
	public void removeFormModelListener(FormModelListener listener) {
		_formModelListeners.remove(listener);
	}

	/**
	 * Registers a field model for reveal-all support.
	 */
	public void registerFieldModel(AbstractFieldModel model) {
		_fieldModels.add(model);
	}

	/**
	 * Unregisters a field model (e.g., when the field is removed dynamically).
	 */
	public void unregisterFieldModel(AbstractFieldModel model) {
		_fieldModels.remove(model);
	}

	/**
	 * Registers a {@link FormParticipant} to participate in the form's editing lifecycle.
	 *
	 * @param participant
	 *        The participant to register.
	 */
	public void registerParticipant(FormParticipant participant) {
		_participants.add(participant);
	}

	/**
	 * Unregisters a {@link FormParticipant}.
	 *
	 * @param participant
	 *        The participant to unregister.
	 */
	public void unregisterParticipant(FormParticipant participant) {
		_participants.remove(participant);
	}

	/**
	 * Sets all registered field models to revealed, making hidden validation errors visible.
	 */
	public void revealAllValidation() {
		for (AbstractFieldModel model : _fieldModels) {
			model.setRevealed(true);
		}
		for (FormParticipant participant : _participants) {
			participant.revealAll();
		}
	}

	/**
	 * Sets the input channel that provides the object to display.
	 *
	 * @param channel
	 *        The input channel.
	 */
	public void setInputChannel(ViewChannel channel) {
		if (_inputChannel != null) {
			_inputChannel.removeListener(_inputListener);
		}
		_inputChannel = channel;
		if (_inputChannel != null) {
			_inputChannel.addListener(_inputListener);
		}
	}

	/**
	 * Sets the optional edit mode channel. When set, the form both publishes edit mode changes to
	 * this channel and reacts to external changes from it.
	 *
	 * <p>
	 * When the channel value changes from outside (i.e., not triggered by this control):
	 * <ul>
	 * <li>If the channel becomes {@code true} and the form is not in edit mode, it enters edit
	 * mode.</li>
	 * <li>If the channel becomes {@code false} and the form is in edit mode, it cancels editing.</li>
	 * </ul>
	 * </p>
	 *
	 * @param channel
	 *        The edit mode channel, may be {@code null}.
	 */
	public void setEditModeChannel(ViewChannel channel) {
		if (_editModeChannel != null) {
			_editModeChannel.removeListener(_editModeListener);
		}
		_editModeChannel = channel;
		if (_editModeChannel != null) {
			_editModeChannel.addListener(_editModeListener);
		}
	}

	/**
	 * Sets the optional dirty channel. When set, the form publishes dirty state changes to this
	 * channel.
	 *
	 * @param channel
	 *        The dirty channel, may be {@code null}.
	 */
	public void setDirtyChannel(ViewChannel channel) {
		_dirtyChannel = channel;
	}

	/**
	 * Registers this control as a {@link ModelListener} on the given scope for the current object.
	 *
	 * <p>
	 * Called lazily on first render via {@code addBeforeWriteAction}, matching the
	 * {@link com.top_logic.layout.view.model.ObservableTableModel} pattern.
	 * </p>
	 *
	 * @param scope
	 *        The model scope to observe.
	 */
	public void attach(ModelScope scope) {
		if (_modelScope != null) {
			return; // Already attached.
		}
		_modelScope = scope;
		registerModelListener();
	}

	/**
	 * Removes all model listeners and releases the scope reference.
	 */
	public void detach() {
		deregisterModelListener();
		_modelScope = null;
	}

	private void registerModelListener() {
		if (_modelScope == null || _currentObject == null || _currentObject.tTransient()) {
			return;
		}
		_modelScope.addModelListener(_currentObject, this);
	}

	private void deregisterModelListener() {
		if (_modelScope == null || _currentObject == null || _currentObject.tTransient()) {
			return;
		}
		_modelScope.removeModelListener(_currentObject, this);
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		if (_currentObject == null) {
			return;
		}
		ModelChangeEvent.ChangeType change = event.getChange(_currentObject);
		if (change == ModelChangeEvent.ChangeType.DELETED) {
			onCurrentObjectDeleted();
		} else if (change == ModelChangeEvent.ChangeType.UPDATED && !_editMode) {
			// In view mode: refresh field values. In edit mode: overlay buffers changes,
			// base values become visible after save/cancel.
			fireFormStateChanged();
		}
	}

	private void onCurrentObjectDeleted() {
		if (_editMode) {
			exitEditMode();
		}
		deregisterModelListener();
		_currentObject = null;
		updateNoModelMessage();
		fireFormStateChanged();
	}

	/**
	 * Sets the child controls of this form.
	 *
	 * <p>
	 * Called by {@link com.top_logic.layout.view.element.FormElement} during control creation to
	 * assign the child control list as React state.
	 * </p>
	 *
	 * @param children
	 *        The child controls.
	 */
	public void setChildren(List<ReactControl> children) {
		putState("children", children);
	}

	/**
	 * Enters edit mode by acquiring a lock, creating an overlay, and notifying listeners.
	 */
	public void enterEditMode() {
		if (_editMode || _currentObject == null) {
			return;
		}

		// Acquire lock first -- if this fails, no overlay is created.
		_lockHandler.acquireLock(_currentObject);
		_overlay = new TLObjectOverlay(_currentObject);

		_validationModel = new FormValidationModel();
		_validationModel.addOverlay(_overlay, _currentObject);
		_validationModel.addConstraintValidationListener((overlay, attribute, result) -> {
			putState(VALID, Boolean.valueOf(_validationModel.isValid()));
		});
		putState(VALID, Boolean.valueOf(_validationModel.isValid()));

		_editMode = true;
		putState(EDIT_MODE, Boolean.TRUE);
		updateEditModeChannel();
		updateDirtyState();

		fireFormStateChanged();
	}

	/**
	 * Applies overlay changes to the knowledge base without leaving edit mode.
	 *
	 * <p>
	 * Opens a KB transaction, transfers overlay changes to the base object, commits, and resets the
	 * overlay. Fires state changed so that field controls can re-read the reset overlay.
	 * </p>
	 */
	public void executeApply() {
		if (!_editMode || _overlay == null || (!_overlay.isDirty() && !hasParticipantChanges())) {
			return;
		}

		if (_validationModel != null && !_validationModel.isValid()) {
			revealAllValidation();
			throw new TopLogicException(
				com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
		}

		// Validate ALL participants so every field gets its error state set.
		boolean participantsValid = true;
		for (FormParticipant participant : _participants) {
			if (!participant.validate()) {
				participantsValid = false;
			}
		}
		if (!participantsValid) {
			revealAllValidation();
			throw new TopLogicException(
				com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.FORM_APPLY);
		try {
			for (FormParticipant participant : _participants) {
				participant.apply(tx);
			}
			_overlay.applyTo(_currentObject);
			tx.commit();
		} finally {
			tx.rollback();
		}

		_overlay.reset();
		updateDirtyState();

		fireFormStateChanged();
	}

	/**
	 * Saves changes (applies and exits edit mode).
	 */
	public void executeSave() {
		if (!_editMode || _overlay == null) {
			return;
		}

		if (_validationModel != null && !_validationModel.isValid()) {
			revealAllValidation();
			throw new TopLogicException(
				com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
		}

		// Validate ALL participants so every field gets its error state set.
		boolean participantsValid = true;
		for (FormParticipant participant : _participants) {
			if (!participant.validate()) {
				participantsValid = false;
			}
		}
		if (!participantsValid) {
			revealAllValidation();
			throw new TopLogicException(
				com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
		}

		if (_overlay.isDirty() || hasParticipantChanges()) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction(I18NConstants.FORM_SAVE);
			try {
				// Participants apply first (e.g. composition tables persist new objects and
				// update reference lists in the overlay), then the overlay transfers all
				// changes to the KB.
				for (FormParticipant participant : _participants) {
					participant.apply(tx);
				}
				_overlay.applyTo(_currentObject);
				tx.commit();
			} finally {
				tx.rollback();
			}
		}

		exitEditMode();
	}

	private boolean hasParticipantChanges() {
		for (FormParticipant participant : _participants) {
			if (participant.isDirty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Cancels editing, discarding overlay changes and releasing the lock.
	 */
	public void executeCancel() {
		if (!_editMode) {
			return;
		}
		for (FormParticipant participant : _participants) {
			participant.cancel();
		}
		exitEditMode();
	}

	/**
	 * Recalculates the form-level dirty state.
	 *
	 * <p>
	 * Called by field controls when a value changes so the form's overall dirty state is updated.
	 * </p>
	 */
	public void updateDirtyState() {
		boolean dirty = _editMode && _overlay != null && (_overlay.isDirty() || hasParticipantChanges());
		putState(DIRTY, Boolean.valueOf(dirty));
		if (_dirtyChannel != null) {
			_dirtyChannel.set(Boolean.valueOf(dirty));
		}
	}

	private void exitEditMode() {
		_overlay = null;
		_editMode = false;

		releaseLock();

		putState(EDIT_MODE, Boolean.FALSE);
		updateEditModeChannel();
		updateDirtyState();

		fireFormStateChanged();

		_validationModel = null;
		_fieldModels.clear();
		_participants.clear();
		putState(VALID, Boolean.TRUE);
	}

	private void releaseLock() {
		_lockHandler.releaseLock();
	}

	private void fireFormStateChanged() {
		for (FormModelListener listener : _formModelListeners) {
			listener.onFormStateChanged(this);
		}
	}

	private void updateEditModeChannel() {
		if (_editModeChannel != null) {
			_updatingEditMode = true;
			try {
				_editModeChannel.set(Boolean.valueOf(_editMode));
			} finally {
				_updatingEditMode = false;
			}
		}
	}

	private void handleEditModeChannelChanged(ViewChannel sender, Object oldValue, Object newValue) {
		if (_updatingEditMode) {
			// Ignore changes that we ourselves triggered to prevent infinite loops.
			return;
		}
		boolean channelEditMode = Boolean.TRUE.equals(newValue);
		if (channelEditMode && !_editMode) {
			enterEditMode();
		} else if (!channelEditMode && _editMode) {
			executeCancel();
		}
	}

	private void updateNoModelMessage() {
		if (_currentObject == null) {
			putState(NO_MODEL_MESSAGE, _noModelMessage);
		} else {
			putState(NO_MODEL_MESSAGE, null);
		}
	}

	private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
		if (_editMode) {
			exitEditMode();
		}
		deregisterModelListener();
		_currentObject = (TLObject) newValue;
		registerModelListener();
		updateNoModelMessage();

		fireFormStateChanged();
	}

	@Override
	protected void cleanupChildren() {
		// No-op: child controls are managed by the React rendering tree.
	}

	@Override
	protected void onCleanup() {
		if (_editMode) {
			exitEditMode();
		}
		deregisterModelListener();
		if (_inputChannel != null) {
			_inputChannel.removeListener(_inputListener);
		}
		if (_editModeChannel != null) {
			_editModeChannel.removeListener(_editModeListener);
		}
	}

	/**
	 * Command that enters edit mode.
	 */
	@ReactCommand("formEdit")
	void handleEdit() {
		enterEditMode();
	}

	/**
	 * Command that applies overlay changes without leaving edit mode.
	 */
	@ReactCommand("formApply")
	void handleApply() {
		executeApply();
	}

	/**
	 * Command that saves changes (applies and exits edit mode).
	 */
	@ReactCommand("formSave")
	void handleSave() {
		executeSave();
	}

	/**
	 * Command that cancels editing, discarding changes.
	 */
	@ReactCommand("formCancel")
	void handleCancel() {
		executeCancel();
	}
}
