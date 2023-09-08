/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.base.chart.configurator.BarChartConfigurator;
import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLType;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.util.Resources;

/**
 * Create a stacked bar chart based in the normal ClassificationDatasetGenerator.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TimeslotClassificationDatasetGenerator extends ClassificationDatasetGenerator {

    private List<RegularTimePeriod> slots;

    private Set<Wrapper> historicObjects;

    private ClassificationModel model;

	private int currentSlot;

    /** 
     * Creates a {@link TimeslotClassificationDatasetGenerator}.
     */
    public TimeslotClassificationDatasetGenerator(ClassificationModel aModel, String aListName, String anAttrName, boolean hasNotAssigned, List<RegularTimePeriod> someSlots) {
        super(aModel, aListName, anAttrName, hasNotAssigned);

        this.model = aModel;
        this.slots = someSlots;
    }

    @Override
    public Dataset createDataset() {
        ExtendedCategoryDataset   theDataset = new ExtendedCategoryDataset();
        PreloadContext            theContext = new PreloadContext();
		Map<String, List<Object>> theItemMap = new HashMap<>();

		this.currentSlot = 0;

        for (RegularTimePeriod theSlot : this.slots) {
            this.moveInTime(theSlot, theContext);

			this.convertDataIntoTimeDataSet(theDataset, super.createDataset(), theSlot);

			// Need to save the classifications from super implementation.
			Map<String, List<Object>> theInnerMap = this.getClassifications();

			if (theInnerMap != null) {
				for (Entry<String, List<Object>> theEntry : theInnerMap.entrySet()) {
					theItemMap.put(theEntry.getKey(), theEntry.getValue());
				}
			}

			this.currentSlot++;
        }

		// Restore the classifications from the temporary map
		Map<String, List<Object>> theRealMap = this.getClassifications();

		for (Entry<String, List<Object>> theEntry : theItemMap.entrySet()) {
			theRealMap.put(theEntry.getKey(), theEntry.getValue());
		}

        return theDataset;
    }

    @Override
    protected Set<Object> getObjects() {
        return CollectionUtil.dynamicCastView(Object.class, this.historicObjects);
    }

	@Override
	protected String createClassificationKey(Resources anRes, FastListElement anElement) {
		return Integer.toString(this.currentSlot) + ':' + FastListElementLabelProvider.INSTANCE.getLabel(anElement);
	}

    /**
	 * Convert the data produced by the super implementation into the right time slot.
	 * 
	 * @param aDataset
	 *        The data set to convert the data to, must not be <code>null</code>.
	 * @param aSource
	 *        The source data to get the data from, must not be <code>null</code>.
	 * @param aSlot
	 *        The current time period, must not be <code>null</code>.
	 */
	protected void convertDataIntoTimeDataSet(ExtendedCategoryDataset aDataset, Dataset aSource, RegularTimePeriod aSlot) {
        ExtendedCategoryDataset theSource = (ExtendedCategoryDataset ) aSource;

        int     theMaxCols   = theSource.getColumnCount();
        int     theMaxRows   = theSource.getRowCount();
        boolean hasBeenAdded = false;

        for (int theCol = 0; theCol < theMaxCols; theCol++) {
            for (int theRow = 0; theRow < theMaxRows; theRow++) {
                Number theValue  = theSource.getValue(theRow, theCol);
                Object theObject = theSource.getObject(theRow, theCol);

                if ((theValue != null) || (theObject != null)) {
    				String theComp = (String) theSource.getRowKey(theRow);
    				theComp = theComp.substring(theComp.indexOf(':') + 1);

    				aDataset.addValue(theValue, theComp, aSlot, theObject);
    				hasBeenAdded = true;
                }
            }
        }

        if (!hasBeenAdded) {
			aDataset.addValue(0.0d, this.createEmptyColumnKey(), aSlot);
        }
    }

	/**
	 * Return a column key to be used, when no data has been produced for a
	 * {@link RegularTimePeriod time slot}.
	 * 
	 * @return The requested comparable, never <code>null</code>.
	 * @see #convertDataIntoTimeDataSet(ExtendedCategoryDataset, Dataset, RegularTimePeriod)
	 */
	protected Comparable<?> createEmptyColumnKey() {
		TLStructuredTypePart theMA = this.model.getMetaAttribute();

		TLType targetType = theMA.getType();
		if (targetType instanceof TLEnumeration) {
		    FastList theList = (FastList) targetType;

			{
		        List<FastListElement> theElements = theList.elements();

				return MetaLabelProvider.INSTANCE.getLabel(theElements.get(0));
		    }
		}
		else {
			return "";
		}
	}

    /**
	 * Move the objects held by this generator in time.
	 * 
	 * @param aSlot
	 *        The period to move the values to, must not be <code>null</code>.
	 * @param aContext
	 *        The context for preload data, must not be <code>null</code>.
	 * @see #getRevision(RegularTimePeriod)
	 * @see ClassificationDatasetGenerator#getObjects()
	 */
    protected void moveInTime(RegularTimePeriod aSlot, PreloadContext aContext) {
        Revision     theRev      = this.getRevision(aSlot);
        Set<Wrapper> theWrappers = CollectionUtil.dynamicCastView(Wrapper.class, super.getObjects());

        this.historicObjects = new HashSet<>(theWrappers.size());

        for (Wrapper theWrapper : theWrappers) {
            Wrapper theHistWrapper = WrapperHistoryUtils.getWrapper(theRev, theWrapper);

            if (theHistWrapper != null) { 
                this.historicObjects.add(theHistWrapper);
            }
        }

        TLStructuredTypePart theMA = this.model.getMetaAttribute();

        if (!CollectionUtil.isEmptyOrNull(this.historicObjects)) {
			MetaElementUtil.preloadAttributes(aContext, this.historicObjects, new TLStructuredTypePart[] { theMA });
        }
    }

    /**
	 * Return the correct revision for the given time slot.
	 * 
	 * @param aSlot
	 *        The time slot to get the requested revision for, must not be <code>null</code>.
	 * @return The requested revision, never <code>null</code>.
	 */
    protected Revision getRevision(RegularTimePeriod aSlot) {
        Date theDate = aSlot.getEnd();
        Date theNow  = new Date();

		if (theDate.after(theNow)) {
			return Revision.CURRENT;
		}
		else {
			return ((HistoryManager) PersistencyLayer.getKnowledgeBase()).getRevisionAt(theDate.getTime());
		}
    }

    /**
	 * Special bar chart configuration for creating a stacked bar chart.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public static class TimeslotBarChartConfigurator extends BarChartConfigurator {

        // Constructors

        /**
		 * Creates a {@link TimeslotBarChartConfigurator}.
		 * 
		 * The constructor creates intern a new stacked bar chart with the given data set.
		 */
        public TimeslotBarChartConfigurator(boolean legend, CategoryDataset aDataset) {
            this("", "", "", legend, aDataset);
        }

        /**
		 * Creates a {@link TimeslotBarChartConfigurator}.
		 * 
		 * The constructor creates intern a new stacked bar chart with the given data set.
		 */
        public TimeslotBarChartConfigurator(String aTitle, String aXAxisLabel, String aYAxisLabel, boolean aLegend, CategoryDataset aDataset) {
            super(ChartFactory.createStackedBarChart(aTitle, aXAxisLabel, aYAxisLabel, aDataset, PlotOrientation.VERTICAL, aLegend, !TOOLTIPS, true));
        }
    }
}

