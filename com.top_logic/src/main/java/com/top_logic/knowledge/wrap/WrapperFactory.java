/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * This creates and caches the Wrappers of a <i>TopLogic</i> System.
 *
 * Wrapper must only be created via the WrapperFactory, wish in turn
 * ensures that every Wrapper exists only once.
 *
 * To do so create a subclass of this factory and configure
 * it in the top-logic.xml configuration file.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public final class WrapperFactory {

	private WrapperFactory() {
		throw new UnsupportedOperationException("Pure static utility.");
	}

    /**
     * Get the wrapper for a given {@link KnowledgeItem}.
     *
     * A null KO will result in a null-Wrapper
     * @param    aKO    the knowledge object to wrap.
	 * @return The {@link TLObject} with the given {@link KnowledgeItem}. Null when the latter is null.
     */
	public static <T extends TLObject> T getWrapper(KnowledgeItem aKO) {
        if (aKO != null)
			return aKO.getWrapper();
        else
            return null;
    }

    /**
     * Get the wrapper for a knowledge object with the given id and potential types
     * in the default knowledge base.
     *
     * @param    anID       The id of the knowledge object
     * @param    someTypes  The types of the knowledge object to consider
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(TLID anID, String someTypes[]) {
		return getWrapper(anID, someTypes, getDefaultKB());
    }

    /**
     * Get the wrapper for a knowledge object with the given id and potential types
     * in the default knowledge base.
     *
     * @param	 aBranch	the Branch. May be <code>null</code> (current branch)
     * @param	 aRevision	the Revision. May be <code>null</code> (current revision)
     * @param    anID       The id of the knowledge object
     * @param    someTypes  The types of the knowledge object to consider
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(Branch aBranch, Revision aRevision, TLID anID, String someTypes[]) {
		return getWrapper(aBranch, aRevision, anID, someTypes, getDefaultKB());
    }


    /**
     * Get the wrapper for a knowledge object with the given id and potential types
     * in the given knowledge base.
     *
     * @param    anID       The id of the knowledge object.
     * @param    someTypes  The types of the knowledge object to consider
     * @param    aKB        The knowledge base to lookup the KO (must not be
     *                    <code>null</code>).
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(TLID anID, String someTypes[], KnowledgeBase aKB) {
		T result = null;
    	for (int i = 0; i < someTypes.length; i++) {
    		result = getWrapper(anID, someTypes[i], aKB);
    		if (result != null) {
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * Get the wrapper for a knowledge object with the given id and potential types
     * in the given knowledge base.
     *
     * @param	 aBranch	the Branch. May be <code>null</code>
     * @param	 aRevision	the Revision. May be <code>null</code>
     * @param    anID       The id of the knowledge object.
     * @param    someTypes  The types of the knowledge object to consider
     * @param    aKB        The knowledge base to lookup the KO (must not be
     *                    <code>null</code>).
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(Branch aBranch, Revision aRevision, TLID anID, String someTypes[],
			KnowledgeBase aKB) {
		T result = null;

        for (int i = 0; i < someTypes.length; i++) {
            result = getWrapper(aBranch, aRevision, anID, someTypes[i], aKB);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Get the wrapper for a knowledge object with the given id
     * in the default knowledge base.
     *
     * @param    anID     The id of the knowledge object
     * @param    aType    The type of the knowledge object
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(TLID anID, String aType) {
		return getWrapper(anID, aType, getDefaultKB());
    }
    
    /**
     * Get the wrapper for a knowledge object with the given id
     * in the default knowledge base.
     *
     * @param	 aBranch	the Branch. May be <code>null</code>
     * @param	 aRevision	the Revision. May be <code>null</code>
     * @param    anID     The id of the knowledge object
     * @param    aType    The type of the knowledge object
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(Branch aBranch, Revision aRevision, TLID anID, String aType) {
		return getWrapper(aBranch, aRevision, anID, aType, getDefaultKB());
    }

    /**
     * Get the wrapper for a knowledge object with the given id
     * in the given knowledge base.
     *
     * @param    anID     The id of the knowledge object.
     * @param    aType    The type of the knowledge object.
     * @param    aKB      The knowledge base to lookup the KO (must not be
     *                    <code>null</code>).
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(TLID anID, String aType, KnowledgeBase aKB) {
		KnowledgeObject ko = aKB.getKnowledgeObject(aType, anID);
		if (ko == null) {
			return null;
		}
		return ko.getWrapper();
    }
    
    /**
     * Get the wrapper for a knowledge object with the given id
     * in the given knowledge base.
     *
     * @param	 aBranch	The Branch. May be <code>null</code> (for context branch)
     * @param	 aRevision The Revision. May be <code>null</code> (for current version)
     * @param    anID     The id of the knowledge object.
     * @param    aType    The type of the knowledge object.
     * @param    aKB      The knowledge base to lookup the KO (must not be
     *                    <code>null</code>).
	 * @return The {@link TLObject} with the given {@link TLID}. Null when there is none.
     */
	public static <T extends TLObject> T getWrapper(Branch aBranch, Revision aRevision, TLID anID, String aType,
			KnowledgeBase aKB) {
		{
			if (aRevision == null) {
				aRevision = Revision.CURRENT;
			}

			if (aBranch == null) {
				aBranch = HistoryUtils.getHistoryManager(aKB).getContextBranch();
			}
			KnowledgeItem ko =
				HistoryUtils.getKnowledgeItem(aBranch, aRevision, aKB.getMORepository().getMetaObject(aType), anID);
			if (ko == null) {
				return null;
			}
			return ko.getWrapper();
		}
    }

    /**
	 * Replaces the elements in a list with wrappers with the corresponding object names.
	 * 
	 * @param wrappers
	 *        a list of wrappers to get their wrapped object names for
	 * @return The given list with a converted content type
	 * 
	 * @see KBUtils#getWrappedObjectName(TLObject)
	 */
	public static List<TLID> toObjectNamesInline(List<? extends TLObject> wrappers) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<TLID> result = (List) wrappers;
		for (int i = 0; i < wrappers.size(); i++) {
			result.set(i, KBUtils.getWrappedObjectName(wrappers.get(i)));
        }
		return result;
    }


    /**
     * This method returns all Wrappers of the given type from the given KnowledgeBase
     *
     * @param koType
     *            the type to get wrappers for
     * @param aKnowledgeBase
     *            to obtain the data objects from
     * @return a collection of wrappers, never <code>null</code>
     */
	public static <T extends TLObject> List<T> getWrappersByType(String koType, KnowledgeBase aKnowledgeBase) {
		Collection<KnowledgeObject> theKOs = aKnowledgeBase.getAllKnowledgeObjects(koType);
		ArrayList<T> theResult = new ArrayList<>(theKOs.size());
		for (Iterator<KnowledgeObject> theIter = theKOs.iterator(); theIter.hasNext();) {
			theResult.add(getWrapper(theIter.next()));
        }
        return theResult;
    }

    /**
     * This method returns all Wrappers of the given type from the given KnowledgeBase,
     * which have the given attribute with the given value.
     *
     * @param koType
     *            the type to get wrappers for
     * @param aKnowledgeBase
     *            to obtain the data objects from
     * @return a collection of wrappers, never <code>null</code>
     * @throws UnknownTypeException
     *             UnknownTypeException in case koType is not known
     */
	public static <T extends TLObject> List<T> getWrappersByAttribute(String koType, KnowledgeBase aKnowledgeBase,
            String anAttribute, Object aValue) throws UnknownTypeException {
		Iterator<KnowledgeItem> theIterator = aKnowledgeBase.getObjectsByAttribute(koType, anAttribute, aValue);
		ArrayList<T> theResult = new ArrayList<>(1024);
        while (theIterator.hasNext()) {
			theResult.add(getWrapper(theIterator.next()));
        }
        theResult.trimToSize();
        return theResult;
    }

    /**
     * Check, if there is a wrapper for a KOwith the given ID in the default KBase.
     *
     * @param objectKey The ID of the knowledge object.
     * @return <code>true</code>, if there is a wrapper for the knowledge object.
     * @see #getDefaultKB()
     */
	public static boolean hasWrapper(ObjectKey objectKey) {
		return (hasWrapper(objectKey, getDefaultKB()));
    }

	/**
	 * Check, if there is a wrapper for a KO with the given ID.
	 * 
	 * @param objectKey
	 *        The ID of the knowledge object.
	 * @param aBase
	 *        The {@link KnowledgeBase} to be used for searching the KO.
	 * @return <code>true</code>, if there is a wrapper for the knowledge object.
	 */
	public static boolean hasWrapper(ObjectKey objectKey, KnowledgeBase aBase) {
		KnowledgeItem item = aBase.resolveCachedObjectKey(objectKey);
		if (item == null) {
			return false;
		}

		return item.getWrapper() != null;
    }

    /**
     * Return the default knowledgebase to be used for searching
     * knowledge objects.
     *
     * @return    The default knowledge base.
     */
	private static KnowledgeBase getDefaultKB() {
        return PersistencyLayer.getKnowledgeBase();
    }

    /**
	 * Generic {@link TLObject}s for the given list of {@link KnowledgeItem}s without dynamic type
	 * check.
	 * 
	 * @since TL_5.7
	 * 
	 * @see #getWrappersForKOs(Class, Collection)
	 */
	public static <T extends TLObject> List<T> getWrappersForKOsGeneric(Collection<? extends KnowledgeItem> someKOs) {
		ArrayList<T> result = new ArrayList<>(someKOs.size());
		
		for (Iterator<? extends KnowledgeItem> it = someKOs.iterator(); it.hasNext();) {
			result.add(getWrapper(it.next()));
		}
		
		return result;
	}

    /**
	 * Typed {@link TLObject}s for the given list of {@link KnowledgeItem}s with dynamic check.
	 * 
	 * @since TL_5.7
	 * 
	 * @see #getWrappersForKOsGeneric(Collection)
	 */
	public static <T extends TLObject> List<T> getWrappersForKOs(Class<T> expectedType,
			Collection<? extends KnowledgeItem> koList) {
		ArrayList<T> result = new ArrayList<>(koList.size());
		addWrappersForKOs(expectedType, result, koList);
		return result;
	}

	/**
	 * Adds typed {@link TLObject}s for the given {@link KnowledgeItem}s to the given output.
	 * 
	 * @since 5.8.0
	 * 
	 * @param expectedType
	 *        Expected {@link TLObject} class.
	 * @param out
	 *        {@link Collection} to add wrappers to.
	 * @param koList
	 *        {@link KnowledgeItem}s to get wrappers for.
	 */
	public static <T> void addWrappersForKOs(Class<? extends T> expectedType, Collection<T> out,
			Collection<? extends KnowledgeItem> koList) {
		for (KnowledgeItem ko : koList) {
			out.add(CollectionUtil.dynamicCast(expectedType, getWrapper(ko)));
		}
	}

	/**
	 * {@link Mapping} of {@link KnowledgeItem}s to {@link TLObject}s using the
	 * {@link WrapperFactory}.
	 */
	public static Mapping<KnowledgeItem, TLObject> getWrapperMapping() {
		return WrapperMapping.INSTANCE;
	}
	
	/**
	 * {@link Mapping} converting {@link KnowledgeItem}s to {@link TLObject}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class WrapperMapping implements Mapping<KnowledgeItem, TLObject> {

		/**
		 * Singleton {@link WrapperFactory.WrapperMapping} instance.
		 */
		public static final WrapperFactory.WrapperMapping INSTANCE = new WrapperFactory.WrapperMapping();

		private WrapperMapping() {
			// Singleton constructor.
		}

		@Override
		public TLObject map(KnowledgeItem input) {
			return WrapperFactory.getWrapper(input);
		}
	}

}
