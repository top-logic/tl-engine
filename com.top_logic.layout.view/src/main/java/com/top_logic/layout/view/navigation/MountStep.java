/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;

/**
 * One step of a {@link MountPath}: the container to ask, and the key of the content group to
 * display.
 *
 * @param container
 *        The element addressing its content by keys, e.g. a sidebar, a tab bar or a master-detail
 *        element.
 * @param key
 *        The key of the {@link ChildGroup} leading to the mounted view, e.g. a sidebar item id or a
 *        tab id.
 */
public record MountStep(UIElement container, String key) {

	@Override
	public String toString() {
		return container.getClass().getSimpleName() + "[" + key + "]";
	}
}
