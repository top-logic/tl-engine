/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.CreateTablesContext;
import com.top_logic.knowledge.service.DropTablesContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseSetup;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.TypeSystemConfiguration;
import com.top_logic.knowledge.service.xml.KnowledgeBaseImporter;

/**
 * {@link KnowledgeBaseSetup} creating the initial data for the {@link KnowledgeBase}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InitialKBDataSetup extends KnowledgeBaseSetup {

	@Override
	public void doCreateTables(CreateTablesContext context) throws Exception {
		DBKnowledgeBase kb = (DBKnowledgeBase) context.getKnowledgeBase();
		setupInitialData(context, kb);
	}

	/**
	 * Creates the initial data for the given {@link KnowledgeBase}.
	 * 
	 * @param protocol
	 *        The {@link Protocol} to log messages to.
	 * @param kb
	 *        The {@link KnowledgeBase} to create initial data for.
	 */
	@FrameworkInternal
	public void setupInitialData(Protocol protocol, DBKnowledgeBase kb) {
		createTrunk(protocol, kb);
		try (Transaction tx = kb.beginTransaction(Messages.INITIAL_IMPORT)) {
			protocol.info("Importing initial data.");
			importInitialData(protocol, kb);
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			protocol.error("Setting up initial data failed.", ex);
		}
	}

	private void createTrunk(Log log, DBKnowledgeBase kb) {
		log.info("Creating initial branch.");
		kb.createBranch(null, Revision.FIRST_REV, Collections.<MetaObject> emptySet());
		kb.installTrunk();
	}

	private void importInitialData(Protocol protocol, KnowledgeBase kb) {
		ApplicationTypes config = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
		SchemaConfiguration schema = config.getTypeSystem(kb.getConfiguration().getTypeSystem()).getConfig();
		if (schema instanceof TypeSystemConfiguration) {
			TypeSystemConfiguration typeSystem = (TypeSystemConfiguration) schema;
			List<ResourceDeclaration> importData = typeSystem.getInitialData();
			for (ResourceDeclaration declaration : importData) {
				importInitialData(kb, protocol, declaration);
			}
		}
	}

	private void importInitialData(KnowledgeBase kb, Protocol protocol, ResourceDeclaration config) {
		String resource = config.getResource();
		if (resource != null) {
			Logger.info("Loading initial data from '" + resource + "'.", DBKnowledgeBase.class);
			importData(kb, protocol, resource);
		}
	}

	private void importData(KnowledgeBase kb, Protocol protocol, String resource) {
		KnowledgeBaseImporter.importObjects(kb, resource, !KnowledgeBaseImporter.USE_IDS, protocol);
	}

	@Override
	public void doDropTables(DropTablesContext context, PrintWriter out) throws Exception {
		// Do not drop content.
	}

}

