/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateContainer.Handle;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.ActivePropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.util.Pointer;

/**
 * Displays an attribute with an {@link AttributeWithFallbackStorage} in a {@link FormField}.
 *
 * <p>
 * The field of such an attribute edits its explicit value (the value of the storage attribute).
 * While the field is {@link FormField#isActive() active}, it displays the explicit value as value
 * and the fallback value as {@link FormField#getPlaceholder() placeholder}. An inactive field does
 * not display a placeholder. Therefore, it displays the fallback value as value, if there is no
 * explicit value.
 * </p>
 *
 * <p>
 * This instance keeps the explicit value itself and computes the value, the default value and the
 * placeholder of the field from the explicit value, the fallback value and the active state of the
 * field each time one of them changes. The explicit value is the value transferred to the model,
 * see {@link #getExplicitValue()}.
 * </p>
 *
 * <p>
 * The fallback value is computed over the overlays of the form in a {@link TracingScope} that
 * records the attributes the computation reads. When the field of one of these attributes changes,
 * the fallback value is computed again. This includes a fallback attribute that is derived from
 * other attributes and has no field of its own. When the field of the storage attribute changes,
 * its value becomes the new explicit value.
 * </p>
 *
 * @see #install(EditContext, FormField, AttributeWithFallbackStorage.Config)
 */
public class FallbackFieldDisplay implements ValueListener, ActivePropertyListener {

	private static final Property<FallbackFieldDisplay> FALLBACK_DISPLAY =
		TypedAnnotatable.property(FallbackFieldDisplay.class, "fallbackDisplay");

	private final EditContext _editContext;

	private final FormField _field;

	private final TLFormObject _overlay;

	private final TLStructuredTypePart _storageAttr;

	private final TLStructuredTypePart _fallbackAttr;

	private final String _cssExplicit;

	private final String _cssFallback;

	private final ValueListener _fallbackListener = (field, oldValue, newValue) -> onFallbackDependencyChanged();

	private final ValueListener _storageListener = (field, oldValue, newValue) -> onStorageChanged();

	/**
	 * The attributes the last computation of the fallback value read.
	 */
	private Set<Pointer> _fallbackDependencies = Set.of();

	private final List<Handle> _fallbackHandles = new ArrayList<>();

	/**
	 * The explicit value the field was initialized with, in the representation of the field.
	 */
	private Object _originalExplicit;

	/**
	 * The current explicit value, in the representation of the field.
	 */
	private Object _explicit;

	/**
	 * The current fallback value, in the representation of the field.
	 */
	private Object _fallback;

	/**
	 * The default value this instance has set to the field.
	 *
	 * <p>
	 * A different default value of the field shows that the field has been initialized from
	 * outside, e.g. by {@link TLFormObject#tUpdate(TLStructuredTypePart, Object)}.
	 * </p>
	 */
	private Object _displayedDefault;

	/**
	 * Whether this instance is currently changing the field, which must not be interpreted as
	 * change of the explicit value.
	 */
	private boolean _updating;

	private FallbackFieldDisplay(EditContext editContext, FormField field,
			AttributeWithFallbackStorage.Config<?> config) {
		_editContext = editContext;
		_field = field;
		_overlay = editContext.getOverlay();
		_storageAttr = _overlay.tType().getPart(config.getStorageAttribute());
		_fallbackAttr = _overlay.tType().getPart(config.getFallbackAttribute());
		_cssExplicit = config.getCssExplicit();
		_cssFallback = config.getCssFallback();
	}

	/**
	 * Initializes the given field displaying an attribute with an
	 * {@link AttributeWithFallbackStorage} and keeps its display up to date.
	 *
	 * @param editContext
	 *        The context of the edited attribute.
	 * @param field
	 *        The field displaying the attribute.
	 * @param config
	 *        The configuration of the storage of the attribute.
	 */
	public static void install(EditContext editContext, FormField field,
			AttributeWithFallbackStorage.Config<?> config) {
		FallbackFieldDisplay display = new FallbackFieldDisplay(editContext, field, config);
		field.set(FALLBACK_DISPLAY, display);
		display.init();
		field.addValueListener(display);
		field.addListener(FormField.ACTIVE_PROPERTY, display);
	}

	/**
	 * The {@link FallbackFieldDisplay} of the given field, or <code>null</code>, if the field does
	 * not display an attribute with an {@link AttributeWithFallbackStorage}.
	 */
	public static FallbackFieldDisplay get(FormField field) {
		return field.get(FALLBACK_DISPLAY);
	}

