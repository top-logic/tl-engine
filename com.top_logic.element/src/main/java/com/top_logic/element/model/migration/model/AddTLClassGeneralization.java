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
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.impl.generated.TLObjectBase;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} creating generalizations from a {@link TLClass} to a list of other
 * {@link TLClass}es.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddTLClassGeneralization extends AbstractConfiguredInstance<AddTLClassGeneralization.Config>
		implements TLModelBaseLineMigrationProcessor {

	private static final String QUALIFIED_TL_OBJECT_NAME =
		TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObjectBase.TL_OBJECT_TYPE);

	/**
	 * Configuration options of {@link AddTLClassGeneralization}.
	 */
	@TagName("add-class-generalizations")
	public interface Config extends PolymorphicConfiguration<AddTLClassGeneralization>,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Qualified name of the {@link TLClass} to add generalizations for.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName name);

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

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(QualifiedTypeName type);

	}

	private Util _util;

	/**
	 * Creates a {@link AddTLClassGeneralization} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddTLClassGeneralization(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Adding generalization extension migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedTypeName specialisation = getConfig().getName();
		boolean updateModelBaseline = false;
		for (Generalization generalization : getConfig().getGeneralizations()) {
			QualifiedTypeName typeName = generalization.getType();
			_util.addGeneralisation(connection, specialisation, typeName);
			if (tlModel == null || getConfig().isSkipModelBaselineChange()) {
				// skip model baseline change
			} else if (QUALIFIED_TL_OBJECT_NAME.equals(
				typeName.getName()) && !TlModelFactory.TL_MODEL_STRUCTURE.equals(specialisation.getModuleName())) {
				/* TLObject extension is typically not contained in model baseline, except for
				 * module "tl.model". */
			} else {
				MigrationUtils.addGeneralisation(log, tlModel, specialisation, typeName);
				updateModelBaseline = true;
			}
			log.info(
				"Added generalisation "
					+ _util.qualifiedName(typeName) + " to "
					+ _util.qualifiedName(specialisation));
		}
		return updateModelBaseline;
	}

}
