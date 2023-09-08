/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import com.top_logic.basic.util.ResKey;

/**
 * Provider for {@link ResKey} for {@link RegexpConstraint}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface RegexpErrorKey {

	/**
	 * Gets the error message for the failing {@link RegexpConstraint}.
	 * 
	 * @param pattern
	 *        The regular expression that does not match the given input.
	 * @param input
	 *        User input that is not matched by the given pattern.
	 */
	ResKey getKey(String pattern, String input);

}

