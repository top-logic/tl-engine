/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Extracts and eventually caches MetaData for an Attributes Object.
 * 
 * This class <em>can</em> be used by the AttributesDataObject
 * buts it use should best be avoided since its implementaion
 * may be to simple.
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class AttributesMetaObject implements MOStructure {

	/** The Attributes we are based on. */
	private Attributes attrs;
	private String definingResource;
	
	/** 
	 * Construct a new AttributesMetaObject for the given Attributes 
	 */
	public AttributesMetaObject(Attributes anAttrs) {
		attrs = anAttrs;
	}
	
	@Override
	public Kind getKind() {
		return Kind.struct;
	}
	
	@Override
	public String getName() {
		return this.attrs.toString();
	}

    @Override
	public String getDefiningResource() {
    	return this.definingResource;
    }
    
    @Override
	public void setDefiningResource(String definingResource) {
    	this.definingResource = definingResource;
    }

	/**
	 * This will only work for primitive types.
	 */
	@Override
	public MOAttribute getAttribute(String attrName) throws NoSuchAttributeException {
		Attribute attr = attrs.get(attrName);

		if (attr == null) {
			throw new NoSuchAttributeException(attrName);
        }

		try {
			Class      classType = attr.get().getClass();
			MetaObject type      = MOPrimitive.getPrimitive(classType);

            if (type == null) {
				throw new NoSuchAttributeException("Cant getAttribute() for " + classType);
            }

			return new MOAttributeImpl(attrName, type);
		} 
        catch (NamingException ex) {
			throw new NoSuchAttributeException(ex);
		}
	}
	
	@Override
	public MOAttribute getAttributeOrNull(String name) {
		try {
			return getAttribute(name);
		} catch (NoSuchAttributeException ex) {
			return null;
		}
	}
	
	@Override
	public MOAttribute getDeclaredAttributeOrNull(String name) {
		return getAttributeOrNull(name);
	}
	
	@Override
	public List<MOAttribute> getDeclaredAttributes() {
		return getAttributes();
	}

	@Override
	public boolean hasAttribute(String attrName) {
		return null != attrs.get(attrName);
	}
	
	@Override
	public boolean hasDeclaredAttribute(String attrName) {
		return hasAttribute(attrName);
	}

	@Override
	public String[] getAttributeNames() {
		ArrayList   theList = new ArrayList(attrs.size());
		Enumeration theEnum = attrs.getIDs();

		while (theEnum.hasMoreElements()) {
			theList.add(theEnum.nextElement());
        }

        return (String[]) theList.toArray(new String[theList.size()]);
	}

    /**
     * Return the list of all attributes defined within this instance.
     * 
     * The return value must not be <code>null</code>, but can be empty. If
     * the returned list contains elements, all of them are of 
     * type {@link com.top_logic.dob.MOAttribute}.
     * 
     * @return    The list of held attributes.
     * @see       com.top_logic.dob.MOAttribute
     */
    @Override
	public List<MOAttribute> getAttributes () {
        ArrayList<MOAttribute> attributes = new ArrayList<>(attrs.size());
        Enumeration<String> theEnum = attrs.getIDs();

        while (theEnum.hasMoreElements()) {
            String theName = theEnum.nextElement();
            MOAttribute attribute;
            try {
				attribute = MetaObjectUtils.getAttribute(this, theName);
            }
            catch (NoSuchAttributeException ex) {
                Logger.error("Inconsistancy in getAttributes()", ex, this);
                continue;
            }
            attributes.add(attribute);
        }

        return attributes;
    }
    
    @Override
	public List<MOReference> getReferenceAttributes() {
        ArrayList<MOReference> attributes = new ArrayList<>(attrs.size());
        Enumeration<String> theEnum = attrs.getIDs();

        while (theEnum.hasMoreElements()) {
            String theName = theEnum.nextElement();
            MOAttribute attribute;
            try {
				attribute = MetaObjectUtils.getAttribute(this, theName);
            }
            catch (NoSuchAttributeException ex) {
                Logger.error("Inconsistancy in getAttributes()", ex, this);
                continue;
            }
            if (attribute instanceof MOReference) {
            	attributes.add((MOReference) attribute);
            }
        }

        return attributes;
        }

    /**
     * Checks if this object is an instance of the given meta object
     * 
     * @param   anObject    The meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          is same.
     */
    @Override
	public boolean isSubtypeOf(MetaObject anObject) {
        return (this.equals(anObject));
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
        return (this.getName().equals(aName));
    }

	@Override
	public void freeze() {
		// Already immutable.
	}
	
	@Override
	public boolean isFrozen() {
		// Already immutable.
		return true;
	}

	@Override
	public void addAttribute(MOAttribute anAttr) throws DuplicateAttributeException {
		throw new UnsupportedOperationException("Immutable type.");
	}

	@Override
	public int getCacheSize() {
		return attrs.size();
	}

	@Override
	public List<MOIndex> getIndexes() {
		return Collections.emptyList();
	}

	@Override
	public MOIndex getPrimaryKey() {
		return null;
	}

	@Override
	public MetaObject copy() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBTableMetaObject getDBMapping() {
		throw new UnsupportedOperationException();
	}
}
