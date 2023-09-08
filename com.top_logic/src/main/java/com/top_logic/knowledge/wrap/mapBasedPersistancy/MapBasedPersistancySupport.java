/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.mapBasedPersistancy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KAIterator;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Map based persistancy is a mechanism to sotre the data of a certain set of objects (instances of
 * {@link com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware}) held by a
 * knowledge object (a container).
 * 
 * The container must be wrapped by a {@link AbstractBoundWrapper}.
 * 
 * Such containers can be associated to a certain area {e.g. an element in a structure tree) and
 * owend by a certain {@link com.top_logic.knowledge.wrap.person.Person}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MapBasedPersistancySupport {

    /** KA Type  to connect a StoredQuery to a Person */
    private static final String OWNER_ASSOCIATION      = "hasOwner";

    /** KA Type  to connect a StoredQuery to a Group */
    private static final String PUBLISHING_ASSOCIATION = "hasPublisher";

    /** The parts of the FlexAttributes are seperated by this character */
    private static final String ATTNAME_SEPARATOR      = "_";

    /**
	 * Create a collection of {@link MapBasedPersistancyAware}s based on the attributes contained in
	 * the given data object
	 * 
	 * @param aDO
	 *        the {@link FlexData} holding the attributes for the objects to create
	 * @return a collection of {@link MapBasedPersistancyAware}, never <code>null</code>, an empty
	 *         collection if aDO is null or does not contain appropritate configurations.
	 */
	public static Collection getObjects(FlexData aDO) {
        
        if (aDO == null) { // No filters yet
            return Collections.EMPTY_LIST;
        }
        
        // Parse the attributes and create the filters
        int        theMaxIndex   = -1; // Highest index found (if any)
        
        
        // Map of Maps indexed by Integer (index)
		Map theValueMaps = new HashMap(); // / 2
		for (String theAttName : aDO.getAttributes()) {
            
            int theFirstIndex = theAttName.indexOf(ATTNAME_SEPARATOR);
            if (theFirstIndex == 0) {
                int theSecondIndex = theAttName.indexOf(ATTNAME_SEPARATOR, 2);
                if (theSecondIndex >= 2) {
                    String theNumberStr = theAttName.substring(1, theSecondIndex);
                    
                    try {
                        // OK since we need Integer as Key in Map
						Integer theNumberI = Integer.valueOf(theNumberStr);
                        int     theNumber  = theNumberI.intValue();
                        
                        // Update max index
                        if (theNumber > theMaxIndex) {
                            theMaxIndex = theNumber;
                        }
                        
                        // Get or create the value map for the number
                        Map theValueMap = (Map) theValueMaps.get(theNumberI);
                        if (theValueMap == null) {
                            theValueMap = new HashMap(5);   // These are quite small
                            theValueMaps.put(theNumberI, theValueMap);
                        }
                        
                        // put the value
                        String theKey = theAttName.substring(theSecondIndex + 1);
                        theValueMap.put(theKey, aDO.getAttributeValue(theAttName));
                    }
                    catch (NumberFormatException nfx) {
                        Logger.error("Unexpected Attribute '" + theAttName + "', ignored", MapBasedPersistancySupport.class);
                    } catch (NoSuchAttributeException ex) {
                        Logger.error("Missing Attribute '" + theAttName + "'", MapBasedPersistancySupport.class);
                    }
                }
            }
        }

        // No filters found?
        if (theMaxIndex == -1) {
            return Collections.EMPTY_LIST;
        }
        
        // Create the objects
        List                       theResult  = new ArrayList(theMaxIndex + 1);
        MapBasedPersistancyFactory theFactory = MapBasedPersistancyFactory.getInstance();
        for(int i=0; i<= theMaxIndex; i++) {
			Map theValueMap = (Map) theValueMaps.get(Integer.valueOf(i));
            if (theValueMap != null) { // everything else would be strange anyway ;-)
                try {
                	MapBasedPersistancyAware theObject = theFactory.getObject(theValueMap);
                    
                    if (theObject != null) {
                        theResult.add(theObject);
                    }
                }
                catch (Exception ex) {
                    Logger.warn ("Couldn't restore object for " + theValueMap + " (continuing)", ex, MapBasedPersistancySupport.class);
                }
            }
        }
        
        return theResult;		
	}

    /**
     * Store the persistancy data for given instances of {@link MapBasedPersistancyAware}
     * in the given {@link NamedValues}, remove old data from the data object if doCleanup is set.
     * 
     * @param someObjects some instances of {@link MapBasedPersistancyAware} to be stored
     * @param aDO         the data object to keep the filter data, MUST NOT BE NULL
     * @param doCleanup   indicates that the existing filter data is to be removed
     */
	public static void setObjects(Collection someObjects, FlexData aDO, boolean doCleanup) {

		if (doCleanup) { // No flex attribs yet
            // Remove old values // TODO KHA/KBU optimize via FlexWrapper.removeAll() or such.
			for (String theAttName : aDO.getAttributes()) {
                aDO.setAttributeValue(theAttName, null);
            }
        }
        
        // Set new values
        if (someObjects == null || someObjects.isEmpty()) { // No filters -> nothing to do
            return;
        }
        int i = 0;
        Iterator theObjectIt = someObjects.iterator();
		{
            while (theObjectIt.hasNext()) {  // For all filters
            	MapBasedPersistancyAware theObject   = (MapBasedPersistancyAware) theObjectIt.next();
                Map              theValueMap = theObject.getValueMap();
                if (theValueMap != null) {
                    String           prefix      = ATTNAME_SEPARATOR + i + ATTNAME_SEPARATOR;
                    i++;
                    Iterator theFilterKeys = theValueMap.keySet().iterator();
                    while (theFilterKeys.hasNext()) {   // Get all filter values
                        String theKey   = (String) theFilterKeys.next();
                        Object theValue = theValueMap.get(theKey);
                        
                        String theNewKey = prefix + theKey;
                        
                        // Add the filter value with a number prefixed id
                        aDO.setAttributeValue(theNewKey, theValue);
                    }
                } // OK (e.g. ContractSecurityFilter  
                /*
                else {
                    Logger.warn("CollectionFilter without value map "+ theFilter, this);
                }
                */
            }
        }
    }

    /**
	 * Associate the query with the owner.
	 * 
	 * @param aContainer
	 *        the container. Must not be <code>null</code>.
	 * @param anOwner
	 *        the owner. May be <code>null</code>.
	 */
	public static void assignContainer(Wrapper aContainer, Person anOwner) {
		assignContainer(aContainer, anOwner, OWNER_ASSOCIATION);
	}
	
    /**
	 * Associate the query with the owner.
	 * 
	 * @param query
	 *        the container. Must not be <code>null</code>.
	 * @param anOwner
	 *        the owner. May be <code>null</code>.
	 * @param anOwnerAssociation
	 *        the name of the association connecting to the owner
	 */
	public static void assignContainer(Wrapper query, Person anOwner,
			String anOwnerAssociation) {
		{
			KnowledgeBase aKB = query.tHandle().getKnowledgeBase();

			// Owner
			if (anOwner != null) {
				aKB.createAssociation(
					query.tHandle(),
					anOwner.tHandle(),
					anOwnerAssociation);
			}
		}
	}

    /**
	 * Associate the query with a group.
	 * 
	 * @param aContainer
	 *            the container. Must not be <code>null</code>.
	 * @param aGroup
	 *            the group. May be <code>null</code>.
	 */
	public static void assignContainer(Wrapper aContainer, Group aGroup) {
		assignContainer(aContainer, aGroup, PUBLISHING_ASSOCIATION);
	}
	
    /**
	 * Associate the query with a group.
	 * 
	 * @param aContainer
	 *            the container. Must not be <code>null</code>.
	 * @param aGroup
	 *            the group. May be <code>null</code>.
     * @param aGroupAssociation   
     *            the name of the association connecting to the group
	 */
	public static void assignContainer(Wrapper aContainer, Group aGroup, String aGroupAssociation) {
		{
			KnowledgeBase aKB = aContainer.tHandle().getKnowledgeBase();
			if (aGroup != null) {
				aKB.createAssociation(aContainer.tHandle(), aGroup.tHandle(), aGroupAssociation);
			}
		}
    }
    
    /**
	 * Checks if this {@link AbstractWrapper} has associations of type
	 * {@link #PUBLISHING_ASSOCIATION}.
	 * 
	 * @param aKO
	 *            a {@link AbstractWrapper} to check for
	 *            {@link #PUBLISHING_ASSOCIATION} associations
	 * @return the partners or an empty List if none is found; never
	 *         <code>null</code>
	 */
    public static List getGroupAssociation(AbstractWrapper aKO) {
    	return aKO.getOutgoingWrappers(PUBLISHING_ASSOCIATION, null);
    }
    
    /**
	 * Deletes the group association between the given {@link Wrapper} and the given {@link Group}.
	 */
	public static void deleteGroupAssociation(AbstractWrapper aKO, Group aGroup) {
		{
			Iterator theIt = aKO.tHandle().getOutgoingAssociations(PUBLISHING_ASSOCIATION);
			KnowledgeBase theKB = aKO.getKnowledgeBase();
			while (theIt.hasNext()) {
				KnowledgeAssociation theKA = (KnowledgeAssociation) theIt.next();
				KnowledgeObject destinationObject = theKA.getDestinationObject();
				if (destinationObject.equals(aGroup.tHandle())) {
					theKB.delete(theKA);
				}
			}
		}
	}

    /**
	 * Get the containers that match the given owner.
	 * 
	 * @param anOwner
	 *        the owner. May be <code>null</code>.
	 * @param aKOFilter
	 *        a filter restricting the objects found
	 * @param checkGroups
	 *        if <code>true</code> all groups of <code>anOwner</code> will be checked as well
	 * @return the StoredQueries that match the given owner and area, null in case of error
	 */
    public static List getContainers(Person anOwner, Filter aKOFilter, boolean checkGroups) {
		return getContainers(anOwner, aKOFilter, OWNER_ASSOCIATION, checkGroups);
    }
    
    /**
	 * Get the containers that match the given owner using custom associations.
	 * 
	 * @param anOwner
	 *        the owner. May be <code>null</code>.
     * @param aKOFilter
	 *        a filter restricting the objects found
     * @param anOwnerAssociation
	 *        the name of the association connecting to the owner
     * @param checkGroups
	 *        if <code>true</code> all groups of <code>anOwner</code> will be checked as well
	 * @return the StoredQueries that match the given owner and area, null in case of error
	 */
	public static List getContainers(Person anOwner, Filter aKOFilter, String anOwnerAssociation, boolean checkGroups) {
        List res = null;
        Filter theFilter = aKOFilter == null ? FilterFactory.trueFilter() : aKOFilter;
        try {
            // Get owner queries
            if (anOwner != null) {
                res = getWrappersFromAssociations(new SourceIterator(
                        anOwner.tHandle(), anOwnerAssociation), 
                        FilterFactory.trueFilter(), theFilter);
            }
            // Get owner groups queries
            List res2 = new ArrayList();
            if(checkGroups) {
				List visibleGroups = new ArrayList(Group.getGroups(anOwner));
				if (!visibleGroups.isEmpty()) {
        			for(Iterator theIt = visibleGroups.iterator(); theIt.hasNext();) {   
        				Group aGroup = (Group)theIt.next();
        				res2.addAll(getWrappersFromAssociations(new SourceIterator(
        						aGroup.tHandle(), PUBLISHING_ASSOCIATION), 
        						FilterFactory.trueFilter(), theFilter));
        			}
        			
					if (res == null || res.isEmpty()) { // Owner not set -> use result
        				res = res2;
        			}
        			else {              // Owner set -> 
        				res.addAll(res2);
        			}
        		}
            }
        } catch (Exception ex) {
			Logger.error("Problem in getContainers for " + anOwner, ex, MapBasedPersistancySupport.class);
        }
        return res;
    }
    
    /**
     * Get the Wrappers found at the other end of the specified KnowledgeAssociations.
     *
     * Any invalid associations are silently filtered out.
     *
     * @param kas             the Associations, never null
     * @param aKAFilter       arbitraty filter for the KA must not be null.
     * @param aKOFilter       arbitraty filter for the KO must not be null.
     *
     * @return the Wrappers never null
     */
    private static List getWrappersFromAssociations(KAIterator kas, Filter aKAFilter , Filter aKOFilter) {
        List theKOs = new ArrayList ();
        
        while (kas.hasNext ()) {
            if (aKAFilter.accept(kas.currentKA())) {
                KnowledgeObject theKO = kas.nextKO();
                if (aKOFilter.accept(theKO)) {
					theKOs.add(WrapperFactory.getWrapper(theKO));
                }
            }
        }

        return theKOs;
    }

    /**
     * Get the objects of a given type.
     * Used to find special objects that are added manually.
     * 
     * @param  anObjectType the type to serach for, must not be null.
     * @return null when no matching Filters where found
     */
    public static List getObjectsOfType(Collection someObjects, Class anObjectType) {
       
        List theRes = null;
        if (!someObjects.isEmpty()) {
            Iterator theObjects = someObjects.iterator();
            // Find a matching filter
            while (theObjects.hasNext()) {
                Object theFilter = theObjects.next();
                if (anObjectType.isInstance(theFilter)) {
                    if (theRes == null) {
                        theRes = new ArrayList(); // epect only a few matching filters
                    }
                    theRes.add(theFilter);
                }
            }
        }
        
        return theRes;
    }
}
