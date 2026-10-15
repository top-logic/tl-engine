/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;

/**
 * The names of the {@link ActionArguments arguments} a command runs its actions with, and how they
 * are filled.
 *
 * <p>
 * Each {@link NamedInputs input} of the command is an argument named after its channel or its
 * {@code as} name. A trigger supplying a value of its own - the row of a table activation, the
 * uploaded file - adds it as the <em>main argument</em> under the name the trigger gives it.
 * Without such a trigger, the first input is the main argument, and a command with neither has the
 * single argument {@link #DEFAULT_ARGUMENT}. The main argument is the default argument of the
 * actions and the input the executability rules of the command decide over.
 * </p>
 */
public final class CommandArguments {

	/**
	 * Name of the main argument of a command with neither inputs nor a trigger supplying a value.
	 */
	public static final String DEFAULT_ARGUMENT = "input";

	private final List<InputRef> _inputs;

	private final String _trigger;

	/**
	 * Creates {@link CommandArguments}.
	 *
	 * @param inputs
	 *        The inputs of the command.
	 * @param trigger
	 *        The name of the value the trigger of the command supplies, {@code null} for a trigger
	 *        supplying none (e.g. a button).
	 */
	public CommandArguments(List<InputRef> inputs, String trigger) {
		_inputs = List.copyOf(inputs);
		_trigger = trigger == null || trigger.isBlank() ? null : trigger.trim();
	}

	/**
	 * The {@link CommandArguments} of a command with the given configuration.
	 *
	 * @param config
	 *        The configuration of the command.
	 * @param trigger
	 *        The scope the trigger of the command supplies: its default name is the name of the
	 *        trigger's value; {@link ActionScope#NONE} for a trigger supplying none.
	 */
	public static CommandArguments of(ViewCommand.Config config, ActionScope trigger) {
		return new CommandArguments(config.getInputs(), config.triggerName(trigger));
	}

	/**
	 * The inputs of the command.
	 */
	public List<InputRef> getInputs() {
		return _inputs;
	}

	/**
	 * The name of the value the trigger supplies, {@code null} if it supplies none.
	 */
	public String getTrigger() {
		return _trigger;
	}

	/**
	 * The name of the main argument.
	 */
	public String mainName() {
		if (_trigger != null) {
			return _trigger;
		}
		if (!_inputs.isEmpty()) {
			return _inputs.get(0).getName();
		}
		return DEFAULT_ARGUMENT;
	}

	/**
	 * The scope the actions of the command are instantiated in.
	 */
	public ActionScope scope() {
		return ActionScope.of(names(), mainName());
	}

	private List<String> names() {
		List<String> names = InputRef.names(_inputs);
		if (_trigger != null || _inputs.isEmpty()) {
			names.add(mainName());
		}
		return names;
	}

	/**
	 * The arguments of a run of the command.
	 *
	 * @param main
	 *        The value of the main argument: the value the trigger supplies, otherwise the value of
	 *        the first input.
	 * @param inputValues
	 *        The values of the inputs, in their order; a missing value is {@code null}. Without a
	 *        trigger supplying a value, the value of the first input is taken from {@code main}.
	 */
	public ActionArguments arguments(Object main, List<?> inputValues) {
		List<Object> values = new ArrayList<>(_inputs.size() + 1);
		for (int n = 0, cnt = _inputs.size(); n < cnt; n++) {
			if (n == 0 && _trigger == null) {
				values.add(main);
			} else {
				values.add(n < inputValues.size() ? inputValues.get(n) : null);
			}
		}
		if (_trigger != null || _inputs.isEmpty()) {
			values.add(main);
		}
		return ActionArguments.of(names(), values, mainName());
	}

	/**
	 * The arguments of a run of the command, the values of the inputs read from the given context.
	 *
	 * @param context
	 *        The context the command runs in; the inputs are read from it when it is a
	 *        {@link ViewContext} that knows their channels, otherwise they are {@code null}.
	 * @param main
	 *        See {@link #arguments(Object, List)}.
	 */
	public ActionArguments arguments(ReactContext context, Object main) {
		List<Object> values = new ArrayList<>(_inputs.size());
		for (InputRef input : _inputs) {
			ChannelRef channel = input.getChannel();
			if (context instanceof ViewContext viewContext && viewContext.hasChannel(channel.getChannelName())) {
				values.add(viewContext.resolveChannel(channel).get());
			} else {
				values.add(null);
			}
		}
		return arguments(main, values);
	}
}
