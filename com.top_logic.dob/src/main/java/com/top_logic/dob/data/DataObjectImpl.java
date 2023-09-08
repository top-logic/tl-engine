/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.ObjectContext;

/**
 * The implementation of the DataObject. This one works with an internal array of
 * values. This array corosponse to the array in the MetaObjectImpl.
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class DataObjectImpl extends AbstractDataObject implements ObjectContext {

	/** This object does the actual Attribute-Value mapping. */
	protected final Object[] storage;

	/** 
     * Create a Dataobject for the given Meta Object.
	 */
	public DataObjectImpl(MetaObject anObject) {
		this(anObject, newStorage(anObject));
	}

	/** 
     * Create a {@link DataObjectImpl} for the given {@link MetaObject} and storage.
	 */
	public DataObjectImpl (MetaObject anObject, Object[] storage) {
		super(anObject);
		this.storage = storage;
	}

	@Override
	public Object getValue(MOAttribute attribute) {
		return attribute.getStorage().getApplicationValue(attribute, this, this, storage);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		AttributeStorage attributeStorage = attribute.getStorage();
		attributeStorage.checkAttributeValue(attribute, this, newValue);
		return attributeStorage.setApplicationValue(attribute, this, this, storage, newValue);
	}

    /**
     * Returns a String usefull for debuggin Dataobjects.
     *
     * @return    The string representation.
     */
    @Override
	public String toString () {
    	return internalToString(getIdentifier());
    }

	protected final String internalToString(Object objectName) {
		StringBuffer buf = new StringBuffer(64);
		buf.append(tTable().getName());
		buf.append("(");
		buf.append(objectName);
		buf.append(')');
        return buf.toString ();
	}


	/**
	 * Creates an arbitrary string representation of this object.
	 */
	public static String dump(DataObject dataObjectImpl) {
		StringBuilder buf = new StringBuilder();
		
		buf.append ("{  Type : ");
		buf.append (dataObjectImpl.tTable ().getName ());
		buf.append (' ');
		buf.append (dataObjectImpl.getIdentifier());
		buf.append ('\n');
		
		try {
			for (MOAttribute moa : dataObjectImpl.getAttributes()) {
				String attrName = moa.getName();
		        Object      obj      = dataObjectImpl.getAttributeValue (attrName);
		        buf.append ("   ");
		        buf.append (attrName);
		        buf.append (" : ");
		        DataObjectImpl.appendValue(buf, obj);
		        buf.append ('\n');
		    }
		}
		catch (Exception ex) {
		    buf.append (" *** Exception : ");
		    buf.append (ex.getMessage());
		}
		
		buf.append ('}');
		return buf.toString ();
	}

	private static void appendValue(StringBuilder buffer, Object value) {
		if (value instanceof DataObject) {
			DataObject dataObject = (DataObject) value;
			buffer.append(dataObject.tTable().getName());
		} else {
			buffer.append(value);
		}
	}

    /**
     *  Returns the storage, use with care. 
     */
	public final Object[] getStorage() {
		return this.storage;
	}
	
	@Override
	public ObjectKey getKnownKey(ObjectKey key) {
		return key;
	}

}
