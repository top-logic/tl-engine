/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * A Wrapper for a {@link javax.naming.directory.Attributes} Object.
 * 
 * This class is needed in TopLog for the LDAP implemantion but may
 * be used in other, jndi related contexts, too.
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class AttributesDataObject implements DataObject {

	/** The Attributes object actually wrapped */
	protected Attributes 			attrs;

	/** Since Attributes are dynamic every AttributeDataObject has its own MetaObject */	
	private AttributesMetaObject 	meta;

	/** 
	 * Construct a new AttributeDataObject wrapping the given Attributes. 
	 */
	public AttributesDataObject(Attributes anAttrs)
	{
		attrs = anAttrs;	
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
	 * Since Attributes are dynamic every AttributeDataObject has its own MetaObject
	 */
	@Override
	public MetaObject tTable() {
		if (meta == null) {
			meta = this.createMetaObject();
		}
		return meta;
	}

	protected AttributesMetaObject createMetaObject() {
		return new AttributesMetaObject(attrs);
	}

	/**
	 * @see com.top_logic.dob.DataObject#isInstanceOf(MetaObject)
	 */
	@Override
	public boolean isInstanceOf(MetaObject aMetaObject) {
		return meta == tTable();
	}

	/**
	 * Always false, this is not actually supported.
	 */
	@Override
	public boolean isInstanceOf(String aMetaObject) {
		// avoid actual creation of AttributesMetaObject
		return aMetaObject.equals(attrs.toString());
	}

	@Override
	public TLID getIdentifier() {
		return StringID.valueOf(attrs.toString());
	}

	/**
	 * Not suppported
	 * 
	 * @param anIdentifier is ignored
	 */
	@Override
	public void setIdentifier(TLID anIdentifier) {
	    // not supported
	}

    /**
     *  if you really must use this function you should optimize it.
     */
    @Override
	public Iterable<? extends MOAttribute> getAttributes() {
        this.tTable();

        ArrayList<MOAttribute>   theList = new ArrayList<>();
        Enumeration<String> theEnum = attrs.getIDs();

        try {
            while (theEnum.hasMoreElements()) {
                theList.add(MetaObjectUtils.getAttribute(this.meta, theEnum.nextElement()));
            }
        } 
        catch (NoSuchAttributeException nsae) {
            Logger.error("getAttributes() failed", nsae, this);
        }

        return theList;
    }

	/**
	 *  if you really must use this function you should optimize it.
	 */
	@Override
	public String[] getAttributeNames() {
		return MetaObjectUtils.getAttributeNames(tTable());
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return MetaObjectUtils.hasAttribute(tTable(), attributeName);
	}

	@Override
	public Object getAttributeValue(String attrName)
		throws NoSuchAttributeException {
		try {
			return attrs.get(attrName).get();
		} catch (NamingException e) {
			return new NoSuchAttributeException(e);
		}
	}

	@Override
	public Object setAttributeValue(String attrName, Object value)
		throws IncompatibleTypeException, NoSuchAttributeException {
		return attrs.put(attrName, value);
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

}
