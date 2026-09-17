/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Objects;

import com.top_logic.layout.form.model.AbstractFieldModel;

/**
 * A field model holding no value of its own: it reads the value from the object it is bound to and
 * writes an edited value back there.
 *
 * <p>
 * Where that value lives is what a subclass says - an attribute of the edited object, a value
 * computed from it. Because the object is where the value lives, it can be changed by something
 * else than this field, which {@link #refreshFromObject()} makes the field show.
 * </p>
 */
public abstract class BoundFieldModel extends AbstractFieldModel {

	/**
	 * Creates a {@link BoundFieldModel} showing the given value.
	 *
	 * @param initialValue
	 *        The value the bound object holds when the field is created, which is the value the
	 *        field is dirty against.
	 */
	protected BoundFieldModel(Object initialValue) {
		super(initialValue);
	}

	@Override
	public Object getValue() {
		return readValue();
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		writeValue(value);
		setValueInternal(value);
		fireValueChanged(oldValue, value);
	}

	/**
	 * Re-reads the value from the bound object and fires a value change when it differs from the
	 * value the field last showed.
	 *
	 * <p>
	 * Call this after something else has changed the object - a detail dialog applying its overlay
	 * onto a row overlay, for instance - so that the field shows what the object now holds.
	 * </p>
	 */
	public final void refreshFromObject() {
		Object cachedValue = getCachedValue();
		Object liveValue = readValue();
		if (!Objects.equals(cachedValue, liveValue)) {
			setValueInternal(liveValue);
			fireValueChanged(cachedValue, liveValue);
		}
	}

	/**
	 * The value the bound object currently holds.
	 */
	protected abstract Object readValue();

	/**
	 * Writes the given value to the bound object.
	 *
	 * @param value
	 *        The edited value. May be {@code null}.
	 */
	protected abstract void writeValue(Object value);

}
