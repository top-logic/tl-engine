/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOReference;

/**
 * {@link DataObject} for easily building structures programatically.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicDataObject implements DataObject {

	private final MOClassImpl type;
	private final Map<MOAttribute, Object> data;

	protected DynamicDataObject() {
		data = new HashMap<>();
		type = new MOClassImpl("<dynamic>");
	}

	@Override
	public TLID getIdentifier() {
		return null;
	}

	@Override
	public void setIdentifier(TLID anIdentifier) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines or re-defines an attribute in this object.
	 * 
	 * @param attributeType
	 *        The type of the new attribute.
	 * @param name
	 *        The name of the attribute.
	 * @param value
	 *        The value of the attribute.
	 * @return The representation of the new attribute.
	 */
	public MOAttribute defineAttribute(MetaObject attributeType, String name, Object value) throws DataObjectException {
		MOClassImpl clazz = (MOClassImpl) tTable();
		
		try {
			MOAttribute oldAttribute = clazz.getAttribute(name);
			setValue(oldAttribute, null);
			clazz.removeAttribute(name);
		} catch (NoSuchAttributeException ex) {
			// Ignored.
		}
		
		MOAttributeImpl attribute = new MOAttributeImpl(name, attributeType);
		try {
			clazz.addAttribute(attribute);
		} catch (DuplicateAttributeException ex) {
			throw new UnreachableAssertion("Was removed before.");
		}
		
		setValue(attribute, value);
		
		return attribute;
	}

	@Override
	public String[] getAttributeNames() {
		return type.getAttributeNames();
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return type.hasAttribute(attributeName);
	}

	@Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
		return data.get(type.getAttribute(attrName));
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		return data.put(type.getAttribute(attrName), value);
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tTable()} instead
	 */
	@Override
	@Deprecated
	public final MOClass getMetaObject() {
		return tTable();
	}

	@Override
	public MOClass tTable() {
		return type;
	}

	@Override
	public boolean isInstanceOf(MetaObject expectedType) {
		return this.type.isSubtypeOf(expectedType);
	}

	@Override
	public boolean isInstanceOf(String typeName) {
		return false;
	}

	@Override
	public Iterable<? extends MOAttribute> getAttributes() {
		return type.getAttributes();
	}

	@Override
	public Object getValue(MOAttribute attribute) {
		return data.get(attribute);
	}

	@Override
	public ObjectKey getReferencedKey(MOReference reference) {
		return AbstractDataObject.getReferencedKey(this, reference);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		if (newValue == null) {
			return data.remove(attribute);
		} else {
			return data.put(attribute, newValue);
		}
	}

}
