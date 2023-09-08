/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;

/**
 * Utilities for loading and saving the {@link KnowledgeBase} schema to the {@link DBProperties}
 * table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class KBSchemaUtil {

	/**
	 * Suffix of the property name to store the configuration of the {@link SchemaSetup} in the
	 * {@link DBProperties}.
	 */
	private static final String DB_PROPERTIES_SCHEMA_SUFFIX = "schemaSetup";

	private static final String DB_PROPERTIES_PREFIX = "KnowledgeBase.";

	/**
	 * Property name to store the configuration of the {@link SchemaSetup} in the
	 * {@link DBProperties}.
	 * 
	 * <p>
	 * The actual property name for the {@link KnowledgeBase} with name "foo" is
	 * "KnowledgeBase.foo.schemaSetup".
	 * </p>
	 * 
	 * @param kbName
	 *        Name of the {@link KnowledgeBase}.
	 * 
	 * @see KBSchemaUtil#DB_PROPERTIES_SCHEMA_SUFFIX
	 */
	@FrameworkInternal
	public static String dbPropertiesSchemaSetup(String kbName) {
		return KBSchemaUtil.dbPropertiesPrefix(kbName).append(KBSchemaUtil.DB_PROPERTIES_SCHEMA_SUFFIX).toString();
	}

	/**
	 * Loads the ({@link SchemaSetup#resolve(InstantiationContext) resolved}) {@link SchemaSetup}
	 * stored for the {@link KnowledgeBase} with the given name.
	 * 
	 * @return The loaded {@link SchemaConfiguration} or <code>null</code> if no stored schema
	 *         exists.
	 * 
	 * @throws KnowledgeBaseException
	 *         If loading or parsing the schema fails.
	 */
	public static SchemaConfiguration loadSchema(PooledConnection connection, String kbName)
			throws KnowledgeBaseException {
		String xml = loadSchemaRaw(connection, kbName);
		if (xml == null) {
			return null;
		}
		try {
			return parseSchema(xml);
		} catch (ConfigurationException ex) {
			throw new KnowledgeBaseException("Unable to parse schema configuration: " + xml, ex);
		}
	}

	/**
	 * Loads the {@link SchemaConfiguration} as raw XML string as stored in the DB.
	 */
	public static String loadSchemaRaw(PooledConnection connection, String kbName) {
		String propertyName = dbPropertiesSchemaSetup(kbName);
	
		try {
			if (!DBProperties.tableExists(connection)) {
				return null;
			}
			return DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, propertyName);
		} catch (SQLException ex) {
			throw new KnowledgeBaseException("Unable to fetch DB property '" + propertyName + ".", ex);
		}
	}

	/**
	 * Parses a XML serialized {@link SchemaConfiguration} (as stored in the {@link DBProperties}
	 * table).
	 */
	public static SchemaConfiguration parseSchema(String xml) throws ConfigurationException {
		return (SchemaConfiguration) TypedConfiguration.fromString(xml);
	}

	/**
	 * Stores the given {@link SchemaConfiguration} for the {@link KnowledgeBase} with the given
	 * name to the {@link DBProperties} table.
	 */
	public static void storeSchema(PooledConnection connection, String kbName, SchemaConfiguration schema)
			throws SQLException {
		storeSchemaRaw(connection, kbName, serializeSchema(schema));
	}

	/**
	 * Stores the given raw XML {@link SchemaConfiguration} string for the {@link KnowledgeBase}
	 * with the given name to the {@link DBProperties} table.
	 */
	public static void storeSchemaRaw(PooledConnection connection, String kbName, String xml) throws SQLException {
		String propertyName = KBSchemaUtil.dbPropertiesSchemaSetup(kbName);
		DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, propertyName, xml);
	}

	/**
	 * Serializes a given {@link SchemaConfiguration} to XML.
	 */
	public static String serializeSchema(SchemaConfiguration schema) {
		String xml = TypedConfiguration.toString(schema);
		return xml;
	}

	/**
	 * Prefix for the properties in the {@link DBProperties} table used by {@link KnowledgeBase}
	 * implementations.
	 * 
	 * <p>
	 * The actual prefix for the {@link KnowledgeBase} with name "foo" is "KnowledgeBase.foo.".
	 * </p>
	 * 
	 * @param kbName
	 *        Name of the {@link KnowledgeBase}.
	 * 
	 * @see KBSchemaUtil#DB_PROPERTIES_PREFIX
	 */
	private static StringBuilder dbPropertiesPrefix(String kbName) {
		return new StringBuilder().append(KBSchemaUtil.DB_PROPERTIES_PREFIX).append(kbName).append('.');
	}

}
