/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Runtime bridge between a stateless {@link ViewCommand} and its UI button.
 *
 * <p>
 * Created by the panel at setup time, one per command. Subscribes to the input channel and
 * re-evaluates executability when the input changes.
 * </p>
 */
public class ViewCommandModel implements ViewChannel.ChannelListener, CommandModel {

	private final ViewCommand _command;

	private final ViewCommand.Config _config;

	private final ViewChannel _inputChannel;

	private final ViewExecutabilityRule _rule;

	private final ViewCommandConfirmation _confirmation;

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
	 * @param confirmation
	 *        The confirmation strategy (may be {@code null} if no confirmation needed).
	 */
	public ViewCommandModel(ViewCommand command, ViewCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule, ViewCommandConfirmation confirmation) {
		_command = command;
		_config = config;
		_inputChannel = inputChannel;
		_rule = rule;
		_confirmation = confirmation;
		_executableState = ExecutableState.EXECUTABLE;
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
		return ResourcesModule.getInstance()
			.getBundle(Locale.getDefault())
			.getString(key);
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

	/**
	 * The command's CSS classes.
	 */
	public String getCssClasses() {
		return _config.getCssClasses();
	}

	@Override
	public String getPlacement() {
		CommandPlacement placement = _config.getPlacement();
		if (placement == null) {
			return PLACEMENT_NONE;
		}
		switch (placement) {
			case TOOLBAR:
				return PLACEMENT_TOOLBAR;
			case BUTTON_BAR:
				return PLACEMENT_BUTTON_BAR;
			case CONTEXT_MENU:
				return PLACEMENT_CONTEXT_MENU;
			default:
				return PLACEMENT_NONE;
		}
	}

	/**
	 * The command's clique.
	 */
	public String getClique() {
		return _config.getClique();
	}

	@Override
	public String getName() {
		return _config.getName();
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
		Object input = resolveInput();

		ExecutableState state = _rule.isExecutable(input);
		if (!state.isExecutable()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// TODO: dirty check (DirtyCheckScope from config)

		if (_confirmation != null) {
			ResKey confirmKey = _confirmation.getConfirmation(_config.getLabel(), input);
			if (confirmKey != null) {
				// TODO: show confirmation dialog, resume on OK
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		return _command.execute(context, input);
	}

	/**
	 * Subscribe to input channel changes and update executability state.
	 */
	public void attach() {
		if (_inputChannel != null) {
			_inputChannel.addListener(this);
		}
		updateExecutableState();
	}

	/**
	 * Unsubscribe from input channel changes.
	 */
	public void detach() {
		if (_inputChannel != null) {
			_inputChannel.removeListener(this);
		}
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
