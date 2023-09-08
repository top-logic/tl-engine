/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Utilities for {@link KnowledgeObject}s and {@link KnowledgeAssociation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeItemUtil {

	/**
	 * Navigates the given link to its destination, even if the destination
	 * object has been deleted in the current context.
	 * 
	 * @param deletedById
	 *        Deleted objects in the current context, indexed by {@link KnowledgeItem#tId() identity}.
	 * @param link
	 *        The link to navigate.
	 * @return the destination object of the given link.
	 */
	public static KnowledgeItem getDestinationObject(Map<?, ? extends KnowledgeItem> deletedById,
			KnowledgeAssociation link) throws InvalidLinkException {
		ObjectKey destId = link.getDestinationIdentity();
		
		// If the destination object is removed in the same commit, the
		// association cannot be navigated to the destination object. The
		// workaround is to lookup the destination by ID in the context's
		// removed object map.
		KnowledgeItem destination = deletedById.get(destId);
		if (destination == null) {
			// The target was not deleted, the association can be navigated.
			destination = link.getDestinationObject();
		}
		return destination;
	}

	/**
	 * Navigates the given link to its source, even if the source
	 * object has been deleted in the current context.
	 * 
	 * @param deletedById
	 *        Deleted objects in the current context, indexed by {@link KnowledgeItem#tId() identity}.
	 * @param link
	 *        The link to navigate.
	 * @return the source object of the given link.
	 */
	public static KnowledgeItem getSourceObject(Map<?, ? extends KnowledgeItem> deletedById, KnowledgeAssociation link)
			throws InvalidLinkException {
		Object destKey = link.getSourceIdentity();
		
		// If the source object is removed in the same commit, the
		// association cannot be navigated to the source object. The
		// workaround is to lookup the source by ID in the context's
		// removed object map.
		KnowledgeItem source = deletedById.get(destKey);
		if (source == null) {
			// The source was not deleted, the association can be navigated.
			source = link.getSourceObject();
		}
		return source;
	}

	/**
	 * Checks whether the given object is not <code>null</code> and a legacy
	 * {@link KnowledgeAssociation}
	 */
	@Deprecated
	public static boolean instanceOfKnowledgeAssociation(Object o) {
		if (o instanceof KnowledgeItem) {
			return hasLegacyAssociationType((KnowledgeItem) o);
		}
		return false;
	}

	private static boolean hasLegacyAssociationType(KnowledgeItem item) {
		MetaObject type = item.tTable();
		KnowledgeBase kb = item.getKnowledgeBase();
		return type.isSubtypeOf(BasicTypes.getKnowledgeAssociationType(kb));
	}

	/**
	 * Checks whether the given object is not <code>null</code> and a legacy
	 * {@link KnowledgeAssociation}
	 */
	@Deprecated
	public static boolean instanceOfKnowledgeObject(Object o) {
		if (o instanceof KnowledgeItem) {
			// everything has type KnowledgeObject and KnowledgeAssociation is a subtype of
			// KnowledgObject so not KnowledgeAssociation is enough
			return !hasLegacyAssociationType((KnowledgeItem) o);
		}
		return false;
	}

	/**
	 * Checks whether the given {@link MetaObject} is an object type but not a legacy association
	 * type.
	 */
	public static boolean isPureObjectType(KnowledgeBase kb, MetaObject mo) {
		MOClass objectType = BasicTypes.getKnowledgeObjectType(kb);
		MOClass associationType = BasicTypes.getKnowledgeAssociationType(kb);
		return isPureObjectType(mo, objectType, associationType);
	}

	/**
	 * Checks whether the given {@link MetaObject} is an object type but not a legacy association
	 * type.
	 */
	public static boolean isPureObjectType(MetaObject mo, MetaObject objectType, MetaObject associationType) {
		return mo.isSubtypeOf(objectType) && !mo.isSubtypeOf(associationType);
	}

}
