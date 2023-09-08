/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.control;

import junit.framework.Test;
import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Tests {@link OnVisibleControl}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestOnVisibleControl extends TestControl {

	private Control _control;

	private FormMember _member;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_member = FormFactory.newStringField("member", null, false);
		_control = new OnVisibleControl(_member);
	}

	@Override
	protected void tearDown() throws Exception {
		_control.detach();
		_control = null;
		_member = null;
		super.tearDown();
	}

	public void testNoNullMember() {
		try {
			final OnVisibleControl illegalControl = new OnVisibleControl(null);
			writeControl(illegalControl);
			fail("Control which reacts on 'null'-member seems to be strange");
		} catch (RuntimeException ex) {
			// expected
		}
	}

	public void testFieldChangedVisibility() {
		writeControl(_control);

		assertTrue(_member.isVisible());
		assertTrue(_control.isVisible());

		_member.setVisible(false);
		assertFalse(_member.isVisible());
		assertFalse(_control.isVisible());

		_member.setVisible(true);
		assertTrue(_member.isVisible());
		assertTrue(_control.isVisible());
	}

	public void testCorrectVisiblityAfterDetach() {
		writeControl(_control);

		assertTrue(_member.isVisible());
		assertTrue(_control.isVisible());

		_control.detach();

		_member.setVisible(false);
		// must first write as no one can expect that a detached control has the correct state
		writeControl(_control);
		assertFalse("After writing the invisible member, the control is visible", _control.isVisible());

		_control.detach();

		_member.setVisible(true);
		// must first write as no one can expect that a detached control has the correct state
		writeControl(_control);
		assertTrue("After writing the visible member, the control is invisible", _control.isVisible());
	}

	public static Test suite() {
		return TestControl.suite(TestOnVisibleControl.class);
	}

}
