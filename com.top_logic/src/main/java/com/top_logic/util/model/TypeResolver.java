/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model;

import com.top_logic.basic.col.Maybe;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLType;

/**
 * Base class for algorithms that resolve {@link TLType}s by textual description.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeResolver {

	/**
	 * Resolves the given textual type description in the given model.
	 * 
	 * @param model
	 *        The context {@link TLModel}.
	 * @param typeDescription
	 *        The textual description of the type.
	 * @return The resolved {@link TLType}.
	 */
	public abstract Maybe<TLType> resolveType(TLModel model, String typeDescription);

}