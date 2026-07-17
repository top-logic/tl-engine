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
 * A {@link ViewChannel} whose value is computed from other channels.
 *
 * <p>
 * The derived value is recomputed whenever any input channel changes. Listeners are only notified if
 * the recomputed value is different from the current value (using {@link Objects#equals}).
 * </p>
 *
 * <p>
 * By default, a derived channel is read-only: calling {@link #set(Object)} throws
 * {@link UnsupportedOperationException}. When bound with a reverse function via
 * {@link #bind(List, Function, Function)}, the channel becomes bidirectional: setting a value
 * applies the reverse function and propagates the result to the first input channel.
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

	private Function<Object, Object> _reverseFunction;

	private List<ViewChannel> _inputs;

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
	 * <p>
	 * The channel is read-only: calling {@link #set(Object)} throws
	 * {@link UnsupportedOperationException}.
	 * </p>
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments to the function.
	 * @param evaluator
	 *        A function that takes an array of input values and returns the derived value.
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> evaluator) {
		bind(inputs, evaluator, null);
	}

	/**
	 * Wires this channel to its input channels with an optional reverse function for bidirectional
	 * propagation.
	 *
	 * <p>
	 * When a non-{@code null} reverse function is given, calling {@link #set(Object)} applies the
	 * reverse function to the written value and sets the result on the first input channel. The
	 * forward function then recomputes this channel's value from the updated input.
	 * </p>
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments to the forward
	 *        function.
	 * @param evaluator
	 *        A function that takes an array of input values and returns the derived value.
	 * @param reverse
	 *        A function that maps a derived value back to the value for the first input channel, or
	 *        {@code null} for a read-only derived channel.
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> evaluator,
			Function<Object, Object> reverse) {
		_inputs = inputs;
		_reverseFunction = reverse;
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
		if (_reverseFunction == null) {
			throw new UnsupportedOperationException("Derived channel '" + _name + "' is read-only.");
		}
		Object sourceValue = _reverseFunction.apply(newValue);
		return _inputs.get(0).set(sourceValue);
	}

	@Override
	public void addListener(ChannelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(ChannelListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void addVetoListener(VetoListener listener) {
		// DerivedViewChannel is read-only; veto listeners are not applicable.
	}

	@Override
	public void removeVetoListener(VetoListener listener) {
		// DerivedViewChannel is read-only; veto listeners are not applicable.
	}

	private void recompute(Function<Object[], Object> evaluator, List<ViewChannel> inputs) {
		Object newValue = evaluate(evaluator, inputs);
		Object oldValue = _value;
		if (!Objects.equals(oldValue, newValue)) {
			_value = newValue;
			ChannelNotificationScope scope = ChannelNotificationScope.current();
			scope.enter();
			try {
				for (ChannelListener listener : _listeners) {
					listener.handleNewValue(this, oldValue, newValue);
				}
			} finally {
				scope.exit();
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
