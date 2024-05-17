/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.model.i18n.I18NAttributeStorage;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} that makes string attributes to I18N attributes.
 */
public class InternationalizeAttributeProcessor extends AbstractConfiguredInstance<InternationalizeAttributeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link InternationalizeAttributeProcessor}.
	 */
	@TagName("internationalize-attribute")
	public interface Config<I extends InternationalizeAttributeProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The name of the table, that defines the non-internationalized attribute as column.
		 */
		@Mandatory
		@Name("table")
		String getTable();

		/**
		 * The supported languages mapped to column names, where the value for the corresponding
		 * language is stored.
		 */
		@Mandatory
		@Name("columns")
		@MapBinding(key = "lang", attribute = "name", tag = "column")
		Map<String, String> getColumns();

		/**
		 * The attribute whose values should be internationalized.
		 */
		@Mandatory
		@Name("attribute")
		QualifiedPartName getAttribute();
	}

	/**
	 * Creates a {@link InternationalizeAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InternationalizeAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		
		Config<?> config = getConfig();
		String objType = config.getTable();

		try {
			TypePart attr = util.getTLTypePartOrFail(connection, config.getAttribute());
			TLID attrId = attr.getID();
			Type owner = attr.getOwner();

			Collection<TLID> implIds = util.getImplementationIds(connection, owner);

			CompiledStatement insert = createI18NInsert(context, connection);

			MOClass table = (MOClass) context.getSchemaRepository().getType(objType);
			MOReference tType = (MOReference) table.getAttribute(PersistentObject.T_TYPE_ATTR);
			String tTypeColumn = tType.getColumn(ReferencePart.name).getDBName();

			for (Entry<String, String> entry : config.getColumns().entrySet()) {
				String lang = entry.getKey();
				String column = entry.getValue();

				MOAttribute objColumn = table.getAttributeOrNull(column);
				final boolean flex;

				CompiledStatement select;
				if (objColumn == null) {
					flex = true;
					select = query(
						select(
							columns(
								util.branchColumnDef(),
								columnDef(AbstractFlexDataManager.IDENTIFIER_DBNAME),
								columnDef(BasicTypes.REV_MIN_DB_NAME),
								columnDef(BasicTypes.REV_MAX_DB_NAME),
								columnDef(AbstractFlexDataManager.DATA_TYPE_DBNAME),
								columnDef(AbstractFlexDataManager.VARCHAR_DATA_DBNAME),
								columnDef(AbstractFlexDataManager.CLOB_DATA_DBNAME)),
							table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
							and(
								eqSQL(column(AbstractFlexDataManager.TYPE_DBNAME), literal(DBType.STRING, objType)),
								eqSQL(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
									literal(DBType.STRING, column))),
							orders(
								order(false, util.branchColumnRef()),
								order(false, column(BasicTypes.IDENTIFIER_DB_NAME)),
								order(false, column(BasicTypes.REV_MIN_DB_NAME))))).toSql(connection.getSQLDialect());
				} else {
					flex = false;
					String valueColumn = objColumn.getDbMapping()[0].getDBName();

					select = query(
						select(
							columns(
								util.branchColumnDef(),
								columnDef(BasicTypes.IDENTIFIER_DB_NAME),
								columnDef(BasicTypes.REV_MIN_DB_NAME),
								columnDef(BasicTypes.REV_MAX_DB_NAME),
								columnDef(valueColumn)),
							table(table.getDBMapping().getDBName()),
							inSet(column(tTypeColumn), implIds, DBType.ID),
							orders(
								order(false, util.branchColumnRef()),
								order(false, column(BasicTypes.IDENTIFIER_DB_NAME)),
								order(false, column(BasicTypes.REV_MIN_DB_NAME))))).toSql(connection.getSQLDialect());
				}

				int cnt = 0;
				try (Batch batch = insert.createBatch(connection)) {
					try (ResultSet result = select.executeQuery(connection)) {
						while (result.next()) {
							long objBranch = result.getLong(1);
							long objId = result.getLong(2);
							long revMin = result.getLong(3);
							long revMax = result.getLong(4);
							String value;
							if (flex) {
								byte dataType = result.getByte(5);
								switch (dataType) {
									case AbstractFlexDataManager.EMPTY_STRING_TYPE:
										value = null;
										break;
									case AbstractFlexDataManager.STRING_TYPE:
										value = result.getString(6);
										break;
									case AbstractFlexDataManager.CLOB_TYPE:
										value = result.getString(7);
										break;
									default:
										value = null;
								}
							} else {
								value = result.getString(5);
							}

							if (value != null) {
								batch.addBatch(
									objBranch, util.newID(connection), revMin, revMax, revMin, objType, objId, attrId,
									lang,
									value);

								if (++cnt >= 1000) {
									batch.executeBatch();
									cnt = 0;

									log.info(
										"Moved '" + cnt + "' values of '" + config.getAttribute().getName() + "' from '"
											+ objType
											+ "' to internationalization table (" + lang + ").");
								}
							}
						}
					}
					if (cnt > 0) {
						batch.executeBatch();
						log.info(
							"Moved '" + cnt + "' values of '" + config.getAttribute().getName() + "' from '" + objType
								+ "' to internationalization table (" + lang + ").");
					}
				}
			}
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to move values of '" + config.getAttribute().getName() + "' from '" + objType
				+ "' to internationalization table.", ex);
		}
	}

	private CompiledStatement createI18NInsert(MigrationContext context, PooledConnection connection)
			throws SQLException {
		Util util = context.getSQLUtils();
		MOClass i18nTable =
			(MOClass) context.getSchemaRepository().getType(I18NAttributeStorage.I18N_STORAGE_KO_TYPE);

		MOReference objRef = (MOReference) i18nTable.getAttribute(I18NAttributeStorage.OBJECT_ATTRIBUTE_NAME);
		MOReference attrRef =
			(MOReference) i18nTable.getAttribute(I18NAttributeStorage.META_ATTRIBUTE_ATTRIBUTE_NAME);
		String langColumn =
			i18nTable.getAttribute(I18NAttributeStorage.LANGUAGE_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();
		String i18nValueColumn =
			i18nTable.getAttribute(I18NAttributeStorage.VALUE_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();

		CompiledStatement insert = query(
			parameters(
				parameterDef(DBType.LONG, "branch"),
				parameterDef(DBType.ID, "id"),
				parameterDef(DBType.LONG, "revMin"),
				parameterDef(DBType.LONG, "revMax"),
				parameterDef(DBType.LONG, "revCreate"),
				parameterDef(DBType.LONG, "objType"),
				parameterDef(DBType.LONG, "objId"),
				parameterDef(DBType.ID, "attrId"),
				parameterDef(DBType.LONG, "lang"),
				parameterDef(DBType.LONG, "value")),
			insert(
				table(i18nTable.getDBMapping().getDBName()),
				Util.listWithoutNull(columnNames(
					util.branchColumnOrNull(),
					BasicTypes.IDENTIFIER_DB_NAME,
					BasicTypes.REV_MIN_DB_NAME,
					BasicTypes.REV_MAX_DB_NAME,
					BasicTypes.REV_CREATE_DB_NAME,
					objRef.getColumn(ReferencePart.type).getDBName(),
					objRef.getColumn(ReferencePart.name).getDBName(),
					attrRef.getColumn(ReferencePart.name).getDBName(),
					langColumn,
					i18nValueColumn)),
				Util.listWithoutNull(expressions(
					util.branchParamOrNull(),
					parameter(DBType.ID, "id"),
					parameter(DBType.LONG, "revMin"),
					parameter(DBType.LONG, "revMax"),
					parameter(DBType.LONG, "revCreate"),
					parameter(DBType.LONG, "objType"),
					parameter(DBType.LONG, "objId"),
					parameter(DBType.ID, "attrId"),
					parameter(DBType.LONG, "lang"),
					parameter(DBType.LONG, "value"))))).toSql(connection.getSQLDialect());
		return insert;
	}

}
