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
 * {@link SetupContext} used for {@link DBSetupAction#doCreateTables(CreateTablesContext)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateTablesContext extends SetupContext {

	private KnowledgeBase _kb;

	/**
	 * @see SetupContext#SetupContext(Protocol, KnowledgeBaseConfiguration)
	 */
	public CreateTablesContext(Protocol protocol, KnowledgeBaseConfiguration kbConfig) {
		super(protocol, kbConfig);
	}

	/**
	 * @see SetupContext#SetupContext(Protocol, String)
	 */
	public CreateTablesContext(Protocol protocol, String kb) {
		super(protocol, kb);
	}

	/**
	 * @see SetupContext#SetupContext(Protocol)
	 */
	public CreateTablesContext(Protocol protocol) {
		super(protocol);
	}

	@Override
	public ConnectionPool getConnectionPool() {
		// Prevent the table setup from loading the persistency layer, if no setup is required.
		// Otherwise, no migration is possible that fixes the baseline of the persistency model,
		// because the table setup already fails.
		return ConnectionPoolRegistry.getConnectionPool(getKBConfig().getConnectionPool());
	}

	/**
	 * The {@link KnowledgeBase} to set up. The returned {@link KnowledgeBase} is not started but
	 * initialised.
	 */
	public KnowledgeBase getKnowledgeBase() {
		if (_kb == null) {
			KnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(this, getKBConfig());
			checkErrors();
			_kb = kb;
		}
		return _kb;
	}

}

