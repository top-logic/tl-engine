/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.demo.generator;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;


/**
 * Die DemoURLGenerator is a dummy tooltip generator which returns
 * every time the <i>TopLogic</i>-URL.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DemoURLGenerator implements CategoryURLGenerator{
    
    /** constant url to return for all CategoryDatasets */
    protected String url;
    
    /** 
     * Create a new DemoURLGenerator for given URL.
     */
    public DemoURLGenerator(String aUrl) {
        this.url = aUrl;
    }

    /** 
     * Create a new DemoURLGenerator for "http://www.top-logic.com"
     */
    public DemoURLGenerator() {
        this("http://www.top-logic.com");
    }

    /**
     * This is a dummy implementation which always returns the same url.
     * 
     * @see org.jfree.chart.urls.CategoryURLGenerator#generateURL
     *      (org.jfree.data.category.CategoryDataset, int, int)
     */
    @Override
	public String generateURL(CategoryDataset arg0, int arg1, int arg2) {
        return this.url;
    }
}

