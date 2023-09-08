/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.dob.MOAttribute;

/**
 * Provider to get a default value for the given {@link MOAttribute}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MODefaultProvider {

	/**
	 * Creates a value for the given {@link MOAttribute}.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to create value for.
	 * @return The default value for the given attribute. May be <code>null</code>.
	 */
	Object createDefault(MOAttribute attribute);

}
