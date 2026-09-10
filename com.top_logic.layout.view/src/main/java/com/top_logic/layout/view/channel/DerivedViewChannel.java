/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;

/**
 * A {@link ViewChannel} whose value is computed from other channels.
 *
 * <p>
 * The derived value is recomputed whenever an input channel takes a new value, and - while the
 * channel is {@link #attach(ModelScope) attached} - whenever one of the objects the inputs hold is
 * edited. An expression reading an attribute of the object on its input channel therefore follows
 * that attribute being stored, although the channel keeps pointing to the same object. Listeners
 * are only notified if the recomputed value is different from the current value (using
 * {@link Objects#equals}).
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
public class DerivedViewChannel implements ObservingChannel {

	private final String _name;

	private Object _value;

	private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	private Function<Object, Object> _reverseFunction;

	private List<ViewChannel> _inputs;

	private Function<Object[], Object> _evaluator;

	private ChannelObjectObserver _inputObserver;

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
		bind(inputs, evaluator, reverse, Set.of());
	}

	/**
	 * Wires this channel to its input channels, additionally observing the given types.
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments to the forward
	 *        function.
	 * @param evaluator
	 *        A function that takes an array of input values and returns the derived value.
	 * @param reverse
	 *        A function that maps a derived value back to the value for the first input channel, or
	 *        {@code null} for a read-only derived channel.
	 * @param observedTypes
	 *        Types whose object changes recompute the value in addition to the objects the inputs
	 *        hold, which are always observed; empty for a function reading nothing but those
	 *        objects.
	 *
	 * @see #attach(ModelScope)
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> evaluator,
			Function<Object, Object> reverse, Set<TLStructuredType> observedTypes) {
		_inputs = inputs;
		_evaluator = evaluator;
		_reverseFunction = reverse;
		_value = evaluate(evaluator, inputs);
		_inputObserver = new ChannelObjectObserver(inputs, observedTypes, this::recompute);

		ChannelListener refreshListener = (sender, oldVal, newVal) -> recompute();
		for (ViewChannel input : inputs) {
			input.addListener(refreshListener);
		}
	}

	/**
	 * Begins following the objects the inputs hold, and recomputes the value for what they are now.
	 */
	@Override
	public void attach(ModelScope scope) {
		_inputObserver.attach(scope);
		recompute();
	}

	/**
	 * Stops following the objects the inputs hold.
	 */
	@Override
	public void detach() {
		_inputObserver.detach();
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

	private void recompute() {
		Object newValue = evaluate(_evaluator, _inputs);
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
