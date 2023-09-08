/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * The basic idea of the IDRangeIterator is that it can iterates over a
 * collection of objects which have unique identifiers and user interface
 * representations. The identifiers can be used for the internal identification
 * and the user interface representation can be shown to the user.
 * 
 * @author  <a href=mailto:tdi@top-logic.com>Thomas Dickhut</a>
 * @author  <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public interface IDRangeIterator {
    
    /**
     * This method returns the next object in the iteration or <code>null</code>
     * if there is no next object.
     * 
     * @return Returns the next object in the iteration or <code>null</code>.
     */
    public Object nextObject();
    
    /**
     * This method returns a string for the given object which can be displayed
     * to the user.
     * 
     * @param  anObject An object which was returned of the {@link #nextObject()} method.
     * @return Returns a string for the given object which can be displayed to
     *         the user.
     */
    public String getUIStringFor(Object anObject);

    /**
     * <p>
     * This method returns for the given object a unique identifier. The
     * identifier can be used for the internal identification.
     * </p>
     * <p>
     * The returned identifier must only contains numbers and ASCII characters.
     * The identifier must be less than 24 characters but it should be as short
     * as possible.
     * </p>
     * 
     * @param  anObject An object which was returned of the {@link #nextObject()} method.
     * @return Returns a unique identifier for the given object.
     */
    public Object getIDFor(Object anObject);
    
    /**
     * reset the Iterator to its initial State.
     */
    public void reset();
    
    /** Create an Array of IDs based on this Iterator */
    public Object[] createCoords();
}
