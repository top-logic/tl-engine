/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.DynamicVisibility;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Fields of the object a single-valued reference of a form's object points to, edited together
 * with the form's object.
 *
 * <p>
 * The control is the {@link FormModel} of the fields nested into it: they display the
 * {@link #getReferencedObject() referenced object} and, while the form is edited, a
 * {@link TLObjectOverlay} of it. The overlay belongs to the edit session of the enclosing
 * {@link FormControl}: it is registered in the form's {@link FormValidationModel} (so that
 * constraints of either object see the values entered for the other one), and this control takes
 * part in the form's editing lifecycle as {@link FormParticipant}. Dirty state, validation, the
 * write-rights check, cancel and save are therefore those of the form; a save stores the changes of
 * the referenced object in the transaction storing the form's object.
 * </p>
 *
 * <p>
 * The reference may be a forward or a backward reference. The control follows the display mode of
 * the reference on the form's object: a reference that is hidden (statically, on creation, by a
 * dynamic visibility, or because the user may not read it) displays nothing, a reference that is
 * read-only by its visibility displays the referenced object without editing it. That the
 * reference itself cannot be changed (a backward reference, or one the user may not write) does not
 * make the referenced object read-only: the control does not replace the referenced object, it
 * edits it, and the rights on its attributes are checked by its fields.
 * </p>
 *
 * <p>
 * Without a referenced object, the control is hidden. It follows the referenced object: when the
 * form displays another object, or when the reference points to another object after a change
 * stored outside the form (e.g. a command creating or deleting the referenced object), the fields
 * are bound to the new object. The fields are created once and rebind themselves, so no subtree is
 * disposed while a notification is running.
 * </p>
 */
