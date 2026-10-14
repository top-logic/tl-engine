/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Excel export of a {@link com.top_logic.table.TableView}.
 *
 * <p>
 * The bridge between the green-field table model of {@link com.top_logic.table} and the office
 * export machinery of {@link com.top_logic.tool.export}: a table is written through the same
 * {@link com.top_logic.base.office.excel.streaming.ExcelWriter} and the same
 * {@link com.top_logic.tool.export.ExcelCellRenderer}s the export of a classic table uses. It lives
 * outside {@link com.top_logic.table}, whose dependency boundary keeps the legacy table and export
 * code out.
 * </p>
 */
package com.top_logic.tool.export.tableview;
