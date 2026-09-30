/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that updates the {@link DBProperties TL properties}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateTLPropertiesProcessor extends AbstractConfiguredInstance<UpdateTLPropertiesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link UpdateTLPropertiesProcessor}.
	 */
	@TagName("update-tl-properties")
	public interface Config extends PolymorphicConfiguration<UpdateTLPropertiesProcessor> {

		/**
		 * The cluster node name of the node-local property. When the property is cluster global,
		 * then {@value DBProperties#GLOBAL_PROPERTY} must be set.
		 */
		@NonNullable
		@StringDefault(DBProperties.GLOBAL_PROPERTY)
		String getNodeName();

		/**
		 * The property to update.
		 */
		@Mandatory
		String getProperty();

		/**
		 * The new property value to set. May be <code>null</code>. In this case the property
		 * assignment is removed.
		 */
		@Nullable
		String getValue();
	}

	/**
	 * Create a {@link UpdateTLPropertiesProcessor}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public UpdateTLPropertiesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config config = getConfig();
		String property = config.getProperty();

		log.info("Updating property '" + property + "'.");
		try {
			DBProperties.setProperty(connection, config.getNodeName(), property, config.getValue());
		} catch (SQLException ex) {
			log.error("Unable to update property '" + property + "'.", ex);
		}
	}

}

