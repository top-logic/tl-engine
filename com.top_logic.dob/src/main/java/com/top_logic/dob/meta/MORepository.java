
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.io.Serializable;
import java.util.List;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;


/** 
 * A MORepository typically manages all the MOs found one scope.
 * <p>
 *  In top-logic the @see com.top_logic.knowledge.service.KnowledgeBase
 *  uses the MORepository. Subclasses may use several sources
 *  (Java <code>Class</code>es, XML-Files, DatabaseSchemata) to create
 *    theire Meta Objects.
 * </p>
 * 
 * @author  Marco Perra
 */
public interface MORepository extends TypeContext, Serializable {

    /**
     * Return a MetaObject from the repository with the specified name.
     *
     * @exception   UnknownTypeException  if this type does not exist
     *              in the repository.
     */
	default MetaObject getMetaObject(String name) throws UnknownTypeException {
		return getType(name);
	}

	/**
	 * Adds a {@link MetaObject} into this repository.
	 * @param aMetaObject
	 *        The type to add to this repository.
	 * 
	 * @throws DuplicateTypeException
	 *         If a MetaObject for the given name already exists.
	 */
	public void addMetaObject(MetaObject aMetaObject)
    	throws DuplicateTypeException;

	/**
	 * Return a {@link MOCollection} of the specified raw type that may contain
	 * instances of the specified element type.
	 * 
	 * @param rawType
	 *        The raw type of the {@link MOCollection}. May be
	 *        {@link MOCollection#LIST} or {@link MOCollection#SET}.
	 * @param elementTypeName
	 *        The name of the element type of the collection.
	 * @exception UnknownTypeException
	 *            If the element type does not exist in this repository.
	 */
    public MetaObject getMOCollection(String rawType, String elementTypeName)
        throws UnknownTypeException;

    /**
     * Returns true if this MORepository contains the specified MetaObject
     * or supports it otherwise false will be returned.
     */
    public boolean containsMetaObject (MetaObject aMetaObject);
	
    /**
     * Returns the names of all MetaObjects known in this repository.
     *
     * @return a List of Strings representing the MetaObjects' names.
     */
	public List<String> getMetaObjectNames ();

	/**
	 * Resolves all type references and freezes all attributes of all known types.
	 * @throws DataObjectException TODO
	 */
	void resolveReferences() throws DataObjectException;

}
