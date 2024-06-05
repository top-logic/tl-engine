/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.reporting.report.exception.LanguageNotSupportedException;
import com.top_logic.reporting.report.exception.NotAllowedException;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.exception.UnsupportedException;
import com.top_logic.reporting.report.model.objectproducer.ObjectProducer;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.reporting.report.view.producer.jfc.JFCReportProducer;
import com.top_logic.util.Resources;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * 
 * @deprecated use {@link RevisedReport}
 */
@Deprecated
public class Report implements ReportConstants {

    private ObjectProducer businessObjectProducer;
    
    /** See {@link #getProducer()}. */
    private JFCReportProducer producer;

    /** See {@link #getLanguage()}. */
    private String language;
    
    /**
     * The map contains the titles of all supported report types ({@link ReportUtilities#getSupportedReportTypes()}). 
     * For all supported report types are the titles never <code>null</code>.
     * The keys are report types (e.g. {@link ReportConstants#REPORT_TYPE_BAR_CHART}). 
     * The values are {@link Title}s.
     */
    private HashMap titles;
    
    /** See {@link #getFilters()}. */
    private List filters;
    
    /**
     * The map contains orientations for the report types ({@link ReportUtilities#getSupportedReportTypes()}).
     * For all supported report types are the orientations never <code>null</code>.
     * The keys are report types (e.g. {@link ReportConstants#REPORT_TYPE_BAR_CHART}). 
     * The values are strings ({@link ReportConstants#ORIENTATION_HORIZONTAL} or {@link ReportConstants#ORIENTATION_VERTICAL}).
     */
    private HashMap orientations;
    
    /** See {@link #getLegend()}. */
    private Legend legend;
    
    /** See {@link #isAntiAlias()}. */
    private boolean antiAlias;
    
    /** See {@link #getRangeAxis()}. */
    private Axis rangeAxis;
    
    /** See {@link #getDomainAxis()}. */
    private Axis domainAxis;
    
    /** See {@link #getChartBackground()}. */
    private Color chartBackground;
    
    /** See {@link #isUseGradientPaint()}. */
    private boolean useGradientPaint;
    
    /** See {@link #isUseSameCategoryColor()}. */
    private boolean useSameCategoryColor;
    
    /** See {@link #isUseIntValuesForRangeAxis()}. */
    private boolean useIntValuesForRangeAxis;
    
    /** See {@link #isShowItemLabels()}. */
    private boolean showItemLables;
    
    /** See {@link #getCategoryItemLabelGenerator()}. */
    private CategoryItemLabelGenerator itemLabelGenerator;
    
    /** See {@link #getCategoryMainFunction()}. */
    private PartitionFunction mainFunction;
    /** See {@link #getCategorySubFunction()}. */
    private PartitionFunction subFunction;

    /** 
     * Creates a {@link Report}.
     */
    public Report () {
        this.titles          = initTitles(); 
        this.language        = initLanguage();
        this.filters         = new ArrayList();
        this.orientations    = initOrientations();
        this.antiAlias       = true;
        this.rangeAxis       = new Axis(getInitI18NHashMap());
        this.domainAxis      = new Axis(getInitI18NHashMap());
        this.chartBackground = initChartBackground();
        this.itemLabelGenerator = new StandardCategoryItemLabelGenerator();
    }

    /** 
     * Init the report with the current user language.
     */
    protected String initLanguage() {
        return Resources.getInstance().getLocale().getLanguage();
    }

    /** 
     * This method inits the chart background color.
     */
    protected Color initChartBackground() {
        return Color.WHITE;
    }
    
    /**
     * This method returns a map with empty string as messages for any supported
     * language (de='' and en='').
     */
    protected HashMap getInitI18NHashMap() {
        return ReportUtilities.createEmptyI18NMap();
    }
    
    /**
     * Init the titles of all supported report types. Subclasses can change this
     * behavior. See {@link #titles}.
     */
    protected HashMap initTitles() {
        HashMap  theTitles = new HashMap();
        Iterator theIter   = ReportUtilities.getSupportedReportTypes().iterator();
        while(theIter.hasNext()) {
            theTitles.put(theIter.next(), new Title(getInitI18NHashMap()));
        }
        
        return theTitles;
    }

