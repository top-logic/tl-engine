/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.lang.ref.WeakReference;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.util.DefaultValidationQueue;
import com.top_logic.util.ToBeValidated;

/**
 * Tests {@link DefaultValidationQueue}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultValidationQueue extends BasicTestCase {
	
	private class TestedValidation implements ToBeValidated {

		int reattach = 0;
		int called = 0;

		TestedValidation() {
			// nothing to to here
		}

		@Override
		public void validate(DisplayContext context) {
			called++;
			if (reattach > 0) {
				reattach--;
				_validationQueue.notifyInvalid(this);
			}
		}

	}

	DefaultValidationQueue _validationQueue;

	DisplayContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_validationQueue = new DefaultValidationQueue();
		_context = DummyDisplayContext.newInstance();
	}
	
	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_validationQueue = null;
		super.tearDown();
	}

	public void testEmpty() {
		_validationQueue.runValidation(_context);
	}
	
	public void testValidatedTwiceDirect() {
		TestedValidation o1 = new TestedValidation();
		o1.reattach = 1;
		_validationQueue.notifyInvalid(o1);
		TestedValidation o2 = new TestedValidation();
		_validationQueue.notifyInvalid(o2);

		_validationQueue.runValidation(_context);
		assertEquals(2, o1.called);
		assertEquals(1, o2.called);
	}

	public void testValidatedTwiceWithOtherBetween() {
		TestedValidation o1 = new TestedValidation();
		o1.reattach = 1;
		_validationQueue.notifyInvalid(o1);
		
		_validationQueue.runValidation(_context);
		assertEquals(2, o1.called);
	}

	public void testSimple() {
		TestedValidation o = new TestedValidation();
		_validationQueue.notifyInvalid(o);
		_validationQueue.runValidation(_context);
		assertEquals(1, o.called);

		o.called = 0;
		_validationQueue.runValidation(_context);
		assertEquals(0, o.called);
	}

	public void testValidationTwice() {
		TestedValidation o = new TestedValidation();
		_validationQueue.notifyInvalid(o);
		_validationQueue.runValidation(_context);
		assertEquals(1, o.called);
		_validationQueue.runValidation(_context);
		assertEquals(1, o.called);
	}

	public void testNotCached() {
		TestedValidation o = new TestedValidation();
		WeakReference<ToBeValidated> reference = new WeakReference<>(o);
		_validationQueue.notifyInvalid(o);
		_validationQueue.runValidation(_context);
		assertEquals(1, o.called);
		// kill reference to let GC remove the object
		o = null;
		Runtime.getRuntime().gc();
		assertNull(reference.get());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefaultValidationQueue.class);
	}

}

