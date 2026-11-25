/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;

/**
 * Access to the type definitions of the schema stored in the database, without parsing it into
 * typed configuration.
 *
 * <p>
 * The pre-processors of a migration adjust the stored schema to code changes, e.g. remove the
 * definition of a table whose implementation class no longer exists. Until all of them have run,
 * the stored schema may not be parseable as typed configuration. A pre-processor that has to look
 * at the stored schema therefore reads it as DOM with these methods.
 * </p>
 */
public class StoredSchemaTypes {

	/**
	 * Loads the schema stored for the default knowledge base.
	 */
	public static Document loadStoredSchema(PooledConnection connection) throws ConfigurationException {
		String schemaXml = KBSchemaUtil.loadSchemaRaw(connection, defaultKnowledgeBase());
		return DOMUtil.parse(schemaXml);
	}

	/**
	 * Stores the given schema as schema of the default knowledge base.
	 */
	public static void storeSchema(PooledConnection connection, Document schema)
			throws SQLException, ConfigurationException {
		KBSchemaUtil.storeSchemaRaw(connection, defaultKnowledgeBase(), DOMUtil.toString(schema));
	}

	private static String defaultKnowledgeBase() throws ConfigurationException {
		PersistencyLayer.Config kbConfig = (PersistencyLayer.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(PersistencyLayer.class);
		return kbConfig.getDefaultKnowledgeBase();
	}

	/**
	 * The definitions of the types with the given names in the given stored schema.
	 *
	 * <p>
	 * A definition is a {@link MetaObjectsConfig#ALTERNATIVE_TAG_NAME alternative}, an association
	 * or a meta object. A name without definition has no entry in the result.
	 * </p>
	 */
	public static List<Element> findTypes(Document schema, Collection<String> typeNames)
			throws XPathExpressionException {
		List<Element> result = new ArrayList<>();
		if (typeNames.isEmpty()) {
			return result;
		}
		String filter = typeNames.stream()
			.map(name -> "@object_name='" + name + "'").collect(Collectors.joining(" or "));

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression typesQuery =
			xPath.compile("//metaobject[" + filter + "] | //association[" + filter + "] | //alternative["
				+ filter + "]");
		NodeList types = (NodeList) typesQuery.evaluate(schema, XPathConstants.NODESET);
		for (int n = 0, cnt = types.getLength(); n < cnt; n++) {
			result.add((Element) types.item(n));
		}
		return result;
	}

	/**
	 * The name of the given type definition.
	 */
	public static String typeName(Element type) {
		return type.getAttributeNS(null, DOXMLConstants.OBJECT_NAME_ATTRIBUTE);
	}

	/**
	 * The name of the database table that the given type definition explicitly configures,
	 * <code>null</code> if it uses the default name.
	 *
	 * @see #tableName(Element)
	 */
	public static String explicitTableName(Element type) {
		return StringServices.nonEmpty(type.getAttributeNS(null, DOXMLConstants.DB_NAME_ATTRIBUTE));
	}

	/**
	 * The name of the database table of the given type definition.
	 */
	public static String tableName(Element type) {
		String dbName = explicitTableName(type);
		if (dbName != null) {
			return dbName;
		}
		return SQLH.mangleDBName(typeName(type));
	}

	/**
	 * Whether the given type definition has a database table.
	 *
	 * <p>
	 * Abstract types and alternatives only exist in the schema.
	 * </p>
	 */
	public static boolean hasTable(Element type) {
		if (MetaObjectsConfig.ALTERNATIVE_TAG_NAME.equals(type.getLocalName())
			|| MetaObjectsConfig.ALTERNATIVE_TAG_NAME.equals(type.getTagName())) {
			return false;
		}
		return !Boolean.parseBoolean(type.getAttributeNS(null, DOXMLConstants.ABSTRACT_ATTRIBUTE));
	}

}
