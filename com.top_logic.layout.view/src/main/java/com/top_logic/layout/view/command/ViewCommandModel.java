/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Runtime bridge between a stateless {@link ViewCommand} and the UI running it - its button, or a
 * gesture like a table row activation that supplies the input itself
 * ({@link #execute(ReactContext, Object)}).
 *
 * <p>
 * Created by the panel at setup time, one per command. While
 * {@link #attach(ModelScope) attached}, the model follows its input in both directions a rule can
 * decide by: the {@link ViewCommand.Config#getInput() input channel} taking a new value, and the
 * object that value points to being edited. A rule testing an attribute of the input object -
 * a workflow command offered only while a ticket is open, say - therefore re-evaluates when that
 * attribute is stored, although the channel keeps pointing to the same object.
 * </p>
 *
 * @see ChannelObjectObserver
 */
public class ViewCommandModel implements ViewChannel.ChannelListener, CommandModel {

	private final ViewCommand _command;

	private final ViewCommand.Config _config;

	private final ViewChannel _inputChannel;

	private final ViewExecutabilityRule _rule;

	private final ChannelObjectObserver _inputObserver;

	private ExecutableState _executableState;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	/**
	 * Creates a new {@link ViewCommandModel}.
	 *
	 * @param command
	 *        The stateless command handler.
	 * @param config
	 *        The command configuration (provides label, image, placement, etc.).
	 * @param inputChannel
	 *        The resolved input channel (may be {@code null} if no input configured).
	 * @param rule
	 *        The combined executability rule.
	 */
	public ViewCommandModel(ViewCommand command, ViewCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule) {
		_command = command;
		_config = config;
		_inputChannel = inputChannel;
		_rule = rule;
		_executableState = ExecutableState.EXECUTABLE;

		List<ViewChannel> observedChannels = inputChannel == null ? List.of() : List.of(inputChannel);
		Set<TLStructuredType> observedTypes = ObservedTypes.resolve(config.getObservedTypes());
		_inputObserver = new ChannelObjectObserver(observedChannels, observedTypes, this::updateExecutableState);
	}

	/**
	 * Creates the {@link ViewCommandModel} matching the given command, choosing a specialized model
	 * for commands that need one (e.g. a {@link ViewUploadCommandModel} for an {@link UploadCommand},
	 * whose button uploads files instead of dispatching a click command).
	 */
	public static ViewCommandModel create(ViewCommand command, ViewCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule) {
		if (config instanceof UploadCommand.Config) {
			return new ViewUploadCommandModel((UploadCommand) command, (UploadCommand.Config) config, inputChannel,
				rule);
		}
		return new ViewCommandModel(command, config, inputChannel, rule);
	}

	/**
	 * Creates the {@link ViewCommandModel} for a command an element has instantiated from its
	 * configuration: resolves the command's {@link ViewCommand.Config#getInput() input channel} in
	 * the given context, builds its executability rule there, and picks the matching model.
	 *
	 * <p>
	 * This is the one construction path for a configured command, shared by every element that
	 * hosts commands. An element that has to interfere with the rule - the form, which additionally
	 * disables the commands its validation would reject - builds the model from
	 * {@link #create(ViewCommand, ViewCommand.Config, ViewChannel, ViewExecutabilityRule)} with the
	 * rule it composed.
	 * </p>
	 *
	 * @param context
	 *        The build-time context of the hosting element, resolving the input channel and binding
	 *        the rules.
	 * @param command
	 *        The instantiated command.
	 * @param config
	 *        The configuration the command was instantiated from.
	 */
	public static ViewCommandModel forCommand(ViewContext context, ViewCommand command,
			ViewCommand.Config config) {
		ChannelRef inputRef = config.getInput();
		ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;
		ViewExecutabilityRule rule = ViewExecutabilityRules.build(config.getExecutability(), context);
		return create(command, config, inputChannel, rule);
	}

	/**
	 * Resolves the current input value from the channel.
	 */
	public Object resolveInput() {
		return _inputChannel != null ? _inputChannel.get() : null;
	}

	@Override
	public String getLabel() {
		ResKey key = _config.getLabel();
		if (key == null) {
			return "";
		}
		return Resources.getInstance().getString(key);
	}

	/**
	 * The command's label key.
	 */
	public ResKey getLabelKey() {
		return _config.getLabel();
	}

	@Override
	public ThemeImage getImage() {
		return _config.getImage();
	}

	@Override
	public String getTooltip() {
		ResKey key = _config.getTooltip();
		if (key == null) {
			return null;
		}
		return Resources.getInstance().getString(key);
	}

	@Override
	public String getCssClasses() {
		return _config.getCssClasses();
	}

	@Override
	public CommandPlacement getPlacement() {
		CommandPlacement placement = _config.getPlacement();
		return placement == null ? CommandPlacement.NONE : placement;
	}

	@Override
	public ButtonDisplayMode getDisplayMode() {
		return _config.getDisplay();
	}

	/**
	 * The command's clique.
	 */
	@Override
	public String getClique() {
		return _config.getClique();
	}

	@Override
	public String getName() {
		return _config.getName();
	}

	@Override
	public KeyStroke getKeyGesture() {
		return _config.getKey();
	}

	/**
	 * The current executability state.
	 */
	public ExecutableState getExecutableState() {
		return _executableState;
	}

	@Override
	public boolean isExecutable() {
		return _executableState.isExecutable();
	}

	@Override
	public boolean isVisible() {
		return _executableState.isVisible();
	}

	@Override
	public HandlerResult executeCommand(ReactContext context) {
		return execute(context, resolveInput());
	}

	/**
	 * Executes the command with an input the caller supplies instead of the
	 * {@link #resolveInput() channel value} - the row a table activation opens, say.
	 *
	 * <p>
	 * The command's executability rules decide over that same input, so a rule that rejects it
	 * makes the call a no-op.
	 * </p>
	 *
	 * @param context
	 *        The context the command executes in.
	 * @param input
	 *        The command's input value.
	 * @return The command's result, {@link HandlerResult#DEFAULT_RESULT} when the rules reject the
	 *         input.
	 */
	public HandlerResult execute(ReactContext context, Object input) {
		ExecutableState state = _rule.isExecutable(input);
		if (!state.isExecutable()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// TODO: dirty check (DirtyCheckScope from config)

		// Confirmation is a chain concern: place a <confirm> guard in the command's action chain
		// (see ConfirmAction), which can suspend/resume the chain and inspect already-stored form
		// state - rather than gating the whole command here.
		return _command.execute(context, input);
	}

	/**
	 * Begins following the input - the channel's value and the object that value holds - and
	 * evaluates the executability for what the input currently is.
	 *
	 * @param scope
	 *        The scope the object observation registers on, {@code null} for a button built outside
	 *        a browser window, which follows the channel value alone.
	 */
	public void attach(ModelScope scope) {
		if (_inputChannel != null) {
			_inputChannel.addListener(this);
		}
		_inputObserver.attach(scope);
		updateExecutableState();
	}

	/**
	 * Re-evaluates the executability, e.g. after state a rule inspects changed without the input
	 * channel changing.
	 */
	public void revalidate() {
		updateExecutableState();
	}

	/**
	 * Stops following the input.
	 */
	public void detach() {
		if (_inputChannel != null) {
			_inputChannel.removeListener(this);
		}
		_inputObserver.detach();
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		updateExecutableState();
	}

	private void updateExecutableState() {
		Object input = resolveInput();
		ExecutableState newState = _rule.isExecutable(input);
		if (newState.visibility() != _executableState.visibility()) {
			_executableState = newState;
			fireStateChanged();
		}
	}

	private void fireStateChanged() {
		for (Runnable listener : _stateChangeListeners) {
			listener.run();
		}
	}
}
