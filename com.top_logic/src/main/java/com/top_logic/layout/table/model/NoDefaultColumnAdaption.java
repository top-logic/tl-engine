/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * {@link TableConfigurationProvider} that does not adapt the default column by default.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 * @deprecated Directly implement {@link TableConfigurationProvider}, since it has a default
 *             implementation for adapting the default configuration that does nothing.
 */
@Deprecated
public abstract class NoDefaultColumnAdaption implements TableConfigurationProvider {
	// No longer required.
}
