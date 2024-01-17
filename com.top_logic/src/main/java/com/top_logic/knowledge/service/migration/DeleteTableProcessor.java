/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLDelete;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor.Config.TableRef;

/**
 * {@link MigrationProcessor} deleting no longer used tables.
 * 
 * @see Config#getTables()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteTableProcessor extends AbstractConfiguredInstance<DeleteTableProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTableProcessor}.
	 */
	public interface Config extends PolymorphicConfiguration<DeleteTableProcessor> {
		/**
		 * Names of the table types to delete.
		 */
		@Name("tables")
		List<TableRef> getTables();

		/**
		 * Names of type providers to remove from the stored DB configuration.
		 */
		@ListBinding(attribute = "name")
		List<String> getProviders();

		/**
		 * Reference to the table to delete.
		 */
		interface TableRef extends MetaObjectName {

			/**
			 * Optional name of the table as used in SQL.
			 * 
			 * @see #getObjectName()
			 */
			@Name(DOXMLConstants.DB_NAME_ATTRIBUTE)
			@Nullable
			String getDBName();

		}
	}

	/**
	 * Creates a {@link DeleteTableProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTableProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			SQLQuery<SQLDelete> delete = query(
				parameters(parameterDef(DBType.STRING, "typeName")),
				delete(
					table(SQLH.mangleDBName(BranchSupport.BRANCH_SWITCH_TYPE_NAME)),
					eqSQL(column(BranchSupport.LINK_TYPE_COLUMN), parameter(DBType.STRING, "typeName"))));

			DBHelper sqlDialect = connection.getSQLDialect();
			CompiledStatement statement = delete.toSql(sqlDialect);

			PersistencyLayer.Config kbConfig = (PersistencyLayer.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(PersistencyLayer.class);
			String kbName = kbConfig.getDefaultKnowledgeBase();
			String schemaXml = KBSchemaUtil.loadSchemaRaw(connection, kbName);
			Document schema = DOMUtil.parse(schemaXml);

			Map<String, String> found = new HashMap<>();
			if (!getConfig().getTables().isEmpty()) {
				String filter = getConfig().getTables().stream()
					.map(config -> "@object_name='" + config.getObjectName() + "'").collect(Collectors.joining(" or "));

				XPath xPath = XPathFactory.newInstance().newXPath();
				XPathExpression tablesQuery =
					xPath.compile("//metaobject[" + filter + "] | //association[" + filter + "]");
				NodeList tables = (NodeList) tablesQuery.evaluate(schema, XPathConstants.NODESET);
				for (int n = tables.getLength() - 1; n >= 0; n--) {
					Element table = (Element) tables.item(n);
					table.getParentNode().removeChild(table);
					String tableName = table.getAttributeNS(null, "object_name");
					String dbName = StringServices.nonEmpty(table.getAttributeNS(null, "db_name"));
					found.put(tableName, dbName);
				}
			}

			if (!getConfig().getProviders().isEmpty()) {
				String filter = getConfig().getProviders().stream()
					.map(name -> "@name='" + name + "'").collect(Collectors.joining(" or "));

				XPath xPath = XPathFactory.newInstance().newXPath();
				XPathExpression providerQuery =
					xPath.compile("//providers/provider[" + filter + "]");
				NodeList providers = (NodeList) providerQuery.evaluate(schema, XPathConstants.NODESET);
				for (int n = providers.getLength() - 1; n >= 0; n--) {
					Element provider = (Element) providers.item(n);
					provider.getParentNode().removeChild(provider);
					log.info("Removing type provider '" + provider.getAttributeNS(null, "name") + "'.");
				}
			}

			try (Statement nativeStatement = connection.createStatement()) {
				for (TableRef tableRef : getConfig().getTables()) {
					String typeName = tableRef.getObjectName();
					if (!found.containsKey(typeName)) {
						log.info("Table '" + typeName + "' did not exist in persistent schema, was not removed.",
							Log.WARN);
						continue;
					}

					statement.executeUpdate(connection, typeName);
					log.info("Deleted branch switch for table '" + typeName + "'.");

					// Explicitly configured DB name.
					String dbName = tableRef.getDBName();
					if (dbName == null) {
						// DB name formerly used in the DB schema.
						dbName = found.get(typeName);
						if (dbName == null) {
							// Default DB name.
							dbName = SQLH.mangleDBName(tableRef.getObjectName());
						}
					}

					// IGNORE FindBugs(SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE): DDL statements cannot use prepared statements.
					nativeStatement.execute("DROP TABLE " + sqlDialect.tableRef(dbName));
					log.info("Deleted table '" + dbName + "'.");
				}
			}

			String newSchemaXml = DOMUtil.toString(schema);
			KBSchemaUtil.storeSchemaRaw(connection, kbName, newSchemaXml);
		} catch (SQLException | ConfigurationException | XPathExpressionException ex) {
			log.error("Delete table migration failed.", ex);
		}
	}

}
