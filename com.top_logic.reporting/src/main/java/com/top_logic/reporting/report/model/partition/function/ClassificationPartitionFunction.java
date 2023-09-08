/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.reporting.report.importer.node.parser.category.ClassificationFunctionParser;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.partition.DefaultPartition;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.util.Resources;

/**
 * The ClassificationPartitionFunction creates partitions based on list elements, especially
 * it can handle {@link FastList} attributes.
 *
 * @see ClassificationFunctionParser
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_CLASSIFICATION})
@Deprecated
public class ClassificationPartitionFunction extends AbstractPartitionFunction {

	/**
	 * This constant is used as key for the classification map for business objects which
	 * have no fastlist element selected.
	 */
	public static final ResKey NOT_SET = I18NConstants.CLASSIFICATION_NOT_SET;

	private FastList fastList;

	/**
	 * Creates a {@link ClassificationPartitionFunction}.
	 */
	public ClassificationPartitionFunction(InstantiationContext aContext, PartitionFunctionConfiguration aConfig) {
	    super(aContext, aConfig);
	}

	/**
	 * Creates a {@link ClassificationPartitionFunction}.
	 *
	 * @param aLanguage
	 *            A language (e.g. 'de' or 'en').
	 */
	public ClassificationPartitionFunction(String anAttributeName, String aLanguage,
			boolean ignoreNullValues, boolean ignoreEmptyPartitions) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyPartitions);
	}

	public ClassificationPartitionFunction(String anAttributeName, String aLanguage,
			boolean ignoreNullValues, boolean ignoreEmptyPartitions, List someFilter) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyPartitions);
		this.setPartitionFilters(someFilter);
	}

	public ClassificationPartitionFunction(String anAttributeName, String aLanguage,
			boolean ignoreNullValues, boolean ignoreEmptyPartitions, FastList aFastList) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyPartitions);
		this.fastList = aFastList;
	}

	@Override
	public List processObjects( Collection someObjects ) {

		if (this.getPartitionFilters() != null) {
			return this.createPartitions(someObjects, false);
		}

		TreeMap theClassifications = new TreeMap(DisplayAnnotations.CLASSIFIER_ORDER);
		{
			if (this.fastList != null) {
				initClassificationMap(theClassifications, this.fastList.elements());
			}
			else {
				List theList = null;
				for (Iterator theIter = someObjects.iterator(); theIter.hasNext();) {
					Wrapper theWrapper = (Wrapper) theIter.next();
					FastList theFastList = this.getFastListFromAttribute(theWrapper);
					if (theFastList == null) {
						continue;
					}
					else {
						theList = theFastList.elements();
						if (theList != null && !theList.isEmpty()) {
							initClassificationMap(theClassifications, theList);
							continue;
						}
					}
				}
			}
		}

		fillClassificationMap(someObjects, theClassifications);

		return createPartitions(theClassifications);
	}

	private List createPartitions( TreeMap someClassifications ) {
		Resources theResource   = Resources.getInstance();
		Set       theEntries    = someClassifications.entrySet();
		ArrayList thePartitions = new ArrayList(theEntries.size() + 1);

		for (Iterator theIter = theEntries.iterator(); theIter.hasNext();) {
			Map.Entry theEntry = (Map.Entry) theIter.next();
			Object    theKey   = theEntry.getKey();
			Collection theColl = (Collection) theEntry.getValue();
			
			if (this.ignoreEmptyPartitions() && CollectionUtil.isEmptyOrNull(theColl)) {
			    continue;
			}
			
			if (NOT_SET == theKey) {
			    if (this.ignoreNullValues()) {
			        continue;
			    }
			    else {
					String theMessage = theResource.getString(NOT_SET);
	                thePartitions.add(new DefaultPartition(theMessage, this.language, new ArrayList((Collection) theEntry.getValue())));
			    }
			} 
			else if (theKey instanceof FastListElement) {
				String theMessage = theResource.getString(FastListElementLabelProvider.labelKey((FastListElement) theKey));
	    		thePartitions.add(new DefaultPartition(theMessage, this.language, new ArrayList((Collection) theEntry.getValue())));
            } else {
                Logger.error("Encounterd objects in classifiactionPaticiotnFunction that are not of Type FastListElement: "+theKey, this);
            }
		}
		return thePartitions;
	}

	
	private void fillClassificationMap(Collection someObjects, TreeMap aClassificationMap) {
		for (Iterator iter = someObjects.iterator(); iter.hasNext();) {
			Wrapper    theWrapper  = (Wrapper) iter.next();
			Collection theElements = (Collection) this.getAttribute(theWrapper);
			
			if (!CollectionUtil.isEmptyOrNull(theElements)) {

//			if (theElements != null) {
				for (Iterator elementIter = theElements.iterator(); elementIter.hasNext();) {
					FastListElement theElement = (FastListElement) elementIter.next();
					Set theSet = (Set) aClassificationMap.get(theElement);
					theSet.add(theWrapper);
				}
			}
			else if (! this.ignoreNullValues()) {
			    Set notSet = (Set) aClassificationMap.get(NOT_SET);
			    if (CollectionUtil.isEmptyOrNull(notSet)) {
			    	notSet =  new HashSet();
			    	aClassificationMap.put(NOT_SET, notSet);
			    }
				notSet.add(theWrapper);
			}
		}
	}

	private void initClassificationMap(TreeMap aClassificationMap, List aFastList) {
		for (Iterator theIter = aFastList.iterator(); theIter.hasNext();) {
			FastListElement theElement = (FastListElement) theIter.next();
			aClassificationMap.put(theElement, new HashSet());
		}
		
		if (! this.ignoreNullValues()) {
		    aClassificationMap.put(NOT_SET, new HashSet());
		}
	}

	private FastListElement getFastListElement(Wrapper aElement) {

		Collection theSelected = (Collection) this.getAttribute(aElement);
		if(CollectionUtil.isEmptyOrNull(theSelected)) {
			return null;
		}

		if (theSelected.size() > 1) {
			Logger.warn("This size '" + theSelected.size()
					+ "' is not allowed for classifications. The size must be '0' or '1'.", this);
			return (FastListElement) theSelected.iterator().next();
		}

		return (FastListElement) theSelected.iterator().next();
	}

	private FastList getFastListFromAttribute( Wrapper anElement ) {

		Object theAttribute = this.getFastListElement(anElement);
		FastList theList = null;

		if (theAttribute instanceof FastListElement) {
			FastListElement theElement = (FastListElement) theAttribute;
			theList = theElement.getList();
		}
		return theList;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getType()
	 */
	@Override
	public String getType() {
		return PartitionFunctionFactory.CLASSIFICATION;
//		return "classififcation";
	}


	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getCriteria()
	 */
	@Override
	public Criteria getCriteria() {
		// TODO tbe: not yet implemented
		return null;
	}
}
