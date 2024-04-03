/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedTypeName;

/**
 * {@link MigrationProcessor} removing generalizations from a {@link TLClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveTLClassGeneralization extends AbstractConfiguredInstance<RemoveTLClassGeneralization.Config>
		implements TLModelBaseLineMigrationProcessor {

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
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

		/**
		 * The extended types.
		 */
		@Name(ObjectTypeConfig.GENERALIZATIONS)
		@EntryTag(ExtendsConfig.TAG_NAME)
		@DefaultContainer
		List<AddTLClassGeneralization.Generalization> getGeneralizations();
	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Removing generalization extension migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		boolean updateTLModel = false;
		QualifiedTypeName specialisation = getConfig().getName();
		for (AddTLClassGeneralization.Generalization generalization : getConfig().getGeneralizations()) {
			QualifiedTypeName typeName = generalization.getType();
			_util.removeGeneralisation(connection, specialisation, typeName);
			boolean removed =
				tlModel == null ? false : MigrationUtils.removeGeneralisation(log, tlModel, specialisation, typeName);
			log.info(
				"Removed generalisation "
					+ _util.qualifiedName(typeName) + " from "
					+ _util.qualifiedName(specialisation));
			updateTLModel |= removed;
		}
		return updateTLModel;
	}

}
