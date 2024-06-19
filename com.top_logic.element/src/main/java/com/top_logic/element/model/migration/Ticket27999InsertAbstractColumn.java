/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that adds the column "abstract" in the table "MetaAttribute".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket27999InsertAbstractColumn extends AbstractConfiguredInstance<Ticket27999InsertAbstractColumn.Config>
		implements MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link Ticket27999InsertAbstractColumn}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<Ticket27999InsertAbstractColumn> {

		/**
		 * The {@link MigrationProcessor} that creates the abstract column.
		 */
		@Mandatory
		@DefaultContainer
		PolymorphicConfiguration<MigrationProcessor> getCreateColumnProcessor();

	}

	private MigrationProcessor _createProcessor;

	/**
	 * Create a {@link Ticket27999InsertAbstractColumn}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public Ticket27999InsertAbstractColumn(InstantiationContext context, Config config) {
		super(context, config);
		_createProcessor = context.getInstance(config.getCreateColumnProcessor());
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		_createProcessor.doMigration(context, log, connection);
		context.getSQLUtils().setAbstractColumn(true);
	}
}