    /**
     * Init the orientations of all supported report types. Subclasses can
     * change this behavior. See {@link #titles}.
     */
    protected HashMap initOrientations() {
        HashMap  theOrientations = new HashMap();
        Iterator theIter         = ReportUtilities.getSupportedReportTypes().iterator();
        while(theIter.hasNext()) {
            theOrientations.put(theIter.next(), ORIENTATION_VERTICAL);
        }
        
        return theOrientations;
    }

    /**
     * This method creates and returns the categories of this report. The
     * categories are created by the {@link #mainFunction} and
     * {@link #subFunction}.
     */
    public List getPartitions() {
        PartitionFunction theMainFunction = getCategoryMainFunction();
        List thePartitions               = theMainFunction.processObjects(getFilteredObjects());
        
        if (theMainFunction.ignoreEmptyPartitions()) {
            for (Iterator theIter = thePartitions.iterator(); theIter.hasNext();) {
                if (((Partition) theIter.next()).getObjects(false).isEmpty()) {
                    theIter.remove();
                }
            }
        }
        
        PartitionFunction theSubFunction = getCategorySubFunction();
        if (theSubFunction != null) {
            for (Iterator theIter = thePartitions.iterator(); theIter.hasNext();) {
                Partition thePartition          = (Partition) theIter.next();
                List theSubPartitions           = theSubFunction.processObjects(thePartition.getObjects(false));
                thePartition.addSubPartitions(theSubPartitions);
            }
            
//            this.setCategoryItemLabelGenerator(theSubFunction.getCategoryItemLabelGenerator());
        }
        else {
//            this.setCategoryItemLabelGenerator(theMainFunction.getCategoryItemLabelGenerator());
        }
        
        return thePartitions;
    }
    
    /**
     * This method returns the {@link JFreeChart} representation of this report.
     */
    public JFreeChart getChart() {
        return this.getProducer().produce(this);
    }
    
    /** 
     * Returns <code>true</code> if the item labels are shown into the chart.
     */
    public boolean isShowItemLabels() {
        return this.showItemLables;
    }
    
    /** See {@link #isShowItemLabels()}. */
    public void setShowItemLabels(boolean aFlag) {
        this.showItemLables = aFlag;
    }
    
    /**
     * This method returns the category main function. The category main
     * function divides the business objects into categories.
     */
    public PartitionFunction getCategoryMainFunction() {
        return this.mainFunction;
    }
    
    /** See {@link #getCategoryMainFunction()}. */
    public void setCategoryMainFunction(PartitionFunction aCategoryFunction) {
        this.mainFunction = aCategoryFunction;
    }
    
    /**
     * This method returns the category sub function. The category sub function
     * divides the business objects into sub categories under the main
     * categories.
     */
    public PartitionFunction getCategorySubFunction() {
        return this.subFunction;
    }
    
    /** See {@link #getCategorySubFunction()}. */
    public void setCategorySubFunction(PartitionFunction aCategoryFunction) {
        this.subFunction = aCategoryFunction;
    }
    
    /**
     * This method returns <code>true</code> if integer values are used for
     * the range axis instead of double values.
     */
    public boolean isUseIntValuesForRangeAxis() {
        return this.useIntValuesForRangeAxis;
    }
    
    /** See {@link #isUseIntValuesForRangeAxis()}. */
    public void setUseIntValuesForRangeAxis(boolean aFlag) {
        this.useIntValuesForRangeAxis = aFlag;
    }
    
    /**
     * This method returns <code>true</code> if gradient paint is used,
     * <code>false</code> otherwise.
     */
    public boolean isUseGradientPaint() {
        return this.useGradientPaint;
    }

    /** See {@link #isUseGradientPaint()}. */
    public void setUseGradientPaint(boolean aFlag) {
        this.useGradientPaint = aFlag;
    }
    
    /**
     * This method returns <code>true</code> if the same color for all
     * categories is used, <code>false</code> otherwise.
     */
    public boolean isUseSameCategoryColor() {
        return this.useSameCategoryColor;
    }
    
