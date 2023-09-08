/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOCollection;

/** 
 * Abstract class to fill a DataObject from Strings.
 * 
 * This means setting the various attributes of a dataobject (using its
 * metaobject) to the given values as strings. The most important functionality
 * is to add the right type of value according to the attribute type
 * 
 * @author   Jamel Zakraoui
 */
public abstract class DataObjectFiller  {

    /** 
     * Override to do return the Input Data as Strings.
	 *
	 * E.G. form a HashMap or an HTTPRequest, see Subclasses
     * this is the only method that is overwritten in subclasses
     * 
     * @param  attr the name of the Attribute
     * @return The Object for the given name, null will be ignored.
     */
    public abstract Object getStringValue(String attr) throws NoSuchAttributeException;
    
	/** Set the (Object) value of the DataObject from a String.
	 * The String will be converted by using the attr-type.
	 * @param dataObject the data object to be filled with attribute/values
	 */
    public DataObject fill(DataObject dataObject) throws DataObjectException
    {
		MetaObject meta    = dataObject.tTable();
		String     attrs[] = dataObject.getAttributeNames();
		int        size     = attrs.length;
		for (int i=0; i < size; i++) 
		{
			String attName   = attrs[i];
			Object value     = getStringValue(attName);
			if (value != null)
			{
			  	fillAttr(dataObject, MetaObjectUtils.getAttribute(meta, attName), value);
			}
			
		}
		//return the dataobject filled with the attribute/value pairs
		return dataObject;
    }
    
	/** 
     * Set the (Object) value of the DataObject from a String.
	 *
	 * The String will be converted by using the attr-type.
     * 
	 * @param dataObj  the data object to be filled with attribute/values
	 * @param attr     the attribute to be added
	 * @param sVal     the value of the attribute
	 */
    protected void fillAttr(DataObject dataObj, MOAttribute attr, Object sVal)
	        throws DataObjectException
    {
		String attrName = attr.getName();
       // get an object representing the value of the attribute
		// and set it to the attribute name. If the attribute is 
		// a structure (means another dataobject, get an inner map containing
		// the attribute attributes/values from the filler map and do the same
		// as for a simple attribute (primitive). All this should be recursive.
		if (sVal instanceof String) {
			Object val = convertStringVal(attr, (String) sVal);
			dataObj.setAttributeValue(attrName, val);
		} else if (sVal instanceof Map) {
			DataObject theDo = (DataObject) dataObj.getAttributeValue(attr.getName());
			if (theDo == null) {
				// we need a new one
				MetaObject metaObject = attr.getMetaObject();
				theDo = new DefaultDataObject(metaObject);
			}

			DataObject nextDO = fillFromMap(theDo, (Map<?, ?>) sVal);
			dataObj.setAttributeValue(attrName, nextDO);
		} else if (sVal instanceof Collection) {
			DOList doList = (DOList) dataObj.getAttributeValue(attr.getName());
			if (doList == null) {
				// this happens for deep nested structures
				MetaObject metaObject = attr.getMetaObject();
				if (metaObject instanceof MOCollection) {
					doList = new DOList((MOCollection) metaObject);
				}
				else {
					throw new RuntimeException(
						"List needs meta object of list type: attribute: " + attrName + " meta object: "
						+ metaObject);
				}
			}
			DOList nextDO = fillFromList(doList, (Collection<?>) sVal);
			dataObj.setAttributeValue(attrName, nextDO);
		} else {
			// ignore !?!
		}
    }
	
	
	/**
	 * Expands the given {@link DOList} by creating new {@link DataObject} for each item in the
	 * given {@link Collection} and adding the item to the {@link DOList}.
	 * 
	 * @return The given {@link DOList}
	 */
	protected DOList fillFromList(DOList doList, Collection<?> sVal) throws DataObjectException {
		MetaObject elementType = ((MOCollection) doList.tTable()).getElementType();

		for (Object obj : sVal) {
			// create new DataObject for elementType with values from obj
			DataObject newObject = createNewDo(elementType, obj);
			doList.add(newObject);
		}
		return doList;
	}

