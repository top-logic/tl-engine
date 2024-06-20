/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;

/**
 * {@link MigrationProcessor} inlining composition links from a link table to the target table.
 * 
 * @see MoveCompositionLinks
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InlineCompositionLinks extends AbstractMoveCompositionLinks<InlineCompositionLinks.Config> {

	/**
	 * Configuration of {@link InlineCompositionLinks}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("inline-composition-links")
	public interface Config extends AbstractMoveCompositionLinks.Config<InlineCompositionLinks> {

		/**
		 * Name of the {@link MOReference} that contains the container element.
		 */
		@Mandatory
		String getContainer();

		/**
		 * Name of the {@link MOReference} that contains the composition reference.
		 */
		@Nullable
		String getContainerReference();

		/**
		 * Name of the {@link DBType#INT int} attribute containing the sort order of the element in
		 * its container.
		 */
		@Nullable
		String getContainerOrder();

	}

	private MORepository _allTypes;

	/**
	 * Creates a new {@link InlineCompositionLinks} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link InlineCompositionLinks}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public InlineCompositionLinks(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		_allTypes = context.getPersistentRepository();

		super.doMigration(context, log, connection);
	}

	@Override
	protected void migrateData(Log log, PooledConnection connection) throws SQLException, MigrationException {
		log.info("Inline composition links from '" + getConfig().getSourceTable() + "' for reference '"
				+ getConfig().getReference().getName() + "' into target tables.");
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

		List<SQLOrder> orders = new ArrayList<>();
		orders.add(order(false, column(destTypeColumn)));
		String branchColumn = util().branchColumnOrNull();
		if (branchColumn != null) {
			orders.add(order(false, column(branchColumn)));
		}
		orders.add(order(false, column(destIdColumn)));
		orders.add(order(false, column(BasicTypes.REV_MIN_DB_NAME)));
		CompiledStatement sourceLinksQ = query(
			parameters(
				util().branchParamDef(),
				parameterDef(DBType.ID, "refId"),
				parameterDef(DBType.STRING, "sourceType"),
				setParameterDef("sourceElements", DBType.ID)),
			select(
				Util.listWithoutNull(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(BasicTypes.REV_CREATE_DB_NAME),
					columnDef(srcIdColumn),
					columnDef(destIdColumn),
					columnDef(destTypeColumn),
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
						setParameter("sourceElements", DBType.ID))),
				orders)).toSql(sql);

		Map<String, List<Link>> linksDestType = new HashMap<>();
		try (ResultSet sourceLinks = sourceLinksQ.executeQuery(connection, branch, refId, sourceTable, sourceElements)) {
			while (sourceLinks.next()) {
				long id = sourceLinks.getLong(1);
				long revMin = sourceLinks.getLong(2);
				long revMax = sourceLinks.getLong(3);
				long revCreate = sourceLinks.getLong(4);
				long srcId = sourceLinks.getLong(5);
				long destId = sourceLinks.getLong(6);
				String destType = sourceLinks.getString(7);
				int sortOrder = sourceLinks.getInt(8);
				List<Link> links = linksDestType.get(destType);
				if (links == null) {
					links = new ArrayList<>();
					linksDestType.put(destType, links);
				}
				links.add(new Link(branch, id, revMin, revMax, revCreate, srcId, sourceTable, destId, destType, sortOrder));
			}
		}

		Map<String, Set<Long>> modifiedRevisions = new HashMap<>();
		for (Entry<String, List<Link>> entry : linksDestType.entrySet()) {
			String destTable = entry.getKey();
			List<Link> links = entry.getValue();
			Set<Long> newRevMins = new HashSet<>();
			modifiedRevisions.put(destTable, newRevMins);

			MOStructure dbType = (MOStructure) _allTypes.getMetaObject(destTable);
			MOReference containerRef = (MOReference) dbType.getAttribute(getConfig().getContainer());
			DBAttribute containerName = containerRef.getColumn(ReferencePart.name);
			DBAttribute containerType = containerRef.getColumn(ReferencePart.type);
			DBAttribute containerReference;
			if (getConfig().getContainerReference() != null) {
				containerReference = ((MOReference) dbType.getAttribute(getConfig().getContainerReference()))
					.getColumn(ReferencePart.name);
			} else {
				containerReference = null;
			}
			DBAttribute containerOrder;
			if (getConfig().getContainerOrder() != null) {
				containerOrder = (DBAttribute) dbType.getAttribute(getConfig().getContainerOrder());
			} else {
				containerOrder = null;
			}
			DBTableMetaObject dbMapping = dbType.getDBMapping();

			List<SQLColumnDefinition> columns = new ArrayList<>();
			for (DBAttribute dbAttr : dbMapping.getDBAttributes()) {
				columns.add(columnDef(dbAttr.getDBName(), NO_TABLE_ALIAS, dbAttr.getDBName()));
			}
			List<SQLOrder> targetTableorders = new ArrayList<>();
			if (branchColumn != null) {
				orders.add(order(false, column(branchColumn)));
			}
			targetTableorders.add(order(false, column(BasicTypes.IDENTIFIER_DB_NAME)));
			targetTableorders.add(order(false, column(BasicTypes.REV_MIN_DB_NAME)));

			CompiledStatement destObjectsQ = query(
				parameters(
					util().branchParamDef(),
					setParameterDef("elements", DBType.ID)),
				select(
					columns,
					table(dbMapping.getDBName()),
					and(
						util().eqBranch(),
						inSet(
							column(BasicTypes.IDENTIFIER_DB_NAME),
							setParameter("elements", DBType.ID))),
					targetTableorders)).toSql(sql);
			destObjectsQ.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			List<NewRow> newRows = new ArrayList<>();
			try (ResultSet rows = destObjectsQ.executeQuery(connection, branch, allDestIds(links))) {
				int i = 0;
				while (rows.next()) {
					boolean rowUpdated = false;
					long rowMin = rows.getLong(BasicTypes.REV_MIN_DB_NAME);
					long rowMax = rows.getLong(BasicTypes.REV_MAX_DB_NAME);
					assert rowMin <= rowMax;

					while (i < links.size()) {
						Link currentLink = links.get(i);
						long linkDestID = currentLink.getDestId();
						long elementID = rows.getLong(BasicTypes.IDENTIFIER_DB_NAME);
						if (elementID != linkDestID) {
							// Element is not target of the link
							break;
						}
						long linkMin = currentLink.getRevMin();
						long linkMax = currentLink.getRevMax();
						assert linkMin <= linkMax;

						if (rowMax < linkMin) {
							/* link was created after lifetime of row */
							break;
						}
						if (linkMin <= rowMin) {
							if (linkMax < rowMin) {
								/* link was dropped before lifetime of row; try next link */
								i++;
								continue;
							} else if (linkMax < rowMax) {
								/* link is valid in [rowMin,linkMax] */
								NewRow newRow = newRowCopy(rows, dbMapping, rowMin, linkMax);
								copyLinkDataToNewRow(newRow, currentLink, refId, containerType, containerName,
									containerReference, containerOrder);
								newRows.add(newRow);
								rows.updateLong(BasicTypes.REV_MIN_DB_NAME, linkMax + 1);
								rowMin = linkMax + 1;
								newRevMins.add(linkMax + 1);
								rowUpdated = true;
								i++;
								continue;
							} else {
								/* Link is valid for the whole row lifetime. */
								copyLinkDataToRow(rows, currentLink, refId, containerType, containerName,
									containerReference, containerOrder);
								rowUpdated = true;
								if (linkMax == rowMax) {
									i++;
								}
								break;
							}
						} else if (linkMin <= rowMax) {
							/* rowMin < linkMin <= rowMax : create new row with data from row before
							 * linkMin */
							newRows.add(newRowCopy(rows, dbMapping, rowMin, linkMin - 1));

							if (linkMax < rowMax) {
								NewRow newRow = newRowCopy(rows, dbMapping, linkMax, linkMax);
								copyLinkDataToNewRow(newRow, currentLink, refId, containerType, containerName,
									containerReference, containerOrder);
								newRows.add(newRow);
								i++;
								rows.updateLong(BasicTypes.REV_MIN_DB_NAME, linkMax + 1);
								rowMin = linkMax + 1;
								rowUpdated = true;
								newRevMins.add(linkMax + 1);
								continue;
							} else {
								/* rowMax<=linkMax */
								copyLinkDataToRow(rows, currentLink, refId, containerType, containerName,
									containerReference, containerOrder);
								rows.updateLong(BasicTypes.REV_MIN_DB_NAME, linkMin);
								rowMin = linkMin;
								rowUpdated = true;
								newRevMins.add(linkMin);
								if (rowMax == linkMax) {
									i++;
								}
								break;
							}
						} else {
							break;
						}
					}
					if (rowUpdated) {
						rows.updateRow();
					}
				}
				for (int k = 0; k < newRows.size(); k++) {
					rows.moveToInsertRow();
					NewRow newRow = newRows.get(k);
					addData(rows, newRow);
					rows.insertRow();
				}
			}

		}
		updateXRefTable(connection, sql, branch, modifiedRevisions);
		
		deleteLinks(connection, branch, refId, sourceTable, sourceElements);
	}

	private Set<Object> allDestIds(List<Link> links) {
		return links.stream().map(Link::getDestId).distinct().map(LongID::valueOf).collect(Collectors.toSet());
	}

	private void addData(ResultSet rows, NewRow newRow) throws SQLException {
		NewRowImpl values = (NewRowImpl) newRow;
		for (Entry<String, ?> entry : values.entrySet()) {
			rows.updateObject(entry.getKey(), entry.getValue());
		}
	}

	private void copyLinkDataToRow(ResultSet rows, Link link, TLID refId, DBAttribute containerType,
			DBAttribute containerName, DBAttribute containerReference, DBAttribute containerOrder) throws SQLException {
		if (link == null) {
			if (containerType != null) {
				rows.updateNull(containerType.getDBName());
			}
			rows.updateLong(containerName.getDBName(), nullForMandatoryIDColumn());
			if (containerReference != null) {
				rows.updateLong(containerReference.getDBName(), nullForMandatoryIDColumn());
			}
			if (containerOrder != null) {
				rows.updateNull(containerOrder.getDBName());
			}
		} else {
			if (containerType != null) {
				rows.updateString(containerType.getDBName(), link.getSrcType());
			}
			rows.updateLong(containerName.getDBName(), link.getSrcId());
			if (containerReference != null) {
				rows.updateLong(containerReference.getDBName(), ((LongID) refId).longValue());
			}
			if (containerOrder != null) {
				rows.updateInt(containerOrder.getDBName(), link.getSortOrder());
			}
		}
	}

	private long nullForMandatoryIDColumn() {
		return ((LongID) IdentifierUtil.nullIdForMandatoryDatabaseColumns()).longValue();
	}

	private void copyLinkDataToNewRow(NewRow newRow, Link link, TLID refId, DBAttribute containerType,
			DBAttribute containerName, DBAttribute containerReference, DBAttribute containerOrder) {
		NewRowImpl values = (NewRowImpl) newRow;
		if (link == null) {
			if (containerType != null) {
				values.remove(containerType.getDBName());
			}
			values.remove(containerName.getDBName());
			if (containerReference != null) {
				values.remove(containerReference.getDBName());
			}
			if (containerOrder != null) {
				values.remove(containerOrder.getDBName());
			}
		} else {
			if (containerType != null) {
				values.put(containerType.getDBName(), link.getSrcType());
			}
			values.put(containerName.getDBName(), link.getSrcId());
			if (containerReference != null) {
				values.put(containerReference.getDBName(), ((LongID) refId).longValue());
			}
			if (containerOrder != null) {
				values.put(containerOrder.getDBName(), link.getSortOrder());
			}
		}

	}

	private NewRow newRowCopy(ResultSet result, DBTableMetaObject table, long revMin, long revMax) throws SQLException {
		NewRowImpl newRow = new NewRowImpl();
		for (DBAttribute attribute : table.getDBAttributes()) {
			String attrName = attribute.getDBName();
			Object value;
			if (BasicTypes.REV_MIN_DB_NAME.equals(attrName)) {
				value = revMin;
			} else if (BasicTypes.REV_MAX_DB_NAME.equals(attrName)) {
				value = revMax;
			} else {
				value = result.getObject(attrName);
			}
			newRow.put(attrName, value);
		}

		return newRow;
	}

	/**
	 * Mark the target table as "touched" for all revisions in which links are modified. This is
	 * necessary to have correct KB dump in a follow-up replay migration.
	 */
	private void updateXRefTable(PooledConnection connection, DBHelper sql, long branch,
			Map<String, Set<Long>> newRevMinsByType) throws SQLException {
		for (Iterator<Set<Long>> revs = newRevMinsByType.values().iterator(); revs.hasNext();) {
			if (revs.next().isEmpty()) {
				// No potential new revMin for table; ignore entry.
				revs.remove();
			}
		}
		if (newRevMinsByType.isEmpty()) {
			// No potential new revMin at all.
			return;
		}

		/* Remove revisions already contained in RevisionXRef table. This is necessary to avoid
		 * "Duplicate-Key" failure, during insert. */
		// Note: in RevisionXRef there is always a "branch" column
		CompiledStatement existingRevsInXRef = query(
			parameters(
				parameterDef(DBType.LONG, "branch"),
				setParameterDef("table", DBType.STRING)),
			selectDistinct(
				columns(
					columnDef(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
					columnDef(SQLH.mangleDBName(RevisionXref.XREF_REV_ATTRIBUTE))),
				table(SQLH.mangleDBName(RevisionXref.REVISION_XREF_TYPE_NAME)),
				and(
					eqSQL(
						column(SQLH.mangleDBName(RevisionXref.XREF_BRANCH_ATTRIBUTE)),
						parameter(DBType.LONG, "branch")),
					inSet(
						column(SQLH.mangleDBName(RevisionXref.XREF_TYPE_ATTRIBUTE)),
						setParameter("table", DBType.STRING))))).toSql(sql);
		try (ResultSet result = existingRevsInXRef.executeQuery(connection, branch, newRevMinsByType.keySet())) {
			while (result.next()) {
				newRevMinsByType.get(result.getString(1)).remove(result.getLong(2));
			}
		}

		for (Iterator<Set<Long>> revs = newRevMinsByType.values().iterator(); revs.hasNext();) {
			if (revs.next().isEmpty()) {
				// No new revMin for table; ignore entry.
				revs.remove();
			}
		}
		if (newRevMinsByType.isEmpty()) {
			// No new revMin at all.
			return;
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
		for (Entry<String, Set<Long>> entry : newRevMinsByType.entrySet()) {
			String table = entry.getKey();
			for (Long revMin : entry.getValue()) {
				insertXRefs.executeUpdate(connection, branch, revMin, table);
			}
		}
	}

	private interface NewRow {
		// Marker interface
	}

	private static class NewRowImpl extends HashMap<String, Object> implements NewRow {
		// No additional here
	}

	private static class Link {
		private long _branch;

		private long _id;

		private long _revMin;

		private long _revMax;

		private long _revCreate;

		private long _srcId;

		private String _srcType;

		private long _destId;

		private String _destType;

		private int _sortOrder;

		Link(long branch, long id, long revMin, long revMax, long revCreate, long srcId, String srcType,
				long destId, String destType, int sortOrder) {
			_branch = branch;
			_id = id;
			_revMin = revMin;
			_revMax = revMax;
			_revCreate = revCreate;
			_srcId = srcId;
			_srcType = srcType;
			_destId = destId;
			_destType = destType;
			_sortOrder = sortOrder;
		}

		long getBranch() {
			return _branch;
		}

		void setBranch(long branch) {
			_branch = branch;
		}

		long getId() {
			return _id;
		}

		void setId(long id) {
			_id = id;
		}

		long getRevMin() {
			return _revMin;
		}

		void setRevMin(long revMin) {
			_revMin = revMin;
		}

		long getRevMax() {
			return _revMax;
		}

		void setRevMax(long revMax) {
			_revMax = revMax;
		}

		long getRevCreate() {
			return _revCreate;
		}

		void setRevCreate(long revCreate) {
			_revCreate = revCreate;
		}

		long getSrcId() {
			return _srcId;
		}

		void setSrcId(long srcId) {
			_srcId = srcId;
		}

		String getSrcType() {
			return _srcType;
		}

		void setSrcType(String srcType) {
			_srcType = srcType;
		}

		long getDestId() {
			return _destId;
		}

		void setDestId(long destId) {
			_destId = destId;
		}

		String getDestType() {
			return _destType;
		}

		void setDestType(String destType) {
			_destType = destType;
		}

		int getSortOrder() {
			return _sortOrder;
		}

		void setSortOrder(int sortOrder) {
			_sortOrder = sortOrder;
		}
	}


}
