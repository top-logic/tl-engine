/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;

/**
 * Preliminary stub for {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModuleBase extends TLModule, MetaElementHolder {

	/**
	 * The datatype defined by this module with the given name.
	 * 
	 * @param name
	 *        The name of the datatype to look up.
	 * @return The datatype defined by this module, or <code>null</code> if not found.
	 */
	TLPrimitive getDatatype(String name);

}
