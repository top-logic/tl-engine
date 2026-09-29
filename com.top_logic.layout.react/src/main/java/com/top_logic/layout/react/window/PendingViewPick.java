/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.function.Consumer;

/**
 * A pending "select view" pick registered by the designer while the main window is in pick mode.
 *
 * @param designerWindowId
 *        The designer window whose sub-session the pick result must run in (so channel-driven
 *        control updates flush to the designer's SSE queue).
 * @param onPicked
 *        Callback invoked with the picked view's source path when the user clicks a view.
 */
public record PendingViewPick(String designerWindowId, Consumer<String> onPicked) {
	// Marker record.
}
