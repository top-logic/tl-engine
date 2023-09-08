/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;

// For Testing only
// import com.top_logic.knowledge.service.reference.SimpleKnowledgeBase;
// import java.io.FileInputStream;
// import com.top_logic.mig.util.DebugHelper;

/**
 * This class allows importing of Knowledgebases via an XML-File.
 *
 * It can be used to import MetaObjects only. Or to import data into
 * an Existing (DB)Knowledgebase.
 *
 * @author  Klaus Halfmann
 */
public class KnowledgeBaseImporter extends MORepositoryImporter {

    /** Use to indicate Use IDs as found in file, false crate new IDs */
    public static final boolean USE_IDS = true;

    /** DateFormat to parse Dates (German for the usual hysterical reasons) 
     *
     * This is used for backward compatibility only, new Dates are saved as milliseconds.
     */
	private DateFormat theFormat = CalendarUtil.getDateInstance(DateFormat.SHORT, Locale.GERMANY);

    /** true - Use IDs as found in file, false crate new IDs */
    protected boolean       useIds;

    /** Maps old KO-IDs to new IDs in case useIDs is false */
	protected Map<String, KnowledgeObject> knowledgeObjectMap;
    
    /** The KnowledgeBase to store the imported Objects */
    protected KnowledgeBase kBase;

    // static functions
    
    /**
     * parse a document and creates MetaObjects, KnowledgeObjects
     * and KnowledgeAssociations.
     *
     * In case you want to import more that one file in a row,
     * consider creating an instance of this class directly.
     *
     * @param   aKnowledgeBase a KnowledgeBase to store the Objects.
     * @param 	aResource a DatasourceName used to fetcxh the XML-File
     * @param   useIds  when set use the IDs (names) as found in file.
     */
    public static void importObjects (
        KnowledgeBase aKnowledgeBase, String aResource, boolean useIds, Protocol protocol) 
    {
        KnowledgeBaseImporter kbi = new KnowledgeBaseImporter(aKnowledgeBase, useIds, protocol);
		kbi.doImport(aResource);
    }

    /** Create a new KnowledgeBaseImporter for a given KnowledgeBase 
     *
     * @param   aKnowledgeBase a KnowledgeBase to store the Objects.
     * @param   useIds  when set use the IDs (names) as found in file.
     * @param protocol The {@link Protocol} to report import status to.
     */
    public KnowledgeBaseImporter(KnowledgeBase aKnowledgeBase, boolean useIds, Protocol protocol) {
    	super(aKnowledgeBase.getMORepository(), protocol);
    	
        this.kBase          = aKnowledgeBase;
        this.useIds         = useIds;
		knowledgeObjectMap = new HashMap<>();
        
        init();
    }

    private void init() {
        registerHandler(KNOWLEDGE_OBJECT_ELEMENT      , new KnowledgeObjectHandler());
        registerHandler(KNOWLEDGE_ASSOCIATION_ELEMENT , new KnowledgeAssociationHandler());
    }
    
    /** Actual importing function.
     *
     * @param  dsn A String describing a <i>TopLogic</i> Datasource.
     */
    public void doImport(String dsn) {
		InputStream is = new DataAccessProxy(dsn).getEntry();
		doImport(is, dsn);
    }
    
    /** Handler that cares about importing of DataObjects (superclass for KO and KA Handler) 
     * Syntax is:<pre>
     * &lt;tagname object_type="aType" object_name="aName"&gt;
     *      &lt;ko_attribute att_name="someName" att_value="someValue" /&gt;
     * &lt;/tagname&gt;
     * </pre>
     *
     */
    abstract protected class DataObjectHandler extends DefaultHandler {
    
        /** The object currently created */
        protected DataObject current;

		protected KnowledgeObject lookupCachedKO(String id) throws SAXException {
			KnowledgeObject cachedItem = knowledgeObjectMap.get(id);
			if (cachedItem == null) {
				throw new SAXException("KnowledgeObject " + id + " is not in lookup table");
			}
			return cachedItem;
		}

        /** Special handling for Date based AttributeValues here */
        protected java.util.Date buildDateAttributeValue (MetaObject type, String value)
            throws SAXException  {
            
            if (value.indexOf('.') > 0 ) { // old, German date   
                try {
                    return theFormat.parse (value);
                } catch (ParseException pex) {
                    report.error("Error in Date format: " + value, pex);
                    return null;
                }
            }
            else {
                long l = Long.parseLong(value);
                // NumberFormatExcpetion is caught by buildAttributeValue ...
                if (type == MOPrimitive.DATE)   {
                    return new java.util.Date(l);
                }
                else if (type == MOPrimitive.SQL_DATE)   {
                    return new java.sql.Date(l);
                }
                else if (type == MOPrimitive.SQL_TIME)   {
                    return new java.sql.Time(l);
                }
                else if (type == MOPrimitive.SQL_TIMESTAMP)   {
                    return new java.sql.Timestamp(l);
                }
            }
            return null;
        }

