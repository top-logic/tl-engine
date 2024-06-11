/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Mix-in interface for a storage configuration that allows to define a database column.
 */
@Abstract
public interface WithStorageAttribute extends ConfigurationItem {

	/** Property name of {@link #getStorageAttribute()}. */
	String STORAGE_ATTRIBUTE = "storage-attribute";

	/**
	 * The name of the database column which is used to store the value.
	 * 
	 * <p>
	 * If not set, it defaults to the name of the attribute.
	 * </p>
	 */
	@Nullable
	@Name(STORAGE_ATTRIBUTE)
	String getStorageAttribute();

	/**
	 * @see #getStorageAttribute()
	 */
	void setStorageAttribute(String value);

}
