/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.format;

import java.text.Format;

import com.top_logic.basic.config.ConfigurationException;

/**
 * Algorithm instantiating a {@link Format}.
 * 
 * <p>
 * Note: A {@link Format} cannot be configured directly, because {@link Format} instances are not
 * generally thread-safe.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormatProvider {

	/**
	 * Allocates the {@link Format} instance.
	 * 
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 */
	Format createFormat() throws ConfigurationException;

}