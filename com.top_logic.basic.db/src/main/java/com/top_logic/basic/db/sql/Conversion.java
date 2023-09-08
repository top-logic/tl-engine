/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.Map;


/**
 * The class {@link Conversion} converts an application value to an SQL usable object.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Conversion {

	/**
	 * Converts the argument to itself.
	 */
	Conversion IDENTITY = new Conversion() {

		@Override
		public Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments) {
			return argument;
		}

	};

	/**
	 * Converts the given argument to an SQL usable argument.
	 * 
	 * @param argument
	 *        The argument value to convert.
	 * @param argumentIndexByName
	 *        Index of all query argument names to their index in the argument array.
	 * @param arguments
	 *        The argument array with all query arguments.
	 * @return The converted argument value.
	 */
	Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments);

}

