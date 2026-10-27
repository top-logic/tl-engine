/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.TypeContext;

/**
 * Factory interface for {@link MetaObject}s that can be non-atomically constructed.
 * 
 * <p>
 * Through a {@link MOFactory}, {@link MetaObject}s can be constructed
 * independently from the concrete {@link MetaObject} implementation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MOFactory {

	/**
	 * Create a new MOClass with the given name.
	 */
	public MOClass createMOClass(String name);

	/**
	 * Create a new MOAttribute with given name and type.
	 */
	public MOAttribute createMOAttribute(String name, MetaObject type);

	/**
	 * Create a new {@link MOReference} with given name and the given type as target of the
	 * reference.
	 * 
	 * @param byValue
	 *        May be <code>null</code>.
	 *        <ul>
	 *        <li>If {@link Boolean#TRUE} the actual referenced object is hold in the cache of the
	 *        attribute.</li>
	 *        <li>If {@link Boolean#FALSE} the reference holds the {@link ObjectKey identifier} of
	 *        the referenced object in cache. In this case it is possible that the referenced
	 *        {@link IdentifiedObject object itself} is removed from cache.</li>
	 *        <li>If <code>null</code> the implementation of {@link MOFactory} decide which
	 *        implementation is used.</li>
	 *        </ul>
	 */
	public MOReference createMOReference(String name, MetaObject targetType, Boolean byValue);
	
	/**
	 * Creates a new builder for an alternative with the given name
	 * 
	 * @param name
	 *        see {@link MOAlternative#getName()}
	 * 
	 * @return the builder for the new alternative
	 */
	public MOAlternativeBuilder createAlternativeBuilder(String name) ;

	/**
	 * The attribute to add to a type for an attribute created from its declaration.
	 * 
	 * <p>
	 * Allows adapting a declared attribute to the branch support of the type system, e.g. the
	 * {@link BasicTypes#BRANCH_ATTRIBUTE_NAME branch attribute}, which has a database column only in a
	 * type system with multiple branches. By default, the declared attribute is used unchanged.
	 * </p>
	 * 
	 * @param declared
	 *        The attribute created from its declaration.
	 * @param multipleBranches
	 *        See {@link TypeContext#multipleBranches()} of the type system the attribute is created
	 *        for.
	 * @return The attribute to use.
	 */
	default MOAttribute adaptAttribute(MOAttribute declared, boolean multipleBranches) {
		return declared;
	}

}