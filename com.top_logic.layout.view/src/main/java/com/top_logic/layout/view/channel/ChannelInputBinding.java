/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;

/**
 * The connection of a channel computed from other channels to those input channels.
 *
 * <p>
 * A channel whose value is computed by a function over the values of input channels - a
 * {@link DerivedViewChannel}, an {@link InitializedViewChannel} - must compute again when an input
 * takes a new value, and when one of the objects an input holds is edited while the input keeps
 * pointing to it. The binding reports both to the computing channel:
 * </p>
 *
 * <ul>
 * <li>A new value of an input is reported for as long as the binding exists.</li>
 * <li>A change of an object held by an input, or of an object of one of the observed types, is
 * reported only while the binding is {@link #attach(ModelScope) attached}, i.e. while the view
 * declaring the channel is displayed. Attaching after a {@link #detach()} reports a change once,
 * since the objects may have been edited unseen in between.</li>
 * </ul>
 *
 * <p>
 * Additionally, a {@link VetoForwarder} from every input to the computing channel makes unsaved
 * changes bound to the computed value block the write of the input it is computed from.
 * </p>
 */
public class ChannelInputBinding {

	private final List<ViewChannel> _inputs;

	private final ChannelObjectObserver _objectObserver;

	/**
	 * The registrations of the listener reporting a new value of one of the {@link #_inputs}.
	 */
	private final List<Registration> _inputRegistrations = new ArrayList<>();

	private final List<Runnable> _vetoForwarderRemovers = new ArrayList<>();

	/**
	 * Creates a {@link ChannelInputBinding} and registers it on the given inputs.
	 *
	 * @param target
	 *        The channel computed from the inputs, the target of the {@link VetoForwarder}s.
	 * @param inputs
	 *        The input channels, in the order their values become arguments of the computing
	 *        function.
	 * @param observedTypes
	 *        Types whose object changes are reported in addition to the changes of the objects the
	 *        inputs hold; empty for a function reading nothing but those objects.
	 * @param onInputChange
	 *        Run when an input takes a new value.
	 * @param onObjectChange
	 *        Run when an observed object is changed while attached, and once when the observation
	 *        resumes after a {@link #detach()}.
	 */
	public ChannelInputBinding(ViewChannel target, List<ViewChannel> inputs, Set<TLStructuredType> observedTypes,
			Runnable onInputChange, Runnable onObjectChange) {
		_inputs = inputs;
		_objectObserver = new ChannelObjectObserver(inputs, observedTypes, onObjectChange);

		ViewChannel.ChannelListener inputListener = (sender, oldValue, newValue) -> onInputChange.run();
		for (ViewChannel input : inputs) {
			_inputRegistrations.add(input.addListener(inputListener));
			_vetoForwarderRemovers.add(VetoForwarder.forward(input, target));
		}
	}

	/**
	 * The input channels, in declaration order.
	 */
	public List<ViewChannel> inputs() {
		return _inputs;
	}

	/**
	 * The current values of the {@link #inputs()}, in declaration order: the arguments of the
	 * computing function.
	 */
	public Object[] arguments() {
		return ChannelInputs.arguments(_inputs);
	}

	/**
	 * Begins reporting the changes of the observed objects.
	 *
	 * @param scope
	 *        The scope model listeners register on, {@code null} for a view built outside a browser
	 *        window.
	 */
	public void attach(ModelScope scope) {
		_objectObserver.attach(scope);
	}

	/**
	 * Stops reporting the changes of the observed objects.
	 */
	public void detach() {
		_objectObserver.detach();
	}

	/**
	 * Unregisters from the inputs - the listener reporting their new values and the
	 * {@link VetoForwarder}s - and stops observing the objects they hold.
	 */
	public void release() {
		for (Registration registration : _inputRegistrations) {
			registration.dispose();
		}
		_inputRegistrations.clear();
		for (Runnable remover : _vetoForwarderRemovers) {
			remover.run();
		}
		_vetoForwarderRemovers.clear();
		_objectObserver.detach();
	}

}
