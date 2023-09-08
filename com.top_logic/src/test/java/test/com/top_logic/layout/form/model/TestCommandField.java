/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;


import junit.framework.Test;

import test.com.top_logic.layout.basic.CommandHandlerCommandModelTest;
import test.com.top_logic.layout.form.PropertyEvent;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the executability and visibility aspect of the {@link CommandModel}
 * {@link CommandField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCommandField extends CommandHandlerCommandModelTest<CommandField> {

	private static final ThemeImage SOME_ICON = ThemeImage.forTest("/someKey");
	private CommandField commandField;
	private FormGroup group;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		group = new FormGroup("group", ResPrefix.forTest("i18nPrefix"));
		commandField = FormFactory.newCommandField("commandField", commandHandler, component, CommandHandler.NO_ARGS);
		group.addMember(commandField);
		addListener(commandField, listener);

	}
	
	@Override
	protected void tearDown() throws Exception {
		removeListener(commandField, listener);
		commandField = null;
		group = null;
		super.tearDown();
	}
	
	@Override
	public CommandField getCommandModel() {
		return commandField;
	}

	public void testInheritFromParent() {
		check(VISIBLE, EXECUTABLE);
		
		listener.clear();
		group.setDisabled(true);
		listener.assertCaptured(FormMember.DISABLED_PROPERTY, false, true);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, true, false);
		check(VISIBLE, NOT_EXECUTABLE);
		
		listener.clear();
		group.setVisible(false);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, true, false);
		check(HIDDEN, NOT_EXECUTABLE);
		
		listener.clear();
		group.setDisabled(false);
		check(HIDDEN, NOT_EXECUTABLE);

		listener.clear();
		group.setVisible(true);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, false, true);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, false, true);
		check(VISIBLE, EXECUTABLE);
	}

	public void testFormFieldAspects() {
		listener.clear();
		getCommandModel().setImmutable(true);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, true, false);
		check(VISIBLE, NOT_EXECUTABLE);

		getCommandModel().setDisabled(true);
		check(VISIBLE, NOT_EXECUTABLE);
		
		getCommandModel().setImmutable(false);
		check(VISIBLE, NOT_EXECUTABLE);
		
		listener.clear();
		getCommandModel().setDisabled(false);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, false, true);
		check(VISIBLE, EXECUTABLE);
		
		
		listener.clear();
		getCommandModel().setVisible(false);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, true, false);
		check(HIDDEN, NOT_EXECUTABLE);
		
		listener.clear();
		getCommandModel().setImmutable(true);
		check(HIDDEN, NOT_EXECUTABLE);
		
		listener.clear();
		getCommandModel().setVisible(true);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, false, true);
		check(VISIBLE, NOT_EXECUTABLE);
	}
	
	public void testImageChangeEvent() {
		listener.clear();

		final ThemeImage oldImage = getCommandModel().getImage();
		final ThemeImage newImage = SOME_ICON;
		getCommandModel().setImage(newImage);

		boolean imageChangeFound = false;
		for (PropertyEvent capturedEvent : listener.getCapturedEvents()) {
			if (capturedEvent.getType() == CommandField.IMAGE_PROPERTY) {
				imageChangeFound = true;
				assertEquals(oldImage, capturedEvent.getOldValue());
				assertEquals(newImage, capturedEvent.getNewValue());
			}
		}
		assertTrue("No ImageChange fired", imageChangeFound);
	}

	public void testEventHandling() {
		ExecutableState disabledState = ExecutableState.createDisabledState(ResKey.text("disabledI18N"));

		TestingExecutabilityModel execModel = new TestingExecutabilityModel(Command.DO_NOTHING);
		CommandField field = FormFactory.newCommandField("name", execModel);

		addListener(field, listener);
		execModel.setExecutability(disabledState);
		assertFalse(field.isExecutable());
		assertEquals(disabledState.getI18NReasonKey(), field.getNotExecutableReasonKey());

		execModel.setExecutability(ExecutableState.EXECUTABLE);
		assertTrue(field.isExecutable());
		removeListener(field, listener);
		
		execModel.setExecutability(disabledState);
		// Field has to bring itself in consistent state.
		addListener(field, listener);
		assertFalse("Ticket #17911: Field is observed. State must be consistent.", field.isExecutable());
		assertEquals(disabledState.getI18NReasonKey(), field.getNotExecutableReasonKey());
		removeListener(field, listener);

	}

	public static Test suite() {
		return suite(TestCommandField.class);
	}

}

