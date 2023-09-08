/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;


/** 
 * Create Objects from JDBC-ResultSets via JavaBeans / Reflection.
 *
 * This builder creates a new instance of his
 * template class (handed over during instanciation) and fills this with the
 * values taken from a result set.
 *
 * @author  Michael Gänsler
 */
public class ObjectBuilder {

    /** Map for storing the method calls. */
    private Map map;

    /** The class for the returning instances. */
    private Class      	resultTemplate;
    
    /** Property Descriptions for the template class*/
    PropertyDescriptor	desciptors[];

    /** Constructs a ObjectBuilder for the given Class.
     *
     * @param    aTemplate    The class of the instances to be returned.
     */
    public ObjectBuilder (Class aTemplate) 
    throws Exception {
        this.resultTemplate = aTemplate;
 		this.map 			= new HashMap ();
        BeanInfo info		= Introspector.getBeanInfo(resultTemplate);
        desciptors			= info.getPropertyDescriptors();
     }

    /**
     * Delivers the description of the instance of this Class.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return getClass ().getName () + " [" + "resultTemplate: "
               + this.resultTemplate + "]";
    }

    /**
     * Returns an instance of the predefined class out of the current element
     * of the given result set.
     *
     * @param    aSet    The set holding the current element.
     * @return   The instance of the predefined class with the matching values
     *           out of the result set.
     */
    public Object buildObject (ResultSet aSet) {
        Object theObject = null;

        try {
        	ResultSetMetaData meta = aSet.getMetaData ();
            int theLength = meta.getColumnCount ();

            theObject = this.createInstance ();

            for (int thePos = 0; thePos < theLength; thePos++) {
                try {
                    this.setAttribute (aSet, meta, theObject, thePos + 1);
                }
                catch (Exception ex) {
                    Logger.info("ObjectBuilder unable to set value" , ex , this);
                }
            }
        }
        catch (Exception ex) {
            Logger.error ("ObjectBuilder unable to build object: ", ex, this);
        }

        return theObject;
    }

    /** Set Attribute of a JavaBean via a ResulSet using the column
     * 
     * @param anIndex	current index in resultset, starting with 1.
     *
     * @throws Exception an evil mix of SQL- and Introspection Exceptions.
     */
    private void setAttribute (	ResultSet aSet	, ResultSetMetaData aMeta, 
    							Object aDest	, int anIndex)
            throws Exception {
        String theColumn = aMeta.getColumnName (anIndex);
        Object theObject = aSet.getObject     (anIndex);

        this.setAttribute (aDest, theColumn, theObject);
    }

    /** Set an Attribute via the JavaBeans Set-accessor.
     *
     * @param aDest		object whos value should be set.
     * @param aName		name of the Property
     * @param aValue	desired value of the property
     *
     * @throws Exception	all sorts of nasty things.
     */
    private void setAttribute (Object aDest, String aName, Object aValue)
            throws Exception {
        Method theMethod = this.getMethod (aDest, aName, aValue);
        Class  param0    = theMethod.getParameterTypes()[0];
        
        // Match java.sql.Date/Time to java.util.Date when needed.
        if (((aValue instanceof Timestamp) 
                 || (aValue instanceof java.sql.Date)
                 || (aValue instanceof java.sql.Time))
            && (param0 == Date.class)) {
            aValue = new Date ((( Date ) aValue).getTime ());
        }
        // Donwncast Numbers when needed (ToDo add more casts here).
        if (aValue instanceof Number) {
            if ((param0 == Float.class || param0 == Float.TYPE) 
                    && !(aValue instanceof Float)) {
				aValue = Float.valueOf(((Number) aValue).floatValue());
            }
            else if ((param0 == Double.class || param0 == Double.TYPE)
                         && !(aValue instanceof Double)) {
				aValue = Double.valueOf(((Number) aValue).doubleValue());
            }
            // is needed in case aValue is a BigDecimal (which is a Number, too)
            else if (param0 == Integer.class || param0 == Integer.TYPE) {
				aValue = Integer.valueOf(((Number) aValue).intValue());
            }
            else if (param0 == Long.class    || param0 == Long.TYPE) {
				aValue = Long.valueOf(((Number) aValue).longValue());
            }
        }

        theMethod.invoke (aDest, new Object []{ aValue });
    }

    /** Return a (cached) Method for a given object and function Name.
     *
     * This will currently not work with polymorphic functions (KHA).
     *
     * @param anObject	The object, is examined via inspection
     * @param aName		Name of the fucntion
     * @param aValue	Value neede to find the matching, polymorphic function.
     *
     * @return Method matching the name for the fiven object
     */
    private Method getMethod (Object anObject, String aName, Object aValue)
            throws Exception {
        String theKey    	  = anObject.getClass().getName () + "." + aName;
        Method theMethod 	  = ( Method ) map.get (theKey);

        if (theMethod == null) {
        	int   len 		  	= desciptors.length;
            // Class theValueClass = aValue.getClass ();
            for (int i=0; i < len; i++) {
        		PropertyDescriptor	d 	  = desciptors[i];
                String 				dname = d.getName();
                
                if (dname.equals(aName))  {
                    /* td.getPropertyType().isAssignableFrom(theValueClass()) { */
	                theMethod = d.getWriteMethod();
    	           	break;
                }
            }
            if (theMethod == null) {
                String err = "unable to find write method for " + theKey + "("
                           + aValue.getClass ().getName () + ")";
                Logger.info (err, this);
                // throw new NoSuchMethodException(err);
            }

            map.put (theKey, theMethod);
        }

        return theMethod;
    }

    /**
     * Cerate a new object to be filled from the database.
     *
     * @return  a new instance of the class resultTemplate.
     *
     * @throws Exception in case creation fails.
     */
    private Object createInstance () throws Exception {
        return this.resultTemplate.newInstance ();
    }

}
