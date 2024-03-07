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
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.basic.func.misc.AlwaysTrue;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Reference;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} updating a {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLReferenceProcessor extends AbstractConfiguredInstance<UpdateTLReferenceProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLReferenceProcessor}.
	 */
	@TagName("update-reference")
	public interface Config extends PolymorphicConfiguration<UpdateTLReferenceProcessor>,
			UpdateTLAssociationEndProcessor.UpdateEndAspectConfig,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Qualified name of the target type.
		 * 
		 * @see PartConfig#getTypeSpec()
		 */
		QualifiedTypeName getNewType();

		/**
		 * Whether the reference to update is an inverse reference.
		 */
		@Hidden
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isInverse();

		/**
		 * Name of the new reference association end.
		 */
		QualifiedPartName getNewEnd();
	}

	/**
	 * Configuration to update an inverse reference. In this case no update of type and owner are
	 * allowed, because they must be derived by the original reference.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("update-inverse-reference")
	public interface InverseConfig extends Config {

		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		QualifiedTypeName getNewType();

		@Override
		@Derived(fun = AlwaysTrue.class, args = {})
		boolean isInverse();
	}

	private Util _util;

	/**
	 * Creates a {@link UpdateTLReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLReferenceProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Updating reference migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName referenceName = getConfig().getName();
		Reference reference;
		try {
			reference = (Reference) _util.getTLTypePartOrFail(connection, referenceName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find reference to update " + _util.qualifiedName(referenceName) + " at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}
		Type newType;
		if (getConfig().getNewType() == null) {
			newType = null;
		} else {
			newType = _util.getTLTypeOrFail(connection, getConfig().getNewType());
		}

		TypePart newEnd;
		if (getConfig().getNewEnd() == null) {
			newEnd = null;
		} else {
			newEnd = _util.getTLTypePartOrFail(connection, getConfig().getNewEnd());
		}

		QualifiedPartName newName = getConfig().getNewName();
		String newReferenceName;
		if (newName == null || referenceName.getPartName().equals(newName.getPartName())) {
			newReferenceName = null;
		} else {
			newReferenceName = newName.getPartName();
		}
		boolean updateModelBaseline;
		if (!getConfig().isInverse()) {
			Type newOwner;
			if (newName == null || (referenceName.getModuleName().equals(newName.getModuleName())
				&& referenceName.getTypeName().equals(newName.getTypeName()))) {
				newOwner = null;
			} else {
				newOwner = _util.getTLTypeOrFail(connection, newName);
			}
			_util.updateTLReference(connection,
				reference, newType, newOwner,
				newReferenceName, getConfig().isMandatory(),
				getConfig().isComposite(), getConfig().isAggregate(), getConfig().isMultiple(),
				getConfig().isBag(),
				getConfig().isOrdered(), getConfig().canNavigate(), getConfig().getHistoryType(), getConfig(), newEnd);
			if (getConfig().isSkipModelBaselineChange()) {
				updateModelBaseline = false;
			} else {
				MigrationUtils.updateReference(log, tlModel,
					referenceName, getConfig().getNewName(), getConfig().getNewType(),
					getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
					getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(),
					getConfig().getHistoryType(), getConfig(), getConfig().getNewEnd());
				updateModelBaseline = true;
			}
			log.info("Updated reference " + _util.qualifiedName(referenceName));
		} else {
			_util.updateInverseReference(connection,
				reference,
				newReferenceName, getConfig().isMandatory(),
				getConfig().isComposite(), getConfig().isAggregate(), getConfig().isMultiple(),
				getConfig().isBag(),
				getConfig().isOrdered(), getConfig().canNavigate(), getConfig().getHistoryType(), getConfig(), newEnd);
			if (getConfig().isSkipModelBaselineChange()) {
				updateModelBaseline = false;
			} else {
				MigrationUtils.updateInverseReference(log, tlModel,
					referenceName, newReferenceName,
					getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
					getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(),
					getConfig().getHistoryType(), getConfig(), getConfig().getNewEnd());
				updateModelBaseline = true;
			}
			log.info("Updated inverse reference " + _util.qualifiedName(referenceName));
		}
		return updateModelBaseline;

	}

}
