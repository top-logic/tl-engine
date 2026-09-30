/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.math.BigDecimal;
import java.sql.Time;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} to add primitive {@link MOAttribute} to a persistent object type.
 */
@Label("Add database column")
public class AddPrimitiveMOAttributeProcessor extends AddMOAttributeProcessor {

	/**
	 * Configuration options for {@link AddPrimitiveMOAttributeProcessor}.
	 */
	@TagName("add-mo-attribute")
	public interface Config<I extends AddPrimitiveMOAttributeProcessor> extends AddMOAttributeProcessor.Config<I> {

		/**
		 * Configuration name of {@link #getPrimitiveAttribute()}
		 */
		String MO_ATTRIBUTE = MetaObjectConfig.ATTRIBUTES_PROPERTY;

		/**
		 * Definition of the {@link MOAttribute} to add to the {@link MetaObject} with name
		 * {@link #getTable()}.
		 */
		@Mandatory
		@Name(MO_ATTRIBUTE)
		PrimitiveAttributeConfig getPrimitiveAttribute();

		@Override
		@DerivedRef(MO_ATTRIBUTE)
		AttributeConfig getAttribute();

	}

	/**
	 * Creates a {@link AddPrimitiveMOAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddPrimitiveMOAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void updateDatabaseTable(MigrationContext context, Log log, PooledConnection connection, MOClass table) {
		String tableName = ((DBTableMetaObject) table).getDBName();
		MOAttribute newAttribute = table.getAttribute(getConfig().getAttribute().getAttributeName());

		SQLProcessor processor = new SQLProcessor(connection);
		for (DBAttribute dbAttribute : newAttribute.getDbMapping()) {
			Object defaultValue = newAttribute.isMandatory() ? getDefaultValue(dbAttribute) : null;
			createColumn(log, processor, tableName, dbAttribute, defaultValue);
		}
		
	}

	private Object getDefaultValue(DBAttribute dbAttribute) {
		switch (dbAttribute.getSQLType()) {
			case BLOB:
				return new byte[0];
			case BOOLEAN:
				return Boolean.FALSE;
			case BYTE:
				return Byte.valueOf((byte) 0);
			case CHAR:
				return Character.valueOf((char) 0);
			case DATE:
				return new java.sql.Date(System.currentTimeMillis());
			case DATETIME:
				return new java.sql.Timestamp(System.currentTimeMillis());
			case DECIMAL:
				return new BigDecimal(0);
			case DOUBLE:
				return Double.valueOf(0);
			case FLOAT:
				return Float.valueOf(0);
			case ID:
				return IdentifierUtil.nullIdForMandatoryDatabaseColumns();
			case INT:
				return Integer.valueOf(0);
			case LONG:
				return Long.valueOf(0);
			case SHORT:
				return Short.valueOf((short) 0);
			case CLOB:
			case STRING:
				return "Dummy string value";
			case TIME:
				return new Time(System.currentTimeMillis());
		}
		throw new UnreachableAssertion("Uncovered case: " + dbAttribute.getSQLType());
	}

}
