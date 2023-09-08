/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;

/**
 * Default implementation of {@link TypeSystem} on a given set of types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTypeSystem extends AbstractTypeSystem implements TypeSystem {

	private final MOClass itemType;
	private final Map<String, MetaObject> typesByName;
	private final Map<MOClass, List<MOClass>> concreteSubtypes;

	/**
	 * Creates a {@link DefaultTypeSystem}.
	 * 
	 * @param types
	 *        All types in the new {@link TypeSystem}.
	 */
	public DefaultTypeSystem(Iterable<? extends MetaObject> types) {
		MOClass itemType = null;
		this.typesByName = new HashMap<>();
		this.concreteSubtypes = new HashMap<>();
		for (MetaObject type : types) {
			if (type.getKind() == Kind.item) {
				MOClass classType = (MOClass) type;
				concreteSubtypes.put(classType, new ArrayList<>());
			}
		}
		for (MetaObject type : types) {
			MetaObject clash = typesByName.put(type.getName(), type);
			if (clash != null) {
				throw new IllegalArgumentException("Duplicate type declaration: " + type.getName());
			}
			
			if (type.getKind() == Kind.item) {
				MOClass classType = (MOClass) type;
				MOClass superclass = classType.getSuperclass();
				if (superclass == null) {
					if (itemType != null) {
						throw new IllegalArgumentException("No common supertype of all item types.");
					}
					
					itemType = classType;
				} 
				
				if (! classType.isAbstract()) {
					// Put the type into all lists with concrete subtypes of all
					// super classes.
					MOClass ancestorClass = classType;
					while (ancestorClass != null) {
						List<MOClass> subtypesOfSuperType = concreteSubtypes.get(ancestorClass);
						if (subtypesOfSuperType == null) {
							throw new IllegalArgumentException("Undeclared super type '" + ancestorClass.getName()
								+ "' of '" + classType.getName() + "'.");
						}
						subtypesOfSuperType.add(classType);
						
						ancestorClass = ancestorClass.getSuperclass();
					}
				}
			}
		}
		
		if (itemType == null) {
			throw new IllegalArgumentException("Incomplete hierarchy without common item type.");
		}
		
		this.itemType = itemType;
	}

	@Override
	public List<? extends MetaObject> getConcreteSubtypes(MetaObject baseType) {
		if (baseType instanceof MOClass) {
			return concreteSubtypes.get(baseType);
		} else {
			return Collections.singletonList(baseType);
		}
	}

	@Override
	public MOClass getItemType() {
		return itemType;
	}

	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		return typesByName.values();
	}

	@Override
	public MetaObject getTypeOrNull(String typeName) {
		return typesByName.get(typeName);
	}

	@Override
	public boolean multipleBranches() {
		return true;
	}

}
