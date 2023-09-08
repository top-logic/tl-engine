package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id: AbstractHTMLElementTest.java 107617 2012-01-03 09:51:15Z jst $
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

import junit.framework.TestCase;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract public class AbstractHTMLElementTest extends TestCase implements DomListener {

    protected HTMLDocumentImpl _htmlDocument;
    private List _eventsReceived = new ArrayList();


    protected void setUp() throws Exception {
        super.setUp();
        _htmlDocument = new HTMLDocumentImpl();
    }


    protected void assertProperties( String comment, String name, HTMLElement[] elements, Object[] expectedValues ) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            assertEquals( comment + " " + i, expectedValues[i], AbstractHTMLElementTest.getProperty( element,  name ) );
        }
    }


    protected HTMLOptionElement createOption( String value, String text, boolean selected ) {
        HTMLOptionElement optionElement = (HTMLOptionElement) createElement( "option", selected ? new String[][] { { "value", value }, { "selected", "true" } }
                                                                             : new String[][] { { "value", value } } );
        optionElement.appendChild( _htmlDocument.createTextNode( text ) );
        return optionElement;
    }


    /**
     * Performs a number of common tests for DOM elements. In the first test, the element is created with a set of
     * attribute values and those values are then verified.
     * In the second test, the element is created without any specified attributes and each attribute is compared with
     * the default value. A write accessor is then sought for each attribute, which should be present unless it is
     * marked as read-only. Finally, each attribute is set to a non-default value, which is then verified.
     * A propertyChanged event should be thrown whenever the attribute is changed.
     * The attributes array has 2-4 values per entry:<ol>
     * <li>the attribute name</li>
     * <li>a non-default value for the attribute</li>
     * <li>the default value for the attribute (defaults to null)</li>
     * <li>"ro" if the attribute is read-only (defaults to writeable)</li>
     */
    protected void doElementTest( String tagName, Class interfaceName, Object[][] attributes ) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Element qualifiedElement = createElement( tagName, attributes );
        assertTrue( "node should be a " + interfaceName.getName() + " but is " + qualifiedElement.getClass().getName(), interfaceName.isAssignableFrom( qualifiedElement.getClass() ) );
        assertEquals( "Tag name", tagName.toUpperCase(), qualifiedElement.getNodeName() );

        for (int i = 0; i < attributes.length; i++) {
            String propertyName = (String) attributes[i][0];
            Object propertyValue = attributes[i][1];
            assertEquals( propertyName, propertyValue, getProperty( qualifiedElement, propertyName ) );
        }

        Element element = createElement( tagName );
        ((ElementImpl) element).addDomListener( this );
        for (int i = 0; i < attributes.length; i++) {
            final String propertyName = (String) attributes[i][0];
            final Object propertyValue = attributes[i][1];
            Object defaultValue = attributes[i].length == 2 ? null : attributes[i][2];
            if (defaultValue == null) {
                assertNull( propertyName + " should not be specified by default", getProperty( element, propertyName ) );
            } else {
                assertEquals( "default " + propertyName, defaultValue,  getProperty( element, propertyName ) );
            }

            Method writeMethod = AbstractHTMLElementTest.getWriteMethod( element, propertyName );
            if (attributes[i].length > 3 && attributes[i][3].equals( "ro" )) {
                assertNull( propertyName + " is not read-only", writeMethod );
            } else {
                assertNotNull( "No modifier defined for " + propertyName );
                clearReceivedEvents();
                writeMethod.invoke( element, new Object[] { propertyValue } );
                assertEquals( "modified " + propertyName, propertyValue, getProperty( element, propertyName ) );
                expectPropertyChange( element, propertyName );
            }
        }
    }


    protected Element createElement( String tagName ) {
        return createElement( tagName, new String[0][] );
    }


    protected Element createElement( String tagName, Object[][] attributes ) {
        Element element = _htmlDocument.createElement( tagName );
        for (int i = 0; i < attributes.length; i++) {
            Object[] attribute = attributes[i];
            element.setAttribute( (String) attribute[0], toAttributeValue( attribute[1] ) );
        }
        return element;
    }


    private static String toAttributeValue( Object value ) {
        return value == null ? null : value.toString();
    }


    private static Object getProperty( Object element, final String propertyName ) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        PropertyDescriptor descriptor = getPropertyDescriptor( element, propertyName );
        if (descriptor == null || descriptor.getReadMethod() == null) return null;
        Method readMethod = descriptor.getReadMethod();
        Object[] args = new Object[0];
        return readMethod.invoke( element, args );

    }


    private static Method getWriteMethod( Object element, final String propertyName ) throws IntrospectionException {
        PropertyDescriptor descriptor = getPropertyDescriptor( element, propertyName );
        return descriptor == null ? null : descriptor.getWriteMethod();
    }


    private static PropertyDescriptor getPropertyDescriptor( Object element, String propertyName ) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo( element.getClass(), Object.class );

        int index;
        while ((index = propertyName.indexOf( '-' )) >= 0) {
            propertyName = propertyName.substring( 0, index ) + Character.toUpperCase( propertyName.charAt( index+1 ) ) + propertyName.substring( index+2 );
        }
        if (element instanceof AttributeNameAdjusted) propertyName = ((AttributeNameAdjusted) element).getJavaAttributeName( propertyName );

        PropertyDescriptor properties[] = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (property.getName().equalsIgnoreCase( propertyName )) return property;
        }
        return null;
    }


    protected void clearReceivedEvents() {
        _eventsReceived.clear();
    }


    protected void expectPropertyChange( Element element, String property ) {
        assertFalse( "Did not receive a property change event for " + element.getTagName() + "." + property, _eventsReceived.isEmpty() );
    }

//-------------------------------------- DomListener methods --------------------------------------------


    public void propertyChanged( Element changedElement, String propertyName ) {
        _eventsReceived.add( new Object[] { changedElement, propertyName } );
    }

}
