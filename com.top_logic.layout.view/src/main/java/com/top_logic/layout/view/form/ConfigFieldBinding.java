/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;

/**
 * Keeps a configuration editor in step with the field it edits.
 *
 * <p>
 * A configuration editor cannot simply read the field's value as it renders: it works on a copy, and
 * it holds that copy for as long as it is displayed. That makes it a snapshot - and a snapshot goes
 * stale, because a field control is <em>not</em> rebuilt when the form is shown a different object.
 * {@link AttributeFieldControl} rebinds the existing field model instead
 * ({@code AttributeFieldModel#setObject}), so an editor that snapshotted at build time would go on
 * showing the previous object's configuration.
 * </p>
 *
 * <p>
 * So the editor lives in a holder here and is rebuilt whenever the field is given something else.
 * Two signals say so, and both are needed:
 * </p>
 * <ul>
 * <li>a value change that this binding did not cause - the ordinary case, where the new object's
 * value differs from the old one;</li>
 * <li>a change of the object the field is bound to, even when the value happens to be equal. Two
 * elements that both carry no annotations produce no value change at all, yet the editor must be
 * rebuilt: what may be added depends on the kind of element, not on what is there already. There is
 * no event for the rebinding itself, but it resets the field's validation state, so that is what is
 * listened to.</li>
 * </ul>
 */
final class ConfigFieldBinding {

	private final FieldModel _field;

	private final ReactStackControl _holder;

	private Supplier<ReactControl> _build;

	private final Supplier<Object> _identity;

	private Object _builtFor;

	private boolean _pushing;

	/**
	 * Creates a {@link ConfigFieldBinding}. Call {@link #start(Supplier)} to build the editor.
	 *
	 * @param identity
	 *        What the editor is built for - the object behind the field. Compared to decide whether
	 *        a rebinding needs a rebuild; {@code null} for a field that has no such object, where
	 *        the value change alone decides.
	 */
	ConfigFieldBinding(ReactContext context, FieldModel field, Supplier<Object> identity) {
		_field = field;
		_identity = identity;
		_holder = new ReactStackControl(context, Collections.emptyList());

		field.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				if (!_pushing && _build != null) {
					rebuild();
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The editor is built with the editability it had; a mode change rebuilds the form.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				if (_build != null && rebound()) {
					rebuild();
				}
			}
		});
	}

	/**
	 * Builds the editor for the first time, with the step that builds it again on every rebuild.
	 *
	 * <p>
	 * Separate from the constructor because the editor needs this binding to push its result back:
	 * building it inside the constructor would ask for an object that does not exist yet.
	 * </p>
	 */
	void start(Supplier<ReactControl> build) {
		_build = build;
		rebuild();
	}

	/** The control to render - the editor, swapped as the field changes. */
	ReactControl holder() {
		return _holder;
	}

	/**
	 * Hands the given value to the field without taking it for a change from outside.
	 */
	void push(Object value) {
		_pushing = true;
		try {
			_field.setValue(value);
		} finally {
			_pushing = false;
		}
	}

	private boolean rebound() {
		return _identity != null && _identity.get() != _builtFor;
	}

	private void rebuild() {
		for (ReactControl child : _holder.scriptingChildren()) {
			child.cleanupTree();
		}
		_builtFor = _identity == null ? null : _identity.get();
		_holder.setChildren(List.of(_build.get()));
	}

}
