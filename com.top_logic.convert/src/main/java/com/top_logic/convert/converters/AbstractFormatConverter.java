/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.top_logic.convert.ConverterMapping;

/**
 * Abstract implementation of {@link FormatConverter}.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public abstract class AbstractFormatConverter implements FormatConverter {

    /** The MIME type of plain text. */
    public static final String TXT_MIMETYPE = "text/plain";
    
    private Collection<ConverterMapping> mimeTypeMappings;
    
    /**
     * creates a new {@link AbstractFormatConverter} and fills
     * the collection with all supported mime-type mappings
     */
    public AbstractFormatConverter() {
        HashSet<ConverterMapping> mappings = new HashSet<>();
        fillMimeTypeMappings(mappings);
        
		this.mimeTypeMappings = mappings;
    }
    
    /**
     * This method fills the given collection of this converter with its
     * {@link ConverterMapping}. Each {@link ConverterMapping} contains a from-mime-type
     * and a to-mime-type. 
     * Use the constant {@link #TXT_MIMETYPE} as target MIME type of a {@link ConverterMapping}.
     * 
     * @param   mappings     to fill with convertermappings
     * @see     ConverterMapping
     */
    protected abstract void fillMimeTypeMappings(Collection<ConverterMapping> mappings);
    
    /** 
     * This method returns an Iterator with all mime-type mappings, this
     * converter supports.
     * 
     * @return  iterator with all mappings
     * 
     * @see     com.top_logic.convert.converters.FormatConverter#getConverterMappings()
     */
    @Override
	public Iterator<ConverterMapping> getConverterMappings() {
        return this.mimeTypeMappings.iterator();
    }

    /** 
     * This method checks, whether the given mime-type combination is
     * supported by this converter
     * 
     * @param   aFromMime the source mime type
     * @param   aToMime the target mime type
     * @return  true or false, whether the conversion is supported or not.
     * 
     * @see com.top_logic.convert.converters.FormatConverter#supports(java.lang.String, java.lang.String)
     */
    @Override
	public boolean supports(String aFromMime, String aToMime) {
        return supports(new ConverterMapping(aFromMime, aToMime));
    }

    @Override
	public boolean supports(String aFromMime) {
        return supports(aFromMime, TXT_MIMETYPE);
    }

    @Override
	public boolean supports(ConverterMapping aMapping) {
        return this.mimeTypeMappings.contains(aMapping);
    }

}
