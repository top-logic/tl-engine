/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.mig.html.layout.PersistentTemplateLayoutWrapper;

/**
 * Migrates the layout template arguments using the configured {@link DocumentRewrite}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutMigrationProcessor extends RewriteMigrationProcessor<RewriteMigrationProcessor.Config<?>> {

	/**
	 * Creates a {@link LayoutMigrationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LayoutMigrationProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void doMigration(Log log, PooledConnection connection, DocumentRewrite rewriter) {
		String table = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
		String layoutKeyColumn = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.LAYOUT_KEY_ATTR);
		String templateColumn = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.TEMPLATE_ATTR);
		String argumentsColumn = SQLH.mangleDBName(PersistentTemplateLayoutWrapper.ARGUMENTS_ATTR);

		SQLSelect select = select(
			columns(
				util().branchColumnDef(),
				columnDef(column(null, BasicTypes.IDENTIFIER_DB_NAME)),
				columnDef(column(null, BasicTypes.REV_MAX_DB_NAME)),
				columnDef(column(null, layoutKeyColumn)),
				columnDef(column(null, templateColumn)),
				columnDef(column(null, argumentsColumn))),
			table(table));

		try {
			CompiledStatement statement = query(select).toSql(connection.getSQLDialect());
			statement.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			try (ResultSet result = statement.executeQuery(connection)) {
				if (result.next()) {
					do {
						String data = result.getString(argumentsColumn);
						if (data != null) {
							String layoutKey = result.getString(layoutKeyColumn);
							String template = result.getString(templateColumn);
							try (Reader in = new StringReader(data)) {
								Document document = DOMUtil.getDocumentBuilder().parse(new InputSource(in));
								if (rewrite(rewriter, layoutKey, template, document)) {
									result.updateString(argumentsColumn, DOMUtil.toString(document));
									result.updateRow();
								}
							}
						}
					} while (result.next());
				}
			}
		} catch (SQLException | IOException | SAXException ex) {
			log.error("Failed to process template layouts.", ex);
		}
	}

	private boolean rewrite(DocumentRewrite rewriter, String layoutKey, String template, Document arguments) {
		if (rewriter instanceof LayoutRewrite) {
			return ((LayoutRewrite) rewriter).rewriteLayout(layoutKey, template, arguments);
		} else {
			return rewriter.rewrite(arguments);
		}
	}
}
