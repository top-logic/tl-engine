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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.logging.Level;
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
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.config.AccessConfigurationSetup;
import com.top_logic.util.Resources;
import com.top_logic.util.StartupHooks;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * Imports BPML and BPMN workflow definition files during application startup.
 *
 * <p>
 * Each file is imported when it is encountered for the first time or when its content has changed
 * since the last import. The content hash of each file is persisted so that unchanged files are
 * skipped on subsequent startups.
 * </p>
 * 
 * <p>
 * The import must run as {@link StartupHooks startup hook}, because the workflows may require any
 * service of the application, e.g. the groups they refer to.
 * </p>
 */
public class InitialProcessSetup implements StartupAction {

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

	private static final Pattern NAME_HASH_PATTERN = Pattern.compile("^(?<name>.+):(?<hash>[A-Za-z0-9/+=]+)$");

	@Override
	public void perform() {
		Resource[] resources = FileManager.getInstance()
			.getResourcePaths(DATA_PATH)
			.stream()
			.map(s -> s.substring(DATA_PATH.length()))
			.filter(InitialProcessSetup::isWorkflowFile)
			.sorted()
			.map(Resource::loadResource)
			.filter(Objects::nonNull)
			.toArray(Resource[]::new);
		if (resources.length == 0) {
			Logger.info("No workflows found in " + DATA_PATH + ".", InitialProcessSetup.class);
			return;
		}

		importWorkflows(Arrays.asList(resources));
	}

	/**
	 * Imports the given workflow files that are new or changed since their last import.
	 *
	 * <p>
	 * Each file is imported in a transaction of its own, together with storing its content hash. A
	 * file that cannot be imported is reported and leaves the knowledge base as it was: neither a
	 * partially imported workflow nor a partially updated one is kept, and its stored hash stays
	 * unchanged, so the import is tried again at the next startup. The other files are imported
	 * all the same.
	 * </p>
	 *
	 * @param resources
	 *        The workflow files, see {@link Resource#create(String, BinaryData)}.
	 */
	public static void importWorkflows(List<Resource> resources) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		List<Resource> newProcesses = new ArrayList<>();
		Map<String, List<Resource>> updatedProcesses = new HashMap<>();
		List<Resource> initHashesProcesses = new ArrayList<>();

		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();
		try {
			for (Resource resource : resources) {
				String storedHash;
				try {
					storedHash = loadHash(readCon, resource);
				} catch (SQLException ex) {
					Logger.error("Cannot test whether data is already loaded.", ex, InitialProcessSetup.class);
					continue;
				}
				if (LOADED.equals(storedHash)) {
					// legacy. Update data.
					initHashesProcesses.add(resource);
					continue;
				}
				if (storedHash == null) {
					// not yet loaded
					newProcesses.add(resource);
					continue;
				}
				Matcher matcher = NAME_HASH_PATTERN.matcher(storedHash);
				if (!matcher.matches()) {
					Logger.error(
						"Unexpected stored hash format. Expected is the format <name>:<hash>, where <name> is the name of the workflow, and <hash> is the Base64 encoded hash of the data: "
							+ storedHash,
						InitialProcessSetup.class);
					continue;
				}
				if (!matcher.group("hash").equals(resource.hash())) {
					// Hash has changed => file content has changed.
					String wfName = matcher.group("name");
					MultiMaps.add(updatedProcesses, wfName, resource, ArrayList::new);
					continue;
				}
			}
		} finally {
			pool.releaseReadConnection(readCon);
		}

		if (newProcesses.isEmpty() && updatedProcesses.isEmpty() && initHashesProcesses.isEmpty()) {
			Logger.info("All workflows in " + DATA_PATH + " are up to date.", InitialProcessSetup.class);
			return;
		}

		for (Resource resource : newProcesses) {
			String resourcePath = resourcePath(resource);
			Logger.info("Loading initial workflow: " + resourcePath, InitialProcessSetup.class);
			importInTransaction(kb, resource, "Cannot load initial workflow: " + resourcePath,
				() -> importWorkflow(kb, resource.data()).getName());
		}