        /** Create an Attributevalue from the given String and attribute */
        protected Object buildAttributeValue (MOAttribute anAttr, String value)
                throws SAXException {
            Object theValue;
            try {
                MetaObject  theType = anAttr.getMetaObject ();
    
                if (theType == MOPrimitive.STRING) {
                    theValue = value;
                }
                else if (theType == MOPrimitive.INTEGER) {
					theValue = Integer.valueOf(value);
                }
                else if (theType == MOPrimitive.BOOLEAN) {
					theValue = Boolean.valueOf(value);
                }
                else if (theType == MOPrimitive.SHORT) {
					theValue = Short.valueOf(value);
                }
                else if (theType == MOPrimitive.LONG) {
                    try {
						theValue = Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        // maybe it's a date
                        theValue = 
							Long.valueOf(buildDateAttributeValue(theType, value).getTime());
                    }
                }
                else if (theType == MOPrimitive.BYTE) {
					theValue = Byte.valueOf(value);
                }
                else if (theType == MOPrimitive.CHARACTER) {
                    if (value.length () == 1) {
						theValue = Character.valueOf(value.charAt(0));
                    }
                     else
                        throw new SAXException("Invalid Character Value '" + value + "'");
                } else if (theType == MOPrimitive.FLOAT) {
					theValue = Float.valueOf(value);
                }
                else if (theType == MOPrimitive.DOUBLE)  {
					theValue = Double.valueOf(value);
                }
                else if (theType == MOPrimitive.DATE)   {
                    theValue = buildDateAttributeValue(theType, value);
                }
        	    else if (theType == MOPrimitive.SQL_DATE)   {
        	        theValue = buildDateAttributeValue(theType, value);
                }
        	    else if (theType == MOPrimitive.SQL_TIME)   {
        	        theValue = buildDateAttributeValue(theType, value);
                }
        	    else if (theType == MOPrimitive.SQL_TIMESTAMP)   {
        	        theValue = buildDateAttributeValue(theType, value);
				}
                else  // value must be a KnowledgeObject in KnowledgeBase
					theValue = lookupCachedKO(value);
            }
            catch (NumberFormatException ex2) {
                throw new SAXException ("Can't assign attribute value to primitive type", ex2);
            }
            /*
            catch (UnknownObjectException ex3) {
                throw new SAXException ("Can't assign attribute value to KnowledgeObject type");
            }
            */
            return theValue;
        }
        
        /** Set the Attribute at the current Object.
         *
         * Sublclasses may wish to override this.
         * 
         * @param name  name  of the Attribute to set.
         * @param value value of attribute as string.
         */
        protected void setAttribute(String name, String value)
            throws SAXException, DataObjectException {

            if (current == null)
                throw new SAXException("No current to setAttribute() " 
                    + name + ':' + value);
            MOAttribute theMOAttribute =
                MetaObjectUtils.getAttribute(current.tTable(), name);

			Object attributeValue;
			try {
				attributeValue = buildAttributeValue(theMOAttribute, value);
			} catch (RuntimeException ex) {
				throw handleAttributeValueFailure(theMOAttribute, value, ex);
			}
			current.setAttributeValue(name, attributeValue);
        }

		private DataObjectException handleAttributeValueFailure(MOAttribute attribute, String value,
				RuntimeException cause) {
			StringBuilder msg = new StringBuilder();
			msg.append("Unable to get value '");
			msg.append(value);
			msg.append("' for attribute '");
			msg.append(attribute);
			msg.append("'");
			return new DataObjectException(msg.toString(), cause);
		}

        /** Called when a "ko_attribute" was found. 
         *
         * The Attributes ... are handled here.
         */
        protected void startAttribute(Attributes attributes) throws SAXException {
            
            String      name        = XMLAttributeHelper.getAsStringOptional(attributes, ATT_NAME_ATTRIBUTE);
            String      value       = XMLAttributeHelper.getAsStringOptional(attributes, ATT_VALUE_ATTRIBUTE);

            if (name == null)  {
                throw new SAXException (
                    "Attribute " + ATT_NAME_ATTRIBUTE + " for a DataObject is mandatory");
            }
            if (value == null) {
                throw new SAXException (
                    "Attribute " + ATT_VALUE_ATTRIBUTE + " for a DataObject is mandatory");
            }
            try {
                setAttribute(name, value);
            }
            catch (DataObjectException dox) {
				throw new SAXException("startAttribute() failed in resource " + currentResourceName, dox);
            }
        }
    }

