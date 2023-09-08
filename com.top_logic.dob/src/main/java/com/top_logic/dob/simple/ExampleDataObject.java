/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.StaticTyped;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOStructureImpl;

/** Standalone Version of a DataObject, can be constructed from Maps or Arrays.
 *<p>
 *  This DataObject may be used whenever you need to create some DataObject
 *  <em>in Place</em> without further Environment. You should not use it
 *  when you plan to create lots of similar objects.
 *</p>
 * <p>
 *  The Method <code>getMetaObject</code> is implemented lazy. Therefore
 *  dont expect that the MetaObject is correct, especially when you change
 *  the underlying Map. When <code>null</code> is used as value the MetaObject 
 *  will assume <code>Void</code> as MetaObjectType.
 * </p>
 *<p>
 *  Its planned main-usage was the EXAMPLE-Operator in the new QueryLanguage
 *  in <i>TopLogic</i> 3.0. But it is used in various other places by now.
 *</p>
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ExampleDataObject implements DataObject {

    /** This is the map we actually use. */
	protected Map<String, Object> map;

    /** Our Local MetaObject */
    protected transient MOStructure meta;
    
    /** Create a completly empty object.
     * 
     * Dear subclasses, use with care !
     */
   protected ExampleDataObject() {
       // use with care
   }

    /** Create the Object with a hashMap of given size.
     *
     * @param size intial size of internal Map.
     */
    public ExampleDataObject(int size) {
		map = new HashMap<>(size);
    }


    /** Create the Object with the given Map.
     *
     * @param someMap as an Example of the Implementation
     */
	public ExampleDataObject(Map<String, ?> someMap) {
		map = new HashMap<>(someMap);
    }
    
    /** Create the Object with a given set of names and values
     *
     * @param names     The Attribute names
     * @param values    The Example values for the Attribute names.
     *
     * @throws IllegalArgumentException in case the length of 
     *                                  the Arrays is unequal
     */
    public ExampleDataObject(String names[], Object values[]) {
        int len = names.length;
        if (len != values.length) {
            throw new IllegalArgumentException(
                "Unequal array length: " + len + "!="  +values.length);
        }

		map = new HashMap<>(len);
        for (int i=0; i < len; i++) {
            map.put(names[i], values[i]);
        }
    }

    /** 
     * Standard, deep equals Operator.
     * TODO KHA: overwrite hashCode() 
     */
    @Override
	public boolean equals(Object theObject) {
    	if (theObject == this) {
    		return true;
    	}
		else if (theObject instanceof ExampleDataObject) {
            ExampleDataObject   other    = (ExampleDataObject) theObject;
            Map<String, Object> otherMap = other.map;

            for (Entry<String, Object> theEntry : map.entrySet()) {
                String key = theEntry.getKey();

                if (!otherMap.containsKey(key)) { 
                	return false;
                }

                Object myVal    = theEntry.getValue();
                Object otherVal = otherMap.get(key);
                // null resistant equals 
                if (myVal != null) {
                    if (!myVal.equals(otherVal)) 
                        return false;
                }
                else  { // myVal == null
                    if (null != otherVal)
                        return false;
                }
            }
            return true;    // all are equal
        } 
        else if (theObject instanceof DataObject) {
            DataObject other = (DataObject) theObject;

            try {
				for (Entry<String, Object> theEntry : map.entrySet()) {
					String key = theEntry.getKey();

					if (!other.getAttributeValue(key).equals(theEntry.getValue())) {
                        return false;
                    }
                }
            }
            catch (NoSuchAttributeException nse) {
                return false;   // No Such Attribute -> not equal
            }
            return true;     // all are equal
        }
        return false;
    }
    
    @Override
	public int hashCode() {
        return map.hashCode();
    }

    /**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tTable()} instead
	 */
	@Override
	@Deprecated
	public final MetaObject getMetaObject() {
		return tTable();
	}

	/** 
     * Returns the MetaObject this object represents.
     *
     * @return somve private MetaObject, unique for this instance.
     */
    @Override
	public MetaObject tTable() {
        if (this.meta == null)  {
			this.meta = this.byExample(this.getMetaObjectName(), map);
        }

        return (this.meta);
    }

    @Override
	public boolean isInstanceOf (MetaObject aMetaObject) {
        return false;
    }

    /** Similar to Class.instanceOf(),
     *
     * @param   aMetaObjectName check if we are of this type.
     * @return  always false for now
     */
    @Override
	public boolean isInstanceOf (String aMetaObjectName) {
        return false;
    }

    @Override
	public TLID getIdentifier() {
		// Pseudo identifier based on the system identity hash code. Borrowed
		// from Object.toString() without relying on the instance hashCode().
		return StringID.valueOf(getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)));
    }

    /** Ignored since we have no identifier.
     *
     * @param   anIdentifier    ignored
     */
    @Override
	public void setIdentifier(TLID anIdentifier) {
        // ignored        
    }

    /** 
     * not supported yet.
     * Returns an iterator over the attributes (of type MOAttribute).
     * 
     * @return null 
     */
    @Override
	public Iterable<? extends MOAttribute> getAttributes () {
        return null;    // not supported yet.
    }

	/** Returns the list of attribute names defined in the MetaObject.
	 *
	 * @return   The list of known attributes.
	 */
	@Override
	public String [] getAttributeNames () {

        String result[] = new String[map.size()];
        map.keySet().toArray(result);
        return result;
    }

	@Override
	public boolean hasAttribute(String attributeName) {
		return map.containsKey(attributeName);
	}
    
    @Override
	public Object getAttributeValue (String attrName) throws NoSuchAttributeException {
        return map.get(attrName);    
    }
    
    @Override
	public Object setAttributeValue (String attributeName, Object newValue) {
        if (newValue != null) {
            Object  oldValue = this.map.put(attributeName, newValue);

            if (oldValue == null) {
                // Potential new value, update MetaObject, if it is in use.
                if ((this.meta != null) && (! this.meta.hasAttribute(attributeName))) {
                    MOAttributeImpl theAttr = MOAttributeImpl.makeAttribute(attributeName, newValue.getClass());
                    try {
						this.meta.addAttribute(theAttr);
					} catch (DuplicateAttributeException ex) {
						throw new UnreachableAssertion(ex);
					}
                }
            }
            
            return oldValue;
        } else {
        	// aValue == null 
        	return this.map.remove(attributeName);
        }
    }

    /** return the Map actually used internally */
	public Map<String, Object> getMap() {
        return map;
    }

    /** Create a Map from the given DataObject */
	public static Map<String, Object> createMap(DataObject aDO) {
        if (aDO instanceof ExampleDataObject) {
            return ((ExampleDataObject) aDO).getMap();
        }

        String names[]= aDO.getAttributeNames();
		int len = names.length;
		Map<String, Object> result = new HashMap<>(len);

        try {
            for (int i=0; i< len; i++) {
                String name = names[i];

                // Get the Property value
                Object value = aDO.getAttributeValue (name);

                // Set attribute recursively
                result.put(name, value);
            }
        }
        catch (NoSuchAttributeException nsax)  {
            Logger.error("createMap() failed", nsax, ExampleDataObject.class); 
        }
        return result;
    }

    /**
     * Return the name of the meta object of this example object.
     * 
     * In this implementation, the name is the string returned by
     * this toString() method.
     * 
     * @return    The name of the meta object.
     */
    public String getMetaObjectName() {
        return (this.toString());
    }

	@Override
	public Object getValue(MOAttribute attribute) {
		return AbstractDataObject.getValue(this, attribute);
	}
	
	@Override
	public ObjectKey getReferencedKey(MOReference reference) {
		return AbstractDataObject.getReferencedKey(this, reference);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		return AbstractDataObject.setValue(this, attribute, newValue);
	}

	/**
	 * Create a MOStructure using an Map of arbitrary Objects as an example.
	 * 
	 * This implementation will allow nested DataObjects. It maps <code>null</code>-values to
	 * {@link MOPrimitive#VOID}. It will even survive <code>null</code>-keys.
	 * 
	 * @param moName
	 *        The name to use for MetaObject
	 * @return MOStructure derived by using aMap as an example.
	 */
	protected MOStructure byExample(String moName, Map<String, Object> aMap) {
		MOStructure struct = new MOStructureImpl(moName);

		for (Entry<String, Object> theEntry : aMap.entrySet()) {
			String      theKey   = theEntry.getKey();
			Object      theValue = theEntry.getValue();
			MOAttribute theAttr;

			if (theValue != null) {
				if (theValue instanceof StaticTyped) {
					// A MetaObjectTyped within a MetaObjectTyped
					theAttr = new MOAttributeImpl(theKey, ((StaticTyped) theValue).tTable());
				}
				else {
					theAttr = MOAttributeImpl.makeAttribute(theKey, theValue);
				}
			}
			else { // Don't know what to support
				theAttr = MOAttributeImpl.makeAttribute(theKey, Void.class);
			}

			try {
				struct.addAttribute(theAttr);
			}
			catch (DuplicateAttributeException ex) {
				throw new UnreachableAssertion("Original keys were unique.");
			}
		}
		return struct;
	}
}
