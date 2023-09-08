
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;


import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;


/**
 * Abstract implementation of the DataObject, which provides some basic
 * functionalities of the DataObject. Implementing classes should use this
 * class as parent to provide problems when the interface will be changed.
 *
 *									field regardless of existing
 *									value
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class AbstractDataObject implements DataObject {

    /** The reference to the meta description of this object. */
	private MetaObject metaObject;

    /**
     * Create a new instance of this data object from the given type.
     *
     * @param    anObject    The type of this data object.
     * @throws   IllegalArgumentException    If the given object is 
     *                                       <code>null</code>.
     */
    protected AbstractDataObject(MetaObject anObject) {
        if (anObject == null) {
            throw new IllegalArgumentException("Given meta object is null");
        }

        this.metaObject = anObject;
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
     * @return     com.top_logic.mig.dataobjects.meta.MetaObject
     */
    @Override
	public MetaObject tTable () {
        return (this.metaObject);
    }

    /**
     * Checks if this object is an instance of the given meta object
     *
     * This method can only work with MOClass, because this implement the
     * MOClass.isInherited() 
     * method, if the given object is no MOClass, this method can only
     * test on one level.
     * 
     * @param   anObject    The meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          is same.
     */
    @Override
	public boolean isInstanceOf(MetaObject anObject) {
        return (this.tTable().isSubtypeOf(anObject));
    }

    /**
     * Checks if this object is an instance of the given name of a meta object.
     *
     * This method can only work with MOClass, because this implement the
     * MOClass.isInherited() 
     * method, if this meta object is no MOClass, this method can only
     * test on one level.
     *
     * @param   aName    The name of the meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          has the same name than the given one.
     */
    @Override
	public boolean isInstanceOf (String aName) {
        return (this.tTable().isSubtypeOf(aName));
    }

    @Override
	public Iterable<? extends MOAttribute> getAttributes () {
		return MetaObjectUtils.getAttributes(this.tTable());
    }

    /**
     * Returns the list of attribute names defined in the MetaObject.
     *
     * @return   The list of known attributes.
     */
    @Override
	public String [] getAttributeNames () {
		return MetaObjectUtils.getAttributeNames(this.tTable());
    }

	@Override
	public boolean hasAttribute(String attributeName) {
		return MetaObjectUtils.hasAttribute(tTable(), attributeName);
	}

	@Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
		return getValue(MetaObjectUtils.getAttribute(tTable(), attrName));
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		return setValue(MetaObjectUtils.getAttribute(tTable(), attrName), value);
	}

	@Override
	public ObjectKey getReferencedKey(MOReference reference) {
		return getReferencedKey(this, reference);
	}

	/**
	 * Returns the {@link ObjectKey} of the value of the given {@link MOReference} in the given
	 * {@link DataObject}.
	 * 
	 * @return Gets the value of the given {@link MOReference} in the given {@link DataObject} via
	 *         {@link DataObject#getValue(MOAttribute)} and returns
	 *         {@link IdentifiedObject#tId() the object key} of the value.
	 */
	public static ObjectKey getReferencedKey(DataObject dataObject, MOReference reference) {
		IdentifiedObject value = (IdentifiedObject) dataObject.getValue(reference);
		if (value != null) {
			return value.tId();
		}
		return null;
	}

    /**
     * Helper method to access the value of the attribute by its name.
     * 
     * @deprecated this should be the other way around.
     */
    @Deprecated
	public static Object getValue(DataObject dataObject, MOAttribute attribute) {
		try {
			return dataObject.getAttributeValue(attribute.getName());
		} catch (NoSuchAttributeException ex) {
			return null;
		}
	}
    
	/**
	 * Helper method to set the value of the attribute by its name.
	 * 
	 * @deprecated this should be the other way around.
	 */
	@Deprecated
	public static Object setValue(DataObject dataObject, MOAttribute attribute, Object newValue) throws DataObjectException {
		if (!attribute.getOwner().equals(dataObject.tTable())) {
			throw new IllegalArgumentException("MOAttribute '" + attribute + "' does not belong to the " + MetaObject.class.getSimpleName()
					+ " of '" + dataObject+ "'");
		}
		return dataObject.setAttributeValue(attribute.getName(), newValue);
	}

    /**
	 * Creates an storage object with slots for all {@link MOAttribute} in the given type, i.e. the
	 * size of the returned storage is the cache size of the given type.
	 * 
	 * @param type
	 *        The type to create storage object for
	 * 
	 * @see MetaObjectUtils#getCacheSize(MetaObject)
	 */
	protected static Object[] newStorage(MetaObject type) {
		int cacheSize = MetaObjectUtils.getCacheSize(type);
		Object[] newStorage;
		if (cacheSize == 0) {
			newStorage = ArrayUtil.EMPTY_ARRAY;
		} else {
			newStorage = new Object[cacheSize];
		}
		return newStorage;
	}

	/** Use identifier to define equality. */
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

    /** Relay hashCode to identifier */
    @Override
	public int hashCode() {
		TLID id = this.getIdentifier();
        return (id == null) ? super.hashCode() 
                            : id.hashCode();
    }

    /** return some reasonable output for debugging. */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                        this.toStringValues() +
                        ']');
    }

    /** 
     * Return the values to be displayed in the {@link #toString()} method. 
     * 
     * @return    The values to be displayed.
     */
    protected String toStringValues() {
        return "ID: " + this.getIdentifier() + 
               ", type: " + this.metaObject.getName();
    }

}
