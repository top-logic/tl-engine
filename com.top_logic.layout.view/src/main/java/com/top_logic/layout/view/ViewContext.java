/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
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
 *
 * @see DefaultViewContext
 */
public interface ViewContext extends ReactContext {

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
	ViewContext childContext(String segment);

	/**
	 * The personalization key for the current position in the view tree.
	 *
	 * <p>
	 * Auto-derived from the tree path (e.g. "view.sidebar", "view.split-panel"). UIElements may
	 * override this with an explicit personalization key from configuration.
	 * </p>
	 */
	String getPersonalizationKey();

	/**
	 * The {@link CommandScope} of the nearest enclosing panel, or {@code null} if no panel is in
	 * scope.
	 */
	CommandScope getCommandScope();

	/**
	 * The {@link FormControl} of the nearest enclosing form element, or {@code null} if no form is
	 * in scope.
	 */
	FormControl getFormControl();

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
	void setFormControl(FormControl formControl);

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
	ViewContext withCommandScope(CommandScope scope);

	/**
	 * Creates a derived context with the given {@link ErrorSink}.
	 *
	 * <p>
	 * Called by the app shell element to make its snackbar error reporting available to all
	 * descendant elements.
	 * </p>
	 *
	 * @param errorSink
	 *        The error sink for the scope.
	 * @return A new context with the given error sink.
	 */
	ViewContext withErrorSink(ErrorSink errorSink);

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
	void registerChannel(String name, ViewChannel channel);

	/**
	 * Whether a channel with the given name is registered.
	 *
	 * @param name
	 *        The channel name to check.
	 * @return {@code true} if a channel with this name exists.
	 */
	boolean hasChannel(String name);

	/**
	 * Resolves a {@link ChannelRef} to its runtime {@link ViewChannel}.
	 *
	 * @param ref
	 *        The channel reference to resolve.
	 * @return The channel instance.
	 * @throws IllegalArgumentException
	 *         if no channel with the referenced name exists.
	 */
	ViewChannel resolveChannel(ChannelRef ref);
}
