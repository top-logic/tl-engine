/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.DeletedObjects;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.execution.ExecutableState;

/**
 * An {@link ViewExecutabilityRule executability rule} applied to the {@link CommandArguments
 * arguments} read from input channels, followed live while the UI it decides for is displayed.
 *
 * <p>
 * The {@link #getState() state} is the rule's answer for the values the input channels hold now. The
 * arguments are built from the channel values the way a command builds them when it runs (see
 * {@link CommandArguments#arguments(Object, List)}); a trigger supplying a value of its own has
 * supplied none yet, its argument is {@code null}. A caller supplying the trigger's value - the row
 * of a table, say - asks {@link #getState(Object)}, which builds the arguments the same way.
 * </p>
 *
 * <p>
 * The rule never sees a deleted object: an argument object that is no longer
 * {@link TLObject#tValid() valid} counts as no value at all, and the deleted members of a
 * collection are left out (see {@link DeletedObjects#withoutDeleted(Object)}). A channel declared by
 * a view drops a deleted object itself while the view is displayed, but an input supplied by the
 * caller, or a channel nobody attached, may still hold one - and nothing can be decided over an
 * object that is gone.
 * </p>
 *
 * <p>
 * While {@link #attach(ModelScope) attached}, every event after which the rule may answer
 * differently runs the change callback given at construction:
 * </p>
 * <ul>
 * <li>any of the input channels taking a new value - also the value an attached channel announces
 * when it drops a deleted object, which disables what the rule decides for,</li>
 * <li>an object such a value holds being changed, or an object of one of the
 * {@link ExecutabilityConfig#getObservedTypes() observed types} being created, changed or deleted
 * (see {@link ChannelObjectObserver}),</li>
 * <li>a rule deciding by more than its arguments reporting a change of its own
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

	private final CommandArguments _arguments;

	private final List<ViewChannel> _inputChannels;

	private final Runnable _onChange;

	/**
	 * The registrations reporting a new value of an {@link #_inputChannels input channel} to the
	 * change callback, while attached.
	 */
	private final List<Registration> _inputRegistrations = new ArrayList<>();

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
	 *        The rule deciding over the arguments, already {@link ContextDependentRule#bind bound}
	 *        to its context where it needs one.
	 * @param arguments
	 *        The names of the arguments and how they are built from the channel values.
	 * @param inputChannels
	 *        The channels of the {@link CommandArguments#getInputs() inputs}, in their order; empty
	 *        where they cannot be resolved, the inputs are then {@code null}.
	 * @param observedTypes
	 *        Types whose object changes let the rule decide anew in addition to the input objects,
	 *        see {@link ObservedTypes#resolve(List)}; empty observes just the input objects.
	 * @param onChange
	 *        Run while attached whenever the rule may answer differently than before.
	 */
	public LiveExecutability(ViewExecutabilityRule rule, CommandArguments arguments,
			List<ViewChannel> inputChannels, Set<TLStructuredType> observedTypes, Runnable onChange) {
		_rule = rule;
		_arguments = arguments;
		_inputChannels = List.copyOf(inputChannels);
		_onChange = onChange;
		_inputObserver = new ChannelObjectObserver(_inputChannels, observedTypes, onChange);
	}

	/**
	 * Creates the {@link LiveExecutability} an {@link InputExecutabilityConfig} declares: its rules
	 * {@link ViewExecutabilityRules#build(List, ViewContext, ActionScope) built} for the given
	 * context, applied to the value of its input under the input's name, followed together with its
	 * observed types.
	 *
	 * @param config
	 *        The declaration of the rules, their input and their observed types.
	 * @param context
	 *        The build-time context the rules are bound to and the input channel is resolved in.
	 * @param onChange
	 *        Run while attached whenever the rules may answer differently than before.
	 */
	public static LiveExecutability create(InputExecutabilityConfig config, ViewContext context,
			Runnable onChange) {
		CommandArguments arguments = InputExecutabilityConfig.arguments(config);
		List<ViewChannel> channels = new ArrayList<>();
		for (InputRef input : arguments.getInputs()) {
			channels.add(context.resolveChannel(input.getChannel()));
		}
		return new LiveExecutability(
			ViewExecutabilityRules.build(config.getExecutability(), context, arguments.scope()),
			arguments,
			channels,
			ObservedTypes.resolve(config.getObservedTypes()),
			onChange);
	}

	/**
	 * The rule deciding over the arguments.
	 */
	public ViewExecutabilityRule getRule() {
		return _rule;
	}

	/**
	 * The current values of the input channels, in their order, without the objects deleted
	 * meanwhile (see {@link DeletedObjects#withoutDeleted(Object)}).
	 */
	public List<Object> inputValues() {
		List<Object> result = new ArrayList<>(_inputChannels.size());
		for (ViewChannel channel : _inputChannels) {
			result.add(DeletedObjects.withoutDeleted(channel.get()));
		}
		return result;
	}

	/**
	 * The current value of the main argument as far as the channels define it: the value of the
	 * first input, unless a trigger supplies the main argument, whose value is not known here.
	 *
	 * @return The value, {@code null} for a trigger supplying the main argument, or without inputs.
	 */
	public Object getInput() {
		if (_arguments.getTrigger() != null || _inputChannels.isEmpty()) {
			return null;
		}
		return DeletedObjects.withoutDeleted(_inputChannels.get(0).get());
	}

	/**
	 * The arguments for the given main argument and the current {@link #inputValues() values of the
	 * input channels}.
	 *
	 * @param main
	 *        The value of the main argument, see {@link CommandArguments#arguments(Object, List)}.
	 */
	public ActionArguments arguments(Object main) {
		return _arguments.arguments(main, inputValues());
	}

	/**
	 * The state the rule assigns to the current values of the input channels, evaluated anew on
	 * every call.
	 *
	 * <p>
	 * The value a trigger supplies is not known here, its argument is {@code null}.
	 * </p>
	 */
	public ExecutableState getState() {
		return getState(getInput());
	}

	/**
	 * The state the rule assigns to the arguments of a run with the given main argument - the value
	 * a trigger supplies, a single row of a table, say.
	 *
	 * @param main
	 *        The value of the main argument, see {@link #arguments(Object)}; the rule sees it
	 *        without the objects deleted meanwhile.
	 */
	public ExecutableState getState(Object main) {
		return _rule.isExecutableFor(arguments(DeletedObjects.withoutDeleted(main)));
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
	 *        browser window, which follows the channel values and the rule alone.
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		for (ViewChannel channel : _inputChannels) {
			_inputRegistrations.add(channel.addListener((sender, oldValue, newValue) -> _onChange.run()));
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
		_inputRegistrations.forEach(Registration::dispose);
		_inputRegistrations.clear();
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
