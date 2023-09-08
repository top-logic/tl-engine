/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._20653;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;

/**
 * {@link RewritingEventVisitor} that sets the sort order attribute to
 * {@link StructuredElementWrapper#CHILD_ASSOCIATION structure child} associations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructureChildOrder extends RewritingEventVisitor {

	private static final String SOURCE_SORT_ORDER = "sortOrder";

	private static final String TARGET_SORT_ORDER = ListStorage.SORT_ORDER;

	/**
	 * All {@link MetaObject#getName() mo names} of types containing structured elements, i.e. the
	 * name of {@link StructuredElement#KO_TYPE} and all sub classes. It is expected that this is a
	 * duper set of all such {@link MetaObject} in the source database.
	 */
	private final Set<String> _structuredElementTypes = new HashSet<>();

	private static final String CHILD_ASSOCIATION = StructuredElementWrapper.CHILD_ASSOCIATION;

	/**
	 * Mapping of the {@link ObjectBranchId} of a {@link StructuredElement} type to its create
	 * event.
	 */
	private final Map<ObjectBranchId, ObjectCreation> _createdStructuredElements = new HashMap<>();

	/**
	 * Mapping of the {@link ObjectBranchId} of a {@link #CHILD_ASSOCIATION} type to its create
	 * event.
	 */
	private final Map<ObjectBranchId, ObjectCreation> _createdChildAssociations = new HashMap<>();

	/**
	 * Mapping of the {@link ObjectBranchId} of a {@link StructuredElement} to the
	 * {@link ObjectBranchId} of the corresponding {@link #CHILD_ASSOCIATION} which has the element
	 * as "destination".
	 */
	private final BidiMap<ObjectBranchId, ObjectBranchId> _childAssociationForSE = new BidiHashMap<>();

	private final List<ItemEvent> _additionalEvents = new ArrayList<>();

	private final Map<ObjectBranchId, Double> _lastSortOrderId = new HashMap<>();

	private Log _log = NoProtocol.INSTANCE;

	/**
	 * Initialises the {@link StructureChildOrder} with the given target {@link MORepository}.
	 */
	@Inject
	public void initMORepository(MORepository repository) throws UnknownTypeException {
		MetaObject structuredElementType = repository.getType(StructuredElement.KO_TYPE);
		for (MetaObject anyMO : repository.getMetaObjects()) {
			if (anyMO.isSubtypeOf(structuredElementType)) {
				_structuredElementTypes.add(anyMO.getName());
			}
		}
	}

	/**
	 * Initialises the log to log errors to.
	 */
	@Inject
	public void setLog(Log log) {
		_log = log;
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		List<ObjectCreation> creations = cs.getCreations();

		for (ObjectCreation creation : creations) {
			ObjectBranchId objectId = creation.getObjectId();
			String typeName = objectId.getObjectType().getName();
			if (_structuredElementTypes.contains(typeName)) {
				_createdStructuredElements.put(objectId, creation);
				_lastSortOrderId.put(objectId, (Double) creation.getValues().get(SOURCE_SORT_ORDER));
			} else if (CHILD_ASSOCIATION.equals(typeName)) {
				_createdChildAssociations.put(objectId, creation);
			}
		}
		for (ObjectCreation childAssociation : _createdChildAssociations.values()) {
			ObjectKey childID =
				(ObjectKey) childAssociation.getValues().get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
			ObjectBranchId destId = ObjectBranchId.toObjectBranchId(childID);
			ObjectCreation createSE = _createdStructuredElements.remove(destId);
			Double sortOrder;
			if (createSE != null) {
				sortOrder = (Double) createSE.getValues().remove(SOURCE_SORT_ORDER);
			} else {
				sortOrder = _lastSortOrderId.get(ObjectBranchId.toObjectBranchId(childID));
			}
			Integer targetSortOrder = getTargetSortOrder(sortOrder);
			childAssociation.setValue(TARGET_SORT_ORDER, null, targetSortOrder);

			ObjectBranchId clash = _childAssociationForSE.put(destId, childAssociation.getObjectId());
			if (clash != null) {
				_childAssociationForSE.put(destId, clash);
				_log.error("Object with id " + destId + " has more than one association: " + clash + ", "
					+ childAssociation.getObjectId());
			}
		}

		_createdChildAssociations.clear();
		_createdStructuredElements.clear();
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		_childAssociationForSE.removeValue(event.getObjectId());
		_lastSortOrderId.remove(event.getObjectId());
		return super.visitDelete(event, arg);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		ObjectBranchId touchedItem = event.getObjectId();
		if (!_structuredElementTypes.contains(touchedItem.getObjectType().getName())) {
			return super.visitUpdate(event, arg);
		}
		Double newSortOrder = (Double) event.getValues().remove(SOURCE_SORT_ORDER);
		Map<String, Object> oldValues = event.getOldValues();
		Double oldSortOrder;
		if (oldValues != null) {
			oldSortOrder = (Double) oldValues.remove(SOURCE_SORT_ORDER);
		} else {
			oldSortOrder = null;
		}

		if (newSortOrder != null) {
			_lastSortOrderId.put(touchedItem, newSortOrder);
			ObjectBranchId objectId = _childAssociationForSE.get(touchedItem);
			if (objectId != null) {
				ItemUpdate linkUpdate = new ItemUpdate(event.getRevision(), objectId, oldValues != null);
				linkUpdate.setValue(TARGET_SORT_ORDER, getTargetSortOrder(oldSortOrder),
					getTargetSortOrder(newSortOrder));
				_additionalEvents.add(linkUpdate);
			} else {
				// No association for touchedItem
				_log.error("No created " + CHILD_ASSOCIATION + " association found for destination " + touchedItem);
			}
		}
		return super.visitUpdate(event, arg);
	}

	@Override
	protected void processCommit(ChangeSet cs) {
		cs.mergeAll(_additionalEvents);
		_additionalEvents.clear();
		super.processCommit(cs);
	}

	private static Integer getTargetSortOrder(Double sortOrder) {
		if (sortOrder == null) {
			return null;
		}
		double d = (sortOrder.doubleValue() + 10000d) / 20000d;
		return (int) (d * Integer.MAX_VALUE);
	}

}