	private void init() {
		Object explicit;
		if (_editContext.isDisabled()) {
			// The value of a disabled context is the value of the attribute, either explicit or
			// fallback.
			explicit = _overlay.tValue(_storageAttr);
		} else {
			explicit = _editContext.getCorrectValues();
		}
		_originalExplicit = _explicit = toFieldValue(explicit);

		_overlay.getScope().addValueListener(_overlay, _storageAttr, _storageListener);

		updateFallback();
	}

	/**
	 * The explicit value of the attribute, in the representation of the field.
	 *
	 * <p>
	 * The value of an inactive field is the fallback value, if there is no explicit value. The
	 * value transferred to the model is the explicit value nevertheless.
	 * </p>
	 *
	 * @see AttributeUpdate#fieldToAttributeValue(FormField)
	 */
	public Object getExplicitValue() {
		return _explicit;
	}

	/**
	 * The attribute the {@link #getExplicitValue() explicit value} is stored in.
	 */
	public TLStructuredTypePart getStorageAttribute() {
		return _storageAttr;
	}

	/**
	 * Whether the explicit value differs from the value the field was initialized with.
	 */
	public boolean isChanged() {
		return !Utils.equals(_explicit, _originalExplicit);
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (_updating) {
			return;
		}

		Object defaultValue = _field.getDefaultValue();
		if (!Utils.equals(defaultValue, _displayedDefault)) {
			// The field was initialized from outside.
			_originalExplicit = defaultValue;
			_explicit = newValue;
		} else if (!_field.isActive() && Utils.equals(newValue, defaultValue)) {
			// An inactive field was reset, e.g. when becoming read-only. Its default value
			// displays the original explicit value, which may be the fallback value.
			_explicit = _originalExplicit;
		} else {
			_explicit = newValue;
		}
		refresh();
	}

	@Override
	public Bubble handleActiveChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		refresh();
		return Bubble.BUBBLE;
	}

	private void onStorageChanged() {
		if (_updating) {
			return;
		}
		Object explicit = toFieldValue(_overlay.tValue(_storageAttr));
		if (Utils.equals(explicit, _explicit)) {
			return;
		}
		_explicit = explicit;
		refresh();
	}

	private void onFallbackDependencyChanged() {
		if (_updating) {
			return;
		}
		updateFallback();
	}

	/**
	 * Computes the fallback value and observes the attributes the computation reads.
	 */
	private void updateFallback() {
		AttributeUpdateContainer scope = _overlay.getScope();

		Set<Pointer> dependencies = new LinkedHashSet<>();
		Object fallback;
		try (TracingScope tracing = TracingScope.open(dependencies::add, scope)) {
			fallback = _overlay.tValue(_fallbackAttr);
		}

		if (!dependencies.equals(_fallbackDependencies)) {
			for (Handle handle : _fallbackHandles) {
				handle.release();
			}
			_fallbackHandles.clear();
			for (Pointer dependency : dependencies) {
				_fallbackHandles.add(scope.addValueListener(dependency.object(), dependency.attribute(),
					_fallbackListener));
			}
			_fallbackDependencies = dependencies;
		}

		_fallback = toFieldValue(fallback);
		refresh();
	}

	/**
	 * Sets value, default value, placeholder and CSS classes of the field according to the explicit
	 * value, the fallback value and the active state of the field.
	 */
	private void refresh() {
		boolean active = _field.isActive();

		_updating = true;
		try {
			_field.setPlaceholder(active ? _fallback : null);

			Object defaultValue = active ? _originalExplicit : display(_originalExplicit);
			if (!Utils.equals(_field.getDefaultValue(), defaultValue)) {
				_field.setDefaultValue(defaultValue);
			}
			_displayedDefault = _field.getDefaultValue();

			// Do not override an invalid input of the user in an active field.
			if (!active || _field.hasValue()) {
				Object value = active ? _explicit : display(_explicit);
				if (!_field.hasValue() || !Utils.equals(_field.getValue(), value)) {
					_field.setValue(value);
				}
			}
		} finally {
			_updating = false;
		}

		updateCss();
	}

	private Object display(Object explicit) {
		return Utils.isEmpty(explicit) ? _fallback : explicit;
	}

	private void updateCss() {
		boolean displaysFallback = Utils.isEmpty(_explicit);
		String remove = displaysFallback ? _cssExplicit : _cssFallback;
		String add = displaysFallback ? _cssFallback : _cssExplicit;
		if (remove != null) {
			_field.removeCssClass(remove);
		}
		if (add != null) {
			_field.addCssClass(add);
		}
	}

	private Object toFieldValue(Object modelValue) {
		return AttributeFormFactory.toFieldValue(_editContext, _field, modelValue);
	}

}
