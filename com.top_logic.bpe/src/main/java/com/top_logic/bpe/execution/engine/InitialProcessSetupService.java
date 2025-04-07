/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.BufferingI18NLog.Entry;
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
import com.top_logic.bpe.bpml.importer.BPMLImporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * {@link ManagedClass} loading initial BPML workflow definitions files from
 * <code>WEB-INF/workflow</code> during application startup.
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class InitialProcessSetupService extends ManagedClass {

	private static final String INITIAL_WORKFLOW_PROPERTY_PREFIX = "initial-workflow.";

	private static final String DATA_PATH = "/WEB-INF/workflow/";

	/**
	 * File name suffix required for initial data files to be automatically picked up.
	 */
	public static final String[] FILE_SUFFIXES = { ".bpml", ".bpmn", ".bpml.xml", ".bpmn.xml" };

	@Override
	protected void startUp() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();

		Predicate<? super String> requiresLoading = name -> {
			try {
				return DBProperties.getProperty(readCon, DBProperties.GLOBAL_PROPERTY,
					INITIAL_WORKFLOW_PROPERTY_PREFIX + name) == null;
			} catch (SQLException ex) {
				Logger.error("Cannot test whether data is already loaded.", ex, InitialProcessSetupService.class);
				return false;
			}
		};

		List<String> resourceNames;
		try {
			resourceNames = FileManager.getInstance()
				.getResourcePaths(DATA_PATH)
				.stream()
				.map(s -> s.substring(DATA_PATH.length()))
				.filter(InitialProcessSetupService::isWorkflowFile)
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
					Logger.info("Loading initial workflow: " + resource, InitialProcessSetupService.class);

					try {
						BinaryData bpml = FileManager.getInstance().getData(resource);

						importWorkflow(kb, bpml);
					} catch (Exception ex) {
						Logger.error("Cannot load initial workflow: " + resource, ex, InitialProcessSetupService.class);
					}

					try {
						DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY,
							INITIAL_WORKFLOW_PROPERTY_PREFIX + name, "loaded");
					} catch (SQLException ex) {
						Logger.error("Cannot mark as loaded: " + resource, ex, InitialProcessSetupService.class);
					}
				}

				tx.commit();
			}
		}

	}

	private static boolean isWorkflowFile(String fileName) {
		for (String suffix : FILE_SUFFIXES) {
			if (fileName.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a new {@link Collaboration} from the given BPML data.
	 */
	public static Object importWorkflow(KnowledgeBase kb, BinaryData bpml) {
		Collaboration collaboration;
		try {
			collaboration = importBPML(DefaultDisplayContext.getDisplayContext(), kb, bpml);
		} catch (XMLStreamException | IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED__DETAILS.fill(ex.getMessage()), ex)
				.initSeverity(ErrorSeverity.WARNING);
		}
		if (StringServices.isEmpty(collaboration.getName())) {
			collaboration.setName(strip(bpml.getName()));
		}

		return collaboration;
	}

	/**
	 * Loads the BPML from the given data and creates a {@link Collaboration}.
	 *
	 * @param context
	 *        The command context.
	 * @param kb
	 *        The target {@link KnowledgeBase}.
	 * @param data
	 *        The BPML source code.
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
	 * Loads the BPML from the given data and creates a {@link Collaboration}.
	 *
	 * @param source
	 *        The BPML source code.
	 * @return The newly created {@link Collaboration}.
	 */
	public static Collaboration importBPML(Source source, ModelBinding binding) throws XMLStreamException {
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(
			new LogProtocol(InitialProcessSetupService.class)
				.asI18NLog(Resources.getSystemInstance())
				.filter(Level.FATAL));
		BPMLImporter importer = new BPMLImporter(log);
		Collaboration newCollaboration = importer.importBPML(binding, source);
		if (errors.hasEntries()) {
			// Hack to get a structured error display: Join multiple errors in a chain of
			// TopLogicException instances.
			Throwable details = null;
			List<Entry> entries = errors.getEntries();
			for (int n = entries.size() - 1; n >= 0; n--) {
				Entry e = entries.get(n);
				details = new TopLogicException(e.getMessage(), details);
			}
			throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED, details)
				.initSeverity(ErrorSeverity.WARNING);
		}
		return newCollaboration;
	}

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

