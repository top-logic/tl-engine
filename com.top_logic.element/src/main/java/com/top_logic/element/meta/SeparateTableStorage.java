/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Map;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageImplementation} that stores values in a table other than the object table.
 * 
 * <p>
 * The object table defines the identity of the base object that defines the attributes. Besides the
 * identity the object table may store values of other attributes of the base object.
 * </p>
 */
public interface SeparateTableStorage extends StorageImplementation {

	/**
	 * The name of the association table in which this {@link AssociationStorage} stores the data
	 * for the {@link TLStructuredTypePart}.
	 */
	String getTable();

	/**
	 * The ID of the base objects to which the attribute data from the given row belongs.
	 *
	 * @param row
	 *        Values of a {@link #getTable() table} row.
	 * @return ID of the object for which a value is stored in the given row.
	 */
	ObjectKey getBaseObjectId(Map<String, Object> row);

	/**
	 * The ID of the attribute for which values are stored in the given row.
	 */
	ObjectKey getPartId(Map<String, Object> row);

	/**
	 * The column in the {@link #getTable()} that is used for storing values.
	 * 
	 * <p>
	 * If two storages of two different attributes report the same storage column for the same
	 * table, both must behave identically when invoking the methods {@link #getBaseObjectId(Map)}
	 * and {@link #getPartId(Map)} for any given argument.
	 * </p>
	 */
	String getStorageColumn();

}
