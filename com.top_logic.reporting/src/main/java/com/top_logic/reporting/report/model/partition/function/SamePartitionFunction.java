/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.partition.DefaultPartition;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.util.Resources;

/**
 * TODO: Explain or remove.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {})
@Deprecated
public class SamePartitionFunction extends AbstractPartitionFunction {

	/**
	 * Creates a {@link SamePartitionFunction}.
	 */
	public SamePartitionFunction(InstantiationContext aContext, PartitionFunctionConfiguration aConfig) {
        super(aContext, aConfig);
	}

	/**
	 * Creates a {@link SamePartitionFunction}.
	 *
	 * @param aLanguage
	 *            A language (e.g. 'de' or 'en').
	 */
	public SamePartitionFunction(String anAttributeName, String aLanguage, boolean ignoreNullValues,
			boolean ignoreEmptyCategories) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyCategories);
	}

	@Override
	public List processObjects(Collection someObjects) {
		TreeMap theClassifications = new TreeMap();
		ArrayList theResult = new ArrayList();
		fillMap(someObjects, theClassifications);

		for (Iterator theIter = theClassifications.keySet().iterator(); theIter.hasNext();) {
			Object theValue = theIter.next();
			String thePFName = theValue.toString();
			String theID = thePFName;
			int pos = thePFName.indexOf("#");
			if(pos != -1) {
				theID = thePFName.substring(0, pos);
				thePFName = thePFName.substring(pos + 1);
			}

			DefaultPartition defaultPartition = new DefaultPartition(thePFName, this.language, (List) theClassifications.get(theValue));
			defaultPartition.setIdentifier(theID);
			theResult.add(defaultPartition);
		}

		return theResult;
	}

	private void fillMap(Collection someObjects, TreeMap someClassifications) {
		Resources theRes = Resources.getInstance();
		for (Iterator theIter = someObjects.iterator(); theIter.hasNext();) {
			Object theObject = theIter.next();

			Object theValue = this.getAttribute(theObject);

			if (theValue == null && this.ignoreNullValues()) {
				continue;
			}

			if (theValue == null) {
				theValue = theRes.getString(I18NConstants.EMPTY_VALUE_LABEL);
			}
			String theId = "";
			if(theValue instanceof Wrapper) {
				theId = KBUtils.getWrappedObjectName(((Wrapper)theValue)) + "#";
			}
			theValue = theId + MetaLabelProvider.INSTANCE.getLabel(theValue);

			List theCatObjects = (List) someClassifications.get(theValue);
			if (theCatObjects == null) {
				theCatObjects = new ArrayList();
				someClassifications.put(theValue, theCatObjects);
			}

			theCatObjects.add(theObject);
		}
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.function.PartitionFunction#getType()
	 */
	@Override
	public String getType() {
		return PartitionFunctionFactory.SAME;
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
