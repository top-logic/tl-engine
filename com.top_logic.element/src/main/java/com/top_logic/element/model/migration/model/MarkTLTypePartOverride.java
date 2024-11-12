/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Reference;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} marks a {@link TLStructuredTypePart} as override of another part.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MarkTLTypePartOverride extends TLModelBaseLineMigrationProcessor<MarkTLTypePartOverride.Config> {

	/**
	 * Configuration options of {@link MarkTLTypePartOverride}.
	 */
	@TagName("mark-override")
	public interface Config extends TLModelBaseLineMigrationProcessor.Config<MarkTLTypePartOverride> {

		/**
		 * Qualified name of the overriding part.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

		/**
		 * Qualified name of the definition of the overriding part, i.e. the top level part in the
		 * override chain.
		 */
		@Mandatory
		QualifiedPartName getDefinition();

		/**
		 * Setter for {@link #getDefinition()}.
		 */
		void setDefinition(QualifiedPartName value);

	}

	private Util _util;

	/**
	 * Creates a {@link MarkTLTypePartOverride} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MarkTLTypePartOverride(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			internalDoMigration(log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error(
				"Marking " + _util.qualifiedName(getConfig().getName()) + " as override of "
					+ _util.qualifiedName(getConfig().getDefinition()) + " failed at " + getConfig().location(),
				ex);
			return false;
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedPartName definition = (getConfig().getDefinition());

		TypePart part = _util.getTLTypePartOrFail(connection, partName);
		TypePart definitionPart = _util.getTLTypePartOrFail(connection, definition);
		CompiledStatement sql = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "partID"),
				parameterDef(DBType.ID, "definitionID")),
			update(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					_util.eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "partID"))),
				Arrays.asList(Util.refID(KBBasedMetaAttribute.DEFINITION_REF)),
				Arrays.asList(parameter(DBType.ID, "definitionID")))).toSql(connection.getSQLDialect());

		sql.executeUpdate(connection, part.getBranch(), part.getID(), definitionPart.getID());

		copyProperties(log, connection, partName, definition, part, definitionPart);

		if (tlModel != null) {
			MigrationUtils.setOverride(log, tlModel, partName, true);
		}

		log.info("Mark " + _util.qualifiedName(partName) + " as override of " + _util.qualifiedName(definition));
	}

	private void copyProperties(Log log, PooledConnection connection, QualifiedPartName partName,
			QualifiedPartName definition, TypePart part, TypePart definitionPart) throws SQLException {
		switch (part.getKind()) {
			case ASSOCIATION_END:
				copyEndValues(log, connection, part.getBranch(), definitionPart.getID(), part.getID(), partName,
					definition);
				return;
			case REFERENCE:
				// Values are stored at association end
				copyEndValues(log, connection, part.getBranch(), ((Reference) definitionPart).getEndID(),
					((Reference) part).getEndID(), partName, definition);
				return;
			case CLASSIFIER:
				throw new IllegalArgumentException("No override for classifier");
			case CLASS_PROPERTY:
			case ASSOCIATION_PROPERTY:
				copyPropertyValues(log, connection, part.getBranch(), definitionPart.getID(), part.getID(), partName,
					definition);
				return;
		}
		throw new UnreachableAssertion("uncovered case: " + part.getKind());
	}

	private void copyEndValues(Log log, PooledConnection connection, long branch, TLID sourceID, TLID destId,
			QualifiedPartName partName, QualifiedPartName definition) throws SQLException {
		boolean withHistoryColumn = _util.hasHistoryColumn();
		boolean withDeletionColumn = _util.hasDeletionColumn();
		CompiledStatement getValuesQuery = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "partID")),
			select(
				Util.listWithoutNull(
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.BAG_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.MULTIPLE_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.ORDERED_ATTR)),
					columnDef(SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR)),
					columnDef(SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR)),
					columnDef(SQLH.mangleDBName(TLAssociationEnd.NAVIGATE_ATTR)),
					withHistoryColumn ? columnDef(SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR)) : null,
					withDeletionColumn ? columnDef(SQLH.mangleDBName(TLAssociationEnd.DELETION_POLICY_ATTR)) : null),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					_util.eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "partID"))
					),
				orders(order(true, column(BasicTypes.REV_MAX_DB_NAME)))))
					.toSql(connection.getSQLDialect());
		boolean bag = false;
		boolean mandatory = false;
		boolean multiple = false;
		boolean ordered = false;
		boolean aggregate = false;
		boolean composite = false;
		boolean navigate = false;

		String historyType = null;
		String deletionPolicy = null;

		int index = 8;
		int historyIndex = withHistoryColumn ? index++ : 0;
		int deletionIndex = withDeletionColumn ? index++ : 0;

		try (ResultSet endValues =
			getValuesQuery.executeQuery(connection, branch, sourceID)) {
			if (endValues.next()) {
				bag = endValues.getBoolean(1);
				mandatory = endValues.getBoolean(2);
				multiple = endValues.getBoolean(3);
				ordered = endValues.getBoolean(4);
				aggregate = endValues.getBoolean(5);
				composite = endValues.getBoolean(6);
				navigate = endValues.getBoolean(7);
				if (withHistoryColumn) {
					historyType = endValues.getString(historyIndex);
				}
				if (withDeletionColumn) {
					deletionPolicy = endValues.getString(deletionIndex);
				}
			} else {
				log.error("Unable to get values to copy from definition '" + definition.getName() + "' to override '"
						+ partName.getName() + "'.");
				return;
			}
		}

		CompiledStatement updateValuesQuery = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "partID"),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.BAG_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.MANDATORY_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.MULTIPLE_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.ORDERED_ATTR),
				parameterDef(DBType.BOOLEAN, TLAssociationEnd.AGGREGATE_ATTR),
				parameterDef(DBType.BOOLEAN, TLAssociationEnd.COMPOSITE_ATTR),
				parameterDef(DBType.BOOLEAN, TLAssociationEnd.NAVIGATE_ATTR),
				parameterDef(DBType.STRING, TLAssociationEnd.HISTORY_TYPE_ATTR),
				parameterDef(DBType.STRING, TLAssociationEnd.DELETION_POLICY_ATTR)),
			update(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					_util.eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "partID"))),
				Util.listWithoutNull(
					SQLH.mangleDBName(TLStructuredTypePart.BAG_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.MULTIPLE_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.ORDERED_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.NAVIGATE_ATTR),
					withHistoryColumn ? SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR) : null,
					withDeletionColumn ? SQLH.mangleDBName(TLAssociationEnd.DELETION_POLICY_ATTR) : null
				),
				Util.listWithoutNull(
					parameter(DBType.BOOLEAN, TLStructuredTypePart.BAG_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.MANDATORY_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.MULTIPLE_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.ORDERED_ATTR),
					parameter(DBType.BOOLEAN, TLAssociationEnd.AGGREGATE_ATTR),
					parameter(DBType.BOOLEAN, TLAssociationEnd.COMPOSITE_ATTR),
					parameter(DBType.BOOLEAN, TLAssociationEnd.NAVIGATE_ATTR),
					withHistoryColumn ? parameter(DBType.STRING, TLAssociationEnd.HISTORY_TYPE_ATTR) : null,
					withDeletionColumn ? parameter(DBType.STRING, TLAssociationEnd.DELETION_POLICY_ATTR) : null
				))).toSql(connection.getSQLDialect());
		updateValuesQuery.executeUpdate(connection, branch, destId, bag, mandatory, multiple, ordered, aggregate,
			composite, navigate, historyType, deletionPolicy);
	}

	private void copyPropertyValues(Log log, PooledConnection connection, long branch, TLID sourceID, TLID destId,
			QualifiedPartName partName, QualifiedPartName definition) throws SQLException {
		CompiledStatement getValuesQuery = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "partID")),
			select(
				columns(
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.BAG_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.MULTIPLE_ATTR)),
					columnDef(SQLH.mangleDBName(TLStructuredTypePart.ORDERED_ATTR))),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					_util.eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "partID"))),
				orders(order(true, column(BasicTypes.REV_MAX_DB_NAME)))))
					.toSql(connection.getSQLDialect());
		boolean bag = false;
		boolean mandatory = false;
		boolean multiple = false;
		boolean ordered = false;
		try (ResultSet endValues =
			getValuesQuery.executeQuery(connection, branch, sourceID)) {
			if (endValues.next()) {
				bag = endValues.getBoolean(1);
				mandatory = endValues.getBoolean(2);
				multiple = endValues.getBoolean(3);
				ordered = endValues.getBoolean(4);
			} else {
				log.error("Unable to get values to copy from definition '" + definition.getName() + "' to override '"
						+ partName.getName() + "'.");
				return;
			}
		}
		CompiledStatement updateValuesQuery = query(
			parameters(
				_util.branchParamDef(),
				parameterDef(DBType.ID, "partID"),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.BAG_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.MANDATORY_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.MULTIPLE_ATTR),
				parameterDef(DBType.BOOLEAN, TLStructuredTypePart.ORDERED_ATTR)),
			update(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					_util.eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "partID"))),
				Arrays.asList(
					SQLH.mangleDBName(TLStructuredTypePart.BAG_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.MULTIPLE_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.ORDERED_ATTR)),
				Arrays.asList(
					parameter(DBType.BOOLEAN, TLStructuredTypePart.BAG_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.MANDATORY_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.MULTIPLE_ATTR),
					parameter(DBType.BOOLEAN, TLStructuredTypePart.ORDERED_ATTR))))
						.toSql(connection.getSQLDialect());
		updateValuesQuery.executeUpdate(connection, branch, destId, bag, mandatory, multiple, ordered);
	}

}
