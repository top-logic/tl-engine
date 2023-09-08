/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.io.PrintWriter;

import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.knowledge.service.CreateTablesContext;
import com.top_logic.knowledge.service.DropTablesContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseSetup;

/**
 * {@link KnowledgeBaseSetup} that stores the {@link SchemaSetup} for the {@link KnowledgeBase} in
 * the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StoreSchemaSetupAction extends KnowledgeBaseSetup {

	@Override
	public void doCreateTables(CreateTablesContext context) throws Exception {
		((DBKnowledgeBase) context.getKnowledgeBase()).storeSchemaSetup(context);
	}

	@Override
	public void doDropTables(DropTablesContext context, PrintWriter out) throws Exception {
		// nothing to drop here
	}

}

