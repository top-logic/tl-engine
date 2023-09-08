/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;

/** A DataObject wrapping a Bean.
 *
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class BeanDataObject implements DataObject {

	/** The bean actually Wrapped */
	Object 			bean;
	
	/** The MetaObject for the Bean */
	BeanMetaObject	meta;
	
	/** used to uniquly indentify this object */
	TLID id;
    
    
    /** 
     * Construct a new BeanDataObject.
     * 
     * Intended to be used by Subclasses.
     */
    protected BeanDataObject() throws IntrospectionException {
        // nothing is done here
    }    
	
	/** Construct a new BeanDataObject for a given Bean */
	public BeanDataObject(Object aBean) throws IntrospectionException {
		Class<?> theClass = aBean.getClass();

		bean = aBean;
		meta = BeanMORepository.getInstance().getMetaObject(theClass);
		id = DummyIDFactory.createId();
	}

    /** Construct a new BeanDataObject for a given Bean */
    public BeanDataObject(Object aBean, Class<?> aClass) throws IntrospectionException {
        Class<?> theClass = aBean.getClass();
        if (! aClass.isAssignableFrom(theClass))
            throw new IntrospectionException(
                "Cannot create a BeanDataObject for " + theClass 
              + "based on " + aClass);

        bean = aBean;
        meta = BeanMORepository.getInstance().getMetaObject(aClass);
		id = DummyIDFactory.createId();
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
	 * @see com.top_logic.dob.DataObject#tTable()
	 */
	@Override
	public MetaObject tTable() {
		return meta;
	}

	/**
	 * @see com.top_logic.dob.DataObject#isInstanceOf(MetaObject)
	 */
	@Override
	public boolean isInstanceOf(MetaObject aMetaObject) {
		return MetaObjectUtils.isClass(aMetaObject)
		    && meta.isInherited((MOClass) aMetaObject);
	}

	/**
	 * @see com.top_logic.dob.DataObject#isInstanceOf(String)
	 */
	@Override
	public boolean isInstanceOf(String aMetaObject) {
		if (meta.getName().equals(aMetaObject)) {
			return true;	// try shortcut first
        }
		try {
			return 	isInstanceOf(BeanMORepository.getInstance()
							.getMetaObject(aMetaObject));
		} catch (UnknownTypeException e) {
            Logger.debug ("Problem when checking " + aMetaObject , e, this);
		}
		return false;
	}

	/**
	 * @see com.top_logic.dob.DataObject#getIdentifier()
	 */
	@Override
	public TLID getIdentifier() {
		return id;
	}

	/** Not supported since identifier is created in CTor.
	 */
	@Override
	public void setIdentifier(TLID anIdentifier) {
        // not supported 
	}

	@Override
	public Iterable<? extends MOAttribute> getAttributes() {
		return meta.getAttributes();
	}

	/**
	 * @see com.top_logic.dob.DataObject#getAttributeNames()
	 */
	@Override
	public String[] getAttributeNames() {
		return meta.getAttributeNames();
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return meta.hasAttribute(attributeName);
	}

	@Override
	public Object getAttributeValue(String attrName)
		throws NoSuchAttributeException {
		PropertyDescriptor desc = meta.getDescriptor(attrName);
		MOAttribute        attr = meta.getAttribute(attrName);
		Method reader = desc.getReadMethod();
		if (reader == null) {
			throw new NoSuchAttributeException("Cannot read Attribute '" 
                                    + attrName + "'");
        }
		try {
			Object result = reader.invoke(bean, (Object[]) null);
			if (attr.getMetaObject() instanceof BeanMetaObject) {
				return new BeanDataObject(result);
            }
			// else
			return result;
		} catch (Exception e) {
			throw new NoSuchAttributeException(e);
		}
	}

	@Override
	public Object setAttributeValue(String attrName, Object value)
		throws IncompatibleTypeException, NoSuchAttributeException {
		
		Object oldValue = getAttributeValue(attrName);
		
		PropertyDescriptor desc = meta.getDescriptor(attrName);
		Method writer = desc.getWriteMethod();
		if (writer == null) {
			throw new NoSuchAttributeException("Cannot write Attribute '" +
                        attrName + "'");
        }
		try {
			if (value instanceof BeanDataObject) {
                writer.invoke(bean, new Object[] { ((BeanDataObject) value).bean });
            }
			writer.invoke(bean, new Object[] { value });
		} catch (Exception e) {
			throw new IncompatibleTypeException(e);
		}
		
		return oldValue;
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
