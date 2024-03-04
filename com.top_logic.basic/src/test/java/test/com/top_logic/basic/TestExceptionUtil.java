/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.ExceptionUtil.*;

import java.util.Arrays;
import java.util.NoSuchElementException;

import jakarta.servlet.ServletException;

import junit.framework.TestCase;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.MultiError;

/**
 * Tests for: {@link ExceptionUtil}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestExceptionUtil extends TestCase {

	public void testPrintThrowableToString() {
		String message1 = "Outer Exception";
		String message2 = "Inner Exception";
		String message3 = "Innermost";
		Throwable exception3 = new NoSuchElementException(message3);
		Throwable exception2 = new IllegalStateException(message2, exception3);
		Throwable exception1 = new RuntimeException(message1, exception2);
		String printedException = printThrowableToString(exception1);
		
		// It would be to complicated to check for the exact content.
		// Therefore, we check only multiple "hints" if it works.
		assertContains(printedException, message1);
		assertContains(printedException, message2);
		assertContains(printedException, message3);
		assertContains(printedException, exception1.getClass().getCanonicalName());
		assertContains(printedException, exception2.getClass().getCanonicalName());
		assertContains(printedException, exception3.getClass().getCanonicalName());
		assertContains(printedException, this.getClass().getCanonicalName()); // Appears within
																				// Stacktrace
	}

	public void testCreateMultiErrorWithArray() {
		MultiError multiError = createMultiError("Outer Message", new RuntimeException(), new IllegalStateException());
		assertNotNull(multiError);
		assertContains(multiError.getMessage(), "Outer Message");
		assertTrue(multiError.getErrors().get(0) instanceof RuntimeException);
		assertTrue(multiError.getErrors().get(1) instanceof IllegalStateException);
	}

	public void testCreateMultiErrorWithList() {
		MultiError multiError = createMultiError("Outer Message", Arrays.<Throwable>asList(new RuntimeException(), new IllegalStateException()));
		assertNotNull(multiError);
		assertContains(multiError.getMessage(), "Outer Message");
		assertTrue(multiError.getErrors().get(0) instanceof RuntimeException);
		assertTrue(multiError.getErrors().get(1) instanceof IllegalStateException);
	}
	
	public void testGetMessage() {
		assertEquals("", getMessage(null));
		assertEquals("", getMessage(new RuntimeException((String) null)));
		assertEquals("Test Message", getMessage(new RuntimeException("Test Message")));
	}

	public void testGetFullMessage() {
		assertEquals("null", getFullMessage(null));
		
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException()));
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException((String) null)));
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException("")));
		assertEquals("java.lang.RuntimeException: Outer Message", getFullMessage(new RuntimeException("Outer Message")));
		
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException((Throwable) null)));
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException((String) null, null)));
		assertEquals("java.lang.RuntimeException: ", getFullMessage(new RuntimeException("", null)));
		assertEquals("java.lang.RuntimeException: Outer Message", getFullMessage(new RuntimeException("Outer Message", null)));
		
		assertEquals("java.lang.RuntimeException: java.lang.IllegalStateException", getFullMessage(new RuntimeException(new IllegalStateException())));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException((String) null, new IllegalStateException())));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("", new IllegalStateException())));
		assertEquals("java.lang.RuntimeException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("Outer Message", new IllegalStateException())));
		
		assertEquals("java.lang.RuntimeException: java.lang.IllegalStateException", getFullMessage(new RuntimeException(new IllegalStateException((String) null))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException((String) null, new IllegalStateException((String) null))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("", new IllegalStateException((String) null))));
		assertEquals("java.lang.RuntimeException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("Outer Message", new IllegalStateException((String) null))));
		
		assertEquals("java.lang.RuntimeException: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException(new IllegalStateException(""))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException((String) null, new IllegalStateException(""))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("", new IllegalStateException(""))));
		assertEquals("java.lang.RuntimeException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new RuntimeException("Outer Message", new IllegalStateException(""))));
		
		assertEquals("java.lang.RuntimeException: java.lang.IllegalStateException: Inner Message", getFullMessage(new RuntimeException(new IllegalStateException("Inner Message"))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new RuntimeException((String) null, new IllegalStateException("Inner Message"))));
		assertEquals("java.lang.RuntimeException:  Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new RuntimeException("", new IllegalStateException("Inner Message"))));
		assertEquals("java.lang.RuntimeException: Outer Message Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new RuntimeException("Outer Message", new IllegalStateException("Inner Message"))));
		
	}
	
	public void testGetFullMessageWithServletException() {
		
		assertEquals("jakarta.servlet.ServletException: ", getFullMessage(new ServletException()));
		assertEquals("jakarta.servlet.ServletException: ", getFullMessage(new ServletException((String)null)));
		assertEquals("jakarta.servlet.ServletException: ", getFullMessage(new ServletException("")));
		assertEquals("jakarta.servlet.ServletException: Outer Message", getFullMessage(new ServletException("Outer Message")));
		
		// Throws an NPE within ServletException itself:
		// assertEquals("jakarta.servlet.ServletException", getFullMessage(new ServletException((Throwable)null)));
		assertEquals("jakarta.servlet.ServletException: ", getFullMessage(new ServletException((String)null, null)));
		assertEquals("jakarta.servlet.ServletException: ", getFullMessage(new ServletException("", null)));
		assertEquals("jakarta.servlet.ServletException: Outer Message", getFullMessage(new ServletException("Outer Message", null)));
		
		assertEquals("jakarta.servlet.ServletException: java.lang.IllegalStateException", getFullMessage(new ServletException(new IllegalStateException())));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException((String)null, new IllegalStateException())));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("", new IllegalStateException())));
		assertEquals("jakarta.servlet.ServletException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("Outer Message", new IllegalStateException())));
		
		assertEquals("jakarta.servlet.ServletException: java.lang.IllegalStateException", getFullMessage(new ServletException(new IllegalStateException((String)null))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException((String)null, new IllegalStateException((String)null))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("", new IllegalStateException((String)null))));
		assertEquals("jakarta.servlet.ServletException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("Outer Message", new IllegalStateException((String)null))));
		
		assertEquals("jakarta.servlet.ServletException: java.lang.IllegalStateException: ", getFullMessage(new ServletException(new IllegalStateException(""))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException((String)null, new IllegalStateException(""))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("", new IllegalStateException(""))));
		assertEquals("jakarta.servlet.ServletException: Outer Message Caused by: java.lang.IllegalStateException: ", getFullMessage(new ServletException("Outer Message", new IllegalStateException(""))));
		
		assertEquals("jakarta.servlet.ServletException: java.lang.IllegalStateException: Inner Message", getFullMessage(new ServletException(new IllegalStateException("Inner Message"))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new ServletException((String)null, new IllegalStateException("Inner Message"))));
		assertEquals("jakarta.servlet.ServletException:  Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new ServletException("", new IllegalStateException("Inner Message"))));
		assertEquals("jakarta.servlet.ServletException: Outer Message Caused by: java.lang.IllegalStateException: Inner Message", getFullMessage(new ServletException("Outer Message", new IllegalStateException("Inner Message"))));
		
	}
	
	private static void assertContains(String actualString, CharSequence... expectedParts) {
		assertTrue((expectedParts != null) && (expectedParts.length > 0));
		for (CharSequence expectedPart : expectedParts) {
			assertTrue(actualString.contains(expectedPart));
		}
	}

}