    /** Handler that cares about importing of KnowledgeObjects.
      * Syntax is:<pre>
      * &lt;knowledgeobject object_type="aType" object_name="aName"&gt;
      *      &lt;ko_attribute att_name="someName" att_value="someValue" /&gt;
      * &lt;/tagname&gt;
      * </pre>
      */
    protected class KnowledgeObjectHandler extends DataObjectHandler {
    
		/**
		 * Id of object currently created. May be <code>null</code>. In this case the object can not
		 * be referenced.
		 */
		protected String currentId;
       
        /** need a public CTor so subclassing s possible. */
        public KnowledgeObjectHandler() {
            // allow subclassing
        }
        
        /** Create a new KO with the given ID. 
         * 
         * Subclasses may however fetch existing KOs, so
         * it is ok to use the name. The setting of the Id depends on 
         * USE_IDS flag
         * 
         * @param type  type of the KO to fetch/create
         * @param id    id of the KO to fetch/create
         * 
         * @return a KnowledgeObject with given type and evtually the ID
         */
        protected KnowledgeObject getKO(String type, String id) throws SAXException {
            
            KnowledgeObject result = null;
            try {
                result = kBase.createKnowledgeObject(transformId(id), type);
            }
            catch (DataObjectException utx) {
                throw new SAXException("Failed to crate "
                    + id + '@' + type , utx);
            }

            return result;
        }

        /** Create the KnowledeObject on start of the tag */
        protected void startKnowledgeObject(Attributes attributes) throws SAXException {

            String type = XMLAttributeHelper.getAsStringOptional(attributes, OBJECT_TYPE_ATTRIBUTE);
            String name = XMLAttributeHelper.getAsStringOptional(attributes, OBJECT_NAME_ATTRIBUTE);
            
            if (type == null)  {
                throw new SAXException (
                    "Attribute " + OBJECT_TYPE_ATTRIBUTE + " for a KnowledgeObject is mandatory");
            }
            KnowledgeObject knowledgeObject = getKO(type, name);

            current   = knowledgeObject;
			currentId = StringServices.nonEmpty(name);
        }
    
        /** Care for "knowledgeobject" and "ko_attribute" tags.
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)  
            throws SAXException {
            if (qName.equals(KO_ATTRIBUTE_ELEMENT)) {
                startAttribute(attributes);
            }
            else if (qName.equals(KNOWLEDGE_OBJECT_ELEMENT)) {
                startKnowledgeObject(attributes);
            }
            else
            	report.fatal("Unexpected Element '" + qName + "' in startElement()");
        }
        
        /** Store the given KnowledgeObject (when needed) */
        protected void putKnowledgeObject(KnowledgeObject ko) {
            // store the generated identifier in the lookup table only when needed
			if (!StringServices.isEmpty(currentId)) {
				knowledgeObjectMap.put(currentId, ko);
            }
        }
    
        /** Store the current KnowledgeObject on end of "knowledgeobject" tag. */
        @Override
		public void endElement(String namespaceURI, String localName, String qName) {

            if (qName.equals(KNOWLEDGE_OBJECT_ELEMENT)) {
                if (current != null) {
                    putKnowledgeObject ((KnowledgeObject) current);
                }
                current     = null;
                currentId   = null;    
            }
        }
    }
    
    /**
	 * Handler that cares about importing of KnowledgeAssociations. Syntax is:
	 * 
	 * <pre>
	 * &lt;knowledgeobject object_type="someTyoe" object_name="someID" &gt;
	 *     &lt;ka_src  object_name="" /&gt;
	 *     &lt;ka_dest object_name="destId" /&gt;
	 *     [&lt;ko_attribute att_name="referenceType" att_value="viewContents" /&gt;] *
	 * &lt;/knowledgeassociation&gt;
	 * </pre>
	 * 
	 * Creation of the actual KnowledgeAssociation is deferred until ka_dest. is parsed so order of
	 * tags is important and must not be changed .
	 */
    protected class KnowledgeAssociationHandler extends DataObjectHandler {
    
		/**
		 * Name of KnowledgeAssociation to be created. May be <code>null</code>. In this case the
		 * object can not be referenced.
		 */
		protected String currName;
        
        /** Type of KnowledgeAssociation to be created */
        protected String            currType;

		private Map<String, String> _cachedAttributes = new HashMap<>();
          
        // There is no dest since this is handled in place ...

        /** need a public CTor so subclassing is possible. */
        public KnowledgeAssociationHandler() {
            // allow subclassing
        }
        
