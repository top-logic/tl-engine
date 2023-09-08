/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link LabelFilterProvider} for columns displaying mandatory values.
 * 
 * @see LabelFilterProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryLabelFilterProvider extends LabelFilterProvider {

	/**
	 * Singleton {@link MandatoryLabelFilterProvider} instance.
	 */
	public static final MandatoryLabelFilterProvider INSTANCE = new MandatoryLabelFilterProvider();

	private MandatoryLabelFilterProvider() {
		super(true);
	}
}
