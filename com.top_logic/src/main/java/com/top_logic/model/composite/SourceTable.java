/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.composite;

import java.util.Objects;

import com.top_logic.model.TLReference;

/**
 * The part is also stored in the container table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SourceTable extends ContainerStorage {

	private final String _table;

	private final String _part;

	private final TLReference _reference;

	/**
	 * Creates a new {@link SourceTable}.
	 */
	public SourceTable(String table, String part, TLReference reference) {
		super();
		_table = table;
		_part = part;
		_reference = reference;
	}

	/**
	 * Name of the table containing the container and the part.
	 */
	public String getTable() {
		return _table;
	}

	/**
	 * Name of the column containing the part of the relation.
	 */
	public String getPartAttribute() {
		return _part;
	}

	/**
	 * {@link TLReference} that defines the relation between container and part.
	 */
	public TLReference getReference() {
		return _reference;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_part, _reference, _table);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceTable other = (SourceTable) obj;
		return Objects.equals(_part, other._part)
				&& Objects.equals(_reference, other._reference)
				&& Objects.equals(_table, other._table);
	}

}

