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
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.layout.view.slot.SlotPath;
import com.top_logic.layout.view.slot.SlotRegistry;
import com.top_logic.layout.view.tiles.TileStackScope;

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
	 * The {@link SecurityScope} established by the nearest enclosing removable unit that carries an
	 * {@code <access-control>}, or {@code null} if none is in scope.
	 *
	 * <p>
	 * Command-level security rules
	 * ({@link com.top_logic.layout.view.security.SecurityRule}) default to this scope when they do
	 * not name one explicitly, mirroring the legacy "one {@code PersBoundComp}, many command groups"
	 * model: the command shares the role mapping of the unit that already gates its visibility.
	 * </p>
	 */
	SecurityScope getSecurityScope();

	/**
	 * Creates a derived context whose {@link #getSecurityScope() security scope} is the given scope.
	 *
	 * <p>
	 * Called by removable elements (nav-item, tab, tile) carrying an {@code <access-control>} when
	 * building their content subtree, so that command rules built underneath default to the unit's
	 * scope. Nested units override (not stack) the scope.
	 * </p>
	 *
	 * @param scope
	 *        The security scope to establish for descendants.
	 * @return A new context with the given security scope.
	 */
	ViewContext withSecurityScope(SecurityScope scope);

	/**
	 * The {@link TileStackScope} of the enclosing
	 * {@link com.top_logic.layout.view.tiles.TileStackElement &lt;tile-stack&gt;}, or
	 * {@code null} if no tile stack is in scope.
	 *
	 * <p>
	 * Used by {@link com.top_logic.layout.view.tiles.NavigatePushCommand &lt;navigate-push&gt;} to
	 * resolve the target stack from within a mounted frame.
	 * </p>
	 */
	TileStackScope getTileStackScope();

	/**
	 * Creates a derived context with the given {@link TileStackScope}.
	 *
	 * <p>
	 * Called by {@link com.top_logic.layout.view.tiles.TileStackElement} when mounting a frame to
	 * make the stack reachable for descendants. Nested stacks deliberately override (not inherit)
	 * the scope - {@code <navigate-push>} from inside a frame always targets the nearest
	 * enclosing stack.
	 * </p>
	 *
	 * @param scope
	 *        The tile stack scope to set.
	 * @return A new context with the given scope.
	 */
	ViewContext withTileStackScope(TileStackScope scope);

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
	 * Creates a derived context with a fresh, empty channel registry.
	 *
	 * <p>
	 * Channels declared in the new context are independent of channels declared in the parent
	 * context. Used by container elements whose children are logically independent — e.g.
	 * tab-bars — so that two sibling subtrees declaring the same channel name do not accidentally
	 * share one channel instance.
	 * </p>
	 *
	 * @return A new context with an empty channels map but otherwise the same fields as this
	 *         context.
	 */
	ViewContext withFreshChannelScope();

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
