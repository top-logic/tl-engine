/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import java.sql.SQLException;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.service.KnowledgeBaseName;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that applies a XSLT to the stored base-line of the application's table
 * definitions (as preparation for an automatic schema migration).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XsltTableDefinitionBaselineProcessor extends
		AbstractXsltProcessor<XsltTableDefinitionBaselineProcessor.Config<?>, XsltTableDefinitionBaselineProcessor> {

	/**
	 * Configuration options for {@link XsltTableDefinitionBaselineProcessor}.
	 */
	public interface Config<I extends XsltTableDefinitionBaselineProcessor>
			extends AbstractXsltProcessor.Config<I>, KnowledgeBaseName {

		// No additional options

	}

	/**
	 * Creates a {@link XsltTableDefinitionBaselineProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public XsltTableDefinitionBaselineProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Pretty prints the given document as table definitions are stored pretty printed.
	 */
	@Override
	protected String transformedToString(Document transformed) {
		return DOMUtil.toString(transformed, true, true);
	}

	@Override
	protected void storeNewValue(PooledConnection connection, String newValue) throws SQLException {
		KBSchemaUtil.storeSchemaRaw(connection, getConfig().getKnowledgeBase(), newValue);
	}

	@Override
	protected String readOldValue(PooledConnection connection) throws SQLException {
		return KBSchemaUtil.loadSchemaRaw(connection, getConfig().getKnowledgeBase());
	}

}
