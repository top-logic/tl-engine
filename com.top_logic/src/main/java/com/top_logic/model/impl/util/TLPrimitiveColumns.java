/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl.util;

import com.top_logic.model.TLPrimitive;
import com.top_logic.model.impl.generated.TlModelFactory;

/**
 * Column names used to store a {@link TLPrimitive} in the database.
 */
public interface TLPrimitiveColumns {

	/**
	 * The table to store {@link TLPrimitive}s in.
	 */
	String OBJECT_TYPE = TlModelFactory.KO_NAME_TL_PRIMITIVE;

	/** Column that stores {@link TLPrimitive#getKind()}. */
	String KIND_ATTR = "kind";

	/** Column that stores {@link TLPrimitive#getDBType()}. */
	String DB_TYPE_ATTR = "dbType";

	/** Column that stores {@link TLPrimitive#getDBSize()}. */
	String DB_SIZE_ATTR = "dbSize";

	/** Column that stores {@link TLPrimitive#getDBPrecision()}. */
	String DB_PRECISION_ATTR = "dbPrecision";

	/** Column that stores {@link TLPrimitive#isBinary()}. */
	String BINARY_ATTR = "binary";

	/** Column that stores {@link TLPrimitive#getStorageMapping()}. */
	String STORAGE_MAPPING = "storageMapping";

}

