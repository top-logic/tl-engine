/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseSetup;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.util.model.ModelService;

/**
 * {@link KnowledgeBaseSetup} loading initial data from XML files in <code>WEB-INF/data</code>.
 * 
 * <p>
 * Initial data files can be produced by {@link XMLInstanceExporter} or in the UI from the export
 * command in the instance editor.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class InitialDataSetupService extends ManagedClass {

	private static final String DATA_PATH = "/WEB-INF/data/";
	/**
	 * File name suffix required for initial data files to be automatically picked up.
	 */
	public static final String FILE_SUFFIX = ".objects.xml";

	@Override
	protected void startUp() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();

		Predicate<? super String> requiresLoading = name -> {
			try {
				return DBProperties.getProperty(readCon, DBProperties.GLOBAL_PROPERTY,
					"initial-data." + name) == null;
			} catch (SQLException ex) {
				Logger.error("Cannot test whether data is already loaded.", ex, InitialDataSetupService.class);
				return false;
			}
		};

		List<String> resourceNames;
		try {
			resourceNames = FileManager.getInstance()
				.getResourcePaths(DATA_PATH)
				.stream()
				.map(s -> s.substring(DATA_PATH.length()))
				.filter(s -> s.endsWith(FILE_SUFFIX))
				.filter(requiresLoading)
				.sorted()
				.toList();
		} finally {
			pool.releaseReadConnection(readCon);
		}

		if (!resourceNames.isEmpty()) {
			try (Transaction tx = kb.beginTransaction(Messages.INITIAL_IMPORT)) {
				CommitContext commitContext = KBUtils.getCurrentContext(kb);
				PooledConnection commitCon = commitContext.getConnection();

				for (String name : resourceNames) {
					String resource = DATA_PATH + name;
					Logger.info("Loading initial data: " + resource, InitialDataSetupService.class);

					try {
						BinaryData data = FileManager.getInstance().getData(resource);
						ObjectsConf config = XMLInstanceImporter.loadConfig(data);
						ModelService modelService = ModelService.getInstance();
						TLModel model = modelService.getModel();
						TLFactory factory = modelService.getFactory();
						XMLInstanceImporter importer = new XMLInstanceImporter(model, factory);
						importer.importInstances(config);
					} catch (ConfigurationException ex) {
						Logger.error("Cannot load initial data: " + resource, ex, InitialDataSetupService.class);
					}

					try {
						DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY, "initial-data." + name,
							"loaded");
					} catch (SQLException ex) {
						Logger.error("Cannot mark as loaded: " + resource, ex, InitialDataSetupService.class);
					}
				}

				tx.commit();
			}
		}

	}

	/**
	 * Singleton holder for the {@link InitialDataSetupService}.
	 */
	public static final class Module extends TypedRuntimeModule<InitialDataSetupService> {

		/**
		 * Singleton {@link InitialDataSetupService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<InitialDataSetupService> getImplementation() {
			return InitialDataSetupService.class;
		}
	}

}

