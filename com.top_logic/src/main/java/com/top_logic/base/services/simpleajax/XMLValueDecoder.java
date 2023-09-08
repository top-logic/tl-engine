/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;

/**
 * Parser for XML-encoded values that can be exchanged between Java and
 * JavaScript.
 * 
 * <p>
 * This class parses the protocol specified by {@link XMLValueConstants}. An
 * instance of this class serves as {@link ContentHandler} for a SAX parser.
 * After the SAX parser has finished, the parsed value can be fetched from
 * {@link #getObject()}.
 * </p>
 * 
 * TODO KHA/BHU Fix inconsitant behaviour with Long-encoding.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLValueDecoder extends DispatchingHandler implements XMLValueConstants {
    
    /**
     * List of remembered parts of the final {@link #getObject() result}.
     * 
     * The index is the id of a referenced object.
     */
    private ArrayList readObjects = new ArrayList();
    
    /**
     * A reference of the finally parsed result.
     */
    private Object finalObject = null;

    /**
     * {@link Stack} of objects that have been constructed, but that are not yet finally
     * initialized.
     */
    private Stack startedObjects = new Stack();

    /**
     * A {@link Stack} of {@link Combinator}s that are used to merge the
     * {@link Stack#peek() top} of the {@link #startedObjects} {@link Stack}
     * with a formerly started object. After this stack is empty, the
     * {@link #finalObject} is constructed.
     */
    private Stack combinators = new Stack();
    
    /**
     * Creates a new {@link XMLValueDecoder}.
     */
    public XMLValueDecoder() {
        super(true);

        registerHandler(OBJECT_ELEMENT    , new ObjectHandler());
        registerHandler(ARRAY_ELEMENT     , new ArrayHandler());
        registerHandler(REF_ELEMENT       , new RefHandler());
        registerHandler(NULL_ELEMENT      , new NullHandler());
        registerHandler(STRING_ELEMENT    , new StringHandler());
        registerHandler(INT_ELEMENT       , new IntHandler());
        registerHandler(FLOAT_ELEMENT     , new FloatHandler());
        registerHandler(BOOLEAN_ELEMENT   , new BooleanHandler());

        startedObjects.push(null); 
        // Push Placeholder for finalObject
        combinators.push(RESULT_COMBINATOR);
    }

    /**
     * Returns the parsed value.
     * 
     * Will actually reset the internal state, so Decoder can be reused.
     * As of so, a second call will always return null.
     */
    public Object getObject() {
        
        // Assert that we are in a final state
        assert combinators.size() == 1;
        assert combinators.peek() == RESULT_COMBINATOR;
        
        assert startedObjects.size() == 1;
        assert startedObjects.peek() == null;
        
        Object result = finalObject;
        
        finalObject = null;
        
        return result;
    }
    
    /**
     * Clear finalObject in case {@link #getObject()} was never called
     */
    @Override
	public void startDocument() throws SAXException {
        super.startDocument();
        finalObject = null;
    }
    
    /**
     * clear the readObjects for the next call.
     */
    @Override
	public void endDocument() throws SAXException {
        
        readObjects.clear();
        super.endDocument();
    }
    
    /**
     * Combines the given current object with the top of the
     * {@link #startedObjects} {@link Stack}.
     * 
     * <p>
     * The combination is performed using the top of the {@link #combinators}
     * {@link Stack}.
     * </p>
     */
    void setValueInCurrent(Object currentObject) {
        Combinator topCombinator = ((Combinator) combinators.peek());
        topCombinator.combine(startedObjects.peek(), currentObject);
    }

    /**
     * Report a {@link SAXException} informing about a protocol fault.
     */
    void handleUnexpectedElement(String uri, String localName) throws SAXException {
        throw new SAXException("Unexpected element: " + localName + " (" + uri + ")");      
    }

    /**
     * A combinator adds a value to an object in an implementation-dependant
     * way.
     */
    private interface Combinator {
        
        /**
         * Adds the given value to the given object.
         * 
         * @param obj
         *     The object.
         * @param value
         *     The value.
         */
        public void combine(Object obj, Object value);
    }

    /**
     * {@link Combinator} that adds a value to a collection.
     */
    private static final Combinator COLLECTION_ADD_COMBINATOR = new Combinator() {
        @Override
		public void combine(Object obj, Object value) {
            ((Collection) obj).add(value);
        }
    };

    /**
     * {@link Combinator} that assigns a value to the {@link #finalObject}
     * result.
     */
    private final Combinator RESULT_COMBINATOR = new Combinator() {
        @Override
		public void combine(Object obj, Object value) {
            assert obj == null;
            finalObject = value;
        }
    };

    /**
     * Sub-handler for array values.
     */
    /*package protected*/ class ArrayHandler extends DefaultHandler implements XMLValueConstants {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (ARRAY_ELEMENT.equals(localName)) {
                ArrayList newObject = new ArrayList();
                readObjects.add(newObject);
                startedObjects.push(newObject);
                combinators.push(COLLECTION_ADD_COMBINATOR);
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (ARRAY_ELEMENT.equals(localName)) {
                Object finishedObject = startedObjects.pop();
                combinators.pop();
                setValueInCurrent(finishedObject);
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for object values (See {@link #OBJECT_ELEMENT}).
     */
    /*package protected*/ class ObjectHandler extends ElementHandler implements XMLValueConstants {
        
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            
            if (OBJECT_ELEMENT.equals(localName)) {
                HashMap newObject = new HashMap();
                readObjects.add(newObject);
                startedObjects.push(newObject);
            }
            else if (NAME_ELEMENT.equals(localName)) {
                super.startElement(uri, localName, qName, attributes);
            }
            else if (PROPERTY_ELEMENT.equals(localName)) {
                // This is just a container Element, the Other Handlers will care.
            }
            else {
                handleUnexpectedElement(uri, localName); 
            }
        }

        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (OBJECT_ELEMENT.equals(localName)) {
                Object finishedObject = startedObjects.pop();
                setValueInCurrent(finishedObject);
            }
            else if (PROPERTY_ELEMENT.equals(localName)) {
                combinators.pop();
            }
            else if (NAME_ELEMENT.equals(localName)) {
                combinators.push(new PropertySetter(this.getStringDestructive()));
            }
            else {
                handleUnexpectedElement(uri, localName); 
            }
        }

        /**
         * {@link XMLValueDecoder.Combinator} that sets a value as property of an object (see
         * {@link XMLValueConstants#OBJECT_ELEMENT}).
         */
        private class PropertySetter implements Combinator {
            String name;
            
            /**
             * Constructs a new setter that values to the property with the
             * given name.
             */
            public PropertySetter(String name) {
                this.name = name;
            }
            
            @Override
			public void combine(Object obj, Object value) {
                ((Map) obj).put(name, value);
            }
        }
    }

    /**
     * Sub-handler for references (see {@link #REF_ELEMENT}).
     */
    /*package protected*/ class RefHandler extends DefaultHandler implements XMLValueConstants {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (REF_ELEMENT.equals(localName)) {
                Object oldObject = readObjects.get(
                    Integer.parseInt(XMLAttributeHelper.getAsStringOptional(attributes, ID_ATTRIBUTE)));
                setValueInCurrent(oldObject);
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!REF_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for <code>null</code> values (See {@link #NULL_ELEMENT}).
     */
    /*package protected*/ class NullHandler extends DefaultHandler implements XMLValueConstants {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (NULL_ELEMENT.equals(localName)) {
                setValueInCurrent(null);
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!NULL_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for {@link String} values (see
     * {@link XMLValueConstants#STRING_ELEMENT}).
     */
    /*package protected*/ class StringHandler extends ElementHandler {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!STRING_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (STRING_ELEMENT.equals(localName)) {
                setValueInCurrent(getStringDestructive());
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for {@link Integer} values (see {@link XMLValueConstants#INT_ELEMENT}).
     */
    /*package protected*/ class IntHandler extends ElementHandler {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!INT_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (INT_ELEMENT.equals(localName)) {
                // TODO wont work for long ....
                setValueInCurrent(Integer.valueOf(getStringDestructive()));
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for {@link Float} values (see {@link XMLValueConstants#FLOAT_ELEMENT}).
     */
    /*package protected*/ class FloatHandler extends ElementHandler {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!FLOAT_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (FLOAT_ELEMENT.equals(localName)) {
                setValueInCurrent(Double.valueOf(getStringDestructive()));
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
    }

    /**
     * Sub-handler for {@link Boolean} values (see
     * {@link XMLValueConstants#BOOLEAN_ELEMENT}).
     */
    /*package protected*/ class BooleanHandler extends ElementHandler {
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!BOOLEAN_ELEMENT.equals(localName)) {
                handleUnexpectedElement(uri, localName);
            }
        }
        
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (BOOLEAN_ELEMENT.equals(localName)) {
                setValueInCurrent(Boolean.valueOf(getStringDestructive()));
            } else {
                handleUnexpectedElement(uri, localName);
            }
        }
    }
}
