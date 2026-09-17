/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.function.Consumer;

/**
 * A pick started by {@link ElementPicker} and waiting for the user to click an element.
 *
 * <p>
 * Held by the session's {@link ReactWindowRegistry} under a correlation token until the client of
 * the {@link #targetWindowId() picked window} reports the hit.
 * </p>
 *
 * @param requesterWindowId
 *        The window that started the pick. The result runs in this window's sub-session, so that
 *        channel-driven control updates flush to its SSE queue.
 * @param targetWindowId
 *        The window that is in pick mode, and in which a {@link PickKind#CONTROL} pick is resolved.
 * @param kind
 *        What the click is resolved to.
 * @param onPicked
 *        Callback invoked with what the user hit.
 */
public record PendingPick(String requesterWindowId, String targetWindowId, PickKind kind,
		Consumer<PickResult> onPicked) {
	// Pure data.
}
