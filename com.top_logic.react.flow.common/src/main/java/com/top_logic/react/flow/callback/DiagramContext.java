/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.callback;

import java.util.List;

import com.top_logic.react.flow.svg.event.SVGDropEvent;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.MouseButton;

/**
 * Operations added to a {@link Diagram}.
 * 
 * @see Diagram#getContext()
 */
public interface DiagramContext {

	/**
	 * Callback that in invoked on client-side, if a mouse click is received by a
	 * {@link ClickTarget} element.
	 */
	void processClick(ClickTarget node, List<MouseButton> pressedButtons);

	/**
	 * Callback that in invoked on client-side, if a drop operation occurs over a {@link DropRegion}
	 * element.
	 */
	void processDrop(DropRegion node, SVGDropEvent event);

}
