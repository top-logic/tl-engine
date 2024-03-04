/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} updating a primitive attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLPropertyProcessor extends AbstractConfiguredInstance<UpdateTLPropertyProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLPropertyProcessor}.
	 * 
	 * @see CreateTLPropertyProcessor.Config
	 */
	@TagName("update-property")
	public interface Config
			extends PolymorphicConfiguration<UpdateTLPropertyProcessor>, UpdateTypePartConfig {

		/**
		 * See {@link PartConfig#getTypeSpec()}.
		 */
		QualifiedTypeName getNewType();

	}

	/**
	 * Configuration to update properties of a {@link TLTypePart}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface UpdateTypePartConfig extends AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name of the {@link TLTypePart} to update.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

		/**
		 * New name for the given property, including new module and new owner.
		 */
		QualifiedPartName getNewName();

		/**
		 * Setter for {@link #getNewName()}.
		 */
		void setNewName(QualifiedPartName value);

		/**
		 * See {@link PartConfig#getMandatory()}.
		 */
		@Name(PartConfig.MANDATORY)
		Boolean isMandatory();

		/**
		 * Setter for {@link #isMandatory()}.
		 */
		void setMandatory(Boolean value);

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(PartConfig.MULTIPLE_PROPERTY)
		Boolean isMultiple();

		/**
		 * Setter for {@link #isMultiple()}.
		 */
		void setMultiple(Boolean value);

		/**
		 * See {@link PartConfig#isOrdered()}.
		 */
		@Name(PartConfig.ORDERED_PROPERTY)
		Boolean isOrdered();

		/**
		 * Setter for {@link #isOrdered()}.
		 */
		void setOrdered(Boolean value);

		/**
		 * See {@link PartConfig#isBag()}.
		 */
		@Name(PartConfig.BAG_PROPERTY)
		Boolean isBag();

		/**
		 * Setter for {@link #isBag()}.
		 */
		void setBag(Boolean value);

	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Update part migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		TypePart part;
		try {
			part = _util.getTLTypePartOrFail(connection, partName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find property to update " + _util.qualifiedName(partName) + " at " + getConfig().location(),
				Log.WARN);
			return false;
		}
		Type newType;
		if (getConfig().getNewType() == null) {
			newType = null;
		} else {
			newType = _util.getTLTypeOrFail(connection, getConfig().getNewType());
		}
		Type newOwner;
		QualifiedPartName newName = getConfig().getNewName();
		if (newName == null || (partName.getModuleName().equals(newName.getModuleName())
			&& partName.getTypeName().equals(newName.getTypeName()))) {
			newOwner = null;
		} else {
			newOwner = _util.getTLTypeOrFail(connection, newName);
		}
		String newLocalName;
		if (newName == null || partName.getPartName().equals(newName.getPartName())) {
			newLocalName = null;
		} else {
			newLocalName = newName.getPartName();
		}
		_util.updateTLProperty(connection, part,
			newType, newOwner, newLocalName,
			getConfig().isMandatory(), getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(),
			getConfig());
		MigrationUtils.updateProperty(log, tlModel, partName, newName, getConfig().getNewType(),
			getConfig().isMandatory(), getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(),
			getConfig());
		log.info("Updated part " + _util.qualifiedName(partName));

		return true;
	}

}
