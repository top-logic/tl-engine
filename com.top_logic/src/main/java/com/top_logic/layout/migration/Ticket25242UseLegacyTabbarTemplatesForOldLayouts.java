/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.sql.SQLH;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.mig.html.layout.PersistentTemplateLayoutWrapper;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} rewriting legacy in-app tabbar definitions to use the legacy template.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Ticket25242UseLegacyTabbarTemplatesForOldLayouts implements MigrationProcessor {

	private Util _util;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		_util = context.getSQLUtils();

		String table = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
		String templateColumn = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.TEMPLATE_ATTR);
		String argumentsColumn = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.ARGUMENTS_ATTR);

		SQLSelect select = select(
			columns(
				_util.branchColumnDef(),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME),
				columnDef(templateColumn),
				columnDef(argumentsColumn)),
			table(table),
			eqSQL(
				column(templateColumn),
				literalString("com.top_logic/tabbar.template.xml")));

		try {
			CompiledStatement statement = query(select).toSql(connection.getSQLDialect());
			statement.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			try (ResultSet result = statement.executeQuery(connection)) {
				if (result.next()) {
					// Legacy template must be used, if any argument other than "components" is
					// used.
					XPathExpression check = XPathFactory.newInstance().newXPath()
						.compile("/arguments/@*|/arguments/*[local-name(.) != 'components']");
					do {
						String data = result.getString(argumentsColumn);
						if (data != null) {
							try (Reader in = new StringReader(data)) {
								Document document = DOMUtil.getDocumentBuilder().parse(new InputSource(in));
								NodeList nodes = (NodeList) check.evaluate(document, XPathConstants.NODESET);
								if (nodes.getLength() > 0) {
									result.updateString(templateColumn, "com.top_logic/legacyTabbar.template.xml");
									result.updateRow();
								}
							}
						}
					} while (result.next());
				}
			}
		} catch (SQLException | IOException | SAXException | XPathException ex) {
			log.error("Failed to process template layouts.", ex);
		}
	}

}
