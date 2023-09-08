/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;

/**
 * Test case for {@link ArrayStack}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestArrayStack extends TestCase {

	public void testEmpty() {
		assertTrue(emptyStack().isEmpty());
		assertFalse(singletonStack().isEmpty());
	}
	
	public void testSize() {
		assertEquals(0, emptyStack().size());
		assertEquals(1, singletonStack().size());
	}
	
	public void testEmptyAfterPop() {
		Stack<String> s = singletonStack();
		s.pop();
		assertTrue(s.isEmpty());
	}

	public void testNonEmptyAfterPush() {
		Stack<String> s = emptyStack();
		s.push("A");
		assertFalse(s.isEmpty());
	}
	
	public void testPushPop() {
		doTestPushPop(emptyStack());
		doTestPushPop(singletonStack());
	}

	private void doTestPushPop(Stack<String> s) {
		s.push("B");
		assertEquals("B", s.pop());
	}
	
	public void testPeek() {
		assertEquals("A", singletonStack().peek());
		assertEquals("A", singletonStack().peek(0));
		
		Stack<String> s = singletonStack();
		s.push("B");
		assertEquals("B", s.peek(0));
		assertEquals("A", s.peek(1));
		assertEquals(2, s.size());
	}
	
	private Stack<String> singletonStack() {
		ArrayStack<String> stack = new ArrayStack<>();
		stack.push("A");
		return stack;
	}

	private Stack<String> emptyStack() {
		return new ArrayStack<>();
	}
	
	public static Test suite() {
		return new TestSuite(TestArrayStack.class);
	}
}
