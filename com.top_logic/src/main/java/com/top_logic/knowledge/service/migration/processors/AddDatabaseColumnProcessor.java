/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.sql.SQLAddColumn;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that adds a new column to an existing database table.
 *
 * <p>
 * This processor operates on the raw database level only. To add a complete TopLogic database
 * attribute (a {@link com.top_logic.dob.MOAttribute} together with its backing column), use
 * {@link AddMOAttributeProcessor} instead.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddDatabaseColumnProcessor extends AbstractConfiguredInstance<AddDatabaseColumnProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AddDatabaseColumnProcessor}.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<AddDatabaseColumnProcessor> {

		/**
		 * Name of the database table the new column is added to.
		 *
		 * <p>
		 * This is the technical table name as it is defined in the database, not the name of the
		 * corresponding {@link com.top_logic.dob.MetaObject}.
		 * </p>
		 */
		@Mandatory
		String getTable();

		/**
		 * @see #getTable()
		 */
		void setTable(String value);

		/**
		 * Name of the column to add to {@link #getTable()}.
		 *
		 * <p>
		 * This is the technical column name as it is defined in the database, not the name of the
		 * corresponding {@link com.top_logic.dob.MOAttribute}.
		 * </p>
		 */
		@Mandatory
		String getColumn();

		/**
		 * @see #getColumn()
		 */
		void setColumn(String value);

		/**
		 * The {@link DBType} of the new column.
		 */
		@Mandatory
		DBType getDBType();

		/**
		 * @see #getDBType()
		 */
		void setDBType(DBType value);

		/**
		 * Whether the new column must not be <code>null</code>.
		 *
		 * <p>
		 * A value of <code>null</code> means that the database default is used.
		 * </p>
		 */
		Boolean isMandatory();

		/**
		 * @see #isMandatory()
		 */
		void setMandatory(Boolean value);

		/**
		 * Whether the new column must be marked as binary.
		 *
		 * <p>
		 * Can be set if {@link #getDBType()} can be set to {@link DBType#binaryParam binary}. A
		 * value of <code>null</code> means that the database default is used.
		 * </p>
		 */
		Boolean isBinary();

		/**
		 * @see #isBinary()
		 */
		void setBinary(Boolean value);

		/**
		 * Size of the new column.
		 *
		 * <p>
		 * Must be set if {@link #getDBType()} requires a {@link DBType#sizeParam size value}.
		 * </p>
		 */
		Long getSize();

		/**
		 * @see #getSize()
		 */
		void setSize(Long value);

		/**
		 * Precision of the new column.
		 *
		 * <p>
		 * Must be set if {@link #getDBType()} requires a {@link DBType#precisionParam precision
		 * value}.
		 * </p>
		 */
		Integer getPrecision();

		/**
		 * @see #getPrecision()
		 */
		void setPrecision(Integer value);

		/**
		 * Supplier for the default value assigned to the new column, or <code>null</code> to add the
		 * column without a default value.
		 */
		PolymorphicConfiguration<? extends Supplier<?>> getDefaultValue();

		/**
		 * @see #getDefaultValue()
		 */
		void setDefaultValue(PolymorphicConfiguration<? extends Supplier<?>> value);
	}

	/**
	 * Create a {@link AddDatabaseColumnProcessor}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AddDatabaseColumnProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config config = getConfig();
		String table = config.getTable();
		String colName = config.getColumn();
		DBType dbType = config.getDBType();
		SQLAddColumn addCol = SQLFactory.addColumn(table(table), colName, dbType);
		if (config.isMandatory() != null) {
			addCol.setMandatory(config.isMandatory());
		}
		if (config.isBinary() != null) {
			addCol.setBinary(config.isBinary());
		}
		if (config.getSize() != null) {
			addCol.setSize(config.getSize());
		} else if (dbType.sizeParam) {
			log.error("Unable to add column '" + colName + "' to table '" + table + "': The database type '" + dbType
					+ "' requires a size.");
			return;
		}
		if (config.getPrecision() != null) {
			addCol.setPrecision(config.getPrecision());
		} else if (dbType.precisionParam) {
			log.error("Unable to add column '" + colName + "' to table '" + table + "': The database type '" + dbType
					+ "' requires a precision.");
			return;
		}
		if (config.getDefaultValue() != null) {
			Supplier<?> defaultValue = TypedConfigUtil.createInstance(config.getDefaultValue());
			addCol.setDefaultValue(defaultValue.get());
		}
		try {
			execute(connection, addCol);
			log.info("Added column '" + colName + "' to table '" + table + "'.");
		} catch (SQLException ex) {
			log.error("Unable to add column '" + colName + "' to table '" + table + "'.", ex);
		}
	}

	private void execute(PooledConnection connection, SQLStatement stmt) throws SQLException {
		query(stmt).toSql(connection.getSQLDialect()).executeUpdate(connection);
	}

}

