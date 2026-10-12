/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.listen.ObservedObjects;
import com.top_logic.tool.execution.ExecutableState;

/**
 * An {@link ViewExecutabilityRule executability rule} applied to the value of an input channel,
 * followed live while the UI it decides for is displayed.
 *
 * <p>
 * The {@link #getState() state} is the rule's answer for the value the input channel holds now.
 * The rule never sees a deleted object: an input object that is no longer
 * {@link TLObject#tValid() valid} counts as no input at all, and the deleted members of a
 * collection input are left out (see {@link #withoutDeleted(Object)}). A channel may hold a deleted
 * object until the display that writes it chooses another value, but nothing can be decided over
 * an object that is gone.
 * </p>
 *
 * <p>
 * While {@link #attach(ModelScope) attached}, every event after which the rule may answer
 * differently runs the change callback given at construction:
 * </p>
 * <ul>
 * <li>the input channel taking a new value,</li>
 * <li>the object that value holds being changed or deleted - a deletion disables what the rule
 * decides for, since the deleted object then counts as no input -, or an object of one of the
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
	 * The registration reporting a new value of the {@link #_inputChannel input channel} to the
	 * change callback, while attached.
	 */
	private Registration _inputRegistration = Registration.NONE;

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

		List<ViewChannel> observedChannels = inputChannel == null ? List.of() : List.of(inputChannel);
		_inputObserver = new ChannelObjectObserver(observedChannels, observedTypes, onChange, onChange);
	}

	/**
	 * Creates the {@link LiveExecutability} an {@link ExecutabilityConfig} declares: its rules
	 * {@link ViewExecutabilityRules#build(List, ViewContext) built} for the given context, applied
	 * to its input channel, followed together with its observed types.
	 *
	 * @param config
	 *        The declaration of the rules, their input and their observed types.
	 * @param context
	 *        The build-time context the rules are bound to and the input channel is resolved in.
	 * @param onChange
	 *        Run while attached whenever the rules may answer differently than before.
	 */
	public static LiveExecutability create(ExecutabilityConfig config, ViewContext context, Runnable onChange) {
		ChannelRef inputRef = config.getInput();
		return new LiveExecutability(
			ViewExecutabilityRules.build(config.getExecutability(), context),
			inputRef == null ? null : context.resolveChannel(inputRef),
			ObservedTypes.resolve(config.getObservedTypes()),
			onChange);
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
	 * The current value of the {@link #getInputChannel() input channel} without the objects deleted
	 * meanwhile (see {@link #withoutDeleted(Object)}), {@code null} without a channel.
	 */
	public Object getInput() {
		return _inputChannel != null ? withoutDeleted(_inputChannel.get()) : null;
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
	 *        The value the rule decides over, after leaving out what is deleted (see
	 *        {@link #withoutDeleted(Object)}).
	 */
	public ExecutableState getState(Object input) {
		return _rule.isExecutable(withoutDeleted(input));
	}

	/**
	 * The given input without the objects in it that are deleted.
	 *
	 * <p>
	 * A single {@link TLObject} that is not {@link TLObject#tValid() valid} any more counts as no
	 * input, {@code null}. A collection holding such objects is answered with a copy leaving them
	 * out - a {@link Set} by a set, any other collection by a list, both in the original order; a
	 * collection of which nothing is left stays an empty collection. Any other value is answered
	 * unchanged.
	 * </p>
	 *
	 * @param input
	 *        The value of an input channel, or a value supplied in its place.
	 * @return The value a rule and a command decide and act over.
	 */
	public static Object withoutDeleted(Object input) {
		if (input instanceof TLObject object) {
			return object.tValid() ? input : null;
		}
		if (input instanceof Collection<?> values) {
			if (ObservedObjects.objects(values).stream().allMatch(TLObject::tValid)) {
				return input;
			}
			Collection<Object> result = values instanceof Set ? new LinkedHashSet<>() : new ArrayList<>();
			for (Object value : values) {
				if (!(value instanceof TLObject object) || object.tValid()) {
					result.add(value);
				}
			}
			return result;
		}
		return input;
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
			_inputRegistration = _inputChannel.addListener((sender, oldValue, newValue) -> _onChange.run());
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
		_inputRegistration.dispose();
		_inputRegistration = Registration.NONE;
		if (_ruleObservation != null) {
			_ruleObservation.run();
			_ruleObservation = null;
		}
		_inputObserver.detach();
	}

	/**
	 * Follows the rule while the given control is displayed.
	 *
	 * <p>
	 * The rule is attached when the control is attached - the update then runs, since the rule may
	 * answer differently than when the control was last displayed - and detached when it is
	 * detached or cleaned up, so that no listener outlives the display.
	 * </p>
	 *
	 * @param context
	 *        The context whose model scope the object observation registers on.
	 * @param control
	 *        The control displaying what the rule decides.
	 * @param update
	 *        Brings the control in line with what the rule answers now.
	 */
	public void followWhileDisplayed(ViewContext context, ReactControl control, Runnable update) {
		control.addAttachListener(() -> {
			attach(context.getModelScope());
			update.run();
		});
		control.addDetachListener(this::detach);
		control.addCleanupAction(this::detach);
	}

}
