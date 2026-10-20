/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
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
 * Upsert semantics depend on the resolver declared in the &lt;resolvers&gt; block of the XML
 * file. With {@link NameBasedInstanceResolver}, an existing instance with the same
 * {@code name} is deleted and re-created from the XML, making the file the authoritative
 * source of truth.
 * </p>
 *
 * <p>
 * A restart after the import does not import the file a second time.
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
			// No export has happened yet, so there is nothing to import. This is an expected state
			// (not an error): guide the user to the export button instead of a generic failure.
			Logger.info("No initial-data file to import yet: " + resource, ImportFromDataFolderCommand.class);
			InfoService.showWarning(I18NConstants.IMPORT_FROM_DEV_ENV_NO_DATA);
			return HandlerResult.DEFAULT_RESULT;
		}

		ObjectsConf objects;
		try {
			objects = XMLInstanceImporter.loadConfig(data);
		} catch (ConfigurationException ex) {
			Logger.error("Cannot parse initial-data file: " + resource, ex, ImportFromDataFolderCommand.class);
			InfoService.showError(I18NConstants.IMPORT_FROM_DEV_ENV_PARSE_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		ImportOutcome outcome;
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			ModelService modelService = ModelService.getInstance();
			outcome = runImport(objects, modelService.getModel(), modelService.getFactory());
			tx.commit();
		} catch (RuntimeException ex) {
			Logger.error("Failed to re-import initial-data file: " + resource, ex, ImportFromDataFolderCommand.class);
			InfoService.showError(I18NConstants.IMPORT_FROM_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		markAsLoaded(fileName);

		// The import deletes and recreates every matched instance, so any object the component still
		// points at (the current selection / open detail form) is now a deleted object. Rendering
		// such a selection trips over DeletedObjectAccess and makes a successful import look broken.
		// Clear the selection and rebuild the table model from the freshly imported instances.
		refreshAfterImport(component);

		if (outcome.hasProblems()) {
			// The importer skips objects it cannot fully import but still commits the rest. Report a
			// warning listing the problems rather than an unconditional success.
			InfoService.showWarningList(
				I18NConstants.IMPORT_FROM_DEV_ENV_PARTIAL__PROBLEMS_TOTAL.fill(
					Integer.valueOf(outcome.problemCount()), Integer.valueOf(outcome.totalCount())),
				outcome.problems());
		} else {
			InfoService.showInfo(I18NConstants.IMPORT_FROM_DEV_ENV_SUCCESS);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Runs the importer for the given configuration, capturing per-object problems.
	 *
	 * <p>
	 * Must be called within an open {@link Transaction}; the caller is responsible for committing.
	 * </p>
	 *
	 * @param objects
	 *        The parsed object descriptions to import.
	 * @param model
	 *        The model the import is based on.
	 * @param factory
	 *        The factory used to create imported objects.
	 * @return The {@link ImportOutcome} describing what was imported and which objects had problems.
	 */
	static ImportOutcome runImport(ObjectsConf objects, TLModel model, TLFactory factory) {
		BufferingI18NLog problemLog = new BufferingI18NLog();
		XMLInstanceImporter importer = new XMLInstanceImporter(model, factory);
		importer.setLog(problemLog);

		List<TLObject> imported = importer.importInstances(objects);

		int total = imported.size();
		List<ResKey> problems = new ArrayList<>();
		for (BufferingI18NLog.Entry entry : problemLog.getEntries()) {
			if (entry.getLevel().is(Level.ERROR)) {
				problems.add(entry.getMessage());
			}
		}

		// Keep the full detail in the server log for developers.
		problemLog.forwardToApplicationLog(ImportFromDataFolderCommand.class);

		return new ImportOutcome(total, problems);
	}

	/**
	 * Drops the component selection and invalidates its model after an import, so the editor never
	 * keeps a deleted object selected.
	 */
	private static void refreshAfterImport(LayoutComponent component) {
		if (component instanceof Selectable) {
			((Selectable) component).clearSelection();
		}
		component.invalidate();
	}

	/**
	 * Result of a {@link #runImport(ObjectsConf, TLModel, TLFactory)} call.
	 */
	static final class ImportOutcome {

		private final int _totalCount;

		private final List<ResKey> _problems;

		ImportOutcome(int totalCount, List<ResKey> problems) {
			_totalCount = totalCount;
			_problems = problems;
		}

		/**
		 * The number of top-level objects the import processed.
		 */
		int totalCount() {
			return _totalCount;
		}

		/**
		 * The number of problems reported while importing.
		 */
		int problemCount() {
			return _problems.size();
		}

		/**
		 * The problem messages reported while importing, one per skipped value or object.
		 */
		List<ResKey> problems() {
			return _problems;
		}

		/**
		 * Whether any object could not be fully imported.
		 */
		boolean hasProblems() {
			return !_problems.isEmpty();
		}
	}

	/**
	 * Stamps the {@code initial-data.<fileName>} flag in {@link DBProperties} so the
	 * {@code InitialDataSetupService} skips this file on the next startup, matching the
	 * post-startup-load behaviour and avoiding a second re-import on the next boot.
	 *
	 * <p>
	 * If the marker was already "loaded" (the typical case) it stays loaded. If the sibling
	 * {@link ExportToDataFolderCommand} cleared it, it is re-set here, so the same
	 * restart-safe behaviour applies either way.
	 * </p>
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
