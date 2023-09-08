/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.command.TableCommandProvider;

/**
 * {@link TableCommandProvider} that wraps a {@link DeactivatedCommandModel} around one produced by
 * a delegate {@link TableCommandProvider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeactivatedTableCommandProvider implements TableCommandProvider {

	private TableCommandProvider _impl;

	/**
	 * Creates a {@link DeactivatedTableCommandProvider}.
	 *
	 * @param impl
	 *        The delegate {@link TableCommandProvider}.
	 */
	public DeactivatedTableCommandProvider(TableCommandProvider impl) {
		_impl = impl;
	}

	@Override
	public CommandModel createCommand(TableData table) {
		return new DeactivatedCommandModel(_impl.createCommand(table));
	}

}
