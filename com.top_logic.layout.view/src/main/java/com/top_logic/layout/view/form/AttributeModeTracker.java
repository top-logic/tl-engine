/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.col.Sink;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Display mode of a model attribute of an edited object, for any display of the attribute: a field
 * of a {@link FormControl}, the cell of a row edited in a table.
 *
 * <p>
 * The mode of the attribute is decided by
 * </p>
 * <ul>
 * <li>a dynamic {@link ModeSelector} of the attribute (see {@link DynamicVisibility}), which takes
 * precedence over the static annotations,</li>
 * <li>otherwise its static visibility: the create visibility
 * ({@link DisplayAnnotations#getCreateVisibility(com.top_logic.model.TLModelPart)}) on a
 * {@link #isNewObject(TLObject) new object}, its visibility on any other,</li>
 * <li>the access rights of the current user (see
 * {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}): a value
 * the user may not read is not {@link #isDisplayable(TLObject, TLStructuredTypePart) displayable},
 * a value the user may not write is read-only while the object is edited.</li>
 * </ul>
 *
 * <p>
 * Between {@link #attach(TLStructuredTypePart)} and {@link #detach()}, the tracker observes the
 * {@link AttributeChanges changes} of the edited object - the fields of a form, the cells of a row
 * - and calls back its owner when an attribute changes that the last {@link #computeMode(TLObject,
 * boolean, boolean) computed} dynamic mode depends on, so that the owner recomputes and applies the
 * mode.
 * </p>
 */
public class AttributeModeTracker {

	/**
	 * The effective display mode of an attribute.
	 *
	 * @param visible
	 *        Whether the attribute is shown.
	 * @param editable
	 *        Whether the value can be edited right now: never outside editing.
	 * @param mandatory
	 *        Whether a value is required.
	 * @param disabled
	 *        Whether the value is presented as an inactive input: only while editing, for the mode
	 *        {@link FormVisibility#DISABLED}.
	 */
	public record AttributeMode(boolean visible, boolean editable, boolean mandatory, boolean disabled) {

		/**
		 * Applies the editability of this mode to the given field model: editable, or presented as
		 * an inactive input, or neither.
		 */
		public void applyTo(AbstractFieldModel model) {
			model.setDisabled(disabled());
			model.setEditable(editable());
		}
	}

	private final AttributeChanges _changes;

	private final Runnable _onModeDependencyChanged;

	private final FormControl.FieldChangeListener _fieldListener = this::onFieldChanged;

	/** Removes {@link #_fieldListener}, {@code null} while it is not registered. */
	private Runnable _unobserve;

	private TLStructuredTypePart _part;

	private ModeSelector _modeSelector;

	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	/**
	 * Creates an {@link AttributeModeTracker} for an attribute of the object of a form.
	 *
	 * @param formControl
	 *        The form whose fields the mode may depend on.
	 * @param onModeDependencyChanged
	 *        Called when a field changes that the dynamic mode of the attribute depends on.
	 */
	public AttributeModeTracker(FormControl formControl, Runnable onModeDependencyChanged) {
		this(AttributeChanges.of(formControl), onModeDependencyChanged);
	}

	/**
	 * Creates an {@link AttributeModeTracker}.
	 *
	 * @param changes
	 *        The edit of the object whose attributes the mode may depend on.
	 * @param onModeDependencyChanged
	 *        Called when an attribute changes that the dynamic mode of the attribute depends on.
	 */
	public AttributeModeTracker(AttributeChanges changes, Runnable onModeDependencyChanged) {
		_changes = changes;
		_onModeDependencyChanged = onModeDependencyChanged;
	}

	/**
	 * Starts tracking the mode of the given attribute.
	 *
	 * <p>
	 * May be called again to switch to another attribute (e.g. the same-named attribute of another
	 * object's type) without a {@link #detach()} in between.
	 * </p>
	 */
	public void attach(TLStructuredTypePart part) {
		_part = part;
		_modeSelector = DynamicVisibility.modeSelector(part);
		if (_modeSelector != null && _unobserve == null) {
			_unobserve = _changes.observe(_fieldListener);
		}
	}

	/**
	 * Stops tracking and releases the listener on the {@link AttributeChanges changes} of the
	 * object.
	 *
	 * <p>
	 * The changes are announced to a snapshot of the listeners, so a detached tracker may still be
	 * notified by a change in progress; it ignores such a notification.
	 * </p>
	 */
	public void detach() {
		if (_unobserve != null) {
			_unobserve.run();
			_unobserve = null;
		}
		_part = null;
		_modeSelector = null;
		_dependencies = Collections.emptySet();
	}

	/**
	 * The attribute tracked, {@code null} while detached.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * Computes the effective mode of the {@link #attach(TLStructuredTypePart) attached} attribute on
	 * the given object and records the dependencies of a dynamic {@link ModeSelector}.
	 *
	 * <p>
	 * While the attribute is edited, the right to write it caps the mode: a refused write makes an
	 * editable attribute read-only, whether the refusal depends on the object or not. A mode that
	 * does not let the attribute be edited anyway stays as it is.
	 * </p>
	 *
	 * @param object
	 *        The displayed object: the object of the form, the row of the table.
	 * @param editMode
	 *        Whether the object is edited.
	 * @param readOnly
	 *        Whether the caller presents the attribute read-only even while the object is edited.
	 * @return The effective mode.
	 */
	public AttributeMode computeMode(TLObject object, boolean editMode, boolean readOnly) {
		TLStructuredTypePart part = _part;
		boolean visible = true;
		boolean editable;
		boolean mandatory;
		boolean disabled = false;
		FormVisibility mode = FormVisibility.DEFAULT;
		if (_modeSelector != null) {
			Set<TLStructuredTypePart> dependencies = new HashSet<>();
			Sink<Pointer> sink = pointer -> dependencies.add(pointer.attribute());
			OverlayLookup overlays = _changes.overlays();
			mode = _modeSelector.getMode(object, part, editMode);
			_modeSelector.traceDependencies(object, part, editMode, sink,
				overlays != null ? overlays : AttributeOptions.NO_OVERLAYS);
			_dependencies = dependencies;
		}
		switch (mode) {
			case HIDDEN:
				visible = false;
				editable = false;
				mandatory = false;
				break;
			case READ_ONLY:
				editable = false;
				mandatory = false;
				break;
			case DISABLED:
				editable = false;
				mandatory = false;
				disabled = true;
				break;
			case EDITABLE:
				editable = true;
				mandatory = false;
				break;
			case MANDATORY:
				editable = true;
				mandatory = true;
				break;
			case DEFAULT:
			default:
				visible = !isStaticallyHidden(object, part);
				editable = isStaticallyEditable(object, part);
				mandatory = isNewObject(object) ? DisplayAnnotations.isMandatoryInCreate(part)
					: DisplayAnnotations.isMandatory(part);
				break;
		}
		boolean editing = editMode && !readOnly;
		if (editing && editable) {
			// The access rights cap the mode of the model: a value the user may not write on this
			// object is read-only.
			if (!isWritable(object, part)) {
				editable = false;
				mandatory = false;
			}
		}
		// A disabled attribute is presented as an inactive input only while editing; otherwise it
		// shows its value like any other.
		return new AttributeMode(visible, editing && editable, mandatory, editing && disabled);
	}

	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (_part == null || _modeSelector == null) {
			// Detached, notified from a snapshot of the listeners.
			return;
		}
		if (changedPart == _part) {
			return;
		}
		if (_dependencies.contains(changedPart)) {
			_onModeDependencyChanged.run();
		}
	}

	/**
	 * Whether the given attribute can be displayed on the given object at all, before a dynamic
	 * mode is considered: it is not hidden by its static visibility (its create visibility on a
	 * {@link #isNewObject(TLObject) new object}), and the current user may read its value.
	 */
	public static boolean isDisplayable(TLObject object, TLStructuredTypePart part) {
		return !isStaticallyHidden(object, part) && isReadable(object, part);
	}

	/**
	 * Whether the attribute is hidden by its static visibility on the given object: its create
	 * visibility on a {@link #isNewObject(TLObject) new object}.
	 */
	public static boolean isStaticallyHidden(TLObject object, TLStructuredTypePart part) {
		return isNewObject(object) ? DisplayAnnotations.isHiddenInCreate(part) : DisplayAnnotations.isHidden(part);
	}

	/**
	 * Whether the attribute can be edited by its static visibility on the given object: its create
	 * visibility on a {@link #isNewObject(TLObject) new object}. Without a visibility annotation, a
	 * derived attribute cannot.
	 */
	public static boolean isStaticallyEditable(TLObject object, TLStructuredTypePart part) {
		return isNewObject(object) ? DisplayAnnotations.isEditableInCreate(part) : DisplayAnnotations.isEditable(part);
	}

	/**
	 * Whether the current user may see the value of the given attribute of the given object.
	 */
	public static boolean isReadable(TLObject object, TLStructuredTypePart part) {
		return ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.READ, object, part).isExecutable();
	}

	/**
	 * Whether the current user may change the value of the given attribute of the given object.
	 */
	public static boolean isWritable(TLObject object, TLStructuredTypePart part) {
		return ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.WRITE, object, part).isExecutable();
	}

	/**
	 * Whether the given object is a new object that is not stored yet: a transient object, or an
	 * overlay (of overlays) of one. An overlay of a stored object edits that object.
	 */
	public static boolean isNewObject(TLObject object) {
		TLObject base = object;
		while (base instanceof TLObjectOverlay overlay) {
			base = overlay.getBase();
		}
		return base != null && base.tTransient();
	}

}
