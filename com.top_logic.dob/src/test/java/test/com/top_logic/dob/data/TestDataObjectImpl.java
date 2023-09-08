
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.data;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.util.MetaObjectUtils;


/**
 * Test the default DataObject implementation.
 *
 *                              actual tests on DataObjectImpl
 *
 * @author  <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestDataObjectImpl extends TestCase {

	/**
	 * Name of mandatory attribute.
	 */
    protected static final String DUMMY_MANDATORY_ATTR = "mandatory";

    /**
     * Name of optional attribute.
     */
    protected static final String DUMMY_OPTIONAL_ATTR = "optional";
    
    /**
     * Name of immutable attribute.
     */
	protected static final String DUMMY_IMMUTABLE_ATTR = "immutable";
	
	/** DUMMY MetaObjcct used for testing */
    private MetaObject DUMMY;

	private MOStructureImpl B;

	protected MOClassImpl C;

	protected MOClassImpl D;

	protected MOClassImpl E;

	private MOCollection LIST_OF_B;

	private MOCollection LIST_OF_C;

	private MOCollection LIST_OF_D;

	private MOCollection LIST_OF_E;

	private MOStructureImpl A;
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
		MOStructureImpl result = new MOStructureImpl("Dummy");
		result.addAttribute(new MOAttributeImpl("a" , MOPrimitive.STRING));
		result.addAttribute(new MOAttributeImpl("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbb" , MOPrimitive.INTEGER));
		result.addAttribute(new MOAttributeImpl("<>&ß Blub" , MOPrimitive.BOOLEAN));
		result.addAttribute(new MOAttributeImpl(DUMMY_MANDATORY_ATTR, MOPrimitive.FLOAT, MOAttribute.MANDATORY));
		result.addAttribute(new MOAttributeImpl(DUMMY_OPTIONAL_ATTR, MOPrimitive.FLOAT, !MOAttribute.MANDATORY));
		result.addAttribute(new MOAttributeImpl(DUMMY_IMMUTABLE_ATTR, MOPrimitive.STRING, !MOAttribute.MANDATORY, MOAttribute.IMMUTABLE));
		
		DUMMY = result;
		DUMMY.freeze();
		
    	B = new MOStructureImpl("B");
    	B.freeze();
    	C = new MOClassImpl("C");
    	C.freeze();
    	D = new MOClassImpl("D");
    	D.setSuperclass(C);
    	E = new MOClassImpl("E");
    	E.freeze();
    	
    	LIST_OF_B = MOCollectionImpl.createListType(B);
    	LIST_OF_C = MOCollectionImpl.createListType(C);
    	LIST_OF_D = MOCollectionImpl.createListType(D);
    	LIST_OF_E = MOCollectionImpl.createListType(E);
    	
    	A = new MOStructureImpl("A");
    	A.addAttribute(new MOAttributeImpl("l", LIST_OF_B, false, false));
    	A.addAttribute(new MOAttributeImpl("l2", LIST_OF_C, false, false));
    	A.freeze();
    }
    
    @Override
    protected void tearDown() throws Exception {
    	DUMMY = null;
    	A = null;
    	B = null;
    	C = null;
    	D = null;
    	E = null;
    	LIST_OF_B = null;
    	LIST_OF_C = null;
    	LIST_OF_D = null;
    	LIST_OF_E = null;
    	super.tearDown();
    }

    /**
     * Default Constructor.
     *
     * @param name (function-) name of test to perform.
     */                                                    
    public TestDataObjectImpl (String name) {
        super (name);
    }

    public void testObjectSemantics() {
        DataObject theObject  = this.createDummy();
        DataObject theObject2 = this.createDummy();

        assertTrue("Two created objects are equal!", 
                        !theObject.equals(theObject2));

        assertNotNull("toString() returns null!", theObject.toString());
        assertTrue("toString() returns empty result!", 
                        theObject.toString().length() > 0);

        assertEquals("Hashcode is not equal on second call!",
                          theObject.hashCode(), theObject.hashCode());
    }

    /**
     * Test for an attribute, which is immutable.
     */
    public void testImmutable() throws Exception  {
        DataObject theObject = this.createDummy();
        theObject.setAttributeValue(DUMMY_IMMUTABLE_ATTR, "Eins");

        try {
            theObject.setAttributeValue(DUMMY_IMMUTABLE_ATTR, "Zwei");

            fail("Immutable Attributes '" + DUMMY_IMMUTABLE_ATTR + "' has been set twice!");
        }
        catch (DataObjectException expected)  { /* expected */ }
    }
    
    public void testInheritance() throws DataObjectException {
    	MOClassImpl A = new MOClassImpl("A");
    	A.freeze();
    	MOClassImpl B = new MOClassImpl("B");
    	B.setSuperclass(A);
    	B.freeze();
    	
    	MOStructureImpl C = new MOStructureImpl("C");
    	C.addAttribute(new MOAttributeImpl("a", A, false, false));
    	C.freeze();

    	DefaultDataObject a1 = new DefaultDataObject(A);
    	DefaultDataObject b1 = new DefaultDataObject(B);
    	DefaultDataObject c1 = new DefaultDataObject(C);
    	c1.setAttributeValue("a", a1);
    	c1.setAttributeValue("a", b1);
    	try {
			c1.setAttributeValue("a", c1);
			fail("C is not assignment compatible to A.");
		} catch (DataObjectException ex) {
			// Expected.
		}
    }
    
    public void testListAttributeEmpty() throws DataObjectException {
    	DefaultDataObject a1 = new DefaultDataObject(A);
    	a1.setAttributeValue("l", Collections.emptyList());
    }
    
    public void testDOListOfStructure() throws DataObjectException {
    	DefaultDataObject a2 = new DefaultDataObject(A);
    	a2.setAttributeValue("l", new DOList(LIST_OF_B));
    	try {
    		a2.setAttributeValue("l", new DOList(LIST_OF_C));
    		fail("Must not be able to assign a list of incompatible type.");
		} catch (DataObjectException ex) {
			// Expected
		}
    }
    
    public void testListAttributeStructureType() throws DataObjectException {
    	DefaultDataObject a3 = new DefaultDataObject(A);
    	DefaultDataObject b1 = new DefaultDataObject(B);
    	a3.setAttributeValue("l", Collections.singleton(b1));
    	
    	DefaultDataObject a4 = new DefaultDataObject(A);
    	try {
        	DefaultDataObject c1 = new DefaultDataObject(C);
    		a4.setAttributeValue("l", Collections.singleton(c1));
    		fail("Must not allow setting attribut with value of incompatible type.");
    	} catch (DataObjectException ex) {
    		// Expected.
		}
    }
    
    public void testListAttributeClassType() throws DataObjectException {
    	DefaultDataObject a5 = new DefaultDataObject(A);
    	DefaultDataObject c1 = new DefaultDataObject(C);
    	a5.setAttributeValue("l2", Collections.singleton(c1));
    	
    	DefaultDataObject d1 = new DefaultDataObject(D);
    	a5.setAttributeValue("l2", Collections.singleton(d1));
    	
    	try {
        	DefaultDataObject e1 = new DefaultDataObject(E);
    		a5.setAttributeValue("l2", Collections.singleton(e1));
    		fail("Must not allow setting attribut with value of incompatible type.");
    	} catch (DataObjectException ex) {
    		// Expected.
    	}
    }
    
    public void testDOListOfClass() throws DataObjectException {
    	DefaultDataObject a6 = new DefaultDataObject(A);
    	a6.setAttributeValue("l2", new DOList(LIST_OF_C));
    	// TODO: Unclear, whether this should succeed: Since the list itself is
		// typed, one could argue that no list with a more restrictive type
		// constraint must be set, because a potential getter would not expect
		// it. Otherwise, the type constraint cannot be stored if a full round
		// trip through the persistency layer is performed. Better: Remove
		// runtime typed list.
    	//
    	// a6.setAttributeValue("l2", new DOList(LIST_OF_D));
    	try {
        	a6.setAttributeValue("l2", new DOList(LIST_OF_B));
    		fail("Must not allow setting attribut with value of incompatible content type.");
    	} catch (DataObjectException ex) {
    		// Expected.
    	}
    	try {
    		a6.setAttributeValue("l2", new DOList(LIST_OF_E));
    		fail("Must not allow setting attribut with value of incompatible content type.");
    	} catch (DataObjectException ex) {
    		// Expected.
    	}
    }

    public void testGetAttributes() throws Exception {
        DataObject theObject = this.createDummy();
        MetaObject theMeta   = theObject.tTable();
        final Iterable<? extends MOAttribute> attributes = theObject.getAttributes();

        assertNotNull("Failed to get attributes from data object!", attributes);
        
		for (MOAttribute attribute : attributes) {
            String theName = attribute.getName();
            assertEquals(attribute, MetaObjectUtils.getAttribute(theMeta, theName));
        }
    }

    /**
     * Test for the getAttributeNames() method.
     */
    public void testGetAttributeNames() throws Exception {
        String     theValue;
        DataObject theObject = this.createDummy();
        String[]   theNames  = theObject.getAttributeNames();

        assertNotNull("Failed to get attribute names from data object!", 
                           theNames);
        
        for (int thePos = 0; thePos < theNames.length; thePos++) {
            theValue = theNames[thePos];

            assertNotNull("Failed to get attribute name " + thePos + 
                               " from data object!", theValue);

            theObject.getAttributeValue(theValue);
        }
    }

    /**
     * Test for the setAttributeValue() method.
     */
    public void testSetAttributeValues() throws Exception {
        Object     theResult;
        DataObject theObject = this.createDummy();
        MetaObject theMeta   = theObject.tTable();
        String[]   attributeNames  = MetaObjectUtils.getAttributeNames(theMeta);

        assertNotNull("Failed to get attribute names from data object!", 
                           attributeNames);

        try {
            theObject.getAttributeValue("Erna");
            fail("Object has an attribute called 'Erna'?");
        }
        catch (NoSuchAttributeException ex)  { /* expected */ }

        for (int thePos = 0; thePos < attributeNames.length; thePos++) {
            String attributeName = attributeNames[thePos];
            assertNotNull("Failed to get attribute name " + thePos + 
                               " from data object!", attributeName);

            theResult = theObject.getAttributeValue(attributeName);

            MOAttribute attribute = MetaObjectUtils.getAttribute(theMeta, attributeName);
			if ((! attribute.isImmutable()) && (! attribute.isMandatory() || theResult != null)) {
                theObject.setAttributeValue(attributeName, theResult);
            }
        }
    }

    /**
     * Test the isInstanceOf() methods.
     */
    public void testIsInstanceOf() {
        DataObject theObject = this.createDummy();
        MetaObject theMeta   = (this.DUMMY);

        assertTrue("isInstanceof(MetaObject) fails!",
                        theObject.isInstanceOf(theMeta));
        assertTrue("isInstanceof(String) fails!",
                        theObject.isInstanceOf(theMeta.getName()));
    }

    protected DataObject createDummy() {
        return (new DefaultDataObject(DUMMY));
    }

	/**
     * the suite of tests to perform
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestDataObjectImpl.class);
        return suite;
    }

    /**
     * main function for direct Testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (TestDataObjectImpl.suite ());
    }

}
