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
 *
 * <p>
 * A value the user has entered is written to the bound object as well. When the bound object stands
 * for another one, which receives a change stored by someone else, {@link #followObject()} makes a
 * field the user has left alone show the stored value, while a field holding the user's entry keeps
 * it.
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
	 * Makes the field show what the bound object holds now, unless the user has changed the field.
	 *
	 * <p>
	 * Call this after a change was stored to the object the bound object stands for, e.g. by a
	 * command run while a form edits that object. A field the user has changed, whether to a
	 * value or to an input that was {@link #getInputError() rejected}, keeps what the user entered.
	 * Any other field takes the object's value both as its displayed value and as the value it is
	 * dirty against, so that it stays unchanged and shows the stored value.
	 * </p>
	 *
	 * <p>
	 * A field that was changed and changed back holds the value it started with and is therefore
	 * left alone by the user as well: the value it wrote back is dropped by
	 * {@link #discardWrittenValue()}, so that the stored value shows through.
	 * </p>
	 */
	public final void followObject() {
		if (isDirty() || getInputError() != null) {
			return;
		}
		Object shownValue = getCachedValue();
		if (Objects.equals(readValue(), shownValue)) {
			discardWrittenValue();
		}
		Object liveValue = readValue();
		setDefaultValue(liveValue);
		if (!Objects.equals(shownValue, liveValue)) {
			setValueInternal(liveValue);
			fireValueChanged(shownValue, liveValue);
		}
	}

	/**
	 * Drops a value this field wrote to the bound object that does not differ from what the field
	 * started with, so that {@link #readValue()} delivers the value of the object the bound object
	 * stands for.
	 *
	 * <p>
	 * Nothing to do for a field whose bound object holds its values itself, which is the default.
	 * </p>
	 */
	protected void discardWrittenValue() {
		// The bound object holds the value itself.
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
