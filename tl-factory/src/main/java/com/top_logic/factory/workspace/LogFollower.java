/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.time.Duration;

/**
 * Handle of the delivery of the log of a workspace, started by
 * {@link WorkspaceProvider#followLog(String, int, java.time.Instant, ExecOutput)}.
 *
 * <p>
 * The delivery ends when the handle is {@link #close() closed} or when the main process of the
 * workspace ends.
 * </p>
 */
public interface LogFollower extends AutoCloseable {

	/**
	 * Waits until the delivery has ended, at most for the given time.
	 *
	 * @param timeout
	 *        The maximum time to wait, {@link Duration#ZERO} to only check.
	 * @return Whether the delivery has ended.
	 */
	boolean awaitEnd(Duration timeout) throws InterruptedException;

	/**
	 * Stops the delivery. Output that arrives afterwards is dropped.
	 */
	@Override
	void close();

}
