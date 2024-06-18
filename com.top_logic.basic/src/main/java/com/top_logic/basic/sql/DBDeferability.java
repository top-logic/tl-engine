/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.DatabaseMetaData;

import com.top_logic.basic.config.annotation.Label;

/**
 * Actions for foreign key constraint.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see DatabaseMetaData#getCrossReference(String, String, String, String, String, String)
 */
@Label("Deferability")
public enum DBDeferability {
	
	/**
	 * Constraint is checked during commit.
	 * 
	 * @see DatabaseMetaData#importedKeyInitiallyDeferred
	 */
	DEFERRED,
	
	/**
	 * Constraints is checked on insert but can be deferred within a transaction.
	 * 
	 * @see DatabaseMetaData#importedKeyInitiallyImmediate
	 */
	IMMEDIATE,
	
	/**
	 * Constraint is checked on insert and cannot be deferred.
	 * 
	 * @see DatabaseMetaData#importedKeyNotDeferrable
	 */
	STRICT;
	
}