    /** See {@link #isUseSameCategoryColor()}. */
    public void setUseSameCategoryColor(boolean aFlag) {
        this.useSameCategoryColor = aFlag;
    }
    
//    /** 
//     * Returns the business object type as string. 
//     */
//    public String getBusinessObjectType() {
//        return this.businessObjectType;
//    }
//    
//    /**
//     * Sets the business object type.
//     * 
//     * @param aBusinessObjectType
//     *        The business object type. 
//     *        Must not be <code>null</code> or empty.
//     */
//    public void setBusinessObjectType(String aBusinessObjectType) {
//        this.businessObjectType = aBusinessObjectType;
//    }
    
    /** 
     * This method returns the background color of the chart.
     */
    public Color getChartBackground() {
        return this.chartBackground;
    }
    
    /** See {@link #getChartBackground()}. */
    public void setChartBackground(Color aColor) {
        this.chartBackground = aColor;
    }
    
    /** 
     * This method returns the range axis of this report.
     */
    public Axis getRangeAxis() {
        return this.rangeAxis;
    }
    
    /** See {@link #getRangeAxis()}.  */
    public void setRangeAxis(Axis anAxis) {
        this.rangeAxis = anAxis;
    }
    
    /**
     * This method returns <code>true</code> is the range axis of the report
     * is visible.
     */
    public boolean isRangeAxisMessageVisible() {
        return this.rangeAxis != null ? this.rangeAxis.isVisible() : false;
    }
    
    /**
     * This method returns <code>true</code> is the domain axis of the report
     * is visible.
     */
    public boolean isDomainAxisMessageVisible() {
        return this.domainAxis != null ? this.domainAxis.isVisible() : false;
    }
    
    /**
     * This method returns <code>true</code> if the range axis grid line is
     * visible, <code>false</code> otherwise.
     */
    public boolean isRangeAxisGridLineVisible() {
        return this.rangeAxis != null ? this.rangeAxis.hasGridline() : false;
    }
    
    /**
     * This method returns <code>true</code> if the range axis grid line is
     * visible, <code>false</code> otherwise.
     */
    public boolean isDomainAxisGridLineVisible() {
        return this.domainAxis != null ? this.domainAxis.hasGridline() : false;
    }
    
    /** 
     * This method returns the range axis font or <code>null</code>.
     */
    public Font getRangeAxisFont() {
        return this.rangeAxis.getFont();
    }
    
    /** 
     * This method returns the domain axis font or <code>null</code>.
     */
    public Font getDomainAxisFont() {
        return this.domainAxis.getFont();
    }
    
    /** 
     * This method returns the range axis font or <code>null</code>.
     */
    public Font getRangeAxisValueFont() {
        return this.rangeAxis.getValueFont();
    }
    
    /** 
     * This method returns the domain axis font or <code>null</code>.
     */
    public Font getDomainAxisValueFont() {
        return this.domainAxis.getValueFont();
    }
    
    /** 
     * This method returns the domain axis of this report.
     */
    public Axis getDomainAxis() {
        return this.domainAxis;
    }
    
    /** See {@link #getDomainAxis()}.  */
    public void setDomainAxis(Axis anAxis) {
        this.domainAxis = anAxis;
    }
    
    /**
     * This method returns the range axis message for the current language.
     */
    public String getRangeMessage() {
        return this.rangeAxis.getMessage(getLanguage());
    }
    
    /**
     * This method returns the domain axis message for the current language.
     */
    public String getDomainMessage() {
        return this.domainAxis.getMessage(getLanguage());
    }
    
    /**
     * This method returns whether anti-aliasing is used.
     */
    public boolean isAntiAlias() {
        return this.antiAlias;
    }
    
    /** See {@link #isAntiAlias()}. */
    public void setAntiAlias(boolean isAntiAlias) {
        this.antiAlias = isAntiAlias;
    }
    
    /**
     * This method returns the title font of current report type or
     * <code>null</code>.
     */
    public Font getTitleFont() {
        return getTitle().getFont();
    }
    
    /** See {@link #orientations}. */
    public String getOrientation() {
        return (String) this.orientations.get(getType());
    }
    
    /** See {@link #orientations}. */
    public void setOrientation(String aReportType, String aOrientation) {
        this.orientations.put(aReportType, aOrientation);
    }
    
    /**
	 * Returns the selected language for the report. The language must be one of the supported
	 * languages ({@link ResourcesModule#getSupportedLocaleNames()}) but is never <code>null</code>.
	 */
    public String getLanguage() {
        return this.language;
    }
    
