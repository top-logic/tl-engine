/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;

/**
 * Interface used to denote that an object holds MetaElements.
 * 
 * This does NOT necessarily mean that the MetaElements are used as attribute template for THIS
 * object! E.g. a project might hold the MetaElements for its sub projects, but the project's type
 * might be held by the root element.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public interface MetaElementHolder extends TLScope, TLObject {
	/**
	 * Add a type to this holder.
	 * 
	 * @param aMetaElement
	 *        the type. Must nor be <code>null</code> or duplicate
	 * @throws IllegalArgumentException
	 *         if the param violates its constraints
	 */
	public void addMetaElement(TLClass aMetaElement) throws IllegalArgumentException;

	/**
	 * Remove a type to this holder.
	 * 
	 * @param aMetaElement
	 *        the type. Has to be a type of this holder.
	 * @throws IllegalArgumentException
	 *         if the param violates its constraints
	 */
	public void removeMetaElement (TLClass aMetaElement) throws IllegalArgumentException;

	/**
	 * Get the type with the given name.
	 * 
	 * @param aMetaElementType
	 *        the type
	 * @return the type or <code>null</code> if there is none of the given type for this holder
	 * @throws IllegalArgumentException
	 *         if aMetaElementType is null
	 */
	public TLClass getMetaElement (String aMetaElementType) throws IllegalArgumentException;

	/**
	 * Get all MetaElements of this holder.
	 * 
	 * @return a list of all MetaElements of this holder.
	 * May be empty but must not be <code>null</code>.
	 */
	public Set<TLClass> getMetaElements();
}
