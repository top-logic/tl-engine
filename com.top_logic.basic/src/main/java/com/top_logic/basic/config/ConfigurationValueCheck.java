/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Algorithm checking a typed configuration value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfigurationValueCheck<T> {

	/**
	 * Checks whether the given value is possibly a legal value for a {@link PropertyDescriptor}
	 * this {@link ConfigurationValueCheck} is responsible for.
	 * 
	 * <p>
	 * <b>Note:</b> The signature is {@link Object} (instead of the type parameter) as the check is
	 * triggered by the generic mechanism, which may check that the value is of the correct raw
	 * type, but it is impossible to check a potential parameter type.
	 * </p>
	 * 
	 * @param value
	 *        The value that should be checked, whether it is allowed to set as configuration value.
	 */
	boolean isLegalValue(Object value);

	/**
	 * Returns the default value which the property has if no one is set.
	 */
	T defaultValue();
	
	/**
	 * Normalizes the given value to a legal value if possible (e.g.
	 * <code>null</code> may be normalized to the empty string or an empty
	 * list). If it is impossible to normalize it, the original value is
	 * returned.
	 * 
	 * So it is not guaranteed the the returned value is adequate for this
	 * {@link ConfigurationValueCheck}, but in some cases (mostly border cases
	 * such as <code>null</code>) it is.
	 * 
	 * @param value
	 *        value to normalize
	 */
	Object normalize(Object value);

}
