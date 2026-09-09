/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.view.ViewLoader;

/**
 * One place a view file is displayed at within a root view.
 *
 * <p>
 * The {@link #steps()} lead from the root view down to the mount, naming only the containers that
 * choose between their content groups. Displaying the view means asking each container, from the
 * first step to the last, to show the group named by the step's key. A view displayed as part of
 * whatever contains it (not chosen by a key anywhere on the way) has an empty step list, as has the
 * root view itself.
 * </p>
 *
 * @param viewRef
 *        Path of the mounted view file relative to {@link ViewLoader#VIEW_BASE_PATH}.
 * @param steps
 *        The containers to ask, outermost first.
 */
public record MountPath(String viewRef, List<MountStep> steps) {

	@Override
	public String toString() {
		return viewRef + "@" + steps.stream().map(MountStep::toString).collect(Collectors.joining("/"));
	}
}
