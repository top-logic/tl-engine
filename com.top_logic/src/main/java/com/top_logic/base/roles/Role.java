/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.roles;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import com.top_logic.basic.Logger;


/**
 * A class for User Roles. A role contains two attributes, name and description.
 *
 * @author  Mathias Maul
 */
public class Role implements Cloneable, Comparable {

	/** The (attribute name for the) name of the Role. */
	private static final String ROLENAME	= "rolename";

	/** The (attribute name for the) name of the Role. */
	private static final String ROLEDESCRIPTION	= "roledescription";

    /** The (javax.naming) attributes of the Role. */
    private Attributes attributes;

	/**
	 * Default constructor, initializing name and description with empty strings.
	 */
	public Role() {
        this("", "");
	}

	/**
	 * Constructor with specified values for name and description.
	 *
	 * @param	aName	The name of the role. Should be unique for the 
     *                  currently running system. Must not contain "," 
     *                  (this is checked when the user adds a new role 
     *                  to the system).
	 * @param	aDesc	A description for the role. 
	 */
	public Role(String aName, String aDesc) {
		this.attributes = new BasicAttributes();

        this.attributes.put(ROLENAME, aName);
        this.attributes.put(ROLEDESCRIPTION, aDesc);
	}

	/**
	 * Constructor using attributes.
	 *
	 * @param	anAttr    				The attributes for the role.
	 * @throws	NullPointerException	If the given parameter is null.
	 */
	public Role (Attributes anAttr) throws NullPointerException {
		if (anAttr == null) {
			throw new NullPointerException ("Parameter may not be null.");
		}

		this.attributes = anAttr;
	}

    /**
     * Returns a string representation of this object.
     */
    @Override
	public String toString() {
        return "[name: '" + this.getName() + "', description: '" + 
                            this.getDescription() + "']";
    }

    /**
     * Returns true if the names of two Role objects are equal, otherwise false.
     */
    @Override
	public boolean equals(Object anObject) {
		if (anObject == this) {
			return true;
		}
        if (!(anObject instanceof Role)) {
            return false;
        } 

        return ((this.getName()).equals(((Role) anObject).getName()));  
    }
    
    /**
     * Generates a hash code based on the name of the Role.
     */
    @Override
	public int hashCode() {
        return (this.getName().hashCode());
    }
    

    /**
     * Returns a clone of this Role.
     */
    @Override
	public Object clone() {
        Role theNew = new Role();

        theNew.setName(this.getName());
        theNew.setDescription(this.getDescription());

        return (theNew);
    }

    /**
     * Implements a compare method to satisfy the Comparable interface.
     */
    @Override
	public int compareTo(Object anObject) {
        if (anObject instanceof Role) {
            return (this.getName()).compareTo(((Role) anObject).getName());
        }
        else {
            String theMessage;

            if (anObject != null) {
                theMessage = anObject.getClass().getName();
            }
            else {
                theMessage = "given object is null!";
            }

            throw new ClassCastException(theMessage);
        } 
    }

	/**
	 * Sets the name of the role.
	 *
	 * @param	aName	The name of the role.
	 */
	public void setName(String aName) {
        this.setAttributeValue (ROLENAME, aName);
	}

	/**
	 * Sets the description of the role.
	 *
	 * @param	aDesc	The description of the role. Should not contain ","
	 */
	public void setDescription(String aDesc) {
        this.setAttributeValue (ROLEDESCRIPTION, aDesc);
	}

	/**
	 * Returns the role's name.
	 *
	 * @return	The name of the role.
	 */
	public String getName() {
		return ((String) this.getAttribute(ROLENAME));
	}

	/**
	 * Returns the role's description.
	 *
	 * @return	The description of the role.
	 */
	public String getDescription() {
		return ((String) this.getAttribute(ROLEDESCRIPTION));
	}

    /**
     * Returns the requested attribute value.
     *
     * @param    aKey The name of the attribute.
     * @return   The requested value or null if the attribute value cannot be read.
     */
    protected Object getAttribute(String aKey) {
        try {
            return (this.attributes.get(aKey).get());
        } 
        catch (NamingException ex) {
            Logger.warn ("Unable to read attribute \"" + aKey + "\" from " +
                         this.attributes + ", reason is: " + ex, this);

            return null;
        }
        catch (NullPointerException ex) {
            Logger.warn ("Unable to read attribute \"" + aKey + "\" from " +
                         this.attributes + ", reason is: " + ex, this);

            return null;
        }
    }

    /**
     * Sets the attribute value of the given name to the given value.
     *
     * @param    aKey        The name of the attribute.
     * @param    anObject    The value to be set.
     */
    public void setAttributeValue(String aKey, Object anObject) {
        this.attributes.put (aKey, anObject);
    }
}
