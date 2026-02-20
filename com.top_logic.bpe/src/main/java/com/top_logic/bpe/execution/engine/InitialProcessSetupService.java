/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.importer.BPMLImporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.util.Updater;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.tool.boundsec.config.AccessConfigurationSetupService;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * {@link ManagedClass} that imports BPML and BPMN workflow definition files from
 * {@value #DATA_PATH} during application startup.
 *
 * <p>
 * Each file is imported when it is encountered for the first time or when its content has changed
 * since the last import. The content hash of each file is persisted so that unchanged files are
 * skipped on subsequent startups.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,

	// Workflows may refer to groups, therefore, those must be created first.
	InitialGroupManager.Module.class,
})
public class InitialProcessSetupService extends ManagedClass {

	/**
	 * Marker value stored in {@link DBProperties} in legacy installations to indicate that a
	 * workflow file was imported, before hash-based change detection was introduced. When this value
	 * is found instead of a hash, the workflow is re-imported so that the proper hash gets written.
	 *
	 * @deprecated The content hash of the workflow file is stored instead.
	 */
	@Deprecated
	private static final String LOADED = "loaded";

	/** Prefix for the {@link DBProperties} keys used to store the per-file content hashes. */
	private static final String INITIAL_WORKFLOW_PROPERTY_PREFIX = "initial-workflow.";

	/** Webapp-relative directory from which workflow definition files are loaded. */
	private static final String DATA_PATH = "/WEB-INF/workflow/";

	/**
	 * File name suffixes that identify workflow definition files in {@value #DATA_PATH} for
	 * automatic import.
	 */
	public static final String[] FILE_SUFFIXES = { ".bpml", ".bpmn", ".bpml.xml", ".bpmn.xml" };

	@Override
	protected void startUp() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		Resource[] resources = FileManager.getInstance()
			.getResourcePaths(DATA_PATH)
			.stream()
			.map(s -> s.substring(DATA_PATH.length()))
			.filter(InitialProcessSetupService::isWorkflowFile)
			.sorted()
			.map(Resource::loadResource)
			.filter(Objects::nonNull)
			.toArray(Resource[]::new);
		if (resources.length == 0) {
			Logger.info("No workflows found in " + DATA_PATH + ".", InitialProcessSetupService.class);
			return;
		}

		List<Resource> newProcesses = new ArrayList<>();
		List<Resource> updatedProcesses = new ArrayList<>();

		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();
		try {
			for (Resource resource : resources) {
				String storedHash;
				try {
					storedHash = loadHash(readCon, resource);
				} catch (SQLException ex) {
					Logger.error("Cannot test whether data is already loaded.", ex, InitialProcessSetupService.class);
					continue;
				}
				if (LOADED.equals(storedHash)) {
					// legacy. Update data.
					updatedProcesses.add(resource);
					continue;
				}
				if (storedHash == null) {
					// not yet loaded
					newProcesses.add(resource);
					continue;
				}
				if (!storedHash.equals(resource.hash())) {
					// Hash has changed => file content has changed.
					updatedProcesses.add(resource);
					continue;
				}
			}
		} finally {
			pool.releaseReadConnection(readCon);
		}

		if (newProcesses.isEmpty() && updatedProcesses.isEmpty()) {
			Logger.info("All workflows in " + DATA_PATH + " are up to date.", InitialProcessSetupService.class);
			return;
		}

