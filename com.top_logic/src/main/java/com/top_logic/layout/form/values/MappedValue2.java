/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * An {@link AbstractValue} combining the given {@link Value}s with the given {@link Function2}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class MappedValue2<A1, A2, T> extends AbstractValue<T> {
	private final Value<? extends A1> _arg1;

	private final Value<? extends A2> _arg2;

	private final Function2<? super A1, ? super A2, ? extends T> _function;

	public MappedValue2(Value<? extends A1> arg1, Value<? extends A2> arg2,
			Function2<? super A1, ? super A2, ? extends T> function) {
		_arg1 = arg1;
		_arg2 = arg2;
		_function = function;
	}

	@Override
	public T get() {
		return _function.eval(_arg1.get(), _arg2.get());
	}

	@Override
	public ListenerBinding addListener(Listener listener) {
		ListenerBinding binding1 = _arg1.addListener(listener);
		ListenerBinding binding2 = _arg2.addListener(listener);
		return () -> {
			binding1.close();
			binding2.close();
		};
	}

}