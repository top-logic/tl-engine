/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.Function;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.TypeSystem;

/**
 * Self-contained {@link ExpansionModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicExpansionModel extends DynamicDataObject implements ExpansionModel {

	private TypeSystem typeSystem;
	
	private Map<String, Function> functions = new HashMap<>();

	/**
	 * Creates a new {@link DynamicExpansionModel} with a single type called 'Object'.
	 */
	public DynamicExpansionModel() {
		typeSystem = new DefaultTypeSystem(Collections.singletonList(new MOClassImpl("Object")));
	}

	/**
	 * Creates a new {@link DynamicExpansionModel} with the given types.
	 */
	public DynamicExpansionModel(Iterable<? extends MetaObject> types) {
		typeSystem = new DefaultTypeSystem(types);
	}

	/**
	 * Defines the given function within this {@link ExpansionModel}.
	 * 
	 * @param name
	 *        The name of the function.
	 * @param function
	 *        The funtion implementation.
	 */
	public void defineFunction(String name, Function function) {
		functions.put(name, function);
	}
	
	@Override
	public MetaObject getTypeForVariable(String nameSpace, String name) {
		try {
			return tTable().getAttribute(name).getMetaObject();
		} catch (NoSuchAttributeException ex) {
			throw errorNoSuchVariable(name);
		}
	}

	@Override
	public Object getValueForVariable(String nameSpace, String name) {
		try {
			return getAttributeValue(name);
		} catch (NoSuchAttributeException ex) {
			throw errorNoSuchVariable(name);
		}
	}

	@Override
	public Object getValueForAttribute(Object object, String attribute) throws NoSuchAttributeException {
		return ((DataObject) object).getAttributeValue(attribute);
	}

	@Override
	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	@Override
	public MOFunction getTypeForFunction(String name) {
		return getImplementationForFunction(name).getType();
	}

	@Override
	public Function getImplementationForFunction(String name) {
		return functions.get(name);
	}

	@Override
	public Iterator<?> getIteratorForObject(Object object) {
		return ((Iterable<?>) object).iterator();
	}

	private IllegalArgumentException errorNoSuchVariable(String name) {
		return new IllegalArgumentException("No such variable '" + name + "'");
	}

	@Override
	public Maybe<String> format(Object value) {
		return Maybe.none();
	}

}
