/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Objects;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;

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
 *
 * <p>
 * A bound object that is deleted (a {@link TLObject} that is no longer {@link TLObject#tValid()
 * valid}) is not accessed any more: the field keeps showing the value it last showed until it is
 * bound to another object or removed from the display.
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

	/**
	 * The object this field reads its value from and writes an edited value to.
	 */
	public abstract Object getObject();

	/**
	 * Whether the bound object is deleted.
	 *
	 * <p>
	 * Only a {@link TLObject} can be deleted. A deleted object can neither be read nor written: an
	 * access fails.
	 * </p>
	 */
	protected final boolean isObjectDeleted() {
		return getObject() instanceof TLObject object && !object.tValid();
	}

	/**
	 * The value the bound object currently holds.
	 *
	 * <p>
	 * When the bound object is {@link #isObjectDeleted() deleted}, this is the value the field last
	 * showed: a control redrawing itself between the deletion and the removal of the field from the
	 * display keeps its content instead of failing.
	 * </p>
	 */
	@Override
	public Object getValue() {
		if (isObjectDeleted()) {
			return getCachedValue();
		}
		return readValue();
	}

	/**
	 * Writes the given value to the bound object and fires a value change.
	 *
	 * <p>
	 * When the bound object is {@link #isObjectDeleted() deleted}, the edit is dropped: there is no
	 * object left to hold it, so neither the object nor the value of the field changes.
	 * </p>
	 */
	@Override
	public void setValue(Object value) {
		if (isObjectDeleted()) {
			return;
		}
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
	 *
	 * <p>
	 * A {@link #isObjectDeleted() deleted} object holds nothing to be shown: the field keeps its
	 * value and fires no change.
	 * </p>
	 */
	public final void refreshFromObject() {
		if (isObjectDeleted()) {
			return;
		}
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
	 * dirty against, so that it stays unchanged and shows the stored value. A field whose bound
	 * object is {@link #isObjectDeleted() deleted} is left as it is.
	 * </p>
	 *
	 * <p>
	 * A field that was changed and changed back holds the value it started with and is therefore
	 * left alone by the user as well: the value it wrote back is dropped by
	 * {@link #discardWrittenValue()}, so that the stored value shows through.
	 * </p>
	 */
	public final void followObject() {
		if (isObjectDeleted() || isDirty() || getInputError() != null) {
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
	 * Releases what this field holds beyond its display, when the edit it belongs to ends: a
	 * registration with the form, an edit session of its own.
	 *
	 * <p>
	 * Nothing to release by default.
	 * </p>
	 */
	public void dispose() {
		// Nothing held.
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
