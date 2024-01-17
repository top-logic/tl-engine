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
import java.util.function.Supplier;

import com.lowagie.text.pdf.codec.wmf.MetaObject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLObject;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} creating a new {@link TLObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLObjectProcessor extends AbstractConfiguredInstance<CreateTLObjectProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLObjectProcessor}.
	 */
	@TagName("create-object")
	public interface Config extends PolymorphicConfiguration<CreateTLObjectProcessor> {

		/**
		 * Name of the {@link MetaObject table} in which the {@link TLObject} must be created.
		 */
		@Mandatory
		String getTable();

		/**
		 * {@link TLObject#tType() Type} of the new created object.
		 */
		@Mandatory
		QualifiedTypeName getType();

		/**
		 * Table values for the new object.
		 */
		@Key(Value.COLUMN)
		List<Value> getValues();
	}

	/**
	 * Column value for a new {@link TLObject}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Value extends ConfigurationItem {

		/** Configuration name of {@link #getColumn()}. */
		String COLUMN = "column";

		/**
		 * Name of the database column.
		 * 
		 * <p>
		 * Note: This is the real name of the column in the database, usually in capital letters and
		 * separated by underscore.
		 * </p>
		 */
		@Name(COLUMN)
		String getColumn();

		/**
		 * Database type of the column.
		 */
		DBType getColumnType();

		/**
		 * Formatted value for {@link #getColumn()}.
		 * 
		 * <p>
		 * Note: If {@link #getValueSupplier()} is set, this value is ignored.
		 * </p>
		 * 
		 * @return The string representation of the database value, matching
		 *         {@link #getColumnType()}.
		 * 
		 * @see #getValueSupplier()
		 */
		String getValue();

		/**
		 * Supplier for the value for {@link #getColumn()}.
		 * 
		 * <p>
		 * Note: If <code>null</code>, {@link #getValue()} is used.
		 * </p>
		 * 
		 * @return Factory creating the database value, matching {@link #getColumnType()}.
		 * 
		 * @see #getValue()
		 */
		PolymorphicConfiguration<? extends Supplier<?>> getValueSupplier();
	}

	private final Map<String, Object> _values = new LinkedHashMap<>();

	private Util _util;

	/**
	 * Creates a {@link CreateTLObjectProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLObjectProcessor(InstantiationContext context, Config config) {
		super(context, config);
		for (Value additionalValue: getConfig().getValues()) {
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
			_values.put(column, value);
		}
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating class migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		createObject(connection);

		log.info("Created object in '" + getConfig().getTable() + "' with values: " + _values);
	}

	/**
	 * Creates the {@link TLObject} according to {@link #getConfig()} in the given database
	 * connection.
	 */
	public BranchIdType createObject(PooledConnection connection) throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();
		
		Type type = _util.getTLTypeOrFail(connection, getConfig().getType());
		TLID newID = _util.newID(connection);
		long branch = type.getBranch();
		
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		columns.add(BasicTypes.BRANCH_DB_NAME);
		values.add(literalLong(branch));
		
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		columns.add(BasicTypes.IDENTIFIER_DB_NAME);
		values.add(parameter(DBType.ID, "identifier"));
		arguments.add(newID);
		
		columns.add(BasicTypes.REV_MAX_DB_NAME);
		values.add(literalLong(Revision.CURRENT_REV));

		parameterDefs.add(parameterDef(DBType.LONG, "revCreate"));
		columns.add(BasicTypes.REV_MIN_DB_NAME);
		values.add(parameter(DBType.LONG, "revCreate"));
		columns.add(BasicTypes.REV_CREATE_DB_NAME);
		values.add(parameter(DBType.LONG, "revCreate"));
		arguments.add(_util.getRevCreate(connection));
		
		parameterDefs.add(parameterDef(DBType.ID, "typeID"));
		columns.add(_util.refID(PersistentObject.TYPE_REF));
		values.add(parameter(DBType.ID, "typeID"));
		arguments.add(type.getID());
		
		for (Value additionalValue: getConfig().getValues()) {
			DBType columnType = additionalValue.getColumnType();
			String column = additionalValue.getColumn();
			parameterDefs.add(parameterDef(columnType, column));
			columns.add(column);
			values.add(parameter(columnType, column));
			arguments.add(_values.get(column));
		}
		
		CompiledStatement createObj = query(
		parameterDefs,
		insert(
			table(SQLH.mangleDBName(getConfig().getTable())),
			columns,
			values)).toSql(sqlDialect);

		createObj.executeUpdate(connection, arguments.toArray());
		return BranchIdType.newInstance(BranchIdType.class, branch, newID, getConfig().getTable());
	}

}
