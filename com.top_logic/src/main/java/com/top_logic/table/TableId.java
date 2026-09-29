/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Stable identity of a table instance, used as the key under which its
 * {@link TableViewState} is persisted by a {@link ViewStateStore}.
 *
 * @param value
 *        The opaque, stable identifier string.
 */
public record TableId(String value) {
	// Pure value type; replaces the legacy ConfigKey for personalization lookups.
}
