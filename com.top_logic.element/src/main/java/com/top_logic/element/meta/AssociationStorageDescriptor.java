/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.meta;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.db.schema.setup.config.KeyAttributes;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Describes how attribute values of a {@link TLStructuredTypePart} are stored in an association
 * table.
 * 
 * <p>
 * Implementations of this interface provide the necessary metadata to locate and identify the
 * storage of attribute values for structured type parts in a relational table. This includes:
 * </p>
 * 
 * <ul>
 * <li>The name of the database table containing the attribute values.</li>
 * <li>The column in that table where the values are stored.</li>
 * <li>How to extract the base object ID and the part (attribute) ID from a given row.</li>
 * </ul>
 * 
 * <p>
 * If multiple attributes share the same table and storage column, they must behave identically in
 * terms of identifying the base object and part IDs for any given row.
 * </p>
 *
 * @see SeparateTableStorage
 * @see TLStructuredTypePart
 */
public interface AssociationStorageDescriptor {

	/**
	 * The name of the association table in which this {@link SeparateTableStorage} stores the data
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

	/**
	 * Checks whether the given columns are configured as {@link KeyAttributes}.
	 * 
	 * <p>
	 * It is necessary for the {@link ChangeLogBuilder}, that both columns are defined as key
	 * attributes. Otherwise the change for the actual value does not contain the information base
	 * object and part id, because these values have not changed. Being key attributes ensures that
	 * these values are always delivered.
	 * </p>
	 * 
	 * @param partCol
	 *        Name of the {@link MOReference} holding the part returned by {@link #getPartId(Map)}.
	 *        May be <code>null</code> when the part is not stored in the database.
	 * @param baseObjectCol
	 *        Name of the {@link MOReference} holding the part returned by
	 *        {@link #getBaseObjectId(Map)}. May be <code>null</code> when the base object is not
	 *        stored in the database.
	 */
	default boolean checkKeyAttributes(TLStructuredTypePart part, String partCol, String baseObjectCol) {
		if (partCol == null && baseObjectCol == null) {
			return true;
		}
		MORepository tableRepo = PersistencyLayer.getKnowledgeBase().getMORepository();
		MOClass storageTable = (MOClass) tableRepo.getType(getTable());
		Set<String> keyAttributes = new HashSet<>();
		while (storageTable != null) {
			KeyAttributes keyAnnotation = storageTable.getAnnotation(KeyAttributes.class);
			if (keyAnnotation != null) {
				keyAttributes.addAll(keyAnnotation.getAttributes());
			}
			storageTable = storageTable.getSuperclass();
		}

		if (partCol != null && !keyAttributes.contains(partCol)) {
			Logger.error(
				"Column '" + partCol + "' is not declared as key column in table '" + getTable()
						+ "'. Changes for attribute '" + TLModelUtil.qualifiedName(part) + "' may not be detected.",
				SeparateTableStorage.class);
			return false;
		}
		if (baseObjectCol != null && !keyAttributes.contains(baseObjectCol)) {
			Logger.error(
				"Column '" + baseObjectCol + "' is not declared as key column in table '" + getTable()
						+ "'. Changes for attribute '" + TLModelUtil.qualifiedName(part) + "' may not be detected.",
				SeparateTableStorage.class);
			return false;
		}
		return true;
	}

}