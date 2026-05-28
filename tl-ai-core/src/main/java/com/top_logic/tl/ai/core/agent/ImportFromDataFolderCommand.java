/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.sql.SQLException;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * Command handler that re-imports an {@code *.objects.xml} file from
 * <code>WEB-INF/data/</code> into the running knowledge base, discarding any runtime edits to
 * the affected instances and restoring the values committed in the source tree.
 *
 * <p>
 * This is the inverse of {@link ExportToDataFolderCommand}: where the export button writes
 * runtime state back to the XML file in the dev workspace, this command takes the XML file
 * the dev workspace currently holds and pushes it back into the persistent store, in place,
 * without requiring a server restart.
 * </p>
 *
 * <p>
 * Upsert semantics depend on the resolver declared in the {@code <resolvers>} block of the
 * XML file. With {@link NameBasedInstanceResolver}, an existing instance with the same
 * {@code name} is deleted and re-created from the XML — making the file the authoritative
 * source of truth.
 * </p>
 *
 * <p>
 * After a successful import, the {@code initial-data.<fileName>} marker in {@link DBProperties}
 * is left in its current state. If the marker was already "loaded" (typical case) it stays
 * loaded, so a restart will not re-import again. If the marker was cleared by the
 * sibling export command, it is re-set, so the same restart-safe behaviour applies.
 * </p>
 */
public class ImportFromDataFolderCommand extends AbstractCommandHandler {

	private static final String DATA_PATH = "/WEB-INF/data/";

	/**
	 * Configuration options for {@link ImportFromDataFolderCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Source file name within {@code WEB-INF/data/}, e.g. {@code agents.objects.xml}.
		 */
		@Mandatory
		String getFileName();
	}

	/**
	 * Creates a new {@link ImportFromDataFolderCommand}.
	 */
	public ImportFromDataFolderCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		Config config = (Config) getConfig();
		String fileName = config.getFileName();
		String resource = DATA_PATH + fileName;

		BinaryData data = FileManager.getInstance().getDataOrNull(resource);
		if (data == null) {
			Logger.error("Initial-data file not found: " + resource, ImportFromDataFolderCommand.class);
			InfoService.showError(I18NConstants.IMPORT_FROM_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		ObjectsConf objects;
		try {
			objects = XMLInstanceImporter.loadConfig(data);
		} catch (ConfigurationException ex) {
			Logger.error("Cannot parse initial-data file: " + resource, ex, ImportFromDataFolderCommand.class);
			InfoService.showError(I18NConstants.IMPORT_FROM_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			ModelService modelService = ModelService.getInstance();
			TLModel tlModel = modelService.getModel();
			TLFactory factory = modelService.getFactory();
			XMLInstanceImporter importer = new XMLInstanceImporter(tlModel, factory);
			importer.importInstances(objects);
			tx.commit();
		} catch (RuntimeException ex) {
			Logger.error("Failed to re-import initial-data file: " + resource, ex, ImportFromDataFolderCommand.class);
			InfoService.showError(I18NConstants.IMPORT_FROM_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		markAsLoaded(fileName);

		InfoService.showInfo(I18NConstants.IMPORT_FROM_DEV_ENV_SUCCESS);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Stamps the {@code initial-data.<fileName>} flag in {@link DBProperties} so the
	 * {@code InitialDataSetupService} skips this file on the next startup — matching the
	 * post-startup-load behaviour and avoiding a second re-import on the next boot.
	 */
	private void markAsLoaded(String fileName) {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		try {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
					"initial-data." + fileName, "loaded");
				connection.commit();
			} finally {
				pool.releaseWriteConnection(connection);
			}
		} catch (SQLException ex) {
			// Non-fatal: import succeeded, just may re-run on next boot.
			Logger.warn("Could not stamp initial-data marker after re-import of " + fileName,
				ex, ImportFromDataFolderCommand.class);
		}
	}
}
