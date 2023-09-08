/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

/**
 * Access to the application model to which data is imported.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelBinding extends ModelBindingBase {

	/**
	 * Whether a concrete value (that already has been imported) is instance of a certain type.
	 *
	 * @param obj
	 *        The concrete value.
	 * @param type
	 *        The (fully-qualified) name of a model type.
	 * @return Whether the given value is of the given type.
	 */
	boolean isInstanceOf(Object obj, String type);


}
