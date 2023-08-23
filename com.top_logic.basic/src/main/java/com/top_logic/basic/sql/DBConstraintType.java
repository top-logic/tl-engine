/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.DatabaseMetaData;

/**
 * Actions for foreign key constraint.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum DBConstraintType {
	
	/**
	 * The action is rejected, if the result would violate the constraint.
	 * 
	 * @see DatabaseMetaData#importedKeyRestrict
	 */
	RESTRICT,
	
	/**
	 * If the target is deleted, the referring row is deleted as well. If the target is updated, the
	 * referring row is updated as well.
	 * 
	 * @see DatabaseMetaData#importedKeyCascade
	 */
	CASCADE,
	
	/**
	 * If the target is deleted, the reference is cleared. If the target is updated, the reference
	 * is cleared.
	 * 
	 * @see DatabaseMetaData#importedKeySetNull
	 */
	CLEAR;
	
}
