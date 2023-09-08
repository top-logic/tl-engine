/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.xml.DelegatingContentHandler;

/**
 * Test case for {@link DelegatingContentHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDelegatingContentHandler extends BasicTestCase {

    /** Some characters for testing. */
    protected static final char[] CHARACTERS = "Some characters for testing"
        .toCharArray();
    
    /** All events are ignored if the delegation target is <code>null</code>. */
	public void testNullDelegation() throws SAXException {
		doTestDelegation((ContentHandler) null);
	}

	public void testDelegation() throws SAXException {
		ContentHandler eventCountingHandler = new ContentHandler() {
			int numberOfEvents = 0;
			
			@Override
			public String toString() {
				return Integer.toString(numberOfEvents);
			}
			
			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void endDocument() throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void endPrefixMapping(String prefix) throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void ignorableWhitespace(char[] ch, int start, int length)
					throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void processingInstruction(String target, String data)
					throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void setDocumentLocator(Locator locator) {
				numberOfEvents++;
			}

			@Override
			public void skippedEntity(String name) throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void startDocument() throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException {
				numberOfEvents++;
			}

			@Override
			public void startPrefixMapping(String prefix, String uri)
					throws SAXException {
				numberOfEvents++;
			}
		};

		// All events are ignored also.
		doTestDelegation(eventCountingHandler);
		
		assertEquals("15", eventCountingHandler.toString());
        doTestDelegation(eventCountingHandler);
        assertEquals("30", eventCountingHandler.toString());
	}

    /**
     * Simulate the callbacks on a content handler that are issued from a 
	 * SAX-Parser during normal operation.
     */
	protected void doTestDelegation(ContentHandler delegate)
			throws SAXException {
		// Wrap the given handler into a delegating handler.
		DelegatingContentHandler handler = 
            new DelegatingContentHandler(delegate);

        assertSame(delegate, handler.getDelegate());
		doTestDelegation(handler);
        assertSame(delegate, handler.setDelegate(delegate));
	}

    /** 
     * Simulate the callbacks on a content handler that are issued from a 
	 * SAX-Parser during normal operation.
	 * 
	 * <p>
	 * Assume, the target handler is a {@link DelegatingContentHandler} to
	 * be able to test its {@link DelegatingContentHandler#getElementDepth()}
	 * method.
	 * </p>
     */
    private void doTestDelegation(DelegatingContentHandler handler) throws SAXException {
        assertEquals(0, handler.getElementDepth());
		handler.startDocument();
        handler.setDocumentLocator(new LocatorImpl());
        handler.processingInstruction("doithere", "and do it this way");
		assertEquals(handler.getElementDepth(), 0);
		handler.startElement("uri", "name", "qualified:name", NO_ATTRIBUTES);
		assertEquals(handler.getElementDepth(), 1);
        handler.startPrefixMapping("tl", "http://www.top-logic.com");
		handler.startElement("uri", "name", "qualified:name", NO_ATTRIBUTES);
		assertEquals(handler.getElementDepth(), 2);
		handler.endElement("uri", "name", "qualified:name");
        handler.characters         (CHARACTERS, 3, 3);
        handler.ignorableWhitespace(CHARACTERS, 0, 7);
		assertEquals(handler.getElementDepth(), 1);
        handler.endPrefixMapping("tl");
		handler.endElement("uri", "name", "qualified:name");
		assertEquals(handler.getElementDepth(), 0);
        handler.characters         (CHARACTERS, 7, 4);
        handler.ignorableWhitespace(CHARACTERS, 3, 9);
        handler.skippedEntity      ("banane");
		handler.endDocument();
		assertEquals(handler.getElementDepth(), 0);
    }

    /** Dummy implemetation of an empty set of attributes. */
	static final Attributes NO_ATTRIBUTES = new Attributes() {
		@Override
		public int getIndex(String qName) {
			return -1;
		}

		@Override
		public int getIndex(String uri, String localName) {
			return -1;
		}

		@Override
		public int getLength() {
			return 0;
		}

		@Override
		public String getLocalName(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public String getQName(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public String getType(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public String getType(String qName) {
			return null;
		}

		@Override
		public String getType(String uri, String localName) {
			return null;
		}

		@Override
		public String getURI(int index) {
			return null;
		}

		@Override
		public String getValue(int index) {
			return null;
		}

		@Override
		public String getValue(String qName) {
			return null;
		}

		@Override
		public String getValue(String uri, String localName) {
			return null;
		}
	};


    /** Returns the suite of tests to execute.
     */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestDelegatingContentHandler.class));
    }

    public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
