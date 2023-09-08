/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.io.PrintWriter;
import java.sql.SQLException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.db2.DBTypeRepository;

/**
 * {@link KnowledgeBaseSetup} creating the tables in the {@link KnowledgeBase}.
 * 
 * @see InitialTableSetup
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InitialKBTableSetup extends KnowledgeBaseSetup {

	@Override
	public void doCreateTables(CreateTablesContext context) {
		StringBuilder createInfo = new StringBuilder();
		createInfo.append("Creating tables in knowledge base '");
		createInfo.append(context.getKBConfig().getName());
		createInfo.append("'");
		context.info(createInfo.toString());
		try {
			KnowledgeBase kb = context.getKnowledgeBase();
			ConnectionPool pool = context.getConnectionPool();
			kb.getSchemaSetup().createTables(pool, kb.getMORepository(), false);
		} catch (SQLException ex) {
			context.fatal(createInfo.append(" failed").toString(), ex);
		}
	}

	@Override
	public void doDropTables(DropTablesContext context, PrintWriter out) {
		StringBuilder dropInfo = new StringBuilder();
		dropInfo.append("Dropping tables in knowledge base '");
		dropInfo.append(context.getKBConfig().getName());
		dropInfo.append("'");
		context.info(dropInfo.toString());
		try {
			KnowledgeBaseConfiguration kbConfig = context.getKBConfig();
			SchemaSetup schemaSetup = KBUtils.getSchemaConfigResolved(kbConfig);

			ConnectionPool pool = context.getConnectionPool();
			MORepository typeRepository = DBTypeRepository.newRepository(context.getConnectionPool().getSQLDialect(),
				schemaSetup, !kbConfig.getDisableVersioning());
			if (out == null) {
				schemaSetup.resetTables(pool, typeRepository, false);
			} else {
				schemaSetup.printResetTables(out, typeRepository, pool.getSQLDialect(), false);
			}
		} catch (SQLException | DataObjectException | ConfigurationException ex) {
			context.error(dropInfo.append(" failed").toString(), ex);
		}
	}

}