		try (Transaction tx = kb.beginTransaction(I18NConstants.IMPORTED_WORKFLOWS)) {
			CommitContext commitContext = KBUtils.getCurrentContext(kb);
			PooledConnection commitCon = commitContext.getConnection();
			
			if (!newProcesses.isEmpty()) {
				for (Resource resource : newProcesses) {
					String resourcePath = DATA_PATH + resource.name();
					Logger.info("Loading initial workflow: " + resourcePath, InitialProcessSetupService.class);

					try {
						importWorkflow(kb, resource.data());
					} catch (Exception ex) {
						Logger.error("Cannot load initial workflow: " + resourcePath, ex,
							InitialProcessSetupService.class);
					}

					try {
						storeHash(commitCon, resource);
					} catch (SQLException ex) {
						Logger.error("Cannot mark as loaded: " + resourcePath, ex, InitialProcessSetupService.class);
					}
				}
			}

			if (!updatedProcesses.isEmpty()) {
				Map<String, List<Collaboration>> collaborationsByName = BPEUtil.collaborationsByName();
				for (Resource resource : updatedProcesses) {
					String resourcePath = DATA_PATH + resource.name();
					BinaryData data = resource.data();

					Collaboration processToUpdate;
					String collaborationName = defaultCollaborationName(data);
					List<Collaboration> collaborations =
						collaborationsByName.getOrDefault(collaborationName, Collections.emptyList());
					switch (collaborations.size()) {
						case 0:
							Logger.warn("No workflow with name '" + collaborationName
								+ "' found. This may happen when the collaboration has a name which defers from the name of its definition file: "
								+ resourcePath, InitialProcessSetupService.class);
							continue;
						case 1:
							processToUpdate = collaborations.get(0);
							Logger.info("Update initial workflow: " + resourcePath, InitialProcessSetupService.class);
							break;
						default:
							Logger.warn("Multiple workflows with name '" + collaborationName
								+ "' found: " + resourcePath, InitialProcessSetupService.class);
							continue;
					}

					try {
						Collaboration newWorkflow = importWorkflow(kb, data);
						new Updater(processToUpdate, newWorkflow, true).update();
					} catch (Exception ex) {
						Logger.error("Cannot load workflow to update: " + resourcePath, ex,
							InitialProcessSetupService.class);
					}

					try {
						storeHash(commitCon, resource);
					} catch (SQLException ex) {
						Logger.error("Cannot mark as loaded: " + resourcePath, ex, InitialProcessSetupService.class);
					}
				}
			}

			tx.commit();
		}
	}

	/**
	 * Persists the content hash of the given resource in {@link DBProperties} so that the file can
	 * be skipped on the next startup if its content has not changed.
	 */
	private static void storeHash(PooledConnection commitCon, Resource resource) throws SQLException {
		DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY, propertyName(resource), resource.hash());
	}

	/**
	 * Returns the content hash stored for the given resource in {@link DBProperties}, or
	 * <code>null</code> if the file has not been imported before.
	 */
	private static String loadHash(PooledConnection readCon, Resource resource) throws SQLException {
		return DBProperties.getProperty(readCon, DBProperties.GLOBAL_PROPERTY, propertyName(resource));
	}

	/** Returns the {@link DBProperties} key used to store the content hash of the given resource. */
	private static String propertyName(Resource resource) {
		return INITIAL_WORKFLOW_PROPERTY_PREFIX + resource.name();
	}

	/** Returns whether the given file name ends with one of the known {@link #FILE_SUFFIXES}. */
	private static boolean isWorkflowFile(String fileName) {
		for (String suffix : FILE_SUFFIXES) {
			if (fileName.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parses the given BPML file and creates a new {@link Collaboration} in the
	 * {@link KnowledgeBase}.
	 *
	 * <p>
	 * If the parsed collaboration has no name, the file name (stripped of path and extension) is
	 * used as a fallback.
	 * </p>
	 *
	 * @param kb
	 *        The target {@link KnowledgeBase}.
	 * @param bpml
	 *        The BPML source file.
	 * @return The newly created {@link Collaboration}.
	 * 
	 * @see #importBPML(DisplayContext, KnowledgeBase, BinaryContent)
	 */
	public static Collaboration importWorkflow(KnowledgeBase kb, BinaryData bpml) {
		Collaboration collaboration;
		try {
			collaboration = importBPML(DefaultDisplayContext.getDisplayContext(), kb, bpml);
		} catch (XMLStreamException | IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED__DETAILS.fill(ex.getMessage()), ex)
				.initSeverity(ErrorSeverity.WARNING);
		}
		if (StringServices.isEmpty(collaboration.getName())) {
			collaboration.setName(defaultCollaborationName(bpml));
		}

		return collaboration;
	}

	private static String defaultCollaborationName(BinaryData bpml) {
		return strip(bpml.getName());
	}

	/**
	 * Parses the given BPML content and creates a new {@link Collaboration} in the
	 * {@link KnowledgeBase}.
	 *
	 * @param context
	 *        The current display context.
	 * @param kb
	 *        The target {@link KnowledgeBase}.
	 * @param data
	 *        The BPML source to parse.
	 * @return The newly created {@link Collaboration}.
	 */
	public static Collaboration importBPML(DisplayContext context, KnowledgeBase kb, BinaryContent data)
			throws XMLStreamException, IOException {
		try (InputStream in = data.getStream()) {
			ModelBinding binding = new ApplicationModelBinding(kb, ModelService.getApplicationModel());
			return importBPML(new StreamSource(in), binding);
		}
	}

	/**
	 * Parses the given BPML source and creates a new {@link Collaboration} using the provided model
	 * binding.
	 *
	 * @param source
	 *        The BPML source to parse.
	 * @param binding
	 *        The {@link ModelBinding} used to resolve and create model objects.
	 * @return The newly created {@link Collaboration}.
	 * @throws XMLStreamException
	 *         If the BPML source contains errors that prevent a valid import.
	 */
	public static Collaboration importBPML(Source source, ModelBinding binding) throws XMLStreamException {
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(
			new LogProtocol(InitialProcessSetupService.class)
				.asI18NLog(Resources.getSystemInstance())
				.filter(Level.FATAL));
		BPMLImporter importer = new BPMLImporter(log);
		Collaboration newCollaboration = importer.importBPML(binding, source);
		if (errors.hasEntries(Level.WARN)) {
			throw errors.asException(I18NConstants.ERROR_IMPORT_FAILED);
		}
		return newCollaboration;
	}

	/**
	 * Strips the leading path and the first dot-separated extension from the given resource name,
	 * returning a plain base name suitable for use as a workflow display name.
	 */
	private static String strip(String name) {
		int slashIndex = name.lastIndexOf('/');
		if (slashIndex >= 0) {
			name = name.substring(slashIndex + 1);
		}
		int dotIndex = name.indexOf('.');
		if (dotIndex >= 0) {
			name = name.substring(0, dotIndex);
		}
		return name;
	}

	/**
	 * A workflow resource file to be imported, identified by its name, binary content, and content
	 * hash.
	 */
	record Resource(String name, BinaryData data, String hash) {

		/**
		 * Loads the workflow file with the given name from {@value InitialProcessSetupService#DATA_PATH}
		 * and computes its content hash.
		 *
		 * @param name
		 *        File name relative to {@value InitialProcessSetupService#DATA_PATH}.
		 * @return The loaded {@link Resource}, or <code>null</code> if the content hash could not be
		 *         computed due to an I/O error.
		 */
		static Resource loadResource(String name) {
			BinaryData data = FileManager.getInstance().getData(DATA_PATH + name);
			String hash = AccessConfigurationSetupService.hash(data);
			if (hash == null) {
				// Error occurred. Was already logged.
				return null;
			}
			return new Resource(name, data, hash);
		}

	}

	/**
	 * Singleton holder for the {@link InitialProcessSetupService}.
	 */
	public static final class Module extends TypedRuntimeModule<InitialProcessSetupService> {

		/**
		 * Singleton {@link InitialProcessSetupService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<InitialProcessSetupService> getImplementation() {
			return InitialProcessSetupService.class;
		}
	}

}

