/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.List;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NamedFilterStore;
import com.top_logic.table.TableId;

/**
 * {@link NamedFilterStore} persisting the {@link NamedFilterCodec serializable form} of a table's
 * saved {@link NamedFilter}s through the current user's {@link PersonalConfiguration}.
 *
 * <p>
 * The filters are stored as a JSON value under a per-table key. When there is no personal
 * configuration available (e.g. no user session), loading yields no filter and saving is a no-op,
 * so a table offers just the filters its definition declares.
 * </p>
 */
public class PersonalConfigNamedFilterStore implements NamedFilterStore {

	/** Shared stateless instance. */
	public static final PersonalConfigNamedFilterStore INSTANCE = new PersonalConfigNamedFilterStore();

	private static final String KEY_PREFIX = "tableFilters.";

	@Override
	public List<NamedFilter> load(TableId id, FilterCodec codec) {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return List.of();
		}
		return NamedFilterCodec.read(config.getJSONValue(key(id)), codec);
	}

	@Override
	public void save(TableId id, List<NamedFilter> filters, FilterCodec codec) {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return;
		}
		config.setJSONValue(key(id), NamedFilterCodec.toJson(filters, codec));
	}

	private static String key(TableId id) {
		return KEY_PREFIX + id.value();
	}

}
