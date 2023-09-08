/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;

/**
 * {@link AbstractClassFilter} is a {@link Filter} which first checks that the
 * object is an instance of a given class and then delegates to the actual check
 * method.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractClassFilter<T> implements Filter<Object>{

	/**
	 * The class of accepted objects. 
	 */
	protected final Class<? extends T> testClass;
	
	/**
	 * Constructs a {@link ClassFilter} that only accepts objects that are
	 * instances of the given class (or their subclasses).
	 */
	public AbstractClassFilter(Class<? extends T> testClass) {
		if (testClass == null) 
			throw new NullPointerException("testClass");
		
		this.testClass = testClass;
	}

	/**
	 * Tests, whether the given object is an instance of the {@link #testClass}.
	 * 
	 * @param anObject
	 *        The tested object.
	 * 
	 * @return true when anObject is != <code>null</code>, assignable from the
	 *         {@link #testClass}, and {@link #internalAccept(Object)}
	 *         successes.
	 */
	@Override
	public boolean accept(Object anObject) {
		return anObject != null && this.testClass.isAssignableFrom(anObject.getClass())
				&& internalAccept(testClass.cast(anObject));
	}

	/** 
	 * Checks whether the given instance of the test class is accepted.
	 * 
	 * @param object not <code>null</code>
	 */
	protected abstract boolean internalAccept(T object);

}
