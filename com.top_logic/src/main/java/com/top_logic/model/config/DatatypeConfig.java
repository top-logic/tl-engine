/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link TypeConfig} specifying a primitive type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(DatatypeConfig.TAG_NAME)
public interface DatatypeConfig extends TypeConfig, DBColumnType {

	/**
	 * @see #getKind()
	 */
	String KIND = "kind";

	/**
	 * Default name of tags.
	 */
	String TAG_NAME = "datatype";

	/**
	 * Property name of {@link #getStorageMapping()}.
	 */
	String STORAGE_MAPPING = "storage-mapping";

	/**
	 * Compatibility with old {@link TLPrimitive} API.
	 */
	@Name(KIND)
	@Mandatory
	Kind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(Kind value);

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
