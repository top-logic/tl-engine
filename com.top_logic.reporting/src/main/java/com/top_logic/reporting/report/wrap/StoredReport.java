/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.filt.DOAttributeFilter;
import com.top_logic.dob.filt.DOTypeNameFilter;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.element.meta.query.FlexWrapperAdminComparator;
import com.top_logic.element.meta.query.FlexWrapperUserComparator;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.util.Utils;

/**
 * The {@link StoredReport} is a {@link com.top_logic.knowledge.objects.KnowledgeObject} for storing
 * a {@link RevisedReport} in a {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class StoredReport extends AttributedWrapper {

	/** The table name of stored reports. */
	public static final String	KO_TYPE			   = "StoredReport";
	/** Attribute holding the serialized report configuration. */
	public static final String	ATTRIBUTE_REPORT   = "report";
	/** Attribute holding the name of the report. */
	public static final String	ATTRIBUTE_NAME	   = "name";
	/** Attribute holding the type of the reported business objects. */
	public static final String	ATTRIBUTE_BO_TYPE  = "bo_type";

    /** Filter Knowledge- (resp. Data-) Objects by my KO-Type*/
    public static final Filter<? super DataObject> KO_TYPE_Filter = new DOTypeNameFilter(KO_TYPE);

	/**
	 * Creates a {@link StoredReport}.
	 */
	public StoredReport(KnowledgeObject ko) {
		super(ko);
	}
	
	/**
	 * The stored reports visible to the given person for the given business object type.
	 *
	 * @param anOwner
	 *        The person the reports are visible to. An administrator sees all reports.
	 * @param checkGroups
	 *        Whether reports published to the groups of the person are also returned.
	 * @param aBOType
	 *        The type of the reported business objects.
	 * @return The reports, sorted.
	 */
	public static List getStoredReports(Person anOwner, boolean checkGroups, final String aBOType) {
		List theStoredReports;
		{
			Comparator theComparator = FlexWrapperUserComparator.INSTANCE;
			
			if (ThreadContext.isAdmin() || Person.isAdmin(anOwner)) {
				
			    Collection allKnowledgeObjects = new ArrayList();
			    Iterator theIter = getDefaultKnowledgeBase().getObjectsByAttribute(KO_TYPE, ATTRIBUTE_BO_TYPE, aBOType);
			    while(theIter.hasNext()) {
			    	allKnowledgeObjects.add(theIter.next());
			    }
                theStoredReports = AbstractWrapper.getWrappersFromCollection(allKnowledgeObjects);

                theComparator = FlexWrapperAdminComparator.INSTANCE;
			}
			else {
			    
			    Filter theFilter = FilterFactory.and(KO_TYPE_Filter, new DOAttributeFilter(ATTRIBUTE_BO_TYPE) {
				    @Override
				    public boolean test(Object theValue) {
				        return Utils.equals(theValue, aBOType);
				    }
				});
			    
				theStoredReports = MapBasedPersistancySupport.getContainers(anOwner, theFilter, checkGroups);
	            // must remove duplicates because Queries may be registered in two groups.
	            theStoredReports = CollectionUtil.removeDuplicates(theStoredReports);
			}
			Collections.sort(theStoredReports, theComparator);

		}
		return theStoredReports;
	}
	
    /**
     * @see com.top_logic.knowledge.wrap.AbstractWrapper#getCreator()
     */
    @Override
	public Person getCreator() {
        Person theP = super.getCreator();
        if (theP == null) {
            return PersonManager.getManager().getRoot();
        }
        return theP;
    }
    
    /**
     * Delete the {@link StoredReport} with the given ID
     *
     * @param anID  the KOID of the query to be deleted
     * @throws Exception if deletion fails
     */
	public static void deleteStoredReport(TLID anID) throws Exception {
        // Get report
        StoredReport theSR = getStoredReport(anID);

        // Delete report
        if (theSR != null) {
            theSR.tDelete();
        }
    }
    
    /**
     * Get an instance for the given KO ID.
     *
     * @param anID  the KOID
     * @return an instance for the given KO ID. <code>null</code> if it couldn't be found.
     */
    public static StoredReport getStoredReport(TLID anID) {
		return (StoredReport) WrapperFactory.getWrapper(anID, StoredReport.KO_TYPE);
    }
    
    /** Setter for {@link #getBOType()}. */
    public void setBOType(String aType) {
        if (StringServices.isEmpty(aType)) {
            throw new IllegalArgumentException("Given business object type is null");
        }
        this.setValue(StoredReport.ATTRIBUTE_BO_TYPE, aType);
    }
    
    /** The type of the reported business objects. */
    public String getBOType() {
    	Object theVal = this.getValue(StoredReport.ATTRIBUTE_BO_TYPE);
    	if(theVal instanceof String) {
    		return (String) theVal;
    	}
    	return "";
    }

}
