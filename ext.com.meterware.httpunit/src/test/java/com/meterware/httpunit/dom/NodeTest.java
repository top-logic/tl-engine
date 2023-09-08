package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: NodeTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004-2007, Russell Gold
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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.w3c.dom.*;

import java.util.Iterator;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class NodeTest extends TestCase {

    private DocumentImpl _document;
    private Element _element;
    private Element _foo1;
    private Element _foo2;
    private Element _bar1;
    private Element _bar2;
    private Text _text;


    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }

    public static TestSuite suite() {
        return new TestSuite( NodeTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _document = DocumentImpl.createDocument();
        _element = _document.createElement( "zork" );
        _foo1 = _document.createElement( "foo" );
        _foo2 = _document.createElement( "foo" );
        _bar1 = _document.createElement( "bar" );
        _bar2 = _document.createElement( "bar" );
        _text = _document.createTextNode( "Something to say" );
        _document.setDocumentElement( _element );
        _element.appendChild( _foo1 );
        _element.appendChild( _bar2 );
        _foo1.appendChild( _bar1 );
        _foo1.appendChild( _text );
        _foo1.appendChild( _foo2 );
    }


    /**
     * Verifies that we can create a document and verify its type.
     */
    public void testDocumentCreation() throws Exception {
        assertEquals( "Node name", "#document", _document.getNodeName() );
        assertEquals( "Node type", Node.DOCUMENT_NODE, _document.getNodeType() );
        assertNull( "Documents should not have attributes", _document.getAttributes() );
        assertNull( "Documents should not have values", _document.getNodeValue() );
        _document.setNodeValue( "an example" );
        assertNull( "Setting the element value should have no effect", _document.getNodeValue() );
        assertSame( "Owner document", _document, _document.getOwnerDocument() );
    }


    /**
     * Verifies that we can create an element with a given name and verify its type.
     */
    public void testElementCreation() throws Exception {
        assertNotNull( "Failed to create an element", _element );
        assertSame( "Owner document", _document, _element.getOwnerDocument() );
        assertEquals( "Tag name", "zork", _element.getTagName() );
        assertEquals( "Node name", "zork", _element.getNodeName() );
        assertEquals( "Node type", Node.ELEMENT_NODE, _element.getNodeType() );
        assertNull( "Elements should not have values", _element.getNodeValue() );
        _element.setNodeValue( "an example" );
        assertNull( "Setting the element value should have no effect", _element.getNodeValue() );
    }


    /**
     * Verifies that we can create a text node and verify its type.
     */
    public void testTextCreation() throws Exception {
        assertNotNull( "Failed to create a text node", _text );
        assertSame( "Owner document", _document, _text.getOwnerDocument() );
        assertEquals( "Node name", "#text", _text.getNodeName() );
        assertEquals( "Node type", Node.TEXT_NODE, _text.getNodeType() );
        assertNull( "Text nodes should not have attributes", _text.getAttributes() );
        assertEquals( "Text node value", "Something to say", _text.getNodeValue() );
        assertEquals( "Text length", "Something to say".length(), _text.getLength() );
        _text.setNodeValue( "an example" );
        assertEquals( "Revised node value", "an example", _text.getNodeValue() );
    }


    /**
     * Verifies that we can create a document type node and verify its type.
     *
    public void testDocumentTypeCreation() throws Exception {
        DocumentType documentType = com.meterware.httpunit.dom.DocumentTypeImpl.createDocumentType( _document );
        assertNotNull( "Failed to create a text node", _text );
        assertSame( "Owner document", _document, _text.getOwnerDocument() );
        assertEquals( "Node name", "#text", _text.getNodeName() );
        assertEquals( "Node type", Node.TEXT_NODE, _text.getNodeType() );
        assertNull( "Text nodes should not have attributes", _text.getAttributes() );
        assertEquals( "Text node value", "Something to say", _text.getNodeValue() );
        assertEquals( "Text length", "Something to say".length(), _text.getLength() );
        _text.setNodeValue( "an example" );
        assertEquals( "Revised node value", "an example", _text.getNodeValue() );
    }


    /**
     * Verifies that node accessors work for empty documents.
     */
    public void testEmptyDocument() throws Exception {
        Document document = DocumentImpl.createDocument();
        assertNull( "Found a bogus first child", document.getFirstChild() );
        assertNull( "Found a bogus last child", document.getLastChild() );
        assertFalse( "Reported bogus children", document.hasChildNodes() );
        assertNull( "Found a bogus next sibling", document.getNextSibling() );
        assertNull( "Found a bogus previous sibling", document.getPreviousSibling() );
        assertNull( "Found a bogus parent", document.getParentNode() );
        verifyNodeList( "empty document children", document.getChildNodes(), new Node[0] );
    }


    /**
     * Verifies that we can add children to an element (or document) and find them.
     */
    public void testAddNodeChildren() throws Exception {
        assertSame( "First child of element", _foo1, _element.getFirstChild() );
        assertSame( "Last child of element", _bar2, _element.getLastChild() );
        assertSame( "Next sibling of foo1", _bar2, _foo1.getNextSibling() );
        assertSame( "Previous sibling of bar2", _foo1, _bar2.getPreviousSibling() );
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _bar1, _text, _foo2 } );
        assertTrue( "Did not find children for foo1", _foo1.hasChildNodes() );
        assertFalse( "Found ghost children for bar1", _bar1.hasChildNodes() );
        assertSame( "Parent of bar1", _foo1, _bar1.getParentNode() );
        assertSame( "Parent of element", _document, _element.getParentNode() );
    }


    /**
     * Verifies that we can add children to an element or document and find them.
     */
    public void testElementChildrenByTagName() throws Exception {
        verifyNodeList( "baz", _element.getElementsByTagName( "baz" ), new Node[0] );
        verifyNodeList( "foo", _element.getElementsByTagName( "foo" ), new Element[] { _foo1, _foo2 } );
        verifyNodeList( "bar", _element.getElementsByTagName( "bar" ), new Element[] { _bar1, _bar2 } );
        verifyNodeList( "*", _element.getElementsByTagName( "*" ), new Element[] { _foo1, _bar1, _foo2, _bar2 } );

        verifyNodeList( "baz", _document.getElementsByTagName( "baz" ), new Node[0] );
        verifyNodeList( "foo", _document.getElementsByTagName( "foo" ), new Element[] { _foo1, _foo2 } );
        verifyNodeList( "bar", _document.getElementsByTagName( "bar" ), new Element[] { _bar1, _bar2 } );
        verifyNodeList( "*", _document.getElementsByTagName( "*" ), new Element[] { _element, _foo1, _bar1, _foo2, _bar2 } );
    }


    /**
     * Verifies that only children of a particular document may be added to its children.
     */
    public void testNodeCreatedByOtherDocument() throws Exception {
        Document foreignDocument = DocumentImpl.createDocument();
        Element foreignElement = foreignDocument.createElement( "stranger" );
        try {
            _element.appendChild( foreignElement );
            fail( "Permitted addition of element from different document" );
        } catch (DOMException e) {
            assertEquals( "Reason for exception", DOMException.WRONG_DOCUMENT_ERR, e.code );
        }

    }


    /**
     * Verifies that a document can have only one 'document element'
     */
    public void testUniqueDocumentElement() throws Exception {
        Element bogusRoot = _document.createElement( "root" );
        try {
            _document.setDocumentElement( bogusRoot );
            fail( "Permitted addition of a second document element" );
        } catch( IllegalStateException e ) {}
    }


    /**
     * Verifies that text nodes cannot have children
     */
    public void testNoChildrenForTextNodes() throws Exception {
        Element orphan = _document.createElement( "baz" );
        try {
            _text.appendChild( orphan );
            fail( "Should not have permitted addition of a child to a text node" );
        } catch (DOMException e) {
            assertEquals( "Reason for exception", DOMException.HIERARCHY_REQUEST_ERR, e.code );
        }
    }


    /**
     * Verifies that a node or one of its ancestors may not be added as its child
     */
    public void testRejectAddSelfOrAncestorAsChild() throws Exception {
        try {
            _element.appendChild( _element );
            fail( "Permitted addition of element as its own child" );
        } catch (DOMException e) {
            assertEquals( "Reason for exception", DOMException.HIERARCHY_REQUEST_ERR, e.code );
        }
        try {
            _bar1.appendChild( _element );
            fail( "Permitted addition of element as its descendant's child" );
        } catch (DOMException e) {
            assertEquals( "Reason for exception", DOMException.HIERARCHY_REQUEST_ERR, e.code );
        }
    }


    /**
     * Verifies that we can insert a child node at a specific position.
     */
    public void testInsertChild() throws Exception {
        Text newText = _document.createTextNode( "Something new" );
        _element.insertBefore( newText, _bar2 );
        verifyNodeList( "element child", _element.getChildNodes(), new Node[] { _foo1, newText, _bar2 } );
        _element.insertBefore( newText, _foo1 );
        verifyNodeList( "element child", _element.getChildNodes(), new Node[] { newText, _foo1, _bar2 } );
    }


    /**
     * Verifies that we cannot insert a child at a target by specifying a node which is not already a child of that target.
     */
    public void testInsertChildWithBadPredecessor() throws Exception {
        Text newText = _document.createTextNode( "Something new" );
        _element.insertBefore( newText, _bar2 );
        try {
            _foo1.insertBefore( newText, _bar2 );
            fail( "Permitted insertion before a node that was not a child of the target" );
        } catch (DOMException e) {
            assertEquals( "Reason for exception", DOMException.NOT_FOUND_ERR, e.code );
        }
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _bar1, _text, _foo2 } );
        verifyNodeList( "element child", _element.getChildNodes(), new Node[] { _foo1, newText, _bar2 } );
    }


    /**
     * Verifies that we can remove a child node from the document.
     */
    public void testRemoveChildFromEnd() throws Exception {
        _foo1.removeChild( _foo2 );
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _bar1, _text } );
    }


    public void testRemoveChildFromBeginning() throws Exception {
        _foo1.removeChild( _bar1 );
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _text, _foo2 } );
    }


    public void testRemoveChildFromMiddle() throws Exception {
        _foo1.removeChild( _text );
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _bar1, _foo2 } );
    }


    /**
     * Verifies that an exception is thrown if we try to remove a node which is not a child.
     */
    public void testRemoveChildFromWrongParent() throws Exception {
        try {
            _foo1.removeChild( _bar2 );
            fail( "Permitted node removal from wrong child" );
        } catch (DOMException e) {
            assertEquals( "reason for exception", DOMException.NOT_FOUND_ERR , e.code );
        }
    }


    /**
     * Verifies that we can replace children (including those already elsewhere in the tree)
     */
    public void testReplaceChild() throws Exception {
        Element baz = _document.createElement( "baz" );
        Node old = _foo1.replaceChild( baz, _text );
        assertSame( "Removed node", _text, old );
        verifyNodeList( "foo1 child", _foo1.getChildNodes(), new Node[] { _bar1, baz, _foo2 } );
    }


    /**
     * Verifies that we can clone nodes
     */
    public void testCloneNode() throws Exception {
        _element.setAttribute( "msg", "hi there" );
        Element shallowClone = (Element) _element.cloneNode( /* deep */ false );
        assertEquals( "Cloned attribute", "hi there", shallowClone.getAttribute( "msg" ) );
        assertFalse( "Shallow clone should not have children", shallowClone.hasChildNodes() );

        Element deepClone = (Element) _element.cloneNode( /* deep */ true );
        assertEquals( "Cloned attribute", "hi there", deepClone.getAttribute( "msg" ) );
        assertTrue( "Deep clone should have children", deepClone.hasChildNodes() );
        NodeList childNodes = deepClone.getChildNodes();
        assertEquals( "Number of deepClone's children", 2, childNodes.getLength() );
        assertTrue( "First child is not an element", childNodes.item(0) instanceof Element );
        assertEquals( "First cloned child's children", 3, childNodes.item(0).getChildNodes().getLength() );
    }


    /**
     * Verifies that we can iterate through nodes in order
     */
    public void testPreOrderIterator() throws Exception {
        Iterator each = ((NodeImpl)_element).preOrderIterator();
        Node[] expectedNodes = { _element, _foo1, _bar1, _text, _foo2, _bar2 };
        for (int i = 0; i < expectedNodes.length; i++) {
            assertTrue( "Iterator prematurely terminated after " + i + " nodes", each.hasNext() );
            assertSame( "Node " + (1 + i) + ":", expectedNodes[i], each.next() );
        }
        assertFalse( "Iterator should have terminated after " + expectedNodes.length + " nodes", each.hasNext() );
    }


    /**
     * Verifies that we can iterate through nodes in order, starting from a specific node.
     */
    public void testPreOrderIteratorFromANode() throws Exception {
        Iterator each = ((NodeImpl)_text).preOrderIterator();
        Node[] expectedNodes = { _text, _foo2, _bar2 };
        for (int i = 0; i < expectedNodes.length; i++) {
            assertTrue( "Iterator prematurely terminated after " + i + " nodes", each.hasNext() );
            assertSame( "Node " + (1 + i) + ":", expectedNodes[i], each.next() );
        }
        assertFalse( "Iterator should have terminated after " + expectedNodes.length + " nodes", each.hasNext() );
    }


    /**
     * Verifies that we can iterate through nodes in order, starting after a specific node.
     */
    public void testPreOrderIteratorAfterANode() throws Exception {
        Iterator each = ((NodeImpl)_foo1).preOrderIteratorAfterNode();
        Node[] expectedNodes = { _bar1, _text, _foo2, _bar2 };
        for (int i = 0; i < expectedNodes.length; i++) {
            assertTrue( "Iterator prematurely terminated after " + i + " nodes", each.hasNext() );
            assertSame( "Node " + (1 + i) + ":", expectedNodes[i], each.next() );
        }
        assertFalse( "Iterator should have terminated after " + expectedNodes.length + " nodes", each.hasNext() );
    }


    /**
     * Verifies that we can iterate through nodes in order skipping a specified subtree.
     */
    public void testPreOrderIteratorWithMask() throws Exception {
        Iterator each = ((NodeImpl)_element).preOrderIterator( new NodeImpl.IteratorMask() {
            public boolean skipSubtree( Node subtreeRoot ) {
                return subtreeRoot == _foo1;
            }
        });
        Node[] expectedNodes = { _element,  _bar2 };
        for (int i = 0; i < expectedNodes.length; i++) {
            assertTrue( "Iterator prematurely terminated after " + i + " nodes", each.hasNext() );
            assertSame( "Node " + (1 + i) + ":", expectedNodes[i], each.next() );
        }
        assertFalse( "Iterator should have terminated after " + expectedNodes.length + " nodes", each.hasNext() );
    }


    private void verifyNodeList( String comment, NodeList nl, Node[] expectedNodes ) {
        assertNotNull( "No " + comment + " node list returned", nl );
        assertEquals( "Number of " + comment + " nodes found", expectedNodes.length, nl.getLength() );
        for (int i = 0; i < expectedNodes.length; i++) {
            Node expectedNode = expectedNodes[i];
            assertSame( comment + " node " + (i+1), expectedNode,  nl.item(i) );
        }
    }
}
