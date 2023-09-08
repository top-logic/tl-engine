/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.changeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.migrate.tl.LoggingRewriter;

/**
 * {@link LoggingRewriter} that changes the {@link MetaObject} of objects that
 * {@link #matches(ItemCreation) matches}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ChangeKOType extends LoggingRewriter {

	private final MetaObject _targetType;

	/**
	 * Value contain [&lt;newId&gt;, &lt;validFrom&gt;, &lt;validTo&gt;, ..., &lt;validFrom&gt;,
	 * &lt;validTo&gt;]
	 */
	private final HashMap<ObjectBranchId, List<Object>> _replacedIds = new HashMap<>();

	public ChangeKOType(Protocol log, MetaObject targetType) {
		super(log);
		_targetType = targetType;
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		if (matches(event)) {
			ObjectBranchId currentObjectId = event.getObjectId();
			objectCreated(event.getRevision(), currentObjectId);
			// new Id is set in visitItemEvent
		}
		return super.visitCreateObject(event, arg);
	}

	private void objectCreated(long revision, ObjectBranchId currentObjectId) {
		List<Object> list = _replacedIds.get(currentObjectId);
		if (list == null) {
			list = new ArrayList<>();
			list.add(newID(currentObjectId));
			_replacedIds.put(currentObjectId, list);
		}
		assert !isAlive(list);
		list.add(revision);
	}

	private boolean isAlive(List<Object> list) {
		return list.size() % 2 == 0;
	}

	private ObjectBranchId newID(ObjectBranchId currentObjectId) {
		return new ObjectBranchId(currentObjectId.getBranchId(), _targetType, currentObjectId.getObjectName());
	}

	@Override
	public Object visitBranch(BranchEvent event, Void arg) {
		Set<String> branchedTypeNames = event.getBranchedTypeNames();
		long baseBranchId = event.getBaseBranchId();
		long baseRevision = event.getBaseRevisionNumber();
		Map<ObjectBranchId, List<Object>> newObjects = new HashMap<>();
		for (Entry<ObjectBranchId, List<Object>> entry : _replacedIds.entrySet()) {
			ObjectBranchId branchedObject = entry.getKey();
			if (branchedObject.getBranchId() != baseBranchId) {
				// object live on different branch
				continue;
			}
			if (!branchedTypeNames.contains(branchedObject.getObjectType().getName())) {
				// object type is not branched.
				continue;
			}

			boolean alive = isAliveAt(baseRevision, entry.getValue());
			if (alive) {
				ObjectBranchId branchedObjectId =
					new ObjectBranchId(event.getBranchId(), branchedObject.getObjectType(),
						branchedObject.getObjectName());
				ArrayList<Object> branchedObjectLifePeriod = new ArrayList<>();
				branchedObjectLifePeriod.add(newID(branchedObjectId));
				branchedObjectLifePeriod.add(event.getRevision());
				newObjects.put(branchedObjectId, branchedObjectLifePeriod);
			}
		}
		if (!newObjects.isEmpty()) {
			/* Some of the alive rewritten elements are branched. In the target system the same
			 * objects must be branched. Therefore the new target type must be added. */
			branchedTypeNames.add(_targetType.getName());
		}
		_replacedIds.putAll(newObjects);
		return super.visitBranch(event, arg);
	}

	private boolean isAliveAt(long revision, List<Object> lifePeriod) {
		boolean alive = false;
		int index = 1;
		while (true) {
			if (index == lifePeriod.size()) {
				// object is currently dead
				assert !alive;
				break;
			} else {
				// check revision is after start of life
				long startLife = ((Long) lifePeriod.get(index++)).longValue();
				if (startLife <= revision) {
					alive = true;
				} else {
					break;
				}
			}

			if (index == lifePeriod.size()) {
				// object is currently alive
				assert alive;
				break;
			} else {
				// check revision is before end of life
				long endLife = ((Long) lifePeriod.get(index++)).longValue();
				if (revision <= endLife) {
					break;
				} else {
					alive = false;
				}
			}
		}
		return alive;
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		// fetch id to find correct map entry
		ObjectBranchId objectId = event.getObjectId();
		// first call super to update key before mark them as deleted.
		Object result = super.visitDelete(event, arg);
		// mark object as deleted
		objectDeleted(event.getRevision(), objectId);
		return result;
	}

	private void objectDeleted(long revision, ObjectBranchId objectId) {
		List<Object> list = _replacedIds.get(objectId);
		assert list != null;
		assert isAlive(list);
		list.add(revision);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		updateValues(event.getOldValues());
		return super.visitUpdate(event, arg);
	}

	private ObjectBranchId getNewId(ObjectBranchId objectId) {
		List<Object> list = _replacedIds.get(objectId);
		if (list == null) {
			return objectId;
		}
		assert isAlive(list);
		return (ObjectBranchId) list.get(0);
	}

	private ObjectKey getNewId(ObjectKey objectId) {
		List<Object> list = _replacedIds.get(ObjectBranchId.toObjectBranchId(objectId));
		if (list == null) {
			return objectId;
		}
		/* A check could be done that the list is alive at history time, but this includes valid
		 * data. (The migration would break, no check is done due to shit in shit out.) */
		ObjectBranchId newId = (ObjectBranchId) list.get(0);
		return newId.toObjectKey(objectId.getHistoryContext());
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		updateValues(event.getValues());
		return super.visitItemChange(event, arg);
	}

	private void updateValues(Map<String, Object> values) {
		if (values == null) {
			return;
		}
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof ObjectKey) {
				entry.setValue(getNewId((ObjectKey) value));
			}
		}
	}

	@Override
	protected Object visitItemEvent(ItemEvent event, Void arg) {
		event.setObjectId(getNewId(event.getObjectId()));
		return super.visitItemEvent(event, arg);
	}

	/**
	 * Whether the given {@link ItemCreation} should be moved to a different type.
	 */
	protected abstract boolean matches(ItemCreation event);

}
