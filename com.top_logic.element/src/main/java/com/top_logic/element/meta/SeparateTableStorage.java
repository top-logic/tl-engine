/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.List;

import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;

/**
 * {@link StorageImplementation} that stores values in a table other than the object table.
 * 
 * <p>
 * The object table defines the identity of the base object that defines the attributes. Besides the
 * identity the object table may store values of other attributes of the base object. Attributes
 * that are stored in the base table must use a storage that implements {@link ColumnStorage}. All
 * other attributes whose values are not stored in the base table must use a storage that implements
 * this interface to allow finding changes of those attributes when computing a change log.
 * </p>
 * 
 * @see ColumnStorage
 * @see ChangeLogBuilder
 */
public interface SeparateTableStorage extends StorageImplementation {

	/**
	 * {@link AssociationStorageDescriptor} which are used by this {@link SeparateTableStorage} to
	 * store values.
	 */
	List<? extends AssociationStorageDescriptor> getStorageDescriptors();

}
