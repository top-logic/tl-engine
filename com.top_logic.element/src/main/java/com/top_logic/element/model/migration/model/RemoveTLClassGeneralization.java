/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;

/**
 * {@link MigrationProcessor} removing generalizations from a {@link TLClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveTLClassGeneralization extends AbstractConfiguredInstance<RemoveTLClassGeneralization.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link RemoveTLClassGeneralization}.
	 */
	@TagName("remove-class-generalizations")
	public interface Config extends PolymorphicConfiguration<RemoveTLClassGeneralization> {

		/**
		 * Qualified name of the {@link TLClass} to remove generalizations from.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * The extended types.
		 */
		@Name(ObjectTypeConfig.GENERALIZATIONS)
		@EntryTag(ExtendsConfig.TAG_NAME)
		List<Generalization> getGeneralizations();
	}

	/**
	 * Configuration of a generalization.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Generalization extends ConfigurationItem {

		/**
		 * The extended type.
		 */
		@Mandatory
		@Name(ExtendsConfig.TYPE)
		QualifiedTypeName getType();

	}

	/**
	 * Creates a {@link RemoveTLClassGeneralization} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveTLClassGeneralization(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Removing generalization extension migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName specialisation = getConfig().getName();
		for (Generalization generalization : getConfig().getGeneralizations()) {
			QualifiedTypeName typeName = generalization.getType();
			Util.removeGeneralisation(connection, specialisation, typeName);
			log.info(
				"Removed generalisation "
					+ Util.qualifiedName(typeName) + " from "
					+ Util.qualifiedName(specialisation));
		}
	}

}
