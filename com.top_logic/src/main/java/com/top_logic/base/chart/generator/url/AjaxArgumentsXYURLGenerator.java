/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.url;

import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;

import com.top_logic.tool.boundsec.CommandHandler;

/**
 * The AjaxArgumentsXYURLGenerator generates the onClick fragment 
 * for an {@link CommandHandler} with the two arguments 'series' and 'categroy'.
 * 
 * @author  <a href="mailto:tbe@top-logic.com">Till Bentz</a>
 */
public class AjaxArgumentsXYURLGenerator implements XYURLGenerator {

    /** Constant for the category. */
    public static final String CATEGORY = "category";
    /** Constant for the series. */
    public static final String SERIES   = "series";

    /** The command identifier of the the corresponding ajax command. */
    private String commandId;

	/**
	 * Only subclasses should use this constructor implicitly. Otherwise the {@link #commandId} will
	 * not be set, which will lead to JS errors in the generated URLs.
	 */
    protected AjaxArgumentsXYURLGenerator() {
    }
    
    /**
     * Creates a {@link AjaxArgumentsXYURLGenerator} with the
     * given parameters.
     * 
     * @param aCommandId
     *        {@link #commandId}. Must not be <code>null</code> or empty.
     */
    public AjaxArgumentsXYURLGenerator(String aCommandId) {
        this.commandId = aCommandId;
    }

    @Override
	public String generateURL(XYDataset aData, int aSeries, int aCategory) {
        StringBuffer theOnClickFragment = new StringBuffer();
        theOnClickFragment.append("return ");
        theOnClickFragment.append(this.commandId);
        theOnClickFragment.append('(');
        theOnClickFragment.append('\'');
        theOnClickFragment.append(aSeries);
        theOnClickFragment.append("\',");
        theOnClickFragment.append('\'');
        theOnClickFragment.append(aCategory);
        theOnClickFragment.append("\')");
        
        return theOnClickFragment.toString();
    }

}

