/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link MigrationProcessor} updating a {@link TLAssociationEnd}.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class UpdateTLAssociationEndProcessor extends AbstractConfiguredInstance<UpdateTLAssociationEndProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLAssociationEndProcessor}.
	 */
	@TagName("update-association-end")
	public interface Config
			extends PolymorphicConfiguration<UpdateTLAssociationEndProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLAssociationEnd} to update.
		 */
		QualifiedPartName getName();

		/**
		 * New name of the association end.
		 */
		QualifiedPartName getNewName();

		/**
		 * See {@link PartConfig#getMandatory()}
		 */
		@Name(PartConfig.MANDATORY)
		Boolean isMandatory();

		/**
		 * See {@link EndAspect#isComposite()}.
		 */
		@Name(EndAspect.COMPOSITE_PROPERTY)
		Boolean isComposite();

		/**
		 * See {@link EndAspect#isAggregate()}.
		 */
		@Name(EndAspect.AGGREGATE_PROPERTY)
		Boolean isAggregate();

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(PartConfig.MULTIPLE_PROPERTY)
		Boolean isMultiple();

		/**
		 * See {@link PartConfig#isBag()}.
		 */
		@Name(PartConfig.BAG_PROPERTY)
		Boolean isBag();

		/**
		 * See {@link PartConfig#isOrdered()}.
		 */
		@Name(PartConfig.ORDERED_PROPERTY)
		Boolean isOrdered();

		/**
		 * See {@link EndAspect#canNavigate()}.
		 */
		@Name(EndAspect.NAVIGATE_PROPERTY)
		Boolean canNavigate();
	}

	/**
	 * Creates a {@link UpdateTLAssociationEndProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLAssociationEndProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Update association end migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName endName = getConfig().getName();
		TypePart associationEnd;
		try {
			associationEnd = Util.getTLTypePartOrFail(connection, endName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find association end to update " + Util.qualifiedName(endName) + " at "
					+ getConfig().location(),
				Log.WARN);
			return;
		}

		QualifiedPartName newName = getConfig().getNewName();
		String newAssociationEndName;
		if (newName == null || endName.getPartName().equals(newName.getPartName())) {
			newAssociationEndName = null;
		} else {
			newAssociationEndName = newName.getPartName();
		}

		Util.updateTLStructuredTypePart(connection, associationEnd, null, null, newAssociationEndName,
			getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(), getConfig().isMultiple(),
			getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(),
			null, null);
	}

}
