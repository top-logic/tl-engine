/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

/**
 * Factory of {@link Generator} for some special arguments.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface GeneratorSpi {

	/**
	 * Returns an {@link Generator} appropriate for the given arguments.
	 * 
	 * @param config
	 *        configuration for the returned {@link Generator}
	 * @return never <code>null</code>
	 */
	Generator bind(String... config);

}
