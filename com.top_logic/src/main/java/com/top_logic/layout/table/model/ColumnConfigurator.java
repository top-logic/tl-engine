/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * Algorithm applying values to a {@link ColumnConfiguration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnConfigurator {

	/**
	 * Configures the given {@link ColumnConfiguration}.
	 */
	void adapt(ColumnConfiguration column);

}
