/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

/**
 * Function selecting the context of uniqueness of generated IDs.
 * 
 * @see ConfiguredNumberHandler.UIConfig#getDynamicSequenceName()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicSequenceName {

	/**
	 * Selects an identifier from the creation context in which generated IDs must be unique.
	 * 
	 * @param context
	 *        The creation context in which IDs are generated.
	 * @return A value identifying the context in which IDs must be unique.
	 */
	Object getSequenceName(Object context);

}
