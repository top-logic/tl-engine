/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.Collection;
import java.util.stream.Collectors;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;

/**
 * Scope of named types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeContext {

	/**
	 * All {@link MetaObject}s in this repository.
	 */
	Collection<? extends MetaObject> getMetaObjects();

	/**
	 * Find the type with the given name.
	 */
	default MetaObject getType(String typeName) throws UnknownTypeException {
		MetaObject result = getTypeOrNull(typeName);
		if (result == null) {
			throw new UnknownTypeException("Unknown table '" + typeName + "', available tables are: "
				+ getMetaObjects().stream().map(t -> t.getName()).sorted().collect(Collectors.joining(", ")));
		}
		return result;
	}

	/**
	 * Find the type with the given name, if it exists, otherwise <code>null</code>.
	 */
	MetaObject getTypeOrNull(String typeName);

	/**
	 * Whether the types in this {@link TypeContext} supports instances on different branches.
	 * 
	 * <p>
	 * If no multiple branches are supported, the implementation may not create a database column
	 * for a branch attribute, because it has a constant value.
	 * </p>
	 */
	boolean multipleBranches();

}
