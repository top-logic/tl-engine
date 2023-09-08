/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.xml;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.simple.ExampleDataObject;

/** This class will (as part of a SAX Parser) parse a DataObject.
 * <p>
 *  This class will only create ExampleDataObjects since this class
 *  is unable to handle more complex things on its own. Look into
 *  <code>com.top_logic.knowledge.service.xml.KnowledgeBaseImporter</code>
 *  to see a more complex way to handle DataObjects.
 * </p><p>
 *   This class is <em>no Threadsafe</em>, So you must normally create a new
 *   instacne for every parser you crate, until you now exactly that only
 *   one parser at a time will use it.
 * </p><p>
 *   This class will not handle nested DataObjects. Doing so requires
 *   the assiantace of some outer class. This might be intodruced later
 *   in case it is demanded. 
 * </p>
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DOXMLHandler extends DefaultHandler implements DOXMLConstants {

    /** 
     * The (ExampleData) Object that will finally be the result. 
     * 
     * This importer assumes it is an ExampleDataObject. Subclasses
     * however may reuse this for lists or such. 
     */
    protected Object     result;

    /** Handle the "dataObject" tag. */
    protected void handleDataObject(Attributes attributes) throws SAXException {
        if (result != null)
            throw new SAXException("Nested dataobject not supported"); 
        result = new ExampleDataObject(16);
    }

    /** Handle the "attribute" tag. */
    protected void handleAttribute(Attributes attributes) throws SAXException {
        String name  = XMLAttributeHelper.getAsStringOptional(attributes, "name");
        String value = XMLAttributeHelper.getAsStringOptional(attributes, "value");
        String type  = XMLAttributeHelper.getAsStringOptional(attributes, "type");
        
        if (type == null) {
            throw new SAXException(
                "Attribute 'type' for a DataObject is mandatory here");
            // But not when we have MetObjects ...
        }
        if (name == null) {
            throw new SAXException(
                "Attribute 'name' for a DataObject is mandatory");
        }
        /* Not true for substructures ...
        if (value == null) {
            throw new SAXException(
                "Attribute "
                    + ATTRIB_VALUE
                    + " for a DataObject is mandatory");
        }
        */
        Object theObject = getValue(name, value, type);
        if (theObject != null)
            ((ExampleDataObject)result).getMap().put(name, theObject);
    }

    /** Convert the given Attribute to an Object mathcing the given Type. */
    protected Object getValue(String name, String value, String type )
        throws SAXException {
        
    	if ("java.util.Date".equals(type)) { // is encoded as long
    		return new Date(Long.parseLong(value));    
    	}
    	else if ("java.sql.Date".equals(type)) { // is encoded as long
    		return new Date(Long.parseLong(value));    
    	}
    	else if ("java.sql.Timestamp".equals(type)) { // is encoded as long
            return new Date(Long.parseLong(value));    
        }
		else if (StringID.class.getName().equals(type)) {
			return StringID.fromExternalForm(value);
		}
		else if (LongID.class.getName().equals(type)) {
			return LongID.fromExternalForm(value);
		}
            
        try {
            return StringServices.getValueWithClass(value, type);
        }
        catch (Exception e) {
            throw new SAXException("Failed to getValue( '" + name 
                + "' ,'" + value + "' , '" + type + "')" , e);
        }
    }

    /**
     * Care about "attribute" and "dataobject" in one fucntion.
     */
    @Override
	public void startElement(
        String uri,String localName,String qName,Attributes attributes) throws SAXException {
        if ("attribute".equals(qName)) {
            handleAttribute(attributes);
        }
        else if ("dataobject".equals(qName)) {
            handleDataObject(attributes);
        }
    }

    /* Not needed here ...
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        
    }
    */

    /**
     * The result will be crated on demad from the Attributes.
     * 
     * @return The dataObject <em>after</em> sucesfull parsing.
     */
    public DataObject getResult() {
        return (DataObject) result;
    }

}
