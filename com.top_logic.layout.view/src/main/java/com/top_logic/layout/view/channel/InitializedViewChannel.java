/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;

/**
 * A writable {@link DefaultViewChannel} whose value is (re)initialized from other channels.
 *
 * <p>
 * The channel is filled with the result of an initialization function over the values of its input
 * channels when it is {@link #bind(List, Function, Set) bound}. It is filled again:
 * </p>
 *
 * <ul>
 * <li>whenever an input channel takes a new value, and</li>
 * <li>while the channel is {@link #attach(ModelScope) attached}, whenever one of the objects the
 * inputs hold (or an object of an observed type) is changed in a way that changes the result of
 * the initialization function - an attribute the function reads is stored, for instance.</li>
 * </ul>
 *
 * <p>
 * Between two fills, the channel holds whatever an element or an action last wrote into it, with
 * the write semantics of a {@link DefaultViewChannel}: veto listeners are asked, and listeners are
 * told about every different value. A change of an observed object that leaves the result of the
 * initialization function as it was keeps a value written meanwhile. Attaching the channel after it
 * was detached computes the function again and fills the channel if the result differs from the one
 * it was last filled with, which catches up with the changes made while the view was off screen.
 * </p>
 *
 * <p>
 * A fill asks no veto listener: a fill caused by an input is asked before the input is written,
 * through a {@link VetoForwarder} from every input to this channel, and a fill caused by a stored
 * object reflects a change that already happened.
 * </p>
 *
 * @see DerivedViewChannel
 */
public class InitializedViewChannel extends DefaultViewChannel {

	private Function<Object[], Object> _init;

	/**
	 * The connection to the input channels, {@code null} while the channel is not bound.
	 */
	private ChannelInputBinding _binding;

	/**
	 * The result of the initialization function the channel was last filled with.
	 */
	private Object _initValue;

	/**
	 * Creates an {@link InitializedViewChannel}.
	 *
	 * @param name
	 *        The channel name (for debugging).
	 */
	public InitializedViewChannel(String name) {
		super(name);
	}

	/**
	 * Wires this channel to its input channels and fills it with the initial value.
	 *
	 * @param inputs
	 *        The resolved input channels whose values become positional arguments of the
	 *        initialization function; empty for a function computed without arguments.
	 * @param init
	 *        A function that takes an array of input values and returns the value to fill the
	 *        channel with.
	 * @param observedTypes
	 *        Types whose object changes compute the initialization function again in addition to
	 *        the objects the inputs hold, which are always observed; empty for a function reading
	 *        nothing but those objects.
	 *
	 * @implNote Connects to the inputs through a {@link ChannelInputBinding}, replacing the one of
	 *           a previous binding.
	 */
	public void bind(List<ViewChannel> inputs, Function<Object[], Object> init,
			Set<TLStructuredType> observedTypes) {
		releaseBinding();

		_init = init;
		_binding = new ChannelInputBinding(this, inputs, observedTypes, this::reinitialize, this::refresh);
		reinitialize();
	}

	/**
	 * Begins observing the objects of the value and the objects the inputs hold, and fills the
	 * channel again if the latter were changed in a way that changes the initial value.
	 */
	@Override
	public void attach(ModelScope scope) {
		super.attach(scope);
		if (_binding != null) {
			_binding.attach(scope);
			refresh();
		}
	}

	/**
	 * Stops observing the objects of the value and the objects the inputs hold.
	 */
	@Override
	public void detach() {
		if (_binding != null) {
			_binding.detach();
		}
		super.detach();
	}

	/**
	 * Unregisters from the inputs and stops all observation.
	 */
	@Override
	public void release() {
		releaseBinding();
		super.release();
	}

	private void releaseBinding() {
		if (_binding != null) {
			_binding.release();
			_binding = null;
		}
	}

	/**
	 * Fills the channel with the value computed from the inputs, replacing what was written.
	 */
	private void reinitialize() {
		_initValue = computeInit();
		replace(_initValue);
	}

	/**
	 * Fills the channel with the value computed from the inputs, if that differs from the value it
	 * was last filled with.
	 */
	private void refresh() {
		Object initValue = computeInit();
		if (!Objects.equals(initValue, _initValue)) {
			_initValue = initValue;
			replace(initValue);
		}
	}

	private Object computeInit() {
		return _init.apply(_binding.arguments());
	}

}
