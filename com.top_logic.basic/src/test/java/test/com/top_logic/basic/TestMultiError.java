/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.basic.MultiError;

/**
 * Tests for: {@link MultiError}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestMultiError extends TestCase {
	
	@SuppressWarnings("unused")
	public void testNoSubErrors() {
		try {
			new MultiError("Test", Collections.<Throwable>emptyList());
			fail();
		} catch (IllegalArgumentException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testNoSubErrorsWithArray() {
		try {
			new MultiError("Test");
			fail();
		} catch (IllegalArgumentException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testSingleSubError() {
		try {
			new MultiError("Test", Collections.<Throwable>singletonList(new RuntimeException()));
			fail();
		} catch (IllegalArgumentException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testSingleSubErrorWithArray() {
		try {
			new MultiError("Test", new RuntimeException());
			fail();
		} catch (IllegalArgumentException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testNullMessage() {
		try {
			new MultiError(null, Arrays.<Throwable>asList(new RuntimeException(), new IllegalStateException()));
			fail();
		} catch (NullPointerException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testNullMessageWithArray() {
		try {
			new MultiError(null, new RuntimeException(), new IllegalStateException());
			fail();
		} catch (NullPointerException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testNullError() {
		try {
			new MultiError(null, Arrays.<Throwable>asList(new RuntimeException(), null, new IllegalStateException()));
			fail();
		} catch (NullPointerException expectedException) {
			// Correct
		}
	}
	
	@SuppressWarnings("unused")
	public void testNullErrorWithArray() {
		try {
			new MultiError(null, new RuntimeException(), null, new IllegalStateException());
			fail();
		} catch (NullPointerException expectedException) {
			// Correct
		}
	}
	
	public void testValidConstruction() {
		MultiError multiError = new MultiError("Outer Message", Arrays.<Throwable>asList(new RuntimeException(), new IllegalStateException()));
		assertNotNull(multiError);
		assertTrue(multiError.getMessage().contains("Outer Message"));
		assertTrue(multiError.getErrors().get(0) instanceof RuntimeException);
		assertTrue(multiError.getErrors().get(1) instanceof IllegalStateException);
	}
	
	public void testValidConstructionWithArray() {
		MultiError multiError = new MultiError("Outer Message", new RuntimeException(), new IllegalStateException());
		assertNotNull(multiError);
		assertTrue(multiError.getMessage().contains("Outer Message"));
		assertTrue(multiError.getErrors().get(0) instanceof RuntimeException);
		assertTrue(multiError.getErrors().get(1) instanceof IllegalStateException);
	}
	
}
