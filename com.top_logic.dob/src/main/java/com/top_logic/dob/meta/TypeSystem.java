/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.MetaObject;

/**
 * A {@link TypeSystem} contains a hierarchy of {@link MetaObject types} and
 * their relations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeSystem extends TypeContext {

	/**
	 * The common super type of all objects and associations.
	 */
	MOClass getItemType();

	/**
	 * A list of concrete (non {@link MOClass#isAbstract() abstract}) types
	 * from this repository that are
	 * {@link MetaObject#isSubtypeOf(MetaObject) assignment compatible} to the
	 * given base type.
	 * 
	 * <p>
	 * Note: This list always contains the given type, if it is non abstract.
	 * </p>
	 */
	List<? extends MetaObject> getConcreteSubtypes(MetaObject baseType);

	/**
	 * Whether there are instances that are can be assigned to variables of both
	 * given types.
	 */
	boolean hasCommonInstances(MetaObject t1, MetaObject t2);

	/**
	 * Whether instances of both given types can be compared to each other.
	 */
	boolean isComparableTo(MetaObject t1, MetaObject t2);

	/**
	 * Find a type to which both given types are assignment compatible.
	 */
	MetaObject getUnionType(MetaObject t1, MetaObject t2);

	/**
	 * Find a type that is assignment compatible to both given types. 
	 */
	MetaObject getIntersectionType(MetaObject t1, MetaObject t2);

	/**
	 * Whether all instances of the right type are also instances of the left
	 * type.
	 */
	boolean isAssignmentCompatible(MetaObject leftType, MetaObject rightType);

}
