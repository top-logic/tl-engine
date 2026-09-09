/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * What a window currently displays, addressed by {@link RevealPath}.
 *
 * <p>
 * One registry per window: every view instance registers itself under the view file it displays and
 * the place it is displayed at, and every container that chooses between its children registers the
 * control revealing them. Displaying an object then walks the containers from the root display down
 * to the place its view is mounted at, and writes the object into the channels of the view instance
 * found there.
 * </p>
 *
 * <p>
 * Registrations end when the registered control is disposed. What a container has not displayed yet
 * is absent, which is why a walk asks one container at a time: revealing a child creates the
 * containers below it, and those register themselves as they are created.
 * </p>
 */
public final class RevealRegistry {

	private final Map<ViewKey, List<ViewContext>> _views = new LinkedHashMap<>();

	private final Map<ContainerKey, List<ChildRevealer>> _containers = new LinkedHashMap<>();

	/**
	 * Registers a view instance as displayed at the given place.
	 *
	 * @param viewRef
	 *        Path of the view file being displayed, relative to
	 *        {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}.
	 * @param path
	 *        The place the instance is displayed at.
	 * @param context
	 *        The context holding the instance's channels.
	 * @return The action removing the registration when the instance is disposed.
	 */
	public Runnable registerView(String viewRef, RevealPath path, ViewContext context) {
		return register(_views, new ViewKey(viewRef, path), context);
	}

	/**
	 * Registers the control that reveals the children of a container.
	 *
	 * @param container
	 *        The element choosing between its children, or {@code null} for a dialog opened on top
	 *        of the display.
	 * @param path
	 *        The place the container itself is displayed at.
	 * @param revealer
	 *        The control displaying one of the container's children.
	 * @return The action removing the registration when the control is disposed.
	 */
	public Runnable registerContainer(UIElement container, RevealPath path, ChildRevealer revealer) {
		return register(_containers, new ContainerKey(container, path), revealer);
	}

	/**
	 * The instance of the given view file displayed at the given place.
	 *
	 * @param viewRef
	 *        Path of the view file, relative to
	 *        {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}.
	 * @param path
	 *        The place to look at.
	 * @return The instance's context, or {@code null} if the view is not displayed there.
	 */
	public ViewContext getView(String viewRef, RevealPath path) {
		return first(_views.get(new ViewKey(viewRef, path)));
	}

	/**
	 * The control revealing the children of the given container at the given place.
	 *
	 * @param container
	 *        The element choosing between its children, or {@code null} for a dialog.
	 * @param path
	 *        The place the container is displayed at.
	 * @return The revealing control, or {@code null} if the container is not displayed there.
	 */
	public ChildRevealer getContainer(UIElement container, RevealPath path) {
		return first(_containers.get(new ContainerKey(container, path)));
	}

	/**
	 * The view file the window's root display shows.
	 *
	 * @return The view registered at {@link RevealPath#ROOT}, or {@code null} while the window
	 *         displays nothing.
	 */
	public String getRootView() {
		return viewAt(RevealPath.ROOT);
	}

	/**
	 * The view file displayed at the given place.
	 *
	 * @param path
	 *        The place to look at.
	 * @return The view file of the instance displayed there, or {@code null} if none is.
	 */
	public String viewAt(RevealPath path) {
		for (Map.Entry<ViewKey, List<ViewContext>> entry : _views.entrySet()) {
			if (entry.getKey().path().equals(path) && !entry.getValue().isEmpty()) {
				return entry.getKey().viewRef();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + _views.keySet();
	}

	private static <K, V> Runnable register(Map<K, List<V>> registrations, K key, V value) {
		registrations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
		return () -> {
			List<V> registered = registrations.get(key);
			if (registered != null) {
				registered.remove(value);
				if (registered.isEmpty()) {
					registrations.remove(key);
				}
			}
		};
	}

	private static <V> V first(List<V> registered) {
		return registered == null || registered.isEmpty() ? null : registered.get(0);
	}

	/**
	 * The identity of a displayed view instance.
	 *
	 * @param viewRef
	 *        Path of the displayed view file.
	 * @param path
	 *        The place it is displayed at.
	 */
	private record ViewKey(String viewRef, RevealPath path) {
		// Pure data.
	}

	/**
	 * The identity of a displayed container.
	 *
	 * @param container
	 *        The element choosing between its children, {@code null} for a dialog.
	 * @param path
	 *        The place the container is displayed at.
	 */
	private record ContainerKey(UIElement container, RevealPath path) {
		// Pure data.
	}
}
