/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.filt.PrimaryKeyOrder;
import com.top_logic.dob.meta.MOStructureImpl;

/**
 * Test case for {@link PrimaryKeyOrder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPrimaryKeyOrder extends TestCase {

	private MOStructureImpl A;
	private MOStructureImpl B;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		A = new MOStructureImpl("A");
		MOAttributeImpl aA1 = createKeyAttribute("a1", MOPrimitive.STRING);
		A.addAttribute(aA1);
		MOAttributeImpl aA2 = createKeyAttribute("a2", MOPrimitive.INTEGER);
		A.addAttribute(aA2);
		A.setPrimaryKey(aA1, aA2);
		A.freeze();
		
		B = new MOStructureImpl("B");
		MOAttributeImpl bA1 = createKeyAttribute("a1", MOPrimitive.INTEGER);
		B.addAttribute(bA1);
		MOAttributeImpl bA2 = createKeyAttribute("a2", MOPrimitive.STRING);
		B.addAttribute(bA2);
		B.setPrimaryKey(bA1, bA2);
		B.freeze();
	}

	public void testOrder() throws DataObjectException {
		ArrayList<DataObject> objs = new ArrayList<>();
		
		objs.add(newB(3, "a"));
		objs.add(newB(2, "a"));
		objs.add(newA("d", 3));
		objs.add(newA("d", 2));
		objs.add(newA("d", 1));
		objs.add(newA("c", 1));
		objs.add(newA("b", 1));
		objs.add(newA("a", 1));
		objs.add(newB(1, "c"));
		objs.add(newB(1, "b"));
		objs.add(newB(1, "a"));
		
		Collections.sort(objs, PrimaryKeyOrder.INSTANCE);
		
		assertEquals("a", objs.get(0).getAttributeValue("a1"));
		assertEquals("b", objs.get(1).getAttributeValue("a1"));
		assertEquals("c", objs.get(2).getAttributeValue("a1"));
		assertEquals("d", objs.get(3).getAttributeValue("a1"));
		assertEquals(1, objs.get(3).getAttributeValue("a2"));
		assertEquals("d", objs.get(4).getAttributeValue("a1"));
		assertEquals(2, objs.get(4).getAttributeValue("a2"));
		assertEquals("d", objs.get(5).getAttributeValue("a1"));
		assertEquals(3, objs.get(5).getAttributeValue("a2"));
		
		assertEquals(1, objs.get(6).getAttributeValue("a1"));
		assertEquals("a", objs.get(6).getAttributeValue("a2"));
		assertEquals(1, objs.get(7).getAttributeValue("a1"));
		assertEquals("b", objs.get(7).getAttributeValue("a2"));
		assertEquals(1, objs.get(8).getAttributeValue("a1"));
		assertEquals("c", objs.get(8).getAttributeValue("a2"));
		assertEquals(2, objs.get(9).getAttributeValue("a1"));
		assertEquals(3, objs.get(10).getAttributeValue("a1"));
	}

	private DefaultDataObject newA(String a1, int a2) throws DataObjectException {
		DefaultDataObject x1 = new DefaultDataObject(A);
		x1.setAttributeValue("a1", a1);
		x1.setAttributeValue("a2", a2);
		return x1;
	}

	private DefaultDataObject newB(int a1, String a2) throws DataObjectException {
		DefaultDataObject x1 = new DefaultDataObject(B);
		x1.setAttributeValue("a1", a1);
		x1.setAttributeValue("a2", a2);
		return x1;
	}
	

	private MOAttributeImpl createKeyAttribute(String name, MOPrimitive type) {
		return new MOAttributeImpl(name, type, true);
	}

	
	public static Test suite() {
		TestSuite suite = new TestSuite(TestPrimaryKeyOrder.class);
		return suite;
	}
	
}
