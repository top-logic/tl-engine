/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that applies a XSLT to all values of a certain column of a certain
 * table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XsltColumnProcessor extends StringColumnTransformProcessor<XsltColumnProcessor.Config<?>> {

	/**
	 * Configuration options for {@link XsltColumnProcessor}.
	 */
	public interface Config<I extends XsltColumnProcessor> extends StringColumnTransformProcessor.Config<I> {

		/**
		 * The XSLT resource.
		 * 
		 * @see FileManager#getStream(String)
		 */
		@Mandatory
		String getTransform();

		/**
		 * Whether to properly indent the result.
		 */
		boolean getIndent();

	}

	/**
	 * Creates a {@link XsltColumnProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public XsltColumnProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void processRows(Log log, ResultSet rows, String table, String column, List<String> keyColumns)
			throws SQLException {

		Config<?> config = getConfig();
		String transform = config.getTransform();
		log.info("Applying '" + transform + "' to all values in column '" + column + "' of table '" + table + "'.");

		Transformer transformer = XsltUtil.createTransformer(transform, config.getIndent());
		while (rows.next()) {
			String xml = rows.getString(1);
			if (xml == null || rows.wasNull()) {
				continue;
			}

			xml = xml.trim();
			if (xml.isEmpty()) {
				continue;
			}

			try {
				StringWriter buffer = new StringWriter();
				transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(buffer));
				String transformationResult = buffer.toString();
				rows.updateString(1, transformationResult);
				rows.updateRow();
			} catch (TransformerException ex) {
				String keySpec;
				if (keyColumns.isEmpty()) {
					keySpec = "";
				} else {
					StringBuilder keyBuffer = new StringBuilder();
					keyBuffer.append(" (");
					for (int n = 0, cnt = keyColumns.size(); n < cnt; n++) {
						if (n > 0) {
							keyBuffer.append(", ");
						}
						keyBuffer.append(keyColumns.get(n));
						keyBuffer.append("=");
						keyBuffer.append(rows.getObject(2 + n));
					}
					keyBuffer.append(")");
					keySpec = keyBuffer.toString();
				}
				log.error("Failed to transform value in column '" + column + "' of table '"
					+ table + "'" + keySpec + ": " + xml, ex);
			}
		}
	}

}
