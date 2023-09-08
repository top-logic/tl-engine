/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link CollectionLabelFilterProvider} for columns displaying a mandatory value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryCollectionLabelFilterProvider extends CollectionLabelFilterProvider {

	/**
	 * Singleton {@link MandatoryCollectionLabelFilterProvider} instance.
	 */
	public static final MandatoryCollectionLabelFilterProvider INSTANCE =
		new MandatoryCollectionLabelFilterProvider();

	private MandatoryCollectionLabelFilterProvider() {
		super(true);
	}
}
