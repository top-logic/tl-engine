/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLProperty;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link MigrationProcessor} updating a primitive attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLPropertyProcessor extends AbstractConfiguredInstance<UpdateTLPropertyProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLPropertyProcessor}.
	 * 
	 * @see CreateTLPropertyProcessor.Config
	 */
	@TagName("update-property")
	public interface Config
			extends PolymorphicConfiguration<UpdateTLPropertyProcessor>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name of the {@link TLProperty} to update.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * New name for the given property, including new module and new owner.
		 */
		QualifiedPartName getNewName();

		/**
		 * See {@link PartConfig#getTypeSpec()}.
		 */
		QualifiedTypeName getNewType();

		/**
		 * See {@link PartConfig#getMandatory()}.
		 */
		@Name(PartConfig.MANDATORY)
		Boolean isMandatory();

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(PartConfig.MULTIPLE_PROPERTY)
		Boolean isMultiple();

		/**
		 * See {@link PartConfig#isOrdered()}.
		 */
		@Name(PartConfig.ORDERED_PROPERTY)
		Boolean isOrdered();

		/**
		 * See {@link PartConfig#isBag()}.
		 */
		@Name(PartConfig.BAG_PROPERTY)
		Boolean isBag();

	}

	/**
	 * Creates a {@link UpdateTLPropertyProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLPropertyProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Update part migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		TypePart part;
		try {
			part = Util.getTLTypePartOrFail(connection, partName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find property to update " + Util.qualifiedName(partName) + " at " + getConfig().location(),
				Log.WARN);
			return;
		}
		Type newType;
		if (getConfig().getNewType() == null) {
			newType = null;
		} else {
			newType = Util.getTLTypeOrFail(connection, getConfig().getNewType());
		}
		Type newOwner;
		QualifiedPartName newName = getConfig().getNewName();
		if (newName == null || (partName.getModuleName().equals(newName.getModuleName())
			&& partName.getTypeName().equals(newName.getTypeName()))) {
			newOwner = null;
		} else {
			newOwner = Util.getTLTypeOrFail(connection, newName);
		}
		String newLocalName;
		if (newName == null || partName.getPartName().equals(newName.getPartName())) {
			newLocalName = null;
		} else {
			newLocalName = newName.getPartName();
		}
		Util.updateTLProperty(connection, part,
			newType, newOwner, newLocalName,
			getConfig().isMandatory(), getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(),
			getConfig());
		log.info("Updated part " + Util.qualifiedName(partName));
	}

}
