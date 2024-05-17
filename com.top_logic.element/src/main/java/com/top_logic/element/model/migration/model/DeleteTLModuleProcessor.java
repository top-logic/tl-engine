/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.Type;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLModuleProcessor extends AbstractConfiguredInstance<DeleteTLModuleProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTLModuleProcessor}.
	 */
	@TagName("delete-module")
	public interface Config extends PolymorphicConfiguration<DeleteTLModuleProcessor>, NamedConfigMandatory,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Whether it is a failure if the module is not empty.
		 */
		boolean isFailOnExistingTypes();

	}

	private Util _util;

	/**
	 * Creates a {@link DeleteTLModuleProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLModuleProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		String moduleName = getConfig().getName();
		Module module = _util.getTLModule(connection, TLContext.TRUNK_ID, moduleName);
		if (module == null) {
			log.info("No module with name '" + moduleName + "' to delete available at " + getConfig().location());
			return false;
		}
		List<BranchIdType> toDelete = new ArrayList<>();
		toDelete.add(module);

		List<BranchIdType> moduleSingletons = getTLModuleSingletons(connection, module);
		toDelete.addAll(moduleSingletons);
		List<Type> types = _util.getTLTypeIdentifiers(connection, module);
		if (getConfig().isFailOnExistingTypes() && !types.isEmpty()) {
			log.error("Module " + _util.toString(module) + " is not empty: " + _util.toString(types));
			return false;
		}
		toDelete.addAll(types);
		for (Type type : types) {
			toDelete.addAll(_util.getTLTypeParts(connection, type));
			toDelete.addAll(_util.getGeneralizations(connection, type));
			toDelete.addAll(_util.getSpecializations(connection, type));
		}

		Map<String, Map<Long, List<BranchIdType>>> byTypeAndBranch = new HashMap<>();
		for (BranchIdType entry : toDelete) {
			Map<Long, List<BranchIdType>> byBranch =
				byTypeAndBranch.computeIfAbsent(entry.getTable(), key -> new HashMap<>());
			List<BranchIdType> elements = byBranch.computeIfAbsent(entry.getBranch(), key -> new ArrayList<>());
			elements.add(entry);
		}

		deleteElements(log, connection, byTypeAndBranch);

		if (tlModel == null || getConfig().isSkipModelBaselineChange()) {
			return false;
		}
		return MigrationUtils.deleteModule(log, tlModel, moduleName);
	}

	private List<BranchIdType> getTLModuleSingletons(PooledConnection connection, BranchIdType module)
			throws SQLException {
		List<BranchIdType> searchResult = new ArrayList<>();

		DBHelper sqlDialect = connection.getSQLDialect();
		String identifierAlias = "id";
		String singletonIdAlias = "singletonId";
		String singletonTypeAlias = "singletonType";
		CompiledStatement selectTLSingleton = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "module")),
			selectDistinct(
				Util.listWithoutNull(
					_util.branchColumnDef(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(_util.refID(TLModuleSingleton.SINGLETON_ATTR),
						NO_TABLE_ALIAS, singletonIdAlias),
					columnDef(_util.refType(TLModuleSingleton.SINGLETON_ATTR),
						NO_TABLE_ALIAS, singletonTypeAlias)),
				table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE_SINGLETONS)),
				and(
					eqSQL(
						column(_util.refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "module")),
					_util.eqBranch()))).toSql(sqlDialect);

		long branch = module.getBranch();
		try (ResultSet moduleResult = selectTLSingleton.executeQuery(connection, branch, module.getID())) {
			while (moduleResult.next()) {
				searchResult.add(BranchIdType.newInstance(BranchIdType.class, branch,
					LongID.valueOf(moduleResult.getLong(identifierAlias)),
					TlModelFactory.KO_NAME_TL_MODULE_SINGLETONS));
				searchResult.add(BranchIdType.newInstance(BranchIdType.class, branch,
					LongID.valueOf(moduleResult.getLong(singletonIdAlias)),
					moduleResult.getString(singletonTypeAlias)));
			}
		}
		return searchResult;
	}

	/**
	 * Deletes the given elements (indexed by table).
	 */
	protected void deleteElements(Log log, PooledConnection connection,
			Map<String, Map<Long, List<BranchIdType>>> byTypeAndBranch) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		for (Map<Long, List<BranchIdType>> tmp1 : byTypeAndBranch.values()) {
			for (List<BranchIdType> tmp2 : tmp1.values()) {
				BranchIdType representative = tmp2.get(0);
				CompiledStatement delete = query(
					parameters(
						_util.branchParamDef(),
						setParameterDef("id", DBType.ID)),
					delete(
						table(SQLH.mangleDBName(representative.getTable())),
						and(
							_util.eqBranch(),
							inSet(
								column(BasicTypes.IDENTIFIER_DB_NAME),
								setParameter("id", DBType.ID))))).toSql(sqlDialect);

				Set<TLID> ids = tmp2.stream().map(BranchIdType::getID).collect(Collectors.toSet());
				int deletedRows = delete.executeUpdate(connection, representative.getBranch(), ids);
				log.info("Deleted " + deletedRows + " rows from '" + representative.getTable() + "' in branch '"
					+ representative.getBranch() + "' elements '" + _util.toString(tmp2) + "'.");
			}
		}
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete module migration failed.", ex);
			return false;
		}
	}

}
