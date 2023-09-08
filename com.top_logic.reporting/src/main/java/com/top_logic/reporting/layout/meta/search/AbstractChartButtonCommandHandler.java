/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.List;
import java.util.Map;

import org.jfree.chart.urls.CategoryURLGenerator;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Allow a GOTO from {@link ChartGotoAware} via a {@link CategoryURLGenerator} and a {@link AttributedSearchResultSet}
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractChartButtonCommandHandler extends AJAXCommandHandler {

    /** The command identifier. */
    public static final String COMMAND_ID = "chartGotoDetails";
    
    
    public static final String CHART_NAME = "chartName";

    
    public static final String ITEM_INT = "itemInt";

    
    public static final String SERIES_INT = "seriesInt";
    
    /** When true a GOTO will even work for empty results (which may or may not make sense) */
	private final boolean showEmptyResult;

	public interface Config extends AJAXCommandHandler.Config {
		/**
		 * @see #getShowEmptyResult()
		 */
		String SHOW_EMPTY_RESULT_PROPERTY = "show-empty-result";

		@Name(SHOW_EMPTY_RESULT_PROPERTY)
		@BooleanDefault(true)
		boolean getShowEmptyResult();
	}

    /** 
     * Create a new ChartGotoAwareGotoHandler which will #showEmptyResult
     */
    public AbstractChartButtonCommandHandler(InstantiationContext context, Config config) {
		super(context, config);

		showEmptyResult = config.getShowEmptyResult();
    }
    
    /** 
     * Perform the calling of the component to display the given items from the given chart.
     */
    protected abstract void showValues(ChartGotoAware aComponent, String aChartName, List someItems);

    /**
     * @see com.top_logic.base.services.simpleajax.AJAXCommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        if ((aComponent instanceof ChartGotoAware)) {
            int    theSeries = -1;
            int    theItem   = -1;
            String theName   = null;
    
            try {
                theName   = (String) someArguments.get(CHART_NAME);
                theSeries = Integer.parseInt((String) someArguments.get(SERIES_INT));
                theItem   = Integer.parseInt((String) someArguments.get(ITEM_INT));
            } 
            catch(Exception e) {
                Logger.warn("Could not get the series and item value from the arguments map.", this);
                return HandlerResult.DEFAULT_RESULT;
            }

            ChartGotoAware theComponent = (ChartGotoAware) aComponent;
            List           theItems     = theComponent.getGotoItems(theName, theSeries, theItem);

            if (showEmptyResult || !CollectionUtil.isEmptyOrNull(theItems)) {
                showValues(theComponent, theName, theItems);
            }
        } else {
            Logger.warn("aComponent is not ChartGotoAware" + aComponent.getClass(), this);

        }

        return HandlerResult.DEFAULT_RESULT;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.AbstractCommandHandler#getAttributeNames()
     */
    @Override
	public String[] getAttributeNames() {
         String[] theAttrNames = super.getAttributeNames();

         if (theAttrNames == null) {
             return new String[] {CHART_NAME, SERIES_INT, ITEM_INT};
         }
         else {
             String[] theTmp = new String[theAttrNames.length + 3];
             System.arraycopy(theAttrNames, 0, theTmp, 0, theAttrNames.length);
             theTmp[theAttrNames.length]     = CHART_NAME;
             theTmp[theAttrNames.length + 1] = SERIES_INT;
             theTmp[theAttrNames.length + 2] = ITEM_INT;
             
             return theTmp;
         }
    }

	public static <C extends Config> C updateShowEmpty(C config, boolean value) {
		return update(config, Config.SHOW_EMPTY_RESULT_PROPERTY, value);
	}

}
