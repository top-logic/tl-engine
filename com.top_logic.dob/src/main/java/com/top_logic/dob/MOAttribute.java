
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.sql.DBAttribute;


/** 
 * Description of a {@link DataObject} attribute.
 * 
 * @see MOStructure#getAttribute(String)
 *
 * @author 	Marco Perra
 */
public interface MOAttribute extends MOPart {

	/**
	 * Empty array of type {@link MOAttribute}.
	 */
	public static final MOAttribute[] NO_MO_ATTRIBUTES = new MOAttribute[0];

    /** Convenience constant for {@link #setMandatory(boolean)} argument. */
    public static final boolean MANDATORY = true;

    /** Convenience constant for {@link #setImmutable(boolean)} argument. */
    public static final boolean IMMUTABLE = true;

	/**
	 * The {@link MOStructure} this {@link MOAttribute} belongs to.
	 */
	public MOStructure getOwner();

	/**
	 * Initializes the {@link #getOwner()} property of this {@link MOAttribute}.
	 * 
	 * <p>
	 * Note: This method must only be called once.
	 * </p>
	 * 
	 * @param newOwner
	 *        The owner of this {@link MOAttribute}.
	 * @param newIndex
	 *        The first index in the cache which is reserved for this attribute. So the attribute
	 *        can use the index <code>n</code> where <i>index &lt;= n &lt; n+cacheSize</i> and
	 *        <i>cacheSize</i> is the value returned by {@link AttributeStorage#getCacheSize()}
	 * 
	 * @see AttributeStorage#getCacheSize()
	 * @see AttributeStorage#getCacheValue(MOAttribute, DataObject, Object[])
	 * @see AttributeStorage#setCacheValue(MOAttribute, DataObject, Object[], Object)
	 */
	public void initOwner(MOStructure newOwner, int newIndex);
	
	/**
	 * The index of the first slot in the cache used by this attribute.
	 */
	int getCacheIndex();

	/**
	 * The type of this attribute.
	 */
    public MetaObject getMetaObject();
    
    /**
     * Sets the type of this attribute.
     * 
     * Must not be called on a {@link #isFrozen()} attribute.
     * 
     * @param type the new type.
     * 
     * @see #isFrozen()
     * @see #freeze()
     */
	public void setMetaObject(MetaObject type);

	/**
	 * Whether the value of this attribute is mandatory and cannot be
	 * <code>null</code>.
	 */
    public boolean isMandatory();

	/**
	 * Sets the {@link #isMandatory()} property of this attribute.
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param value
	 *        the new value of the mandatory flag.
	 *        
     * @see #isFrozen()
     * @see #freeze()
	 */
    public void setMandatory(boolean value);

	/**
	 * Whether this attribute is an initial attribute.
	 * 
	 * <p>
	 * If <code>true</code> then a value for this attribute is needed during construction. Moreover
	 * the value can not be changed until the new object is committed.
	 * </p>
	 * 
	 */
	boolean isInitial();

	/**
	 * Sets value of {@link #isInitial()}
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param value
	 *        the new value of the initial flag.
	 * 
	 * @see #isFrozen()
	 * @see #freeze()
	 * @see #isInitial()
	 */
	void setInitial(boolean value);

    /** 
     * Whether this attribute can be set only once.
     */
    public boolean isImmutable();

	/**
	 * Sets the {@link #isImmutable()} property of this attribute.
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param value
	 *        the new value of the immutable flag.
	 *        
     * @see #isFrozen()
     * @see #freeze()
	 */
    public void setImmutable(boolean value);

    /**
     * Corresponding implementation attribute(s).
     * 
     * @return non <code>null</code> array.
     */
	public DBAttribute[] getDbMapping();

	/**
	 * Whether this attribute is an internal attribute that should not be
	 * exposed to the application.
	 */
	boolean isSystem();
	
	/**
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @see #isSystem()
	 * 
     * @see #isFrozen()
     * @see #freeze()
	 */
	void setSystem(boolean value);

	/**
	 * Returns the storage strategy for this {@link MOAttribute}.
	 */
	AttributeStorage getStorage();

	/**
	 * Sets the storage strategy for this {@link MOAttribute}.
	 */
	@FrameworkInternal
	void setStorage(AttributeStorage storage);

	/**
	 * Whether this attribute is displayed at the gui or not.
	 */
	boolean isHidden();

	/** @see #isHidden() */
	void setHidden(boolean value);

	/**
	 * Creates an (unresolved) copy of this {@link MOAttribute}.
	 */
	public MOAttribute copy();

	/**
	 * Resolves this attribute.
	 * 
	 * @param context
	 *        The context in which to resolve the {@link #getMetaObject()}.
	 * @throws DataObjectException See {@link MetaObject#resolve(TypeContext)}
	 */
	public void resolve(TypeContext context) throws DataObjectException;

	/**
	 * The provider creating the default value for this attribute.
	 * 
	 * @return A {@link MODefaultProvider} creating the default for this attribute. May be
	 *         <code>null</code>.
	 * 
	 * @see AttributeConfig#getDefaultProvider()
	 */
	MODefaultProvider getDefaultProvider();

	/**
	 * Setter for {@link #getDefaultProvider()}.
	 * 
	 * @param defaultValue
	 *        New value of {@link #getDefaultProvider()}.
	 */
	void setDefaultProvider(MODefaultProvider defaultValue);

}
