/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;

/**
 * {@link SetupContext} used for
 * {@link DBSetupAction#doDropTables(DropTablesContext, java.io.PrintWriter)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DropTablesContext extends SetupContext {

	/**
	 * @see SetupContext#SetupContext(Protocol, KnowledgeBaseConfiguration)
	 */
	public DropTablesContext(Protocol protocol, KnowledgeBaseConfiguration kbConfig) {
		super(protocol, kbConfig);
	}

	/**
	 * @see SetupContext#SetupContext(Protocol, String)
	 */
	public DropTablesContext(Protocol protocol, String kb) {
		super(protocol, kb);
	}

	/**
	 * @see SetupContext#SetupContext(Protocol)
	 */
	public DropTablesContext(Protocol protocol) {
		super(protocol);
	}

	@Override
	public ConnectionPool getConnectionPool() {
		return ConnectionPoolRegistry.getConnectionPool(getKBConfig().getConnectionPool());
	}

}

