/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Persistence port for per-user table personalization.
 *
 * <p>
 * Decouples the model tier from any concrete personalization backend: the default
 * implementation persists through {@code PersonalConfiguration}, but the model tier only
 * sees this port and the serializable {@link TableViewState}.
 * </p>
 */
public interface ViewStateStore {

	/**
	 * Loads the persisted view state for the given table, or {@code null} if none is
	 * stored.
	 *
	 * @param filters
	 *        The column-aware codec used to restore persisted column filters.
	 */
	TableViewState load(TableId id, FilterCodec filters);

	/**
	 * Persists the given view state for the given table.
	 *
	 * @param filters
	 *        The column-aware codec used to serialize the state's column filters.
	 */
	void save(TableId id, TableViewState state, FilterCodec filters);

}
