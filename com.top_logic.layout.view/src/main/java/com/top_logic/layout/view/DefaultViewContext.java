/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ReactDisplayContext;
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

	private final ReactDisplayContext _displayContext;

	private final String _personalizationPath;

	private final Map<String, ViewChannel> _channels;

	private final CommandScope _commandScope;

	private FormControl _formControl;

	/**
	 * Creates a root {@link DefaultViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public DefaultViewContext(ReactDisplayContext displayContext) {
		this(displayContext, "view", new HashMap<>(), null, null);
	}

	private DefaultViewContext(ReactDisplayContext displayContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormControl formControl) {
		_displayContext = displayContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formControl = formControl;
	}

	@Override
	public ViewContext childContext(String segment) {
		return new DefaultViewContext(_displayContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formControl);
	}

	@Override
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	@Override
	public ReactDisplayContext getDisplayContext() {
		return _displayContext;
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
		return new DefaultViewContext(_displayContext, _personalizationPath, _channels, scope, _formControl);
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
}
