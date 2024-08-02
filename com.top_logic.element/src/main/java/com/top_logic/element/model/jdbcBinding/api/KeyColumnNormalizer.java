/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding.api;

import com.top_logic.element.model.jdbcBinding.api.annotate.TLForeignKeyBinding;

/**
 * Normalizes a value read form a JDBC connection to make it equal to a value in another column
 * which has the same meaning but a different representation.
 * 
 * @see TLForeignKeyBinding
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FunctionalInterface
public interface KeyColumnNormalizer {

	/**
	 * Normalizes the given value.
	 *
	 * @param value
	 *        The value read from a database.
	 * @return The normalized value.
	 */
	Object normalize(Object value);

}
