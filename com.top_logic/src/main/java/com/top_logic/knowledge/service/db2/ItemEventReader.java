/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.MutableObjectContext;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AttributeItemQuery;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AttributeItemQuery.AttributeItemResult;
import com.top_logic.util.TLContext;

/**
 * {@link EventReader} that constructs {@link ItemEvent}s by differencing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemEventReader extends AbstractKnowledgeEventReader<ItemEvent> {

	private static final String TABLE_ALIAS = NO_TABLE_ALIAS;

	private final boolean keepOldValues;
	
	/**
	 * names of types to look for
	 */
	private final Set<String> typeNameFilter;

	/**
	 * ids of branches on which events must occurred to be reported
	 */
	private final Set<Long> branchFilter;
	
	/**
	 * The current state of the object. From changes of this map, update events are produced. The
	 * content of this Map is used to get values of an object during deletion of that object.
	 * 
	 * @see #setDeletedValues(ItemDeletion)
	 */
	private final HashMap<String, Object> diff;
	
	private final PriorityQueue<EventKey> eventQueue;
	
	private AttributeItemResult flexAttributes;

	private TypeResult touchedTypes;

	/**
	 * Current type of read row attributes.
	 */
	private MOKnowledgeItemImpl currentRowType;
	
	private String currentFlexTypeName;
	
	private MOKnowledgeItemImpl currentFlexType;

	/**
	 * Attributes of {@link #currentRowType}.
	 */
	private List<? extends MOAttribute> currentRowAttributes;

	/**
	 * Queue of row attribute changes.
	 */
	private ItemResult rowAttributes;

	private boolean hasRowTypes;

	/**
	 * Whether {@link #rowAttributes} has a current value.
	 */
	private boolean hasRowAttributes;

	private DBAttribute _rowBranchAttribute;

	private DBAttribute _rowIDAttribute;

	private DBAttribute _rowRevMaxAttribute;

	private DBAttribute _rowRevMinAttribute;

	private ObjectBranchId currentRowId;

	private ItemEvent previousLastEvent;

	private ObjectBranchId currentFlexId;

	/**
	 * Whether a walk in the past is requested, e.g. for some undo operations.
	 * For instance a creation event becomes a delete event, a delete event is
	 * modified to a creation event.
	 */
	private boolean reverted;

	/**
	 * variables to determine whether an {@link ItemEvent} is an initial event
	 * on a branch or not. Those events must not be reported.
	 */
	private long currentBranchId = -1;
	private long currentBranchCreateRev;

	private final MutableObjectContext _objectContext;

	/**
	 * Creates a {@link ItemEventReader} without additional type filter or branch filter.
	 * 
	 * @see ItemEventReader#ItemEventReader(DBKnowledgeBase, boolean, long, long, Set, Set)
	 */
	public ItemEventReader(DBKnowledgeBase kb, boolean keepOldValues, long startRev, long stopRev) throws SQLException {
		this(kb, keepOldValues, startRev, stopRev, null, null);
	}

	/**
	 * Creates a {@link ItemEventReader}.
	 * 
	 * @param kb
	 *        See {@link #getKnowledgeBase()}
	 * @param keepOldValues
	 *        See {@link #getKeepOldValues()}
	 * @param startRev
	 *        See {@link #getStartRev()}
	 * @param stopRev
	 *        See {@link #getStopRev()}
	 * @param typeNameFilter
	 *        Optional type names that restricts the search to the given type.
	 * @param branchFilter
	 *        Optional branch ID that restricts the search to the given branch.
	 */
	public ItemEventReader(DBKnowledgeBase kb, boolean keepOldValues, long startRev, long stopRev,
			Set<String> typeNameFilter, Set<Long> branchFilter) throws SQLException {
		super(kb, min(startRev,stopRev), max(startRev,stopRev));
		this._objectContext = new MutableObjectContext(kb);
		this.reverted = stopRev < startRev;
		if (this.reverted) {
			this.eventQueue = new PriorityQueue<>(50, RevertComparator.INSTANCE);
		} else {
			this.eventQueue = new PriorityQueue<>(50);
		}
		
		this.keepOldValues = keepOldValues;
		this.typeNameFilter = typeNameFilter;
		this.branchFilter = branchFilter;
		
		this.diff = new HashMap<>();
		
		boolean success = false;
		try {
			init();
			success = true;
		} finally {
			if (! success) {
				// Free potentially allocated resources, caller has no chance to
				// close reader, because the object construction fails.
				cleanup();
			}
		}
	}

	private SQLExpression[] addBranchFilter(SQLExpression[] expressions, DBAttribute branchAttribute) {
		if (this.branchFilter != null && !this.branchFilter.isEmpty()) {
			for (int i = 0; i < expressions.length; i++) {
				// the event must occur on a requested branch
				final Iterator<Long> branches = this.branchFilter.iterator();
				SQLExpression branchEquality = attributeEq(TABLE_ALIAS, branchAttribute, branches.next());
				while (branches.hasNext()) {
					branchEquality = or(branchEquality, attributeEq(TABLE_ALIAS, branchAttribute, branches.next()));
				}
				expressions[i] = and(expressions[i], branchEquality);
			}
		}
		return expressions;
	}

	private SQLExpression[] addTypeFilter(SQLExpression[] expressions, DBAttribute typeAttribute) {
		if (typeNameFilter != null && !typeNameFilter.isEmpty()) {
			for (int i = 0; i < expressions.length; i++) {
				// the type of the element the flex attribute belongs to, must be a requested type.
				final Iterator<String> typeNames = typeNameFilter.iterator();
				SQLExpression typeFilter = attributeEq(TABLE_ALIAS, typeAttribute, typeNames.next());
				while (typeNames.hasNext()) {
					typeFilter = or(typeFilter, attributeEq(TABLE_ALIAS, typeAttribute, typeNames.next()));
				}
				expressions[i] = and(expressions[i], typeFilter);
			}
		}
		return expressions;
	}

	private SQLExpression[] correctRevisionFilter(DBAttribute revMaxAttr, DBAttribute revMinAttr) {
		SQLExpression[] itemFilters = new SQLExpression[2];
		// All rows that represent commits with revision numbers r in the range
		// startRev <= r < stopRev plus rows that represent the state of the
		// object just before startRev.
		if (reverted) {
			itemFilters[0] = attributeRange(TABLE_ALIAS, revMaxAttr, startRev, stopRev);
			itemFilters[1] = and(
				attributeRange2(revMinAttr, startRev, stopRev),
				ge(column(TABLE_ALIAS, revMaxAttr), literal(revMaxAttr.getSQLType(), stopRev))
				);
		} else {
			itemFilters[0] = attributeRange(TABLE_ALIAS, revMaxAttr, startRev - 1, stopRev - 1);
			itemFilters[1] = and(
				attributeRange(TABLE_ALIAS, revMinAttr, startRev, stopRev),
				ge(column(TABLE_ALIAS, revMaxAttr), literal(revMaxAttr.getSQLType(), stopRev - 1))
				);
		}
		return itemFilters;
	}

	/**
	 * Creates an expression to describe that <code>attr</code> has a value in the range from
	 * <code>startExcl</code> (exclusive) to <code>stopIncl</code> (inclusive).
	 * 
	 * @see com.top_logic.dob.sql.SQLFactory#attributeRange(String, DBAttribute, Comparable,
	 *      Comparable)
	 */
	private static <T extends Comparable<T>> SQLExpression attributeRange2(DBAttribute attr, T startExcl, T stopIncl) {
		if (startExcl.compareTo(stopIncl) < 0) {
			return and(
				gt(column(TABLE_ALIAS, attr), literal(attr.getSQLType(), startExcl)),
				le(column(TABLE_ALIAS, attr), literal(attr.getSQLType(), stopIncl)));
		}
		return literalFalseLogical();
	}

	/**
	 * Whether {@link ItemUpdate}s are produced that keep old values before the update in each event. 
	 * 
	 * @see ItemUpdate#ItemUpdate(long, ObjectBranchId, boolean)
	 */
	public boolean getKeepOldValues() {
		return keepOldValues;
	}

	private void init() throws SQLException {
		if (stopRev <= startRev) {
			this.hasRowAttributes = false;
			return;
		}
		
		initRowAttributeQueues();
		initFlexAttributeQueues();
	}

	private void initRowAttributeQueues() throws SQLException {
		this.touchedTypes =
			RevisionXref.createTypeResult(kb, getReadConnection(), startRev, stopRev, typeNameFilter, branchFilter);

		findNextRowType();
	}

	private void findNextRowAttributes() throws SQLException {
		if (! hasRowTypes) {
			// No more rows in no types.
			return;
		}
		
		while (true) {
			findNextRowAttributesInType();
			if (hasRowAttributes) {
				// Found next row.
				return;
			}
			
			findNextRowType();
			if (hasRowAttributes || (! hasRowTypes)) {
				// No more rows in no types.
				return;
			}
		}
	}
	
	private void findNextRowType() throws SQLException {
		while (true) {
			this.hasRowTypes = touchedTypes.next();
			if (! hasRowTypes) {
				// No more rows.
				hasRowAttributes = false;
				return;
			}
			
			String nextTypeName = touchedTypes.getType();

			this.currentRowType = kb.lookupType(nextTypeName);
			this.currentRowAttributes = currentRowType.getAttributes();
			_rowIDAttribute = (DBAttribute) currentRowType.getAttributeOrNull(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
			_rowRevMaxAttribute = (DBAttribute) currentRowType.getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
			_rowRevMinAttribute = (DBAttribute) currentRowType.getAttributeOrNull(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
			
			// Enforce creation of new ID.
			this.currentRowId = null;
			
			// Close old row query.
			closeRowAttributes();
			
			SQLExpression[] itemFilters = correctRevisionFilter(_rowRevMaxAttribute, _rowRevMinAttribute);
	 		DBAttribute[] orders;
	 		boolean[] descending;
			if (currentRowType.multipleBranches()) {
				_rowBranchAttribute = (DBAttribute) currentRowType.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME);
				itemFilters = addBranchFilter(itemFilters, _rowBranchAttribute);
				orders = new DBAttribute[] { _rowBranchAttribute, _rowIDAttribute, _rowRevMaxAttribute };
				descending = new boolean[] { false, false, reverted };
			} else {
				orders = new DBAttribute[] { _rowIDAttribute, _rowRevMaxAttribute };
				descending = new boolean[] { false, reverted };
			}
			
			// Read all rows 
			MultipleItemQuery rowAttributesQuery =
				new MultipleItemQuery(kb.dbHelper, currentRowType, TABLE_ALIAS, itemFilters, orders, descending);
			this.rowAttributes = rowAttributesQuery.query(getReadConnection());
			findNextRowAttributesInType();
			
			if (hasRowAttributes) {
				return;
			}
		}
	}

	private void findNextRowAttributesInType() throws SQLException {
		boolean hasNext = rowAttributes.next();
		if (hasNext) {
			long nextRowBranch;
			if (currentRowType.multipleBranches()) {
				nextRowBranch = rowAttributes.getLongValue(_rowBranchAttribute);
			} else {
				nextRowBranch = TLContext.TRUNK_ID;
			}
			TLID nextRowIdentifier = rowAttributes.getIDValue(_rowIDAttribute);
			long nextRevMin = rowAttributes.getLongValue(_rowRevMinAttribute);
			long nextRevMax = rowAttributes.getLongValue(_rowRevMaxAttribute);
			setRowId(nextRowBranch, nextRowIdentifier);
			
			long potentialCreateRev = reverted ? nextRevMax: nextRevMin;
			this.eventQueue.add( 
				new EventKey(
					EventKeyKind.rowAttributes,
					currentRowId,
					potentialCreateRev
				)
			);
			
			if (reverted || nextRevMax < Revision.CURRENT_REV) {
				// A potential deletion.
				long potentialDeleteRev = reverted ? nextRevMin - 1 : nextRevMax + 1;
				if (reverted ? potentialDeleteRev >= startRev : potentialDeleteRev < stopRev) {
					// The potential delete is only relevant, if the revision of the
					// potential delete (revMax + 1) is within the requested range.
					this.eventQueue.add( 
						new EventKey(
							EventKeyKind.objectDeletion,
							currentRowId,
							potentialDeleteRev
						)
					);
				}
			}
		}
		this.hasRowAttributes = hasNext;
	}
	
	private void setRowId(long nextRowBranch, TLID nextRowIdentifier) {
		if (this.currentRowId == null || (! currentRowId.getObjectName().equals(nextRowIdentifier)) || (currentRowId.getBranchId() != nextRowBranch)) {
			this.currentRowId = new ObjectBranchId(nextRowBranch, currentRowType, nextRowIdentifier);
		}
	}

	private void initFlexAttributeQueues() throws SQLException {
		MOKnowledgeItemImpl flexdataType = kb.lookupType(AbstractFlexDataManager.FLEX_DATA);
		DBAttribute revMinAttr = AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.REV_MIN);
		DBAttribute revMaxAttr = AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.REV_MAX);
		DBAttribute typeAttr = AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.TYPE);
		DBAttribute identifierAttr =
			AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.IDENTIFIER);
		DBAttribute attributeAttr =
			AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.ATTRIBUTE);
		
		SQLExpression[] filter = correctRevisionFilter(revMaxAttr, revMinAttr);
		filter = addTypeFilter(filter, typeAttr);

		DBAttribute[] orders;
		boolean[] descending;
		if (flexdataType.multipleBranches()) {
			DBAttribute branchAttr = AbstractFlexDataManager.getAttribute(flexdataType, AbstractFlexDataManager.BRANCH);
			filter = addBranchFilter(filter, branchAttr);
			orders = new DBAttribute[] { typeAttr, branchAttr, identifierAttr, revMinAttr, attributeAttr };
			descending = new boolean[] { false, false, false, reverted, false };
		} else {
			orders = new DBAttribute[] { typeAttr, identifierAttr, revMinAttr, attributeAttr };
			descending = new boolean[] { false, false, reverted, false };
		}
		AttributeItemQuery flexQuery =
			new AttributeItemQuery(kb.dbHelper, flexdataType, TABLE_ALIAS, filter, orders, descending);
		this.flexAttributes = flexQuery.query(getReadConnection());

		findNextFlexAttribute();
	}

	private void findNextFlexAttribute() throws SQLException {
		boolean hasNext = flexAttributes.next();
		if (hasNext) {
			String nextFlexTypeName = flexAttributes.getTypeName();
			long nextFlexBranch = flexAttributes.getBranch();
			TLID nextFlexIdentifier = flexAttributes.getIdentifier();
			long nextRevMin = flexAttributes.getRevMin();
			long nextRevMax = flexAttributes.getRevMax();
			String nextFlexAttributeName = flexAttributes.getAttributeName();
			
			if (! nextFlexTypeName.equals(this.currentFlexTypeName)) {
				this.currentFlexType = kb.lookupType(nextFlexTypeName);
				this.currentFlexId = null;
				
				// Unify names to the same objects (not only equal objects).
				this.currentFlexTypeName = currentFlexType.getName();
			}
			
			if (this.currentFlexId == null || (! currentFlexId.getObjectName().equals(nextFlexIdentifier)) || (currentFlexId.getBranchId() != nextFlexBranch)) {
				this.currentFlexId = new ObjectBranchId(nextFlexBranch, currentFlexType, nextFlexIdentifier);
			}
			
			long setRev = reverted ? nextRevMax: nextRevMin;
			this.eventQueue.add(
				new AttributeEventKey(
					EventKeyKind.flexAttribute,
					currentFlexId,
					setRev,
					nextFlexAttributeName));

			if (reverted || nextRevMax < Revision.CURRENT_REV) {
				// A potential attribute reset.
				long potentialResetRev = reverted ? nextRevMin - 1 : nextRevMax + 1;
				if (reverted ? potentialResetRev >= startRev : potentialResetRev < stopRev) {
					// The potential reset is only relevant, if the revision of the
					// potential reset (revMax + 1) is within the requested range.
					this.eventQueue.add(
						new AttributeEventKey(
							EventKeyKind.attributeDeletion,
							currentFlexId,
							potentialResetRev,
							nextFlexAttributeName));
				}
			}
		}
	}
	
	@Override
	public ItemEvent readEvent() {
		try {
			while (true) {
				ItemEvent result = findNextEvent();
				this.previousLastEvent = result;
				
				if (result == null) {
					return null;
				}
				
				if (reverted ? result.getRevision() >= stopRev : result.getRevision() < startRev) {
					// An event just before the requested interval is created to
					// initialize the differencer. This event must not be reported
					// to the client.
					continue;
				}
				if (!reverted) {
					final long newBranch = result.getOwnerBranch();
					if (currentBranchId != newBranch) {
						currentBranchId = newBranch;
						currentBranchCreateRev = kb.getBranch(currentBranchId).getCreateRevision().getCommitNumber();
					}
					if (result.getRevision() == currentBranchCreateRev) {
						// result event has the same revision as its branch, i.e.
						// its an initial create event on that branch so the result
						// is not of interest.
						continue;
					}
				}
				
				return result;
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
	
	private ItemEvent findNextEvent() throws SQLException {
		ItemEvent nextEvent = null;
		
		EventKey lastEventKey = eventQueue.peek();
		while (true) {
			EventKey currentEventKey = eventQueue.peek();
			if (currentEventKey == null) {
				// No more events.
				return nextEvent;
			}
			
			boolean sameObject = lastEventKey.sameObject(currentEventKey);
			if (! sameObject) {
				assert nextEvent != null : "Event has been constructed.";
				
				// a new object is found. old differences are obsolete
				diff.clear();
				return nextEvent;
			}
			
			boolean sameRev = lastEventKey.sameRev(currentEventKey);
			if (! sameRev) {
				assert nextEvent != null : "Event has been constructed.";
				return nextEvent;
			}
			
			eventQueue.remove();
			
			switch (currentEventKey.kind) {
			case rowAttributes: {
				if (nextEvent != null) {
					throw new AssertionError("No previous event fragment for the same object.");
				}
				
				ItemChange changeEvent;
				if (this.previousLastEvent != null && currentEventKey.sameObject(this.previousLastEvent) && (! (previousLastEvent instanceof ItemDeletion))) {
					changeEvent = new ItemUpdate(currentEventKey.rev, currentEventKey.getObjectId(), keepOldValues);
				} else {
						changeEvent = new ObjectCreation(currentEventKey.rev, currentEventKey.getObjectId());

						/* Must clear diff if an create event is build (Is necessary if some object
						 * rises from the dead. In such case old values are obsolete) */
					diff.clear();
				}
				nextEvent = changeEvent;
					setChangedValues(changeEvent);
				
				findNextRowAttributes();
				break;
			}
			
			case objectDeletion: {
				if (nextEvent instanceof ItemUpdate) {
					// Drop deletion, objects stays alive.
				}
				else if (nextEvent == null) {
					ItemDeletion deleteEvt = new ItemDeletion(currentEventKey.rev, currentEventKey.getObjectId());
					setDeletedValues(deleteEvt);
					nextEvent = deleteEvt;
				}
				else {
					throw new AssertionError("A deletion may only clash with an update");
				}
				
				break;
			}
			
			case attributeDeletion: {
				ItemUpdate updateEvent;
				if (nextEvent == null) {
					updateEvent = new ItemUpdate(currentEventKey.rev, currentEventKey.getObjectId(), keepOldValues);
					nextEvent = updateEvent;
				} 
				else if (nextEvent instanceof ItemUpdate) {
					updateEvent = (ItemUpdate) nextEvent;
				} 
				else if (nextEvent instanceof ItemDeletion) {
					// Ignore attribute deletion, because object is being
					// deleted with the current event.
					break;
				} 
				else {
					throw new AssertionError("Item update or deletion expected.");
				}

				String attributeName = ((AttributeEventKey) currentEventKey).attributeName;
				Object newValue = null;
				Object oldValue = diff.put(attributeName, newValue);
				updateEvent.setValue(attributeName, oldValue, newValue);
				
				break;
			}
			
			case flexAttribute: {
				ItemChange changeEvent;
				if (nextEvent == null) {
					changeEvent = new ItemUpdate(currentEventKey.rev, currentEventKey.getObjectId(), keepOldValues);
					nextEvent = changeEvent;
				}
				else if (nextEvent instanceof ItemChange) {
					changeEvent = (ItemChange) nextEvent;
				} 
				else {
					throw new AssertionError("Item change expected.");
				}

				String attributeName = ((AttributeEventKey) currentEventKey).attributeName;
				Object newValue = this.flexAttributes.getAttributeValue();
				Object oldValue = diff.put(attributeName, newValue);
				changeEvent.setValue(attributeName, oldValue, newValue);
				
				findNextFlexAttribute();
				break;
			}
			}
			
			lastEventKey = currentEventKey;
		}
	}

	private void setChangedValues(ItemChange changeEvent) throws SQLException {
		Set<String> mainAttributes = kb.getKeyAttributes(currentRowType);
		final ObjectContext contextObject = _objectContext.getObjectContext(currentRowId);
		for (int n = 0, cnt = currentRowAttributes.size(); n < cnt; n++) {
			MOAttribute attr = currentRowAttributes.get(n);
			if (attr.isSystem()) {
				continue;
			}
			String attributeName = attr.getName();
			Object newValue = rowAttributes.getValue(attr, contextObject);
			Object oldValue = diff.put(attributeName, newValue);
			changeEvent.setValue(attributeName, oldValue, newValue, !mainAttributes.contains(attributeName));
		}
	}

	/** 
	 * Installs the values of the object at deletion time into the given {@link ItemDeletion}.
	 */
	private void setDeletedValues(ItemDeletion deleteEvt) {
		Set<String> mainAttributes = kb.getKeyAttributes(currentRowType);
		for (Entry<String, Object> currentValue : diff.entrySet()) {
			Object attributeValue = currentValue.getValue();
			String attributeName = currentValue.getKey();
			deleteEvt.setValue(attributeName, attributeValue, null, !mainAttributes.contains(attributeName));
		}
	}
	
	@Override
	public void close() {
		try {
			super.close();
		} finally {
			cleanup();
		}
	}

	private void cleanup() {
		try {
			closeResourcesStep0();
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private void closeResourcesStep0() throws SQLException {
		try {
			closeRowAttributes();
		} finally {
			closeResourcesStep1();
		}
	}

	private void closeRowAttributes() throws SQLException {
		if (rowAttributes == null) {
			return;
		}
		ItemResult result = rowAttributes;
		rowAttributes = null;
		result.close();
	}

	private void closeResourcesStep1() throws SQLException {
		try {
			closeTypes();
		} finally {
			closeResourcesStep2();
		}
	}

	private void closeTypes() throws SQLException {
		if (touchedTypes == null) {
			return;
		}
		TypeResult result = touchedTypes;
		touchedTypes = null;
		result.close();
	}

	private void closeResourcesStep2() throws SQLException {
		if (flexAttributes == null) {
			return;
		}
		AttributeItemResult result = flexAttributes;
		flexAttributes = null;
		result.close();
	}

	/**
	 * Kind of an event identified by an {@link EventKey} or {@link AttributeEventKey}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static enum EventKeyKind {
		
		/**
		 * Assignment of attributes stored in a database row.
		 */
		rowAttributes, 
		
		/**
		 * Potential object deletion (end point of a object row).
		 */
		objectDeletion, 
		
		/**
		 * Potential deletion of a single flexible attribute (end point of a flexible attribute row).
		 */
		attributeDeletion,
		
		/**
		 * Assignment of a single flexible attribute stored in the generic table for flexible attributes.
		 */
		flexAttribute; 
		
	}
	
	/**
	 * Atomic event that identifies a single row from the object table.
	 * 
	 * @see AttributeEventKey
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class EventKey implements Comparable<EventKey> {
		EventKeyKind kind;
		private ObjectBranchId id;
		long rev;
		
		public EventKey(EventKeyKind kind, ObjectBranchId id, long rev) {
			this.kind = kind;
			this.id = id;
			this.rev = rev;
		}
		
		public ObjectBranchId getObjectId() {
			return id;
		}
		
		public boolean sameObject(ItemEvent other) {
			boolean typeCompare = this.getTypeName().equals(other.getObjectType().getName());
			if (! typeCompare) {
				return false;
			} else {
				if (this.getBranchId() != other.getOwnerBranch()) {
					return false;
				} else {
					return this.getObjectName().equals(other.getObjectName());
				}
			}
		}

		public boolean sameObject(EventKey other) {
			boolean typeCompare = this.getTypeName().equals(other.getTypeName());
			if (! typeCompare) {
				return false;
			} else {
				if (this.getBranchId() != other.getBranchId()) {
					return false;
				} else {
					return this.getObjectName().equals(other.getObjectName());
				}
			}
		}

		public boolean sameRev(EventKey other) {
			return this.rev == other.rev;
		}
		
		@Override
		public int compareTo(EventKey other) {
			return EventKey.compareKeys(this, other, true);
		}

		/**
		 * Compares two {@link EventKey}s in the following order
		 * 
		 * <ol>
		 * <li>natural order on {@link #getTypeName() type name}. This is necessary by the current
		 * usage of {@link EventKey}. To detect an {@link ItemUpdate} the lines are read. If max
		 * revision is less than infinity than an {@link ItemDeletion} is supposed unless the
		 * previous event is an {@link ItemUpdate}. In this case the {@link ItemDeletion} is
		 * ignored. If the keys are not sorted by this order, it is possible that (after a type was
		 * completely processed) there is still a (wrong) {@link ItemDeletion} to process, but an
		 * event of a different type is processed first, so that the deletion is executed later.</li>
		 * 
		 * <li>natural order on {@link #getBranchId() branch}</li>
		 * 
		 * <li>natural order on {@link #getObjectName() identifier}</li>
		 * 
		 * <li>revision (depending on <code>ascendingRev</code>)</li>
		 * 
		 * <li>natural order on kind of this key</li>
		 * </ol>
		 * 
		 * @param ascendingRev
		 *        whether the revision should be included ascending or descending
		 */
		public static int compareKeys(EventKey key1, EventKey key2, boolean ascendingRev) {
			// Type
			int typeCompare = key1.getTypeName().compareTo(key2.getTypeName());
			if (typeCompare != 0) {
				return typeCompare;
			} else {
				// Branch
				long thisBranch = key1.getBranchId();
				long otherBranch = key2.getBranchId();
				if (thisBranch < otherBranch) {
					return -1;
				}
				else if (thisBranch > otherBranch) {
					return 1;
				}
				else {
					// Identifier
					int idCompare = key1.getObjectName().compareTo(key2.getObjectName());
					if (idCompare != 0) {
						return idCompare;
					} else {
						// Revision
						long thisRev = key1.rev;
						long otherRev = key2.rev;
						if (thisRev < otherRev) {
							return ascendingRev ? -1 : 1;
						}
						else if (thisRev > otherRev) {
							return ascendingRev ? 1 : -1;
						}
						else {
							// Kind
							return key1.kind.compareTo(key2.kind);
						}
					}
				}
			}
		}

		private String getTypeName() {
			return id.getObjectType().getName();
		}

		long getBranchId() {
			return id.getBranchId();
		}

		TLID getObjectName() {
			return id.getObjectName();
		}
		
		@Override
		public final String toString() {
			StringBuilder buffer = new StringBuilder(this.getClass().getSimpleName());
			buffer.append('(');
			appendProperties(buffer);
			buffer.append(')');
			return buffer.toString();
		}

		protected void appendProperties(StringBuilder buffer) {
			buffer.append("rev=");
			buffer.append(rev);
			buffer.append(", branch=");
			buffer.append(getBranchId());
			buffer.append(", type=");
			buffer.append(getTypeName());
			buffer.append(", name=");
			buffer.append(getObjectName());
			buffer.append(", kind=");
			buffer.append(kind);
		}
	}
	
	/**
	 * Compares {@link EventKey} in the same way the natural order comparison
	 * does, but reverse the order of revisions.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class RevertComparator implements Comparator<EventKey> {
		
		public static final RevertComparator INSTANCE = new RevertComparator();
		
		private RevertComparator() {
			// singleton instance
		}

		@Override
		public int compare(EventKey o1, EventKey o2) {
			return EventKey.compareKeys(o1, o2, false);
		}

	}
	
	/**
	 * Atomic event that identifies a single row from the flex attributes table. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class AttributeEventKey extends EventKey {
		/*package protected*/ final String attributeName;
		
		public AttributeEventKey(EventKeyKind eventType, ObjectBranchId objectId, long deleteRev, String attributeName) {
			super(eventType, objectId, deleteRev);
			this.attributeName = attributeName;
		}
		
		@Override
		protected void appendProperties(StringBuilder buffer) {
			super.appendProperties(buffer);
			buffer.append(", attribute=");
			buffer.append(attributeName);
		}

	}
	
}