        /** Handle start of KnowledgeAssociation tag.
         *
         * The actual creation is deferred until "ka_dest" is found.
         */
        protected void startKnowledgeAssociation(Attributes attributes) throws SAXException {

            String type = XMLAttributeHelper.getAsStringOptional(attributes, OBJECT_TYPE_ATTRIBUTE);
            String name = XMLAttributeHelper.getAsStringOptional(attributes, OBJECT_NAME_ATTRIBUTE);
            
            if (type == null)  {
                throw new SAXException (
                    "Attribute " + OBJECT_TYPE_ATTRIBUTE + " for a KnowledgeAssociation is mandatory");
            }
            
			currName = StringServices.nonEmpty(name);
            currType = type;
            
        }
 
        /** Create a new KA from the given values.
         * 
         * Subclasses may however fetch existing KAs, so
         * it is ok to use the id only. The setting of the Id depends on 
         * USE_IDS flag
         * 
         * @param type      type of the KA to fetch/create
         * @param id        id of the KA to fetch/create
         * @param source    The new source object
         * @param dest      The new destination object
         * 
         * @return a KnowledgeAssociation with given type and evtually the ID.
         */
        protected KnowledgeAssociation getKA(String type, 
                                             String id,
                                             KnowledgeObject source, 
                                             KnowledgeObject dest) 
                                             throws SAXException {
            
            KnowledgeAssociation result = null;
            try {
				result = kBase.createAssociation(transformId(id), source, dest, type);
            }
            catch (DataObjectException dox) {
                throw new SAXException(dox);
            }

                
            return result;
        }

		@Override
		protected void setAttribute(String name, String value) throws SAXException, DataObjectException {
			if (current != null) {
				super.setAttribute(name, value);
				return;
			}
			if (DBKnowledgeAssociation.REFERENCE_DEST_NAME.equals(name)) {
				String sourceName = _cachedAttributes.remove(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
				if (sourceName != null) {
					createKA(sourceName, value);
					return;
				}
			}
			if (DBKnowledgeAssociation.REFERENCE_SOURCE_NAME.equals(name)) {
				String destName = _cachedAttributes.remove(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
				if (destName != null) {
					createKA(value, destName);
					return;
				}
			}
			_cachedAttributes.put(name, value);
		}

		private void createKA(String sourceName, String destName) throws SAXException, DataObjectException {
			KnowledgeObject src = lookupCachedKO(sourceName);
			KnowledgeObject dest = lookupCachedKO(destName);
			current = getKA(currType, currName, src, dest);

			for (Entry<String, String> cachedEntry : _cachedAttributes.entrySet()) {
				super.setAttribute(cachedEntry.getKey(), cachedEntry.getValue());
			}
			_cachedAttributes.clear();
		}
    
        /** Care for "knowledgeobject" and "ko_attribute" tags.
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)  
            throws SAXException {
            if (qName.equals(KO_ATTRIBUTE_ELEMENT)) {
				startAttribute(attributes);
            }
            else if (qName.equals(KNOWLEDGE_ASSOCIATION_ELEMENT)) {
                startKnowledgeAssociation(attributes);
            }
            else {
            	report.fatal("Unexpected Element '" + qName + "' in startElement()");
            }
        }

        /** Store the current KnowledgeAssociation on end of "knowledgeobject" tag. */
        @Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

            if (qName.equals(KNOWLEDGE_ASSOCIATION_ELEMENT)) {
				if (!_cachedAttributes.isEmpty()) {
					StringBuilder noSourceOrDestError = new StringBuilder();
					noSourceOrDestError.append("Invalid definition for knowledge association '");
					noSourceOrDestError.append(currName);
					noSourceOrDestError.append("' in resource ");
					noSourceOrDestError.append(currentResourceName);
					noSourceOrDestError.append(": Either ");
					noSourceOrDestError.append(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
					noSourceOrDestError.append(" or ");
					noSourceOrDestError.append(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
					noSourceOrDestError.append(" not given.");
					throw new SAXException(noSourceOrDestError.toString());
				}
                currName  = null;
                currType  = null;
                current  = null;
            }
        }
    }

	TLID transformId(String externalId) {
		return useIds ? IdentifierUtil.fromExternalForm(externalId) : null;
	}

    /** 
     * Main function for testing 
     */
    /*
    public static void main(String args[]) throws Exception {
    
        long                  time,delta1, delta2;
        KnowledgeBase         kb;
        boolean               useIDs = true;
        
        com.top_logic.util.Constants.setRootPath(".");
        
        kb = new SimpleKnowledgeBase("Test1");
        importObjects(kb, "file://xml/test/kbase/KBMeta.xml", useIDs);
        // importObjects(kb, "file:://xml/knowledgebase/KBData.xml", useIDs);
    }
    */
}
