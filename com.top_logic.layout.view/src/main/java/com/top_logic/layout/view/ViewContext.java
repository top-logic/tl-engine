/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormControl;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements may
 * create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final ViewDisplayContext _displayContext;

	private final String _personalizationPath;

	private final Map<String, ViewChannel> _channels;

	private final CommandScope _commandScope;

	private FormControl _formControl;

	/**
	 * Creates a root {@link ViewContext}.
	 *
	 * @param displayContext
	 *        The view display context providing ID allocation, SSE queue and other rendering
	 *        infrastructure.
	 */
	public ViewContext(ViewDisplayContext displayContext) {
		this(displayContext, "view", new HashMap<>(), null, null);
	}

	private ViewContext(ViewDisplayContext displayContext, String personalizationPath,
			Map<String, ViewChannel> channels, CommandScope commandScope, FormControl formControl) {
		_displayContext = displayContext;
		_personalizationPath = personalizationPath;
		_channels = channels;
		_commandScope = commandScope;
		_formControl = formControl;
	}

	/**
	 * Creates a child context with an appended path segment.
	 *
	 * <p>
	 * The child context shares the same channel registry as the parent.
	 * </p>
	 *
	 * @param segment
	 *        The segment to append (e.g. "sidebar", "split-panel").
	 * @return A new context with the extended personalization path.
	 */
	public ViewContext childContext(String segment) {
		return new ViewContext(_displayContext, _personalizationPath + "." + segment, _channels, _commandScope,
			_formControl);
	}

	/**
	 * The personalization key for the current position in the view tree.
	 *
	 * <p>
	 * Auto-derived from the tree path (e.g. "view.sidebar", "view.split-panel"). UIElements may
	 * override this with an explicit personalization key from configuration.
	 * </p>
	 */
	public String getPersonalizationKey() {
		return _personalizationPath;
	}

	/**
	 * The {@link ViewDisplayContext} for the current session.
	 */
	public ViewDisplayContext getDisplayContext() {
		return _displayContext;
	}

	/**
	 * The {@link CommandScope} of the nearest enclosing panel, or {@code null} if no panel is in
	 * scope.
	 */
	public CommandScope getCommandScope() {
		return _commandScope;
	}

	/**
	 * The {@link FormControl} of the nearest enclosing form element, or {@code null} if no form is
	 * in scope.
	 */
	public FormControl getFormControl() {
		return _formControl;
	}

	/**
	 * Sets the form control for this context.
	 *
	 * <p>
	 * Called by {@link com.top_logic.layout.view.element.FormElement} during
	 * {@link UIElement#createControl(ViewContext)} to make the form available to nested field
	 * elements.
	 * </p>
	 *
	 * @param formControl
	 *        The form control, or {@code null}.
	 */
	public void setFormControl(FormControl formControl) {
		_formControl = formControl;
	}

	/**
	 * Creates a derived context with the given {@link CommandScope}.
	 *
	 * <p>
	 * Called by panel elements to establish their command scope for child elements.
	 * </p>
	 *
	 * @param scope
	 *        The command scope to set.
	 * @return A new context with the given command scope but same display context, personalization
	 *         path, and channels.
	 */
	public ViewContext withCommandScope(CommandScope scope) {
		return new ViewContext(_displayContext, _personalizationPath, _channels, scope, _formControl);
	}

	/**
	 * Registers a channel in this context.
	 *
	 * <p>
	 * Called by {@link ViewElement} during {@link UIElement#createControl(ViewContext)} to populate
	 * the channel registry before child elements are created.
	 * </p>
	 *
	 * @param name
	 *        The channel name.
	 * @param channel
	 *        The channel instance.
	 * @throws IllegalArgumentException
	 *         if a channel with the given name is already registered.
	 */
	public void registerChannel(String name, ViewChannel channel) {
		ViewChannel existing = _channels.put(name, channel);
		if (existing != null) {
			_channels.put(name, existing);
			throw new IllegalArgumentException("Duplicate channel name: '" + name + "'");
		}
	}

	/**
	 * Whether a channel with the given name is registered.
	 *
	 * @param name
	 *        The channel name to check.
	 * @return {@code true} if a channel with this name exists.
	 */
	public boolean hasChannel(String name) {
		return _channels.containsKey(name);
	}

	/**
	 * Resolves a {@link ChannelRef} to its runtime {@link ViewChannel}.
	 *
	 * @param ref
	 *        The channel reference to resolve.
	 * @return The channel instance.
	 * @throws IllegalArgumentException
	 *         if no channel with the referenced name exists.
	 */
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
