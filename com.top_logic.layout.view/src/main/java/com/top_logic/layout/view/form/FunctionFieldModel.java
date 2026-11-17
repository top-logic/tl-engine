/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A {@link BoundFieldModel} reading its value from an object through a function and writing an
 * edited value back through another.
 *
 * <p>
 * The two functions are all that ties the field to the object, so a value no attribute holds - one
 * computed from the object, one stored somewhere else entirely - is edited like any other.
 * </p>
 */
public class FunctionFieldModel extends BoundFieldModel {

	private final Object _object;

	private final Function<Object, Object> _read;

	private final BiConsumer<Object, Object> _write;

	private Runnable _afterSetValue;

	private BiPredicate<Object, Runnable> _writeGuard;

	/**
	 * Creates a {@link FunctionFieldModel}.
	 *
	 * @param object
	 *        The object the value is read from and written to.
	 * @param read
	 *        Computes the value the object currently holds.
	 * @param write
	 *        Writes an edited value, receiving the object and the new value.
	 */
	public FunctionFieldModel(Object object, Function<Object, Object> read, BiConsumer<Object, Object> write) {
		super(read.apply(object));
		_object = object;
		_read = read;
		_write = write;
	}

	/**
	 * The object this field reads from and writes to.
	 */
	@Override
	public Object getObject() {
		return _object;
	}

	/**
	 * Runs the given action after every {@link #setValue(Object)}, when the field shows the value
	 * that was set.
	 *
	 * @param action
	 *        The action to run, e.g. to build what depends on the written value again;
	 *        {@code null} for none.
	 */
	public void setAfterSetValue(Runnable action) {
		_afterSetValue = action;
	}

	/**
	 * Decides around every {@link #setValue(Object)} whether the value is set.
	 *
	 * <p>
	 * The guard receives the {@link #getObject() object} and the setting of the value, which it
	 * runs - e.g. while it holds a lock of the object - or refuses. A refused value is not
	 * written, the field shows the value the object holds again, and the
	 * {@link #setAfterSetValue(Runnable) action after setting a value} does not run.
	 * </p>
	 *
	 * @param guard
	 *        Runs the given setting of the value and answers {@code true}, or answers {@code false}
	 *        without running it; {@code null} to set every value.
	 */
	public void setWriteGuard(BiPredicate<Object, Runnable> guard) {
		_writeGuard = guard;
	}

	@Override
	public void setValue(Object value) {
		BiPredicate<Object, Runnable> guard = _writeGuard;
		if (guard == null) {
			super.setValue(value);
		} else if (!guard.test(_object, () -> super.setValue(value))) {
			// The input shows the value it was given, which was not written.
			fireIfRewritten(value, getValue());
			return;
		}
		Runnable action = _afterSetValue;
		if (action != null) {
			action.run();
		}
	}

	@Override
	protected Object readValue() {
		return _read.apply(_object);
	}

	@Override
	protected void writeValue(Object value) {
		_write.accept(_object, value);
	}

}
