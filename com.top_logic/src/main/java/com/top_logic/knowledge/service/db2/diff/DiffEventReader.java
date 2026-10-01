/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.MutableObjectContext;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractKnowledgeEventReader;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.RevisionXref;
import com.top_logic.knowledge.service.db2.TypeResult;
import com.top_logic.knowledge.service.db2.diff.AbstractDiffUpdateQuery.DiffUpdateResult;
import com.top_logic.knowledge.service.db2.diff.DiffFlexAttributesQuery.DiffFlexUpdateResult;
import com.top_logic.knowledge.service.db2.diff.DiffFlexDeletionQuery.DiffFlexDeletionResult;
import com.top_logic.knowledge.service.db2.diff.DiffRowDeletionQuery.DiffRowDeletionResult;
import com.top_logic.util.TLContext;

/**
 * The {@link DiffEventReader} creates a small set of events to be replayed to
 * the {@link KnowledgeBase} to ensure that the data on two revisions are the
 * same.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class DiffEventReader extends AbstractKnowledgeEventReader<ItemEvent> {

	/**
	 * Revision number used by the {@link DiffEventReader} to create {@link KnowledgeEvent}.
	 * 
	 * TODO: find correct semantic for the commit number of the events
	 * 
	 */
	public static final long NO_REVISION = -1;

	private final long sourceBranch;
	private final long destBranch;
	private final long sourceRev = startRev;
	private final long destRev = stopRev;

	private DBHelper sqlDialect;

	/**
	 * The result sets to get events for the current type
	 */
	private DiffUpdateResult<?> rowAttributesResult;
	private DiffRowDeletionResult rowDeletionResult;

	/**
	 * Changed values of dynamic attributes, one for each table of dynamic attribute values.
	 */
	private final List<FlexUpdates> _flexUpdates = new ArrayList<>();

	/**
	 * Deleted values of dynamic attributes, one for each table of dynamic attribute values.
	 */
	private final List<FlexDeletions> _flexDeletions = new ArrayList<>();

	private MOAttribute _rowBranchAttribute;

	private MOAttribute _rowIDAttribute;

	/**
	 * current knowledge type of for which events are gotten, and whether this
	 * type is an association type.
	 */
	private MOKnowledgeItem currentType;
	/**
	 * Id's to determine event for 'minimal' object to process
	 */
	private ObjectBranchId currentObjectID;
	/**
	 * ID of the current object in {@link #rowAttributesResult}
	 */
	private ObjectBranchId currentRowUpdateID;
	/**
	 * ID of the current object in {@link #rowDeletionResult}
	 */
	private ObjectBranchId currentRowDelID;

	/**
	 * Comparator of {@link ObjectBranchId}s.
	 */
	private static final Comparator<ObjectBranchId> ID_FINDER = new Comparator<>() {

		@Override
		public int compare(ObjectBranchId o1, ObjectBranchId o2) {
			int compareResult = CollectionUtil.compareLong(o1.getBranchId(), o2.getBranchId());
			if (compareResult != 0) {
				return compareResult;
			}
			return o1.getObjectName().compareTo(o2.getObjectName());
		}
	};

	private TypeResult _touchedTypes;

	private final MutableObjectContext _objectContext;

	public DiffEventReader(DBKnowledgeBase kb, long sourceRev, long sourceBranch, long destRev, long destBranch) throws SQLException {
		super(kb, sourceRev, destRev);
		this._objectContext = new MutableObjectContext(kb);
		this.sourceBranch = sourceBranch;
		this.destBranch = destBranch;

		boolean success = false;
		try {
			init();
			success = true;
		} finally {
			if (!success) {
				// Free potentially allocated resources, caller has no chance to
				// close reader, because the object construction fails.
				close();
			}
		}
	}

	private void init() throws SQLException {
		sqlDialect = kb.getConnectionPool().getSQLDialect();
		PooledConnection connection = getReadConnection();
		Set<String> typeNameFilter = null;

		if (sourceBranch == destBranch) {
			long firstRev;
			long lastRev;
			if (sourceRev < destRev) {
				firstRev = sourceRev;
				lastRev = destRev;
			} else {
				firstRev = destRev;
				lastRev = sourceRev;
			}
			_touchedTypes =
				RevisionXref.createTypeResult(getKnowledgeBase(), connection, firstRev, lastRev, typeNameFilter,
					null);
		} else {
			/* Must always use all types. The reason is that a diff between different branches may
			 * include changes up to the common ancestor branch. */
			_touchedTypes = RevisionXref.createAllTypesResult(getKnowledgeBase(), typeNameFilter);
		}

		for (String flexTypeName : List.of(AbstractFlexDataManager.FLEX_DATA,
			AbstractFlexDataManager.FLEX_BINARY_DATA)) {
			MOKnowledgeItemImpl flexDataType = kb.lookupType(flexTypeName);
			_flexUpdates.add(new FlexUpdates(
				DiffFlexAttributesQuery.createDiffFlexAttributesQuery(sqlDialect, flexDataType, null, sourceBranch,
					sourceRev, destBranch, destRev).query(connection)));
			_flexDeletions.add(new FlexDeletions(
				DiffFlexDeletionQuery.createDiffFlexDeletionQuery(sqlDialect, flexDataType, null, sourceBranch,
					sourceRev, destBranch, destRev).query(connection)));
		}

		findNextType();
	}

	/**
	 * sets the new type to process, initializes and process query to get events
	 * for that type.
	 * 
	 * @throws SQLException
	 *         when trying to process query for that type fails.
	 */
	private void findNextType() throws SQLException {
		if (!_touchedTypes.next()) {
			currentType = null;
			return;
		}
		String typeName = _touchedTypes.getType();
		currentType = getKnowledgeBase().lookupType(typeName);
		initCurrentResults();
	}

	/**
	 * initializes the result sets for the given type 
	 */
	private void initCurrentResults() throws SQLException {
		cleanRowResults();
		final PooledConnection connection = getReadConnection();


		_rowBranchAttribute = currentType.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME);
		_rowIDAttribute = currentType.getAttributeOrNull(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);

		rowAttributesResult =
			DiffRowAttributesQuery.createDiffRowAttributesQuery(sqlDialect, currentType, sourceBranch, sourceRev,
				destBranch, destRev).query(connection);
		setNewRowUpdateID();

		rowDeletionResult =
			DiffRowDeletionQuery.createDiffRowDeletionQuery(sqlDialect, currentType, sourceBranch, sourceRev,
				destBranch, destRev).query(connection);
		setNewRowDelID();

		for (FlexChanges changes : allFlexChanges()) {
			changes.findNext();
		}

		findNextObjectName();
	}

	private void setNewRowUpdateID() throws SQLException {
		if (rowAttributesResult.next()) {
			/* Neither branch nor Identifier must need a context object */
			ObjectContext contextObject = null;
			long branch;
			if (currentType.multipleBranches()) {
				branch = ((Long) rowAttributesResult.getNewValue(_rowBranchAttribute, contextObject)).longValue();
			} else {
				branch = TLContext.TRUNK_ID;
			}
			TLID name = (TLID) rowAttributesResult.getNewValue(_rowIDAttribute, contextObject);
			currentRowUpdateID = new ObjectBranchId(branch, currentType, name);
		} else {
			currentRowUpdateID = null;
		}
	}

	private void setNewRowDelID() throws SQLException {
		if (rowDeletionResult.next()) {
			currentRowDelID =
				new ObjectBranchId(rowDeletionResult.getBranchId(), currentType, rowDeletionResult.getObjectName());
		} else {
			currentRowDelID = null;
		}
	}

	private List<FlexChanges> allFlexChanges() {
		List<FlexChanges> result = new ArrayList<>(_flexUpdates.size() + _flexDeletions.size());
		result.addAll(_flexUpdates);
		result.addAll(_flexDeletions);
		return result;
	}

	/**
	 * Changes of dynamic attribute values read from a single table of dynamic attribute values.
	 *
	 * <p>
	 * The changes are ordered by type, branch and identifier. The changes of the
	 * {@link #currentType} are reported object by object.
	 * </p>
	 */
	private abstract class FlexChanges {

		/**
		 * ID of the object of the current row, <code>null</code> if there are no more changes of
		 * the {@link #currentType}.
		 */
		ObjectBranchId _currentID;

		/**
		 * Whether the current row has been read, but not yet been assigned to a type.
		 */
		private boolean _pending;

		/**
		 * Moves to the next row.
		 */
		abstract boolean nextRow() throws SQLException;

		/**
		 * The name of the type of the object of the current row.
		 */
		abstract String typeName() throws SQLException;

		/**
		 * The branch of the object of the current row.
		 */
		abstract long branch() throws SQLException;

		/**
		 * The identifier of the object of the current row.
		 */
		abstract TLID identifier() throws SQLException;

		/**
		 * Adds the change of the current row to the given change.
		 */
		abstract void addTo(ItemChange change) throws SQLException;

		/**
		 * Whether a change for an object of a type without any row change is skipped. Otherwise,
		 * such a change is an error.
		 */
		abstract boolean skipUnknownTypes();

		abstract void close() throws SQLException;

		/**
		 * Moves to the next change of the {@link #currentType}.
		 */
		final void findNext() throws SQLException {
			while (true) {
				if (!_pending) {
					_pending = nextRow();
					if (!_pending) {
						// There is no more change at all.
						_currentID = null;
						break;
					}
				}
				int compareValue = beforeRowType(typeName());
				if (compareValue == 0) {
					_currentID = new ObjectBranchId(branch(), currentType, identifier());
					_pending = false;
				} else if (compareValue > 0) {
					/* All changes for the current type processed. */
					_currentID = null;
				} else {
					/* There is no row change for the current flex change. This is actually not
					 * possible, because if the the touched types are retrieved from that XRef table
					 * than the a change of an flex attribute is also reported to the XRef table,
					 * otherwise all types are considered, therefore flex type must also be
					 * considered. The only reason is a wrong order of the database result. */
					assert skipUnknownTypes() : "Illegal change: rowtype: '" + currentType + "', flextype: '"
						+ typeName() + "', flexid: '" + identifier() + "'";
					_pending = false;
					// check next change
					continue;
				}
				break;
			}
		}

		/**
		 * Adds all changes of the {@link #currentObjectID} to the given change.
		 */
		final void addAll(ItemChange change) throws SQLException {
			while (currentObjectID.equals(_currentID)) {
				addTo(change);
				findNext();
			}
		}

		/**
		 * Skips all changes of the given object.
		 */
		final void skip(ObjectBranchId id) throws SQLException {
			while (id.equals(_currentID)) {
				findNext();
			}
		}

	}

	private final class FlexUpdates extends FlexChanges {

		private final DiffFlexUpdateResult _result;

		FlexUpdates(DiffFlexUpdateResult result) {
			_result = result;
		}

		@Override
		boolean nextRow() throws SQLException {
			return _result.next();
		}

		@Override
		String typeName() throws SQLException {
			return _result.getNewValues().getTypeName();
		}

		@Override
		long branch() throws SQLException {
			return _result.getNewValues().getBranch();
		}

		@Override
		TLID identifier() throws SQLException {
			return _result.getNewValues().getIdentifier();
		}

		/**
		 * Can not assert that the type has row changes when types are fetched from the XRef
		 * table, because the tables of dynamic values contain LOB columns. The diff SQL reports
		 * all rows in which a LOB column is not null, because the database can not compare them.
		 * Therefore the result also contains rows for types which are not contained in the XRef
		 * table.
		 */
		@Override
		boolean skipUnknownTypes() {
			return true;
		}

		/**
		 * Adds the change, if the values differ: The result reports all rows with LOB values,
		 * since the database can not compare them.
		 */
		@Override
		void addTo(ItemChange change) throws SQLException {
			Object oldValue = _result.getOldValues().getAttributeValue();
			Object newValue = _result.getNewValues().getAttributeValue();
			if (!CollectionUtil.equals(oldValue, newValue)) {
				change.setValue(_result.getNewValues().getAttributeName(), oldValue, newValue);
			}
		}

		@Override
		void close() throws SQLException {
			_result.close();
		}

	}

	private final class FlexDeletions extends FlexChanges {

		private final DiffFlexDeletionResult _result;

		FlexDeletions(DiffFlexDeletionResult result) {
			_result = result;
		}

		@Override
		boolean nextRow() throws SQLException {
			return _result.next();
		}

		@Override
		String typeName() throws SQLException {
			return _result.getTypeName();
		}

		@Override
		long branch() throws SQLException {
			return _result.getBranchId();
		}

		@Override
		TLID identifier() throws SQLException {
			return _result.getObjectName();
		}

		@Override
		boolean skipUnknownTypes() {
			return false;
		}

		@Override
		void addTo(ItemChange change) throws SQLException {
			change.setValue(_result.getAttributeName(), _result.getValue(), null);
		}

		@Override
		void close() throws SQLException {
			_result.close();
		}

	}

	private int beforeRowType(String flexTypeName) {
		return flexTypeName.compareTo(currentType.getName());
	}

	/**
	 * Sets the Id of the Object for which events will be produced. This is used
	 * to determine which of the different result sets have to be inspected.
	 */
	private void findNextObjectName() {
		currentObjectID = currentRowUpdateID;
		if (currentObjectID == null || (currentRowDelID != null && ID_FINDER.compare(currentObjectID, currentRowDelID) > 0)) {
			currentObjectID = currentRowDelID;
		}
		for (FlexChanges changes : allFlexChanges()) {
			ObjectBranchId flexID = changes._currentID;
			if (currentObjectID == null || (flexID != null && ID_FINDER.compare(currentObjectID, flexID) > 0)) {
				currentObjectID = flexID;
			}
		}
	}

	@Override
	public ItemEvent readEvent() {
		while (currentType != null) {

			if (currentObjectID != null) {
				ItemEvent itemEvent;

				long revision = NO_REVISION;
				try {
					if (currentObjectID.equals(currentRowUpdateID)) {
						ItemChange change;
						boolean isCreation = rowAttributesResult.isCreation();
						ObjectContext contextObject = _objectContext.getObjectContext(currentRowUpdateID);
						if (isCreation) {
							itemEvent = change = new ObjectCreation(revision, currentRowUpdateID);
						} else {
							itemEvent = change = new ItemUpdate(revision, currentRowUpdateID, true);
						}
						final List<MOAttribute> attributes = currentType.getAttributes();
						for (MOAttribute attr : attributes) {
							if (attr.isSystem()) {
								continue;
							}
							Object oldValue = isCreation ? null : rowAttributesResult.getOldValue(attr, contextObject);
							Object newValue = rowAttributesResult.getNewValue(attr, contextObject);
							change.setValue(attr.getName(), oldValue, newValue);
						}

						// Deletions first: A value moving from one table of dynamic values to another
						// is reported as deletion followed by an update.
						addDeletedFlexAttr(change);
						addUpdatedFlexAttr(change);

						setNewRowUpdateID();

					} else if (currentObjectID.equals(currentRowDelID)) {
						ItemDeletion itemDeletion = new ItemDeletion(revision, currentRowDelID);
						itemEvent = itemDeletion;

						/* Object is deleted in a revision after sourceRev. To ensure that the
						 * referenced objects can be accessed the references are resolved in the
						 * source revision. */
						ObjectContext contextObject =
							_objectContext.getObjectContext(currentRowDelID.toObjectKey(sourceRev));

						final List<MOAttribute> attributes = currentType.getAttributes();
						for (MOAttribute attr : attributes) {
							if (attr.isSystem()) {
								continue;
							}
							Object oldValue = rowDeletionResult.getValue(attr, contextObject);
							itemDeletion.setValue(attr.getName(), oldValue);
						}

						addDeletedFlexAttr(itemDeletion);

						for (FlexUpdates updates : _flexUpdates) {
							assert !currentRowDelID.equals(updates._currentID) : "As the object is deleted, there must not be an update for the same object.";
							updates.skip(currentRowDelID);
						}

						setNewRowDelID();

					} else if (hasFlexChanges(currentObjectID)) {
						ItemChange change;
						itemEvent = change = new ItemUpdate(revision, currentObjectID, true);

						// Deletions first: A value moving from one table of dynamic values to another
						// is reported as deletion followed by an update.
						addDeletedFlexAttr(change);
						addUpdatedFlexAttr(change);

					} else {
						assert false : "Unexpected objectID " + currentObjectID;
						itemEvent = null;
					}
				} catch (SQLException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				}

				findNextObjectName();

				if (itemEvent instanceof ItemUpdate && ((ItemUpdate) itemEvent).getValues().isEmpty()) {
					// No one needs item updates where the values don't have
					// changed
					continue;
				}
				return itemEvent;
			}

			try {
				findNextType();
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}
		return null;
	}

	private boolean hasFlexChanges(ObjectBranchId id) {
		for (FlexChanges changes : allFlexChanges()) {
			if (id.equals(changes._currentID)) {
				return true;
			}
		}
		return false;
	}

	private void addUpdatedFlexAttr(ItemChange change) throws SQLException {
		for (FlexUpdates updates : _flexUpdates) {
			updates.addAll(change);
		}
	}

	private void addDeletedFlexAttr(ItemChange change) throws SQLException {
		for (FlexDeletions deletions : _flexDeletions) {
			deletions.addAll(change);
		}
	}

	@Override
	public void close() {
		try {
			cleanCurrentResults();
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException();
		} finally {
			super.close();
		}
	}

	private void cleanRowDeletionResult() throws SQLException {
		if (rowDeletionResult == null) {
			return;
		}
		DiffRowDeletionResult result = rowDeletionResult;
		rowDeletionResult = null;
		result.close();
	}

	private void cleanRowAttributesResult() throws SQLException {
		if (rowAttributesResult == null) {
			return;
		}
		DiffUpdateResult<?> result = rowAttributesResult;
		rowAttributesResult = null;
		result.close();
	}

	private void cleanCurrentResults() throws SQLException {
		try {
			cleanRowResults();
		} finally {
			cleanFlexResult();
		}
	}

	private void cleanRowResults() throws SQLException {
		try {
			cleanRowDeletionResult();
		} finally {
			cleanRowAttributesResult();
		}
	}

	private void cleanFlexResult() throws SQLException {
		List<FlexChanges> changes = allFlexChanges();
		_flexUpdates.clear();
		_flexDeletions.clear();
		closeAll(changes, 0);
	}

	private static void closeAll(List<FlexChanges> changes, int index) throws SQLException {
		if (index >= changes.size()) {
			return;
		}
		try {
			changes.get(index).close();
		} finally {
			closeAll(changes, index + 1);
		}
	}

}
