/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOCollection;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.dob.xml.DOXMLHandler;
import com.top_logic.dsa.DataAccessException;

/**
 * Uses and extends the features of DispatchingHandler to import an XML structure into a {@link DataObject}. 
 * <ul>
 *   <li><em>StandardImport</em>: {@link DOList Data object lists} are created if a type "LIST" is encountered 
 *     and no value is given. A DataObject is created if the type DataObject or an unknown type name and a 
 *     <code>null</code> value are encountered. This is the normal behavior, if this instance has been 
 *     instantiated without a configuration name.</li>
 *   <li><em>DataObjectHandler</em>: </li> 
 *   <li><em>ContentHandler</em>: </li> 
 *   <li><em>StringListHandler</em>: </li> 
 * </ul>
 * This importer can be configured in the {@link ApplicationConfig}. All needed parts can be done there. 
 * 
 * For extending the import possibilities one can use the {@link #createAdditionalHandler} method.
 * This will be used, if the {@link MappingConfig#TYPE} is unknown according to the upper definition.
 *  
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class DOImporter extends DispatchingHandler {

	/**
	 * Configuration for {@link DOImporter}.
	 */
	public interface Config extends ConfigurationItem {
		/** Converters. */
		@Key(value = ConverterConfig.NAME_ATTRIBUTE)
		Map<String, ConverterConfig> getConverters();
	}

	/**
	 * Configuration of a DO converter.
	 */
	public interface ConverterConfig extends NamedConfigMandatory {
		/** Mappings. */
		@Key(value = MappingConfig.NAME_ATTRIBUTE)
		Map<String, MappingConfig> getMappings();
	}

	/**
	 * Configuration of a mapping for the DO converter.
	 */
	public interface MappingConfig extends NamedConfigMandatory {

		/**
		 * See {@link MappingConfig#getTag}.
		 */
		String TAG = "tag";

		/**
		 * See {@link MappingConfig#getType}.
		 */
		String TYPE = "type";

		/**
		 * See {@link MappingConfig#getInnerTag}.
		 */
		String INNER_TAG = "innerTag";

		/**
		 * See {@link MappingConfig#getDoType}.
		 */
		String DO_TYPE = "doType";

		/**
		 * See {@link MappingConfig#getDoId}.
		 */
		String DO_ID = "doId";

		/**
		 * See {@link MappingConfig#getFormat}.
		 */
		String FORMAT = "format";

		/** Getter for {@link MappingConfig#TAG}. */
		@Name(TAG)
		String getTag();

		/** Getter for {@link MappingConfig#TYPE}. */
		@Name(TYPE)
		Class<? extends DefaultHandler> getType();

		/** Getter for {@link MappingConfig#INNER_TAG}. */
		@Name(INNER_TAG)
		String getInnerTag();

		/** Getter for {@link MappingConfig#DO_TYPE}. */
		@Name(DO_TYPE)
		String getDoType();

		/** Getter for {@link MappingConfig#DO_ID}. */
		@Name(DO_ID)
		String getDoId();

		/** Getter for {@link MappingConfig#FORMAT}. */
		@Name(FORMAT)
		String getFormat();
	}

	/** Name of the default (fall back) {@link ConverterConfig}. */
	public static final String DEFAULT_IMPORT = "DefaultDOImport";

    /** Collection of read data objects. */
	private Collection<Object> doColl;

	/** The name of the {@link ConverterConfig}, must not be <code>null</code>. */
	private final String _converterName;

    /**
	 * Creates an importer which invokes the registration out of the configuration.
	 * 
	 * If the name of the given {@link ConverterConfig} is empty or <code>null</code>, the
	 * {@link DOImporter#DEFAULT_IMPORT} will be used.
	 * 
	 * @param converterName
	 *        The name of the {@link ConverterConfig}, may be <code>null</code>.
	 * @param needsCharacterEvents
	 *        Flag, if character events are needed by the handlers.
	 */
    public DOImporter(String converterName, boolean needsCharacterEvents) {
        super(needsCharacterEvents);
        
		if (StringServices.isEmpty(converterName)) {
			_converterName = DEFAULT_IMPORT;
		} else {
			_converterName = converterName;
		}

		this.registerHandlers();
    }

    /**
	 * Creates an importer which invokes the registration out of the configuration.
	 * 
	 * If the name of the given {@link ConverterConfig} is empty or <code>null</code>, the
	 * {@link DOImporter#DEFAULT_IMPORT} will be used.
	 * 
	 * @param converterName
	 *        The name of the {@link ConverterConfig}, may be <code>null</code>.
	 */
	public DOImporter(String converterName) {
		this(converterName, /* needsCharacterEvents */ true);
    }

    /**
     * Creates an importer which invokes the registration of the standard importer.
     * 
     * @param    needsCharacterEvents    Flag, if character events are needed by the handlers.
     */
    public DOImporter(boolean needsCharacterEvents) {
        this(null, needsCharacterEvents);
    }

    /**
     * Creates an importer which invokes the registration of the standard importer.
     */
    public DOImporter() {
        this(/* needsCharacterEvents */ true);
    }

    /**
	 * Return a collection of all imported DataObjects.
	 * 
	 * TODO KHA Why not return a List ?
     * 
     * @return    The collection of parsed data objects, never <code>null</code>.
	 */
	public Collection<Object> getDoColl() {
		if (this.doColl == null){
			this.doColl = new ArrayList<>(1024); // These tend to become large
		}

        return (this.doColl);
	}
    
	/**
	 * Invoke the import of DataObject from the given file.
     * 
	 * @param aFile the file that should be converted into a DataObject.
	 * @return true if the import has be completed
	 */
	public boolean doImport(File aFile) throws DataAccessException {
		try {
			return this.doImport(new FileInputStream(aFile));
		}
        catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Invoke the import of DataObject from the given file.
     * 
	 * @param   aStream    The input stream that should be converted into a data object.
	 * @return  <code>true</code> if the import has be completed.
     * @throws  DataAccessException    If parsing fails for a reason.
	 */
	public boolean doImport(InputStream aStream) throws DataAccessException {
		try {
			SAXParser theParser = SAXUtil.newSAXParser();

            theParser.parse(aStream, this);

            return (true);
        } 
        catch (SAXException sax) {
			throw new DataAccessException(sax);
		} 
        catch (ParserConfigurationException pcx) {
            throw new DataAccessException(pcx);
		} 
        catch (IOException iox) {
            throw new DataAccessException(iox);
		}
        finally {
        	try {
				aStream.close();
			} catch (IOException e) {
				Logger.warn("Failed to cloase stream- ignored", this);
			}
        }
	}

    /** 
     * Create the standard handler for an XML based import.
     * 
     * @return    The requested importer, never <code>null</code>.
     */
    protected StandardHandler createStandardHandler() {
        return new StandardHandler();
    }

    /**
	 * Hook for defining additional handlers in sub classes.
	 * 
	 * <p>
	 * <b>Attention: This method will be called during creation of this object!</b>
	 * </p>
	 * 
	 * If the default handler are not enough, one can extend {@link #registerHandlers this
	 * mechanism} by own handler configurations.
	 * 
	 * @param mappingName
	 *        The name of the {@link MappingConfig} that serves as the base key for identifying the
	 *        handler.
	 * @return The requested handler, may be <code>null</code>.
	 */
	protected DefaultHandler createAdditionalHandler(String mappingName) {
        return null;
    }

    /**
	 * Register the handlers defined in the configuration for building up the data object.
	 */
	private void registerHandlers() {
        
        // TODO KHA/MGA a Map shared by all inner Handlers to ???
		Map<String, Object> map = new HashMap<>();

		Map<String, MappingConfig> mappings = getMappings();
		for (Entry<String, MappingConfig> entry : mappings.entrySet()) {
			String mappingName = entry.getKey();

			String tag = getTag(mappingName);
			DefaultHandler handler = createHandlerForType(map, mappingName, tag);

			if (handler != null) {
				this.registerHandler(tag, handler);
			}
		}
    }

	/**
	 * Create the special, inner handler based on the given {@link MappingConfig#TYPE}.
	 * 
	 * @param mappingName
	 *        The name of the {@link MappingConfig}.
	 * @param tag
	 *        local name of tag that will trigger dispatching to the given handler
	 */
	protected DefaultHandler createHandlerForType(Map<String, Object> aMap, String mappingName, String tag) {
		Class<? extends DefaultHandler> type = getType(mappingName);
		String innerTag = getInnerTag(mappingName);
		String format = getFormat(mappingName);
        
		if (StandardHandler.class.equals(type)) {
            return this.createStandardHandler();
        }
		if (ContentHandler.class.equals(type)) {
            return new ContentHandler(mappingName, aMap, format);
        }
		if (StringListHandler.class.equals(type)) {
			return new StringListHandler(mappingName, tag, innerTag, aMap, format);
        }
		if (InnerDataObjectHandler.class.equals(type)) {
			return new InnerDataObjectHandler(mappingName, tag, innerTag, aMap);
        }
		if (DataObjectHandler.class.equals(type)) {
			String doId = getDoId(mappingName);
			String doType = getDoType(mappingName);
            return  new DataObjectHandler(tag, doId, doType, this.getDoColl(), aMap);
        }
        
		return this.createAdditionalHandler(mappingName);
    }

	/**
	 * This class is an extension of the DOXMLHandler.
	 * It adds the imported DataObject to the collection doColl of the outer class.
	 * It is allowed to import DataObjects that contain other DataObjects and DOLists.
	 * 
	 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public class StandardHandler extends DOXMLHandler{
		Stack<Object> doStack = new Stack<>();
		Stack<Boolean> listFlagStack = new Stack<>();
		Stack<String> nameStack = new Stack<>();
	
		/**
		 * Overwrite the method end element of the superclass.
		 * It gets the last element from stack. This last element is the
		 * parent-object of the actual DataObject (child). The child is assigned
		 * to the parent.
		 * If the stack is empty, there is no parent of the current DataObject. So the
		 * current DataObject is the Root-Object and will be added to the doColl of 
		 * the outer class.
		 * 
		 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String anUri, String anLocalName, String aQName) 
		            throws SAXException {
			super.endElement(anUri, anLocalName, aQName);
			if ("dataobject".equals(aQName)) {
				if (!doStack.isEmpty()){
					Object child  = result; // save away the (now) child
			    	result = doStack.pop();
			        if (result instanceof ArrayList) {
			        	result = new DOList(MOCollectionImpl.createListType(((DataObject) child).tTable()));
			        }
			        if (result instanceof DOCollection) {
			        	((DOCollection) result).add(child);
			        }
			        else if (result instanceof GenericDataObject) {
						String atrrName = nameStack.pop();
			        	((GenericDataObject)result).getMap().put(atrrName, child);
			        }
			    }
			    else {
					getDoColl().add(result);
				}
						// resume parsing the outer DataObject
				//getDoColl().add(this.getResult());
			}
			else if (!listFlagStack.isEmpty() && listFlagStack.pop().booleanValue()) {
				if (result instanceof ArrayList) {	// Create an empty DOList with dummy MO
					DataObject theDO = new GenericDataObject("Empty", StringID.createRandomID(), 1);
					result = new DOList(MOCollectionImpl.createListType(theDO.tTable()));
				}
				
		    	Object parent = doStack.pop();
		    	if (parent instanceof GenericDataObject) {
					String atrrName = nameStack.pop();
		        	((GenericDataObject)parent).getMap().put(atrrName, result);
		        }
		    	result = parent;
		    } // else normally an attribute
		}	

		/** 
		 * Overwrite the method getValue of the superclass.
		 * If a type of an element is DataObject or the value is null and the type is not LIST, 
		 * assign the parameter name to instance-variable attrName. 
		 * If a type LIST is encountered a DOList will be created as value.
		 * This attrName will be assigned to a child-object when the parsing 
		 * of the element is finished.
		 * 
		 * @see com.top_logic.dob.xml.DOXMLHandler#getValue(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		protected Object getValue(String name, String value, String type)
			throws SAXException {
			if (type != null && (type.equals("LIST") || type.startsWith("LIST<"))) {
				nameStack.push(name);		        
				listFlagStack.push(Boolean.TRUE);
				
				if (result != null) // push parent to stack
					doStack.push(result);
				result = new ArrayList<>();

				return null;
			}
			else {
				listFlagStack.push(Boolean.FALSE);
				if ("DataObject".equals(type) || value == null){
//					Logger.info("Assuming DataObject for attribute name: " + name + " type: " + type, this);
					nameStack.push(name);
					return null; // cannot do that , yet
				} else {
					return super.getValue(name, value, type);
				}
			}
		}

		/** 
		 * Overwrite the method handleDataObject of the superclass. If the current dataObject is a child-object,
		 * the instance-variable result of the superclass is not null. It contains the parent
		 * object of the current object. This parent-object is pushed onto the stack doStack
		 * (instance-variable of the outer class).
		 * 
		 * @see com.top_logic.dob.xml.DOXMLHandler#handleDataObject(org.xml.sax.Attributes)
		 */
		@Override
		protected void handleDataObject(Attributes attributes)
			throws SAXException {
			if (result != null) // push parent to stack
				doStack.push(result);
			String theType = XMLAttributeHelper.getAsStringOptional(attributes, "type");
			if (theType != null && (theType.equals("LIST") || theType.startsWith("LIST<"))) {
				result = new ArrayList<>();
			}
			else {
				result = new GenericDataObject(theType, IdentifierUtil.fromExternalForm(XMLAttributeHelper.getAsStringOptional(attributes, "id")), 16);
			}
		}

	}

    /**
     * Handler for creating a {@link DataObject}.
     * 
     * The created object will be an instance of {@link GenericDataObject}.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    private class DataObjectHandler extends StandardHandler {
        
		private Collection<Object> dohResult;
        private Map<String, Object> contentMap;
        private String type;
        private String tag;
        private String id;
        
        /** 
         * Create a new instance of this class.
         * 
         * @param    aTag           The starting tag during parsing.
         * @param    anID           Name of the ID attribute.
         * @param    aType          The meta object type of the created object.
         * @param    aResult        The collection of data objects to append this object to.
         * @param    aContentMap    The map of values to fill the object later on.
         */
		public DataObjectHandler(String aTag, String anID, String aType, Collection<Object> aResult,
				Map<String, Object> aContentMap) {
			this.tag = aTag;
			this.id = anID;
			this.dohResult = aResult;
			this.contentMap = aContentMap;
			this.type = aType;
        }

        /** 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (this.tag.equals(qName)) {
                this.dohResult.add(this.createDataObject());
            }
            else {
                Logger.warn("No handler defined for tag '" + qName + "', ignoring values!", this);
            }
        }

        /** 
         * Create the data object out of the collected information.
         * 
         * This method will clear the map of values so calling it twice will result in two
         * data objects with different content.
         * 
         * @return    The requested data object.
         * @see       #endElement(String, String, String)
         */
        protected DataObject createDataObject() {
			DataObject theDO = new GenericDataObject(this.contentMap, this.type, StringID.valueOf((String) this.contentMap.get(this.id)));

            this.contentMap.clear();

            return (theDO);
        }
    }

    /**
     * Handler for lists of strings within an attribute.
     * 
     * This handler will create a {@link DOCollection} filled with {@link MOPrimitive} 
     * objects. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    private class StringListHandler extends ElementHandler {
        
		private Map<String, Object> map;
        private String tag;
        private List<String> stringArray;
        private String name;
        private String innerTag;
        private String devider;
        
        /** 
         * Create a new instance of this class.
         * 
         * @param    aName          The name to store the result in the given map, must not be <code>null</code>.
         * @param    aTag           The tag name of the handled element, must not be <code>null</code>.
         * @param    anInnerTag     The tag of the inner elements, must not be <code>null</code>.
         * @param    aContentMap    The map to append the created list to, must not be <code>null</code>.
         * @param    anAdditional   Delimiter for the values, may be <code>null</code>. 
         */
		public StringListHandler(String aName, String aTag, String anInnerTag, Map<String, Object> aContentMap, String anAdditional) {
            this.name     = aName;
            this.tag      = aTag;
            this.innerTag = anInnerTag;
            this.map      = aContentMap;
            this.devider  = StringServices.isEmpty(anAdditional) ? ", " : anAdditional;
        }

        /** 
         * @see com.top_logic.basic.xml.ElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (this.tag.equals(qName)) {
				this.stringArray = new ArrayList<>();
            }
        }

        /** 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (this.innerTag.equals(qName)) {
                this.stringArray.add(this.getStringDestructive().trim());
            }
            else if (!this.tag.equals(qName)) {
                Logger.warn("No handler defined for tag '" + qName + "', ignoring values!", this);
            }
            else {
                this.map.put(this.name, StringServices.toString(this.stringArray, this.devider));
            }
        }
    }

    /**
     * Handler for lists of strings within an attribute.
     * 
     * This handler will create a {@link DOCollection} filled with {@link MOPrimitive} 
     * objects. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    private class InnerDataObjectHandler extends ElementHandler {
        
		private Map<String, Object> contentMap;
        private String tag;
        private DOList idohColl;
        private String name;
        private String innerTag;
        
        /** 
         * Create a new instance of this class.
         * 
         * @param    aName          The name to store the result in the given map, must not be <code>null</code>.
         * @param    aTag           The tag name of the handled element, must not be <code>null</code>.
         * @param    anInnerTag     The tag of the inner elements, must not be <code>null</code>.
         * @param    aContentMap    The map to append the created list to, must not be <code>null</code>.
         */
		public InnerDataObjectHandler(String aName, String aTag, String anInnerTag, Map<String, Object> aContentMap) {
            this.name       = aName;
            this.tag        = aTag;
            this.innerTag   = anInnerTag;
            this.contentMap = aContentMap;
        }

        /** 
         * @see com.top_logic.basic.xml.ElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (this.tag.equals(qName)) {
                this.idohColl = new DOList(MOCollectionImpl.createListType(MOPrimitive.STRING));

                this.contentMap.put(this.name, this.idohColl);
            }
        }

        /** 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (this.innerTag.equals(qName)) {
                this.idohColl.add(this.getStringDestructive().trim());
            }
            else if (!this.tag.equals(qName)) {
                Logger.warn("No handler defined for tag '" + qName + "', ignoring values!", this);
            }
        }
    }

    /**
     * Simple handler for the content between two tags.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    private class ContentHandler extends ElementHandler {

        private String key;

		private Map<String, Object> map;

        /** Optional formatter to format the String into a resulting Object. */ 
        private Format format;

        // Constructors

        /** 
         * Create a new instance of this class.
         * 
         * @param    aKey    The name to store the result in the given map, must not be <code>null</code>.
         * @param    aMap    The map to append the created list to, must not be <code>null</code>.
         * @param    anAdd   Additional formatting properties being used by {@link #createFormatter(String)}, 
         *                   may be <code>null</code>.
         */
		public ContentHandler(String aKey, Map<String, Object> aMap, String anAdd) {

			assert getNeedCharEvent() : "You must configure Character Events for this handler";
            
            this.key    = aKey;
            this.map    = aMap;
			this.format = (!StringServices.isEmpty(anAdd)) ? this.createFormatter(anAdd) : null;
        }

        // Overridden methods from ElementHandler

        /** 
         * @see java.lang.Object#toString()
         */
        @Override
		public String toString() {
            return this.getClass().getName() + " ["
                + "key: '" + this.key
                + "', map: " + this.map
                + ", format: " + this.format
                + ']';
        }

        /** 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            String theString = this.getString();

            try {
                Object theObject = this.convertString(theString);

                this.map.put(this.key, theObject);
            }
            catch (ParseException ex) {
                throw new SAXException("Unable to convert found content ('" + theString + "')", ex);
            }
        }

        /** 
         * Convert the given string, if an additional {@link #format} has been defined.
         * 
         * @param    aString    The string to be converted, must not be <code>null</code>.
         * @return   The converted version of the given string, may be <code>null</code>.
         * @throws   ParseException    If parsing the value by the {@link #format} formatter fails.
         */
        private Object convertString(String aString) throws ParseException {
            Object theObject = null;

            if (this.format != null) {
                try {
                    theObject = this.format.parseObject(aString);
                }
                catch (ParseException ex) {
                    Logger.warn("Unable to format '" + aString + "' with " + this.format, ex, this);

                    return (null);
                }
            }

            if (theObject == null) {
                theObject = aString;
            }

            return (theObject);
        }

        /** 
         * Create a formatter for the given type and additional values.
         * 
         * @param    aType    The type defining the formatter, must not be <code>null</code>.
         * @param    anAdd    Additional values as defined in the configuration, may be <code>null</code>.
         * @return   The requested formatter or <code>null</code>.
         */
        protected Format createFormatter(String aType, String anAdd) {
            if ("Date".equals(aType)) {
				return CalendarUtil.newSimpleDateFormat(anAdd);
            }
            // TODO KHA this is actually more a loose (or lousy ?) Date
            if ("StrictDate".equals(aType)) {
				SimpleDateFormat addFormat = new SimpleDateFormat(anAdd) {
                    @Override
					public Object parseObject(String aSource) throws ParseException {
                        if (StringServices.isEmpty(aSource)) {
                            return (new Date(0));
                        }

                        try {
                            aSource = aSource.substring(0, this.toPattern().length());
                        }
                        catch (Exception ex) {
                           // ignored   
                        }

                        return super.parseObject(aSource);
                    }
                };
				addFormat.setTimeZone(TimeZones.systemTimeZone());
				return addFormat;
            }
            else if ("Double".equals(aType)) {
                return new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH)) {
                    @Override
					public Object parseObject(String source) throws ParseException {
                        Number theNumber = (Number) super.parseObject(source);

						return Double.valueOf(theNumber.doubleValue());
                    }
                };
            }
            else if ("Float".equals(aType)) {
                return new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH)) {
                    @Override
					public Object parseObject(String source) throws ParseException {
                        Number theNumber = (Number) super.parseObject(source);

						return Float.valueOf(theNumber.floatValue());
                    }
                };
            }
            else if ("Integer".equals(aType)) {
                return new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH)) {
                    @Override
					public Object parseObject(String source) throws ParseException {
                        Number theNumber = (Number) super.parseObject(source);

						return Integer.valueOf(theNumber.intValue());
                    }
                };
            }
            else if ("Long".equals(aType)) {
                return new DecimalFormat("#") {
                    @Override
					public Object parseObject(String source) throws ParseException {
                        Number theNumber = (Number) super.parseObject(source);

						return Long.valueOf(theNumber.longValue());
                    }
                };
            }
            
            return null;
        }

        // Private methods

        /** 
         * Divide the type definition from additional parameters and call {@link #createFormatter(String, String)}.
         * 
         * @param    someProps    The properties defined in the configuration, must not be <code>null</code>.
         * @return   The requested formatter or <code>null</code>.
         */
        private Format createFormatter(String someProps) {
            int thePos = someProps.indexOf(':');

            if (thePos >= 0) {
                String theType = someProps.substring(0, thePos);
                String theAdd  = someProps.substring(thePos + 1);

                return (this.createFormatter(theType, theAdd));
            }
            else {
                return (this.createFormatter(someProps, null));
            }
        }
    }

	/**
	 * Getter for {@link DispatchingHandler#needCharEvent}.
	 */
	protected boolean getNeedCharEvent() {
		return needCharEvent;
	}

	/**
	 * Getter for the configuration.
	 */
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#getConverters()}.
	 */
	public Map<String, ConverterConfig> getConverters() {
		return getConfig().getConverters();
	}

	/**
	 * Getter for {@link ConverterConfig}.
	 */
	public ConverterConfig getConverter() {
		Map<String, ConverterConfig> converters = getConverters();
		return converters.get(_converterName);
	}

	/**
	 * Getter for {@link ConverterConfig#getMappings()}.
	 */
	public Map<String, MappingConfig> getMappings() {
		ConverterConfig converter = getConverter();
		if (converter == null) {
			return Collections.emptyMap();
		}
		return converter.getMappings();
	}

	/**
	 * Getter for {@link MappingConfig} by name.
	 */
	public MappingConfig getMapping(String mappingName) {
		Map<String, MappingConfig> mappings = getMappings();
		return mappings.get(mappingName);
	}

	/**
	 * Getter for {@link MappingConfig#TAG} by mapping name.
	 */
	public String getTag(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? StringServices.EMPTY_STRING : mapping.getTag();
	}

	/**
	 * Getter for {@link MappingConfig#TYPE} by mapping name.
	 */
	public Class<? extends DefaultHandler> getType(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? null : mapping.getType();
	}

	/**
	 * Getter for {@link MappingConfig#INNER_TAG} by mapping name.
	 */
	public String getInnerTag(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? StringServices.EMPTY_STRING : mapping.getInnerTag();
	}

	/**
	 * Getter for {@link MappingConfig#DO_TYPE} by mapping name.
	 */
	public String getDoType(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? StringServices.EMPTY_STRING : mapping.getDoType();
	}

	/**
	 * Getter for {@link MappingConfig#DO_ID} by mapping name.
	 */
	public String getDoId(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? StringServices.EMPTY_STRING : mapping.getDoId();
	}

	/**
	 * Getter for {@link MappingConfig#FORMAT} by mapping name.
	 */
	public String getFormat(String mappingName) {
		MappingConfig mapping = getMapping(mappingName);
		return mapping == null ? StringServices.EMPTY_STRING : mapping.getFormat();
	}
}
