/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.TypedAnnotationContainer;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.format.PrimitiveBooleanFormat;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeTypeFactory;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.model.migration.Util;

/**
 * Context for {@link MigrationProcessor}s.
 */
public class MigrationContext extends TypedAnnotationContainer {

	private KnowledgeBaseFactoryConfig _factoryConfig;

	private SchemaSetup _applicationSetup;

	private SchemaConfiguration _applicationSchema;

	private KnowledgeBaseConfiguration _kbConfig;

	private PooledConnection _connection;

	private boolean _branchSupport;

	private final Util _util;

	private MORepository _schemaRepository;

	private final Set<MOStructure> _xrefInvalidates = new HashSet<>();

	/**
	 * Creates a {@link MigrationContext}.
	 */
	public MigrationContext(Log log, PooledConnection connection) {
		_connection = connection;
		try {
			_factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
				.getServiceConfiguration(KnowledgeBaseFactory.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		_kbConfig = _factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		_applicationSetup = KBUtils.getSchemaConfigResolved(_kbConfig);
		_applicationSchema = _applicationSetup.getConfig();
		MOFactory factory = new DBKnowledgeTypeFactory();
		_schemaRepository = _applicationSetup.createMORepository(factory);

		_branchSupport = hasStoredBranchSupport();
		_util = new Util(log, _connection, _branchSupport);
	}

	private boolean hasStoredBranchSupport() {
		// Note: The schema can potentially not be loaded as typed configuration due to schema
		// changes.
		String xml = KBSchemaUtil.loadSchemaRaw(_connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		if (xml != null && !xml.isBlank()) {
			Document document = DOMUtil.parse(xml);
			String value = document.getDocumentElement().getAttribute(SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE);
			if (value == null || value.isBlank()) {
				return SchemaConfiguration.DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE;
			}

			try {
				return ((Boolean) PrimitiveBooleanFormat.INSTANCE.getValue(
					SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE,
					value)).booleanValue();
			} catch (ConfigurationException ex) {
				return SchemaConfiguration.DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE;
			}
		}
		return _applicationSchema.hasMultipleBranches();
	}

	/**
	 * The schema currently stored as baseline in the database.
	 */
	public SchemaConfiguration getPersistentSchema() {
		return KBSchemaUtil.loadSchema(_connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
	}

	/**
	 * The resolved {@link #getPersistentSchema() type repository} read from the current DB.
	 */
	public MORepository getPersistentRepository() {
		SchemaConfiguration persistentSchema = getPersistentSchema();
		SchemaSetup setup =
			new SchemaSetup(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, persistentSchema);
		return setup.createMORepository(DefaultMOFactory.INSTANCE);
	}

	/**
	 * The schema read from the application configuration.
	 */
	public SchemaConfiguration getApplicationSchema() {
		return _applicationSchema;
	}

	/**
	 * The resolved {@link #getApplicationSchema() database schema}.
	 */
	public MORepository getSchemaRepository() {
		return _schemaRepository;
	}

	/**
	 * Whether the {@link KnowledgeBase} to be migrated has branches enabled.
	 * 
	 * @see SchemaConfiguration#hasMultipleBranches()
	 */
	public boolean hasBranchSupport() {
		return _branchSupport;
	}

	/**
	 * The {@link Util} object that can be used to update the model using SQL statements.
	 */
	public Util getSQLUtils() {
		return _util;
	}

	/**
	 * Requests rebuilding the {@link RevisionXref} table.
	 * 
	 * <p>
	 * The rebuild is deferred until the end of the migration or until
	 * {@link #revalidateXRef(Log, PooledConnection)} is called. This speeds up migration
	 * dramatically, because rebuilding the {@link RevisionXref} table is a time-consuming process.
	 * </p>
	 *
	 * @param table
	 *        The table that was modified. {@link RevisionXref} entries for this table must be
	 *        rebuilt.
	 */
	public void invalidateXRef(MOStructure table) {
		_xrefInvalidates.add(table);
	}

	/**
	 * Re-builds the {@link RevisionXref} table for those tables that have been
	 * {@link #invalidateXRef(MOStructure) invalidated}.
	 */
	public void revalidateXRef(Log log, PooledConnection connection) throws SQLException {
		for (MOStructure table : _xrefInvalidates) {
			CompiledStatement removeXref = query(
				delete(table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
					eqSQL(
						column(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
						literal(DBType.STRING, table.getName()))))
							.toSql(connection.getSQLDialect());
			int removed = removeXref.executeUpdate(connection);
			log.info("Cleared " + removed + " rows for table '" + table.getName() + "' from '"
				+ RevisionXref.REVISION_XREF_TYPE_NAME + ".");
		}

		for (MOStructure table : _xrefInvalidates) {
			CompiledStatement fillXref = query(
				insert(
					table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
					columnNames(
						SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE),
						SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE),
						SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
					selectDistinct(
						columns(
							columnDef(BasicTypes.REV_MIN_DB_NAME),
							getSQLUtils().branchColumnDef(),
							columnDef(literal(DBType.STRING, table.getName()))),
						table(table.getDBMapping().getDBName())))).toSql(connection.getSQLDialect());

			int readded = fillXref.executeUpdate(connection);
			log.info("Re-added " + readded + " rows for table '" + table.getName() + "' to '"
				+ RevisionXref.REVISION_XREF_TYPE_NAME + ".");
		}

		_xrefInvalidates.clear();
	}

}
