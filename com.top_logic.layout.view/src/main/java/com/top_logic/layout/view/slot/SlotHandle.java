/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

/**
 * Handle returned by {@link SlotRegistry} on registration of a slot placeholder or a slot
 * contribution. Calling {@link #close()} removes the registration and triggers re-routing.
 */
public interface SlotHandle extends AutoCloseable {

	@Override
	void close();

}
