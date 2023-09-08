/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.template.model.ExpansionModel;

/**
 * Template function that checks whether a given object is of type {@link Iterator} in the context
 * of the {@link ExpansionModel} and is not empty (means {@link Iterator#hasNext()} returns
 * <code>true</code>).
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class IsEmptyFunction extends AbstractBooleanFunction {
	
	public static final String FUNCTION_NAME = "isEmpty";
	private final ExpansionModel model;
	
	public IsEmptyFunction(ExpansionModel aModel) {
		super(FUNCTION_NAME);
		this.model      = aModel;
	}

	/**
	 * Checks if the first object in the given {@link List} of the correct type. If that is the case
	 * the result of {@link Iterator#hasNext()} will be returned. Otherwise an
	 * {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param arguments a {@link List} of arguments
	 * @return <code>true</code> if the (Collection)object is empty or <code>null</code>,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws IllegalArgumentException if the argument has no {@link Iterator} in the context of
	 *             the used {@link ExpansionModel}.
	 */
	@Override
	public Object apply(List<?> arguments) {
		Object a1 = arguments.get(0);

		Iterator<?> theIt = this.model.getIteratorForObject(a1);
		if (theIt != null) {
			return ! theIt.hasNext();
		}
		
		throw new IllegalArgumentException("Wrong type of argument for function " + FUNCTION_NAME + ". Given: " + a1.getClass() + ", expected was an Iterator.");
	}

	@Override
	protected List<MetaObject> getArgumentTypes() {
		List<MetaObject> theArgumentTypes = new ArrayList<>();
		
		theArgumentTypes.add(MOCollectionImpl.createCollectionType(MetaObject.ANY_TYPE));
		return theArgumentTypes;
	}
}
