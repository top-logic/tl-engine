/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Arrays;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.meta.kbbased.PersistentStructuredTypePart;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} changing the {@link TLTypePart#getType() target type} of all type
 * parts.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChangeAttributeTargetType extends AbstractConfiguredInstance<ChangeAttributeTargetType.Config>
		implements MigrationProcessor {

	/**
	 * Configuration of a {@link ChangeAttributeTargetType}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("change-part-type")
	public interface Config extends PolymorphicConfiguration<ChangeAttributeTargetType> {

		/**
		 * Qualified name of the source type.
		 */
		QualifiedTypeName getSource();

		/**
		 * Qualified name of the concrete part whose type must be changed.
		 * 
		 */
		QualifiedPartName getPart();

		/**
		 * Setter for {@link #getPart()}.
		 */
		void setPart(QualifiedPartName value);

		/**
		 * Qualified name of the target type.
		 */
		@Mandatory
		QualifiedTypeName getTarget();

		/**
		 * Setter for {@link #getTarget()}.
		 */
		void setTarget(QualifiedTypeName value);
	}

	/**
	 * Special configuration of a {@link ChangeAttributeTargetType} to change the target type of a
	 * {@link TLReference}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("change-reference-type")
	public interface ChangeRefConfig extends Config {

		/**
		 * Configuration name of {@link #getReference()}.
		 */
		String REFERENCE = "reference";

		/**
		 * Qualified name of the {@link TLReference}.
		 */
		@Mandatory
		@Name(REFERENCE)
		QualifiedPartName getReference();

		/**
		 * Setter for {@link #getReference()}.
		 */
		void setReference(QualifiedPartName value);

		/**
		 * For a concrete {@link TLReference}, no source is needed.
		 */
		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		@Hidden
		QualifiedTypeName getSource();

		/**
		 * Qualified name of the {@link TLAssociationEnd} whose type must be changed.
		 */
		@Override
		@Derived(fun = PartName.class, args = @Ref(REFERENCE))
		@Hidden
		QualifiedPartName getPart();

		/**
		 * Computation of the {@link ChangeRefConfig#getPart()} from
		 * {@link ChangeRefConfig#getReference()}. When the type of a {@link TLReference} must be
		 * changed, the type of the corresponding {@link TLAssociationEnd} must actually be changed.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class PartName extends Function1<QualifiedPartName, QualifiedPartName> {

			@Override
			public QualifiedPartName apply(QualifiedPartName referenceName) {
				if (referenceName == null) {
					return null;
				}
				QualifiedPartName endName = TypedConfiguration.newConfigItem(QualifiedPartName.class);
				String associationName =
					TLStructuredTypeColumns.syntheticAssociationName(referenceName.getTypeName(), referenceName.getPartName());
				endName.setName(TLModelUtil.qualifiedName(referenceName.getModuleName(), associationName)
					+ TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + referenceName.getPartName());
				return endName;
			}

		}

	}

	private Util _util;

	/**
	 * Creates a {@link ChangeAttributeTargetType} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeAttributeTargetType(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);

			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Migration failed with config " + getConfig(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws SQLException, MigrationException {
		QualifiedTypeName source = getConfig().getSource();
		if (source != null) {
			replaceAllPartTypeReferences(log, connection, source);
		} else if (getConfig().getPart() == null) {
			log.error("No source type or part given to replace type at " + getConfig().location());
		} else {
			replacePartTypeReference(log, connection, getConfig().getPart());
		}
	}

	private void replacePartTypeReference(Log log, PooledConnection connection, QualifiedPartName part)
			throws SQLException, MigrationException {
		TypePart sourcePart;
		try {
			sourcePart = _util.getTLTypePartOrFail(connection, part);
		} catch (MigrationException ex) {
			log.info("Unable to find source part " + _util.qualifiedName(part) + " at " + getConfig().location(),
				Log.WARN);
			return;
		}
		Type targetType;
		try {
			targetType = _util.getTLTypeOrFail(connection, getConfig().getTarget());
		} catch (MigrationException ex) {
			log.info(
				"Unable to find new target type '" + _util.qualifiedName(getConfig().getTarget()) + "' for source part "
					+ _util.qualifiedName(part) + " at " + getConfig().location(),
				Log.WARN);
			return;
		}
		if (sourcePart.getBranch() != targetType.getBranch()) {
			throw new MigrationException(
				"Different branches in source type " + sourcePart + " and target type "
					+ getConfig().getTarget() + ": " + sourcePart.getBranch() + " != " + targetType.getBranch());
		}
		
		_util.updateTLStructuredTypePart(connection, sourcePart, targetType, null, null, null, null, null, null, null,
			null, null, null, null, null);
		
		log.info(
			"Changed type of " + _util.qualifiedName(part) + " to " + _util.qualifiedName(getConfig().getTarget()));
	}

	private void replaceAllPartTypeReferences(Log log, PooledConnection connection, QualifiedTypeName source)
			throws SQLException, MigrationException {
		Type sourceType;
		try {
			sourceType = _util.getTLTypeOrFail(connection, source);
		} catch (MigrationException ex) {
			log.info("Unable to find source type " + _util.qualifiedName(source) + " at " + getConfig().location(),
				Log.WARN);
			return;
		}
		Type targetType = _util.getTLTypeOrFail(connection, getConfig().getTarget());
		if (sourceType.getBranch() != targetType.getBranch()) {
			throw new MigrationException(
				"Different branches in source type " + source + " and target type "
					+ getConfig().getTarget() + ": " + sourceType.getBranch() + " != " + targetType.getBranch());
		}
		
		CompiledStatement updateType = query(
		parameters(
			_util.branchParamDef(),
			parameterDef(DBType.STRING, "sourceType"),
			parameterDef(DBType.ID, "sourceID"),
			parameterDef(DBType.STRING, "targetType"),
			parameterDef(DBType.ID, "targetID")),
		update(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE), NO_TABLE_ALIAS),
			and(
				_util.eqBranch(),
				eqSQL(
					column(ReferencePart.type.getReferenceAspectColumnName(
						SQLH.mangleDBName(PersistentStructuredTypePart.TYPE_REF))),
					parameter(DBType.STRING, "sourceType")),
				eqSQL(
					column(ReferencePart.name.getReferenceAspectColumnName(
						SQLH.mangleDBName(PersistentStructuredTypePart.TYPE_REF))),
					parameter(DBType.ID, "sourceID"))),
			Arrays.asList(
				ReferencePart.type
					.getReferenceAspectColumnName(SQLH.mangleDBName(PersistentStructuredTypePart.TYPE_REF)),
				ReferencePart.name
					.getReferenceAspectColumnName(SQLH.mangleDBName(PersistentStructuredTypePart.TYPE_REF))),
			Arrays.asList(
				parameter(DBType.STRING, "targetType"),
				parameter(DBType.ID, "targetID")))).toSql(connection.getSQLDialect());

		int changed = updateType.executeUpdate(connection,
			sourceType.getBranch(),
			sourceType.getTable(), sourceType.getID(),
			targetType.getTable(), targetType.getID());

		log.info(
			"Changed types in " + changed + " rows from " + _util.qualifiedName(source) + " to "
				+ _util.qualifiedName(getConfig().getTarget()));
	}

}
