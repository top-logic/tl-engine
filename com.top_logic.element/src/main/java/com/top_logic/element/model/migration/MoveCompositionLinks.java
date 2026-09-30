/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;

/**
 * {@link MigrationProcessor} moving composition links from a source to a target table.
 * 
 * @see InlineCompositionLinks
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MoveCompositionLinks extends AbstractMoveCompositionLinks<MoveCompositionLinks.Config> {

	/**
	 * Configuration of {@link MoveCompositionLinks}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("move-composition-links")
	public interface Config extends AbstractMoveCompositionLinks.Config<MoveCompositionLinks> {

		/**
		 * Name of the {@link MOClass} to which the links must be moved.
		 */
		@Mandatory
		String getTargetTable();

	}

	/**
	 * Creates a new {@link MoveCompositionLinks} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MoveCompositionLinks}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public MoveCompositionLinks(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void migrateData(Log log, PooledConnection connection) throws SQLException, MigrationException {
		log.info("Move composition links from '" + getConfig().getSourceTable() + "' for reference '"
				+ getConfig().getReference().getName() + "' to '" + getConfig().getTargetTable() + "'.");
		super.migrateData(log, connection);
	}

	@Override
	protected void moveLinks(Log log, PooledConnection connection, long branch, TLID refId, String sourceTable,
			Set<TLID> sourceElements) throws SQLException {
		DBHelper sql = connection.getSQLDialect();

		String srcIdColumn = Util.refID(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		String srcTypeColumn = Util.refType(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		String destIdColumn = Util.refID(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		String destTypeColumn = Util.refType(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		String attrIdColumn = Util.refID(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);

		/* Copy links from source to target table. */
		CompiledStatement copyLinks = query(
			parameters(
				util().branchParamDef(),
				parameterDef(DBType.ID, "refId"),
				parameterDef(DBType.STRING, "sourceType"),
				setParameterDef("sourceElements", DBType.ID)),
			insert(
				table(SQLH.mangleDBName(getConfig().getTargetTable())),
				Util.listWithoutNull(
					util().branchColumnOrNull(),
					BasicTypes.IDENTIFIER_DB_NAME,
					BasicTypes.REV_MIN_DB_NAME,
					BasicTypes.REV_MAX_DB_NAME,
					BasicTypes.REV_CREATE_DB_NAME,
					srcIdColumn,
					srcTypeColumn,
					destIdColumn,
					destTypeColumn,
					attrIdColumn,
					SQLH.mangleDBName(LinkStorage.SORT_ORDER)),
				select(
					Util.listWithoutNull(
						util().branchColumnDefOrNull(),
						columnDef(BasicTypes.IDENTIFIER_DB_NAME),
						columnDef(BasicTypes.REV_MIN_DB_NAME),
						columnDef(BasicTypes.REV_MAX_DB_NAME),
						columnDef(BasicTypes.REV_CREATE_DB_NAME),
						columnDef(srcIdColumn),
						columnDef(srcTypeColumn),
						columnDef(destIdColumn),
						columnDef(destTypeColumn),
						columnDef(attrIdColumn),
						columnDef(SQLH.mangleDBName(LinkStorage.SORT_ORDER))),
					table(SQLH.mangleDBName(getConfig().getSourceTable())),
					and(
						util().eqBranch(),
						eqSQL(
							column(attrIdColumn),
							parameter(DBType.ID, "refId")),
						eqSQL(
							column(srcTypeColumn),
							parameter(DBType.STRING, "sourceType")),
						inSet(
							column(srcIdColumn),
							setParameter("sourceElements", DBType.ID)))))).toSql(sql);


		int moved = copyLinks.executeUpdate(connection, branch, refId, sourceTable, sourceElements);
		if (moved == 0) {
			return;
		}
		updateXRefTable(connection, sql, branch, refId, sourceTable, sourceElements, srcIdColumn, srcTypeColumn,
			attrIdColumn);

		int deleted = deleteLinks(connection, branch, refId, sourceTable, sourceElements);

		if (moved != deleted) {
			log.info(
				moved + " links moved from '" + getConfig().getSourceTable() + "' to '" + getConfig().getTargetTable()
						+ "', but " + deleted + " links deleted in '" + getConfig().getSourceTable() + "'!",
				Protocol.WARN);
		}
		log.info(moved + " links moved from '" + getConfig().getSourceTable() + "' to '" + getConfig().getTargetTable()
				+ "' for composition reference " + getConfig().getReference().getName() + ".");
	}

	/**
	 * Mark the target table as "touched" for all revisions in which links are modified. This is
	 * necessary to have correct KB dump in a follow-up replay migration.
	 */
	private void updateXRefTable(PooledConnection connection, DBHelper sql, long branch, TLID refId, String sourceType,
			Set<TLID> sourceIds, String srcIdColumn, String srcTypeColumn, String attrIdColumn) throws SQLException {
		/* Get all revisions in which a moved link was modified, resp. created. */
		CompiledStatement modificationRevs = query(
			parameters(
				util().branchParamDef(),
				parameterDef(DBType.ID, "refId"),
				parameterDef(DBType.STRING, "sourceType"),
				setParameterDef("sourceElements", DBType.ID)),
			selectDistinct(
				columns(
					columnDef(BasicTypes.REV_MIN_DB_NAME)),
				table(SQLH.mangleDBName(getConfig().getSourceTable())),
				and(
					util().eqBranch(),
					eqSQL(
						column(attrIdColumn),
						parameter(DBType.ID, "refId")),
					eqSQL(
						column(srcTypeColumn),
						parameter(DBType.STRING, "sourceType")),
					inSet(
						column(srcIdColumn),
						setParameter("sourceElements", DBType.ID))))).toSql(sql);
		Set<Long> revisions = new HashSet<>();
		try (ResultSet result = modificationRevs.executeQuery(connection, branch, refId, sourceType, sourceIds)) {
			while (result.next()) {
				revisions.add(result.getLong(1));
			}
		}

		/* Remove revisions already contained in RevisionXRef table. This is necessary to avoid
		 * "Duplicate-Key" failure, during insert. */
		// Note: in RevisionXRef there is always a "branch" column
		CompiledStatement existingRevsInXRef = query(
			parameters(
				parameterDef(DBType.LONG, "branch"),
				parameterDef(DBType.STRING, "table")),
			selectDistinct(
				columns(
					columnDef(SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE))),
				table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
				and(
					eqSQL(
						column(SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE)),
						parameter(DBType.LONG, "branch")),
					eqSQL(
						column(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
						parameter(DBType.STRING, "table"))))).toSql(sql);
		try (ResultSet result = existingRevsInXRef.executeQuery(connection, branch, getConfig().getTargetTable())) {
			while (result.next()) {
				revisions.remove(result.getLong(1));
			}
		}

		/* Mark target table as touched in all revisions in which a link was touched. */
		CompiledStatement insertXRefs = query(
			parameters(
				parameterDef(DBType.LONG, "branch"),
				parameterDef(DBType.LONG, "rev"),
				parameterDef(DBType.STRING, "table")),
			insert(
				table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
				Arrays.asList(
					SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE),
					SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE),
					SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
				Arrays.asList(
					parameter(DBType.LONG, "rev"),
					parameter(DBType.LONG, "branch"),
					parameter(DBType.STRING, "table")))).toSql(sql);
		String targetTable = getConfig().getTargetTable();
		for (Long rev : revisions) {
			insertXRefs.executeUpdate(connection, branch, rev, targetTable);
		}
	}
}
