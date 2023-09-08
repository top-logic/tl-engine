/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.GCQueue;

/**
 * Test case for {@link GCQueue}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestGCQueue extends TestCase {

	public void testConsumption() {
		GCQueue<String> queue = new GCQueue<>();
		Iterator<String> it1 = queue.iterator();
		assertEquals(Arrays.asList(), CollectionUtil.toList(it1));
		queue.add("A");
		queue.add("B");
		assertEquals(Arrays.asList("A", "B"), CollectionUtil.toList(it1));
		Iterator<String> it2 = queue.iterator();
		queue.add("C");
		queue.add("D");
		queue.add("E");
		queue.add("F");
		Iterator<String> it3 = queue.iterator();
		queue.add("G");
		Iterator<String> it4 = queue.iterator();
		
		assertEquals(Arrays.asList("C", "D", "E", "F", "G"), CollectionUtil.toList(it1));
		assertEquals(Arrays.asList("C", "D", "E", "F", "G"), CollectionUtil.toList(it2));
		assertEquals(Arrays.asList("G"), CollectionUtil.toList(it3));
		assertEquals(Arrays.asList(), CollectionUtil.toList(it4));
	}
	
	
    /** 
     * The suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestGCQueue.class);
    }
	
}
