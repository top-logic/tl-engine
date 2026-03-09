/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormControl;

/**
 * Default implementation of {@link ViewContext}.
 *
 * <p>
 * Provides the standard hierarchical context for UIElement control creation. Container elements may
 * create derived contexts that add scoped information for their children via
 * {@link #childContext(String)} and {@link #withCommandScope(CommandScope)}.
 * </p>
 */
public class DefaultViewContext implements ViewContext {

	private final ReactContext _reactContext;

	private final String _personalizationPath;

	private final Map<String, ViewChannel> _channels;

	private final CommandScope _commandScope;

	private final ErrorSink _errorSink;

	private FormControl _formControl;

	/**
	 * Creates a root {@link DefaultViewContext}.
	 *
	 * @param reactContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public DefaultViewContext(ReactContext reactContext) {
		this(reactContext, "view", new HashMap<>(), null, null, null);
	}

	private DefaultViewContext(ReactContext reactContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormControl formControl,
			ErrorSink errorSink) {
		_reactContext = reactContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formControl = formControl;
		_errorSink = errorSink;
	}

	@Override
	public ViewContext childContext(String segment) {
		return new DefaultViewContext(_reactContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formControl, _errorSink);
	}

	@Override
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	@Override
	public CommandScope getCommandScope() {
		return _commandScope;
	}

	@Override
	public FormControl getFormControl() {
		return _formControl;
	}

	@Override
	public void setFormControl(FormControl formControl) {
		_formControl = formControl;
	}

	@Override
	public ViewContext withCommandScope(CommandScope scope) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels, scope, _formControl, _errorSink);
	}

	@Override
	public ViewContext withErrorSink(ErrorSink errorSink) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels,
			_commandScope, _formControl, errorSink);
	}

	@Override
	public ErrorSink getErrorSink() {
		return _errorSink;
	}

	@Override
	public void registerChannel(String name, ViewChannel channel) {
		ViewChannel existing = _channels.put(name, channel);
		if (existing != null) {
			_channels.put(name, existing);
			throw new IllegalArgumentException("Duplicate channel name: '" + name + "'");
		}
	}

	@Override
	public boolean hasChannel(String name) {
		return _channels.containsKey(name);
	}

	@Override
	public ViewChannel resolveChannel(ChannelRef ref) {
		ViewChannel channel = _channels.get(ref.getChannelName());
		if (channel == null) {
			throw new IllegalArgumentException(
				"Unknown channel: '" + ref.getChannelName() + "'. "
					+ "Available channels: " + _channels.keySet());
		}
		return channel;
	}

	@Override
	public String allocateId() {
		return _reactContext.allocateId();
	}

	@Override
	public String getWindowName() {
		return _reactContext.getWindowName();
	}

	@Override
	public String getContextPath() {
		return _reactContext.getContextPath();
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _reactContext.getSSEQueue();
	}
}
