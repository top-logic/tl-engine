/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModel;

/**
 * {@link MigrationProcessor} that needs to adapt the stored {@link TLModel} base line.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TLModelBaseLineMigrationProcessor<C extends TLModelBaseLineMigrationProcessor.Config<?>>
		extends AbstractConfiguredInstance<C> implements MigrationProcessor {

	/**
	 * Configuration for a {@link MigrationProcessor} to skip change of model base line in
	 * exceptional cases.
	 */
	public interface Config<I extends TLModelBaseLineMigrationProcessor<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Whether the model base line in the database must not be changed.
		 * 
		 * <p>
		 * This configuration is an additional option for {@link MigrationProcessor} which normally
		 * change the model baseline. Here you can configure that this does not happen in
		 * exceptional cases.
		 * </p>
		 */
		boolean isSkipModelBaselineChange();

		/**
		 * @see #isSkipModelBaselineChange()
		 */
		void setSkipModelBaselineChange(boolean value);
	}

	/**
	 * Creates a {@link TLModelBaseLineMigrationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLModelBaseLineMigrationProcessor(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		if (getConfig().isSkipModelBaselineChange()) {
			migrateTLModel(context, log, connection, null);
		} else {
			MigrationUtils.modifyTLModel(log, connection,
				document -> migrateTLModel(context, log, connection, document));
		}
	}

	/**
	 * Migrates the persistent {@link TLModel} (or parts of it) in the database and adapts the
	 * stored model base line.
	 * 
	 * @param context
	 *        The migration context.
	 * @param log
	 *        The {@link Log} to log informations and potential errors to.
	 * @param connection
	 *        The connection to the database to modify
	 * @param tlModel
	 *        The parsed model base line from the database, or <code>null</code> if no model
	 *        baseline exists.
	 * 
	 * @return Whether the given {@link Document model} has been adapted and needs to be written to
	 *         the database.
	 */
	protected abstract boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection,
			Document tlModel);

}

