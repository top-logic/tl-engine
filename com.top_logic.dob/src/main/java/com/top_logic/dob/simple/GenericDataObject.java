/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.DuplicateAttributeException;

/**
 * Extends the ExampleDataObject with an ID and tracks modifications.
 * 
 * This is used by the FlexDataManager to optimize the Operations
 * to the database in <code>doStore</code>. 
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class GenericDataObject extends ExampleDataObject {

    /** Map containing all inserts for this instance. */
    public Map insertMap;

    /** Map containing all updates for this instance. */
    public Map updateMap;

    /** Map containing all deletes for this instance. */
    public Map deleteMap;

    /** The type of this data object. */
    private String type;

    /** The unique ID of this data object. */
	private TLID id;

    /**
     * @param   size    Intial size of the internaly used map.
     * @param   aType   The type of the instance.
     * @param   anID    The unique ID of the instance.
     */
    public GenericDataObject(String aType, TLID anID, int size) {
        super(size);

        this.type = aType;
        this.id   = anID;
    }

    /**
     * @param    someMap       Intial Attributes/Values for the DataObject
     * @param    aType         The type of the instance.
     * @param    anID          The unique ID of the instance.
     */
    public GenericDataObject(Map someMap, String aType, TLID anID) {
        super(someMap);
    
        this.type = aType;
        this.id   = anID;
     }

    /**
     * Create a new instance of this class.
     * 
     * @param    aType         The type of the instance.
     * @param    anID          The unique ID of the instance.
     * @param    someKeys      The names of the attributes.
     * @param    someValues    The default values.
     */
    public GenericDataObject(String aType, TLID anID, 
                             String[] someKeys, Object[] someValues) {
        super(someKeys, someValues);

        this.type = aType;
        this.id   = anID;
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    "id: '" + this.id +
                    "', type: '" + this.type +
                    "']");
    }

    /** Remove a key from a (potentially null) Map */
    protected static void removeKey(Map aMap, Object aKey) {
        if (aMap != null)
            aMap.remove(aKey);
    }


    @Override
	public Object setAttributeValue(String aName, Object aValue) {
    	Object oldValue;
        if (aValue != null) {
			oldValue = this.map.put(aName, aValue);

            if (oldValue == null) {
            	// new value
                try {
                    // Care about MetaObject, when in use
                    if ((this.meta != null) && (! this.meta.hasAttribute(aName))) {
                        MOAttributeImpl theAttr = MOAttributeImpl.makeAttribute(aName, aValue.getClass());
                        this.meta.addAttribute(theAttr);
                    }
                } catch (DuplicateAttributeException ex) {
                	// hasAttribute() was checked.
                	throw new UnreachableAssertion(ex);
                }
                
                // Still remove it it may have been an update before ...
                // removeKey(this.deleteMap, aName);

                Map theMap = this.insertMap;
                if (theMap == null) {
                	theMap = this.insertMap = new HashMap();
                }
                theMap.put(aName, aValue);
            } else {
            	// theOld != null 
                if (! oldValue.equals(aValue)) {
                    Map theMap = this.insertMap;
                
                    // Check if value is to be inserted.
                    if (theMap == null || !theMap.containsKey(aName)) {
                        theMap = this.updateMap;
                        if (theMap == null) {
                        	theMap = this.updateMap = new HashMap();
                        }
                    } else { // insertMap.containsKey
                        theMap = this.insertMap;
                    }
                    theMap.put(aName, aValue);
                } else {
                	// theOld.equals(aValue)
                    // nothing to do: value is equal anyway
                }
            }
        } else {
        	// aValue == null 
            oldValue = this.map.remove(aName);
            if (oldValue == null) {
            	// nothing actually deleted
            	return oldValue;
            }

            // Value deleted after insert/update ?
            removeKey(this.insertMap, aName);
            removeKey(this.updateMap, aName);

            Map theMap = this.deleteMap;
            if (theMap == null) {
            	theMap = this.deleteMap = new HashMap();
            }
            theMap.put(aName, aName);
            
            this.meta = null;
        }

        return oldValue; 
    }

    /**
     * Clear all the maps (usually after an update / insert)
     */
    public void resetMaps() {
        this.insertMap =
        this.updateMap = 
        this.deleteMap = null;
    }

    /**
     * @see com.top_logic.dob.DataObject#getIdentifier()
     */
    @Override
	public TLID getIdentifier() {
        return (this.id);
    }

    /**
     * @see com.top_logic.dob.simple.ExampleDataObject#getMetaObjectName()
     */
    @Override
	public String getMetaObjectName() {
        return (this.type);
    }
    
    /** 
     * @see com.top_logic.dob.DataObject#isInstanceOf(com.top_logic.dob.MetaObject)
     */
    @Override
	public boolean isInstanceOf(MetaObject aMetaObject) {
    	return (this.getMetaObjectName().equals(aMetaObject.getName()));
    }
    
    /** 
     * @see com.top_logic.dob.DataObject#isInstanceOf(java.lang.String)
     */
    @Override
	public boolean isInstanceOf(String aMetaObjectName) {
    	return (this.getMetaObjectName().equals(aMetaObjectName));
    }
}