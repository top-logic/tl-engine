/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.function.BiConsumer;
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
	public Object getObject() {
		return _object;
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
