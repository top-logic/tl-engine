
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import java.util.Map.Entry;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;

/**
 * Abstract class for MetaObject Repositories, based on a Map.
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public class DefaultMORepository implements MORepository {

    /**
     * Hashtable to store the MetaObjects (fully MetaObject name as key)
     */
	private Map<String, MetaObject> metaObjectMap = new HashMap<>();

	private final boolean _multipleBranches;

    /**
	 * Create a new {@link DefaultMORepository} with a Map of {@link MetaObject}s.
	 * 
	 * @param aMetaMap
	 *        a Map of {@link MetaObject}s indexed by {@link MetaObject#getName()}.
	 */
	public DefaultMORepository(Map<String, MetaObject> aMetaMap, boolean multipleBranches) {
        metaObjectMap = aMetaMap;
		_multipleBranches = multipleBranches;
    }
    
	/**
	 * Create a new empty {@link DefaultMORepository} .
	 */
	public DefaultMORepository(boolean multipleBranches) {
		this(new HashMap<>(64), multipleBranches);
	}

	@Override
	public MetaObject getTypeOrNull(String name) {
		return metaObjectMap.get(name);
	}
    
    @Override
	public void addMetaObject (MetaObject mob) throws DuplicateTypeException {
    	String className = mob.getName();
        if (metaObjectMap.containsKey (className)) {
            throw new DuplicateTypeException (
                "Class " + className + " already exists in repository!");
        }
        metaObjectMap.put (className, mob);
    }
    
    @Override
	public boolean containsMetaObject (MetaObject aMetaObject) {
        return metaObjectMap.containsKey(aMetaObject.getName());
    }

    @Override
	public MetaObject getMOCollection(String rawType, String elementTypeName) throws UnknownTypeException {
        MetaObject mo = getMetaObject (elementTypeName);
        if (rawType.equals(MOCollection.LIST)) {
            return MOCollectionImpl.createListType(mo);
        }
        if (rawType.equals(MOCollection.SET)) {
            return MOCollectionImpl.createSetType(mo);
        }
        throw new UnknownTypeException("Unknown kind " + rawType);
    }

	@Override
	public List<String> getMetaObjectNames() {
		return new ArrayList<>(metaObjectMap.keySet());
	}

	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		return Collections.unmodifiableCollection(metaObjectMap.values());
	}

	@Override
	public void resolveReferences() throws DataObjectException {
		for (Entry<String, MetaObject> entry : metaObjectMap.entrySet()) {
			MetaObject resolvedType = entry.getValue().resolve(this);
			entry.setValue(resolvedType);
		}
		for (MetaObject type : metaObjectMap.values()) {
			type.freeze();
		}

	}
	
	@Override
	public boolean multipleBranches() {
		return _multipleBranches;
	}
	
}
