/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui.handler;

import com.top_logic.graphic.flow.callback.DiagramHandler;
import com.top_logic.graphic.flow.callback.DropHandler;
import com.top_logic.graphic.flow.data.DropRegion;
import com.top_logic.layout.dnd.DndData;

/**
 * Server-side drop handler that is able to receive <i>DnD</i> data.
 */
public interface ServerDropHandler extends DropHandler, DiagramHandler {

	/**
	 * Server-side callback, when a drop operation is recorded on the client.
	 */
	void onDrop(DropRegion target, DndData data);

}
