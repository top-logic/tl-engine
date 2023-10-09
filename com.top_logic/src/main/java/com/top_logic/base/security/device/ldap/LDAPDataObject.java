/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import com.top_logic.base.security.attributes.LDAPAttributes;
import com.top_logic.base.security.device.DeviceMapping;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.AttributesDataObject;
import com.top_logic.dob.simple.AttributesMetaObject;

/**
 * Extends the AttributesDataObject to Map Attribute names to other names.
 * 
 * Allows ReadOnly Access to AttributeObject originating from the JNDI Services.
 * 
 * Automatically uses a mapping to match the top-logic internal attribute names
 * to the externally used attribute names.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler </a>
 */
public class LDAPDataObject extends AttributesDataObject {

    /** Classes for which no mapping exists for at least one of its types. */
    private static Set<String>  unmappedClasses;   

    private DeviceMapping   genericMapping;

    private String          myDeviceID;
    
	private LDAPAuthenticationAccessDevice myDevice;
    
	private TLID identifier;
    
    private String          objectClass;
    
    private Map             attributeValues;
    
    /**
     * Creates an instance of this class.
     * 
     * @param someAttr
     *            The attributes for this instance.
     */
	public LDAPDataObject(Attributes someAttr, LDAPAuthenticationAccessDevice aDevice) throws NamingException {
        super(someAttr);
        this.attributeValues = new HashMap();
        this.myDeviceID = aDevice.getDeviceID();
        this.myDevice = aDevice;
        this.genericMapping = aDevice.getMapping(DeviceMapping.OBJ_CLASS_GENERIC);
        this.identifier = StringID.createRandomID (); 
        this.objectClass = _getObjectClass();
        
        //simplified attempt of resolving / retrieving values now to avoid LDAP connects during access
        //it doesn't take multivalue attributes into account, as  we just want to support the DO interface, its enough for now
        //we lowercase the key since the jndi key value mapping was not case sensitive so we don't want it to be now
        NamingEnumeration theIDs = someAttr.getIDs();
        while(theIDs.hasMore()){
            String theID = (String)theIDs.next();
            Object attr = someAttr.get(theID);
            if(attr instanceof Attribute){
                Object theValue = (((Attribute) attr).get());
                this.attributeValues.put(theID.toLowerCase(),theValue); 
            }else{
                this.attributeValues.put(theID.toLowerCase(),attr);
            }
        }
    }
    
    /**
     * Use an LDAPMetaObject here to handle class hierarchy correctly
     * 
     * @see com.top_logic.dob.simple.AttributesDataObject#createMetaObject()
     */
    @Override
	protected AttributesMetaObject createMetaObject() {
        return new LDAPMetaObject(this.attrs, this.objectClass);
    }

    @Override
	public Iterable<? extends MOAttribute> getAttributes() {
		return this.attributeValues.keySet();
    }

