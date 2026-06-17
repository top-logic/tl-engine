/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Reusable concrete column filters for the green-field table model (ticket #29108).
 *
 * <p>
 * Each filter is a {@link com.top_logic.table.FilterState} (serializable user criteria)
 * paired with a {@link com.top_logic.table.ColumnFilter} (UI-neutral input descriptor plus
 * matching logic): text, comparison/range, option-set and tri-state boolean. The filters
 * are in-memory by default; query pushdown is added once a query backend is wired.
 * </p>
 */
package com.top_logic.table.filter;
