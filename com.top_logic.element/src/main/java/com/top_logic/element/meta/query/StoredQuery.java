/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.filt.DOTypeNameFilter;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.NoFlexData;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.Utils;

/**
 * Store queries based on {@link com.top_logic.element.meta.query.CollectionFilter}s.
 *
 * TODO KBU/KHA Do actual filtering here (== model) and not in the Component(s).
 *
 * The queries may be bound to an owner (Person)
 * and a query area (an arbitrary Wrapper).
 *
 * The CollectionFilters are created by fetching Flex-Attributes,
 * containing the classname of the filter. Any additional attributes
 * ar then interpreted by the Filter itself.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class StoredQuery extends StoredFlexWrapper {

    /** KO ID separator. */
    public static final char SEPARATOR = ',';

    /** Name of underlying KO */
    public static final String STORED_QUERY_KO = "StoredQuery";

    /** Filter Knowledge- (resp. Data-) Objects by my KO-Type*/
    public static final Filter KO_TYPE_Filter = new DOTypeNameFilter(STORED_QUERY_KO);

    /** Attribute storing the meta element, which is used to define the query. */
    public static final String QUERY_META_ELEMENT = "QueryMetaElement";

    /** Attribute storing the result columns of the query. */
    public static final String RESULT_COLUMNS = "ResultColumns";

    /** KA Type to connect a StoredQuery to a Person */
    public static final String OWNER_ASSOCIATION = "hasOwner";


    /**
     * CTor for WrapperFactory ONLY
     *
     * @param ko       the KO
     */
	public StoredQuery(KnowledgeObject ko) {
        super(ko);
    }

    /**
	 * Creates a new {@link StoredQuery} with the given name for the given person.
	 *
	 * @param aName
	 *        the name of the query. Must not be <code>null</code> or empty
     * @param anOwner
	 *        the owner of the query
     * @param aKB
	 *        the KB to create the query in. If it is <code>null</code> the default KB will be used.
	 * @return the new query
	 * @throws Exception
	 *         if creation fails
	 */
    public static StoredQuery newStoredQuery(String aName, Person anOwner, KnowledgeBase aKB) throws Exception {
        // Check params
        if (StringServices.isEmpty(aName)) {
            throw new IllegalArgumentException ("Name for StoredQuery must not be null!");
        }
        if (aKB == null) {
            aKB = getDefaultKnowledgeBase();
        }

        // Create query
		KnowledgeObject theKO = aKB.createKnowledgeObject(STORED_QUERY_KO);
        StoredQuery     theWrap = (StoredQuery) StoredFlexWrapper.getStoredQuery(theKO);
        MapBasedPersistancySupport.assignContainer(theWrap, anOwner);
		theWrap.tSetData(NAME_ATTRIBUTE, aName);
        return theWrap;
    }

    /**
     * Get the StoredQueries that match the given owner and apply to the given meta element.
     *
     * @param    aME        The meta element to return the queries for, may be <code>null</code>.
     * @param    anOwner    The owner. May be <code>null</code>.
     * @return   The stored queries that match the given owner, null in case of error.
     * @since    <i>TopLogic</i> 5.4
     */
    public static List getStoredQueries(TLClass aME, Person anOwner) {
        List theStoredQueries = MapBasedPersistancySupport.getContainers(anOwner, StoredQuery.KO_TYPE_Filter, false);

        if (aME != null && !theStoredQueries.isEmpty()) {
            for (Iterator theIt = theStoredQueries.iterator(); theIt.hasNext();) {
                StoredQuery theSQ = (StoredQuery) theIt.next();
                TLClass theME = theSQ.getQueryMetaElement();

                if ((theME == null) || !theME.equals(aME)) {
                    theIt.remove();
                }
            }
        }

        return theStoredQueries;
    }

	public static List getStoredQueries(TLClass aME, Person anOwner, boolean checkGroups) {
		List theStoredQueries;
		{
			Comparator theComparator = FlexWrapperUserComparator.INSTANCE;
			
			if (ThreadContext.isAdmin()) {
				
			    Collection allKnowledgeObjects = getDefaultKnowledgeBase().getAllKnowledgeObjects(STORED_QUERY_KO);
                theStoredQueries = AbstractWrapper.getWrappersFromCollection(allKnowledgeObjects);

                theComparator = FlexWrapperAdminComparator.INSTANCE;
			}
			else {
				theStoredQueries = MapBasedPersistancySupport.getContainers(anOwner, KO_TYPE_Filter, checkGroups);
	            // must remove duplicates because Queries may be registered in two groups.
	            theStoredQueries = CollectionUtil.removeDuplicates(theStoredQueries);
			}
			Collections.sort(theStoredQueries, theComparator);

			if (aME != null) {
				for (Iterator theIt = theStoredQueries.iterator(); theIt.hasNext();) {
					StoredQuery theSQ = (StoredQuery) theIt.next();
					TLClass theME = theSQ.getQueryMetaElement();

					if ((theME == null) || !theME.equals(aME)) {
						theIt.remove();
					}
				}
			}
		}
		return theStoredQueries;
	}

    /**
	 * Get the StoredQueries that match the given owner.
	 *
	 * @param anOwner
	 *            The owner, may be <code>null</code>.
	 * @return The stored queries that match the given owner, null in case of
	 *         error.
	 */
    public static List getStoredQueries(Person anOwner) {
        return StoredQuery.getStoredQueries((TLClass) null, anOwner);
    }

    /**
     * Get all StoredQueries of the given KB
     *
     * @param aKB the KB. If it is <code>null</code> the default KB will be used.
     * @return a List of the StoredQueries. Never <code>null</code>.
     */
	public static List getAllStoredQueries(KnowledgeBase aKB) {
        if (aKB == null) {
            aKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        }
        Collection      allKOs = aKB.getAllKnowledgeObjects(STORED_QUERY_KO);
        return getWrappersFromCollection(allKOs);
    }

    /**
     * Get the filters of this stored query.
     *
     * @return the filters of this stored query. May be empty
     * but is never <code>null</code>.
     */
	public Collection getFilters() {

        // Get DataObject
		FlexData theDO = this.getDataObjectForRead();

        Collection theResult = MapBasedPersistancySupport.getObjects(theDO);

        return theResult;
    }

    /**
     * Return the meta element this query is based on.
     *
     * Queries before "<i>TopLogic</i> 5.4" may return <code>null</code> here, newer queries should
     * return a matching value here (needed by the {@link AttributedSearchComponent}).
     *
     * @return    The requested meta element, may be <code>null</code> on old queries.
     * @since     <i>TopLogic</i> 5.4
     */
    public TLClass getQueryMetaElement() {
		TLID theID = IdentifierUtil.fromExternalForm((String) this.getValue(StoredQuery.QUERY_META_ELEMENT));
        TLClass result = null;
        if (theID != null) {
			result = KBBasedMetaElement.getInstance(theID);
        }
        if (result == null) {
			Logger.warn("No type for '" + theID + "'.", StoredQuery.class);
        }

        return result;
    }

    /**
     * Set the meta element, which is base of this stored query.
     *
     * @param    aME    The meta element to be used, must not be <code>null</code>.
     */
    public void setQueryMetaElement(TLClass aME) {
        if (aME == null) {
            throw new IllegalArgumentException("Given meta element is null");
        }

        if (aME instanceof Wrapper) {
			this.setValue(StoredQuery.QUERY_META_ELEMENT,
				IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(((Wrapper) aME))));
        }
    }

    /**
	 * Set the filtes for this StoredQuery
	 *
	 * @param someFilters
	 *        the filtes for this StoredQuery
	 */
	public void setFilters(Collection someFilters) {
    	boolean doCleanup = this.getDataObjectForRead() != NoFlexData.INSTANCE; // Don't use this DO if it exists! Changes would not be committed!
		FlexData theDO = getDataObjectForWrite(); // Provoke lock so that commit works
    	MapBasedPersistancySupport.setObjects(someFilters, theDO, doCleanup);
    }


    /**
	 * Get the filter for an attribute.
	 *
	 * (In case you use this in a loop considet using the static variant, this avlids fetching the
	 * List more than once)
	 *
	 * @param anAttribute
	 *        a attribute
	 * @return a matching filter or <code>null</code>.
	 */
    public MetaAttributeFilter getFilterFor(TLStructuredTypePart anAttribute, String anAcessPath) {
        // TODO KBU basically this could be a list of filters...!

        // Check params
        if (anAttribute == null) {
            return null;
        }

        Collection  theFilters;

        // Get the filters
		theFilters = this.getFilters();
        return getFilterFor(theFilters, anAttribute, anAcessPath);
    }

    /**
	 * Get the filter for an attribute.
	 *
	 * (In case you use this in a loop considet using the static variant, this avlids fetching the
	 * List more than once)
	 *
	 * @return a matching filter or <code>null</code>.
	 */
    public Object getFilterFor(Class aClass) {
        // Check params
        if (aClass == null) {
            return null;
        }

        Collection  theFilters;

        // Get the filters
		theFilters = this.getFilters();
        return getFilterFor(theFilters, aClass);
    }

    /**
     * Set the result columns
     */
    public void setResultColumns(List someColumns) {
    	if (CollectionUtil.isEmptyOrNull(someColumns)) {
    		this.setString(RESULT_COLUMNS, "");
    	}
    	else {
    		String theString = StringServices.toString(someColumns, ",");
    		this.setString(RESULT_COLUMNS, theString);
    	}
    }

    /**
     * Get the result columns
     *
     * @return the result columns
     */
    public List<String> getResultColumns() {
        String theColumns = this.getString(RESULT_COLUMNS);

        if (StringServices.isEmpty(theColumns)) {
        	return Collections.emptyList();
        }
        else {
        	return StringServices.toListAllowEmpty(theColumns, ',');
        }
    }

    @Override
	public BoundObject getSecurityParent() {
        return super.getSecurityParent();
    }

    /**
	 * Get the filter for an attribute in given Collection.
	 *
	 * (In case you use this in a loop considet using the static variant, this avlids fetching the
	 * List more than once)
	 *
	 * @param anAttribute
	 *        an attribute
	 * @return a matching filter or <code>null</code>.
	 */
    public static MetaAttributeFilter getFilterFor(Collection aFilterColl, TLStructuredTypePart anAttribute, String anAccessPath) {

        // Check params
        if (anAttribute == null) {
            return null;
        }

        Iterator theFilters = aFilterColl.iterator();
        // Find a matching filter

        while (theFilters.hasNext()) {
            Object theFilter = theFilters.next();
            if ( theFilter instanceof MetaAttributeFilter) {
                MetaAttributeFilter theMAF = (MetaAttributeFilter) theFilter;
                if (Utils.equals(theMAF.getFilteredAttribute(), anAttribute) && Utils.equals(theMAF.getAccessPath(), anAccessPath)) {
                    return (MetaAttributeFilter) theFilter;
                }
            }
        }

        return null;
    }

    /**
	 * Get the filter for an attribute in given Collection.
	 *
	 * (In case you use this in a loop considet using the static variant, this avlids fetching the
	 * List more than once)
	 *
	 * @return a matching filter or <code>null</code>.
	 */
    public static Object getFilterFor(Collection aFilterColl, Class aClass) {

        // Check params
        if (aClass == null) {
            return null;
        }

        Iterator theFilters = aFilterColl.iterator();
        // Find a matching filter

        while (theFilters.hasNext()) {
            Object theFilter = theFilters.next();
            if (aClass.isInstance(theFilter)) {
                return theFilter;
            }
        }

        return null;
    }

    @Override
	public Person getCreator() {
        Person theP = super.getCreator();
        if (theP == null) {
            return PersonManager.getManager().getRoot();
        }
        return theP;
    }

    public Person getOwner() {
        try {
            Iterator it = tHandle().getOutgoingAssociations(OWNER_ASSOCIATION);
            while (it.hasNext()) {
                KnowledgeAssociation theKA = (KnowledgeAssociation)it.next();
                Wrapper theOwner = WrapperFactory.getWrapper(theKA.getDestinationObject());
                if (theOwner instanceof Person) {
                    return (Person)theOwner;
                }
            }
            return null;
        }
        catch (Exception ex) {
            throw new WrapperRuntimeException("Failed to get owner: ", ex);
        }
    }

}
