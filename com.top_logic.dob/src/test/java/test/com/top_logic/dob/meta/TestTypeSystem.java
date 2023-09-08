/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAlternativeBuilder;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOAlternativeImpl;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOStructureImpl;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestTypeSystem extends TestCase {
	
	private DefaultTypeSystem typeSystem;
	private MOStructureImpl structDummy;
	private MOStructureImpl structDummy2;
	private MOClassImpl classA;
	private MOClassImpl classB;
	private MOClassImpl classC;
	private MOClassImpl classD;
	private MOCollection list_classA;
	private MOCollection list_classB;
	private MOCollection list1_sd1;
	private MOCollection set1_sd1;
	private MOCollection coll_any;
	private MOCollection list1_sd2;
	private MOCollection set1_sd2;
	private MOCollection list2_sd1;
	private MOCollection set2_sd1;
	private MOCollection set_any;
	private MOCollection list_any;
	private MOCollection coll_sd1;
	private MOCollection coll_sd2;

	private MOAlternative _alternativeBOrC;
	private MOAlternative _alternativeCOrD;



	public TestTypeSystem(String aName) {
		super(aName);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MetaObject theRoot = new MOClassImpl("root");

		List<MetaObject> theTypes = new ArrayList<>();
		
		structDummy   = new MOStructureImpl("struct_dummy");
		structDummy2  = new MOStructureImpl("struct_dummy2");
		classA        = new MOClassImpl("class_A");
		classA.freeze();
		classB        = new MOClassImpl("class_B");
		classB.setSuperclass(classA);   
		classC = new MOClassImpl("class_C");
		classC.setSuperclass(classA);
		classD = new MOClassImpl("class_D");
		classD.setSuperclass(classB);
		list_classA   = MOCollectionImpl.createListType(classA);
		list_classB   = MOCollectionImpl.createListType(classB);

		coll_any      = MOCollectionImpl.createCollectionType(MetaObject.ANY_TYPE);
		coll_sd1      = MOCollectionImpl.createCollectionType(structDummy);
		coll_sd2      = MOCollectionImpl.createCollectionType(structDummy2);
		list1_sd1     = MOCollectionImpl.createListType(structDummy);
		list2_sd1     = MOCollectionImpl.createListType(structDummy);
		list1_sd2     = MOCollectionImpl.createListType(structDummy2);
		list_any      = MOCollectionImpl.createListType(MetaObject.ANY_TYPE);
		set1_sd1      = MOCollectionImpl.createSetType(structDummy);
		set2_sd1      = MOCollectionImpl.createSetType(structDummy);
		set1_sd2      = MOCollectionImpl.createSetType(structDummy2);
		set_any       = MOCollectionImpl.createSetType(MetaObject.ANY_TYPE);

		MOAlternativeBuilder builder = new MOAlternativeImpl("B_OR_C");
		builder.registerSpecialisation(classB);
		builder.registerSpecialisation(classC);
		_alternativeBOrC = builder.createAlternative();
		_alternativeBOrC.freeze();

		builder = new MOAlternativeImpl("C_OR_D");
		builder.registerSpecialisation(classC);
		builder.registerSpecialisation(classD);
		_alternativeCOrD = builder.createAlternative();
		_alternativeCOrD.freeze();

		theTypes.add(structDummy);
		theTypes.add(structDummy2);
		theTypes.add(list1_sd1);
		theTypes.add(list1_sd2);
		theTypes.add(list_any);
		theTypes.add(coll_any);
		theTypes.add(coll_sd1);
		theTypes.add(coll_sd2);
		theTypes.add(set1_sd1);
		theTypes.add(set1_sd2);
		theTypes.add(set_any);
		theTypes.add(theRoot);

		typeSystem = new DefaultTypeSystem(theTypes);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.structDummy   = null;
		this.structDummy2  = null;
		this.list1_sd1     = null;
		this.list2_sd1     = null; 
		this.list1_sd2     = null;
		this.list_any      = null;
		this.coll_any      = null;
		this.coll_sd1      = null;
		this.coll_sd2      = null;
		this.set1_sd1      = null;
		this.set2_sd1      = null;     
		this.set1_sd2      = null;     
		this.set_any       = null;
	    
	    this.typeSystem = null;
	    
	}
	
    /**       
     * the suite of Tests to execute.
     */
	public static Test suite() {
		return new TestSuite (TestTypeSystem.class);
	}

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
    public void testGetUnionType() {
    	
    }
    
    public void testHasCommonInstances_Alternatives() {
		assertTrue(typeSystem.hasCommonInstances(_alternativeBOrC, _alternativeCOrD));
		assertTrue(typeSystem.hasCommonInstances(_alternativeBOrC, classB));
		assertTrue(typeSystem.hasCommonInstances(_alternativeBOrC, classC));
		assertTrue("D is subclass of B", typeSystem.hasCommonInstances(_alternativeBOrC, classD));
		assertTrue(typeSystem.hasCommonInstances(_alternativeCOrD, classC));
		assertTrue(typeSystem.hasCommonInstances(_alternativeCOrD, classD));
		assertTrue(typeSystem.hasCommonInstances(_alternativeCOrD, classB));
    }
    

	/**
	 * Lists can have a common interface. If ones element type is {@link Kind#ANY} the result is
	 * <code>true</code>. If they have the same element type the result is <code>true</code>.
	 * Otherwise the result must be <code>false</code>.
	 */
    public void testHasCommonInstances_ListList() {
    	assertFalse(this.typeSystem.hasCommonInstances(this.list1_sd1, this.list1_sd2));

    	assertTrue(this.typeSystem.hasCommonInstances(this.list1_sd1, this.list2_sd1));

    	assertTrue(this.typeSystem.hasCommonInstances(this.list1_sd1, this.list_any));
    }
    
    /**
     * A List and a Set have no common instance. So the result must be <code>false</code>.
     */
    public void testHasCommonInstances_ListSet() {
    	assertFalse(this.typeSystem.hasCommonInstances(this.list1_sd1, this.set1_sd1));
    }
    
    /**
	 * Sets can have a common interface. If ones element type is {@link Kind#ANY} the result is
	 * <code>true</code>. If they have the same element type the result is <code>true</code>.
	 * Otherwise the result must be <code>false</code>.
	 */
    public void testHasCommonInstances_SetSet() {
    	assertFalse(this.typeSystem.hasCommonInstances(this.set1_sd1, this.set1_sd2));

    	assertTrue(this.typeSystem.hasCommonInstances(this.set1_sd1, this.set2_sd1));

    	assertTrue(this.typeSystem.hasCommonInstances(this.set1_sd1, this.set_any));
    	
    }
    
    /**
     * Collections and Sets can have a common interface. Important is the element type.
     */
    public void testHasCommonInstances_SetCollection() {
    	assertTrue(this.typeSystem.hasCommonInstances(this.coll_any, this.set1_sd2));

    	assertTrue(this.typeSystem.hasCommonInstances(this.set1_sd1, this.coll_any));

    	assertTrue(this.typeSystem.hasCommonInstances(this.set_any, this.coll_any));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.set_any, this.coll_sd1));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.set1_sd1, this.coll_sd1));
    	
    	assertFalse(this.typeSystem.hasCommonInstances(this.set1_sd2, this.coll_sd1));
    	
    }
    
    /**
     * Collections and List can have a common interface. Important is the element type.
     */
    public void testHasCommonInstances_ListCollection() {
    	assertTrue(this.typeSystem.hasCommonInstances(this.coll_any, this.list1_sd2));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.list_any, this.coll_any));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.list_any, this.coll_sd1));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.list1_sd1, this.coll_sd1));
    	
    	assertFalse(this.typeSystem.hasCommonInstances(this.list1_sd2, this.coll_sd1));
    }
    
    /**
	 * Collections can have a common interface. If ones element type is {@link Kind#ANY} the result is
	 * <code>true</code>. If they have the same element type the result is <code>true</code>.
	 * Otherwise the result must be <code>false</code>.
	 */
    public void testHasCommonInstances_CollectionCollection() {
    	assertTrue(this.typeSystem.hasCommonInstances(this.coll_any, this.coll_sd2));
    	
    	assertTrue(this.typeSystem.hasCommonInstances(this.coll_sd2, this.coll_any));
    	
    	assertFalse(this.typeSystem.hasCommonInstances(this.coll_sd1, this.coll_sd2));
    }
    
    public void testIsAssignmentCompatible() {
    	assertTrue(this.typeSystem.isAssignmentCompatible(list1_sd1, list1_sd1));
    	
    	assertTrue(this.typeSystem.isAssignmentCompatible(list2_sd1, list1_sd1));
    	
    	assertFalse(this.typeSystem.isAssignmentCompatible(list1_sd2, list1_sd1));

    	assertFalse(this.typeSystem.isAssignmentCompatible(set1_sd1, list1_sd1));
    	
    	assertTrue(this.typeSystem.isAssignmentCompatible(list_any, list1_sd1));
    	assertTrue(this.typeSystem.isAssignmentCompatible(list_any, list1_sd1));
    	assertFalse(this.typeSystem.isAssignmentCompatible(list_any, MOPrimitive.BOOLEAN));

    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, list1_sd1));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, set1_sd1));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, classA));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, list_classA));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, structDummy));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.ANY_TYPE, MOPrimitive.BOOLEAN));

		// invalid type needs to be assignable from and to every other type so that no follow up
		// errors are produced in case of type errors
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, list1_sd1));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, set1_sd1));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, classA));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, list_classA));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, structDummy));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MetaObject.INVALID_TYPE, MOPrimitive.BOOLEAN));

    	assertTrue(this.typeSystem.isAssignmentCompatible(list1_sd1, MetaObject.INVALID_TYPE));
    	assertTrue(this.typeSystem.isAssignmentCompatible(set1_sd1, MetaObject.INVALID_TYPE));
    	assertTrue(this.typeSystem.isAssignmentCompatible(classA, MetaObject.INVALID_TYPE));
    	assertTrue(this.typeSystem.isAssignmentCompatible(list_classA, MetaObject.INVALID_TYPE));
    	assertTrue(this.typeSystem.isAssignmentCompatible(structDummy, MetaObject.INVALID_TYPE));
    	assertTrue(this.typeSystem.isAssignmentCompatible(MOPrimitive.BOOLEAN, MetaObject.INVALID_TYPE));

    	assertTrue(this.typeSystem.isAssignmentCompatible(classA, classA));
    	assertTrue(this.typeSystem.isAssignmentCompatible(classA, classB));
    	assertTrue(this.typeSystem.isAssignmentCompatible(classB, classB));
    	assertFalse(this.typeSystem.isAssignmentCompatible(classB, classA));
    	assertFalse(this.typeSystem.isAssignmentCompatible(classB, MOPrimitive.BOOLEAN));
    	assertFalse(this.typeSystem.isAssignmentCompatible(MOPrimitive.BOOLEAN, classA));
    	
    	assertFalse(this.typeSystem.isAssignmentCompatible(list_classB, classB));
    	assertFalse(this.typeSystem.isAssignmentCompatible(list_classB, MOPrimitive.BOOLEAN));

		// in a functional view: lists and objects are immutable therefore a list of subtype can be
		// assigned to a list of supertypes, because in this case the getter returns compatible
		// types.
    	assertTrue(this.typeSystem.isAssignmentCompatible(list_classA, list_classB));
    	assertFalse(this.typeSystem.isAssignmentCompatible(list_classB, list_classA));
    	
    }

    public void testIsComparableTo() {
    	
    }
    
}
