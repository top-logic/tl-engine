/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.composite;

import java.util.Objects;

/**
 * Connection between a container and its parts is stored in a separate link table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class LinkTable extends ContainerStorage {

	private final String _table;

	/**
	 * Creates a new {@link LinkTable}.
	 */
	public LinkTable(String table) {
		super();
		_table = table;
	}

	/**
	 * Name of the table in which the links are stored.
	 */
	public String getTable() {
		return _table;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_table);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinkTable other = (LinkTable) obj;
		return Objects.equals(_table, other._table);
	}

}

