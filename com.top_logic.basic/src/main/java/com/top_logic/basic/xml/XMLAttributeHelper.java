/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Static helper methods for extracting values of basic types from
 * {@link org.xml.sax.Attributes}.
 * 
 * The helper methods of this class were extracted from
 * <code>LayoutComponent</code> and are intended to replace these methods.
 * 
 * TODO TL 5.2: Remove these methods from LayoutComponent and remove this
 * comment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLAttributeHelper {
    
    /**
     * Extracts the optional attribute of the given name and returs its value String.
     * 
     * @param atts          the set of SAX attributes from which the String is to be
     *                      extracted.
     * @param name          the name of the attribute to extract.
     * @param defaultValue  the value to return, if there is no attribute with
     *                      the given name.
     */
    public static String getAsString(Attributes atts, String name, String defaultValue) {
        String result = XMLAttributeHelper.getAsStringOptional(atts, name);
        if ((result == null) || ("null".equals(result))) {
            result = defaultValue;
        }
        return result;
    }

    /**
	 * Same as {@link #getAsInstanceOfClass(Attributes, String, Class)}, but
	 * without default value.
	 * 
	 * @see #getAsInstanceOfClass(Attributes, String, Class)
	 */
    public static Object getAsInstanceOfClass(Attributes atts, String name) throws SAXException {
		return getAsInstanceOfClass(atts, name, (Class) null);
    }

    /**
     * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * 
     * @param atts
     *     The set of attributes
     * @param propertyName
     *     The name of the requested attribute.
	 * @param defaultClass
	 *        The class to instantiate or look up the singleton from, if no
	 *        configuration is given. May be <code>null</code>. In that case, a
	 *        configuration is required.
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
     * @throws SAXException
     *     If the attribute is mandatory but not found in the set of attributes, or 
     *     if an exception occurs during instantiating the class. 
     */
    public static Object getAsInstanceOfClass(Attributes atts, String propertyName, Class defaultClass) throws SAXException {
        String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);
        
		try {
			return ConfigUtil.getInstanceWithClassDefault(Object.class, propertyName, propertyValue, defaultClass);
		} catch (ConfigurationException ex) {
			throw wrap(ex);
		}
    }

	public static Object getAsInstance(Attributes atts, String propertyName, Object aDefaultInstance) throws SAXException {
        String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);

        try {
			return ConfigUtil.getInstanceWithInstanceDefault(Object.class, propertyName, propertyValue, aDefaultInstance);
		} catch (ConfigurationException e) {
			throw wrap(e);
		}
    }

	/**
     * Create a new iNstance of some class using CTor indictaed by signature.
     * 
     * In case no such a CTor is found the empty Ctor will be used - with a Warning.
     * 
     * @param arguments The Arguments the Ctor will be called with.
     * 
     * @throws SAXException in case Object cretaion fails.
     */
    public static Object getAsInstanceOfConfiguredClass(Attributes atts, String propertyName, Class defaultClass, Class[] signature, Object[] arguments) throws SAXException {
    	String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);
    	
    	try {
			return ConfigUtil.getInstance(Object.class, propertyName, propertyValue, defaultClass, signature, arguments);
		} catch (ConfigurationException e) {
			throw wrap(e);
		}
    }

	/**
	 * Serializes the given attribute to a {@link TagWriter}. This is the "inverse" operation of
	 * {@link #getAsInstanceOfClass(Attributes, String, Class)} or
	 * {@link #getAsInstance(Attributes, String, Object)}. The attribute is only written, if its
	 * value differs from its default value.
	 * 
	 * @param out
	 *        The writer to which the attribute is written.
	 * @param name
	 *        The name of the attribute to write.
	 * @param valueClass
	 *        The value of the attribute.
	 * @param defaultClass
	 *        The attributes default value.
	 */
	public static void writeClassAttributeFromClass(TagWriter out, String name, Class valueClass, Class defaultClass) throws IOException {
		if (valueClass != defaultClass) {
			out.writeAttribute(name, valueClass.getName());
		}
	}

	/**
	 * Same as
	 * {@link #writeClassAttributeFromClass(TagWriter, String, Class, Class)},
	 * but looks up the class for the given object. The attribute is not
	 * written, if the value is null.
	 * 
	 * @see #writeClassAttributeFromClass(TagWriter, String, Class, Class)
	 */
	public static void writeClassAttributeFromObject(TagWriter out, String name, Object value, Class defaultClass) throws IOException {
		if (value == null) return;
		writeClassAttributeFromClass(out, name, value.getClass(), defaultClass);
	}

    /**
     * Extracts the obligatory (mandatory) attribute of the given name as a String.
     * 
     * @throws SAXException if there is no attribute with the given name.
     */
    public static String getAsString(Attributes atts, String propertyName) throws SAXException {
        String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);
		checkNotNull(propertyName, propertyValue);
        return propertyValue;
    }

    /**
	 * @see Attributes#getValue(String)
	 */
	public static String getAsStringOptional(Attributes attributes, String qName) {
		return attributes.getValue(qName);
	}

	/**
     * Extracts the optional attribute of the given name as a boolean.
     * 
     * Values starting case insensitive with a <code>t</code> or a
     * <code>y</code> are interpreted as <code>true</code> {same as
     * {@link StringServices#parseBoolean(String)}.
     * 
     * @param atts          the set of SAX attributes from which the String is to be
     *                      extracted.
     * @param name          the name of the attribute to extract.
     * @param defaultValue  the value to return, if there is no attribute with
     *                      the given name.
     */
    public static boolean getAsBoolean(
        Attributes atts,  String name, boolean defaultValue) {

        String val = XMLAttributeHelper.getAsStringOptional(atts, name);
        if (val == null) {
            return defaultValue;
        }
        else 
            return StringServices.parseBoolean(val);
    }
    
    /**
     * Extracts the obligatory (mandatory) attibute of the given name as a boolean.
     * 
     * Values starting case insensitive with a <code>t</code> or a
     * <code>y</code> are interpreted as <code>true</code>.
     */
    public static boolean getAsBoolean(Attributes atts, String propertyName) throws SAXException {
        String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);
		checkNotNull(propertyName, propertyValue);
        return  StringServices.parseBoolean(propertyValue);
    }
    
    /**
     * Extracts the optional attribute of the given name as a integer.
     * 
     * @param atts the set of SAX attributes from which the String is to be
     * extracted.
     * @param name the name of the attribute to extract.
     * @param defaultValue the value to return, if there is no attribute with
     * the given name.
     * @throws NumberFormatException if the value of the attribute is not an
     * integer.
     */
    public static int getAsInteger(Attributes atts, String name, int defaultValue) {

        String val = XMLAttributeHelper.getAsStringOptional(atts, name);
        if (val == null) {
            return defaultValue;
        }
        else
            return Integer.parseInt(val);
    }

    /**
     * Extracts the attribute of the given name as an integer. 
     * 
     * Expects the value of the attribute to be an integer. 
     * Throws an exception otherwise.
     * 
     * @throws NumberFormatException if the value of the attribute
     *         is not an integer.
     * @throws SAXException if there is no attribute with the given name.
     */
    public static int getAsInteger(Attributes atts, String propertyName) throws SAXException {
        String propertyValue = XMLAttributeHelper.getAsStringOptional(atts, propertyName);
		checkNotNull(propertyName, propertyValue);
        return Integer.parseInt(propertyValue);
    }

	public static SAXException wrap(ConfigurationException ex) throws SAXException {
		return new SAXException(ex.getMessage(), ex);
	}

	private static void checkNotNull(String propertyName, String propertyValue) throws SAXException {
		if (propertyValue == null) {
	        throw new SAXException("Property '" + propertyName + "' is mandatory.");
	    }
	}

}
