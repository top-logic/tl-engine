/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.OneWayListSink;

/**
 *  Test the {@link OneWayListSink}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestOneWayListSink extends TestCase {

    /** 
     * Create a new TestOneWayListSink by name.
     */
    public TestOneWayListSink(String aName) {
       super(aName);
    }

    /**
     * Test supported methods.
     */
    public void testOK() {
       OneWayListSink owls = OneWayListSink.INSTANCE;
       owls.add(owls);
       owls.add(-73, owls);
       owls.addAll(owls);
       owls.addAll(32777, owls);
    }

    /**
     * Test unsupported methods.
     */
    public void testUnsupported() {
        OneWayListSink owls = OneWayListSink.INSTANCE;
        try { owls.size(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.get(97); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.clear(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.contains(this); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.containsAll(owls); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.indexOf(this); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.isEmpty(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.iterator(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.lastIndexOf(this); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.listIterator(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.listIterator(83); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.remove(null); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.remove(12); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.removeAll(owls); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.retainAll(owls); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.set(13, this); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.subList(9,99); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.toArray(); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
        try { owls.toArray(new Object[0]); fail("Expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { /* expected */ }
    }
    
    /** Return the suite of Tests to perform */
    public static Test suite () {
        return new TestSuite (TestOneWayListSink.class);
    }

}

