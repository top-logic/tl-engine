/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.bean;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MORepository;

/**
 * Repository for all BeanMetaObjects, implemented as singleton.
 *
 * @author 	Theo Sattler / Klaus Halfmann
 */
public class BeanMORepository implements MORepository  {

	/** Yet another Singleton */
	private static BeanMORepository singleton;

	/** Yet another Singleton */
	public static BeanMORepository getInstance() {
		if (singleton == null) {
			singleton = new BeanMORepository();
        }
		return singleton;
	}

	/** Map of BeanMetaObjects indexed by Bean ClassName */
	private Map metaMap;
	
	/** List of all currently known classes */
	private List moNames;

	/** Construct a new, empty BeanMORepository */
	protected BeanMORepository() {
		metaMap = new HashMap();
		moNames = new ArrayList();
	}

	/**
	 * @see com.top_logic.dob.meta.MORepository#containsMetaObject(MetaObject)
	 */
	@Override
	public boolean containsMetaObject(MetaObject aMetaObject) {
		return  aMetaObject instanceof BeanMetaObject 
			&&	metaMap.containsKey(aMetaObject.getName());
	}

	@Override
	public MetaObject getTypeOrNull(String typeName) {
		BeanMetaObject theMo = (BeanMetaObject) metaMap.get(typeName);
		if (theMo == null) {
			theMo = createBeanMetaObject(typeName);
        }
        return theMo;
	}

    /**
	 * Variant that works directly with the class.
	 */
	public BeanMetaObject getMetaObject(Class aClass) throws IntrospectionException {
		String         className = aClass.getName();
        BeanMetaObject theMo     = (BeanMetaObject) metaMap.get(className);
        if (theMo == null) {
            theMo = createBeanMetaObject(aClass);
        }
        return theMo;
    }

    /** 
     * Internal method that actually creates (and maps) BeanMetaObjects 
     *
     * @param aClassName must not be null
     */
    protected BeanMetaObject createBeanMetaObject(String aClassName) throws UnknownTypeException {
        BeanMetaObject result = null;
        try {
            result = new BeanMetaObject(Class.forName(aClassName));
        } catch (ClassNotFoundException ex) {
        	throw new UnknownTypeException(ex);
        } catch (IntrospectionException ex) {
        	throw new UnknownTypeException(ex);
        }
        metaMap.put(aClassName, result); // may well be null
        moNames.add(aClassName);
        return result;
    }

    /** Internal method that actually creates (and maps) BeanMetaObjects */
    protected BeanMetaObject createBeanMetaObject(Class aClass) {
        BeanMetaObject result = null;
        try {
            result = new BeanMetaObject(aClass);
        } catch (IntrospectionException ie) {
            Logger.warn ("Failed to retrieve result for " + aClass, ie, this);
        }
        String className = aClass.getName();
        metaMap.put(className, result); // may well be null
        moNames.add(className);
        return result;
    }

	/**
	 * @see com.top_logic.dob.meta.MORepository#getMetaObjectNames()
	 */
	@Override
	public List getMetaObjectNames() {
		return moNames;
	}

	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		return Collections.unmodifiableCollection(metaMap.values());
	}

	/** Not supported
	 * 
	 * @return always null.
	 */
	@Override
	public MetaObject getMOCollection(String kind, String typeName)
		throws UnknownTypeException {
		return null;
	}

    /** Not supported, MetaObject are created via classes only. 
     *
     * @param   mob         the MetaObject to be stored
     */
    @Override
	public void addMetaObject (MetaObject mob)
	      throws DuplicateTypeException {
		throw new DuplicateTypeException("Not supported");
	}
    
    @Override
	public void resolveReferences() throws DataObjectException {
    	// Ignore.
    }

	@Override
	public boolean multipleBranches() {
		return false;
	}
}
