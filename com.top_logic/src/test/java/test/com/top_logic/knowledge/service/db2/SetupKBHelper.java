/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.db.schema.MultipleBranchSetup;
import test.com.top_logic.basic.db.schema.SchemaTestSetup;
import test.com.top_logic.basic.db.schema.setup.SchemaSetupForTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup.DBKnowledgeBaseAccess;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.InitialKBDataSetup;

/**
 * Helper class to setup a {@link KnowledgeBase} from the {@link KnowledgeBaseConfiguration}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetupKBHelper {

	private final KnowledgeBaseConfiguration _configuration;

	private final ArrayList<TypeProvider> _additionalProviders = new ArrayList<>();

	private DBKnowledgeBase kb;

	public SetupKBHelper(KnowledgeBaseConfiguration configuration) {
		_configuration = configuration;
	}

	/**
	 * Sets up the {@link KnowledgeBase}.
	 */
	public DBKnowledgeBase setup() throws Exception {
		SchemaSetup schemaSetup = newSchemaSetup(_configuration, _additionalProviders);

		Protocol log = new AssertProtocol();
		kb = new DBKnowledgeBaseAccess(schemaSetup);
		kb.initialize(log, _configuration);

		schemaSetup.resetTables(kb.getConnectionPool(), kb.getMORepository(), true);
		schemaSetup.createTables(kb.getConnectionPool(), kb.getMORepository(), true);
		dropSequences();
		new InitialKBDataSetup().setupInitialData(log, kb);
		kb.startup(log);
		return kb;
	}

	static SchemaSetup newSchemaSetup(KnowledgeBaseConfiguration kbConfig,
			Iterable<TypeProvider> additionalTypeProviders) throws ConfigurationException {
		SchemaConfiguration config = newTestSchemaConfig(kbConfig);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaSetupForTest setup = (SchemaSetupForTest) context.getInstance(config);
		for (TypeProvider provider : additionalTypeProviders) {
			setup.addTypeProvider(provider);
		}

		return setup.resolve(context);
	}

	private static SchemaConfiguration newTestSchemaConfig(KnowledgeBaseConfiguration kbConfig) {
		SchemaConfiguration schemaConfig = KBUtils.getSchemaConfig(kbConfig).getConfig();
		Decision multipleBranches = MultipleBranchSetup.multipleBranches();
		SchemaConfiguration updatedSchemaConf = SchemaTestSetup.updateMultipleBranches(schemaConfig, multipleBranches);
		if (updatedSchemaConf == schemaConfig) {
			// Need to create a copy to be able to set implementation class.
			updatedSchemaConf = TypedConfiguration.copy(schemaConfig);
		} else {
			// Update already created a new configuration. No need to create a copy
		}
		updatedSchemaConf.setImplementationClass(SchemaSetupForTest.class);
		return updatedSchemaConf;
	}

	/**
	 * Tear down the {@link KnowledgeBase}.
	 */
	public void tearDown() throws Exception {
		kb.shutDown();
		dropSequences();
		SchemaSetup schemaSetup = newSchemaSetup(_configuration, _additionalProviders);
		schemaSetup.resetTables(kb.getConnectionPool(), kb.getMORepository(), true);
		kb = null;
	}

	/**
	 * Adds the Types given by the {@link TypeProvider}s to the created {@link KnowledgeBase}.
	 */
	public void addAdditionalTypes(Collection<? extends TypeProvider> providers) {
		if (kb != null) {
			throw new IllegalStateException("Setup already executed.");
		}
		_additionalProviders.addAll(providers);
	}

	private void dropSequences() throws SQLException {
		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(_configuration.getConnectionPool());
		DropSequenceSetup.dropSequences(pool, DBKnowledgeBase.BRANCH_SEQUENCE, DBKnowledgeBase.REVISION_SEQUENCE,
			DBKnowledgeBase.ID_SEQ);

	}

}
