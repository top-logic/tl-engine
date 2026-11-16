/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Map;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.kbbased.storage.InlineCollectionStorage.StoreInTargetConfig;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AssociationStorageDescriptor} of a reference whose values are stored in the rows of the
 * referenced objects, see {@link StoreInTargetConfig}.
 *
 * <p>
 * A row of the target table stores a value of the reference, if its
 * {@link StoreInTargetConfig#getContainerColumn() container column} holds the referencing object.
 * </p>
 *
 * @see InlineCollectionStorage
 * @see ReverseForeignKeyStorage
 */
public class StoreInTargetDescriptor implements AssociationStorageDescriptor {

	private final String _table;

	private final StoreInTargetConfig _config;

	private final TLStructuredTypePart _attribute;

	/**
	 * Creates a {@link StoreInTargetDescriptor}.
	 *
	 * @param table
	 *        See {@link #getTable()}.
	 * @param config
	 *        The columns of the target table storing the reference.
	 * @param attribute
	 *        The reference whose values are described.
	 */
	public StoreInTargetDescriptor(String table, StoreInTargetConfig config, TLStructuredTypePart attribute) {
		_table = table;
		_config = config;
		_attribute = attribute;
	}

	@Override
	public String getTable() {
		return _table;
	}

	@Override
	public ObjectKey getBaseObjectId(Map<String, Object> row) {
		return baseObjectId(_config, row);
	}

	@Override
	public String getBaseObjectColumn() {
		return _config.getContainerColumn();
	}

	@Override
	public String getStorageColumn() {
		return _config.getContainerColumn();
	}

	@Override
	public ObjectKey getPartId(Map<String, Object> row) {
		return partId(_config, _attribute, row);
	}

	/**
	 * The {@link #getBaseObjectId(Map)} of a target table row.
	 *
	 * @param config
	 *        The columns of the target table storing the reference.
	 * @param row
	 *        Values of a target table row.
	 */
	static ObjectKey baseObjectId(StoreInTargetConfig config, Map<String, Object> row) {
		return (ObjectKey) row.get(config.getContainerColumn());
	}

	/**
	 * The {@link #getPartId(Map)} of a target table row.
	 *
	 * @param config
	 *        The columns of the target table storing the reference.
	 * @param attribute
	 *        The reference whose values are stored.
	 * @param row
	 *        Values of a target table row.
	 */
	static ObjectKey partId(StoreInTargetConfig config, TLStructuredTypePart attribute, Map<String, Object> row) {
		String referenceColumn = config.getReferenceColumn();
		if (referenceColumn == null) {
			// A row stores a value of the reference exactly when its container column holds a
			// value. The container column is no key attribute, so the row of an update that keeps
			// the container does not contain the container column.
			if (row.get(config.getContainerColumn()) == null) {
				return null;
			}
			return attribute.getDefinition().tId();
		} else {
			return (ObjectKey) row.get(referenceColumn);
		}
	}

}
