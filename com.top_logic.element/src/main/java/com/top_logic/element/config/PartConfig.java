/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;


import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.AttributeConfigBase;
import com.top_logic.model.config.PartAspect;
import com.top_logic.model.config.TypeRef;


/**
 * The configuration for a meta attribute, containing also properties which are directly accessible
 * over the {@link TLStructuredTypePart} interface.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 * @author Dieter Rothb&auml;cher
 */
@Abstract
public interface PartConfig extends AttributeConfigBase, PartAspect, TypeRef {
    
	/** Property name of {@link #getMandatory()}. */
	String MANDATORY = TLStructuredTypePart.MANDATORY_ATTR;
    
	/** @see #isMultiple() */
	String MULTIPLE_PROPERTY = TLReference.MULTIPLE_ATTR;

	/** @see #isAbstract() */
	String ABSTRACT_PROPERTY = TLStructuredTypePart.ABSTRACT_ATTR;

	/** @see #isOrdered() */
	String ORDERED_PROPERTY = TLReference.ORDERED_ATTR;

	/** @see #isBag() */
	String BAG_PROPERTY = TLReference.BAG_ATTR;

	/** Name of the property {@link #isOverride()}. */
	String OVERRIDE = "override";

	/**
	 * Whether this attribute may not be empty.
	 * 
	 * @see TLStructuredTypePart#isMandatory()
	 */
	@Name(MANDATORY)
	public boolean getMandatory();
    
    /**
     * @param aMandatory The mandatory to set.
     */
	public void setMandatory(boolean aMandatory);
    
	/**
	 * Whether this attribute can hold multiple values of {@link #getTypeSpec()} (collection
	 * valued).
	 * 
	 * @see TLStructuredTypePart#isMultiple()
	 */
	@Name(MULTIPLE_PROPERTY)
	boolean isMultiple();

	/** @see #isMultiple() */
	void setMultiple(boolean value);

	/**
	 * Whether this attribute must be overridden in concrete classes.
	 * 
	 * @see TLStructuredTypePart#isAbstract()
	 */
	@Name(ABSTRACT_PROPERTY)
	boolean isAbstract();

	/** @see #isAbstract() */
	void setAbstract(boolean value);

	/**
	 * If {@link #isMultiple()}, whether the order of values of this attribute is significant (list
	 * or ordered set valued).
	 * 
	 * @see TLStructuredTypePart#isOrdered()
	 */
	@Name(ORDERED_PROPERTY)
	boolean isOrdered();

	/** @see #isOrdered() */
	void setOrdered(boolean value);

	/**
	 * If {@link #isMultiple()}, whether the same value may appear more than once in the values of
	 * this attribute (not set valued).
	 * 
	 * @see TLStructuredTypePart#isBag()
	 */
	@Name(BAG_PROPERTY)
	boolean isBag();

	/** @see #isBag() */
	void setBag(boolean value);

	/**
	 * Whether this is an override of an existing reference.
	 * <p>
	 * Analogous to the Java {@link Override} annotation.
	 * </p>
	 */
	@Name(OVERRIDE)
	boolean isOverride();

	/** Setter for {@link #isOverride()}. */
	void setOverride(boolean isOverride);

}
