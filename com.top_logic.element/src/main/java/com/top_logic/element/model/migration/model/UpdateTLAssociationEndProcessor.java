/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.model.migration.model.UpdateTLPropertyProcessor.UpdateTypePartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} updating a {@link TLAssociationEnd}.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class UpdateTLAssociationEndProcessor extends AbstractConfiguredInstance<UpdateTLAssociationEndProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLAssociationEndProcessor}.
	 */
	@TagName("update-association-end")
	public interface Config extends PolymorphicConfiguration<UpdateTLAssociationEndProcessor>, UpdateEndAspectConfig {

		// sum interface

	}

	/**
	 * {@link UpdateTypePartConfig} for association end aspects.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface UpdateEndAspectConfig extends UpdateTypePartConfig {

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
		 * See {@link EndAspect#canNavigate()}.
		 */
		@Name(EndAspect.NAVIGATE_PROPERTY)
		Boolean canNavigate();

		/**
		 * See {@link EndAspect#getHistoryType()}.
		 */
		@Name(EndAspect.HISTORY_TYPE_PROPERTY)
		@NullDefault
		@Nullable
		@Label("Historization")
		HistoryType getHistoryType();
	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();

			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Update association end migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName endName = getConfig().getName();
		TypePart associationEnd;
		try {
			associationEnd = _util.getTLTypePartOrFail(connection, endName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find association end to update " + _util.qualifiedName(endName) + " at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}

		QualifiedPartName newName = getConfig().getNewName();
		String newAssociationEndName;
		if (newName == null || endName.getPartName().equals(newName.getPartName())) {
			newAssociationEndName = null;
		} else {
			newAssociationEndName = newName.getPartName();
		}

		_util.updateTLStructuredTypePart(connection, associationEnd, null, null, newAssociationEndName,
			getConfig().isMandatory(), getConfig().isAbstract(), getConfig().isComposite(), getConfig().isAggregate(),
			getConfig().isMultiple(),
			getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(), getConfig().getHistoryType(),
			null, null);

		boolean updateModelBaseline;
		if (tlModel == null || TLStructuredTypeColumns.isSyntheticAssociationName(endName.getTypeName())) {
			/* AssociationEnd is an end of an internal TLAssociation which is not defined in the
			 * model baseline. */
			updateModelBaseline = false;
		} else {
			MigrationUtils.updateAssociationEnd(log, tlModel, endName, newName, null,
				getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
				getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(), getConfig().isAbstract(),
				getConfig().canNavigate(), getConfig().getHistoryType(),
				getConfig());
			updateModelBaseline = true;
		}
		return updateModelBaseline;
	}

}
