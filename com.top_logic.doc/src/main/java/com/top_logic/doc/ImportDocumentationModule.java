/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.Date;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.properties.DBPropertiesSchema;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.doc.export.DocumentationImporter;
import com.top_logic.doc.export.TLDocExportImportConstants;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;
import com.top_logic.util.model.ModelService;

/**
 * {@link KBBasedManagedClass} importing the documentation of the application during the start up.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
	BookmarkService.Module.class,
	CommandHandlerFactory.Module.class,
})
public class ImportDocumentationModule extends KBBasedManagedClass<ImportDocumentationModule.Config> {

	/**
	 * Property name in {@link DBPropertiesSchema#TABLE_NAME} that holds the timestamp of the most
	 * recent documentation file that was imported.
	 */
	public static final String LAST_IMPORT_TIME_PROPERTY = ImportDocumentationModule.class.getName() + ".lastImport";

	/**
	 * Typed configuration interface definition for {@link ImportDocumentationModule}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends KBBasedManagedClass.Config<ImportDocumentationModule> {
		// configuration interface definition
	}

	/**
	 * Create a {@link ImportDocumentationModule}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ImportDocumentationModule(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		updateDocumentationFromFS(new LogProtocol(ImportDocumentationModule.class));
	}

	private void updateDocumentationFromFS(Log log) {
		String rootPath = TLDocExportImportConstants.ROOT_RELATIVE_PATH;
		Date lastDocModification = lastDocumentationModification();
		if (lastDocModification == null) {
			// No Documentation found.
			log.info("No documentation to import found in " + rootPath);
			return;
		}
		Date lastImport = readImportDate(log);
		if (lastImport == null || lastImport.before(lastDocModification)) {
			importDocumentation(log, rootPath);
			storeImportDate(log, lastDocModification);
		} else {
			log.info(
				"Last documentation modification " + lastDocModification + " not after last import " + lastImport + ".",
				Protocol.VERBOSE);
			log.info("No documentation change detected.");
		}
		
	}

	private void importDocumentation(Log log, String rootPath) {
		log.info("Import documentation from path '" + rootPath + "'.");
		DocumentationImporter importer;
		importer = new DocumentationImporter(rootPath);
		importer.setMissingDocumentationHandler(locale -> {
			log.info("No data for locale " + locale.getLanguage() + " for import available.");

		});
		importer.doImport(log);
		log.info("Documentation updated.");
	}

	private Date lastDocumentationModification() {
		ObjectFlag<FileTime> lastModified = new ObjectFlag<>(null);

		for (Path path : FileManager.getInstance().getPaths()) {
			Path docPath = path.resolve("doc");
			if (Files.exists(docPath)) {
				updateLastModified(lastModified, docPath);
			}
		}

		if (lastModified.get() == null) {
			return null;
		} else {
			return new Date(lastModified.get().toMillis());
		}
	}

	private void updateLastModified(ObjectFlag<FileTime> lastModified, Path root) {
		FileUtilities.walkFileTree(root, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				FileVisitResult result = super.visitFile(file, attrs);
				FileTime fileModification = attrs.lastModifiedTime();
				FileTime tmp = lastModified.get();
				if (tmp == null || tmp.compareTo(fileModification) < 0) {
					lastModified.set(fileModification);
				}
				return result;
			}

		});
	}

	private Date readImportDate(Log log) {
		PooledConnection connection = connectionPool().borrowReadConnection();
		try {
			String storedDate =
				DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, LAST_IMPORT_TIME_PROPERTY);
			if (storedDate == null) {
				return null;
			}
			return parse(storedDate);
		} catch (SQLException | NumberFormatException ex) {
			log.error("Unable to get last documentation import time", ex);
			return null;
		} finally {
			connectionPool().releaseReadConnection(connection);
		}
	}

	private void storeImportDate(Log log, Date date) {
		PooledConnection connection = connectionPool().borrowWriteConnection();
		try {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, LAST_IMPORT_TIME_PROPERTY,
				format(date));
			connection.commit();
		} catch (SQLException ex) {
			log.error("Unable to store last documentation import time.", ex);
		} finally {
			connectionPool().releaseWriteConnection(connection);
		}
	}

	private Date parse(String date) throws NumberFormatException {
		return new Date(Long.parseLong(date));
	}

	private String format(Date date) {
		return Long.toString(date.getTime());
	}

	private ConnectionPool connectionPool() {
		return KBUtils.getConnectionPool(kb());
	}

	/**
	 * Module for instantiation of the {@link ImportDocumentationModule}.
	 */
	public static class Module extends TypedRuntimeModule<ImportDocumentationModule> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ImportDocumentationModule> getImplementation() {
			return ImportDocumentationModule.class;
		}

	}

}

