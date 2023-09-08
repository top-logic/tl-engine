/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;
 
import java.io.Serializable;

/**
 * This class provides functionality for the specification of a service (by 
 * namespace and name or wildcard) by either sending or receiving classes. 
 * Additionally, the equals() and hashcode() methods in java.lang.Object are 
 * overwritten to enable comparison of services.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Service implements Serializable {

    /** The wildcard to access any kind of information of one type. */
    public static final String WILDCARD = "*";

    /** The name of the Service (e.g. "WDR"). */
    private String name; 

    /** The namespace of the Service (e.g. "Fernsehen"). */
    private String nameSpace; 

    /**
     * Creates an instance of Service and instantiates it with String values
     * (e.g. "Fernsehen", "WDR").
     * 
     */
    public Service (String aNameSpace, String aName) {
        this.setName (aName);
        this.setNameSpace (aNameSpace);
    }

    /**
     * Delivers the services´ namespace.
     *
     * @return    The namespace of the sender.
     */
    public String getNameSpace () {
        return this.nameSpace;
    }

    /**
     * Delivers the services´ name. 
     * 
     * @return   The name of the sender.
     */
    public String getName () {                                      
        return this.name;
    }

    /**
    * Returns a String representation of this EventObject.
    *
    * @return  A String representation of this EventObject.
    */
    @Override
	public String toString () {
        return this.getClass () + " ["
            + "namespace: " + this.nameSpace
            + ", name: "     + this.name
            + ']';
    }

    /**
     *  Indicates wether some other object is "equal to" this one.
     *
     * @param  anObject   The reference object with which to compare.
     * @return            true, if this object is the same as the anObject 
     *                    argument,false otherwise.
     */
    @Override
	public boolean equals (Object anObject) {
		if (anObject == this) {
			return true;
		}
        if (anObject instanceof Service) {
            Service other = (Service) anObject;
            String mySpace    = this.getNameSpace ();
            String otherSpace = other.getNameSpace ();
            if (!mySpace.equals (otherSpace)) {
               return  WILDCARD.equals(mySpace)
                    || WILDCARD.equals(otherSpace);
            }
            String myName    = this.getName ();
            String otherName = other.getName ();
            return myName.equals (otherName)
                || WILDCARD.equals(myName)
                || WILDCARD.equals(otherName);
        }
        return false;
   }

    /**
     * Returns a hash code value for the object (the same int value for each 
     * object that is returned true by the equals method).
     *
     * @return  A hash code value for the object.
     */
    @Override
	public int hashCode () {
        return nameSpace.hashCode() ^ name.hashCode();
    }

    protected void setNameSpace (String aNameSpace) {
        this.nameSpace = aNameSpace;
    }

    protected void setName (String aName) {
        this.name = aName;
    }
}   
