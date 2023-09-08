/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.ResourceView;

/**
 * {@link TableConfigurationProvider} that only sets {@link TableConfiguration#getResPrefix()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetTableResPrefix extends NoDefaultColumnAdaption {

	private final ResourceView _resPrefix;

	/**
	 * Creates a {@link SetTableResPrefix}.
	 * 
	 * @param resPrefix
	 *        The prefix to set to {@link TableConfiguration#getResPrefix()}.
	 */
	public SetTableResPrefix(ResourceView resPrefix) {
		_resPrefix = resPrefix;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setResPrefix(_resPrefix);
	}

}
