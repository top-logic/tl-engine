/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.basic.Logger;
import com.top_logic.reporting.report.jfc.JFCSimpleDateFormat;
import com.top_logic.reporting.report.jfc.TickMarkPeriodAxis;

/**
 * The PeriodAxisUtil contains useful static methods for {@link PeriodAxis}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use your own methods with the help of {@link ReportUtilities}
 */
@Deprecated
public class PeriodAxisUtil {

    public static final String DEFAULT_PATTERN_MONTH_STR      = "MMM";
    public static final String DEFAULT_PATTERN_QUARTER_STR    = JFCSimpleDateFormat.PATTERN_QUARTER;
    public static final String DEFAULT_PATTERN_HALF_YEAR_STR  = JFCSimpleDateFormat.PATTERN_HALF_YEAR;
    public static final String DEFAULT_PATTERN_YEAR_STR       = "yyyy";

    private PeriodAxisUtil() {
        // Use the static methods of this class
    }

    /** See {@link #setMonthYearPeriodAxis(PeriodAxis, String, String)}. */
    public static void setMonthYearPeriodAxis(PeriodAxis aPeriodAxis) {
        setMonthYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_MONTH_STR, DEFAULT_PATTERN_YEAR_STR);
    }
    
    /**
     * This method sets a month year period axis. The pattern string are used as
     * patterns for {@link JFCSimpleDateFormat}.
     * 
     * E.g. month pattern = MMM and year pattern = yyyy
     * |         2007
     * +-----+-----+-----+-----+ ...
     * | ... | Feb | Mrz | ... | ...
     * 
     * @param aPeriodAxis
     *        A {@link PeriodAxis} to set. Must not be <code>null</code>.
     * @param aMonthPatternString
     *        A month pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     * @param aYearPatternString
     *        A year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     */
    public static void setMonthYearPeriodAxis(PeriodAxis aPeriodAxis, String aMonthPatternString, String aYearPatternString) {
        setLabelInfosTo(aPeriodAxis, Month.class, aMonthPatternString, aYearPatternString);
    }

    /** See {@link #setQuarterYearPeriodAxis(PeriodAxis, String, String)}. */
    public static void setQuarterYearPeriodAxis(PeriodAxis aPeriodAxis) {
        setQuarterYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_QUARTER_STR, DEFAULT_PATTERN_YEAR_STR);
    }
    
    /**
     * This method sets a quarter year period axis. The pattern string are used as
     * patterns for {@link JFCSimpleDateFormat}.
     * 
     * E.g. quarter pattern = q and year pattern = yyyy
     * |         2006          | ...
     * +-----+-----+-----+-----+ ...
     * |  1  |  2  |  3  |  4  | ...
     * 
     * @param aPeriodAxis
     *        A {@link PeriodAxis} to set. Must not be <code>null</code>.
     * @param aQuarterPatternString
     *        A quarter pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     * @param aYearPatternString
     *        A year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     */
    public static void setQuarterYearPeriodAxis(PeriodAxis aPeriodAxis, String aQuarterPatternString, String aYearPatternString) {
        setLabelInfosTo(aPeriodAxis, ReportUtilities.getQuarterPeriodClass(false), aQuarterPatternString, aYearPatternString);
    }

    public static void setQuarterYearPeriodAxis(PeriodAxis aPeriodAxis, DateFormat aQuarterFormat, DateFormat aYearFormat) {
    	setLabelInfosTo(aPeriodAxis, ReportUtilities.getQuarterPeriodClass(false), aQuarterFormat, aYearFormat);
    }

    /** See {@link #setHalfYearYearPeriodAxisPeriodAxis(PeriodAxis, String, String)}. */
    public static void setHalfYearYearPeriodAxisPeriodAxis(PeriodAxis aPeriodAxis) {
        setHalfYearYearPeriodAxisPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_HALF_YEAR_STR, DEFAULT_PATTERN_YEAR_STR);
    }
    
    /**
     * This method sets a quarter year period axis. The pattern string are used as
     * patterns for {@link JFCSimpleDateFormat}.
     * 
     * E.g. half year pattern = q and year pattern = yyyy
     * |   2006    |    2007   | ...
     * +-----+-----+-----+-----+ ...
     * |  1  |  2  |  1  |  2  | ...
     * 
     * @param aPeriodAxis
     *        A {@link PeriodAxis} to set. Must not be <code>null</code>.
     * @param aHalfYearPatternString
     *        A half year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     * @param aYearPatternString
     *        A year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     */
    public static void setHalfYearYearPeriodAxisPeriodAxis(PeriodAxis aPeriodAxis, String aHalfYearPatternString, String aYearPatternString) {
        setLabelInfosTo(aPeriodAxis, ReportUtilities.getHalfYearPeriodClass(false), aHalfYearPatternString, aYearPatternString);
    }

    /** See {@link #setYearPeriodAxis(PeriodAxis, String)}. */
    public static void setYearPeriodAxis(PeriodAxis aPeriodAxis) {
        setYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_YEAR_STR);
    }
    
    /** 
     * This method sets a year period axis. The pattern string are used as
     * patterns for {@link JFCSimpleDateFormat}.
     * 
     * E.g. half year pattern = q and year pattern = yyyy
     * |   2006    |    2007   | ...
     * +-----+-----+-----+-----+ ...
     * 
     * @param aPeriodAxis
     *        A {@link PeriodAxis} to set. Must not be <code>null</code>.
     * @param aYearPatternString
     *        A year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     */
    public static void setYearPeriodAxis(PeriodAxis aPeriodAxis, String aYearPatternString) {
        PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[1];
        info[0] = new PeriodAxisLabelInfo(ReportUtilities.getYearPeriodClass(true),  new JFCSimpleDateFormat(aYearPatternString));
        aPeriodAxis.setLabelInfo(info);
    }
    
    public static void setPeriodAxisfor(PeriodAxis aPeriodAxis, Class aRegularTimePeriodClass) {
        if (aRegularTimePeriodClass.equals(Month.class)) {
            setMonthYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_MONTH_STR, DEFAULT_PATTERN_YEAR_STR);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getQuarterPeriodClass(false))) {
            setQuarterYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_QUARTER_STR, DEFAULT_PATTERN_YEAR_STR);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getHalfYearPeriodClass(false))) {
            setHalfYearYearPeriodAxisPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_HALF_YEAR_STR, DEFAULT_PATTERN_YEAR_STR);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getYearPeriodClass(false))) {
            setYearPeriodAxis(aPeriodAxis, DEFAULT_PATTERN_YEAR_STR);
        }
        else {
            throw new UnsupportedOperationException("The regular time period class ('" + aRegularTimePeriodClass + "') is not supported!");
        }
    }
    
    /**
     * This method sets for the given {@link RegularTimePeriod} class a suitable
     * sub label. 
     * See {@link #setMonthYearPeriodAxis(PeriodAxis, String, String)}. 
     * See {@link #setQuarterYearPeriodAxis(PeriodAxis, String, String)}. 
     * See {@link #setHalfYearYearPeriodAxisPeriodAxis(PeriodAxis, String, String)}.
     * See {@link #setYearPeriodAxis(PeriodAxis, String)}.
     * 
     * @param aPeriodAxis
     *        A {@link PeriodAxis}. Must not be <code>null</code>.
     * @param aRegularTimePeriodClass
     *        A {@link RegularTimePeriod} class. Must not be <code>null</code>.
     * @param aSubPeriodPatternString
     *        A sub pattern string. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}. Maybe null (e.g. regular time period
     *        class == Year.class).
     * @param aYearPatternString
     *        A year pattern. Must be a valid pattern for the
     *        {@link JFCSimpleDateFormat}.
     */
    public static void setPeriodAxisfor(PeriodAxis aPeriodAxis, Class aRegularTimePeriodClass, String aSubPeriodPatternString, String aYearPatternString) {
        if (aRegularTimePeriodClass.equals(Month.class)) {
            setMonthYearPeriodAxis(aPeriodAxis, aSubPeriodPatternString, aYearPatternString);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getQuarterPeriodClass(false))) {
            setQuarterYearPeriodAxis(aPeriodAxis, aSubPeriodPatternString, aYearPatternString);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getHalfYearPeriodClass(false))) {
            setHalfYearYearPeriodAxisPeriodAxis(aPeriodAxis, aSubPeriodPatternString, aYearPatternString);
        }
        else if (aRegularTimePeriodClass.equals(ReportUtilities.getYearPeriodClass(false))) {
            setYearPeriodAxis(aPeriodAxis, aYearPatternString);
        }
        else {
            throw new UnsupportedOperationException("The regular time period class ('" + aRegularTimePeriodClass + "') is not supported!");
        }
    }
    
    /**
     * This method creates a instance of an time period of the given class. The
     * date is set to the created time period.
     * 
     * @param aPeriodClass
     *        A {@link RegularTimePeriod}. Must not be <code>null</code>.
     * @param aStartDate
     *        A start date. Must not be <code>null</code>.
     */
    public static RegularTimePeriod getRegularTimePeriod(Class aPeriodClass, Date aStartDate) {
        try {
            Constructor constructor = aPeriodClass.getDeclaredConstructor(new Class[] {Date.class});
            
            return (RegularTimePeriod) constructor.newInstance(new Object[] { aStartDate });
        }
        catch (Exception e) {
            Logger.error("The jfreechart no constructor with the only parameter (Date) is found for the regular time period class ('" + aPeriodClass + "').", e, PeriodAxisUtil.class);
        }
        
        return null;
    }
    
    public static void addPeriodAxis(JFreeChart aChart, Date aStartDate, Date aEndDate, Class aRegularPeriodClass, boolean isDomainAxis, Color aIntervalColor) {
        long startTime = aStartDate.getTime();
        long endTime   = aEndDate.getTime();
        
        PeriodAxis timeAxis = new TickMarkPeriodAxis("");
        timeAxis.setAutoRangeTimePeriodClass(aRegularPeriodClass);
		timeAxis.setRange(ChartUtil.normalizeRange(startTime, endTime));
        
        PeriodAxisUtil.setPeriodAxisfor(timeAxis, aRegularPeriodClass);
        
        timeAxis.setTickMarksVisible(false);
        
        // Paint any second time period with a color
        RegularTimePeriod timePeriod   = PeriodAxisUtil.getRegularTimePeriod(aRegularPeriodClass, new Date(startTime));
        Plot              plot         = aChart.getPlot();
        
        while(timePeriod != null && timePeriod.next() != null) {
            IntervalMarker periodMarker = new IntervalMarker(timePeriod.getFirstMillisecond(), timePeriod.getLastMillisecond());
            periodMarker.setPaint(aIntervalColor);
            
            if (plot instanceof CategoryPlot) {
                ((CategoryPlot) plot).addRangeMarker(periodMarker, Layer.BACKGROUND);
            } else {
                if (isDomainAxis) {
                    ((XYPlot) plot).addDomainMarker(periodMarker, Layer.BACKGROUND);
                } else {
                    ((XYPlot) plot).addRangeMarker(periodMarker, Layer.BACKGROUND);
                }
            }
            
            // Alternate get the next time period to mark
            timePeriod = timePeriod.next();
            
            if (timePeriod == null || timePeriod.getLastMillisecond() > endTime) break;
            
            timePeriod = timePeriod.next();
        }
        
        if (plot instanceof CategoryPlot) {
            ((CategoryPlot) plot).setRangeAxis(timeAxis);
        } else {
            if (isDomainAxis) {
                ((XYPlot) plot).setDomainAxis(timeAxis);
            } else {
                ((XYPlot) plot).setRangeAxis(timeAxis);
            }
        }
    }
    
    // Private helper methods
    
    private static void setLabelInfosTo(PeriodAxis aPeriodAxis, Class aPeriodClass, String aHalfYearPatternString, String aYearPatternString) {
    	setLabelInfosTo(aPeriodAxis, aPeriodClass, new JFCSimpleDateFormat(aHalfYearPatternString), new JFCSimpleDateFormat(aYearPatternString));
    }
    
    private static void setLabelInfosTo(PeriodAxis aPeriodAxis, Class aPeriodClass, DateFormat aPeriodFormat, DateFormat aYearFormat) {
    	PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];
    	info[0] = new PeriodAxisLabelInfo(aPeriodClass,  aPeriodFormat);
    	info[1] = new PeriodAxisLabelInfo(ReportUtilities.getYearPeriodClass(false),    aYearFormat);
    	aPeriodAxis.setLabelInfo(info);
    	
    }
}

