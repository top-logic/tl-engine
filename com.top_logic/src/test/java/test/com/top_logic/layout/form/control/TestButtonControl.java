/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.control;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.ActivateCommand;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Test of {@link ButtonControl}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestButtonControl extends TestControl {

	private static final String ACTIVATE_COMMAND_ID = ActivateCommand.INSTANCE.getID();

	MutableInteger _executeCounter;

	private CommandModel _model;

	private ButtonControl _button;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_executeCounter = new MutableInteger();
		_model = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				_executeCounter.inc();
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		_button = new ButtonControl(_model);

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		_button.detach();
		_button = null;
		_model = null;
		_executeCounter = null;
	}

	public void testNoExecutionDuringConstruction() {
		writeControl(_button);

		assertEquals("command must not be executed during construction", 0, _executeCounter.intValue());
	}

	public void testExecution() {
		writeControl(_button);
		
		executeButton();
		assertEquals("command was not executed once", 1, _executeCounter.intValue());
		executeButton();
		assertEquals("command was not executed twice", 2, _executeCounter.intValue());
	}

	private HandlerResult executeButton() {
		return executeControlCommand(_button, ACTIVATE_COMMAND_ID, Collections.<String, Object> emptyMap());
	}

	public void testSetDisabled() {
		writeControl(_button);

		_button.disable(Resources.encodeLiteralText("some reason"));
		assertTrue(_button.isDisabled());
		HandlerResult errorResult = executeButton();
		assertEquals("command was executed whereas it was disabled", 0, _executeCounter.intValue());
		assertFalse("command not executed but result has no error", errorResult.isSuccess());

		_button.enable();
		assertFalse(_button.isDisabled());
		HandlerResult successfulResult = executeButton();
		assertEquals("command was executed whereas it was reenabled", 1, _executeCounter.intValue());
		assertTrue("execution successful but result has error", successfulResult.isSuccess());
	}

	public void testSetInvisible() {
		writeControl(_button);

		_button.setVisible(false);
		assertFalse(_button.isVisible());
		HandlerResult errorResult = executeButton();
		assertEquals("command was executed whereas it was invisible", 0, _executeCounter.intValue());
		assertFalse("command not executed but result has no error", errorResult.isSuccess());
		assertTrue("Ticket #18399: Switching visibilty of button must force update of client.", _button.isInvalid());

		writeControl(_button);

		_button.setVisible(true);
		assertTrue(_button.isVisible());
		HandlerResult successfulResult = executeButton();
		assertEquals("command was executed whereas it was visible", 1, _executeCounter.intValue());
		assertTrue("execution successful but result has error", successfulResult.isSuccess());
		assertTrue("Ticket #18399: Switching visibilty of button must force update of client.", _button.isInvalid());
	}

	public void testPortedModelProps() {
		writeControl(_button);

		// check initial properties
		assertTrue(_model.isExecutable());
		assertTrue(_model.isVisible());
		assertTrue(_button.isVisible());
		assertFalse(_button.isDisabled());

		_model.setNotExecutable(Resources.encodeLiteralText("some reason"));
		assertFalse(_model.isExecutable());
		assertTrue("model not executable but button enabled", _button.isDisabled());
		assertTrue("model visible but button invisible", _button.isVisible());

		_model.setExecutable();
		assertTrue(_model.isExecutable());
		assertFalse("model executable but button disabled", _button.isDisabled());
		assertTrue("model visible but button invisible", _button.isVisible());
		
		_model.setVisible(false);
		assertFalse(_model.isVisible());
		// Don't care about disabled state if button is invisible
		assertFalse("model not visible but button", _button.isVisible());

		_model.setVisible(true);
		assertTrue(_model.isVisible());
		assertTrue("model visible but button invisible", _button.isVisible());
		assertFalse("model has turned invisible and visible again, but button is disabled", _button.isDisabled());
	}

	public static Test suite() {
		return TestControl.suite(TestButtonControl.class);
	}
}
