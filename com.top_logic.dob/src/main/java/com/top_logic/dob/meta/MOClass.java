/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;


/**
 * This is a MetaObject for classes, This adds the concept of
 * a superclass to the MOStructure. other concepts like Methods
 * or associations are not found here anymore
 *
 * @author  Klaus Halfmann / Marco Perra
 */
public interface MOClass extends MOStructure {

    /** 
     * Indicates that instances of DataObjects for this MO can be generated.
     * 
     * Especially for Databases this does not create a Table, and unneccessary
     * access can be avoided.
     *
     * @return true if this object represent an abstract Object.
     */
    public boolean isAbstract ();

    /**
     * Whether this class cannot be sub-classed.
     */
	public boolean isFinal();


    /**
     * Setter for {@link #isAbstract()}
     */
    public void setAbstract(boolean abstractModifier);

    /**
     * Acessor to set set the (MO)SuperClass for this class.
     */
    public void setSuperclass (MOClass aMOClass);

    /**
     * Acessor to get set the (MO)SuperClass for this class
     */
    public MOClass getSuperclass ();

    /**
     * Return true if this class is inherited from other.
     *
     * @param  other a potential Superclass
     */
    public boolean isInherited (MOClass other);

    /**
     * Whether this class supports versioning.
     */
	boolean isVersioned();

	/**
	 * Sets the {@link #isVersioned()} property.
	 */
	void setVersioned(boolean versioned);

	/**
	 * Adds the given annotation to this {@link MOClass}.
	 * @param annotation
	 *        The annotation instance.
	 * 
	 * @param <T>
	 *        The annotation type.
	 */
	<T extends MOAnnotation> void addAnnotation(T annotation);

	/**
	 * Retrieves the annotation of the given annotation class.
	 * 
	 * @param <T>
	 *        The annotation type.
	 * @param annotationClass
	 *        The dynamic instance of <code>T</code>.
	 * @return The annotation of the given type.
	 */
	<T extends MOAnnotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * Removes an annotation of the given annotation class.
	 * 
	 * @param <T>
	 *        The annotation type.
	 * @param annotationClass
	 *        The dynamic instance of <code>T</code>.
	 * @return The removed annotation or <code>null</code>, if no annotation of the given class was
	 *         set.
	 */
	<T extends MOAnnotation> T removeAnnotation(Class<T> annotationClass);

	/**
	 * Whether this class represents an association.
	 */
	boolean isAssociation();

	/**
	 * Setter for {@link #isAssociation()}.
	 */
	void setAssociation(boolean isAssociation);

	/**
	 * Uses the given {@link MOAttribute} to override the attribute with the same name of the super
	 * class.
	 * 
	 * <p>
	 * Suppose this {@link MOClass} has a {@link #getSuperclass() super class} and in the super
	 * class an {@link MOAttribute} is present with the same name as the given attribute. Than the
	 * attribute of the superclass is not copied to this class, but the given attribute is used.
	 * </p>
	 * 
	 * <p>
	 * If there is no super class or no attribute with the same name in the super class, an
	 * {@link DataObjectException} is thrown in {@link #resolve(TypeContext)}.
	 * </p>
	 * 
	 * @see #resolve(TypeContext)
	 */
	void overrideAttribute(MOAttribute attribute);
    
}
