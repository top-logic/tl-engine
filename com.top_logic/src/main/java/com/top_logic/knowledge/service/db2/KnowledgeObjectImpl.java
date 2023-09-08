/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;


/**
 * Static mix-in implementation of the {@link KnowledgeObjectInternal} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class KnowledgeObjectImpl extends KnowledgeItemImpl {

	/**
	 * @see KnowledgeObjectInternal#delete()
	 */
	static void delete(DBKnowledgeItem self) throws DataObjectException {
		DBKnowledgeBase kb = self.getKnowledgeBase();
		
		// Fail early, before allocating a context.
		DBContext context = kb.getCurrentDBContext();
		checkAccess(self, context);
		
		if (context == null) {
			context = kb.createDBContext();
		}
		context.removeObject(self);
	}

	static void deleteAll(DBKnowledgeBase kb, Collection<? extends KnowledgeItem> items) throws DataObjectException {
		switch (items.size()) {
			case 0: {
				return;
			}
			case 1: {
				items.iterator().next().delete();
				return;
			}
			default: {
				DBKnowledgeItem[] dbItems = new DBKnowledgeItem[items.size()];
				int numberDBItems = 0;
				DBContext context = kb.getCurrentDBContext();
				for (KnowledgeItem item : items) {
					if (item.getKnowledgeBase() != kb) {
						throw new KnowledgeBaseRuntimeException("Illegal argument: Deleting '" + item
								+ "' is not possible, because it does not belong to the KnowledgeBase '" + kb + "': "
								+ item.getKnowledgeBase());
					}

					if (item instanceof DBKnowledgeItem) {
						DBKnowledgeItem dbItem = (DBKnowledgeItem) item;

						// Fail early, before allocating a context.
						checkAccess(dbItem, context);

						if (context == null) {
							context = kb.createDBContext();
						}
						dbItems[numberDBItems++] = dbItem;
					} else {
						item.delete();
					}
				}
				if (numberDBItems > 0) {
					context.removeObjects(dbItems, numberDBItems);
				}
			}
		}

	}

	/**
	 * @see KnowledgeObjectInternal#getOutgoingAssociations()
	 */
	public static Iterator<KnowledgeAssociation> getOutgoingAssociations(KnowledgeObjectInternal source) {
		checkAccess(source);
		return source.getKnowledgeBase().getAnyOutgoingAssociations(source).iterator();
	}

	/**
	 * @see KnowledgeObjectInternal#getOutgoingAssociations(String)
	 */
	public static Iterator<KnowledgeAssociation> getOutgoingAssociations(KnowledgeObjectInternal source,
			String associationTypeName) {
		checkAccess(source);
		return source.getKnowledgeBase().getOutgoingAssociations(source, associationTypeName).iterator();
	}

	/**
	 * @see KnowledgeObjectInternal#getOutgoingAssociations(String, KnowledgeObject)
	 */
	public static Iterator<KnowledgeAssociation> getOutgoingAssociations(KnowledgeObjectInternal source,
			String associationTypeName, KnowledgeObject target) {
		checkAccess(source);
		return source.getKnowledgeBase().getAssociations(source, associationTypeName, (KnowledgeObjectInternal) target).iterator();
	}

	/**
	 * @see KnowledgeObjectInternal#getIncomingAssociations()
	 */
	public static Iterator<KnowledgeAssociation> getIncomingAssociations(KnowledgeObjectInternal target) {
		checkAccess(target);
		return target.getKnowledgeBase().getAnyIncomingAssociations(target).iterator();
	}

	/**
	 * @see KnowledgeObjectInternal#getIncomingAssociations(String)
	 */
	public static Iterator<KnowledgeAssociation> getIncomingAssociations(KnowledgeObjectInternal target,
			String associationTypeName) {
		checkAccess(target);
		return target.getKnowledgeBase().getIncomingAssociations(target, associationTypeName).iterator();
	}

	public static void notifyLocalAssociationChange(
			Map<?, ? extends AbstractAssociationCache<?, ?>> selfAssociationCaches, DBContext context,
			MOReference reference, KnowledgeItemInternal link, int changeType) {
		if (selfAssociationCaches != null) {
			for (AbstractAssociationCache<?, ?> cache : selfAssociationCaches.values()) {
				if (cache.getReferenceAttribute() != reference) {
					continue;
				}
				
				if (!cache.hasCacheType(link)) {
					continue;
				}
				
				cache.handleLocalAssociationChange(context, link, changeType);
			}
		}
	}

	public static void notifyAssociationChange(Map<?, ? extends AbstractAssociationCache<?, ?>> selfAssociationCaches,
			long revision, MOReference reference, KnowledgeItemInternal link) {
		if (selfAssociationCaches != null) {
			for (AbstractAssociationCache<?, ?> cache : selfAssociationCaches.values()) {
				if (cache.getReferenceAttribute() != reference) {
					continue;
				}
				
				if (!cache.hasCacheType(link)) {
					continue;
				}
				
				cache.handleAssociationChange(revision, link);
			}
		}
	}

	/**
	 * @see KnowledgeObjectInternal#dropLocalAssocationCache(DBContext)
	 */
	public static void dropLocalAssociationCaches(Map<?, AbstractAssociationCache<?, ?>> associationCaches,
			DBContext caller) {
		if (associationCaches == null) {
			return;
		}
		for (AbstractAssociationCache<?, ?> cache : associationCaches.values()) {
			caller.dropLocalCache(cache);
		}
	}

}
