/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * A read-only {@link ViewChannel} whose value is computed from other channels.
 *
 * <p>
 * The derived value is recomputed whenever any input channel changes. Listeners are only notified if
 * the recomputed value is different from the current value (using {@link Objects#equals}).
 * </p>
 *
 * <p>
 * This is a per-session object. The evaluation function is typically compiled once at configuration
 * time (e.g. from a TL-Script expression) and passed in via {@link #bind(List, Function)}.
 * </p>
 */
public class DerivedViewChannel implements ViewChannel {

	private final String _name;

	private Object _value;

	private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link DerivedViewChannel}.
	 *
	 * @param name
	 *        The channel name (for error messages and debugging).
	 */
	public DerivedViewChannel(String name) {
		_name = name;
	}

	/**
	 * Wires this channel to its input channels, computes the initial value, and attaches change
	 * listeners for automatic recomputation.
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments to the function.
	 * @param evaluator
	 *        A function that takes an array of input values and returns the derived value.
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> evaluator) {
		_value = evaluate(evaluator, inputs);

		ChannelListener refreshListener = (sender, oldVal, newVal) -> recompute(evaluator, inputs);
		for (ViewChannel input : inputs) {
			input.addListener(refreshListener);
		}
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	public boolean set(Object newValue) {
		throw new IllegalStateException("Derived channel '" + _name + "' is read-only.");
	}

	@Override
	public void addListener(ChannelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(ChannelListener listener) {
		_listeners.remove(listener);
	}

	private void recompute(Function<Object[], Object> evaluator, List<ViewChannel> inputs) {
		Object newValue = evaluate(evaluator, inputs);
		Object oldValue = _value;
		if (!Objects.equals(oldValue, newValue)) {
			_value = newValue;
			for (ChannelListener listener : _listeners) {
				listener.handleNewValue(this, oldValue, newValue);
			}
		}
	}

	private static Object evaluate(Function<Object[], Object> evaluator, List<ViewChannel> inputs) {
		Object[] args = new Object[inputs.size()];
		for (int i = 0; i < args.length; i++) {
			args[i] = inputs.get(i).get();
		}
		return evaluator.apply(args);
	}

	@Override
	public String toString() {
		return "DerivedViewChannel[" + _name + "=" + _value + "]";
	}
}
