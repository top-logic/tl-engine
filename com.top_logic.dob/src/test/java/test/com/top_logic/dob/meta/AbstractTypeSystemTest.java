/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.AbstractTypeSystem;
import com.top_logic.dob.meta.TypeSystem;

/**
 * The class {@link AbstractTypeSystemTest} is a superclass to test
 * {@link AbstractTypeSystem}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTypeSystemTest extends BasicTestCase {

	/**
	 * The type system under test. is set during {@link #setUp()} and removed
	 * during {@link #tearDown()}
	 */
	protected AbstractTypeSystem typeSystem;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.typeSystem = createTypeSystem();
	}

	@Override
	protected void tearDown() throws Exception {
		this.typeSystem = null;
		super.tearDown();
	}

	/**
	 * creates the {@link TypeSystem} under test
	 * 
	 * @return a non <code>null</code> type system for test.
	 */
	protected abstract AbstractTypeSystem createTypeSystem();
	
	public void testUnionType() throws Exception {
		checkUnionType(MetaObject.NULL_TYPE, MetaObject.NULL_TYPE, MetaObject.NULL_TYPE);
		checkUnionType(MetaObject.NULL_TYPE, MetaObject.ANY_TYPE, MetaObject.ANY_TYPE);
		checkUnionType(MetaObject.ANY_TYPE, MetaObject.ANY_TYPE, MetaObject.ANY_TYPE);
		checkUnionType(MetaObject.INVALID_TYPE, MetaObject.ANY_TYPE, MetaObject.INVALID_TYPE);
		
		checkUnionType(MOPrimitive.STRING, MetaObject.NULL_TYPE, MOPrimitive.STRING);
		checkUnionType(MOPrimitive.STRING, MetaObject.ANY_TYPE, MetaObject.ANY_TYPE);
		checkUnionType(MOPrimitive.STRING, MOPrimitive.BOOLEAN, MetaObject.ANY_TYPE);
	}
	
	protected void checkUnionType(MetaObject left, MetaObject right, MetaObject expectedUnion) {
		final MetaObject unionType = typeSystem.getUnionType(left, right);
		assertEquals("Union is not symmetric", unionType, typeSystem.getUnionType(right, left));
		assertEquals(expectedUnion, unionType);
		
		assertEquals("Union of " + left + " with ifself is not a no op", left, typeSystem.getUnionType(left, left));
		assertEquals("Union of " + right + " with ifself is not a no op", right, typeSystem.getUnionType(right, right));
	}
	
	protected void checkUnionTypeByName(String left, String right, String expectedUnion) throws Exception {
		checkUnionType(typeSystem.getType(left), typeSystem.getType(right), typeSystem.getType(expectedUnion));
	}
	
	public void testIntersectionType() throws Exception {
		checkIntersectionType(MetaObject.NULL_TYPE, MetaObject.NULL_TYPE, MetaObject.NULL_TYPE);
		checkIntersectionType(MetaObject.NULL_TYPE, MetaObject.ANY_TYPE, MetaObject.NULL_TYPE);
		checkIntersectionType(MetaObject.ANY_TYPE, MetaObject.ANY_TYPE, MetaObject.ANY_TYPE);
		checkIntersectionType(MetaObject.NULL_TYPE, MetaObject.INVALID_TYPE, MetaObject.INVALID_TYPE);

		checkIntersectionType(MOPrimitive.STRING, MetaObject.ANY_TYPE, MOPrimitive.STRING);
		checkIntersectionType(MOPrimitive.STRING, MOPrimitive.BOOLEAN, MetaObject.INVALID_TYPE);
	}
	
	protected void checkIntersectionType(MetaObject left, MetaObject right, MetaObject expectedIntersection) {
		final MetaObject intersection = typeSystem.getIntersectionType(left, right);
		assertEquals("Intersection is not symmetric", intersection, typeSystem.getIntersectionType(right, left));
		assertEquals(expectedIntersection, intersection);
	}
	
	protected void checkIntersectionTypeByName(String left, String right, String expectedIntersection) throws Exception {
		checkIntersectionType(typeSystem.getType(left), typeSystem.getType(right), typeSystem.getType(expectedIntersection));
	}
	
	protected void checkHasCommonInstances(MetaObject left, MetaObject right, boolean expectedHasCommonInstances) {
		final boolean hasCommonInst = typeSystem.hasCommonInstances(left, right);
		assertEquals("Intersection is not symmetric", hasCommonInst, typeSystem.hasCommonInstances(right, left));
		assertEquals(expectedHasCommonInstances, hasCommonInst);
	}
	
	protected void checkHasCommonInstancesByName(String left, String right, boolean expectedHasCommonInstances) throws Exception {
		checkHasCommonInstances(typeSystem.getType(left), typeSystem.getType(right), expectedHasCommonInstances);
	}
	
}
