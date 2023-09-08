/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * Static facade to the {@link HistoryManager} for navigating {@link TLObject}s in time.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperHistoryUtils {

    /**
     * @see HistoryUtils#getBranch(KnowledgeItem)
     */
	public static Branch getBranch(TLObject anObject) {
		KnowledgeItem wrappedObject = anObject.tHandle();
		return HistoryUtils.getBranch(wrappedObject);
    }
    
    /**
     * @see HistoryUtils#getRevision(KnowledgeItem)
	 */
	public static Revision getRevision(TLObject anObject) {
		return anObject.tRevision();
    }
    
    /**
     * @see HistoryManager#getItemIdentity(KnowledgeItem, Object)
     */
	public static ObjectReference getUnversionedIdentity(TLObject anObject) {
		KnowledgeItem wrappedObject = anObject.tHandle();
		return HistoryUtils.getItemIdentity(wrappedObject, anObject);
    }

	/**
	 * Compares the two objects unversioned.
	 * 
	 * @param first
	 *        is allowed to be <code>null</code>
	 * @param second
	 *        is allowed to be <code>null</code>
	 */
	public static boolean equalsUnversioned(TLObject first, TLObject second) {
		if (first == second) {
			return true;
		}
		if (first == null || second == null) {
			return false;
		}
		return getUnversionedIdentity(first).equals(getUnversionedIdentity(second));
	}

	/**
	 * Compares the two objects unversioned and branch ignorant.
	 * 
	 * @param first
	 *        is allowed to be <code>null</code>
	 * @param second
	 *        is allowed to be <code>null</code>
	 */
	public static boolean equalsObjectName(TLObject first, TLObject second) {
		if (first == null) {
			return second == null;
		}
		if (second == null) {
			return false;
		}
		return KBUtils.getWrappedObjectName(first).equals(KBUtils.getWrappedObjectName(second));
	}

    /**
     * @see HistoryManager#getLastUpdate(KnowledgeItem)
	 */
	public static Revision getLastUpdate(TLObject anObject) {
		KnowledgeItem wrappedObject = anObject.tHandle();
    	return HistoryUtils.getLastUpdate(wrappedObject);
    }
    
    /**
     * @see HistoryManager#getCreateRevision(KnowledgeItem)
     */
	public static Revision getCreateRevision(TLObject anObject) {
		KnowledgeItem wrappedObject = anObject.tHandle();
    	return HistoryUtils.getCreateRevision(wrappedObject);
    }
    
    /**
	 * Whether the given object is a current object.
	 * 
	 * @since TL_5.7
	 * 
	 * @param anObject
	 *        The object to test.
	 * @return Whether the given object is from the {@link Revision#CURRENT}.
	 * 
	 * @see #getCurrent(TLObject)
	 */
	public static boolean isCurrent(TLObject anObject) {
		return isInRevision(Revision.CURRENT, anObject);
    }

	/**
	 * Whether the history context of the given object is the given
	 * {@link Revision}.
	 * 
     * @since TL_5.7
     * 
	 * @param revision
	 *        The {@link Revision} to test the given object against.
	 * @param anObject
	 *        The object to test.
	 * @return Whether the given object is from the given {@link Revision}.
	 */
	public static boolean isInRevision(Revision revision, TLObject anObject) {
		return revision.getCommitNumber() == anObject.tHistoryContext();
	}

	/**
	 * Whether the branch context of the given object is the given
	 * {@link Branch}.
	 * 
     * @since TL_5.7
     * 
	 * @param branch
	 *        The {@link Branch} to test the given object against.
	 * @param anObject
	 *        The object to test.
	 * @return Whether the given object is from the given {@link Branch}.
	 */
	public static boolean isInBranch(Branch branch, TLObject anObject) {
		{
			KnowledgeItem wrappedObject = anObject.tHandle();
			return wrappedObject.getBranchContext() == branch.getBranchId();
		}
	}

	/**
	 * Resolve the given argument in the {@link Revision#CURRENT current revision}.
	 * 
	 * Short-cut for {@link #getWrapper(Revision, TLObject)} with {@link Revision#CURRENT} as
	 * argument.
	 * 
	 * @since TL_5.7
	 * 
	 * @param anObject
	 *        the historic object.
	 * @return The given object in the current {@link Revision}.
	 * 
	 * @see #isCurrent(TLObject)
	 */
	public static <T extends TLObject> T getCurrent(T anObject) {
		if (isCurrent(anObject)) {
			return filterInvalid(anObject);
		}
		{
			KnowledgeItem wrappedObject = anObject.tHandle();
			KnowledgeObject historicObject = (KnowledgeObject) HistoryUtils.getKnowledgeItem(Revision.CURRENT, wrappedObject);
			return WrapperFactory.getWrapper(historicObject);
		}
	}

	/**
	 * @see HistoryUtils#getKnowledgeItem(Revision, KnowledgeItem)
	 */
	public static <T extends TLObject> T getWrapper(Revision revision, T anObject) {
		if (isInRevision(revision, anObject)) {
			return filterInvalid(anObject);
		}
		{
			KnowledgeItem wrappedObject = anObject.tHandle();
			KnowledgeObject historicObject = (KnowledgeObject) HistoryUtils.getKnowledgeItem(revision, wrappedObject);
			return WrapperFactory.getWrapper(historicObject);
		}
	}

	/**
	 * Return the given wrappers in the requested revision.
	 * 
	 * @param revision
	 *        The requested revision.
	 * @param someObjects
	 *        The wrappers to be looked up.
	 * 
	 * @return The wrappers in the historic version.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends TLObject> Collection<W> getWrappers(Revision revision, Collection<W> someObjects) {
		int numberWrappers = someObjects.size();
		switch (numberWrappers) {
			case 0: {
				return Collections.emptyList();
			}
			case 1: {
				W historicWrapper = getWrapper(revision, someObjects.iterator().next());
				return CollectionUtil.singletonOrEmptyList(historicWrapper);
			}
			default: {
				ArrayList<ObjectKey> historicKeys = new ArrayList<>(numberWrappers);
				long historyContext;
				if (revision.isCurrent()) {
					historyContext = Revision.CURRENT_REV;
				} else {
					historyContext = revision.getCommitNumber();
				}
				KnowledgeBase commonKB = null;
				for (TLObject wrapper : someObjects) {
					KnowledgeItem wrappedItem = wrapper.tHandle();
					KnowledgeBase kb = wrappedItem.getKnowledgeBase();
					if (commonKB == null) {
						commonKB = kb;
					} else {
						if (commonKB != kb) {
							StringBuilder differentKB = new StringBuilder();
							differentKB.append("Can not fetch wrappers from different KB's '");
							differentKB.append(commonKB);
							differentKB.append("' vs '");
							differentKB.append(kb);
							differentKB.append("'.");
							throw new IllegalArgumentException(differentKB.toString());
						}
					}
					ObjectKey origKey = wrappedItem.tId();
					historicKeys.add(KBUtils.createHistoricObjectKey(origKey, historyContext));
				}
				BulkIdLoad loader = new BulkIdLoad(commonKB);
				loader.addAll(historicKeys);
				List<KnowledgeItem> items = loader.load();
				// Need actually KnowledgeObjects.
				List objects = items;
				return (Collection<W>) WrapperFactory.getWrappersForKOsGeneric(objects);
			}
		}
	}
	
	/**
	 * @see HistoryUtils#getKnowledgeItem(Branch, KnowledgeItem)
	 */
	public static <T extends TLObject> T getWrapper(Branch branch, T anObject) {
		if (isInBranch(branch, anObject)) {
			return filterInvalid(anObject);
		}
		
		{
			KnowledgeItem wrappedObject = anObject.tHandle();
			KnowledgeObject historicObject = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch, wrappedObject);
			return (T) WrapperFactory.getWrapper(historicObject);
		}
	}
	
	/**
	 * @see HistoryUtils#getKnowledgeItem(Branch, Revision, KnowledgeItem)
	 */
	public static <T extends TLObject> T getWrapper(Branch aBranch, Revision aRevision, T anObject) {
		if (isInBranch(aBranch, anObject) && isInRevision(aRevision, anObject)) {
			return filterInvalid(anObject);
		}
		{
			KnowledgeItem wrappedObject = anObject.tHandle();
			KnowledgeObject correspondingObject = (KnowledgeObject) HistoryUtils.getKnowledgeItem(aBranch, aRevision, wrappedObject);
			
			return (T) WrapperFactory.getWrapper(correspondingObject);
		}
	}
	
	private static <T extends TLObject> T filterInvalid(T anObject) {
		if (!anObject.tValid()) {
			// Does not exist in the context revision.
			return null;
		}
		return anObject;
	}

    /**
     * @see HistoryManager#getObjectsByAttribute(Branch, Revision, String, String, Object)
	 */
    public static List<Wrapper> getObjectsByAttribute(Revision revision, String aType, String attributeName, Object attributeValue) throws DataObjectException {
    	List<KnowledgeObject> historicObjects = CollectionUtil.dynamicCastView(KnowledgeObject.class, HistoryUtils.getObjectsByAttribute(revision, aType, attributeName, attributeValue));

    	return WrapperFactory.getWrappersForKOsGeneric(historicObjects);
    }

	/** 
	 * @see HistoryManager#getObjectsByAttributes(Branch, Revision, String, String[], Object[])
	 */
	public static List<Wrapper> getWrappersByAttributes(Branch branch, Revision aRevision, String type, String[] attrs, Object[] values) {
		List<KnowledgeObject> historicObjects = CollectionUtil.dynamicCastView(KnowledgeObject.class, HistoryUtils.getObjectsByAttributes(branch, aRevision, type, attrs, values));    		

		return WrapperFactory.getWrappersForKOsGeneric(historicObjects);
	}

}