    /** See {@link #getLanguage()}. */
    public void setLanguage(String aLanguage) {
		String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
        
		if (!ArrayUtil.contains(supportedLanguages, aLanguage)) {
			throw new LanguageNotSupportedException(this.getClass(), "The language '" + aLanguage
				+ "' is not supported. Please use one of the supported languages '" + Arrays.asList(supportedLanguages)
				+ '.');
        }
        
        this.language = aLanguage;
    }
    
    /** 
     * This method returns if the current title is visible.
     */
    public boolean isTitleVisible() {
        return getTitle().isVisible();
    }
    
    /** 
     * This method returns the alignment of the current title.
     */
    public String getTitleAlign() {
        return getTitle().getAlign();
    }
    
    /**
     * This method returns the title message for the selected language and
     * report type.
     */
    public String getTitleMessage() {
        return getTitle().getMessage(getLanguage());
    }
    
    /**
     * This method returns the title message for the given language and report
     * type.
     */
    public String getTitleMessage(String aLanguage) {
        return getTitle().getMessage(aLanguage);
    }
    
    /** 
     * Returns the {@link Title} for the selected report type.
     * See {@link #titles}.
     */
    public Title getTitle() {
        return (Title) this.titles.get(getType());
    }
    
    /** 
     * Returns the {@link Title} for the given report type.
     * See {@link #titles}.
     */
    public Title getTitle(String aReportType) {
        return (Title) this.titles.get(aReportType);
    }
    
    /**
     * Sets the {@link Title} for the given report type.
     * 
     * @param aReportType 
     * 
     * See {@link #titles}.
     */
    public void setTitle(String aReportType, Title aTitle) {
        if (!ReportUtilities.isReportTypeSupported(aReportType)) {
            throw new UnsupportedException(this.getClass(), "The report type '" + aReportType + "' is not supported.");
        }
        if (aTitle == null) {
            throw new NullPointerException(this.getClass() + ": The title must not be null!");
        }
        this.titles.put(aReportType, aTitle);
    }
    
    /**
     * Returns the producer of this report. 
     */
    public JFCReportProducer getProducer() {
        return this.producer;
    }
    
    /**
     * This method returns the report type as string. The report type is defined
     * by the producer and it is never <code>null</code> or empty.
     * Constants for the report type {@link ReportConstants}.
     */
    public String getType() {
        return getProducer().getReportType();
    }
    
    /**
     * See {@link #getProducer()}. 
     */
    public void setProducer(JFCReportProducer aProducer) {
        if (aProducer == null) {
            throw new NotAllowedException(this.getClass(), "It is not allowed to set the producer of an report to 'null'.");
        }
        
        if(aProducer != getProducer())
        this.producer = aProducer;
    }
    
    /**
     * Returns the list of filter of this report. The list is never
     * <code>null</code> but maybe empty. The list entries are
     * {@link MetaAttributeFilter}s. 
     * 
     * Note the collection is read-only!
     */
    public List getFilters() {
        return Collections.unmodifiableList(this.filters);
    }
    
    /**
     * Adds the given filter to the filter list.
     * 
     * @param aFilter
     *        A {@link MetaAttributeFilter}. Must not be <code>null</code>.
     */
    public void addFilter(MetaAttributeFilter aFilter) {
        if (aFilter == null) {
            throw new NullPointerException("The MetaAttributeFilter must NOT be null!");
        }
        this.filters.add(aFilter);
        Collections.sort(this.filters);
    }
    
    /**
     * Adds the given filters to the filter list.
     * 
     * @param someFilters
     *        A list of {@link MetaAttributeFilter}s. Must not be
     *        <code>null</code>.
     */
    public void addFilters(List someFilters) {
        if (!FilterUtil.containsOnly(MetaAttributeFilter.class, someFilters)) {
            throw new IllegalArgumentException("The filter list must only contains elements from type MetaAttributeFilter.");
        }
        
        this.filters.addAll(someFilters);
        Collections.sort(this.filters);
    }
    
    /**
     * This method removes the given filter from the filter list.
     * 
     * @param aFilter
     *        A {@link MetaAttributeFilter}. Must not be <code>null</code>.
     * @return Returns <code>true</code> if the filter could be removed.
     */
    public boolean removeFilter(MetaAttributeFilter aFilter) {
        return getFilters().remove(aFilter);
    }
    
