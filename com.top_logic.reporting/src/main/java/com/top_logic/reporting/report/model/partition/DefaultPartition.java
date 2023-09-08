/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.util.ReportUtilities;

/**
 * The DefaultPartition is the basic implementation of a {@link Partition}.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class DefaultPartition implements Partition {

	/**
	 * The criteria defines, how this partition was derived from the basis set of objects.
	 * It can be a single Object, e.g. a {@link Date}, or an Array containing several
	 * Objects, e.g. a <code>StartDate</code> and an <code>EndDate</code>.
	 */
	private Criteria criteria;

	/**
	 * A {@link List} of Objects that match the used {@link PartitionFunction}.
	 * 
	 * @see Partition#add(Collection)
	 */
	private List businessObjects;

	/**
	 * A {@link List} of SubPartitions.
	 * 
	 * @see Partition#addSubPartitions(Collection)
	 */
	private List subPartitions;

	/**
	 * A {@link HashMap} of internationalized labels. The <code>key</code> is a country code,
	 * e.g. "en", the value is the appropriate translation for the label.
	 */
	private HashMap internationalizedLabels;
	
	private String identifier;

	public DefaultPartition(String aName, String aLanguage) {
		this(aName, aLanguage, new ArrayList());
	}

	public DefaultPartition(String aName, String aLanguage, Criteria aCriteria) {
		this(aName, aLanguage, new ArrayList());
		this.criteria = aCriteria;
	}

	public DefaultPartition(HashMap someMessages) {
		this.internationalizedLabels = ReportUtilities.createEmptyI18NMap();
		this.internationalizedLabels.putAll(someMessages);

		this.businessObjects = new ArrayList();
		this.subPartitions = new ArrayList();
	}

	public DefaultPartition(String aName, String aLanguage, List someObjects) {
		this.internationalizedLabels = ReportUtilities.createEmptyI18NMap();
		this.internationalizedLabels.put(aLanguage, aName);

		this.businessObjects = someObjects != null ? someObjects : new ArrayList();
		this.subPartitions = new ArrayList();
		this.identifier = aName;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[name=" + this.internationalizedLabels + "]";
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#add(Collection)
	 */	
	@Override
	public void add(Collection someObjects){
		if(hasSubPartitions()) {
			throw new IllegalStateException("This partition has subPartitions");
		}
		getObjects(false).addAll(someObjects);
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#add(Object)
	 */
	@Override
	public void add(Object anObject) {
		if(hasSubPartitions()) {
			throw new IllegalStateException("This partition has subPartitions");
		}
		getObjects(false).add(anObject);
	}
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#addSubPartition(Partition)
	 */
	@Override
	public void addSubPartition(Partition aCategory) {
//		if(hasBusinessObjects()) {
//			throw new IllegalStateException("This partition has BusinessObjects");
//		}
		getSubPartitions().add(aCategory);
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#addSubPartitions(Collection)
	 */
	@Override
	public void addSubPartitions(Collection someCategories) {
//		if(hasBusinessObjects()) {
//			throw new IllegalStateException("This partition has BusinessObjects");
//		}
		getSubPartitions().addAll(someCategories);
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#getSubPartitions()
	 */
	@Override
	public List getSubPartitions() {
		return this.subPartitions;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#hasSubPartitions()
	 */
	@Override
	public boolean hasSubPartitions() {
		return this.subPartitions.size() > 0;
	}
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#hasBusinessObjects()
	 */
	@Override
	public boolean hasBusinessObjects() {
		return this.businessObjects.size() > 0;
	}

	@Override
	public String getName(String aLanguage) {
//		WrapperResourceProvider theResProvider = WrapperResourceProvider.INSTANCE;
//		return theResProvider.getLabel(this);
		return (String) this.internationalizedLabels.get(aLanguage);
	}
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.Partition#getObjects(boolean)
	 */
	@Override
	public List getObjects(boolean aRecursive) {
		return this.businessObjects;
	}

	/**
	 * This method returns the criteria.
	 * 
	 * @return    Returns the criteria.
	 */
	@Override
	public Criteria getCriteria() {
		return (this.criteria);
	}

	@Override
	public String getIdentifier() {
	    return this.identifier;
    }

	@Override
	public void setIdentifier(String anID) {
		this.identifier = anID;
    }
}
