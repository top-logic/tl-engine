/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.List;

import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttRow;

/**
 * Result of {@link AxisProvider#buildAxis}: rows and items representing the axis visually.
 */
public record AxisContent(List<GanttRow> rows, List<GanttItem> items) {
}