		for (Resource resource : initHashesProcesses) {
			String resourcePath = resourcePath(resource);
			Logger.info("Setting initial hash for workflow: " + resourcePath, InitialProcessSetup.class);
			importInTransaction(kb, resource, "Cannot load workflow to get name: " + resourcePath, () -> {
				/* Workaround to get the correct name of the workflow if it had been created. */
				Collaboration newWorkflow = importWorkflow(kb, resource.data());
				String wfName = newWorkflow.getName();
				newWorkflow.tDelete();
				return wfName;
			});
		}

		if (!updatedProcesses.isEmpty()) {
			Map<String, List<Collaboration>> collaborationsByName = BPEUtil.collaborationsByName();
			for (Entry<String, List<Resource>> nameAndResource : updatedProcesses.entrySet()) {
				Resource resource;
				if (nameAndResource.getValue().size() == 1) {
					resource = nameAndResource.getValue().get(0);
				} else {
					Logger.warn("Multiple resources ("
						+ nameAndResource.getValue().stream().map(InitialProcessSetup::resourcePath)
							.collect(Collectors.joining(", "))
						+ ") created a workflow with name '" + nameAndResource.getKey()
						+ "'. No updated is executed.", InitialProcessSetup.class);
					continue;
				}

				String resourcePath = resourcePath(resource);
				String collaborationName = nameAndResource.getKey();
				List<Collaboration> collaborations =
					collaborationsByName.getOrDefault(collaborationName, Collections.emptyList());
				Collaboration processToUpdate;
				switch (collaborations.size()) {
					case 0:
						Logger.warn(
							"No workflow with name '" + collaborationName + "' found for resource: " + resourcePath,
							InitialProcessSetup.class);
						continue;
					case 1:
						processToUpdate = collaborations.get(0);
						Logger.info("Update initial workflow: " + resourcePath, InitialProcessSetup.class);
						break;
					default:
						Logger.warn("Multiple workflows with name '" + collaborationName
							+ "' found: " + resourcePath, InitialProcessSetup.class);
						continue;
				}

				importInTransaction(kb, resource, "Cannot load workflow to update: " + resourcePath, () -> {
					Collaboration newWorkflow = importWorkflow(kb, resource.data());
					new Updater(processToUpdate, newWorkflow, true).update();
					return processToUpdate.getName();
				});
			}
		}
	}

	/**
	 * Performs the import of a single workflow file in a transaction of its own and stores the
	 * content hash of the file in the same transaction.
	 *
	 * <p>
	 * When the import or the commit fails, the failure is logged with the given message, and the
	 * transaction is rolled back: the knowledge base keeps no part of the import, and the hash of
	 * the file is not updated.
	 * </p>
	 *
	 * @param operation
	 *        The import, resulting in the name of the imported workflow.
	 */
	private static void importInTransaction(KnowledgeBase kb, Resource resource, String failureMessage,
			Supplier<String> operation) {
		try (Transaction tx = kb.beginTransaction(I18NConstants.IMPORTED_WORKFLOWS)) {
			try {
				String wfName = operation.get();
				storeHash(KBUtils.getCurrentContext(kb).getConnection(), resource, wfName);
				tx.commit();
			} catch (Exception ex) {
				Logger.error(failureMessage, ex, InitialProcessSetup.class);
				tx.rollback();
			}
		}
	}

	private static String resourcePath(Resource resource) {
		return DATA_PATH + resource.name();
	}

	/**
	 * Persists the content hash of the given resource in {@link DBProperties} so that the file can
	 * be skipped on the next startup if its content has not changed.
	 */
	private static void storeHash(PooledConnection commitCon, Resource resource, String wfName) throws SQLException {
		// See NAME_HASH_PATTERN
		String value = wfName + ":" + resource.hash();
		DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY, propertyName(resource), value);
	}

	/**
	 * The import state stored for the workflow file of the given name: the name of the imported
	 * workflow and the content hash of the file at its last successful import, separated by a
	 * colon.
	 *
	 * @return The stored value, <code>null</code> if the file was never imported.
	 */
	public static String importState(Resource resource) throws SQLException {
		ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		PooledConnection readCon = pool.borrowReadConnection();
		try {
			return loadHash(readCon, resource);
		} finally {
			pool.releaseReadConnection(readCon);
		}
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
	 * @see #importBPML(KnowledgeBase, BinaryContent)
	 */
	public static Collaboration importWorkflow(KnowledgeBase kb, BinaryData bpml) {
		Collaboration collaboration;
		try {
			collaboration = importBPML(kb, bpml);
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
	 * 
	 * @deprecated Use {@link #importBPML(KnowledgeBase, BinaryContent)}.
	 */
	@Deprecated
	public static Collaboration importBPML(DisplayContext context, KnowledgeBase kb, BinaryContent data)
			throws XMLStreamException, IOException {
		return importBPML(kb, data);
	}

	/**
	 * Parses the given BPML content and creates a new {@link Collaboration} in the
	 * {@link KnowledgeBase}.
	 *
	 * @param kb
	 *        The target {@link KnowledgeBase}.
	 * @param data
	 *        The BPML source to parse.
	 * @return The newly created {@link Collaboration}.
	 */
	public static Collaboration importBPML(KnowledgeBase kb, BinaryContent data)
			throws XMLStreamException, IOException {
		try (InputStream in = data.getStream()) {
			// Importing a process model runs with definer's rights, see
			// AbstractModelBinding#usesSecurity().
			ModelBinding binding = new ApplicationModelBinding(kb, ModelService.getApplicationModel(), false);
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
	 * @throws TopLogicException
	 *         If the source contains no workflow definition.
	 */
	public static Collaboration importBPML(Source source, ModelBinding binding) throws XMLStreamException {
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(
			new LogProtocol(InitialProcessSetup.class)
				.asI18NLog(Resources.getSystemInstance())
				.filter(Level.FATAL));
		BPMLImporter importer = new BPMLImporter(log);
		Collaboration newCollaboration = importer.importBPML(binding, source);
		if (errors.hasEntries(Level.WARN)) {
			throw errors.asException(I18NConstants.ERROR_IMPORT_FAILED);
		}
		if (newCollaboration == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_WORKFLOW_DEFINITION)
				.initSeverity(ErrorSeverity.WARNING);
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
	 *
	 * @param name
	 *        The file name relative to {@value InitialProcessSetup#DATA_PATH}.
	 * @param data
	 *        The content of the file.
	 * @param hash
	 *        The hash of the content, stored after a successful import to recognize a changed file.
	 */
	public record Resource(String name, BinaryData data, String hash) {

		/**
		 * Loads the workflow file with the given name from {@value InitialProcessSetup#DATA_PATH}
		 * and computes its content hash.
		 *
		 * @param name
		 *        File name relative to {@value InitialProcessSetup#DATA_PATH}.
		 * @return The loaded {@link Resource}, or <code>null</code> if the content hash could not be
		 *         computed due to an I/O error.
		 */
		static Resource loadResource(String name) {
			return create(name, FileManager.getInstance().getData(DATA_PATH + name));
		}

		/**
		 * Creates the {@link Resource} of a workflow file with the given content.
		 *
		 * @param name
		 *        File name relative to {@value InitialProcessSetup#DATA_PATH}, identifying the
		 *        file between two imports.
		 * @param data
		 *        The content of the file.
		 * @return The {@link Resource}, or <code>null</code> if the content hash could not be
		 *         computed due to an I/O error.
		 */
		public static Resource create(String name, BinaryData data) {
			String hash = AccessConfigurationSetup.hash(data);
			if (hash == null) {
				// Error occurred. Was already logged.
				return null;
			}
			return new Resource(name, data, hash);
		}

	}

}
