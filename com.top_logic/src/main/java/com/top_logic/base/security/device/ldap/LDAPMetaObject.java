/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.ldap;

import java.util.Collections;
import java.util.List;

import javax.naming.directory.Attributes;

import com.top_logic.basic.StringServices;
import com.top_logic.dob.simple.AttributesMetaObject;

/**
 * Extends the AttributesMetaObject with LDAP-specific class handling.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch </a>
 */
public class LDAPMetaObject extends AttributesMetaObject {

	String name;
	
	public LDAPMetaObject(Attributes anAttrs, String aName) {
		super(anAttrs);
		
		this.name = aName;
	}
	
	/**
	 * @see com.top_logic.dob.simple.AttributesMetaObject#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

    /**
     * Super class check
     * 
     * @param aClass				a classes string (classes separated by '.')
     * @param aPotentialSuperClass	a potential super classes string
     * @return true if all classes of aClass are found in aPotentialSuperClass
     */
    public static boolean isInstanceOf(String aClass, String aPotentialSuperClass) {
    	List theClasses      = getClasses(aClass, false);
    	List theSuperClasses = getClasses(aPotentialSuperClass, false);
    	return theClasses.containsAll(theSuperClasses);
    }
    
    /**
     * Get the classes from a classes string (classes separated by '.')
     * 
     * @param aClass	the classes
     * @param sorted	if true sort the classes by name
     * @return the list of class names. Never <code>null</code>
     */
    public static List getClasses(String aClass, boolean sorted) {
    	List theList = StringServices.toNonNullList(aClass, '.');
    	if (sorted) {
    		Collections.sort(theList);
    	}
    	
    	return theList;
    }

    /**
     * Checks if this object is an instance of the given name of a meta object.
     *
     * @param   aName    The name of the meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          has the same name than the given one.
     */
    @Override
	public boolean isSubtypeOf (String aName) {
        return isInstanceOf(this.getName(), aName);
    }

    @Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
    	if (!(obj instanceof AttributesMetaObject)) {
    		return false;
    	}
    	
    	return getClasses(this.getName(), true).equals(getClasses(((AttributesMetaObject) obj).getName(), true));
    }
    
    /**
     * Must override this because equals is overridden
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
    	return getClasses(this.getName(), true).hashCode();
    }
}
