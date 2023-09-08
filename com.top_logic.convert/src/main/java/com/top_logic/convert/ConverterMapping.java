/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert;

/**
 * This class is used to map one mime-type to another. This can be used
 * by a converter to specify, which mime-types it is able to convert.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public final class ConverterMapping {

    private final String fromMIMEType;
    
    private final String toMIMEType;

    /** 
     * Create a new convertermapping. Both given mime-types should look like this:
     * application/pdf or text/plain and should not contain any whitespaces.
     * 
     * @param aFromMIMEType the mime-type of the source-document
     * @param aToMIMEType the mime-type to convert content to
     */
    public ConverterMapping(String aFromMIMEType, String aToMIMEType) {
        this.fromMIMEType = aFromMIMEType;
        this.toMIMEType = aToMIMEType;
    }

    
    /**
     * Returns the fromMIMEType.
     */
    public String getFromMIMEType() {
        return (fromMIMEType);
    }


    /**
     * Returns the toMIMEType.
     */
    public String getToMIMEType() {
        return (toMIMEType);
    }


    /**
     * This method returns the key of this mapping, containing both mime-types
     * separated by a whitespace. It looks like this: "application/pdf text/plain"
     * 
     * @return the key e.g. "application/pdf text/plain"
     */
    public String getKey() {
        return fromMIMEType + ' ' + toMIMEType;
    }
    
    
    /** 
     * This method calculates the hash code of this object via
     * from- and to-mimetpyes hash code.
     * 
     * @return the hash code of the string representation, 
     *         such as "application/pdf text/plain"
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return getKey().hashCode();
    }

    
    /** 
     * Overridden to check equality of two {@link ConverterMapping}s
     * 
     * @param anObject the object to check
     * @return true or false whether the given obj is equal or not
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object anObject) {
        if (this == anObject)
            return true;
        if (anObject == null)
            return false;
        if (getClass() != anObject.getClass())
            return false;
        /* At this point, anObject is a ConverterMapping */
        final ConverterMapping other = (ConverterMapping) anObject;
        return getKey().equals(other.getKey()); 
    }
    
    
    /**
     * This method returns the string representation of this object,
     * which contains the classname and the mapping, represented by this
     * Class. This looks like "fromMIME->toMIME"
     * 
     * @return  Classname[fromMIME->toMIME]
     */
    @Override
	public String toString() {
        return getClass().getName() + '[' + getFromMIMEType() + "->" + getToMIMEType() + ']';
    }
}
