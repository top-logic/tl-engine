/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.callback;

import java.util.Set;

import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.MouseButton;

/**
 * Callback interface to specify custom behavior for {@link ClickTarget} diagram elements.
 * 
 * @see ClickTarget#getClickHandler()
 */
public interface ClickHandler extends DiagramHandler {
	/**
	 * Server-callback invoked, if a user clicks on a {@link ClickTarget} diagram element.
	 *
	 * @param target
	 *        The diagram element that has been clicked.
	 * @param buttons
	 *        The mouse button that was used for the click (subset of
	 *        {@link ClickTarget#getButtons()}.
	 */
	void onClick(ClickTarget target, Set<MouseButton> buttons);
}
