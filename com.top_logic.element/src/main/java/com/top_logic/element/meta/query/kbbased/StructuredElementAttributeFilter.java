/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query.kbbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter for TypedWrapperAttributes with a StructuredElement as value.
 * Allows specification of a single StructuredElement but to use the
 * sub tree elements as passible values as well.
 *
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class StructuredElementAttributeFilter extends
        KBBasedWrapperValuedAttributeFilter {

	public static final int START_LEVEL_ELEMENT = 0;
	public static final int START_LEVEL_CONTEXT = 1;
	public static final int START_LEVEL_ROOT    = 2;
	
	/** key for value Map to store wrapper ids. */
	protected static final String KEY_USESTRUCTURE = "useStructure";
	
    /** key for value Map to store wrapper ids. */
    protected static final String KEY_STARTLEVEL = "startLevel";

    /** The SE to search. */
    private  Set<StructuredElementWrapper> seWrappers;

    /** If true search the SE and its sub tree, if false only the SE. */
    private boolean searchSubTree;
    
    private int startLevel;

    public StructuredElementAttributeFilter(Map aValueMap)
            throws IllegalArgumentException {
        super(aValueMap);
    }

    /**
	 * @param anAttribute
	 *        the attribute to be searched
	 * @param aWrapper
	 *        the Wrapper that should match as values of anAttribute
	 * @param subTree
	 *        if true take the sub-trees of someWrapper into account
	 * @param doNegate
	 *        if true the element that do not match will match
	 * @param isRelevant
	 *        if true apply the filter. Otherwise do not filter
	 * @param anAccessPath
	 *        a path to access the object the value to be used to filter is part of. Default is
	 *        <code>null</code>
	 * @throws IllegalArgumentException
	 *         if the arguments are not correct
	 */
    public StructuredElementAttributeFilter(TLStructuredTypePart anAttribute,
    		StructuredElementWrapper aWrapper, boolean subTree, boolean doNegate, boolean isRelevant, String anAccessPath)
    throws IllegalArgumentException {
    	this(anAttribute, Collections.singleton(aWrapper), subTree, doNegate, isRelevant, anAccessPath, START_LEVEL_ELEMENT);
    }

    /**
	 * @param anAttribute
	 *        the attribute to be searched
	 * @param someWrappers
	 *        the Wrappers that should match as values of anAttribute
	 * @param subTree
	 *        if true take the sub-trees of someWrapper into account
	 * @param doNegate
	 *        if true the element that do not match will match
	 * @param isRelevant
	 *        if true apply the filter. Otherwise do not filter
	 * @param anAccessPath
	 *        a path to access the object the value to be used to filter is part of. Default is
	 *        <code>null</code>
	 * @param aStartLevel
	 *        one of START_LEVEL_...
	 * @throws IllegalArgumentException
	 *         if the arguments are not correct
	 */
    public StructuredElementAttributeFilter(TLStructuredTypePart anAttribute,
            Set<StructuredElementWrapper> someWrappers, boolean subTree, boolean doNegate, boolean isRelevant, String anAccessPath, int aStartLevel)
            throws IllegalArgumentException {
        super(anAttribute, null, doNegate, isRelevant, anAccessPath);

        this.searchSubTree = subTree;
        this.seWrappers    = someWrappers;
        
        this.startLevel = aStartLevel;
        if (this.startLevel < START_LEVEL_ELEMENT || this.startLevel > START_LEVEL_ROOT) {
        	Logger.warn("Invalid start level " + aStartLevel + " using " + START_LEVEL_ELEMENT, this);
        	this.startLevel = START_LEVEL_ELEMENT;
        }

		updateValues(this.getAllWrappers(this.seWrappers, this.searchSubTree));
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
        List theWrappers= (this.seWrappers == null) ? Collections.EMPTY_LIST : new ArrayList(this.seWrappers);
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), theWrappers, null, domain);
    }

	/**
	 * Add wrapper list
	 *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        // Add wrapper list
        List theWrappers = new ArrayList(this.seWrappers);
        if (theWrappers != null && !theWrappers.isEmpty()) {
			List<String> theTypes = new ArrayList<>(1); // expecting only one type.
            int size = theWrappers.size();
            StringBuffer hlp = new StringBuffer(size << 5); // * 32;
            Wrapper wRaPp3R = (Wrapper) theWrappers.get(0);
            theTypes.add(wRaPp3R.tTable().getName());
			hlp.append(IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(wRaPp3R)));
            for (int i=1; i < size; i++) {
            	wRaPp3R = (Wrapper) theWrappers.get(i);
                hlp.append(StoredQuery.SEPARATOR)
					.append(IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(wRaPp3R)));
				String theType = wRaPp3R.tTable().getName();
				if (!theTypes.contains(theType)) {
					theTypes.add(theType);
				}
			}
	        theMap.put(KEY_WRAPPERIDS  , hlp.toString());
            theMap.put(KEY_WRAPPERTYPES, StringServices.toString(theTypes,String.valueOf(StoredQuery.SEPARATOR)));
        }

        theMap.put(KEY_USESTRUCTURE, this.searchSubTree);
        theMap.put(KEY_STARTLEVEL,   this.startLevel);

        return theMap;
    }

    /**
     * @see com.top_logic.element.meta.query.CollectionFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
    	Object theStartLevel = aValueMap.get(KEY_STARTLEVEL);
    	this.startLevel = (theStartLevel == null) ? START_LEVEL_ELEMENT : (Integer) theStartLevel;

    	super.setUpFromValueMap(aValueMap);

        Object theSearchSubTree = aValueMap.get(KEY_USESTRUCTURE);
        if ((theSearchSubTree != null) && !(theSearchSubTree instanceof Boolean)) {
            throw new IllegalArgumentException ("Value for key " + KEY_USESTRUCTURE + " must be a Boolean!");
        }

        this.searchSubTree = (theSearchSubTree == null) ? true : ((Boolean) theSearchSubTree).booleanValue();

		List theWrappers = this.getWrappers();
		if (theWrappers != null && !theWrappers.isEmpty()) {
            this.seWrappers = new HashSet<>(theWrappers);
        }
		else {
			this.seWrappers = Collections.EMPTY_SET;
		}

		updateValues(this.getAllWrappers(this.seWrappers, this.searchSubTree));
    }

	private List<StructuredElementWrapper> getAllWrappers(Set<StructuredElementWrapper> theWrappers, boolean doSearchSubTree) {
		if (theWrappers != null) {
			Set<StructuredElementWrapper> theResult = new HashSet<>();
			for (StructuredElementWrapper theWrapper : theWrappers) {
				theResult.addAll(this.getWrappers(theWrapper, this.searchSubTree));
			}
			
			return new ArrayList(theResult);
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

    /**
     * Get the Wrappers that are allowed values in filtering
     *
     * @param    aWrapper         The StructuredElement to start from.
     * @param    doSearchSubTree    If true include all elements of the sub tree of aWrapper
     * @return   The list of allowed {@link com.top_logic.knowledge.wrap.Wrapper wrappers}, never <code>null</code>.
     */
    private List getWrappers(StructuredElementWrapper aWrapper, boolean doSearchSubTree) {
        if (aWrapper == null) {
            return Collections.EMPTY_LIST;
        }

        if (!aWrapper.isRoot()) {
        	if (START_LEVEL_CONTEXT == this.startLevel) {
        		aWrapper = (StructuredElementWrapper) aWrapper.getStructureContext();
        	}
        	else if (START_LEVEL_ROOT == this.startLevel) {
        		aWrapper = (StructuredElementWrapper) aWrapper.getRoot();
        	}
        }
        
        if (!doSearchSubTree) {
            List theList = new ArrayList();
            theList.add(aWrapper);
            return theList;
        }
        else {
            AllElementVisitor theVisitor = new AllElementVisitor();

            TraversalFactory.traverse(aWrapper, theVisitor, TraversalFactory.DEPTH_FIRST);

            return (new ArrayList(theVisitor.getList()));
        }
    }
}
