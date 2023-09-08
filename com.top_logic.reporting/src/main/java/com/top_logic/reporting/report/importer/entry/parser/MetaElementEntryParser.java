/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.model.TLClass;

/**
 * The MetaElementEntryParser parses tags with the tag name 'meta-element'.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class MetaElementEntryParser extends AbstractEntryParser {

    /** The single instance of this class. */
    public static final MetaElementEntryParser INSTANCE = new MetaElementEntryParser();

    /**
     * Creates a {@link MetaElementEntryParser}. 
     * Please, use the single instance of this class ({@link #INSTANCE}).
     */
    private MetaElementEntryParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[tag name=meta-element]";
    }

    @Override
	protected Object getValue(String aValueAsString) {
        TLClass theMetaElement = MetaElementFactory.getInstance().getGlobalMetaElement(aValueAsString.trim());
        
        return theMetaElement;
    }


}

