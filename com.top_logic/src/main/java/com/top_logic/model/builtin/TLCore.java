/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.builtin;

import com.top_logic.model.TLModel;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;

/**
 * Utilities for accessing the built-in core module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLCore {

	/**
	 * Name of the core module defining built-in primitive types.
	 */
	public static final String TL_CORE = "tl.core";

	/**
	 * The predefined primitive type with the given name.
	 * 
	 * @param tlModel
	 *        The context model.
	 * @param kind
	 *        The primitive type name.
	 * @return The built-in primitive type.
	 */
	public static TLPrimitive getPrimitiveType(TLModel tlModel, Kind kind) {
		return (TLPrimitive) tlModel.getModule(TL_CORE).getType(kind.getExternalName());
	}

}