	private DataObject createNewDo(MetaObject elementType, Object obj) throws DataObjectException {
		DataObject dataObject;
		if (elementType instanceof MOCollection) {
			String rawType = ((MOCollection) elementType).getRawType();
			if (MOCollection.LIST.equals(rawType) || MOCollection.COLLECTION.equals(rawType)) {
				dataObject = new DOList((MOCollection) elementType);
			} else {
				throw new UnsupportedOperationException("Can not create DataObject for type '" + elementType
					+ "' of raw type '" + rawType + "'");
			}
		} else {
			dataObject = new DefaultDataObject(elementType);
		}

		// Here is no attribute. Therefore fillAttr() can not be used.
		if (obj instanceof Map) {
			fillFromMap(dataObject, (Map<?, ?>) obj);
		}
		if (obj instanceof Collection) {
			fillFromList((DOList) dataObject, (Collection<?>) obj);
		}
		return dataObject;
	}

	/**
	 * a filled dataobject that is the value of an attribute this is used for structured
	 *         dataobjects, that means those who's attributes are not only primitives, but
	 *         structures!
	 */
	protected DataObject fillFromMap(DataObject dobj, Map<?, ?> anInnerMap) throws DataObjectException
	{
	
		MetaObject meobj = dobj.tTable();
		String objAttrs[] = MetaObjectUtils.getAttributeNames(meobj);
		if (objAttrs == null) {
			return dobj;
		}

		int size = objAttrs.length;
		for (int i=0; i < size; i++) 
		{
			String attName = objAttrs[i];
			Object aValue = anInnerMap.get(attName);
			if (aValue != null)
			{ 
				fillAttr(dobj, MetaObjectUtils.getAttribute(meobj, attName), aValue);
			}
		}
		//return the dataobject filled with the attribute/value pairs
		return dobj;
	}
	
	
	/**
	 * Returns a java primitive instance according to the Type specified
	 * in the MOAttribute attr from the String value
	 * @param attr the attribute to be added 
	 * @param sVal the attribute to be added
	 */
	private Object convertStringVal(MOAttribute attr, String sVal)
            throws NoSuchAttributeException, IncompatibleTypeException {
        
        MOPrimitive moprimitive = (MOPrimitive)  attr.getMetaObject();
        if (moprimitive == MOPrimitive.STRING) {
            return sVal;
        }
        try {
            if (moprimitive == MOPrimitive.INTEGER) {
				return Integer.valueOf(sVal);
            }
            if (moprimitive == MOPrimitive.DOUBLE) {
				return Double.valueOf(sVal);
            }
            if (moprimitive == MOPrimitive.LONG) {
				return Long.valueOf(sVal);
            }
            if (moprimitive == MOPrimitive.SHORT) {
				return Short.valueOf(sVal);
            }
            if (moprimitive == MOPrimitive.FLOAT) {
				return Float.valueOf(sVal);
            }
            if (moprimitive == MOPrimitive.BYTE) {
				return Byte.valueOf(sVal);
            }
        } catch (NumberFormatException exp) {
            throw new IncompatibleTypeException("Argument:  " + sVal
                    + "can not be converted to " + moprimitive.getName());
        }

        if (moprimitive == MOPrimitive.DATE) {
			// the date must have the Format: dd.MM.yyyy
            if (sVal.length() > 0) 
              try {
                return getDateFormat().parse(sVal);
            } catch (ParseException exp) {
                throw new IncompatibleTypeException(exp);
            } else {
                return null;
            }
        }

        if (moprimitive == MOPrimitive.SQL_DATE) {
            try {
                return java.sql.Date.valueOf(sVal);

            } catch (java.lang.IllegalArgumentException exp) {
                throw new IncompatibleTypeException("given argument: " + sVal
                        + " is not an SQL_Date");
            }
        }

        if (moprimitive == MOPrimitive.BOOLEAN) {
            return Boolean.valueOf(StringServices.parseBoolean(sVal));
        }

        throw new NoSuchAttributeException("Dont know how to Map '" + sVal
                + "' to " + moprimitive.getName());
    }

	public DateFormat getDateFormat() {
		return CalendarUtil.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
    }
	
}
