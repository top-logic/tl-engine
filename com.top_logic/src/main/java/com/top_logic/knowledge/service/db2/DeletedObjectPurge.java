/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;

/**
 * Removes deleted objects from a {@link DBKnowledgeBase} as if they had never existed.
 *
 * <p>
 * The operation starts at a set of seed objects and extends it to the hull that can be removed
 * without leaving a dangling value behind: every object that holds a mandatory reference to a member
 * of the hull in any of its versions belongs to the hull (in particular every association link that
 * touches a member), and so does every object a member contained through a container reference and
 * that was deleted together with it. All rows of all members, in all revisions, are erased from the
 * item tables and from the table of dynamic values. A row that survives and whose optional reference
 * points to a member is rewritten to the value of a reference without a target, so that reading such
 * an attribute answers nothing instead of failing. The revision, cross reference and branch tables
 * are untouched: the history itself stays as it is, only the removed objects are missing from it.
 * </p>
 *
 * <p>
 * Every member of the hull must be deleted. An object that is still alive blocks the operation and
 * is reported with the reference that pulled it into the hull. An optional reference from a living
 * object is no obstacle, because it is cleared.
 * </p>
 *
 * <p>
 * {@link #analyze(Collection, Log) Analyzing} a set of seeds computes the hull, checks that
 * precondition and counts the rows that would be erased and the reference values that would be
 * cleared, without changing anything.
 * </p>
 *
 * <p>
 * The operation works with plain SQL on the connection pool of the {@link KnowledgeBase}, not
 * through a {@link KnowledgeBase} transaction. A {@link KnowledgeBase} instance that was running
 * while its database was purged keeps the removed objects in its caches and must be restarted.
 * </p>
 *
 * @see HistoryCompaction Collapsing the history below a revision.
 * @see NullReference The value a reference without a target holds.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeletedObjectPurge {

	private static final String PARAM_IDS = "ids";

	private static final String PARAM_TARGET_IDS = "targetIds";

	private static final String PARAM_TYPE = "type";

	private static final String PARAM_BRANCH = "branch";

	private static final String PARAM_BRANCHES = "branches";

	private static final String RESULT_BRANCH = "branch";

	private static final String RESULT_ID = "id";

	private static final String RESULT_REV_MAX = "revMax";

	private static final String RESULT_TARGET_ID = "targetId";

	private static final String RESULT_TARGET_BRANCH = "targetBranch";

	private static final String RESULT_TARGET_TYPE = "targetType";

	private static final String RESULT_TYPE = "type";

	private static final String RESULT_DATA_BRANCH = "dataBranch";

	private static final String RESULT_COUNT = "cnt";

	/** Result column index of {@link #RESULT_ID} in the row revision query. */
	private static final int REVISION_RESULT_ID = 1;

	/** Result column index of {@link #RESULT_REV_MAX} in the row revision query. */
	private static final int REVISION_RESULT_REV_MAX = 2;

	/** Result column index of {@link #RESULT_BRANCH} in the referring row query. */
	private static final int REFERER_RESULT_BRANCH = 1;

	/** Result column index of {@link #RESULT_ID} in the referring row query. */
	private static final int REFERER_RESULT_ID = 2;

	/** Result column index of {@link #RESULT_TARGET_ID} in the referring row query. */
	private static final int REFERER_RESULT_TARGET_ID = 3;

	/** Result column index of {@link #RESULT_ID} in the content query. */
	private static final int CONTENT_RESULT_ID = 1;

	/** Result column index of {@link #RESULT_REV_MAX} in the content query. */
	private static final int CONTENT_RESULT_REV_MAX = 2;

	/** Result column index of {@link #RESULT_TARGET_ID} in the content query. */
	private static final int CONTENT_RESULT_TARGET_ID = 3;

	/** Result column index of {@link #RESULT_TARGET_BRANCH} in the content query. */
	private static final int CONTENT_RESULT_TARGET_BRANCH = 4;

	/** Result column index of {@link #RESULT_TARGET_TYPE} in the content query. */
	private static final int CONTENT_RESULT_TARGET_TYPE = 5;

	/** Result column index of {@link #RESULT_BRANCH} in the branch switch query. */
	private static final int SWITCH_RESULT_BRANCH = 1;

	/** Result column index of {@link #RESULT_TYPE} in the branch switch query. */
	private static final int SWITCH_RESULT_TYPE = 2;

	/** Result column index of {@link #RESULT_DATA_BRANCH} in the branch switch query. */
	private static final int SWITCH_RESULT_DATA_BRANCH = 3;

	private final DBHelper _sqlDialect;

	private final ConnectionPool _pool;

	private final MORepository _repository;

	private final ItemTables _tables;

	private final Map<String, TableAccess> _tableAccessByType;

	private final List<TableAccess> _tableAccess;

	private final FlexDataAccess _flexDataAccess;

	private final CompiledStatement _selectBranches;

	private final CompiledStatement _selectBranchSwitch;

	/**
	 * Creates a {@link DeletedObjectPurge}.
	 *
	 * @param sqlDialect
	 *        The dialect to compile the statements for.
	 * @param pool
	 *        The pool to get connections from.
	 * @param repository
	 *        The types of the {@link KnowledgeBase} to purge objects from.
	 */
	public DeletedObjectPurge(DBHelper sqlDialect, ConnectionPool pool, MORepository repository) {
		_sqlDialect = sqlDialect;
		_pool = pool;
		_repository = repository;
		_tables = new ItemTables(repository);

		List<TableAccess> tableAccess = new ArrayList<>();
		Map<String, TableAccess> tableAccessByType = new HashMap<>();
		for (ItemTables.Table table : _tables.getItemTables()) {
			TableAccess access = new TableAccess(table);
			tableAccess.add(access);
			tableAccessByType.put(table.getType().getName(), access);
		}
		_tableAccess = Collections.unmodifiableList(tableAccess);
		_tableAccessByType = tableAccessByType;

		ItemTables.Table flexData = _tables.getFlexData();
		_flexDataAccess = flexData == null ? null : new FlexDataAccess(flexData);

		_selectBranches = createSelectBranches();
		_selectBranchSwitch = createSelectBranchSwitch();
	}

	/**
	 * Creates a {@link DeletedObjectPurge} for the given {@link KnowledgeBase}.
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} whose connection pool, SQL dialect and types the operation
	 *        works on.
	 */
	public static DeletedObjectPurge newInstance(DBKnowledgeBase kb) {
		return new DeletedObjectPurge(kb.dbHelper, kb.getConnectionPool(), kb.getMORepository());
	}

	/**
	 * The tables processed by this operation.
	 */
	public ItemTables getTables() {
		return _tables;
	}

	/**
	 * The types of the {@link KnowledgeBase} to purge objects from.
	 */
	public MORepository getRepository() {
		return _repository;
	}

	/**
	 * Computes what purging the given objects would remove, without changing anything.
	 *
	 * @param seeds
	 *        The objects to remove. The history context of a key is ignored, an object is removed in
	 *        all of its revisions. The type of a key must be one of the
	 *        {@link ItemTables#getItemTables() item tables}.
	 * @param log
	 *        Receives progress information.
	 * @return The hull, the numbers of rows and reference values affected and the objects that block
	 *         the operation.
	 */
	public Report analyze(Collection<ObjectKey> seeds, Log log) throws SQLException {
		Report report = new Report();
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			BranchMapping branches = readBranchMapping(connection);
			computeHull(connection, report, seeds, branches, log);
			findBlockers(connection, report, log);
			countErasedRows(connection, report, log);
			countClearedReferences(connection, report, branches, log);
			log.info(report.toString());
		} finally {
			_pool.releaseReadConnection(connection);
		}
		return report;
	}

	/**
	 * Extends the seeds to the hull that can be removed as a whole.
	 */
	private void computeHull(PooledConnection connection, Report report, Collection<ObjectKey> seeds,
			BranchMapping branches, Log log) throws SQLException {
		List<ObjectBranchId> frontier = new ArrayList<>();
		for (ObjectKey seed : seeds) {
			ObjectBranchId identity = toIdentity(seed);
			if (report.addMember(identity, Origin.seed())) {
				frontier.add(identity);
			}
		}
		while (!frontier.isEmpty()) {
			List<ObjectBranchId> found = new ArrayList<>();
			addReferers(connection, report, frontier, branches, found);
			addContents(connection, report, frontier, branches, found);
			frontier = found;
		}
		log.info("The hull of " + seeds.size() + " objects has " + report.getHull().size() + " objects.", Log.VERBOSE);
	}

	private ObjectBranchId toIdentity(ObjectKey seed) {
		MetaObject type = seed.getObjectType();
		ItemTables.Table table = _tables.getItemTable(type.getName());
		if (table == null) {
			throw new IllegalArgumentException(
				"Objects of type '" + type.getName() + "' are not stored in an item table: " + seed);
		}
		return new ObjectBranchId(seed.getBranchContext(), table.getType(), seed.getObjectName());
	}

	/**
	 * Adds every object that holds a mandatory reference to one of the given objects.
	 */
	private void addReferers(PooledConnection connection, Report report, List<ObjectBranchId> targets,
			BranchMapping branches, List<ObjectBranchId> found) throws SQLException {
		for (Group group : groupByTypeAndBranch(targets)) {
			MetaObject targetType = group.getTable().getType();
			List<Long> viewBranches = branches.viewBranches(targetType.getName(), group.getBranch());
			for (TableAccess table : _tableAccess) {
				MetaObject refererType = table.getTable().getType();
				for (ReferenceAccess reference : table.getMandatoryReferences()) {
					if (!reference.acceptsTarget(targetType, viewBranches)) {
						continue;
					}
					reference.selectReferers(connection, group.getIds(), targetType.getName(), viewBranches,
						(branch, id, targetId) -> {
							ObjectBranchId identity = new ObjectBranchId(branch, refererType, id);
							ObjectBranchId target = new ObjectBranchId(group.getBranch(), targetType, targetId);
							if (report.addMember(identity, Origin.reference(reference.getLocation(), target))) {
								found.add(identity);
							}
						});
				}
			}
		}
	}

	/**
	 * Adds the contents that were deleted together with one of the given objects.
	 *
	 * <p>
	 * A content belongs to the hull if its last row ends in the same revision as the row of its
	 * container that refers to it. A content that was removed from its container earlier, or that is
	 * still alive, stays.
	 * </p>
	 */
	private void addContents(PooledConnection connection, Report report, List<ObjectBranchId> containers,
			BranchMapping branches, List<ObjectBranchId> found) throws SQLException {
		List<ContentCandidate> candidates = new ArrayList<>();
		for (Group group : groupByTypeAndBranch(containers)) {
			TableAccess table = _tableAccessByType.get(group.getTable().getType().getName());
			for (ReferenceAccess reference : table.getContainerReferences()) {
				reference.selectContents(connection, group, branches, candidates);
			}
		}
		if (candidates.isEmpty()) {
			return;
		}
		Map<ObjectBranchId, List<ContentCandidate>> byContent = new LinkedHashMap<>();
		for (ContentCandidate candidate : candidates) {
			byContent.computeIfAbsent(candidate.getContent(), key -> new ArrayList<>()).add(candidate);
		}
		Map<ObjectBranchId, Long> lastRevisions = lastRevisions(connection, byContent.keySet());
		for (Entry<ObjectBranchId, List<ContentCandidate>> entry : byContent.entrySet()) {
			ObjectBranchId content = entry.getKey();
			if (report.isMember(content)) {
				continue;
			}
			Long lastRevision = lastRevisions.get(content);
			if (lastRevision == null || lastRevision.longValue() == Revision.CURRENT_REV) {
				continue;
			}
			for (ContentCandidate candidate : entry.getValue()) {
				if (candidate.getRevMax() != lastRevision.longValue()) {
					continue;
				}
				if (report.addMember(content,
					Origin.content(candidate.getReference(), candidate.getContainer()))) {
					found.add(content);
				}
				break;
			}
		}
	}

	/**
	 * Reports every member of the hull that is not deleted.
	 */
	private void findBlockers(PooledConnection connection, Report report, Log log) throws SQLException {
		Map<ObjectBranchId, Long> lastRevisions = lastRevisions(connection, report.getHull());
		for (ObjectBranchId identity : new ArrayList<>(report.getHull())) {
			Long lastRevision = lastRevisions.get(identity);
			if (lastRevision != null && lastRevision.longValue() == Revision.CURRENT_REV) {
				report.addBlocker(identity);
			}
		}
		if (report.isBlocked()) {
			log.info("The purge is blocked by " + report.getBlockers().size() + " objects that are not deleted.",
				Log.VERBOSE);
		}
	}

	/**
	 * The last revision a row of each of the given objects is valid in.
	 *
	 * <p>
	 * An object without a row at all is missing from the result, an object that is not deleted
	 * answers {@link Revision#CURRENT_REV}.
	 * </p>
	 */
	private Map<ObjectBranchId, Long> lastRevisions(PooledConnection connection,
			Collection<ObjectBranchId> identities) throws SQLException {
		Map<ObjectBranchId, Long> result = new HashMap<>();
		for (Group group : groupByTypeAndBranch(identities)) {
			TableAccess table = _tableAccessByType.get(group.getTable().getType().getName());
			for (List<TLID> chunk : chunks(group.getIds())) {
				Map<TLID, Long> revisions = table.lastRevisions(connection, group.getBranch(), chunk);
				for (Entry<TLID, Long> revision : revisions.entrySet()) {
					result.put(new ObjectBranchId(group.getBranch(), group.getTable().getType(), revision.getKey()),
						revision.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * Counts the rows of the hull in the item tables and in the table of dynamic values.
	 */
	private void countErasedRows(PooledConnection connection, Report report, Log log) throws SQLException {
		for (Group group : groupByTypeAndBranch(report.getHull())) {
			ItemTables.Table table = group.getTable();
			TableAccess access = _tableAccessByType.get(table.getType().getName());
			long rows = 0;
			long flexRows = 0;
			for (List<TLID> chunk : chunks(group.getIds())) {
				rows += access.countRows(connection, group.getBranch(), chunk);
				if (_flexDataAccess != null) {
					flexRows +=
						_flexDataAccess.countRows(connection, group.getBranch(), table.getType().getName(), chunk);
				}
			}
			if (rows > 0) {
				report.table(table.getDBName()).addErasedRows(rows);
			}
			if (flexRows > 0) {
				report.table(_flexDataAccess.getTable().getDBName()).addErasedRows(flexRows);
			}
		}
		log.info("The purge erases " + report.getErasedRows() + " rows.", Log.VERBOSE);
	}

	/**
	 * Counts the optional reference values that point into the hull from a row that survives.
	 */
	private void countClearedReferences(PooledConnection connection, Report report, BranchMapping branches, Log log)
			throws SQLException {
		for (Group group : groupByTypeAndBranch(report.getHull())) {
			MetaObject targetType = group.getTable().getType();
			List<Long> viewBranches = branches.viewBranches(targetType.getName(), group.getBranch());
			for (TableAccess table : _tableAccess) {
				MetaObject refererType = table.getTable().getType();
				String tableName = table.getTable().getDBName();
				for (ReferenceAccess reference : table.getOptionalReferences()) {
					if (!reference.acceptsTarget(targetType, viewBranches)) {
						continue;
					}
					reference.selectReferers(connection, group.getIds(), targetType.getName(), viewBranches,
						(branch, id, targetId) -> {
							if (report.isMember(new ObjectBranchId(branch, refererType, id))) {
								// The row is erased, there is nothing left to clear.
								return;
							}
							report.table(tableName).addClearedReference(reference.getName());
						});
				}
			}
		}
		log.info("The purge clears " + report.getClearedValues() + " reference values.", Log.VERBOSE);
	}

	/**
	 * The given objects, grouped by their type and branch.
	 */
	private List<Group> groupByTypeAndBranch(Collection<ObjectBranchId> identities) {
		Map<String, Map<Long, List<TLID>>> idsByTypeAndBranch = new LinkedHashMap<>();
		for (ObjectBranchId identity : identities) {
			idsByTypeAndBranch
				.computeIfAbsent(identity.getObjectType().getName(), name -> new LinkedHashMap<>())
				.computeIfAbsent(Long.valueOf(identity.getBranchId()), branch -> new ArrayList<>())
				.add(identity.getObjectName());
		}
		List<Group> result = new ArrayList<>();
		for (Entry<String, Map<Long, List<TLID>>> byType : idsByTypeAndBranch.entrySet()) {
			ItemTables.Table table = _tables.getItemTable(byType.getKey());
			for (Entry<Long, List<TLID>> byBranch : byType.getValue().entrySet()) {
				result.add(new Group(table, byBranch.getKey().longValue(), byBranch.getValue()));
			}
		}
		return result;
	}

	/**
	 * Splits the given values into parts that fit into a single {@code IN} set.
	 *
	 * @see DBHelper#getMaxSetSize()
	 */
	private List<List<TLID>> chunks(List<TLID> values) {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}
		int maxSetSize = _sqlDialect.getMaxSetSize();
		if (values.size() <= maxSetSize) {
			return Collections.singletonList(values);
		}
		List<List<TLID>> result = new ArrayList<>();
		for (int start = 0, size = values.size(); start < size; start += maxSetSize) {
			result.add(values.subList(start, Math.min(start + maxSetSize, size)));
		}
		return result;
	}

	/**
	 * A set of objects of the same type living on the same branch.
	 */
	private static final class Group {

		private final ItemTables.Table _table;

		private final long _branch;

		private final List<TLID> _ids;

		Group(ItemTables.Table table, long branch, List<TLID> ids) {
			_table = table;
			_branch = branch;
			_ids = ids;
		}

		ItemTables.Table getTable() {
			return _table;
		}

		long getBranch() {
			return _branch;
		}

		List<TLID> getIds() {
			return _ids;
		}

		/**
		 * The identity of the object of this group with the given identifier.
		 */
		ObjectBranchId identity(TLID id) {
			return new ObjectBranchId(_branch, _table.getType(), id);
		}
	}

	/**
	 * An object a member of the hull refers to through a container reference.
	 */
	private static final class ContentCandidate {

		private final ObjectBranchId _content;

		private final ObjectBranchId _container;

		private final String _reference;

		private final long _revMax;

		ContentCandidate(ObjectBranchId content, ObjectBranchId container, String reference, long revMax) {
			_content = content;
			_container = container;
			_reference = reference;
			_revMax = revMax;
		}

		/**
		 * The object that is referred to.
		 */
		ObjectBranchId getContent() {
			return _content;
		}

		/**
		 * The member of the hull that refers to it.
		 */
		ObjectBranchId getContainer() {
			return _container;
		}

		/**
		 * The table and the name of the container reference.
		 */
		String getReference() {
			return _reference;
		}

		/**
		 * The last revision the referring row is valid in.
		 */
		long getRevMax() {
			return _revMax;
		}
	}

	/**
	 * Receives a row whose reference points to an object of the hull.
	 */
	private interface RefererHandler {

		/**
		 * Reports a matching row.
		 *
		 * @param branch
		 *        The branch of the row.
		 * @param id
		 *        The identity of the object the row belongs to.
		 * @param targetId
		 *        The identifier the reference points to.
		 */
		void handle(long branch, TLID id, TLID targetId);
	}

	/**
	 * Access to a single item table, with all statements compiled once.
	 */
	private final class TableAccess {

		private final ItemTables.Table _table;

		private final CompiledStatement _countRows;

		private final CompiledStatement _rowRevisions;

		private final List<ReferenceAccess> _mandatoryReferences = new ArrayList<>();

		private final List<ReferenceAccess> _optionalReferences = new ArrayList<>();

		private final List<ReferenceAccess> _containerReferences = new ArrayList<>();

		TableAccess(ItemTables.Table table) {
			_table = table;
			_countRows = createCountRows(table);
			_rowRevisions = createRowRevisions(table);
			for (ItemTables.Reference reference : table.getReferences()) {
				ReferenceAccess access = new ReferenceAccess(table, reference);
				if (reference.isMandatory()) {
					_mandatoryReferences.add(access);
				} else {
					_optionalReferences.add(access);
				}
				if (reference.isContainer()) {
					_containerReferences.add(access);
				}
			}
		}

		ItemTables.Table getTable() {
			return _table;
		}

		List<ReferenceAccess> getMandatoryReferences() {
			return _mandatoryReferences;
		}

		List<ReferenceAccess> getOptionalReferences() {
			return _optionalReferences;
		}

		List<ReferenceAccess> getContainerReferences() {
			return _containerReferences;
		}

		/**
		 * The number of rows the given objects have in this table, over all revisions.
		 */
		long countRows(PooledConnection connection, long branch, List<TLID> ids) throws SQLException {
			return queryCount(connection, _countRows, arguments(_table.hasBranchColumn(), branch, ids));
		}

		/**
		 * The last revision a row of each of the given objects is valid in.
		 */
		Map<TLID, Long> lastRevisions(PooledConnection connection, long branch, List<TLID> ids) throws SQLException {
			Map<TLID, Long> result = new HashMap<>();
			try (ResultSet dbResult =
				_rowRevisions.executeQuery(connection, arguments(_table.hasBranchColumn(), branch, ids))) {
				while (dbResult.next()) {
					TLID id = IdentifierUtil.getId(dbResult, REVISION_RESULT_ID);
					long revMax = dbResult.getLong(REVISION_RESULT_REV_MAX);
					Long before = result.get(id);
					if (before == null || before.longValue() < revMax) {
						result.put(id, Long.valueOf(revMax));
					}
				}
			}
			return result;
		}
	}

	/**
	 * Access to a single reference of an item table.
	 */
	private final class ReferenceAccess {

		private final ItemTables.Table _table;

		private final ItemTables.Reference _reference;

		private final boolean _typeFiltered;

		private final boolean _branchFiltered;

		private final CompiledStatement _selectReferers;

		private final CompiledStatement _selectContents;

		ReferenceAccess(ItemTables.Table table, ItemTables.Reference reference) {
			_table = table;
			_reference = reference;
			_typeFiltered = reference.getTypeColumn() != null;
			_branchFiltered = reference.getBranchColumn() != null || table.hasBranchColumn();
			_selectReferers = createSelectReferers(table, reference, _typeFiltered, _branchFiltered);
			_selectContents = reference.isContainer() ? createSelectContents(table, reference) : null;
		}

		String getName() {
			return _reference.getName();
		}

		/**
		 * The reference and the table declaring it, as they appear in a report.
		 */
		String getLocation() {
			return _table.getDBName() + "." + _reference.getName();
		}

		/**
		 * Whether a value of this reference can point to an object of the given type on one of the
		 * given branches.
		 */
		boolean acceptsTarget(MetaObject targetType, List<Long> viewBranches) {
			if (viewBranches.isEmpty()) {
				return false;
			}
			if (!_branchFiltered && !viewBranches.contains(Long.valueOf(TLContext.TRUNK_ID))) {
				// The value of this reference constantly refers to the trunk.
				return false;
			}
			return _reference.acceptsTarget(targetType);
		}

		/**
		 * Reports every row whose value of this reference is one of the given objects.
		 *
		 * @param targetIds
		 *        The identifiers of the objects to look for, all of the given type and living on the
		 *        data branch the given branches switch to.
		 * @param targetType
		 *        The name of the concrete type of those objects.
		 * @param viewBranches
		 *        The branches a value of this reference must name to refer to that data branch.
		 */
		void selectReferers(PooledConnection connection, List<TLID> targetIds, String targetType,
				List<Long> viewBranches, RefererHandler handler) throws SQLException {
			for (List<TLID> chunk : chunks(targetIds)) {
				List<Object> arguments = new ArrayList<>();
				arguments.add(chunk);
				if (_typeFiltered) {
					arguments.add(targetType);
				}
				if (_branchFiltered) {
					arguments.add(viewBranches);
				}
				try (ResultSet dbResult = _selectReferers.executeQuery(connection, arguments.toArray())) {
					while (dbResult.next()) {
						handler.handle(
							dbResult.getLong(REFERER_RESULT_BRANCH),
							IdentifierUtil.getId(dbResult, REFERER_RESULT_ID),
							IdentifierUtil.getId(dbResult, REFERER_RESULT_TARGET_ID));
					}
				}
			}
		}

		/**
		 * Collects the objects the given containers refer to through this container reference.
		 *
		 * @param candidates
		 *        Receives one entry per referring row.
		 */
		void selectContents(PooledConnection connection, Group containers, BranchMapping branches,
				List<ContentCandidate> candidates) throws SQLException {
			TLID nullId = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
			for (List<TLID> chunk : chunks(containers.getIds())) {
				try (ResultSet dbResult = _selectContents.executeQuery(connection,
					arguments(_table.hasBranchColumn(), containers.getBranch(), chunk))) {
					while (dbResult.next()) {
						TLID targetId = IdentifierUtil.getId(dbResult, CONTENT_RESULT_TARGET_ID);
						if (targetId == null || targetId.equals(nullId)) {
							continue;
						}
						String targetType = _typeFiltered ? dbResult.getString(CONTENT_RESULT_TARGET_TYPE)
							: _reference.getMonomorphicTargetType();
						if (targetType == null) {
							continue;
						}
						ItemTables.Table targetTable = _tables.getItemTable(targetType);
						if (targetTable == null) {
							continue;
						}
						long viewBranch = dbResult.getLong(CONTENT_RESULT_TARGET_BRANCH);
						long dataBranch = branches.dataBranch(targetType, viewBranch);
						ObjectBranchId content =
							new ObjectBranchId(dataBranch, targetTable.getType(), targetId);
						ObjectBranchId container =
							containers.identity(IdentifierUtil.getId(dbResult, CONTENT_RESULT_ID));
						candidates.add(new ContentCandidate(content, container, getLocation(),
							dbResult.getLong(CONTENT_RESULT_REV_MAX)));
					}
				}
			}
		}
	}

	/**
	 * Access to the table of dynamic attribute values.
	 *
	 * <p>
	 * Its rows carry the type of the object they belong to in a column of their own, so they are
	 * addressed by branch, type and identifier.
	 * </p>
	 *
	 * @see AbstractFlexDataManager#FLEX_DATA
	 */
	private final class FlexDataAccess {

		private final ItemTables.Table _table;

		private final CompiledStatement _countRows;

		FlexDataAccess(ItemTables.Table table) {
			_table = table;
			_countRows = createCountFlexRows(table);
		}

		ItemTables.Table getTable() {
			return _table;
		}

		long countRows(PooledConnection connection, long branch, String type, List<TLID> ids) throws SQLException {
			List<Object> arguments = new ArrayList<>();
			if (_table.hasBranchColumn()) {
				arguments.add(Long.valueOf(branch));
			}
			arguments.add(type);
			arguments.add(ids);
			return queryCount(connection, _countRows, arguments.toArray());
		}
	}

	private static Object[] arguments(boolean withBranch, long branch, List<TLID> ids) {
		if (withBranch) {
			return new Object[] { Long.valueOf(branch), ids };
		}
		return new Object[] { ids };
	}

	private long queryCount(PooledConnection connection, CompiledStatement statement, Object[] arguments)
			throws SQLException {
		try (ResultSet result = statement.executeQuery(connection, arguments)) {
			if (!result.next()) {
				return 0;
			}
			return result.getLong(RESULT_COUNT);
		}
	}

	/**
	 * {@code SELECT count(1) FROM t WHERE BRANCH = branch AND IDENTIFIER IN (ids)}
	 */
	private CompiledStatement createCountRows(ItemTables.Table table) {
		List<Parameter> parameters = new ArrayList<>();
		List<SQLExpression> conditions = new ArrayList<>();
		addBranchCondition(table, parameters, conditions);
		addIdCondition(table, parameters, conditions);
		return query(parameters,
			select(
				Collections.singletonList(columnDef(count(literalInteger(1)), RESULT_COUNT)),
				table(table.getType(), NO_TABLE_ALIAS),
				and(conditions.toArray(new SQLExpression[conditions.size()])))).toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT count(1) FROM FLEX_DATA WHERE BRANCH = branch AND TYPE = type AND IDENTIFIER IN
	 * (ids)}
	 */
	private CompiledStatement createCountFlexRows(ItemTables.Table table) {
		DBAttribute typeColumn = flexTypeColumn(table);
		List<Parameter> parameters = new ArrayList<>();
		List<SQLExpression> conditions = new ArrayList<>();
		addBranchCondition(table, parameters, conditions);
		parameters.add(parameterDef(typeColumn, PARAM_TYPE));
		conditions.add(eq(column(NO_TABLE_ALIAS, typeColumn, NOT_NULL), parameter(typeColumn, PARAM_TYPE)));
		addIdCondition(table, parameters, conditions);
		return query(parameters,
			select(
				Collections.singletonList(columnDef(count(literalInteger(1)), RESULT_COUNT)),
				table(table.getType(), NO_TABLE_ALIAS),
				and(conditions.toArray(new SQLExpression[conditions.size()])))).toSql(_sqlDialect);
	}

	private static DBAttribute flexTypeColumn(ItemTables.Table table) {
		MOAttribute attribute = table.getType().getAttributeOrNull(AbstractFlexDataManager.TYPE);
		return attribute.getDbMapping()[0];
	}

	/**
	 * {@code SELECT IDENTIFIER, REV_MAX FROM t WHERE BRANCH = branch AND IDENTIFIER IN (ids)}
	 */
	private CompiledStatement createRowRevisions(ItemTables.Table table) {
		List<Parameter> parameters = new ArrayList<>();
		List<SQLExpression> conditions = new ArrayList<>();
		addBranchCondition(table, parameters, conditions);
		addIdCondition(table, parameters, conditions);
		return query(parameters,
			select(
				columns(
					columnDef(column(NO_TABLE_ALIAS, table.getIdentifier(), NOT_NULL), RESULT_ID),
					columnDef(column(NO_TABLE_ALIAS, table.getRevMax(), NOT_NULL), RESULT_REV_MAX)),
				table(table.getType(), NO_TABLE_ALIAS),
				and(conditions.toArray(new SQLExpression[conditions.size()])))).toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT BRANCH, IDENTIFIER, R_ID FROM t WHERE R_ID IN (targetIds) AND R_TYPE = type AND
	 * R_BRC IN (branches)}
	 *
	 * <p>
	 * The type condition is dropped for a monomorphic reference, which has no type column, and the
	 * branch condition for a branch local reference of a table without a branch column, whose value
	 * constantly refers to the trunk.
	 * </p>
	 */
	private CompiledStatement createSelectReferers(ItemTables.Table table, ItemTables.Reference reference,
			boolean typeFiltered, boolean branchFiltered) {
		DBAttribute idColumn = reference.getIdColumn();
		List<Parameter> parameters = new ArrayList<>();
		List<SQLExpression> conditions = new ArrayList<>();
		parameters.add(setParameterDef(PARAM_TARGET_IDS, idColumn.getSQLType()));
		conditions.add(inSet(column(NO_TABLE_ALIAS, idColumn, NOT_NULL),
			setParameter(PARAM_TARGET_IDS, idColumn.getSQLType())));
		if (typeFiltered) {
			DBAttribute typeColumn = reference.getTypeColumn();
			parameters.add(parameterDef(typeColumn, PARAM_TYPE));
			conditions
				.add(eq(column(NO_TABLE_ALIAS, typeColumn, NOT_NULL), parameter(typeColumn, PARAM_TYPE)));
		}
		if (branchFiltered) {
			parameters.add(setParameterDef(PARAM_BRANCHES, DBType.LONG));
			conditions.add(inSet(table.viewBranchExpression(reference),
				setParameter(PARAM_BRANCHES, DBType.LONG)));
		}
		return query(parameters,
			select(
				columns(
					columnDef(table.branchExpression(), RESULT_BRANCH),
					columnDef(column(NO_TABLE_ALIAS, table.getIdentifier(), NOT_NULL), RESULT_ID),
					columnDef(column(NO_TABLE_ALIAS, idColumn, NOT_NULL), RESULT_TARGET_ID)),
				table(table.getType(), NO_TABLE_ALIAS),
				and(conditions.toArray(new SQLExpression[conditions.size()])))).toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT IDENTIFIER, REV_MAX, R_ID, R_BRC, R_TYPE FROM t WHERE BRANCH = branch AND
	 * IDENTIFIER IN (ids)}
	 */
	private CompiledStatement createSelectContents(ItemTables.Table table, ItemTables.Reference reference) {
		List<Parameter> parameters = new ArrayList<>();
		List<SQLExpression> conditions = new ArrayList<>();
		addBranchCondition(table, parameters, conditions);
		addIdCondition(table, parameters, conditions);
		List<SQLColumnDefinition> columns = new ArrayList<>();
		columns.add(columnDef(column(NO_TABLE_ALIAS, table.getIdentifier(), NOT_NULL), RESULT_ID));
		columns.add(columnDef(column(NO_TABLE_ALIAS, table.getRevMax(), NOT_NULL), RESULT_REV_MAX));
		columns.add(columnDef(column(NO_TABLE_ALIAS, reference.getIdColumn(), NOT_NULL), RESULT_TARGET_ID));
		columns.add(columnDef(table.viewBranchExpression(reference), RESULT_TARGET_BRANCH));
		DBAttribute typeColumn = reference.getTypeColumn();
		if (typeColumn != null) {
			columns.add(columnDef(column(NO_TABLE_ALIAS, typeColumn, !NOT_NULL), RESULT_TARGET_TYPE));
		}
		return query(parameters,
			select(columns, table(table.getType(), NO_TABLE_ALIAS),
				and(conditions.toArray(new SQLExpression[conditions.size()])))).toSql(_sqlDialect);
	}

	/**
	 * Adds {@code BRANCH = branch}, if the table has a branch column.
	 */
	private static void addBranchCondition(ItemTables.Table table, List<Parameter> parameters,
			List<SQLExpression> conditions) {
		DBAttribute branch = table.getBranch();
		if (branch == null) {
			return;
		}
		parameters.add(parameterDef(branch, PARAM_BRANCH));
		conditions.add(eq(column(NO_TABLE_ALIAS, branch, NOT_NULL), parameter(branch, PARAM_BRANCH)));
	}

	/**
	 * Adds {@code IDENTIFIER IN (ids)}.
	 */
	private static void addIdCondition(ItemTables.Table table, List<Parameter> parameters,
			List<SQLExpression> conditions) {
		DBAttribute identifier = table.getIdentifier();
		parameters.add(setParameterDef(PARAM_IDS, identifier.getSQLType()));
		conditions.add(inSet(column(NO_TABLE_ALIAS, identifier, NOT_NULL),
			setParameter(PARAM_IDS, identifier.getSQLType())));
	}

	/**
	 * Reads which branch holds the data of a type for each branch it can be viewed on.
	 */
	private BranchMapping readBranchMapping(PooledConnection connection) throws SQLException {
		Set<Long> branches = new LinkedHashSet<>();
		try (ResultSet result = _selectBranches.executeQuery(connection)) {
			while (result.next()) {
				branches.add(Long.valueOf(result.getLong(RESULT_BRANCH)));
			}
		}
		if (branches.isEmpty()) {
			branches.add(Long.valueOf(TLContext.TRUNK_ID));
		}
		Map<String, Map<Long, Long>> dataBranchByType = new HashMap<>();
		if (_selectBranchSwitch != null) {
			try (ResultSet result = _selectBranchSwitch.executeQuery(connection)) {
				while (result.next()) {
					Long branch = Long.valueOf(result.getLong(SWITCH_RESULT_BRANCH));
					String type = result.getString(SWITCH_RESULT_TYPE);
					Long dataBranch = Long.valueOf(result.getLong(SWITCH_RESULT_DATA_BRANCH));
					dataBranchByType.computeIfAbsent(type, name -> new HashMap<>()).put(branch, dataBranch);
				}
			}
		}
		return new BranchMapping(new ArrayList<>(branches), dataBranchByType);
	}

	/**
	 * {@code SELECT IDENTIFIER FROM BRANCH}
	 */
	private CompiledStatement createSelectBranches() {
		MOClass branchType = BasicTypes.getBranchType(_repository);
		DBAttribute branchId = BranchSupport.getBranchIDAttr(branchType);
		return query(
			select(
				Collections.singletonList(columnDef(column(NO_TABLE_ALIAS, branchId, NOT_NULL), RESULT_BRANCH)),
				table((DBTableMetaObject) branchType, NO_TABLE_ALIAS))).toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT BRANCH, TYPE, DATA_BRANCH FROM BRANCH_SWITCH}
	 */
	private CompiledStatement createSelectBranchSwitch() {
		MetaObject type = _repository.getTypeOrNull(BranchSupport.BRANCH_SWITCH_TYPE_NAME);
		if (!(type instanceof MOClass)) {
			return null;
		}
		MOClass switchType = (MOClass) type;
		return query(
			select(
				columns(
					columnDef(column(NO_TABLE_ALIAS, BranchSupport.getLinkBranchAttr(switchType), NOT_NULL),
						RESULT_BRANCH),
					columnDef(column(NO_TABLE_ALIAS, BranchSupport.getLinkTypeAttr(switchType), NOT_NULL),
						RESULT_TYPE),
					columnDef(column(NO_TABLE_ALIAS, BranchSupport.getLinkDataBranchAttr(switchType), NOT_NULL),
						RESULT_DATA_BRANCH)),
				table((DBTableMetaObject) switchType, NO_TABLE_ALIAS))).toSql(_sqlDialect);
	}

	/**
	 * Where the data of a type is stored for each branch it can be viewed on.
	 *
	 * <p>
	 * A reference stores the branch its target is looked up on, which is the branch of the view the
	 * value was created in. The rows of the target live on the branch that view switches to for the
	 * type of the target, which is a different one whenever that type was not branched.
	 * </p>
	 *
	 * @see BranchSupport#BRANCH_SWITCH_TYPE_NAME
	 * @see Branch#getBaseBranchId(MetaObject)
	 */
	private static final class BranchMapping {

		private final List<Long> _branches;

		private final Map<String, Map<Long, Long>> _dataBranchByType;

		private final Map<String, Map<Long, List<Long>>> _viewBranchesByType = new HashMap<>();

		BranchMapping(List<Long> branches, Map<String, Map<Long, Long>> dataBranchByType) {
			_branches = branches;
			_dataBranchByType = dataBranchByType;
		}

		/**
		 * The branch the data of the given type lies on when viewed from the given branch.
		 */
		long dataBranch(String typeName, long viewBranch) {
			Map<Long, Long> byBranch = _dataBranchByType.get(typeName);
			if (byBranch == null) {
				return viewBranch;
			}
			Long result = byBranch.get(Long.valueOf(viewBranch));
			return result == null ? viewBranch : result.longValue();
		}

		/**
		 * The branches from which the data of the given type on the given branch is seen.
		 */
		List<Long> viewBranches(String typeName, long dataBranch) {
			return _viewBranchesByType
				.computeIfAbsent(typeName, name -> new HashMap<>())
				.computeIfAbsent(Long.valueOf(dataBranch),
					branch -> computeViewBranches(typeName, branch.longValue()));
		}

		private List<Long> computeViewBranches(String typeName, long dataBranch) {
			List<Long> result = new ArrayList<>();
			for (Long branch : _branches) {
				if (dataBranch(typeName, branch.longValue()) == dataBranch) {
					result.add(branch);
				}
			}
			return result;
		}
	}

	/**
	 * Why an object belongs to the hull of a purge.
	 *
	 * @see Report#getOrigin(ObjectBranchId)
	 */
	public static final class Origin {

		/**
		 * The rule that put an object into the hull.
		 */
		public enum Kind {

			/**
			 * The object was named as an object to remove.
			 */
			SEED,

			/**
			 * The object holds a mandatory reference to a member of the hull.
			 */
			REFERENCE,

			/**
			 * The object was contained in a member of the hull and was deleted together with it.
			 */
			CONTENT;
		}

		private static final Origin SEED_ORIGIN = new Origin(Kind.SEED, null, null);

		private final Kind _kind;

		private final String _reference;

		private final ObjectBranchId _cause;

		private Origin(Kind kind, String reference, ObjectBranchId cause) {
			_kind = kind;
			_reference = reference;
			_cause = cause;
		}

		/**
		 * An object that was named as an object to remove.
		 */
		public static Origin seed() {
			return SEED_ORIGIN;
		}

		/**
		 * An object that holds a mandatory reference to a member of the hull.
		 *
		 * @param reference
		 *        The table and the name of the reference that points to the member.
		 * @param target
		 *        The member the reference points to.
		 */
		public static Origin reference(String reference, ObjectBranchId target) {
			return new Origin(Kind.REFERENCE, reference, target);
		}

		/**
		 * An object that was contained in a member of the hull and was deleted together with it.
		 *
		 * @param reference
		 *        The table and the name of the container reference.
		 * @param container
		 *        The member that contained the object.
		 */
		public static Origin content(String reference, ObjectBranchId container) {
			return new Origin(Kind.CONTENT, reference, container);
		}

		/**
		 * The rule that put the object into the hull.
		 */
		public Kind getKind() {
			return _kind;
		}

		/**
		 * The table and the name of the reference the object was reached through, or
		 * <code>null</code> for a {@link Kind#SEED seed}.
		 */
		public String getReference() {
			return _reference;
		}

		/**
		 * The member of the hull the object was reached from, or <code>null</code> for a
		 * {@link Kind#SEED seed}.
		 */
		public ObjectBranchId getCause() {
			return _cause;
		}

		@Override
		public String toString() {
			switch (_kind) {
				case REFERENCE:
					return "'" + _reference + "' refers to " + _cause;
				case CONTENT:
					return "contained in " + _cause + " through '" + _reference + "'";
				default:
					return "seed";
			}
		}
	}

	/**
	 * An object of the hull that is not deleted and therefore blocks the purge.
	 *
	 * @see Report#getBlockers()
	 */
	public static final class Blocker {

		private final ObjectBranchId _identity;

		private final Origin _origin;

		Blocker(ObjectBranchId identity, Origin origin) {
			_identity = identity;
			_origin = origin;
		}

		/**
		 * The object that is not deleted.
		 */
		public ObjectBranchId getIdentity() {
			return _identity;
		}

		/**
		 * Why the object belongs to the hull.
		 */
		public Origin getOrigin() {
			return _origin;
		}

		@Override
		public String toString() {
			return _identity + " (" + _origin + ")";
		}
	}

	/**
	 * What a {@link DeletedObjectPurge} removes, or would remove.
	 */
	public static final class Report {

		private final Map<ObjectBranchId, Origin> _hull = new LinkedHashMap<>();

		private final Map<String, TableReport> _tables = new LinkedHashMap<>();

		private final List<Blocker> _blockers = new ArrayList<>();

		Report() {
			super();
		}

		/**
		 * Adds an object to the hull.
		 *
		 * @return Whether the object was not a member before.
		 */
		boolean addMember(ObjectBranchId identity, Origin origin) {
			return _hull.putIfAbsent(identity, origin) == null;
		}

		boolean isMember(ObjectBranchId identity) {
			return _hull.containsKey(identity);
		}

		void addBlocker(ObjectBranchId identity) {
			_blockers.add(new Blocker(identity, getOrigin(identity)));
		}

		/**
		 * All objects that are removed, the seeds among them.
		 */
		public Collection<ObjectBranchId> getHull() {
			return Collections.unmodifiableSet(_hull.keySet());
		}

		/**
		 * Why the given object belongs to the {@link #getHull() hull}, or <code>null</code> if it is
		 * no member.
		 */
		public Origin getOrigin(ObjectBranchId identity) {
			return _hull.get(identity);
		}

		/**
		 * The per-table numbers, in the order the tables were touched.
		 */
		public Collection<TableReport> getTables() {
			return Collections.unmodifiableCollection(_tables.values());
		}

		/**
		 * The numbers of the table with the given database name, or <code>null</code> if that table
		 * is not touched.
		 */
		public TableReport getTable(String dbName) {
			return _tables.get(dbName);
		}

		/**
		 * The numbers of the table with the given database name, created on first access.
		 */
		TableReport table(String dbName) {
			return _tables.computeIfAbsent(dbName, TableReport::new);
		}

		/**
		 * The objects of the {@link #getHull() hull} that are not deleted.
		 */
		public List<Blocker> getBlockers() {
			return Collections.unmodifiableList(_blockers);
		}

		/**
		 * Whether an object that is not deleted prevents the purge.
		 */
		public boolean isBlocked() {
			return !_blockers.isEmpty();
		}

		/**
		 * Total number of rows that are erased.
		 */
		public long getErasedRows() {
			return _tables.values().stream().mapToLong(TableReport::getErasedRows).sum();
		}

		/**
		 * Total number of reference values that are reset to the value of a reference without a
		 * target.
		 */
		public long getClearedValues() {
			return _tables.values().stream().mapToLong(TableReport::getClearedValues).sum();
		}

		/**
		 * Whether nothing would be changed.
		 */
		public boolean isEmpty() {
			return getErasedRows() == 0 && getClearedValues() == 0;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("Purge of ");
			buffer.append(_hull.size());
			buffer.append(" objects: ");
			buffer.append(getErasedRows());
			buffer.append(" rows erased, ");
			buffer.append(getClearedValues());
			buffer.append(" references cleared.");
			for (TableReport table : _tables.values()) {
				buffer.append("\n  ");
				buffer.append(table.toString());
			}
			for (Blocker blocker : _blockers) {
				buffer.append("\n  Blocked by ");
				buffer.append(blocker.toString());
			}
			return buffer.toString();
		}
	}

	/**
	 * The part of a {@link Report} that concerns a single database table.
	 */
	public static final class TableReport {

		private final String _tableName;

		private long _erasedRows;

		private final Map<String, Long> _clearedReferences = new LinkedHashMap<>();

		TableReport(String tableName) {
			_tableName = tableName;
		}

		/**
		 * The database name of the table.
		 */
		public String getTableName() {
			return _tableName;
		}

		/**
		 * Number of rows that are erased.
		 */
		public long getErasedRows() {
			return _erasedRows;
		}

		void addErasedRows(long rows) {
			_erasedRows += rows;
		}

		/**
		 * Number of values per reference that are reset to the value of a reference without a
		 * target.
		 *
		 * @return The counts by {@link MOReference#getName() reference name}; a reference without a
		 *         value to clear is missing from the result.
		 */
		public Map<String, Long> getClearedReferences() {
			return Collections.unmodifiableMap(_clearedReferences);
		}

		/**
		 * Total number of values of this table that are reset to the value of a reference without a
		 * target.
		 */
		public long getClearedValues() {
			long result = 0;
			for (Long count : _clearedReferences.values()) {
				result += count.longValue();
			}
			return result;
		}

		void addClearedReference(String referenceName) {
			_clearedReferences.merge(referenceName, Long.valueOf(1), (before, one) -> before + one);
		}

		@Override
		public String toString() {
			return _tableName + ": " + _erasedRows + " rows erased, " + getClearedValues() + " references cleared "
				+ _clearedReferences;
		}
	}

}
