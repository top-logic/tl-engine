/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ContextMenuOpener;
import com.top_logic.layout.view.form.FormModel;

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

	private FormModel _formModel;

	private final List<ViewReloadListener> _reloadListeners;

	private DirtyChannel _dirtyChannel;

	private final ContextMenuOpener _contextMenuOpener;

	/**
	 * Creates a root {@link DefaultViewContext}.
	 *
	 * @param reactContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public DefaultViewContext(ReactContext reactContext) {
		this(reactContext, "view", new HashMap<>(), null, null, null, null,
			resolveReloadListeners(reactContext), null);
	}

	private static List<ViewReloadListener> resolveReloadListeners(ReactContext reactContext) {
		if (reactContext instanceof DefaultViewContext dvc) {
			return dvc._reloadListeners;
		}
		return new ArrayList<>();
	}

	private DefaultViewContext(ReactContext reactContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormModel formModel,
			ErrorSink errorSink, DirtyChannel dirtyChannel, List<ViewReloadListener> reloadListeners,
			ContextMenuOpener contextMenuOpener) {
		_reactContext = reactContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formModel = formModel;
		_errorSink = errorSink;
		_dirtyChannel = dirtyChannel;
		_reloadListeners = reloadListeners;
		_contextMenuOpener = contextMenuOpener;
	}

	@Override
	public ViewContext childContext(String segment) {
		return new DefaultViewContext(_reactContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formModel, _errorSink, _dirtyChannel, _reloadListeners, _contextMenuOpener);
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
	public FormModel getFormModel() {
		return _formModel;
	}

	@Override
	public void setFormModel(FormModel formModel) {
		_formModel = formModel;
	}

	@Override
	public DirtyChannel getDirtyChannel() {
		return _dirtyChannel;
	}

	@Override
	public void setDirtyChannel(DirtyChannel dirtyChannel) {
		_dirtyChannel = dirtyChannel;
	}

	@Override
	public ViewContext withCommandScope(CommandScope scope) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels, scope, _formModel, _errorSink,
			_dirtyChannel, _reloadListeners, _contextMenuOpener);
	}

	@Override
	public ViewContext withErrorSink(ErrorSink errorSink) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels,
			_commandScope, _formModel, errorSink, _dirtyChannel, _reloadListeners, _contextMenuOpener);
	}

	@Override
	public ContextMenuOpener getContextMenuOpener() {
		if (_contextMenuOpener != null) {
			return _contextMenuOpener;
		}
		return _reactContext == null ? null : (ContextMenuOpener) _reactContext.getContextMenuOpener();
	}

	@Override
	public ViewContext withContextMenuOpener(ContextMenuOpener opener) {
		return new DefaultViewContext(_reactContext, _personalizationPath, _channels, _commandScope, _formModel,
			_errorSink, _dirtyChannel, _reloadListeners, opener);
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

	@Override
	public ReactWindowRegistry getWindowRegistry() {
		return _reactContext.getWindowRegistry();
	}

	@Override
	public com.top_logic.model.listen.ModelScope getModelScope() {
		return _reactContext.getModelScope();
	}

	@Override
	public void addViewReloadListener(ViewReloadListener listener) {
		_reloadListeners.add(listener);
	}

	@Override
	public void removeViewReloadListener(ViewReloadListener listener) {
		_reloadListeners.remove(listener);
	}

	@Override
	public void fireViewChanged(Set<String> changedPaths) {
		for (ViewReloadListener listener : List.copyOf(_reloadListeners)) {
			listener.viewChanged(changedPaths);
		}
	}
}
