/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * Marker for wrapped cells that display form fields, where the generic fallback CSS class mechanic
 * for tables is not required.
 */
public interface EditingCell extends Cell {
	// Pure marker interface.
}
