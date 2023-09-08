/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.sequence;

import com.top_logic.basic.sql.SQLH;

/**
 * Definition of a sequence.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SequenceDefinition {

	final String table;

	final String serialColumn;

	private SequenceDefinition(String table, String serialColumn) {
		this.table = table;
		this.serialColumn = serialColumn;
	}

	/**
	 * Creates a new {@link SequenceDefinition}.
	 * 
	 * @param table
	 *        the table to create sequence for
	 * @param serialColumn
	 *        name of the column which is filled with the generated value
	 */
	public static SequenceDefinition newDefinition(String table, String serialColumn) {
		table = SQLH.mangleDBName(table);
		serialColumn = SQLH.mangleDBName(serialColumn);
		return new SequenceDefinition(table, serialColumn);
	}

	@Override
	public String toString() {
		return "SequenceDefinition [table=" + table + ", serialColumn=" + serialColumn + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serialColumn == null) ? 0 : serialColumn.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SequenceDefinition))
			return false;
		SequenceDefinition other = (SequenceDefinition) obj;
		if (serialColumn == null) {
			if (other.serialColumn != null)
				return false;
		} else if (!serialColumn.equals(other.serialColumn))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		return true;
	}
}
