/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.provider.MetaLabelProvider;
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
public class FormControl extends ReactControl implements FormModel, ModelListener, StateHandler {

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

	private boolean _autoEditMode;

	private final LockHandler _lockHandler;

	private ViewChannel _inputChannel;

	private ViewChannel _editModeChannel;

	private ViewChannel _dirtyChannel;

	private FormValidationModel _validationModel;

	private ConstraintValidationListener _validityListener;

	private final List<FormModelListener> _formModelListeners = new ArrayList<>();

	private final List<FormParticipant> _participants = new ArrayList<>();

	private final List<FieldChangeListener> _fieldChangeListeners = new ArrayList<>();

	private final ViewChannel.ChannelListener _inputListener = this::handleInputChanged;

	private final ViewChannel.ChannelListener _editModeListener = this::handleEditModeChannelChanged;

	private VetoListener _inputVeto;

	private DirtyChannel _scopeDirtyChannel;

	/**
	 * Guard flag to prevent re-entrant loops when publishing to and reacting from the edit mode
	 * channel.
	 */
	private boolean _updatingEditMode;

	private final String _noModelMessage;

	private ModelScope _modelScope;

	/**
	 * The object this control is registered for as {@link ModelListener} in {@link #_modelScope},
	 * {@code null} if not registered.
	 */
	private TLObject _observedObject;

	/** Whether this control was {@link #detach() detached} and has not been attached again. */
	private boolean _suspended;

