/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Algorithm for selecting a concrete type from values of an imported database row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeSelector {

	/**
	 * Find the type to instantiate for representing the given row as {@link TLObject}.
	 * 
	 * <p>
	 * In a monomorphic mapping, a constant value is returned. In a polymorphic mapping, the type is
	 * determined from some column values being read.
	 * </p>
	 *
	 * @param row
	 *        The current row being imported.
	 * @return The type to instantiate for the given row.
	 */
	TLClass getType(ImportRow row);

}
