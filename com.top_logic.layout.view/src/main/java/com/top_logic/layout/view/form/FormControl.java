/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Session-scoped form control managing the editing lifecycle (view/edit mode, locking, KB
 * transactions).
 *
 * <p>
 * The control provides four commands: edit, apply, save, and cancel. In view mode, the current
 * object is presented read-only. Entering edit mode creates a {@link TLObjectOverlay}, acquires a
 * {@link Lock} on the object, and switches all registered field controls to editable state.
 * </p>
 *
 * <p>
 * Apply commits overlay changes to the knowledge base without leaving edit mode. Save applies and
 * then exits edit mode. Cancel discards the overlay and releases the lock.
 * </p>
 */
public class FormControl extends ReactControl {

	/** Lock operation name used when locking objects for editing. */
	private static final String EDIT_OPERATION = "edit";

	/** State key for the current edit mode. */
	private static final String EDIT_MODE = "editMode";

	/** State key for the dirty flag. */
	private static final String DIRTY = "dirty";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		EditCommand.INSTANCE, ApplyCommand.INSTANCE, SaveCommand.INSTANCE, CancelCommand.INSTANCE);

	private TLObject _currentObject;

	private TLObjectOverlay _overlay;

	private boolean _editMode;

	private Lock _lock;

	private ViewChannel _inputChannel;

	private ViewChannel _editModeChannel;

	private ViewChannel _dirtyChannel;

	private final List<ReactControl> _fields = new ArrayList<>();

	private final ViewChannel.ChannelListener _inputListener = this::handleInputChanged;

	/**
	 * Creates a new {@link FormControl}.
	 *
	 * @param initialObject
	 *        The initial object to display, may be {@code null}.
	 */
	public FormControl(TLObject initialObject) {
		super(initialObject, "TLFormLayout", COMMANDS);
		_currentObject = initialObject;
		_editMode = false;
		putState(EDIT_MODE, Boolean.FALSE);
		putState(DIRTY, Boolean.FALSE);
	}

	/**
	 * The current object being displayed or edited.
	 *
	 * <p>
	 * In edit mode, returns the {@link TLObjectOverlay}. In view mode, returns the base
	 * {@link TLObject}.
	 * </p>
	 */
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
	 * Whether the form is currently in edit mode.
	 */
	public boolean isEditMode() {
		return _editMode;
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
	 * Sets the optional edit mode channel. When set, the form publishes edit mode changes to this
	 * channel.
	 *
	 * @param channel
	 *        The edit mode channel, may be {@code null}.
	 */
	public void setEditModeChannel(ViewChannel channel) {
		_editModeChannel = channel;
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
	 * Registers a field control with this form.
	 *
	 * <p>
	 * Registered fields are notified when the current object changes or edit mode changes.
	 * </p>
	 *
	 * @param field
	 *        The field control to register.
	 */
	public void registerField(ReactControl field) {
		_fields.add(field);
	}

	/**
	 * Enters edit mode by creating an overlay, acquiring a lock, and switching fields to editable.
	 */
	public void enterEditMode() {
		if (_editMode) {
			return;
		}
		if (_currentObject == null) {
			return;
		}

		_overlay = new TLObjectOverlay(_currentObject);
		_lock = LockService.getInstance().acquireLock(EDIT_OPERATION, _currentObject);

		_editMode = true;
		putState(EDIT_MODE, Boolean.TRUE);
		updateEditModeChannel();
		updateDirtyState();
		notifyFields();
	}

	/**
	 * Applies overlay changes to the knowledge base without leaving edit mode.
	 *
	 * <p>
	 * Opens a KB transaction, transfers overlay changes to the base object, commits, and resets the
	 * overlay.
	 * </p>
	 */
	public void executeApply() {
		if (!_editMode || _overlay == null) {
			return;
		}
		if (!_overlay.isDirty()) {
			return;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.FORM_APPLY);
		try {
			_overlay.applyTo(_currentObject);
			tx.commit();
		} finally {
			tx.rollback();
		}

		_overlay.reset();
		updateDirtyState();
		notifyFields();
	}

	/**
	 * Saves changes (applies and exits edit mode).
	 */
	public void executeSave() {
		if (!_editMode || _overlay == null) {
			return;
		}

		if (_overlay.isDirty()) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction(I18NConstants.FORM_SAVE);
			try {
				_overlay.applyTo(_currentObject);
				tx.commit();
			} finally {
				tx.rollback();
			}
		}

		exitEditMode();
	}

	/**
	 * Cancels editing, discarding overlay changes and releasing the lock.
	 */
	public void executeCancel() {
		if (!_editMode) {
			return;
		}
		exitEditMode();
	}

	/**
	 * Updates the dirty channel based on the current overlay state.
	 *
	 * <p>
	 * Package-visible so that field controls can trigger a dirty update when values change.
	 * </p>
	 */
	void updateDirtyChannel() {
		updateDirtyState();
	}

	/**
	 * Notifies all registered field controls to refresh their display.
	 */
	void notifyFields() {
		for (ReactControl field : _fields) {
			field.requestRepaint();
		}
	}

	private void exitEditMode() {
		_overlay = null;
		_editMode = false;

		releaseLock();

		putState(EDIT_MODE, Boolean.FALSE);
		updateEditModeChannel();
		updateDirtyState();
		notifyFields();
	}

	private void releaseLock() {
		if (_lock != null && _lock.isStateAcquired()) {
			try {
				_lock.unlock();
			} catch (IllegalStateException ex) {
				Logger.warn("Failed to release lock.", ex, FormControl.class);
			}
		}
		_lock = null;
	}

	private void updateEditModeChannel() {
		if (_editModeChannel != null) {
			_editModeChannel.set(Boolean.valueOf(_editMode));
		}
	}

	private void updateDirtyState() {
		boolean dirty = _editMode && _overlay != null && _overlay.isDirty();
		putState(DIRTY, Boolean.valueOf(dirty));
		if (_dirtyChannel != null) {
			_dirtyChannel.set(Boolean.valueOf(dirty));
		}
	}

	private void handleInputChanged(ViewChannel sender, Object oldValue, Object newValue) {
		if (_editMode) {
			exitEditMode();
		}
		_currentObject = (TLObject) newValue;
		notifyFields();
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl field : _fields) {
			field.cleanupTree();
		}
	}

	@Override
	protected void internalDetach() {
		if (_editMode) {
			exitEditMode();
		}
		if (_inputChannel != null) {
			_inputChannel.removeListener(_inputListener);
		}
		super.internalDetach();
	}

	/**
	 * Command that enters edit mode.
	 */
	public static class EditCommand extends ControlCommand {

		/** Command identifier. */
		private static final String COMMAND_ID = "formEdit";

		/** Singleton instance. */
		public static final EditCommand INSTANCE = new EditCommand();

		private EditCommand() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_EDIT;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).enterEditMode();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command that applies overlay changes without leaving edit mode.
	 */
	public static class ApplyCommand extends ControlCommand {

		/** Command identifier. */
		private static final String COMMAND_ID = "formApply";

		/** Singleton instance. */
		public static final ApplyCommand INSTANCE = new ApplyCommand();

		private ApplyCommand() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_APPLY;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeApply();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command that saves changes (applies and exits edit mode).
	 */
	public static class SaveCommand extends ControlCommand {

		/** Command identifier. */
		private static final String COMMAND_ID = "formSave";

		/** Singleton instance. */
		public static final SaveCommand INSTANCE = new SaveCommand();

		private SaveCommand() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_SAVE;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeSave();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command that cancels editing, discarding changes.
	 */
	public static class CancelCommand extends ControlCommand {

		/** Command identifier. */
		private static final String COMMAND_ID = "formCancel";

		/** Singleton instance. */
		public static final CancelCommand INSTANCE = new CancelCommand();

		private CancelCommand() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_CANCEL;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {
			((FormControl) control).executeCancel();
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
