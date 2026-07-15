/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Green-field table model SPI (ticket #29108).
 *
 * <p>
 * A clean, UI-agnostic replacement for the legacy table stack
 * ({@code com.top_logic.layout.table.TableModel} / {@code TableViewModel} /
 * {@code ObjectTableModel} / {@code TreeTableModel}). Designed from the UI backward: the
 * viewport needs a <em>windowed sequence of rows</em>, each carrying a key and optional
 * depth/expansion, plus per-cell value/renderer, plus a small command set.
 * </p>
 *
 * <p>
 * The model is split into three independent ingredients composed by a single binding
 * object {@link com.top_logic.table.TableView}:
 * </p>
 * <ul>
 * <li>{@link com.top_logic.table.Column} - declarative, type-safe column
 * definitions,</li>
 * <li>{@link com.top_logic.table.RowSource} - a windowed (lazy/pushdown-capable) row
 * sequence that unifies flat, tree and grouped tables,</li>
 * <li>{@link com.top_logic.table.TableViewState} - the serializable view state
 * (sort/filter/order/width/frozen/expansion/selection/paging).</li>
 * </ul>
 *
 * <p>
 * <b>Dependency boundary.</b> This package must not depend on the legacy table, form or
 * control code. Permitted: the JDK, {@code ResKey}/i18n,
 * {@code com.top_logic.layout.form.model.FieldModel} (the new field abstraction the React
 * controls bind to), and neutral rendering SAMs defined here. Forbidden:
 * {@code TableModel}, {@code TableConfiguration}, {@code ColumnConfiguration},
 * {@code Accessor}, {@code FormContext}/{@code FormField}, {@code Control}/
 * {@code DisplayContext}, {@code FilterDialogBuilder}/{@code ConfiguredFilter},
 * {@code PersonalConfiguration}.
 * </p>
 *
 * @see <a href="file:../../../../../../../../docs/plans/2026-06-17-greenfield-table-model-spec.md">Specification</a>
 */
package com.top_logic.table;
