/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.io.PrintWriter;
import java.util.Collection;

import com.top_logic.basic.module.BasicRuntimeModule;

/**
 * Action taking part of the setup of the application database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBSetupAction {

	/**
	 * Implementation of the "create tables" command.
	 */
	void doCreateTables(CreateTablesContext context) throws Exception;

	/**
	 * Implementation of the "drop tables" command.
	 */
	void doDropTables(DropTablesContext context, PrintWriter out) throws Exception;

	/**
	 * Additional dependencies to execute this {@link DBSetupAction}.
	 * 
	 * <p>
	 * {@link com.top_logic.basic.sql.ConnectionPoolRegistry.Module} must not be contained.
	 * </p>
	 */
	Collection<? extends BasicRuntimeModule<?>> getDependencies();

}