    @Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
        String theName = this.getExternalAttrName(attrName).toLowerCase();
        Object theValue = this.attributeValues.get(theName);
        if (theValue instanceof Attribute) {
            try {
                return (((Attribute) theValue).get());
            } catch (NamingException ex) {
                Logger.error("getAttributeValue(\"" + attrName + "\", [real: \"" + theName + "\"]): this=" + this + ", error: " + ex, this);
                return null;
            }
        } else {
            return theValue;
        }

    }

    @Override
	public Object setAttributeValue(String attrName, Object value) {
        throw new IllegalStateException("Write Access is not supported on this object");
    }

    /**
     * Overwritten to match DataObjectImpl which is used by other security devices
     * This is to make sure that if a.equals(b) then b.equals(a)
     */
    @Override
	public boolean equals(Object anObject) {
        boolean isEqual = false;

        if (anObject == this) {
            isEqual = true;    // shortcut identity
        }
        else if (anObject instanceof DataObject) {
			TLID theID = ((DataObject) anObject).getIdentifier();
			TLID thisID = this.getIdentifier();
            isEqual = thisID.equals(theID);
        }//Else: Keep false default.

        return isEqual;
    }

    /** 
     * Relay hashCode to identifier 
     * Overwritten to match DataObjectImpl which is used by other security devices
     * This is to make sure that if a.equals(b) then b.equals(a)
     */
    @Override
	public int hashCode() {
		TLID id = this.getIdentifier();
        return (id == null) ? super.hashCode() 
                            : id.hashCode();
    }
    
    @Override
	public TLID getIdentifier(){
        return this.identifier;
    }

    /**
     * Return the string representation of this instance. This should be used
     * for debugging options only!
     * 
     * @return The string representation of this instance.
     */
    @Override
	public String toString() {
		StringBuffer theList = new StringBuffer();

        if (this.attrs != null) {
            Attribute theAttr;
            String theName;
            Object theValue;
            NamingEnumeration theEnum = this.attrs.getAll();

            try {
                while (theEnum.hasMore()) {
                    theAttr = (Attribute) theEnum.next();
                    theName = theAttr.getID();
                    theValue = theAttr.get();

					if (theList.length() > 0) {
                        theList.append(", ");
                    }

                    theList.append(theName).append('=').append(theValue);
                }
            } catch (Throwable ex) {
				if (theList.length() > 0) {
					theList.append(", ");
				}
                theList.append(ex);
            }
        }

        return (this.getClass().getName() + " [" + "attributes: (" + theList + ")]");
    }

    /**
     * Returns the JNDI attributes.
     * 
     * @return The JNDI attributes.
     */
    public Attributes getJNDIAttributes() {
        return (this.attrs);
    }

    /**
     * Return the name from the JNDI service to be used for the specified
     * <i>TopLogic</i> attribute name. If there is no matching name, the given one
     * will be returned.
     * 
     * @param aName
     *            The <i>TopLogic</i> attribute name.
     * @return The JNDI attribute name.
     */
    private String getExternalAttrName(String aName) {
        return (this.getTypeSpecificMapping().mapAttribute(aName, aName));
    }

    /**
     * the object class of this object as seen in the directory system
     */
    private String _getObjectClass() {
        StringBuffer theName = null;
		Attribute theAttr =
			this.attrs.get(genericMapping.mapAttribute(LDAPAttributes.OBJECT_CLASS, LDAPAttributes.OBJECT_CLASS));
        int theSize = theAttr.size();

        for (int thePos = 0; thePos < theSize; thePos++) {
            try {
                if (theName == null) {
                    theName = new StringBuffer(theAttr.get(thePos).toString());
                } else {
                    theName.append('.').append(theAttr.get(thePos));
                }
            } catch (NamingException ex) {
                Logger.error("Unable to get name of object " + this.attrs + ", reason is: " + ex, this);
            }
        }
        return (theName.toString());
    }

    /**
	 * @see com.top_logic.dob.simple.AttributesDataObject#tTable()
	 */
    protected String getMetaObjectName() {
        return this.getObjectClass();
    }

    /**
     * a string with all object classes of this object (may be in arbitrary order for multiple inheritance)
     */
    public String getObjectClass() {
        return objectClass;
    }
    
    /**
     * @see com.top_logic.dob.simple.AttributesDataObject#isInstanceOf(java.lang.String)
     */
    @Override
	public boolean isInstanceOf(String metaObject) {
        return this.tTable().isSubtypeOf(metaObject);
    }
    
    /**
     * @see com.top_logic.dob.simple.AttributesDataObject#isInstanceOf(com.top_logic.dob.MetaObject)
     */
    @Override
	public boolean isInstanceOf(MetaObject metaObject) {
        return this.tTable().isSubtypeOf(metaObject);
    }
    
    /**
     * the mapping to be used to translate top-logic attribute names to external names
     * for this object
     */
    public DeviceMapping getTypeSpecificMapping() {
        String         metaObjectName   = this.getMetaObjectName();
        List           theObjectClasses = LDAPMetaObject.getClasses(metaObjectName, false);
        DeviceMapping  dm               = this.myDevice.getMapping(theObjectClasses);
        if (dm.isEmpty()) {
            // Log warning only once per unmappedClass
            if (unmappedClasses == null) {
                unmappedClasses = new HashSet<>();
            }
            if (!unmappedClasses.contains(metaObjectName)) {
                unmappedClasses.add(metaObjectName); 
                Logger.warn("Using generic Mapping as default for objClass '" + metaObjectName + "'", this);
            }
            
            dm = this.genericMapping;
        }
        return dm;
    }
}