public class ObjectFieldsControl extends ReactFormGroupControl
		implements FormModel, FormModelListener, FormParticipant, ModelListener {

	private final FormModel _parent;

	private final FormControl _form;

	private final String _attribute;

	private final boolean _readOnly;

	private final ModelScope _modelScope;

	private final List<FormModelListener> _listeners = new ArrayList<>();

	private final FormControl.FieldChangeListener _fieldListener = this::onFieldChanged;

	private boolean _fieldListenerRegistered;

	/** The attributes the dynamic mode of the reference was last computed from. */
	private Set<TLStructuredTypePart> _modeDependencies = Collections.emptySet();

	/** The reference on the type of the form's current object, {@code null} if it has none. */
	private TLStructuredTypePart _reference;

	/** The object the reference points to, {@code null} for none. */
	private TLObject _base;

	/** Whether the {@link #_base referenced object} is edited. */
	private boolean _editMode;

	/** The overlay editing {@link #_base}, {@code null} outside editing. */
	private TLObjectOverlay _overlay;

	/** The validation model of the edit session {@link #_overlay} is registered in. */
	private FormValidationModel _session;

	private TLStructuredType _observedType;

	private Registration _modelRegistration = Registration.NONE;

	private boolean _disposed;

	/**
	 * Creates an {@link ObjectFieldsControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param parent
	 *        The model of the form (or of the enclosing {@link ObjectFieldsControl}) whose object
	 *        holds the reference.
	 * @param attribute
	 *        The name of the single-valued reference on the object of the parent model.
	 * @param readOnly
	 *        Whether the referenced object is displayed without being edited, also while the form is
	 *        edited.
	 * @param modelScope
	 *        The scope reporting changes stored outside the form, {@code null} to not observe
	 *        changes.
	 */
	public ObjectFieldsControl(ReactContext context, FormModel parent, String attribute, boolean readOnly,
			ModelScope modelScope) {
		super(context, null, false, false, GroupBorder.NONE, true, List.of(), List.of());
		_parent = parent;
		_form = parent.getFormControl();
		_attribute = attribute;
		_readOnly = readOnly;
		_modelScope = modelScope;

		_parent.addFormModelListener(this);
		addAttachListener(this::onDisplayed);
		addDetachListener(this::stopObserving);

		update();
	}

	/**
	 * Sets the controls displaying the fields of the referenced object.
	 *
	 * <p>
	 * The controls are created in a context whose form model is this control, and bind themselves
	 * to the current referenced object.
	 * </p>
	 */
	public void setContent(List<? extends ReactControl> children) {
		replaceChildren(children);
	}

	/**
	 * The object the reference points to, {@code null} if there is none or the reference is
	 * hidden.
	 */
	public TLObject getReferencedObject() {
		return _base;
	}

	@Override
	public TLObject getCurrentObject() {
		if (_editMode && _overlay != null) {
			return _overlay;
		}
		return _base;
	}

	@Override
	public boolean isEditMode() {
		return _editMode;
	}

	@Override
	public FormControl getFormControl() {
		return _form;
	}

	@Override
	public void addFormModelListener(FormModelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeFormModelListener(FormModelListener listener) {
		_listeners.remove(listener);
	}

	// -- Following the parent form --

	@Override
	public void onFormStateChanged(FormModel source) {
		if (_disposed) {
			return;
		}
		update();
	}

	@Override
	public void onValidityChanged(FormModel source) {
		if (_disposed) {
			return;
		}
		for (FormModelListener listener : new ArrayList<>(_listeners)) {
			listener.onValidityChanged(this);
		}
	}

	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (_disposed || changedPart == _reference) {
			return;
		}
		if (_modeDependencies.contains(changedPart)) {
			update();
		}
	}

	/**
	 * Binds this control to the object the reference currently points to, in the mode the parent
	 * form and the reference allow, and announces the result to the nested fields.
	 */
	private void update() {
		TLObject owner = _parent.getCurrentObject();
		TLStructuredTypePart reference = owner == null || !owner.tValid() ? null : owner.tType().getPart(_attribute);
		FormVisibility mode = reference == null ? FormVisibility.HIDDEN : mode(owner, reference);

		TLObject target = mode == FormVisibility.HIDDEN ? null : referenced(owner, reference);
		boolean editing = target != null && _parent.isEditMode() && !_readOnly && mode != FormVisibility.READ_ONLY;
		FormValidationModel session = editing ? _form.getValidationModel() : null;
		if (session == null) {
			editing = false;
		}

		if (target != _base || session != _session) {
			dropOverlay();
			_base = target;
			if (editing) {
				_overlay = new TLObjectOverlay(target);
				_session = session;
				session.addOverlay(_overlay, target);
			}
		}
		_editMode = editing;
		if (editing) {
			// The form forgets its participants with every new edit session.
			_form.registerParticipant(this);
		}

		_reference = reference;
		observe(reference);
		setHidden(target == null);

		for (FormModelListener listener : new ArrayList<>(_listeners)) {
			listener.onFormStateChanged(this);
		}
	}

	/**
	 * The display mode of the reference on the given object: {@link FormVisibility#HIDDEN},
	 * {@link FormVisibility#READ_ONLY}, or {@link FormVisibility#EDITABLE}.
	 */
	private FormVisibility mode(TLObject owner, TLStructuredTypePart reference) {
		if (!AttributeModeTracker.isDisplayable(owner, reference)) {
			trackModeDependencies(Collections.emptySet());
			return FormVisibility.HIDDEN;
		}

		FormVisibility dynamic = FormVisibility.DEFAULT;
		ModeSelector selector = DynamicVisibility.modeSelector(reference);
		Set<TLStructuredTypePart> dependencies = new HashSet<>();
		if (selector != null) {
			FormValidationModel overlays = _form.getValidationModel();
			dynamic = selector.getMode(owner, reference, _parent.isEditMode());
			selector.traceDependencies(owner, reference, _parent.isEditMode(),
				pointer -> dependencies.add(pointer.attribute()),
				overlays != null ? overlays : AttributeOptions.NO_OVERLAYS);
		}
		trackModeDependencies(dependencies);

		switch (dynamic) {
			case HIDDEN:
				return FormVisibility.HIDDEN;
			case READ_ONLY:
			case DISABLED:
				return FormVisibility.READ_ONLY;
			case EDITABLE:
			case MANDATORY:
				return FormVisibility.EDITABLE;
			case DEFAULT:
			default:
				Visibility visibility = AttributeModeTracker.isNewObject(owner)
					? DisplayAnnotations.getCreateVisibility(reference)
					: DisplayAnnotations.getVisibility(reference);
				switch (visibility) {
					case HIDDEN:
						return FormVisibility.HIDDEN;
					case READ_ONLY:
						return FormVisibility.READ_ONLY;
					default:
						return FormVisibility.EDITABLE;
				}
		}
	}

	private void trackModeDependencies(Set<TLStructuredTypePart> dependencies) {
		_modeDependencies = dependencies;
		boolean listen = !dependencies.isEmpty();
		if (listen && !_fieldListenerRegistered) {
			_form.addFieldChangeListener(_fieldListener);
			_fieldListenerRegistered = true;
		} else if (!listen && _fieldListenerRegistered) {
			_form.removeFieldChangeListener(_fieldListener);
			_fieldListenerRegistered = false;
		}
	}

	private static TLObject referenced(TLObject owner, TLStructuredTypePart reference) {
		Object value = owner.tValue(reference);
		if (value instanceof TLObject target && target.tValid()) {
			return target;
		}
		return null;
	}

	private void dropOverlay() {
		if (_overlay != null && _session != null && _session == _form.getValidationModel()) {
			_session.removeOverlay(_overlay);
		}
		_overlay = null;
		_session = null;
	}

	// -- Changes stored outside the form --

	private void onDisplayed() {
		if (_disposed) {
			return;
		}
		// What was stored while the control was not displayed is unknown.
		update();
	}

	/**
	 * Observes the objects of the type the reference points to, so that a referenced object
	 * created, changed or deleted outside the form is displayed.
	 */
	private void observe(TLStructuredTypePart reference) {
		TLStructuredType type = targetType(reference);
		if (type == _observedType && _modelRegistration.isActive()) {
			return;
		}
		stopObserving();
		if (type == null || _modelScope == null || !isAttached()) {
			return;
		}
		_observedType = type;
		_modelRegistration = _modelScope.addModelListener(type, this);
	}

	private static TLStructuredType targetType(TLStructuredTypePart reference) {
		if (!(reference instanceof TLReference)) {
			return null;
		}
		TLType type = reference.getType();
		return type instanceof TLStructuredType structured ? structured : null;
	}

	private void stopObserving() {
		_modelRegistration.dispose();
		_modelRegistration = Registration.NONE;
		_observedType = null;
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		if (_disposed) {
			return;
		}
		TLObject owner = _parent.getCurrentObject();
		TLObject target = _reference == null || owner == null || !owner.tValid() ? null : referenced(owner, _reference);
		if (target != _base) {
			update();
			return;
		}
		if (_base == null || event.getChange(_base) != ModelChangeEvent.ChangeType.UPDATED) {
			return;
		}
		if (_editMode) {
			// The fields show the stored values where the user has not changed anything.
			for (FormModelListener listener : new ArrayList<>(_listeners)) {
				if (listener instanceof FormParticipant participant) {
					participant.onObjectChanged();
				}
			}
			_form.updateDirtyState();
			_form.fireValidityChanged();
		} else {
			for (FormModelListener listener : new ArrayList<>(_listeners)) {
				listener.onFormStateChanged(this);
			}
		}
	}

	// -- FormParticipant --

	@Override
	public boolean validate() {
		// The fields of the referenced object validate themselves as participants of the form.
		return true;
	}

	@Override
	public void checkApplyState() {
		if (_overlay != null) {
			_overlay.checkApply();
		}
	}

	@Override
	public void persist(Transaction tx) {
		// Stored by applyState(), which also runs for a transient form object.
	}

	/**
	 * Stores the changes of the referenced object.
	 *
	 * <p>
	 * A new (transient) referenced object receives the changes and becomes persistent with the
	 * object that makes it so. A persistent one is stored through {@link BufferSave} in a
	 * transaction that joins the transaction storing the form's object.
	 * </p>
	 */
	@Override
	public void applyState() {
		if (_overlay == null || !_overlay.isDirty()) {
			return;
		}
		if (_base.tTransient()) {
			_overlay.apply();
			return;
		}
		Transaction tx = _base.tKnowledgeBase().beginTransaction(
			I18NConstants.UPDATED__MODEL.fill(MetaLabelProvider.INSTANCE.getLabel(_base)));
		try {
			BufferSave.save(_overlay);
			_overlay.reset();
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	@Override
	public void onObjectChanged() {
		// The form's object was stored by someone else: the reference may point elsewhere now.
		update();
	}

	@Override
	public void cancel() {
		if (_overlay != null) {
			_overlay.reset();
		}
	}

	@Override
	public void revealAll() {
		// The fields of the referenced object reveal their errors as participants of the form.
	}

	@Override
	public boolean isDirty() {
		return _editMode && _overlay != null && _base != null && _base.tValid() && _overlay.isDirty();
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		stopObserving();
		trackModeDependencies(Collections.emptySet());
		_parent.removeFormModelListener(this);
		_form.unregisterParticipant(this);
		dropOverlay();
		super.onCleanup();
	}

}
