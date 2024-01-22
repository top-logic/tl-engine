/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migration;

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
import com.top_logic.basic.db.schema.properties.DBPropertiesSchema;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.sql.SQLH;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.migration.TLModelMigrationUtil;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.layout.tools.rewrite.RewriteMigrationProcessor;
import com.top_logic.model.search.providers.DefaultByExpression;

/**
 * Migrates the {@link DefaultByExpression} by removing the value for ui configuration property
 * using the given rewriter.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultByExpressionProcessor extends RewriteMigrationProcessor<RewriteMigrationProcessor.Config<?>> {

	/**
	 * Creates a {@link DefaultByExpressionProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DefaultByExpressionProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void doMigration(Log log, PooledConnection connection, DocumentRewrite rewriter) {
		rewriteMetaAttributes(log, connection, rewriter);
		rewriteTLProperties(log, connection, rewriter);
	}

	private void rewriteMetaAttributes(Log log, PooledConnection connection, DocumentRewrite rewriter) {
		String annotationColumn = SQLH.mangleDBName("annotations");

		SQLSelect select = createMetaAttributeSelect(annotationColumn);

		rewrite(log, connection, rewriter, annotationColumn, select);
	}

	private void rewriteTLProperties(Log log, PooledConnection connection, DocumentRewrite rewriter) {
		String propValueColumn = DBPropertiesSchema.PROP_VALUE_COLUMN_NAME;

		SQLSelect select = createTLPropertiesSelect(propValueColumn);

		rewrite(log, connection, rewriter, propValueColumn, select);
	}

	private SQLSelect createMetaAttributeSelect(String annotationColumn) {
		return select(
			columns(
				columnDef(BasicTypes.BRANCH_DB_NAME),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME),
				columnDef(annotationColumn)),
			table(SQLH.mangleDBName(TLModelMigrationUtil.STRUCTURED_TYPE_PART_MO)));
	}

	private SQLSelect createTLPropertiesSelect(String propValueColumn) {
		String propKeyColumn = DBPropertiesSchema.PROP_KEY_COLUMN_NAME;

		return select(
			columns(
				columnDef(DBPropertiesSchema.NODE_COLUMN_NAME),
				columnDef(propKeyColumn),
				columnDef(propValueColumn)),
			table(SQLH.mangleDBName(DBPropertiesSchema.TABLE_NAME)),
			eqSQL(column(propKeyColumn), literalString("applicationModel")));
	}

	private void rewrite(Log log, PooledConnection connection, DocumentRewrite rewriter, String column, SQLSelect select) {
		try {
			CompiledStatement statement = createStatement(connection, select);

			try (ResultSet result = statement.executeQuery(connection)) {
				if (result.next()) {
					do {
						String data = result.getString(column);
						if (data != null) {
							rewriteData(log, rewriter, column, result, data);
						}
					} while (result.next());
				}
			}
		} catch (SQLException exception) {
			log.error("Failed to process default by expressions.", exception);
		}
	}

	private void rewriteData(Log log, DocumentRewrite rewriter, String column, ResultSet result, String data) {
		try (Reader in = new StringReader(data)) {
			Document document = DOMUtil.getDocumentBuilder().parse(new InputSource(in));

			if (rewriter.rewrite(document)) {
				result.updateString(column, DOMUtil.toString(document));
				result.updateRow();
			}
		} catch (SQLException | IOException | SAXException exception) {
			log.error("Failed to process default by expressions.", exception);
		}
	}

	private CompiledStatement createStatement(PooledConnection connection, SQLSelect select) throws SQLException {
		CompiledStatement statement = query(select).toSql(connection.getSQLDialect());

		statement.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		return statement;
	}

}
