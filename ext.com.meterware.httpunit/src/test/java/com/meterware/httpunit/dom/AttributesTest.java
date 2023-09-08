package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: AttributesTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import org.w3c.dom.*;

import junit.textui.TestRunner;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class AttributesTest extends TestCase {

    private DocumentImpl _document;
    private Element      _element;
    private Attr         _heightAttribute;
    private Attr         _weightAttribute;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }

    public static TestSuite suite() {
        return new TestSuite( AttributesTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _document = DocumentImpl.createDocument();
        _element = _document.createElement( "zork" );
        _heightAttribute = _document.createAttribute( "height" );
        _weightAttribute = _document.createAttribute( "weight" );
    }


    /**
     * Verifies that we can create an attributes node and verify it.
     */
    public void testAttributeCreation() throws Exception {
        assertSame( "Owner document", _document, _heightAttribute.getOwnerDocument() );
        assertEquals( "Name", "height", _heightAttribute.getName() );
        assertEquals( "Node name", "height", _heightAttribute.getNodeName() );
        assertEquals( "Node type", Node.ATTRIBUTE_NODE, _heightAttribute.getNodeType() );
        assertNull( "Attributes should not have attributes", _heightAttribute.getAttributes() );
        assertEquals( "Initial attribute value", "", _heightAttribute.getNodeValue() );
        assertEquals( "Initial Value", "", _heightAttribute.getValue() );
        assertFalse( "Should not be marked as specified", _heightAttribute.getSpecified() );

        _heightAttribute.setNodeValue( "an example" );
        assertEquals( "Node Value after nodevalue update", "an example", _heightAttribute.getNodeValue() );
        assertEquals( "Value after nodevalue update", "an example", _heightAttribute.getValue() );
        _heightAttribute.setValue( "another one" );
        assertEquals( "Node Value after value update", "another one", _heightAttribute.getNodeValue() );
        assertEquals( "Value after value update", "another one", _heightAttribute.getValue() );
        assertTrue( "Should now be marked as specified", _heightAttribute.getSpecified() );
    }


    /**
     * Verifies that we can set unique attribute nodes on an element and retrieve and remove them.
     */
    public void testSimpleAttrNodeAssignment() throws Exception {
        assertFalse( "Element should report no attributes", _element.hasAttributes() );
        assertNull( "Onwer element should not be set before assignment", _heightAttribute.getOwnerElement() );
        _element.setAttributeNode( _heightAttribute );
        _element.setAttributeNode( _weightAttribute );
        assertSame( "owner element", _element, _heightAttribute.getOwnerElement() );
        assertTrue( "Element should acknowledge having attributes", _element.hasAttributes() );

        NamedNodeMap attributes = _element.getAttributes();
        assertNotNull( "No attributes returned", attributes );
        assertAttributesInMap( attributes, new Attr[] { _heightAttribute, _weightAttribute } );
        assertSame( "height attribute", _heightAttribute, _element.getAttributeNode( "height" ) );
        assertSame( "weight attribute", _weightAttribute, _element.getAttributeNode( "weight" ) );

        _element.removeAttributeNode( _heightAttribute );
        assertAttributesInMap( _element.getAttributes(), new Attr[] { _weightAttribute } );
        assertNull( "height attribute should be gone", _element.getAttributeNode( "height" ) );
        assertSame( "weight attribute", _weightAttribute, _element.getAttributeNode( "weight" ) );

        assertNull( "Onwer element should not be set after removal", _heightAttribute.getOwnerElement() );
    }


    /**
     * Verifies that we cannot remove attribute nodes that are not defined.
     */
    public void testIllegalAttributeNodeRemoval() throws Exception {
        _element.setAttributeNode( _heightAttribute );
        try {
            _element.removeAttributeNode( _weightAttribute );
            fail( "Should have rejected attempt to remove unknown attribute node" );
        } catch (DOMException e ) {
            assertEquals( "Reason for failure", DOMException.NOT_FOUND_ERR, e.code );
        }
    }


    /**
     * Verifies that setting an attribute node removes any older matching attribute node.
     */
    public void testSetReplacementAttributeNode() throws Exception {
        _element.setAttributeNode( _heightAttribute );
        Attr newHeight = _document.createAttribute( "height" );
        _element.setAttributeNode( newHeight );
        assertSame( "owner element", _element, newHeight.getOwnerElement() );
        assertNull( "Onwer element should not be set after removal", _heightAttribute.getOwnerElement() );

        assertAttributesInMap( _element.getAttributes(), new Attr[] { newHeight } );
        assertSame( "height attribute", newHeight, _element.getAttributeNode( "height" ) );
    }


    /**
     * Verifies that an undefined attribute is returned as an empty string.
     */
    public void testEmptyAttribute() throws Exception {
        assertEquals( "Value for undefined attribute", "", _element.getAttribute( "abcdef" ) );
    }


    /**
     * Verifies that we can set and get attributes by value.
     */
    public void testSimpleAttributes() throws Exception {
        _element.setAttribute( "height", "3" );
        _element.setAttribute( "width", "really wide" );
        assertAttributesInMap( _element.getAttributes(), new NVPair[] { new NVPair( "height", "3" ), new NVPair( "width", "really wide" ) } );
        assertTrue( "Did not recognize height attribute", _element.hasAttribute( "height" ) );
        assertFalse( "Should not have claimed attribute 'color' was present", _element.hasAttribute( "color" ) );
        assertEquals( "width attribute", "really wide", _element.getAttribute( "width") );

        _element.removeAttribute( "height" );
        assertFalse( "Height attribute should be gone now", _element.hasAttribute( "height" ) );
    }


    static class NVPair {
        private String _name;
        private String _value;

        public NVPair( String name, String value ) {
            _name = name;
            _value = value;
        }

        public String getName() {
            return _name;
        }

        public NVPair( Attr attrNode) {
            this( attrNode.getName(), attrNode.getValue() );
        }

        public String toString() {
            return "NV Pair: {" + _name + "," + _value + "}";
        }

        public boolean equals( Object obj ) {
            return getClass().equals( obj.getClass() ) && equals( (NVPair) obj );
        }

        private boolean equals( NVPair obj ) {
            return _name.equals( obj._name ) && _value.equals( obj._value );
        }
    }


    private void assertAttributesInMap( NamedNodeMap attributes, NVPair[] expectedAttributes ) {
        assertEquals( "Number of known attribute nodes", expectedAttributes.length, attributes.getLength() );

        List attributesMissing = new ArrayList();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributesMissing.add( new NVPair( (Attr) attributes.item( i )) );
        }

        for (int i = 0; i < expectedAttributes.length; i++) {
            NVPair expectedAttribute = expectedAttributes[i];
            assertTrue( "Did not find attribute " + expectedAttribute + " in item sequence", attributesMissing.contains( expectedAttribute ) );
            assertEquals( "attribute named '" + expectedAttribute.getName() + "' in map", expectedAttribute, new NVPair( (Attr) attributes.getNamedItem( expectedAttribute.getName() ) ) );
            attributesMissing.remove( expectedAttribute );
        }
    }


    /**
     * Confirms that the map contains the expected attributes.
     */
    private void assertAttributesInMap( NamedNodeMap attributes, Attr[] expectedAttributes ) {
        assertEquals( "Number of known attribute nodes", expectedAttributes.length, attributes.getLength() );

        List attributesMissing = new ArrayList();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributesMissing.add( attributes.item(i) );
        }

        for (int i = 0; i < expectedAttributes.length; i++) {
            Attr expectedAttribute = expectedAttributes[i];
            assertTrue( "Did not find attribute " + expectedAttribute + " in item sequence", attributesMissing.contains( expectedAttribute ) );
            assertSame( "attribute named '" + expectedAttribute.getName() + "' in map", expectedAttribute, attributes.getNamedItem( expectedAttribute.getName() ) );
            attributesMissing.remove( expectedAttribute );
        }
    }


}
