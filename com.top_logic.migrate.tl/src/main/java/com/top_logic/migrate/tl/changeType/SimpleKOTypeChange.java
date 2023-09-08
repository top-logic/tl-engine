/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.changeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.migrate.tl.LoggingRewriter;

/**
 * Change KOType of certain elements. Matching objects to change must be independent from the branch
 * of the objects.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SimpleKOTypeChange extends LoggingRewriter {

	private final MetaObject _targetType;

	private final Map<String, Set<Object>> _rewrittenObjectsByType = new HashMap<>();

	private final boolean _newTargetType;

	/**
	 * Creates a new {@link SimpleKOTypeChange}.
	 * 
	 * @param srcTypes
	 *        Names of the source types potentially rewritten.
	 * @param targetType
	 *        Type to rewrite objects to.
	 * @param newTargetType
	 *        Whether the target type is a new type in the destination database.
	 */
	public SimpleKOTypeChange(Protocol log, Iterable<String> srcTypes, MetaObject targetType, boolean newTargetType) {
		super(log);
		for (String srcType : srcTypes) {
			_rewrittenObjectsByType.put(srcType, new HashSet<>());
		}
		if (_rewrittenObjectsByType.size() == 0) {
			throw new IllegalArgumentException("There must be at least one type to rewrite.");
		}
		_targetType = targetType;
		_newTargetType = newTargetType;
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		if (matches(event)) {
			ObjectBranchId currentObjectId = event.getObjectId();
			_rewrittenObjectsByType.get(typeName(event)).add(currentObjectId.getObjectName());
			// new id is set in visitItemEvent.
		}
		return super.visitCreateObject(event, arg);
	}

	private boolean matches(ItemCreation event) {
		if (!correctType(event)) {
			return false;
		}
		return matches(event.getObjectType(), event.getObjectName(), event.getValues());
	}

	private boolean correctType(ItemEvent event) {
		return srcTypes().contains(typeName(event));
	}

	private Set<String> srcTypes() {
		return _rewrittenObjectsByType.keySet();
	}

	private String typeName(ItemEvent event) {
		return event.getObjectType().getName();
	}

	private ObjectBranchId newID(ObjectBranchId currentObjectId) {
		return new ObjectBranchId(currentObjectId.getBranchId(), _targetType, currentObjectId.getObjectName());
	}

	private ObjectKey newID(ObjectKey id) {
		return KBUtils.createObjectKey(id.getBranchContext(), id.getHistoryContext(), _targetType, id.getObjectName());
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		rewriteValues(event.getValues());
		return super.visitItemChange(event, arg);
	}

	private void rewriteValues(Map<String, Object> values) {
		if (values == null) {
			return;
		}
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof ObjectKey) {
				entry.setValue(transformToFormerRewrittenId((ObjectKey) value));
			}
		}
	}

	@Override
	protected Object visitItemEvent(ItemEvent event, Void arg) {
		event.setObjectId(transformToFormerRewrittenId(event.getObjectId()));
		return super.visitItemEvent(event, arg);
	}

	@Override
	public Object visitBranch(BranchEvent event, Void arg) {
		Set<String> branchedTypes = event.getBranchedTypeNames();
		Iterator<String> srcTypes = srcTypes().iterator();
		boolean containsFirst = branchedTypes.contains(srcTypes.next());
		while (srcTypes.hasNext()) {
			if (containsFirst != branchedTypes.contains(srcTypes.next())) {
				throw _log.fatal("Branch event '" + event + "' must contain either all or none of the source types '"
					+ srcTypes() + "'.");
			}
		}
		if (_newTargetType) {
			if (containsFirst) {
				branchedTypes.add(_targetType.getName());
			}
		} else {
			if (containsFirst) {
				if (!branchedTypes.contains(_targetType.getName())) {
					throw _log.fatal("Branch event '" + event + "' contains all source types '" + srcTypes()
						+ "'. As the target type already exists, it must also be branched.");
				}
			} else {
				if (branchedTypes.contains(_targetType.getName())) {
					throw _log.fatal("Branch event '" + event + "' contains no source types '" + srcTypes()
						+ "'. As the target type already exists, it must also not be branched.");
				}
			}
		}
			
		return super.visitBranch(event, arg);
	}

	/**
	 * @see #transformToFormerRewrittenId(ObjectBranchId)
	 */
	private ObjectKey transformToFormerRewrittenId(ObjectKey id) {
		Set<Object> rewrittenObjects = _rewrittenObjectsByType.get(id.getObjectType().getName());
		if (rewrittenObjects == null) {
			// not a rewritten type
			return id;
		}
		// Can not remove object name as such an object can still exists on different branch.
		if (!rewrittenObjects.contains(id.getObjectName())) {
			// not a rewritten object
			return id;
		}
		return newID(id);
	}

	/**
	 * @see #transformToFormerRewrittenId(ObjectKey)
	 */
	ObjectBranchId transformToFormerRewrittenId(ObjectBranchId id) {
		Set<Object> rewrittenObjects = _rewrittenObjectsByType.get(id.getObjectType().getName());
		if (rewrittenObjects == null) {
			// not a rewritten type
			return id;
		}
		// Can not remove object name as such an object can still exists on different branch.
		if (!rewrittenObjects.contains(id.getObjectName())) {
			// not a rewritten object
			return id;
		}
		return newID(id);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		rewriteValues(event.getOldValues());
		return super.visitUpdate(event, arg);
	}

	/**
	 * @see GenericKOTypeChange.Matcher#matches(MetaObject, Object, Map)
	 */
	protected abstract boolean matches(MetaObject objectType, Object objectName, Map<String, Object> creationValues);

}

