/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static java.util.Objects.*;

import java.util.function.Consumer;

/**
 * A {@link TableConfigurationProvider} that will call the given {@link Consumer} when
 * {@link #adaptConfigurationTo(TableConfiguration)} is called.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FunctionalTableConfigProvider extends NoDefaultColumnAdaption {

	private final Consumer<TableConfiguration> _tableConfigProvider;

	/**
	 * Creates a {@link FunctionalTableConfigProvider} that will call the given {@link Consumer}
	 * when {@link #adaptConfigurationTo(TableConfiguration)} is called.
	 * 
	 * @param tableConfigProvider
	 *        Is not allowed to be null.
	 */
	public FunctionalTableConfigProvider(Consumer<TableConfiguration> tableConfigProvider) {
		_tableConfigProvider = requireNonNull(tableConfigProvider);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration tableConfig) {
		_tableConfigProvider.accept(tableConfig);
	}

}
