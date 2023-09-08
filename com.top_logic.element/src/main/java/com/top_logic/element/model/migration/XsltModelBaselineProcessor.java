/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.db2.migration.processor.AbstractXsltProcessor;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that applies a XSLT to the stored base-line of the application model
 * (as preparation for an automatic model migration).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XsltModelBaselineProcessor
		extends AbstractXsltProcessor<AbstractXsltProcessor.Config<?>, XsltModelBaselineProcessor> {

	/**
	 * Creates a {@link XsltModelBaselineProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public XsltModelBaselineProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void storeNewValue(PooledConnection connection, String newValue) throws SQLException {
		DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
			DynamicModelService.APPLICATION_MODEL_PROPERTY, newValue);
	}

	@Override
	protected String readOldValue(PooledConnection connection) throws SQLException {
		String oldModelXml = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
			DynamicModelService.APPLICATION_MODEL_PROPERTY);
		return oldModelXml;
	}

}