	private ViewExecutabilityRule _editRule = ViewExecutabilityRule.ALWAYS_EXECUTABLE;

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
		showEditMode(false);
		putState(DIRTY, Boolean.FALSE);
		updateNoModelMessage();
		FormLayoutEditModeBinding.bind(this, readOnly -> putState(ReactFormLayoutControl.READ_ONLY, readOnly), this);
	}

	/**
	 * Tells the client whether the form is being edited.
	 *
	 * <p>
	 * Outside edit mode the form's grid is {@link ReactFormLayoutControl#READ_ONLY read-only}: its
	 * fields show their values only, without the required marker, help and messages that belong to
	 * editing.
	 * </p>
	 */
	private void showEditMode(boolean editMode) {
		putState(EDIT_MODE, Boolean.valueOf(editMode));
		putState(ReactFormLayoutControl.READ_ONLY, Boolean.valueOf(!editMode));
	}

	/**
	 * Lays the fields of this form out in the given grid.
	 *
	 * @param maxColumns
	 *        The greatest number of columns the fields are distributed over, written as
	 *        {@link ReactFormLayoutControl#MAX_COLUMNS}. How many of them are actually filled
	 *        follows the available width.
	 * @param labelPosition
	 *        Where the fields render their labels relative to their inputs, written as
	 *        {@link ReactFormLayoutControl#LABEL_POSITION}.
	 */
	public void setLayout(int maxColumns, LabelPosition labelPosition) {
		putState(ReactFormLayoutControl.MAX_COLUMNS, Integer.valueOf(maxColumns));
		putState(ReactFormLayoutControl.LABEL_POSITION, labelPosition.getExternalName());
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

	/**
	 * Sets the rule deciding whether this form offers editing its object, evaluated against the
	 * displayed object.
	 *
	 * @param rule
	 *        The rule, {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} to offer editing to everyone
	 *        who sees the form.
	 *
	 * @see #editPermission()
	 */
	public void setEditRule(ViewExecutabilityRule rule) {
		_editRule = rule;
		fireFormStateChanged();
	}

	/**
	 * Whether the current user may edit the displayed object here.
	 *
	 * <p>
	 * The permission alone, independent of the form's lifecycle state: a form already in edit mode
	 * still reports the permission that got it there. The Edit command combines this with its state
	 * condition, and {@link #enterEditMode()} refuses a transition the permission denies — so the same
	 * decision governs the button, a command a client sends directly, the initial edit mode, an object
	 * switch of an auto-edit form, and the edit-mode channel.
	 * </p>
	 *
	 * <p>
	 * The permission combines the {@link #setEditRule(ViewExecutabilityRule) configured rule} with
	 * the model right to write the displayed object, see
	 * {@link ModelAccessPolicy#onEdit(TLObject)}: editing is offered only where both allow it. Of two
	 * refusals, the stronger one wins (a hidden command beats a disabled one, see
	 * {@link ExecutableState#combine(ExecutableState)}); of two equally strong refusals, the one of
	 * the model right gives the reason. A transient draft (e.g. of a create dialog) is not refused
	 * by the model right: its creation was checked when it was created.
	 * </p>
	 */
	public ExecutableState editPermission() {
		if (_currentObject == null || !_currentObject.tValid()) {
			// Nothing to edit, and a deleted object has no rights to ask for.
			return _editRule.isExecutable(getCurrentObject());
		}
		ExecutableState right = ModelAccessPolicy.onEdit(_currentObject);
		return right.combine(_editRule.isExecutable(getCurrentObject()));
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
	 * Registers a {@link FormParticipant} to participate in the form's editing lifecycle.
	 *
	 * @param participant
	 *        The participant to register.
	 */
	public void registerParticipant(FormParticipant participant) {
		if (!_participants.contains(participant)) {
			_participants.add(participant);
		}
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
	 * Listener notified when the value of a field in this form changes.
	 *
	 * <p>
	 * Used by option-based fields whose options depend on other fields, so that they can recompute
	 * their options when a dependency changes.
	 * </p>
	 */
	public interface FieldChangeListener {

		/**
		 * Called after a field value changed.
		 *
		 * @param part
		 *        The attribute whose field changed.
		 */
		void onFieldChanged(TLStructuredTypePart part);
	}

	/**
	 * Registers a {@link FieldChangeListener}.
	 */
	public void addFieldChangeListener(FieldChangeListener listener) {
		_fieldChangeListeners.add(listener);
	}

	/**
	 * Unregisters a {@link FieldChangeListener}.
	 */
	public void removeFieldChangeListener(FieldChangeListener listener) {
		_fieldChangeListeners.remove(listener);
	}

	/**
	 * Notifies all {@link FieldChangeListener}s that the field for the given attribute changed.
	 *
	 * @param part
	 *        The attribute whose field value changed.
	 */
	public void notifyFieldChanged(TLStructuredTypePart part) {
		if (_fieldChangeListeners.isEmpty()) {
			return;
		}
		for (FieldChangeListener listener : new ArrayList<>(_fieldChangeListeners)) {
			listener.onFieldChanged(part);
		}
	}

	/**
	 * Makes hidden validation errors visible on all registered participants.
	 */
	public void revealAllValidation() {
		for (FormParticipant participant : _participants) {
			participant.revealAll();
		}
		fireValidityChanged();
	}

	/**
	 * Whether any participant reports a validation error that is visible to the user.
	 *
	 * <p>
	 * Unlike {@link #hasErrors()}, an error that is still hidden (not yet
	 * {@link FormParticipant#revealAll() revealed}, because the user has neither touched the field
	 * nor attempted to save) does not count: a command must stay available as long as the user
	 * cannot see what is wrong.
	 * </p>
	 */
	public boolean hasVisibleErrors() {
		for (FormParticipant participant : _participants) {
			if (!participant.validate()) {
				return true;
			}
		}
		return false;
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
	 * <li>If the channel becomes {@code true} and the form is not in edit mode, it
	 * {@link #enterEditMode() enters edit mode}; if that transition is refused, the form resets the
	 * channel to {@code false}.</li>
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
	 * Sets the {@link ModelScope} this control should observe for changes to its current object.
	 *
	 * <p>
	 * Listener registration happens via {@link #onAttach()}/{@link #onDetach()} hooks, so the
	 * listener is active only while this control is displayed.
	 * </p>
	 *
	 * @param scope
	 *        The model scope to observe.
	 */
	public void setModelScope(ModelScope scope) {
		if (_modelScope == scope) {
			return;
		}
		deregisterModelListener();
		_modelScope = scope;
		registerModelListener();
	}

	/**
	 * Starts observing the current object, and catches up with the changes it missed when this
	 * control resumes.
	 *
	 * <p>
	 * A control that is displayed again after a {@link #detach()} (a hidden sidebar section or tab
	 * keeps its content for re-use) did not observe its object while it was hidden. What it displays
	 * is therefore treated as unknown and the control reacts as to a change of its object, see
	 * {@link #catchUp()}. The first attach is silent: the fields were just built from the object.
	 * This is the resume behavior of {@link RowSourceObserver} for element lists.
	 * </p>
	 */
	@Override
	protected void onAttach() {
		registerModelListener();
		if (_suspended) {
			_suspended = false;
			catchUp();
		}
	}

	/**
	 * Stops observing the current object while this control is not displayed.
	 *
	 * @see #onAttach()
	 */
	@Override
	protected void onDetach() {
		deregisterModelListener();
		_suspended = true;
	}

	/**
	 * Registers this control as {@link ModelListener} for its current object, if displayed and not
	 * yet registered.
	 *
	 * <p>
	 * A transient object is not observed: its changes are not reported by a {@link ModelScope}.
	 * </p>
	 */
	private void registerModelListener() {
		if (!isAttached() || _observedObject != null || _modelScope == null || _currentObject == null
			|| _currentObject.tTransient()) {
			return;
		}
		_modelScope.addModelListener(_currentObject, this);
		_observedObject = _currentObject;
	}

	/**
	 * Removes the registration made by {@link #registerModelListener()}, if any.
	 */
	private void deregisterModelListener() {
		if (_observedObject == null) {
			return;
		}
		_modelScope.removeModelListener(_observedObject, this);
		_observedObject = null;
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		if (_currentObject == null) {
			return;
		}
		ModelChangeEvent.ChangeType change = event.getChange(_currentObject);
		if (change == ModelChangeEvent.ChangeType.DELETED) {
			onCurrentObjectChanged(true);
		} else if (change == ModelChangeEvent.ChangeType.UPDATED) {
			onCurrentObjectChanged(false);
		}
	}

	/**
	 * Reacts to changes of the current object that happened while this control was not displayed.
	 *
	 * <p>
	 * Which changes happened is unknown, so the current object is treated as changed whenever it is
	 * one a {@link ModelScope} reports changes of: the values shown may stem from the object as well
	 * as from objects associated with it, so no property of the object itself tells whether the
	 * display is still current. A persistent object that is no longer {@link TLObject#tValid()
	 * valid} was deleted meanwhile. A transient object is not observed while displayed either (see
	 * {@link #registerModelListener()}), so there is nothing to catch up with.
	 * </p>
	 */
	private void catchUp() {
		if (_currentObject == null || _currentObject.tTransient()) {
			return;
		}
		onCurrentObjectChanged(!_currentObject.tValid());
	}

	/**
	 * Updates the display after the current object may have changed.
	 *
	 * @param deleted
	 *        Whether the current object was deleted.
	 */
	private void onCurrentObjectChanged(boolean deleted) {
		if (deleted) {
			onCurrentObjectDeleted();
		} else if (_editMode) {
			followStoredChanges();
		} else {
			fireFormStateChanged();
		}
	}

	/**
	 * Makes the edit session show a change stored to the edited object by someone else than this
	 * form.
	 *
	 * <p>
	 * The participants show the stored values wherever the user has not changed anything, see
	 * {@link FormParticipant#onObjectChanged()}. The user's changes stay in the overlay and are
	 * written by the next save.
	 * </p>
	 */
	private void followStoredChanges() {
		for (FormParticipant participant : new ArrayList<>(_participants)) {
			participant.onObjectChanged();
		}
		updateDirtyState();
		fireValidityChanged();
	}

	private void onCurrentObjectDeleted() {
		if (_editMode) {
			discardEditSession();
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
	 * Makes the form enter edit mode whenever an object becomes available.
	 *
	 * <p>
	 * Set for forms configured with {@code initial-edit-mode} (and no edit-mode channel): such a
	 * form is editable not only for its first object, but also after its input channel switches to
	 * another object (e.g. a new-entry form whose channel is re-filled with a fresh transient
	 * object after each submit). Each entry is subject to the check of {@link #enterEditMode()}, so an
	 * object the {@link #editPermission() edit permission} denies is displayed in view mode.
	 * </p>
	 *
	 * @param autoEditMode
	 *        Whether to re-enter edit mode on every object switch.
	 */
	public void setAutoEditMode(boolean autoEditMode) {
		_autoEditMode = autoEditMode;
	}

	/**
	 * Enters edit mode by acquiring a lock, creating an overlay, and notifying listeners.
	 *
	 * <p>
	 * Every transition into edit mode goes through this method, so it is the single place that checks
	 * the {@link #editPermission() edit permission}. It refuses the transition when the form displays
	 * no object, is already in edit mode, or the permission is denied for the displayed object. A
	 * refused transition acquires no lock and creates no overlay; instead, the form writes its actual
	 * mode to the {@link #setEditModeChannel(ViewChannel) edit-mode channel}, so that the channel
	 * always mirrors the form's mode — a channel set to {@code true} while the transition is refused
	 * is reset to {@code false}.
	 * </p>
	 *
	 * <p>
	 * The write-back is {@link ChannelNotificationScope#afterNotification(Runnable) deferred} until
	 * the channel notification in progress has completed (it runs immediately outside any
	 * notification). Writing from inside the notification would let the channel's remaining
	 * listeners receive the outer, outdated value after the reset, so that a listener following the
	 * reported values would end up with the refused mode.
	 * </p>
	 *
	 * @return Whether this call started an edit session.
	 */
	public boolean enterEditMode() {
		if (_editMode || _currentObject == null || !editPermission().isExecutable()) {
			ChannelNotificationScope.current().afterNotification(this::updateEditModeChannel);
			return false;
		}

		// Acquire lock first -- if this fails, no overlay is created.
		_lockHandler.acquireLock(_currentObject);

		_editMode = true;
		showEditMode(true);
		updateEditModeChannel();

		if (_inputChannel != null && _inputVeto == null) {
			// The form blocks any object switch while it holds unsaved changes, independent of
			// which object would come next.
			_inputVeto = new VetoListener() {
				@Override
				public List<StateHandler> checkVeto(ViewChannel sender, Object oldValue, Object newValue) {
					return checkDirty(sender);
				}

				@Override
				public List<StateHandler> checkDirty(ViewChannel sender) {
					return isDirty() ? List.of(FormControl.this) : List.of();
				}
			};
			_inputChannel.addVetoListener(_inputVeto);
		}

		setupEditSession();
		return true;
	}

	/**
	 * Applies overlay changes to the knowledge base without leaving edit mode.
	 *
	 * <p>
	 * {@link #executeStoreState() Stores} the changes, then sets up a fresh edit session (new overlay,
	 * new validation model).
	 * Participants re-register via {@link FormModelListener#onFormStateChanged(FormModel)}.
	 * </p>
	 */
	public void executeApply() {
		if (!_editMode || _overlay == null || (!_overlay.isDirty() && !hasParticipantChanges())) {
			return;
		}

		executeStoreState();

		setupEditSession();
	}

	/**
	 * Validates the form and stores all its changes.
	 *
	 * <p>
	 * This is the single path storing form state: {@link #executeApply()}, {@link #executeSave()}
	 * and the {@link com.top_logic.layout.view.command.StoreFormStateAction} all go through this
	 * method. It runs in this order:
	 * </p>
	 * <ol>
	 * <li>{@link #validateOrThrow() Validates} all participants.</li>
	 * <li>{@link #checkWriteRights() Checks the write rights} of all changes.</li>
	 * <li>For a persistent base object, opens a KB transaction and lets every participant
	 * {@link FormParticipant#persist(Transaction) persist} its KB-specific changes (e.g. a composition
	 * table creates its new rows and writes the persisted row list into the overlay).</li>
	 * <li>Lets every participant {@link FormParticipant#applyState() apply} its state.</li>
	 * <li>Applies the overlay to the base object, and commits the transaction.</li>
	 * </ol>
	 *
	 * <p>
	 * KB transactions nest: when called within an open transaction (e.g. from an action inside a
	 * {@link com.top_logic.layout.view.command.WithTransactionAction}), the transaction of this
	 * method joins the outer one and the outer transaction decides whether the changes are
	 * committed. Without an outer transaction, the changes are committed by this method.
	 * </p>
	 *
	 * <p>
	 * For a transient base object (e.g. in a create dialog), no transaction is opened: all changes
	 * are applied to the transient object and its transient rows, which become persistent together
	 * when the object is made persistent.
	 * </p>
	 *
	 * @return The base object with overlay changes applied, or {@code null} if no overlay exists.
	 * @throws TopLogicException
	 *         If any participant reports a validation error, or if the current user is not allowed
	 *         to write one of the changes, see {@link #checkWriteRights()}. In that case nothing is
	 *         stored.
	 */
	public TLObject executeStoreState() {
		validateOrThrow();

		if (_overlay == null) {
			return null;
		}

		checkWriteRights();

		TLObject base = _overlay.getBase();
		if (base.tTransient()) {
			applyState();
			return base;
		}

		Transaction tx = base.tKnowledgeBase().beginTransaction(
			I18NConstants.UPDATED__MODEL.fill(MetaLabelProvider.INSTANCE.getLabel(base)));
		try {
			for (FormParticipant participant : _participants) {
				participant.persist(tx);
			}
			applyState();
			tx.commit();
		} finally {
			tx.rollback();
		}
		return base;
	}

	/**
	 * Transfers the state of all participants and of the overlay to the base objects.
	 */
	private void applyState() {
		for (FormParticipant participant : _participants) {
			participant.applyState();
		}
		_overlay.apply();
	}

	/**
	 * Saves changes (applies and exits edit mode).
	 */
	@Override
	public void executeSave() {
		if (!_editMode || _overlay == null) {
			return;
		}

		if (_overlay.isDirty() || hasParticipantChanges()) {
			executeStoreState();
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
	 * Validates all participants and throws if any are invalid.
	 *
	 * <p>
	 * Re-runs all constraint checks first, since stored results can be outdated when persistent
	 * data has changed after the value was entered (e.g. a uniqueness conflict introduced by
	 * another commit). Then reveals all hidden validation errors (so model-level errors become
	 * visible via {@code hasError()}) and iterates all participants without short-circuiting.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If any participant reports a validation error.
	 */
	public void validateOrThrow() {
		if (_validationModel != null) {
			_validationModel.revalidateAll();
		}
		revealAllValidation();

		boolean valid = true;
		for (FormParticipant participant : _participants) {
			if (!participant.validate()) {
				valid = false;
			}
		}
		if (!valid) {
			throw new TopLogicException(
				com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
		}
	}

	/**
	 * Starts a fresh edit session after overlay edits have been applied to the base object, so the
	 * form reports a clean state relative to the updated base.
	 *
	 * <p>
	 * Called after {@link #executeStoreState()} when the form stays alive (e.g. a new-entry form
	 * that is re-used for the next entry): without a fresh session, field models would still
	 * compare against their original default values and report unsaved changes that are in fact
	 * already stored.
	 * </p>
	 */
	public void refreshEditSession() {
		if (!_editMode) {
			return;
		}
		setupEditSession();
	}

	/**
	 * Sets up a fresh edit session: creates a new overlay and validation model, clears participants
	 * (they re-register via {@link #fireFormStateChanged()}), and fires state changed.
	 */
	private void setupEditSession() {
		// Clean up old validation model before replacing it.
		if (_validationModel != null && _validityListener != null) {
			_validationModel.removeConstraintValidationListener(_validityListener);
		}

		_participants.clear();

		_overlay = new TLObjectOverlay(_currentObject);

		// Participants announce a changed validity themselves, once they have applied the new
		// result to their field models - announcing it from here would report the state as seen
		// before the participants updated.
		_validityListener = (overlay, attribute, result) -> {
			putState(VALID, Boolean.valueOf(_validationModel.isValid()));
		};
		_validationModel = new FormValidationModel();
		_validationModel.addOverlay(_overlay, _currentObject);
		_validationModel.addConstraintValidationListener(_validityListener);
		putState(VALID, Boolean.valueOf(_validationModel.isValid()));

		updateDirtyState();
		fireFormStateChanged();
	}

	/**
	 * Recalculates the form-level dirty state.
	 *
	 * <p>
	 * Called by field controls when a value changes so the form's overall dirty state is updated.
	 * </p>
	 */
	public void updateDirtyState() {
		boolean dirty = hasUnsavedChanges();
		putState(DIRTY, Boolean.valueOf(dirty));
		if (_dirtyChannel != null) {
			_dirtyChannel.set(Boolean.valueOf(dirty));
		}
		if (_scopeDirtyChannel != null) {
			_scopeDirtyChannel.updateState(this, dirty);
		}
	}

	// -- StateHandler --

	@Override
	public boolean isDirty() {
		return hasUnsavedChanges();
	}

	/**
	 * Whether the form holds changes that can still be saved.
	 *
	 * <p>
	 * An object that is no longer {@link TLObject#tValid() valid} is gone, and the edits made to it
	 * cannot be kept. Such a form holds nothing to protect: it reports itself clean, so that it
	 * neither publishes a dirty state nor blocks the object switch that replaces the object it
	 * displays.
	 * </p>
	 */
	private boolean hasUnsavedChanges() {
		if (!_editMode || _overlay == null) {
			return false;
		}
		if (_currentObject != null && !_currentObject.tValid()) {
			return false;
		}
		return _overlay.isDirty() || hasParticipantChanges();
	}

	@Override
	public boolean hasErrors() {
		return _validationModel != null && !_validationModel.isValid();
	}

	@Override
	public void executeDiscard() {
		executeCancel();
	}

	@Override
	public String getDescription() {
		if (_currentObject != null) {
			return MetaLabelProvider.INSTANCE.getLabel(_currentObject);
		}
		return "Form";
	}

	/**
	 * Sets the scope-level {@link DirtyChannel} that this form publishes its dirty state to.
	 *
	 * @param dirtyChannel
	 *        The dirty channel of the enclosing scope (e.g. tab).
	 */
	public void setScopeDirtyChannel(DirtyChannel dirtyChannel) {
		_scopeDirtyChannel = dirtyChannel;
	}

	/**
	 * Ensures that the current user may write all changes of this form: those of every
	 * {@link FormParticipant} and those of the form's own overlay.
	 *
	 * <p>
	 * Runs before anything is persisted or applied, so a refused save leaves all buffered changes in
	 * place and the user can correct or cancel the edit.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the current user is not allowed to write one of the changes.
	 */
	private void checkWriteRights() {
		for (FormParticipant participant : _participants) {
			participant.checkApplyState();
		}
		if (_overlay != null) {
			_overlay.checkApply();
		}
	}

	/**
	 * Ends the edit session and announces the resulting view-mode state.
	 *
	 * <p>
	 * Used where the form keeps displaying the same object ({@link #executeSave()},
	 * {@link #executeCancel()}): the fields must drop the overlay values and show the base values
	 * again, which the notification triggers.
	 * </p>
	 */
	private void exitEditMode() {
		discardEditSession();

		fireFormStateChanged();
	}

	/**
	 * Ends the edit session without notifying the {@link FormModelListener}s.
	 *
	 * <p>
	 * Used where the form stops displaying its current object ({@link #handleInputChanged},
	 * {@link #onCurrentObjectDeleted()}) or stops displaying anything at all
	 * ({@link #onCleanup()}). Such a caller switches the object and then fires once, so that no
	 * field is rebound to the object the form is leaving: rebinding computes field state for that
	 * object (options, constraints, validation) although the result is thrown away by the
	 * notification for the new object — and for a deleted object the computation fails.
	 * </p>
	 */
	private void discardEditSession() {
		if (_inputVeto != null && _inputChannel != null) {
			_inputChannel.removeVetoListener(_inputVeto);
			_inputVeto = null;
		}
		_overlay = null;
		_editMode = false;

		releaseLock();

		showEditMode(false);
		updateEditModeChannel();
		updateDirtyState();

		if (_validationModel != null && _validityListener != null) {
			_validationModel.removeConstraintValidationListener(_validityListener);
			_validityListener = null;
		}
		_validationModel = null;
		_participants.clear();
		putState(VALID, Boolean.TRUE);
	}

	private void releaseLock() {
		_lockHandler.releaseLock();
	}

	private void fireFormStateChanged() {
		// A listener may deregister while being notified, e.g. a field grid disposed by the change.
		for (FormModelListener listener : new ArrayList<>(_formModelListeners)) {
			listener.onFormStateChanged(this);
		}
		// The participants have rebuilt themselves, so what the user sees may differ from before.
		// The second pass reaches every listener with the settled state, independent of the order
		// in which the participants were notified above.
		fireValidityChanged();
	}

	/**
	 * Announces that the validation errors visible to the user may have changed.
	 *
	 * <p>
	 * Called by participants whose displayed validation state changed, so that commands gated on
	 * {@link #hasVisibleErrors()} re-evaluate their executability.
	 * </p>
	 */
	public void fireValidityChanged() {
		for (FormModelListener listener : new ArrayList<>(_formModelListeners)) {
			listener.onValidityChanged(this);
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
			discardEditSession();
		}
		deregisterModelListener();
		_currentObject = (TLObject) newValue;
		registerModelListener();
		updateNoModelMessage();

		fireFormStateChanged();

		if (_autoEditMode) {
			// The form is configured to be editable whenever an object is available, so the
			// object switch re-enters edit mode for the new object, if its edit permission allows.
			enterEditMode();
		}
	}

	@Override
	protected void onCleanup() {
		if (_scopeDirtyChannel != null) {
			_scopeDirtyChannel.removeHandler(this);
		}
		if (_editMode) {
			discardEditSession();
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
	 *
	 * <p>
	 * Refused when {@link #enterEditMode()} refuses the transition, i.e. unless the form displays an
	 * object, is not already in edit mode, and the user has the {@link #editPermission() permission
	 * to edit it} — the condition under which {@link FormCommandModel#editCommand(FormControl) the
	 * Edit command} is executable. The lifecycle commands are dispatched to this control directly, so
	 * the check in {@link #enterEditMode()} applies to them instead of the toolbar button's
	 * executability.
	 * </p>
	 */
	@ReactCommandHandler("formEdit")
	HandlerResult handleEdit() {
		if (_currentObject == null) {
			return HandlerResult.notExecutable(ExecutableState.NO_EXEC_NO_MODEL);
		}
		if (_editMode) {
			return notExecutable();
		}
		ExecutableState permission = editPermission();
		if (!permission.isExecutable()) {
			return HandlerResult.notExecutable(permission);
		}
		if (!enterEditMode()) {
			return notExecutable();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Command that applies overlay changes without leaving edit mode.
	 *
	 * <p>
	 * Refused outside an edit session, see {@link #handleEdit()}.
	 * </p>
	 */
	@ReactCommandHandler("formApply")
	HandlerResult handleApply() {
		if (!_editMode) {
			return notExecutable();
		}
		executeApply();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Command that saves changes (applies and exits edit mode).
	 *
	 * <p>
	 * Refused outside an edit session, see {@link #handleEdit()}.
	 * </p>
	 */
	@ReactCommandHandler("formSave")
	HandlerResult handleSave() {
		if (!_editMode) {
			return notExecutable();
		}
		executeSave();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Command that cancels editing, discarding changes.
	 *
	 * <p>
	 * Refused outside an edit session, see {@link #handleEdit()}.
	 * </p>
	 */
	@ReactCommandHandler("formCancel")
	HandlerResult handleCancel() {
		if (!_editMode) {
			return notExecutable();
		}
		executeCancel();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The refusal of a lifecycle command the form's state does not offer.
	 */
	private static HandlerResult notExecutable() {
		return HandlerResult.notExecutable(ExecutableState.NOT_EXEC_DISABLED);
	}
}
