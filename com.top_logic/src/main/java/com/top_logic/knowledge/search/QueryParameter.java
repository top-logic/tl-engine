/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;

/**
 * Named parameter in an {@link Expression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface QueryParameter {

	/**
	 * The parameter name.
	 */
	String getName();

	/**
	 * The type of the parameter.
	 */
	MetaObject getDeclaredType();

	/**
	 * @see #getDeclaredType()
	 */
	void setDeclaredType(MetaObject declaredType);

}
