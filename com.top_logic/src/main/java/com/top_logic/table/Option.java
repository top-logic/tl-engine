/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.basic.util.ResKey;

/**
 * A selectable value of an option-style {@link FilterInput.Options column filter}.
 *
 * <p>
 * The number of rows matching an option is not part of the option itself; it is provided
 * separately via {@link MatchCounts} so that counts can be recomputed as other filters
 * change without rebuilding the option list.
 * </p>
 *
 * @param value
 *        The domain value to filter by.
 * @param label
 *        The display label for the value.
 */
public record Option(Object value, ResKey label) {
	// Pure value type.
}
