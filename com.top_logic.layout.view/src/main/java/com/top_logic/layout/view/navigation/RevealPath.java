/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Where a part of the display sits within its window: the containers that had to display a child of
 * theirs for it to exist, outermost first.
 *
 * <p>
 * Established as a scope of the {@link ViewContext}: a container that displays one of its children
 * at a time {@link #append(UIElement, String) appends} a step when it derives the context of that
 * child, so everything created below knows the way from the window's root display down to itself.
 * The path is the runtime counterpart of the statically scanned {@link MountPath}: its
 * {@link #mountSteps()} are comparable to the steps a scan of the view configuration produces.
 * </p>
 *
 * @see RevealRegistry
 */
public final class RevealPath {

	/** The path of the display a window starts with: no container chose it. */
	public static final RevealPath ROOT = new RevealPath(List.of());

	private final List<RevealStep> _steps;

	private RevealPath(List<RevealStep> steps) {
		_steps = steps;
	}

	/**
	 * The path established for the given context, {@link #ROOT} for a context no container derived.
	 *
	 * @param context
	 *        The context to read the path from, may be {@code null}.
	 */
	public static RevealPath of(ViewContext context) {
		RevealPath result = context == null ? null : context.getScope(RevealPath.class);
		return result == null ? ROOT : result;
	}

	/**
	 * The steps from the window's root display down to here, outermost first.
	 */
	public List<RevealStep> steps() {
		return _steps;
	}

	/**
	 * Whether this is the path of the display the window starts with.
	 */
	public boolean isRoot() {
		return _steps.isEmpty();
	}

	/**
	 * The path of the child the given container displays under the given key.
	 *
	 * @param container
	 *        The element choosing between its children, or {@code null} for a dialog opened on top
	 *        of the display.
	 * @param key
	 *        The key the container addresses the child by.
	 * @return The extended path.
	 */
	public RevealPath append(UIElement container, String key) {
		List<RevealStep> steps = new ArrayList<>(_steps.size() + 1);
		steps.addAll(_steps);
		steps.add(new RevealStep(container, key));
		return new RevealPath(List.copyOf(steps));
	}

	/**
	 * This path in the shape a scan of the view configuration produces, for comparison with the
	 * {@link MountPath mounts} of a view file.
	 *
	 * @return The leading steps that a configured container accounts for. A dialog ends the answer:
	 *         nothing below it is part of any mount.
	 */
	public List<MountStep> mountSteps() {
		List<MountStep> result = new ArrayList<>(_steps.size());
		for (RevealStep step : _steps) {
			MountStep mountStep = step.mountStep();
			if (mountStep == null) {
				break;
			}
			result.add(mountStep);
		}
		return result;
	}

	/**
	 * This path as the place the given view file is displayed at.
	 *
	 * @param viewRef
	 *        Path of the view file displayed here, relative to
	 *        {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}.
	 * @return The mount naming this place.
	 */
	public MountPath toMountPath(String viewRef) {
		return new MountPath(viewRef, mountSteps());
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof RevealPath other && _steps.equals(other._steps);
	}

	@Override
	public int hashCode() {
		return _steps.hashCode();
	}

	@Override
	public String toString() {
		return _steps.stream().map(RevealStep::toString).collect(Collectors.joining("/"));
	}
}
