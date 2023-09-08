/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * {@link QueryPart}s that depend on the {@link MetaObject} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeSystemDependent {

	/**
	 * The name of the declared type
	 * 
	 * @see #getDeclaredType()
	 */
	String getTypeName();

	/**
	 * The type that is checked for.
	 */
	MetaObject getDeclaredType();

	/**
	 * @param type
	 *        new value of {@link #getDeclaredType()}
	 */
	void setDeclaredType(MetaObject type);

	/**
	 * Whether the {@link TypeBinding} has already computed a binding to
	 * concrete type implementations from the {@link MetaObject} hierarchy.
	 */
	boolean hasTypeBinding();

}
