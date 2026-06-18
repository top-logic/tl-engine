/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.Map;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.table.TableId;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;

/**
 * {@link ViewStateStore} persisting the {@link TableViewStateCodec serializable subset} of a
 * {@link TableViewState} through the current user's {@link PersonalConfiguration}.
 *
 * <p>
 * The state is stored as a JSON value under a per-table key. When there is no personal
 * configuration available (e.g. no user session), loading yields {@code null} and saving is a
 * no-op, so a table simply falls back to its default state.
 * </p>
 */
public class PersonalConfigViewStateStore implements ViewStateStore {

	/** Shared stateless instance. */
	public static final PersonalConfigViewStateStore INSTANCE = new PersonalConfigViewStateStore();

	private static final String KEY_PREFIX = "tableView.";

	@Override
	public TableViewState load(TableId id) {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return null;
		}
		Object json = config.getJSONValue(key(id));
		if (json instanceof Map<?, ?> map) {
			TableViewState state = new TableViewState();
			TableViewStateCodec.readInto(state, map);
			return state;
		}
		return null;
	}

	@Override
	public void save(TableId id, TableViewState state) {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return;
		}
		config.setJSONValue(key(id), TableViewStateCodec.toJson(state));
	}

	private static String key(TableId id) {
		return KEY_PREFIX + id.value();
	}

}
