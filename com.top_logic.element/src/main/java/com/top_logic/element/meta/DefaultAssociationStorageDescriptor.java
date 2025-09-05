/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.meta;

import java.util.Map;
import java.util.Objects;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AssociationStorageDescriptor} which uses static column names to fetch base object ID and
 * part ID.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultAssociationStorageDescriptor implements AssociationStorageDescriptor {

	private final String _table;

	private final String _baseObjectCol;

	private final String _partCol;

	private final String _storageCol;

	/**
	 * Creates a new {@link DefaultAssociationStorageDescriptor}.
	 */
	public DefaultAssociationStorageDescriptor(String table, String baseObjectCol, String partCol, String storageCol) {
		_table = table;
		_baseObjectCol = baseObjectCol;
		_partCol = partCol;
		_storageCol = storageCol;
	}

	@Override
	public String getTable() {
		return _table;
	}

	@Override
	public ObjectKey getBaseObjectId(Map<String, Object> row) {
		return (ObjectKey) row.get(baseObjectCol());
	}

	@Override
	public ObjectKey getPartId(Map<String, Object> row) {
		return (ObjectKey) row.get(partCol());
	}

	@Override
	public String getStorageColumn() {
		return _storageCol;
	}

	/**
	 * The name of the column that stores the base object.
	 * 
	 * @see #getBaseObjectId(Map)
	 */
	public String baseObjectCol() {
		return _baseObjectCol;
	}

	/**
	 * The name of the column that stores the part.
	 * 
	 * @see #getPartId(Map)
	 */
	public String partCol() {
		return _partCol;
	}

	/**
	 * Checks whether the {@link #getTable()} has {@link #baseObjectCol()} and {@link #partCol()} as
	 * key attributes.
	 */
	public void checkKeyAttributes(TLStructuredTypePart attribute) {
		checkKeyAttributes(attribute, partCol(), baseObjectCol());
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseObjectCol(), partCol(), _storageCol, _table);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultAssociationStorageDescriptor other = (DefaultAssociationStorageDescriptor) obj;
		return Objects.equals(baseObjectCol(), other.baseObjectCol()) && Objects.equals(partCol(), other.partCol())
				&& Objects.equals(_storageCol, other._storageCol) && Objects.equals(_table, other._table);
	}

}

