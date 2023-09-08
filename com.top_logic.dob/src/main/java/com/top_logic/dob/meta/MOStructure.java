/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.sql.DBTableMetaObject;


/**
 * This MetaObject defines a structure (like a struct found in C).
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface MOStructure extends MetaObject {

	/**
	 * The attribute of this type with the given name.
	 * 
	 * @param attrName
	 *        The name of the requested attribute.
	 * @return The requested attribute.
	 * 
	 * @throws NoSuchAttributeException
	 *         If no attribute with the given name exists (in particular, if
	 *         this is not a {@link MOStructure} or {@link MOClass} type).
	 * 
	 * @see #hasAttribute(String) to check if the requested attribute does
	 *      exist.
	 */
    public MOAttribute getAttribute(String attrName) throws NoSuchAttributeException;

	/**
	 * The attribute of this type with the given name.
	 * 
	 * @param name
	 *        The name of the attribute, see {@link MOAttribute#getName()}.
	 * @return The requested attribute, or <code>null</code>, if this type has no such attribute.
	 */
	MOAttribute getAttributeOrNull(String name);
	
	/**
	 * The attribute locally declared in this type with the given name.
	 * 
	 * @param name
	 *        The name of the attribute, see {@link MOAttribute#getName()}.
	 * @return The requested attribute, or <code>null</code>, if this type has no such attribute.
	 * 
	 * @see #getDeclaredAttributes()
	 */
	MOAttribute getDeclaredAttributeOrNull(String name);
    

	/**
	 * Check whether this type has an attribute with the given name.
	 * 
	 * @param attrName
	 *        The name of the attribute.
	 * @return true if this type has an attribute with the given name.
	 */
    public boolean hasAttribute(String attrName);
    
    /**
     * Check whether this type has a locally declared attribute with the given name.
     * 
     * @param attrName
     *        The name of the attribute.
     * @return true if this type has an attribute with the given name.
     * 
     * @see #getDeclaredAttributes()
     */
    public boolean hasDeclaredAttribute(String attrName);

	/**
	 * Attributes that are declared in this type (not including attributes of any super types).
	 */
    public List<MOAttribute> getDeclaredAttributes();

    /**
	 * The list of all attributes defined by this type.
	 * 
	 * @return The list of {@link MOAttribute}s. Never <code>null</code>.
	 */
    public List<MOAttribute> getAttributes();
    
    /** 
     * The list of all {@link MOReference} defined by this type 
     * 
     * @return The list of {@link MOReference}s. Never <code>null</code>.
     */
    public List<MOReference> getReferenceAttributes();

    /**
	 * The names of all attributes defined by this type. 
	 * 
	 * TODO KHA/BHU: The following does not hold for existing implementations (anyway,
	 * who needs a binary search, if there is a hash map?)
	 * 
	 * The returned array is ordered by name, so you can use a binary search.
	 * 
	 * TODO BHU: KbRefactoring: Remove method.
	 * 
	 * @return The array of all known attribute names (may be null).
	 */
    public String[] getAttributeNames();
	
    /**
     * Add the given attribute to this structure.
     *
     * This method will append the given attribute to the list of held 
     * attributes. If there is already an attribute, which has the name
     * of the given one (via {@link com.top_logic.dob.MOAttribute#getName()}), 
     * this method has to throw an exception.
     * 
     * @param     anAttr    The attribute to be added, must not 
     *                      be <code>null</code>.
     * @throws    DuplicateAttributeException    Will be thrown if there exists 
     *                                           already an attribute with the
     *                                           same name.
     */
    public void addAttribute(MOAttribute anAttr)
                                            throws DuplicateAttributeException;

	/**
	 * The size of the cache which are used by the attributes in this struture all together.
	 */
    public int getCacheSize();

    /**
     * Return a list of all indices, supported by this instance.
     * 
     * The return value must not be <code>null</code>, but can be empty. If
     * the returned list contains elements, all of them are of 
     * type {@link com.top_logic.dob.meta.MOIndex}.
     * 
     * @return    The list of held indices.
     * @see       com.top_logic.dob.meta.MOIndex
     */
    public List<MOIndex> getIndexes();
    
    /**
	 * The primary {@link MOIndex}.
	 */
	public MOIndex getPrimaryKey();
	
	/**
	 * The the storage type of this type.
	 */
	public DBTableMetaObject getDBMapping();

}
