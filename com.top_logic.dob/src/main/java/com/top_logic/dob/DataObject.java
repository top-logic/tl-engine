/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import java.io.Serializable;

import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;

/** 
 * DataObjects have Attributes/values and are "Instances" of MetaObjects.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DataObject extends StaticTyped, NamedValues, Serializable {

	/**
	 * Checks, whether this object's {@link #tTable() type} is assignment
	 * compatible to the given type.
	 * 
	 * @param type
	 *        The type to check this object for being an instance of.
	 * @return Whether the type of this object is assignment compatible to the
	 *         given type.
	 */
    public boolean isInstanceOf(MetaObject type);

	/**
	 * Same as {@link #isInstanceOf(MetaObject)}, if only the the name of the
	 * type to check is given.
	 * 
	 * @param typeName
	 *        The name of the type to check.
	 * @return Whether the type of this object is assignment compatible to the
	 *         type identified by the given name.
	 */
    public boolean isInstanceOf(String typeName);

    /** Returns the identifier for this object. 
     * 
     * @return Identifier of the object, may be null
     */
    public TLID getIdentifier ();

    /** Sets the identifier when not already set.
     *
     * Once the identifier is set you can´t override the existing one.
     */
    public void setIdentifier (TLID anIdentifier);

	/**
	 * Returns an {@link Iterable} over the attributes of this
	 * {@link DataObject}.
	 */
    public Iterable<? extends MOAttribute> getAttributes ();

    /**
     * Returns the value of the given {@link MOAttribute} in this
     * {@link DataObject}.
     * 
     * @param attribute must not be <code>null</code>
     */
    public Object getValue(MOAttribute attribute);

	/**
	 * Returns the {@link ObjectKey} of the value of the given {@link MOReference}.
	 * 
	 * @param reference
	 *        an {@link MOReference} known by this {@link DataObject}.
	 *        
	 * @return The {@link ObjectKey} of the referenced value, or <code>null</code> when currently no
	 *         reference is set.
	 */
	public ObjectKey getReferencedKey(MOReference reference);

    /**
     * Sets the value of the given {@link MOAttribute} in this
     * {@link DataObject}.
     * 
     * @param attribute
     *        the attribute must not be <code>null</code>
     * @param newValue
     *        the new value of the given attribute
     *        
     * @return the old value of the attribute
     * 
     * @throws DataObjectException when setting fails
     */
    public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException;

}
