/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */package com.top_logic.reporting.report.view.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.util.Resources;

/**
 * The ClassificationDatasetGenerator creates from a risk item version holder, risk items and
 * classification list a {@link Dataset} for a category chart (e.g. bar chart).
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ClassificationDatasetGenerator {

	/**
	 * The constant for the classification not set. A ordered list returns the
	 * selected FastListElement in a collection. But if no FastListElement is
	 * selected an empty collection is returned. In this context is this constant
	 * in use.
	 */
	private static final ResKey CLASSIFICATION_NOT_SET = I18NConstants.CLASSIFICATION_NOT_SET_LABEL;

	/**
	 * This map contains for all elements of an ordered list a list with risk
	 * items (+ a not set list with risk items). The keys are the translated
	 * names of the FastListElements or {@link #CLASSIFICATION_NOT_SET}. The
	 * values are {@link List}s.
	 */
	private Map<String, List<Object>>   classifications;
	private Set<Object>                 objects;
	private String                      orderedListName;
	private final CollectionToNumberCalculator calculator;
    private final String attrName;
    private final boolean hasNotAssigned;

	/**
	 * Creates a {@link ClassificationDatasetGenerator} with the
	 * given parameters.
	 * 
	 * @param aOrderedListName
	 *            The name of the classification list (ordered list). Must not
	 *            be <code>null</code>.
	 */
	public ClassificationDatasetGenerator(ClassificationModel classificationModel, String aOrderedListName, String anAttrName, boolean hasNotAssigned) {
        this.calculator      = classificationModel.getCalculator();
		this.objects         = classificationModel.getObjects();
		this.orderedListName = aOrderedListName;
		this.attrName        = anAttrName;
		this.hasNotAssigned  = hasNotAssigned;
	}

	/**
	 * This method creates from the risk item version holder, risk items and the
	 * classification list a {@link Dataset} for a category chart (e.g. bar
	 * chart).
	 *
	 * @return Returns a {@link Dataset} which can be used for category charts.
	 */
	public Dataset createDataset() {
		ExtendedCategoryDataset theDataset = new ExtendedCategoryDataset();
		Set<Object>             theObjects = this.getObjects();

		/* Return null, it is nothing to do without a model. */
		if (CollectionUtil.isEmptyOrNull(theObjects)) {
		    return theDataset;
		}

		/* Create a sorted map for the classifications of the risk items. */
		this.createClassificationMap(theObjects.iterator());

		Resources theRes      = Resources.getInstance();
		String    theCatLabel = "";

		// Reset the number of the risk items for the new data set.
		this.calculator.reset();

		// Iterate over the list and fill the data set.
		FastList theOrderedList = FastList.getFastList(this.orderedListName);

		for (FastListElement theElement : this.getFastListElements(theOrderedList)) {
			this.addRow(theCatLabel, theDataset, this.createClassificationKey(theRes, theElement));
		}

		if (this.hasNotAssigned) {
    		// Do not forget the not set classification.
			this.addRow(theCatLabel, theDataset, theRes.getString(CLASSIFICATION_NOT_SET));
		}

		return theDataset;
	}

	/** 
	 * Return the objects we are operating on.
	 * 
	 * @return    The requested objects.
	 */
	protected Set<Object> getObjects() {
		return this.objects;
	}

	/**
	 * This method returns a list of {@link FastListElement}s for the given
	 * {@link FastList} or a {@link Collections#EMPTY_LIST}.
	 *
	 * @param aOrderedList
	 *        A {@link FastList}. Must not be <code>null</code>.
	 */
	private List<FastListElement> getFastListElements(FastList aOrderedList) {
		return aOrderedList.elements();
	}

	/**
	 * This method sets a row into the given dataset with the given category
	 * name and key ({@link #classifications}.
	 *
	 * @param aCatLabel
	 *        A category name for the given dataset. Must not be <code>null</code>.
	 * @param aDataset
	 *        A {@link ExtendedCategoryDataset}. Must not be <code>null</code>.
	 * @param aKey
	 *        A key for the {@link #classifications} map. Must not be <code>null</code>.
	 */
	private void addRow(String aCatLabel, ExtendedCategoryDataset aDataset, String aKey) {
		aDataset.addValue(this.calculator.calculateNumberFor(this.classifications.get(aKey)), aKey, aCatLabel);
	}

	/**
	 * This method create the {@link #classifications} map.
	 */
	private void createClassificationMap(Iterator<?> aIterator) {
		/* Reset the classification map. */
		this.classifications = new HashMap<>();

		/* Get the resources. */
		Resources theRes       = Resources.getInstance();
		String theEmptyName = theRes.getString(CLASSIFICATION_NOT_SET);

		/* Iterate over the risk items and put them into the classification map. */
		while (aIterator.hasNext()) {
			Object                      theObject = aIterator.next();
			Collection<FastListElement> theColl   = this.getClassification(theObject);

			// Check if there is at least one classification.
			if (theColl.size() > 0) {
                // Append the item to the matching classifications.
                for (FastListElement theElement : theColl) {
					String theName = this.createClassificationKey(theRes, theElement);
                    List<Object> theObjects = this.classifications.get(theName);

                    // Does not exist a list for the classification element, create one.
                    if (theObjects == null) {
                        theObjects = new ArrayList<>();
                        this.classifications.put(theName, theObjects);
                    }

                    // Add the risk item to the list.
                    theObjects.add(theObject);
                }
			}
			else {
                // Append the item to the classification list for empty selections.
                List<Object> theObjects = this.classifications.get(theEmptyName);

                // Does not exist a list for the classification element, create one.
                if (theObjects == null) {
                    theObjects = new ArrayList<>();
                    this.classifications.put(theEmptyName, theObjects);
                }

                // Add the risk item to the list.
                theObjects.add(theObject);
			}
		}
	}

	/** 
	 * Create the key for storing data in the classification map.
	 * 
	 * @param    aRes         I18N for translating element name, must not be <code>null</code>.
	 * @param    anElement    The element to get the key for, must not be <code>null</code>.
	 * @return   The requested key, never <code>null</code>.
	 */
	protected String createClassificationKey(Resources aRes, FastListElement anElement) {
		return FastListElementLabelProvider.INSTANCE.getLabel(anElement);
	}

    /** 
     * Return the requested classifications for the given element.
     * 
     * @param    anObject    The object to get the classifications for, may be <code>null</code>.
     * @return   The requested collection of classifications, never <code>null</code>
     */
    @SuppressWarnings("unchecked")
    protected Collection<FastListElement> getClassification(Object anObject) {
        if (anObject instanceof Wrapper) {
            Object theValue = ((Wrapper) anObject).getValue(this.attrName);

            if (theValue instanceof Collection) { 
                return (Collection<FastListElement>) theValue;
            }
        }

        return Collections.emptyList();
    }

	/**
	 * This method returns the classifications.
	 *
	 * @return Returns the classifications.
	 */
	public Map<String, List<Object>> getClassifications() {
		return this.classifications;
	}
}
