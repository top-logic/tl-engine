/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.DelegatingContentHandler;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * SAX handler that extracts a single {@link AJAXRequest} object from a SOAP request message.
 * 
 * The parsed SOAP message is expected to have the following format (specified in RelaxNG compact
 * syntax).
 * 
 * <pre>
 *   namespace env = http://www.w3.org/2003/05/soap-envelope
 *   namespace cs = http://top-logic.com/base/services/cs
 *   namespace ajax = http://top-logic.com/base/services/ajax
 *    
 *   element env:Envelope {
 *      element env:Header {
 *         element cs:sequence {
 *            attribute tx {
 *               int
 *            }
 *         }
 *         element cs:source {
 *            string
 *         }
 *      }
 *      element env:Body {
 *         element ajax:execute {
 *            element cs:component {
 *               attribute source {
 *                  string
 *               }
 *               attribute target {
 *                  string
 *               }?
 *            }
 *            element cs:submit {
 *               attribute value {
 *                  int
 *               }
 *            }
 *            element ajax:command {
 *               string
 *            },
 *            element ajax:arguments {
 *               element ajax:argument {
 *                  element ajax:name { string },
 *                  element ajax:value { string }
 *               }*
 *            }
 *         }*
 *      }
 *   }
 * </pre>
 * 
 * This parser is not very strict about the concrete syntax of the message. It accepts nearly any
 * message that contains some relevant elements of the grammar given above.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AJAXRequestParser extends DispatchingHandler implements AJAXConstants {

	AJAXRequest request = new AJAXRequest();

	CommandRequest currentRequestedCommand;

	String currentParameterName;
	
	/**
	 * Create a request parser for parsing a fresh request.
	 */
	public AJAXRequestParser() {
        super(true);

        registerHandler(TARGET_COMPONENT_ELEMENT, new TargetComponentParser());
		registerHandler(COMMAND_ELEMENT, new CommandParser());
		registerHandler(SEQUENCE_ELEMENT, new SequenceParser());
		registerHandler(ACK_ELEMENT, new AckParser());
		registerHandler(SUBMIT_ELEMENT, new SubmitParser());
		registerHandler(ARGUMENT_ELEMENT, new ArgumentParser());
		registerHandler(VALUE_ELEMENT, new ValueParser()); 
		registerHandler(EXECUTE_ELEMENT, new ExecuteParser());
		registerHandler(SOURCE_ELEMENT, new SourceParser());
	}
	
	/**
	 * Returns the parsed result.
	 */
	public AJAXRequest getAJAXRequest() {
        AJAXRequest result = request;
        request = null;
        return result;
	}
	
	/**
     * Setup a new AJAXRequest for every new Document.
     */
    @Override
	public void startDocument() throws SAXException {
        super.startDocument();
        request = new AJAXRequest();
		currentRequestedCommand = request;
    }
    
    /**
     * Drop currentParameterName at end of parsing
     */
    @Override
	public void endDocument() throws SAXException {
        currentParameterName = null;
		currentRequestedCommand = null;
        super.endDocument();
    }

	/**
	 * Inner parser that extracts the {@link #TARGET_COMPONENT_ELEMENT} header.
	 */
	public class TargetComponentParser extends DefaultHandler implements AJAXConstants {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			
			currentRequestedCommand.sourceComponentID = getComponentNameOptional(attributes, SOURCE_ID_ATTRIBUTE);
			currentRequestedCommand.targetComponentID = getComponentNameOptional(attributes, TARGET_ID_ATTRIBUTE);
		}

		private ComponentName getComponentNameOptional(Attributes attributes, String attr) throws SAXException {
			String value = XMLAttributeHelper.getAsStringOptional(attributes, attr);
			if (value == null) {
				return null;
			}
			try {
				return ComponentName.newConfiguredName(attr, value);
			} catch (ConfigurationException ex) {
				throw new SAXException("Invalid component name '" + value + "'", ex);
			}
		}
		
	}

	/**
	 * Inner parser that extracts the {@link #SEQUENCE_ELEMENT} header.
	 */
	public class SequenceParser extends DefaultHandler implements AJAXConstants {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			
			String txAttributeValue = XMLAttributeHelper.getAsStringOptional(attributes, TX_ATTRIBUTE);
			if (txAttributeValue != null) {
				request.txSequence = Integer.valueOf(txAttributeValue);
			}
		}
		
	}

	/**
	 * Inner parser that extracts the {@link #ACK_ELEMENT} header.
	 */
	public class AckParser extends DefaultHandler implements AJAXConstants {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			String txAttributeValue = XMLAttributeHelper.getAsStringOptional(attributes, TX_ATTRIBUTE);
			if (txAttributeValue != null) {
				request.getAcks().add(Integer.valueOf(txAttributeValue));
			}
		}

	}

	/**
	 * Inner parser that extracts the {@link #SUBMIT_ELEMENT} header.
	 */
	public class SubmitParser extends DefaultHandler implements AJAXConstants {
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			
			String submitNrValue = XMLAttributeHelper.getAsStringOptional(attributes, VALUE_ATTRIBUTE);
			if (submitNrValue != null) {
				currentRequestedCommand.submitNumber = Integer.valueOf(submitNrValue);
			}
		}
		
	}
	
	/**
	 * Inner parser that extracts the {@link #COMMAND_ELEMENT} element.
	 */
	public class CommandParser extends ElementHandler implements AJAXConstants {
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (COMMAND_ELEMENT.equals(localName)) 
				currentRequestedCommand.command = getString();
		}
	}
	
	/**
	 * Inner parser that extracts a {@link #NAME_ELEMENT}/{@link #VALUE_ELEMENT}
	 * pair from a {@link #ARGUMENT_ELEMENT}.
	 */
	public class ArgumentParser extends ElementHandler implements AJAXConstants {
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (NAME_ELEMENT.equals(localName)) {
				currentParameterName = getStringDestructive();
			}
		}
	}
	
	class ExecuteParser extends ElementHandler implements AJAXConstants {

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (EXECUTE_ELEMENT.equals(localName)) {
				request.addCommand(currentRequestedCommand);
				currentRequestedCommand = new CommandRequest();
			}
		}

	}
	
	class SourceParser extends ElementHandler implements AJAXConstants {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			request.window = XMLAttributeHelper.getAsStringOptional(attributes, WINDOW_ATTRIBUTE);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (SOURCE_ELEMENT.equals(localName)) {
				try {
					request.source = ComponentName.newConfiguredName(SOURCE_ELEMENT, getString());
				} catch (ConfigurationException ex) {
					throw new SAXException(ex);
				}
			}
		}

	}

	/**
	 * Inner parser that parses a {@link #VALUE_ELEMENT} by forwarding to a
	 * {@link XMLValueDecoder}.
	 */
	public class ValueParser extends DelegatingContentHandler implements AJAXConstants {
		
	    XMLValueDecoder valueContentHandler = new XMLValueDecoder();
		
		/**
		 * Creates a new ValueParser that has not yet initialized its delegate.
		 */
		public ValueParser() { 
			super(null);
		}
		
		/**
		 * @see com.top_logic.basic.xml.DelegatingContentHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
		    super.startDocument();
		    valueContentHandler.startDocument();
		}
		
		/**
		 * @see com.top_logic.basic.xml.DelegatingContentHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException {
		    valueContentHandler.endDocument();
		    super.endDocument();
		}
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(namespaceURI, localName, qName, attributes);
			if (getElementDepth() == 1) {
				assertElement(namespaceURI, localName, VALUE_ELEMENT);
				setDelegate(valueContentHandler);
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (getElementDepth() == 1) {
				assertElement(namespaceURI, localName, VALUE_ELEMENT);

				Object value = valueContentHandler.getObject();
				String currPar = currentParameterName;
				currentRequestedCommand.arguments.put(currPar, value);

				setDelegate(null);
			}
			super.endElement(namespaceURI, localName, qName);
		}
		

		private void assertElement(String uri, String localName, String expectedLocalName) throws SAXException {
			if (! expectedLocalName.equals(localName)) {
				throw new SAXException(
					"Unexpected element: " + localName + " (" + uri + "), depth=" + getElementDepth());
			}
		}
	}

}