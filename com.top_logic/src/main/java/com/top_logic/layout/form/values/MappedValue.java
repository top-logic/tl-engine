/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.col.Mapping;

/**
 * An {@link AbstractValue} mapping the given {@link Value} with the given {@link Mapping}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class MappedValue<S, T> extends AbstractValue<T> {
	private final Value<? extends S> _value;

	private final Mapping<? super S, ? extends T> _function;

	public MappedValue(Value<? extends S> value, Mapping<? super S, ? extends T> function) {
		_value = value;
		_function = function;
	}

	@Override
	public T get() {
		return _function.map(_value.get());
	}

	@Override
	public ListenerBinding addListener(Listener listener) {
		return _value.addListener(listener);
	}
}