/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.Arrays;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.element.model.PersistentModuleSingletons;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.Module;

/**
 * {@link MigrationProcessor} creating a new singleton for a module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLSingletonProcessor extends AbstractConfiguredInstance<CreateTLSingletonProcessor.Config>
		implements MigrationProcessor {

	private CreateTLObjectProcessor _singletonCreator;

	private Util _util;

	/**
	 * Configuration options of {@link CreateTLSingletonProcessor}.
	 */
	@TagName("create-singleton")
	public interface Config extends PolymorphicConfiguration<CreateTLSingletonProcessor> {

		/**
		 * Name of the module to create singleton for.
		 */
		@Mandatory
		String getModule();

		/**
		 * Name of the singleton in the module.
		 */
		@Mandatory
		String getName();

		/**
		 * Configuration of the new singleton.
		 */
		@Mandatory
		CreateTLObjectProcessor.Config getSingleton();

	}


	/**
	 * Creates a {@link CreateTLSingletonProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLSingletonProcessor(InstantiationContext context, Config config) {
		super(context, config);
		_singletonCreator = context.getInstance(config.getSingleton());
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating singleton migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		Module module = _util.getTLModuleOrFail(connection, getConfig().getModule());
		BranchIdType newSingleton = _singletonCreator.createObject(connection);
		CompiledStatement createSingleton = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.ID, "module"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.STRING, "singletonType"),
			parameterDef(DBType.ID, "singletonID")),
		insert(
			table(SQLH.mangleDBName(PersistentModuleSingletons.OBJECT_NAME)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
					_util.refID(PersistentModuleSingletons.SOURCE_ATTR),
				SQLH.mangleDBName(PersistentModuleSingletons.NAME_ATTR),
					_util.refType(PersistentModuleSingletons.DEST_ATTR),
					_util.refID(PersistentModuleSingletons.DEST_ATTR)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.ID, "module"),
				parameter(DBType.STRING, "name"),
				parameter(DBType.STRING, "singletonType"),
				parameter(DBType.ID, "singletonID")))).toSql(connection.getSQLDialect());

		createSingleton.executeUpdate(connection, newSingleton.getBranch(), _util.newID(connection),
			_util.getRevCreate(connection), module.getID(), getConfig().getName(), newSingleton.getTable(),
			newSingleton.getID());
		log.info("Created singleton '" + getConfig().getName() + "' for module " + _util.toString(module));

	}

}
