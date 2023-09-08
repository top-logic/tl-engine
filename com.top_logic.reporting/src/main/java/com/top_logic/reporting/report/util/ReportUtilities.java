/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;
import com.top_logic.base.chart.dataset.ExtendedTimeSeries;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.common.period.BYHalfYear;
import com.top_logic.reporting.common.period.BYQuarter;
import com.top_logic.reporting.common.period.BYYear;
import com.top_logic.reporting.common.period.HalfYear;
import com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationComponent.AdditionalReportingOptionProvider;
import com.top_logic.reporting.report.exception.UnsupportedException;
import com.top_logic.reporting.report.model.DataSeries;
import com.top_logic.reporting.report.model.DataSet;
import com.top_logic.reporting.report.model.ItemVO;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportTree.ReportNode;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;
import com.top_logic.reporting.report.view.producer.jfc.JFCBarChartProducer;
import com.top_logic.reporting.report.view.producer.jfc.JFCPieChartProducer;
import com.top_logic.reporting.report.view.producer.jfc.JFCReportProducer;
import com.top_logic.util.Resources;

/**
 * The ReportUtilities contains useful static methods.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ReportUtilities implements ReportConstants {

	/** Constants for the path of the reload image. */
	public static final String		PATH_RELOAD_IMG								= "/misc/reload-small.png";
	public static final String		PATH_RELOAD_IMG_DISABLED					= "/misc/reload-small-disabled.png";

	/** Constants for the path of the delete image */
	public static final String		PATH_DELETE_IMG_DISABLED					= "/icons/removeNode_disabled.png";
	public static final String		PATH_DELETE_IMG								= "/icons/removeNode.png";

	/** The section is the global reporting configuration section. */
	public static final String		CONFIGURATION_GLOBAL_REPORTING_SECTION		= "GlobalReportingConfiguration";
	/**
	 * Indicates that vista client are used and we can't use the default toolkit to load images because
	 * the vista color schema is changed ugly.
	 */
	public static final String		CONFIGURATION_USE_VISTA_COMPATIBLE_IMAGES	= "useVistaCompatibleImages";
	private ReportUtilities() {
		// Do nothing.
	}

	/**
	 * This method generates a {@link CategoryDataset} for the given report. This method returns never
	 * <code>null</code> but surely an empty dataset.
	 * 
	 * @param aReport  a {@link Report}.
	 * 
	 * @deprecated Consider using the {@link RevisedReport} and its methods instead.
	 */
	@Deprecated
	public static CategoryDataset generateCategoryDatasetFor(Report aReport) {
		return new ExtendedCategoryDataset();
	}

	public static CategoryDataset generateCategoryDatasetFor(RevisedReport aReport) {
		ExtendedCategoryDataset theResultSet = new ExtendedCategoryDataset();
		ReportNode              theRoot      = (ReportNode) aReport.getReportTree().getRoot();
		
		parseCategoryChildren(theResultSet, theRoot.getChildren(), "", "");
		
		return theResultSet;
	}
	
	private static void parseCategoryChildren(ExtendedCategoryDataset aResultSet, List<ReportNode> someChildren, String aCategory, String aSeries) {
		String theCurrentCategory = aSeries;
		String theCurrentSeries   = aSeries;
		
		for (int i = 0; i < someChildren.size(); i++) {
			ReportNode theChild = someChildren.get(i);
			theCurrentSeries    = theChild.getName();
			
			if (theChild.isLeaf()) {
				String theCategory = aCategory;
				String theSeries   = aSeries;
				
				if (StringServices.isEmpty(aCategory)) {
					theCategory = theCurrentCategory;
					theSeries = theCurrentSeries;
				}
				aResultSet.addValue(theChild.getValue(), theSeries, theCategory);
			}
			else {
				List<ReportNode> theChildren   = theChild.getChildren();
				parseCategoryChildren(aResultSet, theChildren, theCurrentCategory, theCurrentSeries);
			}
		}
	}
	
	public static PieDataset generatePieDatasetFor(RevisedReport aReport) {
	    return new CategoryToPieDataset(generateCategoryDatasetFor(aReport), TableOrder.BY_ROW, 0);
	}

	public static CategoryDataset generateWaterfallDatasetFor(RevisedReport aReport) {
		CategoryDataset theDataset = generateCategoryDatasetFor(aReport);
		return calculateSum(theDataset);
    }

	private static CategoryDataset calculateSum(CategoryDataset aDataset) {
		ExtendedCategoryDataset theDataset = (ExtendedCategoryDataset) aDataset;
		Resources theRessources = Resources.getInstance();
		List theRowKeys = theDataset.getRowKeys();
		List theColKeys = theDataset.getColumnKeys();
		Map<String, Double> theNewVals = new HashMap<>(theRowKeys.size());
		for(Iterator theRows = theRowKeys.iterator(); theRows.hasNext(); ) {
			String theRowKey = (String) theRows.next();
			double theResult = 0.0;
			for(Iterator theCols = theColKeys.iterator(); theCols.hasNext(); ) {
				String theColKey = (String) theCols.next();
				Number value = theDataset.getValue(theRowKey, theColKey);
				if(value != null) {
					theResult += value.doubleValue();
				}
			}
			theNewVals.put(theRowKey, Double.valueOf(theResult));
		}
		String theResultString = theRessources.getString(I18NConstants.WATERFALL_CHART_TOTAL);
		Set<String> theKeys = theNewVals.keySet();
		for(Iterator<String> theIT = theKeys.iterator(); theIT.hasNext(); ) {
			String theKey = theIT.next();
			theDataset.addValue(theNewVals.get(theKey), theKey, theResultString);
		}
		return theDataset;
	}

	/**
	 * Creates an object from the given object, which consists of only JSON compatible types.
	 * Particularly transforms wrappers into a string notation consisting of KB ID and type.
	 * 
	 * @param object
	 *        the object to make JSON compatible
	 * @return a JSON compatible version of the given object
	 * @see #restoreFromJSONCompatibility(Object)
	 * @see JSON
	 */
	public static Object serializeForJSONCompatibility(Object object) {
		if (object instanceof Wrapper) {
			String type = ((Wrapper) object).tTable().getName();
			String id = IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName((Wrapper) object));
			return "$WRAPPER$ID=" + id + "$TYPE=" + type;
		}
		else if (object instanceof Enum) {
			Enum<?> element = (Enum<?>) object;
			return "$ENUM$NAME=" + element.name() + "$TYPE=" + element.getDeclaringClass().getName();
		}
		else if (object instanceof Map) {
			Map<Object, Object> map = MapUtil.newMap(((Map<?, ?>) object).size());
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				map.put(serializeForJSONCompatibility(entry.getKey()), serializeForJSONCompatibility(entry.getValue()));
			}
			return map;
		}
		else if (object instanceof List) {
			List<Object> list = new ArrayList<>((List<?>) object);
			for (int i = 0, length = list.size(); i < length; i++) {
				list.set(i, serializeForJSONCompatibility(list.get(i)));
			}
			return list;
		}
		else if (object instanceof Set) {
			List<Object> list = new ArrayList<>((List<?>) object);
			for (int i = 0, length = list.size(); i < length; i++) {
				list.set(i, serializeForJSONCompatibility(list.get(i)));
			}
			list.add(0, "$SET$");
			return list;
		}
		else if (object instanceof Tuple) {
			List<Object> list = CollectionUtil.toList(TupleFactory.toArray((Tuple) object));
			for (int i = 0, length = list.size(); i < length; i++) {
				list.set(i, serializeForJSONCompatibility(list.get(i)));
			}
			list.add(0, "$TUPLE$");
			return list;
		}
		else if (object instanceof Object[]) {
			List<Object> list = CollectionUtil.toList((Object[]) object);
			for (int i = 0, length = list.size(); i < length; i++) {
				list.set(i, serializeForJSONCompatibility(list.get(i)));
			}
			list.add(0, "$ARRAY$");
			return list;
		}
		return object;
	}

	/**
	 * Restores an object from a JSON compatible version of the object created by the
	 * {@link #serializeForJSONCompatibility(Object)} method.
	 * 
	 * @param object
	 *        the object to restore
	 * @return the restored object
	 * @see #serializeForJSONCompatibility(Object)
	 * @see JSON
	 */
	public static Object restoreFromJSONCompatibility(Object object) {
		if (object instanceof String) {
			String string = ((String) object);
			if (string.startsWith("$WRAPPER$")) {
				int index = string.indexOf("$ID=");
				if (index < 0)
					return null;
				string = string.substring(index + 4);
				index = string.indexOf("$TYPE=");
				if (index < 0)
					return null;
				String id = string.substring(0, index);
				String type = string.substring(index + 6);
				// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
				return WrapperFactory.getWrapper(IdentifierUtil.fromExternalForm(id), type);
			}
			else if (string.startsWith("$ENUM$")) {
				int index = string.indexOf("$NAME=");
				if (index < 0)
					return null;
				string = string.substring(index + 6);
				index = string.indexOf("$TYPE=");
				if (index < 0)
					return null;
				String name = string.substring(0, index);
				String type = string.substring(index + 6);
				try {
					return Enum.valueOf((Class) Class.forName(type), name);
				} catch (ClassNotFoundException ex) {
					return null;
				}
			}
		}
		else if (object instanceof Map) {
			Map<Object, Object> map = MapUtil.newMap(((Map) object).size());
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				map.put(restoreFromJSONCompatibility(entry.getKey()), restoreFromJSONCompatibility(entry.getValue()));
			}
			return map;
		}
		else if (object instanceof List) {
			List<Object> list = new ArrayList<>((List) object);
			Object first = CollectionUtil.getFirst(list);
			if ("$SET$".equals(first)) {
				list.remove(0);
				for (int i = 0, length = list.size(); i < length; i++) {
					list.set(i, restoreFromJSONCompatibility(list.get(i)));
				}
				return CollectionUtil.toSet(list);
			}
			else if ("$TUPLE$".equals(first)) {
				list.remove(0);
				for (int i = 0, length = list.size(); i < length; i++) {
					list.set(i, restoreFromJSONCompatibility(list.get(i)));
				}
				return TupleFactory.newTuple(list);
			}
			else if ("$ARRAY$".equals(first)) {
				list.remove(0);
				for (int i = 0, length = list.size(); i < length; i++) {
					list.set(i, restoreFromJSONCompatibility(list.get(i)));
				}
				return list.toArray();
			}
			else {
				for (int i = 0, length = list.size(); i < length; i++) {
					list.set(i, restoreFromJSONCompatibility(list.get(i)));
				}
				return list;
			}
		}
		return object;
	}

	/**
	 * This method creates a {@link BoxAndWhiskerXYDataset} for a given {@link RevisedReport}. 
	 * 
	 * Each item in this data set consists of 6 values:
	 * <ol>
	 * 	<li>minimum</li>
	 * 	<li>lower quartil</li>
	 * 	<li>mean</li>
	 * 	<li>median</li>
	 * 	<li>upper quartil</li>
	 * 	<li>maximum</li>
	 * </ol>
	 * 
	 * @param aReport which contains all necessary information
	 * @return a {@link BoxAndWhiskerXYDataset}
	 */
	public static BoxAndWhiskerXYDataset generateBoxAndWhiskerXYDatasetFor(RevisedReport aReport) {
		DefaultBoxAndWhiskerXYDataset theDataset = new DefaultBoxAndWhiskerXYDataset("key");
		ReportNode                    theRoot    = (ReportNode) aReport.getReportTree().getRoot();
		
		return theDataset;
	}

	/**
	 * This method takes the {@link DataSet} from a given report and transforms it into a
	 * {@link TimeSeriesCollection}, which is needed by JFChart to produce a
	 * TimeSeriesChartConfigurator.
	 * 
	 * @param aReport a {@link RevisedReport} which is the basis for a chart
	 * 
	 * @return a {@link TimeSeriesCollection} containing the {@link DataSeries} from the report.
	 */
	@Deprecated
	public static TimeSeriesCollection generateTimeSeriesCollectionFor(RevisedReport aReport, List aModel, AdditionalReportingOptionProvider aProvider, TLClass aME) {
		
	    DataSet              theDataSet     = aReport.getDataSet();
		Object               theGranularity = aReport.getGranularity();
		Class<?>             theTimePeriod  = getPeriod(theGranularity, useBusinessYear(aReport.getConfiguration()));
		List                 theDataSeries  = theDataSet.getDataSeries();
		String               thePrefix      = aME.getName();
		TimeSeriesCollection theTimeSeries = new TimeSeriesCollection(TimeZones.systemTimeZone());
		
		for (int i = 0; i < theDataSeries.size(); i++) {
		    double theAccumulation = 0.0;
		    
		    AggregationFunctionConfiguration theConf = (AggregationFunctionConfiguration) aModel.get(i);
		    
		    String       theAttributeName = theConf.getAttributePath();
		    boolean      accumulated      = theConf.isAccumulated();
			DataSeries   theData          = (DataSeries) theDataSeries.get(i);
			String       theAttribute     = getMetaAttributeName(aME, thePrefix + "." + theAttributeName, aProvider);
			ExtendedTimeSeries theSeries  = new ExtendedTimeSeries(getDataSeriesLabel(theData.getLabel(), theAttribute, aME), theTimePeriod);
			List<ItemVO>         theItems = theData.getItemVOs();
			
			for (int j = 0; j < theItems.size(); j++) {
				ItemVO  theItem = theItems.get(j);
				Date    theDate = (Date) ((IntervalCriteria) theItem.getFirstPartition().getCriteria()).getEnd();
				double  theVal  = theItem.getValue().doubleValue();
				RegularTimePeriod thePeriod = getPeriod(theTimePeriod, theDate);
				if(accumulated) {
					theAccumulation += theVal;
					theVal = theAccumulation;
				}
				theSeries.addOrUpdate(thePeriod, Double.valueOf(theVal));
			}
			theTimeSeries.addSeries(theSeries);
		}
		theTimeSeries.setXPosition(TimePeriodAnchor.MIDDLE);
		return theTimeSeries;
	}
	
	public static TimeSeriesCollection generateTimeSeriesCollectionFor(RevisedReport aReport) {
		TimeSeriesCollection theTimeSeries = new TimeSeriesCollection(TimeZones.systemTimeZone());
		ReportNode           theRoot        = (ReportNode) aReport.getReportTree().getRoot();
		Object               theGranularity = aReport.getGranularity();
		Class<?>             theTimePeriod  = getPeriod(theGranularity, useBusinessYear(aReport.getConfiguration()));
		
		parseTimeSeriesChildren(theTimeSeries, theRoot.getChildren(), null, theGranularity, theTimePeriod);
		
		return theTimeSeries;
	}
	
    public static boolean useBusinessYear(ReportConfiguration report) {
        PartitionFunctionConfiguration config = report.getPartitionConfiguration();
        if (config instanceof DatePartitionConfiguration) {
            return ((DatePartitionConfiguration) config).shouldUseBusinessYear();
        }
        return false;
    }
	
	private static void parseTimeSeriesChildren(TimeSeriesCollection aTimeSeries, List<ReportNode> someChildren, RegularTimePeriod aTimePeriod, Object aGranularity, final Class<?> aPeriodClass) {
		for (int i = 0; i < someChildren.size(); i++) {
			ReportNode theChild = someChildren.get(i);
			
			if (theChild.isLeaf()) {
				double             theVal    = theChild.getValue().doubleValue();
				ExtendedTimeSeries theSeries = (ExtendedTimeSeries) aTimeSeries.getSeries(theChild.getName());
				AggregationFunctionConfiguration theConf = theChild.getConfig();
				boolean            addSeries = false;
				
				boolean accumulated = theConf.isAccumulated();
				
				if (theSeries == null) {
					theSeries = new ExtendedTimeSeries(theChild.getName(), aPeriodClass);
					addSeries = true;
				}
				if (!StringServices.isEmpty(aTimePeriod)) {
					int itemCount = theSeries.getItemCount();
					
					if (itemCount > 0 && accumulated) {
						Number theSeriesVal = theSeries.getValue(itemCount - 1);
						
						if (theSeriesVal != null) {
							theVal += theSeriesVal.doubleValue();
						}
					}
					theSeries.addOrUpdate(aTimePeriod, Double.valueOf(theVal));
					
					if (addSeries) {
						aTimeSeries.addSeries(theSeries);
					}
				}
			}
			else {
				Date              theDate     = (Date) ((IntervalCriteria) theChild.getCriteria()).getEnd();
				RegularTimePeriod thePeriod   = getPeriod(aPeriodClass, theDate);
				List<ReportNode>  theChildren = theChild.getChildren();
				
				parseTimeSeriesChildren(aTimeSeries, theChildren, thePeriod, aGranularity, aPeriodClass);
			}
		}
	}

	private static String getDataSeriesLabel(String aFunc, String anAttrib, TLClass aME) {
		Resources theRes = Resources.getInstance();
		{
			return (aFunc + " ("  + anAttrib + ")");
		}
	}
	
	
	/**
	 * Takes a {@link TLClass} and an attribute name which begins with the
	 * given {@link TLClass}s type. This prefix is removed and replaced
	 * with the real prefix, depending on the current {@link TLStructuredTypePart}.
	 * This is necessary to support attribute paths separated via '.' to support
	 * derived attribute names.
	 * 
	 * @param aME              the {@link TLClass} which type is used as a "worng" prefix
	 *                         for the given attribute
	 * @param anAttributeName  Format: metaElementType.attributeName
	 * 
	 * @return the translated attribute name
	 */
	private static String getMetaAttributeName(TLClass aME, String anAttributeName) {
		if(aME == null) {
			return anAttributeName;
		}
		String theMEPrefix = aME.getName();
		String theAttributeName = anAttributeName;
		
		if(theAttributeName.indexOf(theMEPrefix) != -1) {
			theAttributeName = theAttributeName.substring(theMEPrefix.length() + 1);
		}
		try {
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(aME, theAttributeName);
			theAttributeName = AttributeOperations.getMetaElement(theMA).getName() + '.' + theAttributeName;
		}
		catch (NoSuchAttributeException ex) {
			Logger.error("Requested attribute " + theAttributeName + " does not exist for " + theMEPrefix, ex, ReportUtilities.class);
		}
		return Resources.getInstance().getString(ResKey.legacy(theAttributeName), theAttributeName);
	}
	
	private static String getMetaAttributeNameFromProvider(TLClass aME, String anAttributeName, AdditionalReportingOptionProvider aProvider) {
		String thePrefix = aME.getName();
		try {
			TLStructuredTypePart theMA = null;
			if(aProvider != null && aProvider.hasOption(anAttributeName)) {
				theMA = aProvider.getMetaAttributeForReportingOption(anAttributeName);
				return Resources.getInstance().getString(ResKey.legacy(anAttributeName));
			}
			else {
				anAttributeName = anAttributeName.substring(thePrefix.length() + 1);
				theMA = MetaElementUtil.getMetaAttribute(aME, anAttributeName);
			}
			anAttributeName = AttributeOperations.getMetaElement(theMA).getName() + '.' + anAttributeName;
		}
		catch (NoSuchAttributeException ex) {
			Logger.error("Requested attribute " + anAttributeName + " does not exist for " +thePrefix, ex, ReportUtilities.class);
		}
		return Resources.getInstance().getString(ResKey.legacy(anAttributeName));
	}

	private static String getMetaAttributeName(TLClass aME, String anAttributeName, AdditionalReportingOptionProvider aProvider) {
	    if (aProvider != null) {
	        return getMetaAttributeNameFromProvider(aME, anAttributeName, aProvider);
	    }
	    else {
	        return getMetaAttributeName(aME, anAttributeName);
	    }
	}
	
	private static Class getPeriod(Object aGranularity, boolean useBusinessYear) {
		if(aGranularity instanceof Integer) {
			return getIntegerPeriod((Integer) aGranularity, useBusinessYear);
		}
		else {
			return getStringPeriod((Long) aGranularity, useBusinessYear);
		}
	}
	
	private static Class<?> getIntegerPeriod(Integer aGranularity, boolean useBusinessYear) {
		Class<?> theResult;
		int theGranularity = aGranularity.intValue();
		switch (theGranularity) {
		case DateConstants.DAY:
			theResult = Day.class;
			break;
		case DateConstants.WEEK:
			theResult = Week.class;
			break;
		case DateConstants.MONTH:
			theResult = Month.class;
			break;
		case DateConstants.QUARTER:
		    theResult = getQuarterPeriodClass(useBusinessYear);
		    break;
	    case DateConstants.HALFYEAR:
	        theResult = getHalfYearPeriodClass(useBusinessYear);
	        break;
		case DateConstants.YEAR:
			theResult = getYearPeriodClass(useBusinessYear);
			break;
		default:
			theResult = Hour.class;
		break;
		}
		return theResult;
	}
	
	private static Class<?> getStringPeriod(Long aGranularity, boolean useBusinessYear) {
		if (TimeRangeFactory.HOUR_INT.equals(aGranularity)) {
		    return Hour.class;
		}
		else if (TimeRangeFactory.DAY_INT.equals(aGranularity)) {
		    return Day.class;
		}
		else if (TimeRangeFactory.WEEK_INT.equals(aGranularity)) {
		    return Week.class;
		}
		else if (TimeRangeFactory.MONTH_INT.equals(aGranularity)) {
		    return Month.class;
		}
		else if (TimeRangeFactory.QUARTER_INT.equals(aGranularity)) {
		    return getQuarterPeriodClass(useBusinessYear);
		}
		else if (TimeRangeFactory.HALFYEAR_INT.equals(aGranularity)) {
		    return getHalfYearPeriodClass(useBusinessYear);
		}
		else if (TimeRangeFactory.YEAR_INT.equals(aGranularity)) {
		    return getYearPeriodClass(useBusinessYear);
		}
		else {
		    throw new IllegalArgumentException("Granularity of '" + aGranularity + "' cannot be translated into a JFreeChart class");
		}
	}
	
	public static Class<? extends RegularTimePeriod> getQuarterPeriodClass(boolean useBusinessYear) {
	    return useBusinessYear ? BYQuarter.class : Quarter.class;
	}
	
	public static Class<? extends RegularTimePeriod> getYearPeriodClass(boolean useBusinessYear) {
	    return useBusinessYear ? BYYear.class : Year.class;
	}
	
	public static Class<? extends RegularTimePeriod> getHalfYearPeriodClass(boolean useBusinessYear) {
	    return useBusinessYear ? BYHalfYear.class : HalfYear.class;
	}
	
	private static RegularTimePeriod getPeriod(Class<?> aClass, Date aDate) {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		TimeZone timeZone = context.getSubSessionContext().getCurrentTimeZone();
		Locale locale = context.getSubSessionContext().getCurrentLocale();
		return RegularTimePeriod.createInstance(aClass, aDate, timeZone, locale);
	}
	
	/**
	 * This method generates a {@link PieDataset} for the given report. This method returns never
	 * <code>null</code> but surely an empty dataset.
	 * 
	 * @param aReport A {@link Report}.
	 * 
	 * @deprecated use {@link RevisedReport} instead.
	 */
	@Deprecated
	public static PieDataset generatePieDatasetFor(Report aReport) {
		return new DefaultPieDataset();
	}

	@Deprecated
	public static JFCReportProducer getRendererFor(String aReportType) {
		if (aReportType.equalsIgnoreCase(REPORT_TYPE_BAR_CHART)) {
			return JFCBarChartProducer.INSTANCE;
		}
		if (aReportType.equalsIgnoreCase(REPORT_TYPE_PIE_CHART)) {
			return JFCPieChartProducer.INSTANCE;
		}
		if (aReportType.equalsIgnoreCase(REPORT_TYPE_TABLE)) {
			return null;
		}

		throw new UnsupportedException(ReportUtilities.class,
				"Could not found a report renderer for the type '" + aReportType
						+ "'. This type is not supported.");
	}

	/**
	 * This method returns a unmodifiable list with the supported report types. The report types (list
	 * elements) are {@link String}s.
	 */
	@Deprecated
	public static List<String> getSupportedReportTypes() {
		ArrayList<String> theReportTypes = new ArrayList<>(2);
		theReportTypes.add(REPORT_TYPE_CHART);
		theReportTypes.add(REPORT_TYPE_TABLE);

		return Collections.unmodifiableList(theReportTypes);
	}

	/**
	 * This method returns whether the given report type is supported.
	 */
	@Deprecated
	public static boolean isReportTypeSupported(String aReportType) {
		return getSupportedReportTypes().contains(aReportType);
	}

	/**
	 * This method returns for a style string the java font constant.
	 * 
	 * @param aFontStyle  A font style (e.g. 'bold').
	 */
	public static int getFontStyle(String aFontStyle) {
		if (aFontStyle.equalsIgnoreCase(FONT_STYLE_PLAIN)) {
			return Font.PLAIN;
		}
		if (aFontStyle.equalsIgnoreCase(FONT_STYLE_BOLD)) {
			return Font.BOLD;
		}
		if (aFontStyle.equalsIgnoreCase(FONT_STYLE_ITALIC)) {
			return Font.ITALIC;
		}

		throw new UnsupportedException(ReportUtilities.class, "The font style '" + aFontStyle
				+ "' is not supported. The supported styles are '" + FONT_STYLE_PLAIN + "', '"
				+ FONT_STYLE_BOLD + "' and '" + FONT_STYLE_ITALIC);
	}

	/**
	 * This method returns the a {@link RectangleEdge} for the given string.
	 * 
	 * @param anAlign An alignment. Must not be <code>null</code>.
	 */
	public static RectangleEdge getLegendAlign(String anAlign) {
		if (anAlign.equalsIgnoreCase(ALIGN_TOP)) {
			return RectangleEdge.TOP;
		}
		if (anAlign.equalsIgnoreCase(ALIGN_RIGHT)) {
			return RectangleEdge.RIGHT;
		}
		if (anAlign.equalsIgnoreCase(ALIGN_BOTTOM)) {
			return RectangleEdge.BOTTOM;
		}
		if (anAlign.equalsIgnoreCase(ALIGN_LEFT)) {
			return RectangleEdge.LEFT;
		}

		throw new UnsupportedException(ReportUtilities.class, "The legend alignment '" + anAlign
				+ "' is not supported. The supported alignments are '" + ALIGN_TOP + "', '"
				+ ALIGN_RIGHT + "', '" + ALIGN_BOTTOM + "' and '" + ALIGN_LEFT + "'.");
	}

	/**
	 * This method returns a i18n map with all supported languages. Keys: language shortcuts (e.g. 'en'
	 * or 'de') Values: empty strings
	 */
	public static HashMap<String, String> createEmptyI18NMap() {
		String[] theSupportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
		HashMap<String, String> theMap = new HashMap<>(theSupportedLanguages.length);

		for (int i = 0; i < theSupportedLanguages.length; i++) {
			theMap.put(theSupportedLanguages[i], "");
		}

		return theMap;
	}

	/**
	 * This method returns the current theme background color.
	 * 
	 * @return Returns the current theme background color.
	 */
	public static Color getThemeBackgroundColor() {
		return JFreeChartComponent.getThemeBackgroundColor();
	}

    /**
     * This method returns the current theme background color for the content of the chart himself.
     * 
     * @return    Returns the current theme background color for the chart content.
     */
	public static Color getThemeChartBackgroundColor() {
		return JFreeChartComponent.getThemeChartBackgroundColor();
	}

	/**
	 * Returns <code>true</code> if the vista compatible images should be used, <code>false</code>
	 * otherwise. If the vista compatible images are used it isn't possible to load images with the
	 * default toolkit. If the default window toolkit is used the color schema of vista is changed to
	 * ugly default.
	 */
	public static boolean useVistaCompatibleImages() {
		return ConfigurationUtil.getBoolean(CONFIGURATION_GLOBAL_REPORTING_SECTION,
				CONFIGURATION_USE_VISTA_COMPATIBLE_IMAGES, true);
	}

	public static DecimalFormat getNumberFormat() {
		return new DecimalFormat("###,###,###,###,##0.##");
	}
}
