/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} moving objects from one table to another.
 */
public class MoveObjectsProcessor extends AbstractConfiguredInstance<MoveObjectsProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MoveObjectsProcessor}.
	 */
	@TagName("move-objects")
	public interface Config<I extends MoveObjectsProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table to take the objects from.
		 */
		@Name("source-table")
		String getSourceTable();

		/**
		 * Name of the table to move the objects to.
		 */
		@Name("dest-table")
		String getDestTable();

		/**
		 * The type of objects to move.
		 */
		@Mandatory
		@Name("type")
		@Format(QualifiedTypeName.ListFormat.class)
		List<QualifiedTypeName> getType();

		/**
		 * Whether to only move objects of the given type excluding sub-classes.
		 */
		boolean getMonomorphic();
	}

	/**
	 * Creates a {@link MoveObjectsProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveObjectsProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String sourceTableName = config.getSourceTable();
		String destTableName = config.getDestTable();

		String typeNames = config.getType().stream().map(t -> "'" + t.getName() + "'").collect(Collectors.joining(", "));
		log.info(
			"Moving objects of type " + typeNames + " from table '" + sourceTableName + "' to '" + destTableName
				+ "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure sourceTable = (MOStructure) repository.getMetaObject(sourceTableName);
		MOStructure destTable = (MOStructure) repository.getMetaObject(destTableName);
		MOReference typeRef = (MOReference) sourceTable.getAttribute(PersistentObject.TYPE_REF);

		Set<MOReference> refToUpdate = new HashSet<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOStructure)) {
				continue;
			}

			MOStructure structType = (MOStructure) type;

			for (MOAttribute attr : structType.getAttributes()) {
				if (!(attr instanceof MOReference)) {
					continue;
				}

				MOReference ref = (MOReference) attr;
				MetaObject targetType = ref.getMetaObject();

				if (targetType.isSubtypeOf(sourceTable)) {
					refToUpdate.add(ref);
				}
			}
		}

		Util util = context.getSQLUtils();
		try {
			Set<TLID> movedTypes = new HashSet<>();
			for (QualifiedTypeName type : config.getType()) {
				Type declaredType = util.getTLTypeOrFail(connection, type);
				if (config.getMonomorphic()) {
					movedTypes.add(declaredType.getID());
				} else {
					movedTypes.addAll(util.getTransitiveSpecializations(connection, declaredType));
				}
			}

			log.info("Moving objects with concrete type IDs: " + movedTypes);

			// Adjust references.
			for (MOReference ref : refToUpdate) {
				MOStructure refOwner = ref.getOwner();

				for (MetaObject refTable : repository.getMetaObjects()) {
					if (!(refTable instanceof MOClass)) {
						continue;
					}

					MOClass refClass = (MOClass) refTable;
					if (refClass.isAbstract()) {
						continue;
					}

					if (refClass.isSubtypeOf(refOwner)) {
						// Concrete foreign key to modify found.

						if (ref.isMonomorphic()) {
							log.info("Monomorphic reference '" + ref.getName() + "' in table '" + refTable.getName()
								+ "' not updated, make sure that either all objects point to '" + sourceTableName
								+ "' and schema is updated, or no objects point to '" + sourceTableName + "'.");
							continue;
						}

						log.info("Updating reference '" + ref.getName() + "' in table '" + refTable.getName() + "'.");

						CompiledStatement update = query(
							update(
								table(refClass.getDBMapping().getDBName()),
								and(
									eqSQL(
										column(ref.getColumn(ReferencePart.type).getDBName()),
										literal(DBType.STRING, sourceTableName)),
									inSetSelect(
										column(ref.getColumn(ReferencePart.name).getDBName()),
										select(
											columns(columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
											table(sourceTable.getDBMapping().getDBName()),
											inSet(
												column(typeRef.getColumn(ReferencePart.name).getDBName()),
												setLiteral(movedTypes, DBType.ID))))),
								columnNames(ref.getColumn(ReferencePart.type).getDBName()),
								expressions(literal(DBType.STRING, destTableName))))
									.toSql(connection.getSQLDialect());

						int cnt = update.executeUpdate(connection);
						log.info("Updated " + cnt + " object references '" + ref.getName() + "' in table '"
							+ refTable.getName() + "' pointing to '" + sourceTableName + "'.");
					}
				}
			}

			// Copy values.
			List<String> columns = new ArrayList<>();
			List<SQLColumnDefinition> columnDefs = new ArrayList<>();
			for (MOAttribute attr : sourceTable.getAttributes()) {
				if (destTable.getAttributeOrNull(attr.getName()) == null) {
					continue;
				}

				for (DBAttribute column : attr.getDbMapping()) {
					columns.add(column.getDBName());
					columnDefs.add(columnDef(column.getDBName()));
				}
			}

			CompiledStatement copy = query(
				insert(
					table(destTable.getDBMapping().getDBName()),
					columns,
					select(
						columnDefs,
						table(sourceTable.getDBMapping().getDBName()),
						inSet(
							column(typeRef.getColumn(ReferencePart.name).getDBName()),
							setLiteral(movedTypes, DBType.ID)))))
								.toSql(connection.getSQLDialect());

			int cntCopy = copy.executeUpdate(connection);
			log.info("Copied " + cntCopy + " rows from table '" + sourceTableName + "' to '" + destTableName + "'.");

			CompiledStatement update = query(
				update(
					table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
					and(
						eqSQL(
							column(AbstractFlexDataManager.TYPE_DBNAME),
							literal(DBType.STRING, sourceTableName)),
						inSetSelect(
							column(AbstractFlexDataManager.IDENTIFIER_DBNAME),
							select(
								columns(columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
								table(sourceTable.getDBMapping().getDBName()),
								inSet(
									column(typeRef.getColumn(ReferencePart.name).getDBName()),
									setLiteral(movedTypes, DBType.ID))))),

					columnNames(AbstractFlexDataManager.TYPE_DBNAME),
					expressions(literal(DBType.STRING, destTableName)))).toSql(connection.getSQLDialect());

			int cntUpdate = update.executeUpdate(connection);
			log.info("Migrated " + cntUpdate + " flex attributes from table '" + sourceTableName + "' to '" + destTableName + "'.");
			
			CompiledStatement delete = query(
				delete(
					table(sourceTable.getDBMapping().getDBName()),
					inSet(
						column(typeRef.getColumn(ReferencePart.name).getDBName()),
						setLiteral(movedTypes, DBType.ID))))
				.toSql(connection.getSQLDialect());

			int cntDelete = delete.executeUpdate(connection);
			log.info("Deleted " + cntDelete + " rows from table '" + sourceTableName + ".");

			// Update RevisionXref
			log.info("Updating '" + RevisionXref.REVISION_XREF_TYPE_NAME + ".");
			updateXref(context, log, connection, sourceTable);
			updateXref(context, log, connection, destTable);
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to move objects of type " + typeNames + " from '" + sourceTableName + "' to '" + destTableName
					+ "': " + ex.getMessage(),
				ex);
		}
	}

	private static void updateXref(MigrationContext context, Log log, PooledConnection connection,
			MOStructure table) throws SQLException {
		CompiledStatement removeXref = query(
			delete(table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
				eqSQL(
					column(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
					literal(DBType.STRING, table.getName()))))
						.toSql(connection.getSQLDialect());
		int removed = removeXref.executeUpdate(connection);
		log.info("Cleared " + removed + " rows for table '" + table.getName() + "' from '"
			+ RevisionXref.REVISION_XREF_TYPE_NAME + ".");

		CompiledStatement fillXref = query(
			insert(
				table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
				columnNames(
					SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE),
					SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE),
					SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
				select(true,
					columns(
						columnDef(BasicTypes.REV_MIN_DB_NAME),
						context.getSQLUtils().branchColumnDef(),
						columnDef(literal(DBType.STRING, table.getName()))),
					table(table.getDBMapping().getDBName())))).toSql(connection.getSQLDialect());

		int readded = fillXref.executeUpdate(connection);
		log.info("Re-added " + readded + " rows for table '" + table.getName() + "' from '"
			+ RevisionXref.REVISION_XREF_TYPE_NAME + ".");
	}

}
