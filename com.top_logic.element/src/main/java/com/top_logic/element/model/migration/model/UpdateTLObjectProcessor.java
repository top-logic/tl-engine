/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.model.migration.model.CreateTLObjectProcessor.Value;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLObject;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} updating a {@link TLObject}.
 * 
 * @see CreateTLObjectProcessor
 */
public class UpdateTLObjectProcessor extends AbstractConfiguredInstance<UpdateTLObjectProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLObjectProcessor}.
	 */
	@TagName("update-object")
	public interface Config extends PolymorphicConfiguration<UpdateTLObjectProcessor> {

		/**
		 * Name of the {@link MetaObject table} in which {@link TLObject} to change lives.
		 */
		@Mandatory
		String getTable();

		/**
		 * Setter for {@link #getTable()}.
		 */
		void setTable(String value);

		/**
		 * {@link TLObject#tType() Type} of the changed object.
		 */
		@Mandatory
		QualifiedTypeName getType();

		/**
		 * Setter {@link #getType()}.
		 */
		void setType(QualifiedTypeName value);

		/**
		 * Whether the {@link #getTable() table} has no {@link PersistentObject#TYPE_REF} column.
		 * 
		 * <p>
		 * A table that stores entries for exactly one type may have no type column. The
		 * {@link PersistentObject#TYPE_REF} is computed in this case.
		 * </p>
		 */
		boolean hasNoTypeColumn();

		/**
		 * Setter for {@link #hasNoTypeColumn()}.
		 */
		void setNoTypeColumn(boolean value);

		/**
		 * Values identifying the element.
		 */
		@Key(Value.COLUMN)
		List<Value> getKeyValues();

		/**
		 * Table values for the changed object.
		 */
		@Key(Value.COLUMN)
		List<Value> getValues();
	}

	private final Map<String, Object> _values = new LinkedHashMap<>();

	private final Map<String, Object> _keyValues = new LinkedHashMap<>();

	/**
	 * Creates a {@link UpdateTLObjectProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLObjectProcessor(InstantiationContext context, Config config) {
		super(context, config);
		resolveValues(context, config, _keyValues, getConfig().getKeyValues());
		resolveValues(context, config, _values, getConfig().getValues());
	}

	private void resolveValues(InstantiationContext context, Config config, Map<String, Object> out,
			List<Value> input) {
		for (Value additionalValue : input) {
			DBType columnType = additionalValue.getColumnType();
			String column = additionalValue.getColumn();
			Object value;
			if (additionalValue.getValueSupplier() != null) {
				value = context.getInstance(additionalValue.getValueSupplier()).get();
			} else {
				String formattedValue = additionalValue.getValue();
				try {
					value = DBType.parseSQLValue(columnType, formattedValue);
				} catch (ParseException ex) {
					context.error("Unable to parse value '" + formattedValue + "' for column '" + column + "' at "
						+ config.location());
					continue;
				}
			}
			out.put(column, value);
		}
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			internalDoMigration(context, log, connection);
		} catch (Exception ex) {
			log.error("Updating object migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(MigrationContext context, Log log, PooledConnection connection) throws Exception {
		int rows = updateObject(context, connection);

		log.info("Updated '" + rows + "' for object in '" + getConfig().getTable() + "' with keys '" + _keyValues
				+ "' with values: " + _values);
	}

	/**
	 * Updates the {@link TLObject} according to {@link #getConfig()} in the given database
	 * connection.
	 */
	public int updateObject(MigrationContext context, PooledConnection connection)
			throws SQLException, MigrationException {
		Util util = context.getSQLUtils();

		DBHelper sqlDialect = connection.getSQLDialect();
		
		MOStructure table = (MOStructure) context.getPersistentRepository().getTypeOrNull(getConfig().getTable());
		if (table == null) {
			throw new KnowledgeBaseRuntimeException("No table with name '" + getConfig().getTable() + "' available.");
		}
		Type type = util.getTLTypeOrFail(connection, getConfig().getType());
		
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		SQLExpression where = literalTrueLogical();

		for (Value additionalValue : getConfig().getKeyValues()) {
			DBType columnType = additionalValue.getColumnType();
			String column = additionalValue.getColumn();
			String paramName = column + "_key";
			parameterDefs.add(parameterDef(columnType, paramName));
			where = and(eq(column(column), parameter(columnType, paramName)), where);
			arguments.add(_keyValues.get(column));
		}

		if (!getConfig().hasNoTypeColumn()) {
			parameterDefs.add(parameterDef(DBType.ID, "typeID"));
			where = and(eq(column(Util.refID(PersistentObject.TYPE_REF)), parameter(DBType.ID, "typeID")), where);
			arguments.add(type.getID());
		}
		
		for (Value additionalValue: getConfig().getValues()) {
			DBType columnType = additionalValue.getColumnType();
			String column = additionalValue.getColumn();
			String paramName = column;
			parameterDefs.add(parameterDef(columnType, paramName));
			columns.add(column);
			values.add(parameter(columnType, paramName));
			arguments.add(_values.get(column));
		}
		
		CompiledStatement updateObj = query(
			parameterDefs,
			update(
				table(table.getDBMapping().getDBName()),
				where,
				columns,
				values)).toSql(sqlDialect);

		return updateObj.executeUpdate(connection, arguments.toArray());
	}

}
