/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import java.sql.SQLException;
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
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AttributeItemQuery.AttributeItemResult;
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
	private DiffFlexDeletionResult flexDeletionResult;
	private DiffRowDeletionResult rowDeletionResult;
	private DiffFlexUpdateResult flexUpdateResult;

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
	 * ID of the current object in {@link #flexUpdateResult}
	 */
	private ObjectBranchId currentFlexUpdateID;
	/**
	 * ID of the current object in {@link #flexDeletionResult}
	 */
	private ObjectBranchId currentFlexDelID;

	private boolean _hasFlexUpdate;

	private boolean _hasFlexDeletion;

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
		MOKnowledgeItemImpl flexDataType = kb.lookupType(AbstractFlexDataManager.FLEX_DATA);
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

		flexUpdateResult =
			DiffFlexAttributesQuery.createDiffFlexAttributesQuery(sqlDialect, flexDataType, null, sourceBranch,
				sourceRev, destBranch, destRev).query(connection);
		flexDeletionResult =
			DiffFlexDeletionQuery.createDiffFlexDeletionQuery(sqlDialect, flexDataType, null, sourceBranch,
				sourceRev, destBranch, destRev).query(connection);

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
		while (true) {
			boolean hasChange = _touchedTypes.next();
			if (!hasChange) {
				currentType = null;
				break;
			}
			String typeName = _touchedTypes.getType();
			currentType = getKnowledgeBase().lookupType(typeName);
			initCurrentResults();
			break;
		}
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

		setNewFlexUpdateID();
		setNewFlexDelID();

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

	private void setNewFlexUpdateID() throws SQLException {
		if (!_hasFlexUpdate) {
			_hasFlexUpdate = flexUpdateResult.next();
		}
		if (_hasFlexUpdate) {
			AttributeItemResult newValues = flexUpdateResult.getNewValues();
			int compareValue = beforeRowType(newValues.getTypeName());
			if (compareValue == 0) {
				currentFlexUpdateID = new ObjectBranchId(newValues.getBranch(), currentType, newValues.getIdentifier());
				_hasFlexUpdate = false;
			} else if (compareValue > 0) {
				/* All changes for the current type processed. */
				currentFlexUpdateID = null;
			} else {
				/* Can currently not assert the following when types are fetched from the XRef
				 * table, because the flex table contains a BLOB column. The diff SQL reports all
				 * rows in which the BLOB column is not null, because the database can not compare
				 * them. Therefore the result also contains rows for types which are not contained
				 * in the XRefTable. */
				/* There is no row change for the current flex change. This is actually not
				 * possible, because if the the touched types are retrieved from that XRef table
				 * than the a change of an flex attribute is alos reported to the XRef table,
				 * otherwise all types are considered, therefore flex type must also be considered.
				 * The only reason is a wrong order of the database result. not restricted. */
//				assert false : "Illegal update: rowtype: '" + currentType + "', flextype: '" + newValues.getTypeName()
//					+ "', flexid: '" + newValues.getIdentifier() + "'";
				_hasFlexUpdate = false;
				setNewFlexUpdateID();
			}
		} else {
			currentFlexUpdateID = null;
		}
	}

	private void setNewFlexDelID() throws SQLException {
		if (!_hasFlexDeletion) {
			_hasFlexDeletion = flexDeletionResult.next();
		}
		if (_hasFlexDeletion) {
			int compareValue = beforeRowType(flexDeletionResult.getTypeName());
			if (compareValue == 0) {
				currentFlexDelID =
					new ObjectBranchId(flexDeletionResult.getBranchId(), currentType,
						flexDeletionResult.getObjectName());
				_hasFlexDeletion = false;
			} else if (compareValue > 0) {
				/* All changes for the current type processed. */
				currentFlexDelID = null;
			} else {
				/* There is no row change for the current flex change. This is actually not
				 * possible, because if the the touched types are retrieved from that XRef table
				 * than the a change of an flex attribute is alos reported to the XRef table,
				 * otherwise all types are considered, therefore flex type must also be considered.
				 * The only reason is a wrong order of the database result. not restricted. */
				assert false : "Illegal update: rowtype: '" + currentType + "', flextype: '" + flexDeletionResult.getTypeName()
					+ "', flexid: '" + flexDeletionResult.getObjectName() + "'";
				_hasFlexDeletion = false;
				setNewFlexDelID();
			}
		} else {
			currentFlexDelID = null;
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
		if (currentObjectID == null || (currentFlexUpdateID != null && ID_FINDER.compare(currentObjectID, currentFlexUpdateID) > 0)) {
			currentObjectID = currentFlexUpdateID;
		}
		if (currentObjectID == null || (currentFlexDelID != null && ID_FINDER.compare(currentObjectID, currentFlexDelID) > 0)) {
			currentObjectID = currentFlexDelID;
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

						addUpdatedFlexAttr(change);
						addDeletedFlexAttr(change);

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

						assert !currentRowDelID.equals(currentFlexUpdateID) : "As the object is deleted, there must not be an update for the same object.";
						while (currentRowDelID.equals(currentFlexUpdateID)) {
							setNewFlexUpdateID();
						}

						setNewRowDelID();

					} else if (currentObjectID.equals(currentFlexUpdateID)) {
						ItemChange change;
						itemEvent = change = new ItemUpdate(revision, currentFlexUpdateID, true);

						addUpdatedFlexAttr(change);
						addDeletedFlexAttr(change);

					} else if (currentObjectID.equals(currentFlexDelID)) {
						ItemChange change;
						itemEvent = change = new ItemUpdate(revision, currentFlexDelID, true);

						addDeletedFlexAttr(change);

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

	private void addUpdatedFlexAttr(ItemChange change) throws SQLException {
		while (currentObjectID.equals(currentFlexUpdateID)) {
			final Object oldValue = flexUpdateResult.getOldValues().getAttributeValue();
			final Object newValue = flexUpdateResult.getNewValues().getAttributeValue();
			change.setValue(flexUpdateResult.getNewValues().getAttributeName(), oldValue, newValue);

			setNewFlexUpdateID();
		}
	}

	private void addDeletedFlexAttr(ItemChange change) throws SQLException {
		while (currentObjectID.equals(currentFlexDelID)) {
			change.setValue(flexDeletionResult.getAttributeName(), flexDeletionResult.getValue(), null);

			setNewFlexDelID();
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

	private void cleanFlexUpdateResult() throws SQLException {
		if (flexUpdateResult == null) {
			return;
		}
		DiffFlexUpdateResult result = flexUpdateResult;
		flexUpdateResult = null;
		result.close();
	}

	private void cleanFlexDeletionResult() throws SQLException {
		if (flexDeletionResult == null) {
			return;
		}
		DiffFlexDeletionResult result = flexDeletionResult;
		flexDeletionResult = null;
		result.close();
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
		try {
			cleanFlexUpdateResult();
		} finally {
			cleanFlexDeletionResult();
		}
	}

}
