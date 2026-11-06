/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracingScope;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} that reads and writes a single attribute of a
 * {@link TLObject}.
 *
 * <p>
 * The object may be a base persistent object (view mode) or a {@link TLObjectOverlay} (edit mode).
 * Dirty tracking is handled by the inherited {@link BoundFieldModel} logic which compares the
 * current value to the default value.
 * </p>
 *
 * <p>
 * A {@link TLStructuredTypePart#isDerived() derived} attribute follows the edits of the form it is
 * displayed in: while the form is edited, its value is computed in a {@link TracingScope} over the
 * form's overlays, and the attributes read by that computation are recorded. When the field of one of those attributes changes, the value is
 * computed again and {@link #followObject() taken} as the field's value and as the value it is
 * dirty against. A derived field is therefore never dirty, while its value change is announced to
 * its listeners like any other, so that fields depending on the derived attribute follow in turn.
 * </p>
 *
 * <p>
 * An editable field of an attribute with a {@link com.top_logic.model.fallback.StorageWithFallback}
 * holds the explicit value of the attribute only, see {@link FallbackPlaceholder}.
 * </p>
 *
 * <p>
 * An entered value of an attribute annotated with a {@link TLValueNormalizer} is brought into its
 * canonical form before it is written to the object. The normalizer sees the object the field
 * writes to, in a form the overlay holding the other values entered there.
 * </p>
 *
 * <p>
 * Bound to a {@link TLObjectOverlay}, the field shows a value written to its attribute of the
 * overlay by someone else, e.g. an {@link com.top_logic.model.annotate.AttributeValueListener}
 * reacting to the change of another field: the field takes the written value and announces its
 * change like an edit of the user (see {@link #refreshFromObject()}). The value the field writes
 * itself is announced once, by the field. When the attribute is written again in reaction to that
 * write, the field shows the value written last, after announcing its own.
 * </p>
 */
public class AttributeFieldModel extends BoundFieldModel {

	private TLObject _object;

	private TLStructuredTypePart _part;

	private final FormControl _form;

	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	private final FormControl.FieldChangeListener _dependencyListener = this::onFieldChanged;

	private boolean _listening;

	private final TLObjectOverlay.ValueChangeListener _overlayListener = this::onOverlayValueChanged;

	/**
	 * The overlay this model is registered at with {@link #_overlayListener}, {@code null} if not
	 * registered.
	 */
	private TLObjectOverlay _observedOverlay;

	/**
	 * Whether this model is writing to its object, so that the change announced by the overlay is
	 * the one this model announces itself.
	 */
	private boolean _writing;

	/**
	 * The value this model is {@link #_writing writing}.
	 */
	private Object _writtenValue;

	/**
	 * Whether the attribute was written again while this model was {@link #_writing writing} it,
	 * e.g. by an {@link com.top_logic.model.annotate.AttributeValueListener} reacting to the write.
	 */
	private boolean _rewritten;

	/**
	 * Creates a new model for the given attribute, outside of a form.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part) {
		this(object, part, null);
	}

	/**
	 * Creates a new model for the given attribute, displayed in the given form.
	 *
	 * @param object
	 *        The object to read/write values from.
	 * @param part
	 *        The attribute to bind to.
	 * @param form
	 *        The form the field is displayed in, providing the overlays a derived value is computed
	 *        over and the notifications about changed fields. {@code null} for a field outside of
	 *        a form.
	 */
	public AttributeFieldModel(TLObject object, TLStructuredTypePart part, FormControl form) {
		super(FallbackPlaceholder.fieldValue(object, part, true));
		_object = object;
		_part = part;
		_form = form;
		setNormalizer(ValueNormalizer.annotated(part));
		setMandatory(part.isMandatory());
		if (isTraced()) {
			Object value = readValue();
			setDefaultValue(value);
			setValueInternal(value);
		}
		updateDependencyListener();
		updateOverlayListener();
	}

	/**
	 * The value of the attribute, computed over the form's overlays for a derived attribute of a
	 * form being edited.
	 *
	 * <p>
	 * Reading a computed value records the attributes the computation reads, whose changes make
	 * the field compute its value again.
	 * </p>
	 */
	@Override
	protected Object readValue() {
		OverlayLookup overlays = _form == null ? null : _form.getValidationModel();
		if (!isTraced() || overlays == null) {
			_dependencies = Collections.emptySet();
			return readAttribute();
		}
		Set<TLStructuredTypePart> dependencies = new HashSet<>();
		Object value;
		try (TracingScope scope = TracingScope.open(pointer -> dependencies.add(pointer.attribute()), overlays)) {
			value = readAttribute();
		}
		_dependencies = dependencies;
		return value;
	}

	/**
	 * The value of the attribute the field displays, see
	 * {@link FallbackPlaceholder#fieldValue(TLObject, TLStructuredTypePart, boolean)}.
	 */
	private Object readAttribute() {
		return FallbackPlaceholder.fieldValue(_object, _part, isEditable());
	}

	/**
	 * Makes the field take what it displays in its new editability as its value, since the
	 * displayed value of an attribute with a fallback depends on it, see
	 * {@link FallbackPlaceholder#fieldValue(TLObject, TLStructuredTypePart, boolean)}.
	 */
	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		followEditability();
	}

	/**
	 * Makes the field take what it displays in its new editability as its value, see
	 * {@link #setEditable(boolean)}.
	 */
	@Override
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);
		followEditability();
	}

	/**
	 * Takes the displayed value of an attribute with a fallback as the value of the field and the
	 * value it is dirty against, unless the user has changed the field.
	 *
	 * <p>
	 * The control has already been sent the displayed value on the editability change, so no value
	 * change is fired.
	 * </p>
	 */
	private void followEditability() {
		if (FallbackPlaceholder.storage(_part) == null || isObjectDeleted() || isDirty()) {
			return;
		}
		Object value = readValue();
		setDefaultValue(value);
		setValueInternal(value);
	}

	/**
	 * Whether the value of the attribute is computed and the attributes the computation reads are
	 * traced, which is the case for a {@link TLStructuredTypePart#isDerived() derived} attribute.
	 */
	private boolean isTraced() {
		return _part.isDerived();
	}

	/**
	 * Registers for the changes of the form's fields while the attribute is
	 * {@link #isTraced() traced}, and deregisters otherwise.
	 */
	private void updateDependencyListener() {
		boolean listen = _form != null && isTraced();
		if (listen == _listening) {
			return;
		}
		if (listen) {
			_form.addFieldChangeListener(_dependencyListener);
		} else {
			_form.removeFieldChangeListener(_dependencyListener);
		}
		_listening = listen;
	}

	/**
	 * Registers for the value changes of the bound object, if it is a {@link TLObjectOverlay}, and
	 * deregisters from a previously bound overlay.
	 */
	private void updateOverlayListener() {
		TLObjectOverlay overlay = _object instanceof TLObjectOverlay bound ? bound : null;
		if (overlay == _observedOverlay) {
			return;
		}
		stopObservingOverlay();
		if (overlay != null) {
			overlay.addValueChangeListener(_overlayListener);
			_observedOverlay = overlay;
		}
	}

	private void stopObservingOverlay() {
		if (_observedOverlay != null) {
			_observedOverlay.removeValueChangeListener(_overlayListener);
			_observedOverlay = null;
		}
	}

	/**
	 * Shows a value written to the attribute of the bound overlay by someone else than this model.
	 */
	private void onOverlayValueChanged(TLObjectOverlay overlay, TLStructuredTypePart part, Object oldValue,
			Object newValue) {
		if (overlay != _observedOverlay || part != _part) {
			return;
		}
		if (_writing) {
			if (!Objects.equals(newValue, _writtenValue)) {
				_rewritten = true;
			}
			return;
		}
		refreshFromObject();
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		showRewrittenValue();
	}

	/**
	 * Shows the value of the attribute after it was written again while this model wrote it.
	 *
	 * <p>
	 * To be called by an override of {@link #setValue(Object)}, after the written value is
	 * announced.
	 * </p>
	 */
	protected final void showRewrittenValue() {
		if (_rewritten) {
			_rewritten = false;
			refreshFromObject();
		}
	}

	/**
	 * Computes the value again when an attribute the last computation read has changed.
	 */
	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (changedPart == _part) {
			// The own change of a derived field is no reason to compute it again.
			return;
		}
		if (_dependencies.contains(changedPart)) {
			followObject();
		}
	}

	@Override
	protected TLObject normalizationObject() {
		return _object;
	}

	@Override
	protected TLStructuredTypePart normalizationAttribute() {
		return _part;
	}

	@Override
	protected void writeValue(Object value) {
		boolean before = _writing;
		Object writtenBefore = _writtenValue;
		_writing = true;
		_writtenValue = value;
		try {
			_object.tUpdate(_part, value);
		} finally {
			_writing = before;
			_writtenValue = writtenBefore;
		}
	}

	@Override
	protected void discardWrittenValue() {
		if (_object instanceof TLObjectOverlay overlay) {
			boolean before = _writing;
			Object writtenBefore = _writtenValue;
			_writing = true;
			_writtenValue = overlay.getBaseValue(_part);
			try {
				overlay.revert(_part);
			} finally {
				_writing = before;
				_writtenValue = writtenBefore;
				// The caller shows the value the object holds after the revert.
				_rewritten = false;
			}
		}
	}

	/**
	 * Rebinds this model to a different object.
	 *
	 * <p>
	 * Called when the form switches objects or transitions between view and edit mode. Re-resolves
	 * the attribute from the new object's type, updates the default value (clearing dirty state),
	 * and fires value changed if the value differs.
	 * </p>
	 *
	 * @param newObject
	 *        The new object to bind to.
	 */
	public void setObject(TLObject newObject) {
		Object oldValue = getCachedValue();
		boolean hadInputError = getInputError() != null;
		_object = newObject;
		TLStructuredTypePart oldPart = _part;
		_part = resolvePart(newObject);
		if (_part != oldPart) {
			setNormalizer(ValueNormalizer.annotated(_part));
		}
		updateDependencyListener();
		updateOverlayListener();
		setMandatory(_part.isMandatory());
		Object newValue = getValue();
		setDefaultValue(newValue);
		setValueInternal(newValue);

		// Reset validation state from previous object. This includes an input error: the raw text
		// it rejected belongs to an edit that is over (saved, cancelled, or on another object).
		setRevealed(false);
		setError(null);
		setModelValidationError(null);
		setModelValidationWarnings(java.util.Collections.emptyList());

		// A rejected raw input left the value unchanged, so only the input control still shows it.
		// Push the value even though it did not change, to replace that text.
		if (hadInputError || !Objects.equals(oldValue, newValue)) {
			fireValueChanged(oldValue, newValue);
		}
	}

	/**
	 * Detaches this model from the form's field-change notifications and from the value changes of
	 * the bound overlay.
	 */
	@Override
	public void dispose() {
		if (_listening) {
			_form.removeFieldChangeListener(_dependencyListener);
			_listening = false;
		}
		stopObservingOverlay();
	}

	/**
	 * The resolved attribute part.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * The object this model currently reads from and writes to (base object or overlay).
	 *
	 * <p>
	 * Public because a field control may have to look at what it is editing, not only at its value:
	 * {@link AnnotationsFieldControlProvider} decides from the element's kind which surroundings to
	 * build for an annotation.
	 * </p>
	 */
	@Override
	public TLObject getObject() {
		return _object;
	}

	private TLStructuredTypePart resolvePart(TLObject obj) {
		TLStructuredType type = obj.tType();
		TLStructuredTypePart part = type.getPart(_part.getName());
		if (part == null) {
			throw new IllegalArgumentException(
				"Attribute '" + _part.getName() + "' not found in type '" + type + "'.");
		}
		return part;
	}
}
