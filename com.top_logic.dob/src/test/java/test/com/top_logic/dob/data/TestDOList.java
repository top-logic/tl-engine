
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.data;

import java.util.Iterator;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOStructureImpl;


/**
 * Testcase for {@link com.top_logic.dob.data.DOList}.
 * 
 * This Testcase is incomplet since the DOList does not correctly
 * implements features intodruced with later JDKs (
 * (e.g. ListIterator, subList()).
 * It should hovere have a complete function coverage. Since the
 * DOList is not widely used its current implemenation should be
 * sufficient.
 */
public class TestDOList extends TestCase {

    /**
     * Default CTor for a Testcase
     *
     * @param name name of fucntion to execute for Testing.
     */
    public TestDOList (String name) {
        super (name);
    }

    /**
     * Breeding area for actual testcases
     */
    public void testMain() throws Exception {
        
        MOStructureImpl mo1 = new MOStructureImpl("struct1", 10);
        
        mo1.addAttribute(new MOAttributeImpl("uno" , MOPrimitive.INTEGER));
        mo1.addAttribute(new MOAttributeImpl("due" , MOPrimitive.STRING));
        mo1.addAttribute(new MOAttributeImpl("tre" , MOPrimitive.DOUBLE));

        MOCollection moc1 = MOCollectionImpl.createListType(mo1);

        MOStructureImpl mo2 = new MOStructureImpl("struct2", 10);
        
        mo2.addAttribute(new MOAttributeImpl("un"   , MOPrimitive.SHORT));
        mo2.addAttribute(new MOAttributeImpl("dos"  , MOPrimitive.BYTE));
        mo2.addAttribute(new MOAttributeImpl("tres" , MOPrimitive.STRING));

        MOCollection moc2 = MOCollectionImpl.createListType(mo2);

        DOList dl1 = new DOList(moc1);
        
        assertTrue(dl1.isEmpty());
        assertEquals(dl1,dl1);
        dl1.add(new DefaultDataObject(mo1));
        assertEquals(dl1,dl1);
        DataObject do11 = new DefaultDataObject(mo1);
        dl1.add(0, do11);
        assertEquals(dl1,dl1);

        DOList dl2 = new DOList(moc2);
        assertEquals(dl2,dl2);
        assertTrue(! dl2.equals(dl1));
        dl2.add(new DefaultDataObject(mo2));
        assertEquals(dl2,dl2);
        assertTrue(! dl2.equals(dl1));

        assertTrue  (dl1    .contains(do11));
        assertSame  (do11   , dl1.get(0));
        assertEquals(0      , dl1.indexOf(do11));
        assertEquals(0      , dl1.lastIndexOf(do11));
        Iterator it = dl1.iterator();
        assertTrue(it.hasNext());
        assertEquals(do11   , it.next());

        ListIterator li = dl1.listIterator();
        assertTrue(li.hasNext());
        assertEquals(do11   , li.next());

        li = dl1.listIterator(1);
        assertTrue(li.hasPrevious());
        assertEquals(do11   , li.previous());

        assertTrue(dl1.hashCode() != dl2.hashCode());
        
        assertFalse(dl2.add(new DefaultDataObject(mo1)));
        assertFalse(dl2.add(dl1));
        assertNotNull(dl1.set(0, do11));

        assertEquals (do11, dl1.remove(0));
        assertTrue   (!dl1.remove(do11));

        dl1.clear();
        assertEquals(0, dl1.size());
        
        // subList will NOT work correctly ...
        
        assertNotNull(dl1.toString());
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestDOList.class);
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
