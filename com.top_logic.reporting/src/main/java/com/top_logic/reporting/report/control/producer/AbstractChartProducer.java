/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.producer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.reporting.report.util.ValueUtil;
import com.top_logic.util.Resources;


/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class AbstractChartProducer extends AbstractConfiguredInstance<AbstractChartProducer.Config>
		implements ChartProducer {

	public interface Config extends PolymorphicConfiguration<ChartProducer> {
		@Name(XML_ATTRIBUTE_SHOW_LEGEND)
		@BooleanDefault(LEGEND)
		boolean getShowLegend();

		@Name(XML_ATTRIBUTE_SHOW_TOOLTIPS)
		@BooleanDefault(!TOOLTIPS)
		boolean getShowTooltips();

		@Name(XML_ATTRIBUTE_SHOW_URLS)
		@BooleanDefault(!URLS)
		boolean getShowURLs();
	}

	/**
     * A constant defining the layout XML attribute allowing clients to
     * show/hide the chart's legend.
     */
    public static final String XML_ATTRIBUTE_SHOW_LEGEND = "showLegend";
    
    /**
     * A constant defining the layout XML attribute allowing clients to
     * show/hide tooltips.
     */
    public static final String XML_ATTRIBUTE_SHOW_TOOLTIPS = "showTooltips";

    /**
     * A constant defining the layout XML attribute allowing clients to
     * show/hide URLs.
     */
    public static final String XML_ATTRIBUTE_SHOW_URLS = "showURLs";
    
    /**
     * @deprecated transfer this into {@link ChartContext} or {@link ReportConfiguration}
     */
	@Deprecated
	public static final String LABEL_TITLE         = "title";
	/**
     * @deprecated transfer this into {@link ChartContext} or {@link ReportConfiguration}
     */
    @Deprecated
	public static final String LABEL_X_AXIS        = "xAxisLabel";
    /**
     * @deprecated transfer this into {@link ChartContext} or {@link ReportConfiguration}
     */
    @Deprecated
	public static final String LABEL_Y_AXIS        = "yAxisLabel";

    /**
     * A flag indicating whether to show the chart's legend or not.
     */
    private boolean showLegend;
    
    /**
     * A flag indicating whether to show the chart's tooltips or not.
     */
    private boolean showTooltips;
    
    /**
     * A flag indicating whether to show the chart's URL or not.
     */
    private boolean showURLs;

    /**
     * Creates an instance of this class and initializes it using the specified
     * attributes.
     * 
     * @param someAttrs
     *            the attributes to be used for instance configuration
     */
    public AbstractChartProducer(final InstantiationContext context, Config someAttrs) {
		super(context, someAttrs);
        this.showLegend   = someAttrs.getShowLegend();
        this.showTooltips = someAttrs.getShowTooltips();
        this.showURLs     = someAttrs.getShowURLs();
    }

	 protected abstract JFreeChart createChart(ChartContext aChartContext);

	@Override
	public JFreeChart produceChart(ChartContext aContext) {
		return AbstractChartProducer.adaptChart(aContext, this.createChart(aContext));
	}

	public double getDouble(ChartContext aChartContext, String aKey, double aDefault) {
		return ValueUtil.getDoubleValue(aChartContext.getValue(aKey), aDefault);
	}

    public int getInt(ChartContext aChartContext, String aKey, int aDefault) {
        return ValueUtil.getIntValue(aChartContext.getValue(aKey), aDefault);
    }

    public String getString(ChartContext aChartContext, String aKey, String aDefault) {
        return ValueUtil.getStringValue(aChartContext.getValue(aKey), aDefault);
    }

    public static boolean getBoolean(ChartContext aChartContext, String aKey, boolean aDefault) {
        return ValueUtil.getBooleanValue(aChartContext.getValue(aKey), aDefault);
    }

    public Paint getPaint(ChartContext aChartContext, String aKey, Paint aDefault) {
        return ValueUtil.getPaint(aChartContext.getValue(aKey), aDefault);
    }

    public Paint getPaint(ChartContext aChartContext, String aKey, String aDefault) {
        return ValueUtil.getPaint(aChartContext.getValue(aKey), Color.decode(aDefault));
    }

    public Shape getShape(ChartContext aChartContext, String aKey, Shape aDefault) {
        return ValueUtil.getShapeValue(aChartContext.getValue(aKey), aDefault);
    }

    public DateFormat getDateFormat(ChartContext aChartContext, String aKey, DateFormat aDefault) {
    	return ValueUtil.getDateFormat(aChartContext.getValue(aKey), aDefault);
    }

    public Date getDate(ChartContext aChartContext, String aKey, Date aDefault) {
    	Object value = aChartContext.getValue(aKey);

    	return (Date) (value != null ? value : aDefault);
    }

    /**
	 * This method returns the default resource prefix (e.g. reporting.chart.).
	 */
    public ResPrefix getDefaultResourcePrefix() {
		return ResPrefix.GLOBAL;
    }

    /**
	 * Use the {@link #getDefaultResourcePrefix()} as default resource prefix.
	 */
    public String getLabel(ChartContext aChartContext, String aKey) {
    	return getLabel(aChartContext, aKey, getDefaultResourcePrefix());
    }

    /**
	 * This method returns the i18n label from the chart context. If the chart
	 * context doesn't contain a label the default label is used. The default
	 * label is get from the resouces (key = aDefaultPrefix + aKey).
	 *
	 * E.g. the chart context has no label for
	 * key            = title
	 * aDefaultPrefix = reporting.chart.eva.
	 * => reporting.chart.eva.title
	 *
	 * @param aChartContext
	 *            A {@link ChartContext}. Must NOT be <code>null</code>.
	 * @param aKey
	 *            A key. Must NOT be <code>null</code>.
	 */
	public String getLabel(ChartContext aChartContext, String aKey, ResPrefix aDefaultPrefix) {
    	String label = getString(aChartContext, aKey, null);
    	if (label == null) {
			label = Resources.getInstance().getString(aDefaultPrefix.key(aKey));
    	}

    	return label;
    }

    /**
	 * Use the {@link #getDefaultResourcePrefix()} as default resource prefix.
	 *
	 * @see #getLabel(ChartContext, String, ResPrefix)
	 */
    public String getLabel(ChartContext aChartContext, String aKey, String[] someValues) {
        return getLabel(aChartContext, aKey, getDefaultResourcePrefix(), someValues);
    }

    /**
     * This method returns the i18n label from the chart context. If the chart
     * context doesn't contain a label the default label is used. The default
     * label is get from the resouces (key = aDefaultPrefix + aKey).
     *
     * E.g. the chart context has no label for
     * key            = title
     * aDefaultPrefix = reporting.chart.eva.
     * => reporting.chart.eva.title
     *
     * @param aChartContext
     *            A {@link ChartContext}. Must NOT be <code>null</code>.
     * @param aKey
     *            A key. Must NOT be <code>null</code>.
     */
	public String getLabel(ChartContext aChartContext, String aKey, ResPrefix aDefaultPrefix, Object[] someValues) {
        String label = getString(aChartContext, aKey, null);
        if (label == null) {
			label = Resources.getInstance().getMessage(aDefaultPrefix.key(aKey), someValues);
        }

        return label;
    }

    public static Paint getBackgroundPaint(ChartContext aChartContext, JFreeChart aChart) {
        Paint thePaint = (Paint) aChartContext.getValue(VALUE_PAINT_BACKGROUND);

        if (thePaint == null) {
            thePaint = ReportUtilities.getThemeBackgroundColor();
        }

        return thePaint;
    }
    
    public static Paint getChartBackgroundPaint(ChartContext aChartContext, JFreeChart aChart) {
        return ReportUtilities.getThemeChartBackgroundColor();
    }

    public boolean showLegend(ChartContext aChartContext) {
    	return getBoolean(aChartContext, VALUE_SHOW_LEGEND, showLegend);
    }

    public boolean showUrls(ChartContext aChartContext) {
    	return getBoolean(aChartContext, VALUE_SHOW_URLS, showURLs);
    }

    public boolean showTooltips(ChartContext aChartContext) {
    	return getBoolean(aChartContext, VALUE_SHOW_TOOLTIPS, showTooltips);
    }

    public static StandardChartTheme getChartTheme() {
        return (StandardChartTheme) ChartFactory.getChartTheme();
    }

    /**
     * Applies the attributes for this theme to a {@link ValueAxis}.
     *
     * @param anAxis  the axis (<code>null</code> not permitted).
     */
    public static void applyToValueAxis(Axis anAxis) {
        ChartTheme theChartTheme = ChartFactory.getChartTheme();

        if (theChartTheme instanceof StandardChartTheme) {
            StandardChartTheme theTheme = (StandardChartTheme) theChartTheme;

            if (anAxis instanceof CategoryAxis) {
                AbstractChartProducer.applyToCategoryAxis((CategoryAxis) anAxis, theTheme);
            }
            else {
                anAxis.setLabelFont(theTheme.getLargeFont());
                anAxis.setLabelPaint(theTheme.getAxisLabelPaint());
                anAxis.setTickLabelFont(theTheme.getRegularFont());
                anAxis.setTickLabelPaint(theTheme.getTickLabelPaint());

                if (anAxis instanceof SymbolAxis) {
                    AbstractChartProducer.applyToSymbolAxis((SymbolAxis) anAxis, theTheme);
                }
                if (anAxis instanceof PeriodAxis) {
                    AbstractChartProducer.applyToPeriodAxis((PeriodAxis) anAxis, theTheme);
                }
            }
        }
    }

    /** 
     * Adapt the general chart layout.
     * 
     * @param    aContext    The context defining some parameters, must not be <code>null</code>.
     * @param    aChart      The chart to be adapted, must not be <code>null</code>.
     * @return   The requested chart, never <code>null</code>.
     * @see      #adaptChart(ChartContext, JFreeChart, boolean)
     */
    public static JFreeChart adaptChart(ChartContext aContext, JFreeChart aChart) {
        return AbstractChartProducer.adaptChart(aContext, aChart, false);
    }

    /** 
     * Adapt the general chart layout.
     * 
     * @param    aContext    The context defining some parameters, must not be <code>null</code>.
     * @param    aChart      The chart to be adapted, must not be <code>null</code>.
     * @param    withAxis    Flag, if the axis layout should be adapted too (in that case <code>true</code>).
     * @return   The requested chart, never <code>null</code>.
     */
    public static JFreeChart adaptChart(ChartContext aContext, JFreeChart aChart, boolean withAxis) {
        if (aContext != null && aChart != null) {
            Plot thePlot = aChart.getPlot();

            aChart.setAntiAlias(getBoolean(aContext, VALUE_ANTI_ALIAS, true));
            aChart.setBackgroundPaint(AbstractChartProducer.getBackgroundPaint(aContext, aChart));
            thePlot.setBackgroundPaint(AbstractChartProducer.getChartBackgroundPaint(aContext, aChart));

            if (withAxis) {
                if (thePlot instanceof XYPlot) {
                    AbstractChartProducer.applyToValueAxis(((XYPlot) thePlot).getDomainAxis());
                    AbstractChartProducer.applyToValueAxis(((XYPlot) thePlot).getRangeAxis());
                }
                else if (thePlot instanceof CategoryPlot) {
                    AbstractChartProducer.applyToValueAxis(((CategoryPlot) thePlot).getDomainAxis());
                    AbstractChartProducer.applyToValueAxis(((CategoryPlot) thePlot).getRangeAxis());
                }
            }
        }

        return aChart;
    }

    /**
     * Applies the attributes for this theme to a {@link SymbolAxis}.
     *
     * @param anAxis  the axis (<code>null</code> not permitted).
     */
    protected static void applyToSymbolAxis(SymbolAxis anAxis, StandardChartTheme aTheme) {
        anAxis.setGridBandPaint(aTheme.getGridBandPaint());
        anAxis.setGridBandAlternatePaint(aTheme.getGridBandAlternatePaint());
    }

    /**
     * Applies the attributes for this theme to a {@link PeriodAxis}.
     *
     * @param anAxis  the axis (<code>null</code> not permitted).
     */
    protected static void applyToPeriodAxis(PeriodAxis anAxis, StandardChartTheme aTheme) {
        PeriodAxisLabelInfo[] theInfos          = anAxis.getLabelInfo();
        Font                  theRegularFont    = aTheme.getRegularFont();
        Paint                 theTickLabelPaint = aTheme.getTickLabelPaint();

        for (int thePos = 0; thePos < theInfos.length; thePos++) {
            PeriodAxisLabelInfo theInfo    = theInfos[thePos];
            PeriodAxisLabelInfo theNewInfo = new PeriodAxisLabelInfo(theInfo.getPeriodClass(),
                    theInfo.getDateFormat(), theInfo.getPadding(), theRegularFont,
                    theTickLabelPaint, theInfo.getDrawDividers(),
                    theInfo.getDividerStroke(), theInfo.getDividerPaint());

            theInfos[thePos] = theNewInfo;
        }

        anAxis.setLabelInfo(theInfos);
    }

    /**
     * Applies the attributes for this theme to a {@link CategoryAxis}.
     *
     * @param anAxis  the axis (<code>null</code> not permitted).
     */
    protected static void applyToCategoryAxis(CategoryAxis anAxis, StandardChartTheme aTheme) {
        anAxis.setLabelFont(aTheme.getLargeFont());
        anAxis.setLabelPaint(aTheme.getAxisLabelPaint());
        anAxis.setTickLabelFont(aTheme.getRegularFont());
        anAxis.setTickLabelPaint(aTheme.getTickLabelPaint());

        if (anAxis instanceof SubCategoryAxis) {
            SubCategoryAxis theAxis = (SubCategoryAxis) anAxis;

            theAxis.setSubLabelFont(aTheme.getRegularFont());
            theAxis.setSubLabelPaint(aTheme.getTickLabelPaint());
        }
    }
}

