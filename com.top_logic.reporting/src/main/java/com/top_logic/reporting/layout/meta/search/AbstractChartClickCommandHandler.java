/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.List;
import java.util.Map;

import org.jfree.chart.urls.CategoryURLGenerator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Allow opening a dialog to display the details from {@link ChartGotoAware} via a
 * {@link CategoryURLGenerator} and a {@link AttributedSearchResultSet}.
 * 
 * This will create a list of items that in turn can be displayed via an {@link TableComponent}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public abstract class AbstractChartClickCommandHandler extends OpenModalDialogCommandHandler {

    /** The command identifier. */
    public static final String COMMAND_ID = "chartDisplayDetails";
    
    /** The name of the chart from which we are called */
    public static final String CHART_NAME = "chartName";

    /** The item number selected */
    public static final String ITEM_INT = "itemInt";

    /** The seleced series */
    public static final String SERIES_INT = "seriesInt";

	public AbstractChartClickCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	

    /** 
     * Perform the calling of the component to display the given items from the given chart.
     */
    protected abstract void showValues(ChartGotoAware theComponent, String theName, List<?> theItems);

    @Override
	protected void beforeOpening(DisplayContext aContext,
    		LayoutComponent aComponent, Map someArguments, LayoutComponent aDialog) {
    	super.beforeOpening(aContext, aComponent, someArguments, aDialog);
    	
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
            }

            ChartGotoAware theComponent = (ChartGotoAware) aComponent;
            List           theItems     = theComponent.getGotoItems(theName, theSeries, theItem);

            showValues(theComponent, theName, theItems);
        } else {
            Logger.warn("aComponent is not ChartGotoAware" + aComponent.getClass(), this);

        }

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
}