    /**
     * Resets the filter list.
     */
    public void resetFilters() {
        this.filters = new ArrayList();
    }
    
    /**
	 * Returns a collection of filtered wrappers.
	 */
    public Collection getFilteredObjects() {
//        String theType = getBusinessObjectType();
//        
//        if (StringServices.isEmpty(theType)) {
//            Logger.info("The report has no business object type.", this);
//            return Collections.EMPTY_LIST;
//        }
        
//        Collection theFilteredObjects = getAllWrapperFor(theType);
        Collection theFilteredObjects = getBusinessObjectProducer().getObjects();
        List       theFilters         = getFilters();
        for (Iterator theIter = theFilters.iterator(); theIter.hasNext();) {
            CollectionFilter theFilter = (CollectionFilter) theIter.next();
            try {
                theFilteredObjects = theFilter.filter(theFilteredObjects);
            }
            catch (Exception nsae) {
                throw new ReportingException(getClass(), "", nsae);
            }
        }
        
        return theFilteredObjects;
    }
    
//    private Collection getAllWrapperFor(String aType) {
//        Collection theKOs    = KnowledgeBaseFactory.getDefaultKnowledgeBase().getAllKnowledgeObjects(aType);
//        ArrayList  theResult = new ArrayList(theKOs.size());
//
//        WrapperFactory theFactory = WrapperFactory;
//        for (Iterator theIter = theKOs.iterator(); theIter.hasNext();) {
//            KnowledgeObject theKO = (KnowledgeObject) theIter.next();
//            theResult.add(theFactory.getWrapper(theKO));
//        }
//        
//        return theResult;
//    }
    
    /**
     * This method returns <code>true</code> if the legend is visible,
     * <code>false</code> otherwise. 
     */
    public boolean isLegendVisible() {
        return this.legend != null ? this.legend.isVisible() : false;
    }
    
    /**
     * This method returns the legend alignment or <code>null</code>. Null is
     * returned if no legend exists.
     * 
     * Please, use the alignment constant ({@link ReportConstants}).
     */
    public String getLegendAlign() {
        return this.legend != null ? this.legend.getAlign() : null;
    }
    
    /**
     * This method returns the font of the legend of this report or
     * <code>null</code>.
     */
    public Font getLegendFont() {
        return this.legend != null ? this.legend.getFont() : null;
    }
    
    /**
     * This method returns the legend of the chart representation of this
     * report. The legend can be <code>null</code>.
     */
    public Legend getLegend() {
        return this.legend;
    }
    
    /** See {@link #getLegend()}. */
    public void setLegend(Legend aLegend) {
        this.legend = aLegend;
    }
    
    @Override
	public String toString() {
        String theResult = this.getClass() + "[producer="     + getProducer().getReportType() + 
                                             ",titleMessage=" + getTitleMessage()             + 
                                             ",language="     + getLanguage()                 + 
                                             "]";
        return theResult;
    }
    
    /**
     * This method returns a {@link CategoryItemLabelGenerator} used 
     */
    public CategoryItemLabelGenerator getCategoryItemLabelGenerator() {
        return this.itemLabelGenerator;
    }

     /**
      * See {@link #getCategoryItemLabelGenerator()}
      */
    public void setCategoryItemLabelGenerator(CategoryItemLabelGenerator aGenerator) {
        this.itemLabelGenerator = aGenerator;
    }

    protected ObjectProducer getBusinessObjectProducer() {
        
        if (this.businessObjectProducer == null) { // init default producer
            throw new ReportingException(this.getClass(), "The object producer must not be null");
//            WrapperObjectProducer theProducer = new WrapperObjectProducer();
//            Map theOptionMap = new HashMap(1);
//            theOptionMap.put(ObjectProducer.PROPERTY_OBJECT_TYPE, this.getBusinessObjectType());
//            theProducer.init(theOptionMap);
//            this.businessObjectProducer = theProducer;
        }
        return businessObjectProducer;
    }

    public void setBusinessObjectProducer(ObjectProducer anObjectProducer) {
        this.businessObjectProducer = anObjectProducer;
    }
}

