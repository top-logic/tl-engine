/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link AbstractTableCommandProvider} that implements {@link TableCommand} directly.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DirectTableCommandProvider
		extends AbstractTableCommandProvider<AbstractTableCommandProvider.Config<?>>
		implements TableCommand {

	/**
	 * Creates a {@link DirectTableCommandProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectTableCommandProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected TableCommand getTableCommand() {
		return this;
	}

}
