/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.component.TableComponent;


/**
 * Component handling the search results.
 *
 * @author    <a href="mailto:DRO@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class SearchResultComponent extends TableComponent {
    
    /**
     * Create a new SearchResultComponent. 
     */
    public SearchResultComponent(InstantiationContext context, Config attr, String[] aData) throws ConfigurationException {
        super(context, attr);
    }
    
    /**
     * Create a new SearchResultComponent.
     */
    public SearchResultComponent(InstantiationContext context, Config attr) throws ConfigurationException {
        super(context, attr);
    }

}