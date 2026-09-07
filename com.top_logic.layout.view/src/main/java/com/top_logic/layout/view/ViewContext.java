/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.slot.SlotPath;
import com.top_logic.layout.view.slot.SlotRegistry;

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
	 * The scope of the given type established by an enclosing element via
	 * {@link #withScope(Class, Object)}, or {@code null} if none is in scope.
	 *
	 * <p>
	 * A generic, type-keyed registry for the scoped services an enclosing element makes available to
	 * its descendants (e.g. {@link com.top_logic.layout.view.command.CommandScope}, a security scope,
	 * an object-list scope). Application code can introduce its own scope types this way without
	 * extending this interface.
	 * </p>
	 *
	 * @param type
	 *        The scope type used as the registry key.
	 */
	<S> S getScope(Class<S> type);

	/**
	 * Creates a derived context with the given scope registered under the given type.
	 *
	 * <p>
	 * Descendant contexts inherit the scope; a nested {@link #withScope(Class, Object)} for the same
	 * type overrides (does not stack) it.
	 * </p>
	 *
	 * @param type
	 *        The scope type used as the registry key, typically the scope's interface.
	 * @param scope
	 *        The scope instance to establish for descendants.
	 * @return A new context with the scope registered.
	 */
	<S> ViewContext withScope(Class<S> type, S scope);

	/**
	 * The {@link FormModel} of the nearest enclosing form element, or {@code null} if no form is in
	 * scope.
	 */
	FormModel getFormModel();

	/**
	 * Sets the form model for this context.
	 *
	 * <p>
	 * Called by {@link com.top_logic.layout.view.element.FormElement} during
	 * {@link UIElement#createControl(ViewContext)} to make the form available to nested field
	 * elements.
	 * </p>
	 *
	 * @param formModel
	 *        The form model, or {@code null}.
	 */
	void setFormModel(FormModel formModel);

	/**
	 * The {@link DirtyChannel} of the nearest enclosing scope, or {@code null} if no dirty tracking
	 * is configured.
	 *
	 * <p>
	 * {@link com.top_logic.layout.view.form.StateHandler} implementations use this to register
	 * their dirty state so that enclosing containers (e.g. tab bars) can check for unsaved changes
	 * before navigation.
	 * </p>
	 *
	 * @see DirtyChannel
	 */
	DirtyChannel getDirtyChannel();

	/**
	 * Sets the dirty channel for this context scope.
	 *
	 * <p>
	 * Called by container elements (e.g. tab bars) that want to track dirty state of their children.
	 * </p>
	 *
	 * @param dirtyChannel
	 *        The dirty channel, or {@code null}.
	 */
	void setDirtyChannel(DirtyChannel dirtyChannel);

	/**
	 * The {@link ContextMenuOpener} of the nearest enclosing frame, or {@code null} if no opener is
	 * in scope.
	 *
	 * <p>
	 * Exactly one opener is instantiated per top-level frame (at the root {@link ViewElement}) and
	 * bound onto the context before children are created. Child elements dispatch context-menu
	 * requests via this opener.
	 * </p>
	 */
	@Override
	ContextMenuOpener getContextMenuOpener();

	/**
	 * Creates a derived context with the given {@link ContextMenuOpener}.
	 *
	 * <p>
	 * Called by the root {@link ViewElement} to install the frame's opener for all descendant
	 * elements.
	 * </p>
	 *
	 * @param opener
	 *        The context menu opener to set.
	 * @return A new context with the given opener but same display context, personalization path,
	 *         channels, and command scope.
	 */
	ViewContext withContextMenuOpener(ContextMenuOpener opener);

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
	 * The current position in the rendered view tree, used by the slot mechanism to route
	 * {@code <slot-content>} contributions to the nearest matching {@code <slot>} placeholder.
	 *
	 * <p>
	 * Containers extend the path by one segment per child via {@link #withChildSlotPath(String)}.
	 * Containers that do not extend it have all children share the same path, which is fine when
	 * the slot mechanism does not need to distinguish their positions.
	 * </p>
	 */
	SlotPath getSlotPath();

	/**
	 * The shared slot registry for this view tree.
	 *
	 * <p>
	 * One registry per root {@link ViewContext}. Child contexts inherit the same registry, so
	 * {@code <slot>} and {@code <slot-content>} anywhere in the tree see each other.
	 * </p>
	 */
	SlotRegistry getSlotRegistry();

	/**
	 * Creates a child context with the {@link #getSlotPath() slot path} extended by one segment.
	 *
	 * <p>
	 * Called by container elements once per child when building their child controls, so that two
	 * sibling children get distinct positions in the slot routing tree.
	 * </p>
	 *
	 * @param segment
	 *        Segment identifier (typically the child index as a string, but any unique-per-parent
	 *        string works).
	 * @return A new context with the extended slot path.
	 */
	ViewContext withChildSlotPath(String segment);

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
	 * Creates a derived context whose channel namespace is a copy of this context's namespace with
	 * the given channel added (replacing a same-named channel from the enclosing scope).
	 *
	 * <p>
	 * Called by elements that instantiate a content template once per model object (e.g.
	 * {@link com.top_logic.layout.view.list.ObjectListElement &lt;object-list&gt;}): each instance
	 * gets its own context where the item channel resolves to that instance's object, while all
	 * channels of the enclosing scope stay visible. The local channel does not leak into the
	 * enclosing scope.
	 * </p>
	 *
	 * @param name
	 *        The local channel name.
	 * @param channel
	 *        The channel instance visible only in the derived context (and its children).
	 * @return A new context with the extended channel namespace.
	 */
	ViewContext withLocalChannel(String name, ViewChannel channel);

	/**
	 * Creates a derived context for an embedded view: a fresh, empty channel namespace and a fresh
	 * form model, while all other ambient scopes (registered {@link #withScope(Class, Object) scopes},
	 * error sink, dirty channel, {@link #getSlotPath() slot path} and personalization key) are
	 * inherited.
	 *
	 * <p>
	 * This is the shared seam for instantiating a self-contained view with its own channel scope:
	 * used by {@link ReferenceElement &lt;view-ref&gt;} for each embedded view, and reached by an
	 * {@link com.top_logic.layout.view.list.ObjectListElement &lt;object-list&gt;} row whose content
	 * is a {@code <view-ref>}. Inheriting the slot path keeps distinct positions (hence distinct
	 * personalization) for multiple instances; inheriting the object list scope keeps
	 * {@link com.top_logic.layout.view.list.RemoveElementAction &lt;remove-element&gt;} and
	 * {@link com.top_logic.layout.view.list.LinkElementAction &lt;link-element&gt;} working inside a
	 * referenced row view. Channels shared with the embedded view are established afterwards via
	 * {@link #registerChannel(String, ViewChannel)}.
	 * </p>
	 *
	 * @return A new context with an isolated channel namespace.
	 */
	ViewContext withIsolatedChannels();

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

	/**
	 * Registers a listener that is notified when view files change.
	 *
	 * <p>
	 * Listeners are always registered at the root context. Child contexts delegate registration
	 * upward so that a single {@link #fireViewChanged(Set)} call reaches all listeners in the
	 * hierarchy.
	 * </p>
	 *
	 * @param listener
	 *        The listener to register.
	 *
	 * @see #removeViewReloadListener(ViewReloadListener)
	 * @see #fireViewChanged(Set)
	 */
	void addViewReloadListener(ViewReloadListener listener);

	/**
	 * Removes a previously registered {@link ViewReloadListener}.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeViewReloadListener(ViewReloadListener listener);

	/**
	 * Notifies all registered {@link ViewReloadListener}s that the given view files have changed.
	 *
	 * <p>
	 * Called by the designer's save command after writing modified {@code .view.xml} files to disk.
	 * Child contexts delegate this call to the root context.
	 * </p>
	 *
	 * @param changedPaths
	 *        The set of changed view file paths.
	 */
	void fireViewChanged(Set<String> changedPaths);
}
