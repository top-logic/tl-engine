/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;
import java.util.Set;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.execution.ExecutableState;

/**
 * An {@link ViewExecutabilityRule executability rule} applied to the value of an input channel,
 * followed live while the UI it decides for is displayed.
 *
 * <p>
 * The {@link #getState() state} is the rule's answer for the value the input channel holds now.
 * While {@link #attach(ModelScope) attached}, every event after which the rule may answer
 * differently runs the change callback given at construction:
 * </p>
 * <ul>
 * <li>the input channel taking a new value,</li>
 * <li>the object that value holds being changed or deleted, or an object of one of the
 * {@link ExecutabilityConfig#getObservedTypes() observed types} being created, changed or deleted
 * (see {@link ChannelObjectObserver}),</li>
 * <li>a rule deciding by more than its input reporting a change of its own
 * ({@link ObservableRule}),</li>
 * <li>re-attaching after a {@link #detach()}, since the objects may have changed unseen
 * meanwhile.</li>
 * </ul>
 *
 * <p>
 * The callback is told that the state <em>may</em> have changed; it re-reads the
 * {@link #getState() state} and compares it with what it displays. After {@link #detach()}, no
 * listener stays registered and the callback is not run any more.
 * </p>
 *
 * @see ViewCommandModel
 */
public class LiveExecutability {

	private final ViewExecutabilityRule _rule;

	private final ViewChannel _inputChannel;

	private final Runnable _onChange;

	/**
	 * Reports a new value of the {@link #_inputChannel input channel} to the change callback.
	 */
	private final ViewChannel.ChannelListener _inputListener;

	private final ChannelObjectObserver _inputObserver;

	/**
	 * Stops the rule reporting changes again, {@code null} while the rule is not observed.
	 */
	private Runnable _ruleObservation;

	private boolean _attached;

	/**
	 * Creates a {@link LiveExecutability}.
	 *
	 * @param rule
	 *        The rule deciding over the input, already {@link ContextDependentRule#bind bound} to
	 *        its context where it needs one.
	 * @param inputChannel
	 *        The channel holding the input the rule decides over; {@code null} where there is no
	 *        input, the rule then decides over {@code null}.
	 * @param observedTypes
	 *        Types whose object changes let the rule decide anew in addition to the input object,
	 *        see {@link ObservedTypes#resolve(List)}; empty observes just the input object.
	 * @param onChange
	 *        Run while attached whenever the rule may answer differently than before.
	 */
	public LiveExecutability(ViewExecutabilityRule rule, ViewChannel inputChannel,
			Set<TLStructuredType> observedTypes, Runnable onChange) {
		_rule = rule;
		_inputChannel = inputChannel;
		_onChange = onChange;
		_inputListener = (sender, oldValue, newValue) -> onChange.run();

		List<ViewChannel> observedChannels = inputChannel == null ? List.of() : List.of(inputChannel);
		_inputObserver = new ChannelObjectObserver(observedChannels, observedTypes, onChange);
	}

	/**
	 * The rule deciding over the input.
	 */
	public ViewExecutabilityRule getRule() {
		return _rule;
	}

	/**
	 * The channel holding the input, {@code null} if there is none.
	 */
	public ViewChannel getInputChannel() {
		return _inputChannel;
	}

	/**
	 * The current value of the {@link #getInputChannel() input channel}, {@code null} without one.
	 */
	public Object getInput() {
		return _inputChannel != null ? _inputChannel.get() : null;
	}

	/**
	 * The state the rule assigns to the current {@link #getInput() input}, evaluated anew on every
	 * call.
	 */
	public ExecutableState getState() {
		return getState(getInput());
	}

	/**
	 * The state the rule assigns to an input the caller supplies instead of the
	 * {@link #getInput() channel value} - a single row of a table, say.
	 *
	 * @param input
	 *        The value the rule decides over.
	 */
	public ExecutableState getState(Object input) {
		return _rule.isExecutable(input);
	}

	/**
	 * Whether changes are followed, i.e. {@link #attach(ModelScope)} was called and not yet undone
	 * by {@link #detach()}.
	 */
	public boolean isAttached() {
		return _attached;
	}

	/**
	 * Begins following everything the rule's answer depends on.
	 *
	 * <p>
	 * Idempotent: attaching again while attached changes nothing. Attaching after a
	 * {@link #detach()} runs the change callback once, since the objects may have changed unseen.
	 * The state is not evaluated here: the caller reads {@link #getState()} when it needs it.
	 * </p>
	 *
	 * @param scope
	 *        The scope the object observation registers on, {@code null} for a UI built outside a
	 *        browser window, which follows the channel value and the rule alone.
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		if (_inputChannel != null) {
			_inputChannel.addListener(_inputListener);
		}
		if (_rule instanceof ObservableRule observable) {
			_ruleObservation = observable.observe(_onChange);
		}
		_inputObserver.attach(scope);
	}

	/**
	 * Stops following, removing every listener {@link #attach(ModelScope)} registered.
	 *
	 * <p>
	 * Idempotent: detaching while not attached changes nothing.
	 * </p>
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		if (_inputChannel != null) {
			_inputChannel.removeListener(_inputListener);
		}
		if (_ruleObservation != null) {
			_ruleObservation.run();
			_ruleObservation = null;
		}
		_inputObserver.detach();
	}

}
