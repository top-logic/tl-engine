/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.dob.MOAttribute;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.access.WithStorageAttribute;

/**
 * {@link StorageImplementation} that stores values in a column of the object's table.
 * 
 * <p>
 * If a storage does not implement this interface and does not represent a derived attribute, it
 * must implement {@link SeparateTableStorage}.
 * </p>
 * 
 * @see SeparateTableStorage
 * @see WithStorageAttribute
 */
public interface ColumnStorage extends StorageImplementation {

	/**
	 * The name of the {@link MOAttribute} of the objects table storing the attribute value.
	 * 
	 * @see WithStorageAttribute#getStorageAttribute()
	 */
	String getStorageAttribute();

}
