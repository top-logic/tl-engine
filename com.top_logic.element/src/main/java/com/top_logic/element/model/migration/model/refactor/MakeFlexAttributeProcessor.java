/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} moving attribute values from columns to flex attribute table.
 */
public class MakeFlexAttributeProcessor extends AbstractConfiguredInstance<MakeFlexAttributeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MakeFlexAttributeProcessor}.
	 */
	@TagName("make-flex-attribute")
	public interface Config<I extends MakeFlexAttributeProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table to take column values from.
		 */
		@Name("table")
		String getTable();

		/**
		 * Name of the column to take attribute values from.
		 */
		@Name("column")
		String getColumn();

		/**
		 * Name of the flex attribute to create.
		 * 
		 * <p>
		 * If not given, the same name as the {@link #getColumn()} is used.
		 * </p>
		 */
		@Name("attribute")
		@Nullable
		String getAttribute();

		/**
		 * The type of objects to create flex attribtes for.
		 */
		@Mandatory
		@Name("types")
		@Format(QualifiedTypeName.ListFormat.class)
		List<QualifiedTypeName> getTypes();

		/**
		 * Whether to only move objects of the given type excluding sub-classes.
		 */
		@Name("monomorphic")
		boolean getMonomorphic();
	}

	/**
	 * Creates a {@link MakeFlexAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MakeFlexAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();

		String typeNames =
			config.getTypes().stream().map(t -> "'" + t.getName() + "'").collect(Collectors.joining(", "));
		String columnName = config.getColumn();
		String attributeName = config.getAttribute();
		if (attributeName == null) {
			attributeName = columnName;
		}

		log.info(
			"Moving values of type " + typeNames + " from column '" + columnName + "' in table '" + tableName
				+ "' to flex attribute '" + attributeName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);
		MOStructure flexTable = (MOStructure) repository.getMetaObject(AbstractFlexDataManager.FLEX_DATA);
		MOReference typeRef = (MOReference) table.getAttribute(PersistentObject.TYPE_REF);

		MOAttribute column = table.getAttribute(columnName);
		if (column == null) {
			log.info("Column '" + columnName + "' of table '" + tableName + "' does not exist, ignoring.", Log.WARN);
			return;
		}
		if (column.getDbMapping().length != 1) {
			log.error("Trying to move column '" + columnName + "' of table '" + tableName
				+ "' with non-trivial DB mapping of size " + column.getDbMapping().length + " to flex data.");
			return;
		}
		DBAttribute dbColumn = column.getDbMapping()[0];

		Util util = context.getSQLUtils();
		try {
			Set<TLID> changedTypes = new HashSet<>();
			for (QualifiedTypeName type : config.getTypes()) {
				Type declaredType = util.getTLTypeOrFail(connection, type);
				if (config.getMonomorphic()) {
					changedTypes.add(declaredType.getID());
				} else {
					changedTypes.addAll(util.getTransitiveSpecializations(connection, declaredType));
				}
			}

			log.info("Moving column values of objects with concrete type IDs: " + changedTypes);

			List<String> columnNames = new ArrayList<>();
			if (context.hasBranchSupport()) {
				columnNames.add(AbstractFlexDataManager.BRANCH_DBNAME);
			}
			Collections.addAll(columnNames,
				AbstractFlexDataManager.TYPE_DBNAME,
				AbstractFlexDataManager.IDENTIFIER_DBNAME,
				BasicTypes.REV_MAX_DB_NAME,
				AbstractFlexDataManager.ATTRIBUTE_DBNAME,
				BasicTypes.REV_MIN_DB_NAME,
				AbstractFlexDataManager.DATA_TYPE_DBNAME,
				AbstractFlexDataManager.LONG_DATA_DBNAME,
				AbstractFlexDataManager.DOUBLE_DATA_DBNAME,
				AbstractFlexDataManager.VARCHAR_DATA_DBNAME,
				AbstractFlexDataManager.CLOB_DATA_DBNAME,
				AbstractFlexDataManager.BLOB_DATA_DBNAME);

			SQLExpression typeValue;

			SQLExpression longValue = literal(DBType.LONG, null);
			SQLExpression doubleValue = literal(DBType.DOUBLE, null);
			SQLExpression stringValue = literal(DBType.STRING, null);
			SQLExpression clobValue = literal(DBType.CLOB, null);
			SQLExpression blobValue = literal(DBType.BLOB, null);

			SQLColumnReference sourceValue = column(dbColumn.getDBName());
			switch (dbColumn.getSQLType()) {
				case BLOB:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.BLOB_TYPE);
					blobValue = sourceValue;
					break;
				case BOOLEAN:
					typeValue = sqlCase(sourceValue,
						literal(DBType.INT, AbstractFlexDataManager.BOOLEAN_TRUE),
						literal(DBType.INT, AbstractFlexDataManager.BOOLEAN_FALSE));
					break;
				case BYTE:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.INTEGER_TYPE);
					longValue = sourceValue;
					break;
				case CHAR:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.STRING_TYPE);
					stringValue = sourceValue;
					break;
				case CLOB:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.CLOB_TYPE);
					clobValue = sourceValue;
					break;
				case DECIMAL:
				case DOUBLE:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.DOUBLE_TYPE);
					doubleValue = sourceValue;
					break;
				case FLOAT:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.FLOAT_TYPE);
					doubleValue = sourceValue;
					break;
				case ID:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.TL_ID_TYPE);
					longValue = sourceValue;
					break;
				case INT:
				case SHORT:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.INTEGER_TYPE);
					longValue = sourceValue;
					break;
				case LONG:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.LONG_TYPE);
					longValue = sourceValue;
					break;
				case STRING:
					typeValue = literal(DBType.INT, AbstractFlexDataManager.STRING_TYPE);
					stringValue = sourceValue;
					break;
				case DATE:
				case DATETIME:
				case TIME:
				default:
					log.error("Unsupported DB type '' of column '' in table '' for a move to a flex attribute.");
					return;
			}

			List<SQLColumnDefinition> columnDefs = new ArrayList<>();
			if (context.hasBranchSupport()) {
				columnDefs.add(columnDef(BasicTypes.BRANCH_DB_NAME));
			}
			columnDefs.addAll(Arrays.asList(
				columnDef(literal(DBType.STRING, tableName)),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME),
				columnDef(literal(DBType.STRING, attributeName)),
				columnDef(BasicTypes.REV_MIN_DB_NAME),

				columnDef(typeValue),

				columnDef(longValue),
				columnDef(doubleValue),
				columnDef(stringValue),
				columnDef(clobValue),
				columnDef(blobValue)));
			CompiledStatement copy = query(
				insert(
					table(flexTable.getDBMapping().getDBName()),
					columnNames,
					select(
						columnDefs,
						table(table.getDBMapping().getDBName()),
						inSet(
							column(typeRef.getColumn(ReferencePart.name).getDBName()),
							setLiteral(changedTypes, DBType.ID)))))
								.toSql(connection.getSQLDialect());

			int cntCopy = copy.executeUpdate(connection);
			log.info("Copied " + cntCopy + " values from table '" + tableName + "' to flex attribute '" + attributeName
				+ "'.");
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to move values of column '" + columnName + "' from '" + tableName + "' to flex attribute '"
					+ attributeName + "': " + ex.getMessage(),
				ex);
		}
	}

}
