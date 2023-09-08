/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic.component;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.layout.DummyUpdateListener;

import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.component.AJAXControlSupport;
import com.top_logic.layout.basic.component.AJAXSupport;

/**
 * The class {@link TestAJAXControlSupport} tests the {@link AJAXControlSupport}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestAJAXControlSupport extends TestCase {

	public void testConstructor() {
		try {
			createAJAXControlSupport(null);
			fail("additional AJAXSupport must not be null!");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testStartRendering() {
		final boolean[] succeded = new boolean[1];
		AJAXControlSupport support = createAJAXControlSupport(new AJAXSupport() {

			@Override
			public boolean isRevalidateRequested() {
				return false;
			}

			@Override
			public void revalidate(DisplayContext context, UpdateQueue actions) {
			}

			@Override
			public void startRendering() {
				succeded[0] = true;
			}

			@Override
			public void invalidate() {
			}

		});
		support.startRendering();
		assertTrue(succeded[0]);
	}

	public void testValidation() {
		// Test AJAXSupport invalid, ControlSupport aspect valid
		AJAXSupport additionalAJAXSupport = createAJAXSupport(true);
		AJAXControlSupport support = createAJAXControlSupport(additionalAJAXSupport);
		assertTrue(support.isRevalidateRequested());
		support.revalidate(DummyDisplayContext.newInstance(), new DefaultUpdateQueue());
		assertFalse(support.isRevalidateRequested());
		assertFalse(additionalAJAXSupport.isRevalidateRequested());

		// Test AJAXSupport valid, ControlSupport aspect invalid
		additionalAJAXSupport = createAJAXSupport(false);
		support = createAJAXControlSupport(additionalAJAXSupport);
		DummyUpdateListener invalidListener = new DummyUpdateListener();
		invalidListener.setInvalid(true);
		support.addUpdateListener(invalidListener);
		assertTrue(support.isRevalidateRequested());
		support.revalidate(DummyDisplayContext.newInstance(), new DefaultUpdateQueue());
		assertFalse(support.isRevalidateRequested());

		// Test AJAXSupport invalid, ControlSupport aspect invalid
		additionalAJAXSupport = createAJAXSupport(true);
		support = createAJAXControlSupport(additionalAJAXSupport);
		invalidListener.setInvalid(true);
		support.addUpdateListener(invalidListener);
		support.revalidate(DummyDisplayContext.newInstance(), new DefaultUpdateQueue());
		assertFalse(additionalAJAXSupport.isRevalidateRequested());
		assertFalse(support.isRevalidateRequested());
		assertFalse(invalidListener.isInvalid());
	}

	private AJAXControlSupport createAJAXControlSupport(AJAXSupport additionalSupport) {
		return new AJAXControlSupport(ComponentTestUtils.newSimpleComponent("header", "content"), additionalSupport);
	}

	private AJAXSupport createAJAXSupport(final boolean revalidateRequested) {
		return new AJAXSupport() {

			boolean invalid = revalidateRequested;

			@Override
			public boolean isRevalidateRequested() {
				return invalid;
			}

			@Override
			public void revalidate(DisplayContext context, UpdateQueue actions) {
				invalid = false;
			}

			@Override
			public void startRendering() {
			}

			@Override
			public void invalidate() {
			}

		};
	}

	/**
	 * the suite of Tests to execute
	 */
	static public Test suite() {
		Test suite = new TestSuite(TestAJAXControlSupport.class);
		return TLTestSetup.createTLTestSetup(suite);
	}

}
