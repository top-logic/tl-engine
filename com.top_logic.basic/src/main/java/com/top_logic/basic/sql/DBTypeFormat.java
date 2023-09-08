/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link AbstractConfigurationValueProvider} to parse a {@link DBType} definition.
 * 
 * <p>
 * Besides their {@link DBType#getExternalName() normative names}, {@link DBType}s can also be
 * configured by various alias names for compatibility reasons.
 * </p>
 * 
 * @since 5.8.0
 * 
 *          com.top_logic.knowledge.service.xml.MORepositoryImporter$MetaObjectHandler
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DBTypeFormat extends AbstractConfigurationValueProvider<DBType> {

	/** Singleton {@link DBTypeFormat} instance. */
	public static final DBTypeFormat INSTANCE = new DBTypeFormat();

	private DBTypeFormat() {
		super(DBType.class);
	}

	@Override
	protected DBType getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return getSqlTypeCode(propertyValue.toString());
	}

	@Override
	protected String getSpecificationNonNull(DBType configValue) {
		return getSqlTypeName(configValue);
	}

	private static final Map<String, DBType> SQL_TYPES_BY_NAME;

	static {
		Map<String, DBType> all = new HashMap<>();

		// Normative names.
		for (DBType type : DBType.values()) {
			all.put(type.getExternalName(), type);
		}

		// Alias names.
		all.put("bit", DBType.BOOLEAN);
		all.put("tinyint", DBType.BYTE);
		all.put("smallint", DBType.SHORT);
		all.put("integer", DBType.INT);
		all.put("bigint", DBType.LONG);
		all.put("numeric", DBType.DECIMAL);
		all.put("timestamp", DBType.DATETIME);
		all.put("real", DBType.DOUBLE);
		all.put("binary", DBType.BLOB);
		all.put("longvarbinary", DBType.BLOB);
		all.put("varbinary", DBType.BLOB);
		all.put("nchar", DBType.CHAR);
		all.put("longvarchar", DBType.CLOB);
		all.put("nclob", DBType.CLOB);
		all.put("longnvarchar", DBType.CLOB);
		all.put("varchar", DBType.STRING);
		all.put("nvarchar", DBType.STRING);

		SQL_TYPES_BY_NAME = all;
	}

	private static String getSqlTypeName(DBType type) {
		return type.getExternalName();
	}

	private static DBType getSqlTypeCode(String sqlTypeName) throws ConfigurationException {
		DBType typeCode = SQL_TYPES_BY_NAME.get(sqlTypeName.toLowerCase());
		if (typeCode == null) {
			throw new ConfigurationException("Type code '" + sqlTypeName + "' does not exist.");
		}
		return typeCode;
	}

}

