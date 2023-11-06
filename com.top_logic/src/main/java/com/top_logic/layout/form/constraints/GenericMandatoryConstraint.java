/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;
import java.util.Map;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Constraint that checks, whether the {@link FormField#getValue() value} of a
 * {@link FormField} is non-empty.
 * 
 * @see #check(Object) for details 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericMandatoryConstraint extends AbstractConstraint {

	/**
	 * The single shared instance of this class.
	 */
	public static final GenericMandatoryConstraint SINGLETON = 
		new GenericMandatoryConstraint();
	
	/** This class is a singleton. Use {@link #SINGLETON} instead. */
	protected GenericMandatoryConstraint() {}
	
	/**
	 * Implements the non-empty check in a generic way.
	 * 
	 * <ul>
	 * <li>{@link String} values are checked not to consist solely of whitespace.</li>
	 * <li>{@link java.util.Collection} values are checked to contain at least one element.</li>
	 * <li>{@link java.util.Map} values are checked to contain at least one element.</li>
	 * <li>All other types are checked to be not <code>null</code>.</li>
	 * </ul>
	 * 
	 * @see com.top_logic.layout.form.Constraint#check(java.lang.Object)
	 */
	@Override
	public boolean check(Object value) throws CheckException {
		if (value == null) 
			throw createNotEmptyException();
		
		if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.trim().length() == 0) {
				throw createNotEmptyException();
			}
		} else if (value instanceof Collection){
			Collection<?> collectionValue = (Collection<?>) value;
			if (collectionValue.size() == 0) {
				throw createNotEmptyException();
			}
		} else if (value instanceof Map) {
			Map<?, ?> collectionValue = (Map<?, ?>) value;
			if (collectionValue.size() == 0) {
				throw createNotEmptyException();
			}
		}
		
		return true;
	}

	private CheckException createNotEmptyException() {
		return new CheckException(Resources.getInstance().getString(
			I18NConstants.NOT_EMPTY));
	}

}
