/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.Iterator;

import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.Function;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.TypeSystem;

/**
 * This interface is used to enable the replacement of tokens and defined blocks in a template using
 * the template language.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public interface ExpansionModel {
	
	/**
	 * This method returns the type of the object that is specified by the given nameSpace and name.
	 * In the template these values are specified inside assignments: "$nameSpace:name". "$" and ":"
	 * are not part of the actual values.
	 * 
	 * @param nameSpace a String defining the nameSpace.
	 * @param name a String defining the name.
	 * @return the {@link MetaObject}.
	 */
	public MetaObject getTypeForVariable(String nameSpace, String name);
	
	/** 
	 * Returns the value for an object specified by nameSpace and name.
	 * 
	 * @param nameSpace a String defining the nameSpace.
	 * @param name a String defining the name.
	 * @return the {@link Object}.
	 */
	public Object getValueForVariable(String nameSpace, String name);
	
	/**
	 * Returns the attribute value for a given object and attribute name.
	 * 
	 * @param object the object whose attribute is needed
	 * @param attribute the name of the attribute
	 * @return the value of the attribute with the given name
	 * @throws NoSuchAttributeException if the given object has no attribute with the given name.
	 */
	public Object getValueForAttribute(Object object, String attribute) throws NoSuchAttributeException;
	
	/** 
	 * Returns the {@link TypeSystem} used for the {@link MetaObject}s in this ExpansionModel.
	 * 
	 * @return this ExpansionModels {@link TypeSystem}.
	 */
	public TypeSystem getTypeSystem();
	
	/**
	 * Returns the {@link MetaObject} describing the {@link Function} with the given name or
	 * <code>null</code> if the name is unknown.
	 * 
	 * @param name a function name
	 * @return the {@link MOFunction} for the given name or <code>null</code>.
	 */
	public MOFunction getTypeForFunction(String name);
	
	/**
	 * Returns the {@link Function} object for the given name or <code>null</code> if the name is
	 * unknown.
	 * 
	 * @param name a function name
	 * @return the {@link Function} for the given name or <code>null</code>
	 */
	public Function getImplementationForFunction(String name);
	
	/**
	 * Returns the {@link Iterator} for the given Object or <code>null</code> if the given object
	 * has no Iterator.
	 * 
	 * @param object the Object the Iterator is needed for
	 * @return the respective {@link Iterator} or <code>null</code>.
	 */
	public Iterator<?> getIteratorForObject(Object object);

	/**
	 * Formats the given {@link Object}.
	 * <p>
	 * Returns {@link Maybe#none()} if it cannot format the given value.<br/>
	 * {@code return Maybe.none()} is a valid implementation.
	 * </p>
	 * 
	 * @param value
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public Maybe<String> format(Object value);

}
