/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;

import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SequenceTypeProvider;

/**
 * {@link DBKnowledgeBaseClusterTestSetup} for migration tests (with modified target schema).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBKnowledgeBaseMigrationTestSetup extends AbstractMultiKBTestSetup implements
		KnowledgeBaseMigrationTestScenario {

	private final List<TypeProvider> _additionalProviders = new ArrayList<>(
		KnowledgeBaseMigrationTestScenarioImpl.getMigrationTypes());
	{
		// setup sequence table. There is no problem to reset it because no other Module (e.g.
		// ClusterManager) uses the migration database.
		_additionalProviders.add(SequenceTypeProvider.INSTANCE);
	}

	private SetupKBHelper _setupKBHelper2;

	public DBKnowledgeBaseMigrationTestSetup(Test test) {
		super(test);
	}

	@Override
	protected DBKnowledgeBase setupSecondKB(KnowledgeBaseConfiguration config) throws Exception {
		_setupKBHelper2 = new SetupKBHelper(config);
		_setupKBHelper2.addAdditionalTypes(_additionalProviders);
		return _setupKBHelper2.setup();
	}

	@Override
	protected void teardownSecondKB(DBKnowledgeBase kb) throws Exception {
		_setupKBHelper2.tearDown();
	}

	@Override
	protected String getTestDBNode2() {
		return DatabaseTestSetup.MIGRATION_POOL_NAME;
	}
	
	/**
	 * Adds {@link TypeProvider} for the migration KB.
	 */
	public void addAdditionalMigrationTypes(TypeProvider provider) {
		_additionalProviders.add(provider);
	}

}
