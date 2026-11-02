/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.col.Sink;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link AttributeFieldModel} for attributes whose value is chosen from a set of options (enums,
 * references, option-bearing datatypes, or attributes with a configured options provider).
 *
 * <p>
 * The value is adapted between the attribute's single/multiple representation and the {@code List}
 * representation expected by the select control: {@link #getValue()} exposes a {@link List} for
 * multi-valued attributes (and the raw value for single-valued ones, which the control wraps), while
 * {@link #setValue(Object)} accepts the control's selection list and stores it back as the proper
 * single value or collection.
 * </p>
 *
 * <p>
 * Options come from an {@link OptionSource} and are recomputed whenever a dependency of the (possibly
 * TL-Script) options expression changes in the form, so that options depending on other fields stay
 * up to date, and whenever the field is bound to another object.
 * </p>
 *
 * <p>
 * While a control {@link #observeWhileDisplayed(ReactControl) displays} the field, the options also
 * follow the model: an object of one of the observed types that is created, changed or deleted, and
 * an offered option that is changed or deleted, compute the options again. An object created after
 * the field was built is therefore offered, and a deleted one no longer is, without building the
 * field anew.
 * </p>
 */
public class AttributeSelectFieldModel extends AttributeFieldModel implements SelectFieldModel {

	/**
	 * Computes the option list for a select field.
	 */
	@FunctionalInterface
	public interface OptionSource {

		/**
		 * Computes the available options.
		 *
		 * @param self
		 *        The object owning the attribute (an editing overlay in edit mode, the persistent
		 *        object in display mode).
		 * @param overlays
		 *        Lookup for the editing overlays of other in-form objects referenced by an option
		 *        expression, or {@code null} in display mode.
		 * @param dependencies
		 *        Receives the attributes read while computing the options, so that the field can
		 *        recompute when one of them changes.
		 * @return The available options.
		 */
		List<?> compute(TLObject self, OverlayLookup overlays, Sink<Pointer> dependencies);
	}

	private final FormControl _formControl;

	private final OptionSource _optionSource;

	private final boolean _multiple;

	private List<?> _options = Collections.emptyList();

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	private Set<TLStructuredTypePart> _dependencies = Collections.emptySet();

	private final FormControl.FieldChangeListener _fieldChangeListener = this::onFieldChanged;

	private final Set<TLStructuredType> _observedTypes;

	/**
	 * The observation of the model while a control displays the field, {@code null} if no control
	 * observes it.
	 */
	private RowSourceObserver<Object> _observer;

	/**
	 * Creates a new {@link AttributeSelectFieldModel} and computes its initial options.
	 *
	 * @param object
	 *        See {@link AttributeFieldModel#AttributeFieldModel(TLObject, TLStructuredTypePart, FormControl)}.
	 * @param part
	 *        See {@link AttributeFieldModel#AttributeFieldModel(TLObject, TLStructuredTypePart, FormControl)}.
	 * @param formControl
	 *        The enclosing form, providing the overlay lookup and field-change notifications.
	 * @param optionSource
	 *        Computes the available options.
	 */
	public AttributeSelectFieldModel(TLObject object, TLStructuredTypePart part, FormControl formControl,
			OptionSource optionSource) {
		this(object, part, formControl, optionSource, Set.of());
	}

	/**
	 * Creates an {@link AttributeSelectFieldModel} whose options follow the changes of objects of the
	 * given types while the field is {@link #observeWhileDisplayed(ReactControl) displayed}.
	 *
	 * @param observedTypes
	 *        The types whose object changes compute the options again, typically the class the
	 *        attribute refers to. Empty for options that do not follow the model, such as the
	 *        classifiers of an enumeration.
	 *
	 * @see #AttributeSelectFieldModel(TLObject, TLStructuredTypePart, FormControl, OptionSource)
	 */
	public AttributeSelectFieldModel(TLObject object, TLStructuredTypePart part, FormControl formControl,
			OptionSource optionSource, Set<TLStructuredType> observedTypes) {
		super(object, part, formControl);
		_formControl = formControl;
		_optionSource = optionSource;
		_observedTypes = observedTypes;
		_multiple = part.isMultiple();
		recomputeOptions();
		_formControl.addFieldChangeListener(_fieldChangeListener);
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public void setOptions(List<?> options) {
		_options = options == null ? Collections.emptyList() : options;
		for (SelectOptionsListener listener : _optionsListeners) {
			listener.onOptionsChanged(this, _options);
		}
	}

	@Override
	public void addOptionsListener(SelectOptionsListener listener) {
		if (_optionsListeners.isEmpty()) {
			_optionsListeners = new ArrayList<>(2);
		}
		_optionsListeners.add(listener);
	}

	@Override
	public void removeOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.remove(listener);
	}

	@Override
	public Object getValue() {
		Object raw = super.getValue();
		if (_multiple) {
			if (raw instanceof Collection) {
				return new ArrayList<>((Collection<?>) raw);
			}
			return raw == null ? Collections.emptyList() : Collections.singletonList(raw);
		}
		return raw;
	}

	@Override
	public void setValue(Object selection) {
		if (isObjectDeleted()) {
			return;
		}
		Object value = toAttributeValue(selection);
		Object newValue = normalize(value);
		Object oldValue = super.getValue();
		if (Objects.equals(oldValue, newValue)) {
			fireIfRewritten(value, newValue);
			return;
		}
		getObject().tUpdate(getPart(), newValue);
		setValueInternal(newValue);
		fireValueChanged(oldValue, newValue);
	}

	private Object toAttributeValue(Object selection) {
		List<?> values;
		if (selection instanceof List) {
			values = (List<?>) selection;
		} else if (selection == null) {
			values = Collections.emptyList();
		} else {
			values = Collections.singletonList(selection);
		}
		if (_multiple) {
			return new ArrayList<>(values);
		}
		return values.isEmpty() ? null : values.get(0);
	}

	@Override
	public void setObject(TLObject newObject) {
		super.setObject(newObject);
		recomputeOptions();
	}

	private void onFieldChanged(TLStructuredTypePart changedPart) {
		if (changedPart == getPart()) {
			// Changing this field does not affect its own options.
			return;
		}
		if (_dependencies.contains(changedPart)) {
			recomputeOptions();
		}
	}

	/**
	 * Follows the model while the given control displays this field.
	 *
	 * <p>
	 * From the moment the control is attached until it is detached, an object of one of the
	 * observed types that is created, changed or deleted, and an offered option that is changed or
	 * deleted, compute the options again - for the object the field is bound to now, over the edits
	 * of the form, exactly as a change of a field the options depend on does. The value the field
	 * holds stays as it is. A control attached again catches up with what happened while it was
	 * detached.
	 * </p>
	 *
	 * <p>
	 * A field that observes no types does not follow the model.
	 * </p>
	 *
	 * @param control
	 *        The control displaying this field.
	 */
	public void observeWhileDisplayed(ReactControl control) {
		if (_observedTypes.isEmpty()) {
			return;
		}
		if (_observer == null) {
			_observer = new RowSourceObserver<>(new ArrayList<Object>(_options), args -> computeOptions(),
				_observedTypes, List.of(), this::setOptions);
		}
		_observer.observeWhileAttached(control);
	}

	private void recomputeOptions() {
		setOptions(computeOptions());
	}

	/**
	 * Computes the options for the object the field is bound to now, and records the fields they
	 * depend on.
	 */
	private List<?> computeOptions() {
		Set<TLStructuredTypePart> dependencies = new HashSet<>();
		Sink<Pointer> sink = pointer -> dependencies.add(pointer.attribute());
		OverlayLookup overlays = _formControl.getValidationModel();
		List<?> options = _optionSource.compute(getObject(), overlays, sink);
		_dependencies = dependencies;
		return options;
	}

	/**
	 * Detaches this model from the form's field-change notifications and from the model.
	 */
	@Override
	public void dispose() {
		_formControl.removeFieldChangeListener(_fieldChangeListener);
		if (_observer != null) {
			_observer.detach();
		}
		super.dispose();
	}

}
