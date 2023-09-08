/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured;


import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.element.core.TLElementComparator;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.util.model.ModelService;


/**
 * Provides access to the different kinds of root elements.
 * 
 * The factory is responsible of the root elements (and therefore for the handled structures). To
 * get the root element of a structure, one can call the method {@link #lookupSingleton(String)}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class StructuredElementFactory extends DefaultModelFactory {

    /** 
     * Map of StructuredElements (roots) indexed by structure type and branch (structure type '-' branch id). 
     * 
     * Also used to synchronize some root related operations.
     */
	private ConcurrentMap<String, TLObject> _roots = new ConcurrentHashMap<>();

    /**
	 * The default root element in this module.
	 * 
	 * @deprecated Use {@link #lookupSingleton(String)} for generic access, or the method
	 *             <code>getMyRootSingleton()</code> in the concrete generated subclass.
	 */
	@Deprecated
	public StructuredElement getRoot() {
		return (StructuredElement) lookupSingleton(TLModule.DEFAULT_SINGLETON_NAME);
    }
    
    /**
	 * The default root element in this module in the given branch context.
	 * 
	 * @param branch
	 *        The branch context in which the query should be executed.
	 * 
	 * @return The requested root element or <code>null</code>, if no such root element is defined.
	 * 
	 * @deprecated Use {@link #lookupSingleton(String, Branch)} for generic access, or the method
	 *             <code>getMyRootSingleton()</code> in the concrete generated subclass.
	 */
	@Deprecated
    public StructuredElement getRoot(Branch branch) {
		return (StructuredElement) lookupSingleton(TLModule.DEFAULT_SINGLETON_NAME, branch);
	}

	/**
	 * The default root element in this module in the given branch and revision context.
	 * 
	 * @param aBranch
	 *        The branch context in which the query should be executed.
	 * @param aRevision
	 *        The desired revision of the root element (<code>null</code> denotes current)
	 * 
	 * @return The requested root element or <code>null</code>, if no such root element is defined.
	 * 
	 * @deprecated Use {@link #lookupSingleton(String, Branch, Revision)} for generic access, or the
	 *             method <code>getMyRootSingleton()</code> in the concrete generated subclass.
	 */
	@Deprecated
    public StructuredElement getRoot(Revision aRevision, Branch aBranch) {
		return (StructuredElement) lookupSingleton(TLModule.DEFAULT_SINGLETON_NAME, aBranch, aRevision);
    }

	/**
	 * Return the root element of the structure with the given name in the given branch context.
	 * 
	 * @param revision
	 *        The desired revision of the root element (<code>null</code> denotes current)
	 * @param contextBranch
	 *        The branch context in which the query should be executed.
	 * 
	 * @return The requested root element.
	 */
	public final TLObject lookupSingleton(String singletonName, Branch contextBranch, Revision revision) {
		TLObject result = lookupSingleton(singletonName, contextBranch);
		if (revision == null || revision.isCurrent()) {
			return result;
    	}
		return WrapperHistoryUtils.getWrapper(revision, result);
    }

	/**
	 * Looks up the singleton with the given name.
	 * 
	 * <p>
	 * Note: This method is used from generated factory code.
	 * </p>
	 */
	public final TLObject lookupSingleton(String singletonName) {
		return lookupSingleton(singletonName, HistoryUtils.getHistoryManager().getContextBranch());
	}

	/**
	 * Looks up the singleton with the given name on the given branch.
	 * 
	 * <p>
	 * Note: This method is used from generated factory code.
	 * </p>
	 */
	public final TLObject lookupSingleton(String singletonName, Branch contextBranch) {
		long contextBranchId = contextBranch.getBranchId();
        
		// Cache root per branch.
		String key = getModuleName() + "#" + singletonName + "-" + contextBranchId;
		TLObject cachedRoot = _roots.get(key);
		if (cachedRoot != null) {
			if (cachedRoot.tValid()) {
				return cachedRoot;
			} else {
				_roots.remove(key);
			}
		}

		Branch formerBranch = HistoryUtils.setContextBranch(contextBranch);
		try {
			TLModule module = getModule();
			TLObject root = module.getSingleton(singletonName);
			if (root != null) {
				root = MapUtil.putIfAbsent(_roots, key, root);
			}
			return root;
		} finally {
			HistoryUtils.setContextBranch(formerBranch);
		}
	}

	/**
	 * Return the comparator to be used for elements created by this instance.
	 * 
	 * @return A comparator for elements created by this factory.
	 * 
	 * @see #getComparePreload()
	 */
    public Comparator getComparator() {
        return TLElementComparator.INSTANCE;
    }
    
	/**
	 * {@link PreloadOperation} to execute to use {@link #getComparator()} efficient.
	 * 
	 * @see #getComparator()
	 */
	public PreloadOperation getComparePreload() {
		return NoPreload.INSTANCE;
	}

	/**
	 * @deprecated Use {@link DynamicModelService#getAllFactories()}.
	 */
    public static Iterable<StructuredElementFactory> getFactories() {
		return FilterUtil.filterIterable(StructuredElementFactory.class, DynamicModelService.getAllFactories());
    }

    /**
     * @param    anElement    The element to find the siblings for.
     * @return   The array of siblings of the given element.
     */
    public static StructuredElement[] getSiblings(StructuredElement anElement) {
        if (anElement.isRoot() || !anElement.tValid()) {
            return new StructuredElement[0];
        }
        else {
            StructuredElement parent = anElement.getParent();
            if (parent == null) {
				// Happens, if the object is deleted in the database, but the
				// deletion was not yet fetched by the local node, see Ticket
				// #7527. 
            	return new StructuredElement[0];
            }
			List                theList  = parent.getChildren(null);
            StructuredElement[] theArray = new StructuredElement[theList.size() - 1];

            theList.remove(anElement);

            return ((StructuredElement[]) theList.toArray(theArray));
        }
    }

    /**
     * Return the next sibling to the given one.
     * 
     * @param    anElement    The element to find the next sibling for.
     * @return   The next sibling or <code>null</code>, if the given element
     *           is the last in the list of children. 
     */
    public static StructuredElement findNextSibling(StructuredElement anElement) {
        StructuredElement theElement = null;

        if (anElement != null) {
            StructuredElement theParent = anElement.getParent();

            if (theParent != null) {
                List theChildren = theParent.getChildren(null);
                int  thePos      = theChildren.indexOf(anElement);

                if ((thePos >= 0) && (thePos < theChildren.size() - 1)) {
                    theElement = (StructuredElement) theChildren.get(thePos + 1);
                }
            }
        }

        return (theElement);
    }

    /**
     * Return the previous sibling to the given one.
     * 
     * @param    anElement    The element to find the previous sibling for.
     * @return   The previous sibling or <code>null</code>, if the given element
     *           is the first in the list of children. 
     */
    public static StructuredElement findPrevSibling(StructuredElement anElement) {
        StructuredElement theElement = null;

        if (anElement != null) {
            StructuredElement theParent = anElement.getParent();

            if (theParent != null) {
                List theChildren = theParent.getChildren(null);
                int  thePos      = theChildren.indexOf(anElement);

                if (thePos > 0) {
                    theElement = (StructuredElement) theChildren.get(thePos - 1);
                }
            }
        }

        return (theElement);
    }

	/**
	 * @deprecated Use {@link DynamicModelService#getFactoryFor(String)}.
	 */
	@Deprecated
    public static StructuredElementFactory getInstanceForStructure(String aStructureName) {
		return (StructuredElementFactory) DynamicModelService.getFactoryFor(aStructureName);
    }

	/**
	 * Lookup the {@link StructuredElement#CONTAINER_TL_TYPE_NAME} type.
	 */
	public TLClass getStructuredElementContainerType() {
		TLModule module = ModelService.getApplicationModel().getModule(StructuredElement.MODULE_NAME);
		return (TLClass) module.getType(StructuredElement.CONTAINER_TL_TYPE_NAME);
	}

	/**
	 * Lookup {@link StructuredElement#CHILDREN_ATTR} of {@link StructuredElement}.
	 */
	public TLStructuredTypePart getChildrenStructuredElementAttr() {
		return getStructuredElementContainerType().getPart(StructuredElement.CHILDREN_ATTR);
	}

}
