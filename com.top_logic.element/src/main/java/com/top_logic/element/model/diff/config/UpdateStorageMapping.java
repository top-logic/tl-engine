/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.config.DatatypeConfig;

/**
 * Updates the {@link StorageMapping} of a {@link TLPrimitive}.
 * 
 * @see DatatypeConfig#getStorageMapping()
 */
public interface UpdateStorageMapping extends ClassUpdate {

	/**
	 * Property name of {@link #getStorageMapping()}.
	 */
	String STORAGE_MAPPING = "storage-mapping";

	/**
	 * The {@link StorageMapping} to apply when loading and storing values.
	 */
	@Name(STORAGE_MAPPING)
	@Mandatory
	@DefaultContainer
	PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();

	/** @see #getStorageMapping() */
	void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);

}
