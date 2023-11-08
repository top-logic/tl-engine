/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.base.services.simpleajax.NothingCommand;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * The class {@link DemoButtonComponent} is a demo class for toggling buttons in a
 * {@link ButtonComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoButtonComponent extends FormComponent {

	public static final String GROUP_NAME = "toggleCommands";
	public static final String TOGGLE_BUTTONS = "toggleButtons";

	public DemoButtonComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		FormContext ctx = new FormContext(this);
		FormGroup grp = new FormGroup(GROUP_NAME, getResPrefix());
		ctx.addMember(grp);
		final List<CommandModel> commands = new ArrayList<>();
		FormField theField = FormFactory.newBooleanField(TOGGLE_BUTTONS);
		theField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean buttonsVisible = ((Boolean) newValue).booleanValue();
				getButtonComponent().setTransientButtons(buttonsVisible ? commands : null);
			}
		});
		grp.addMember(theField);

		FormField toggleToolbarField = FormFactory.newBooleanField("toggleToolbar");
		toggleToolbarField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean buttonsVisible = ((Boolean) newValue).booleanValue();
				if (buttonsVisible) {
					getToolBar().defineGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP).addButtons(commands);
				} else {
					getToolBar().removeGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP);
				}
			}
		});
		grp.addMember(toggleToolbarField);

		FormField toggleMenuField = FormFactory.newBooleanField("toggleMenu");
		toggleMenuField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean buttonsVisible = ((Boolean) newValue).booleanValue();
				if (buttonsVisible) {
					getToolBar().defineGroup(CommandHandlerFactory.ADDITIONAL_GROUP).addButtons(commands);
				} else {
					getToolBar().removeGroup(CommandHandlerFactory.ADDITIONAL_GROUP);
				}
			}

		});
		grp.addMember(toggleMenuField);

		CommandModel model1 = CommandModelFactory.commandModel(NothingCommand.INSTANCE, this, ResKey.text("Kommando1"));
		CommandModel model2 = CommandModelFactory.commandModel(NothingCommand.INSTANCE, this, ResKey.text("Kommando2"));
		CommandModel model3 = CommandModelFactory.commandModel(NothingCommand.INSTANCE, this, ResKey.text("Kommando3"));
		commands.add(model1);
		addToogleCommandFields(grp, model1, I18NConstants.DEMO_BUTTON_COMPONENT_BUTTON1_NOT_EXECUTABLE);
		commands.add(model2);
		addToogleCommandFields(grp, model2, I18NConstants.DEMO_BUTTON_COMPONENT_BUTTON2_NOT_EXECUTABLE);
		commands.add(model3);
		addToogleCommandFields(grp, model3,
			I18NConstants.DEMO_BUTTON_COMPONENT_BUTTON3_NOT_EXECUTABLE__TIME.fill(new Date()));

		return ctx;
	}

	/**
	 * This method adds two {@link CommandField}s to the given parent. The first toggles the
	 * visibility of the given {@link CommandModel}, the second toggles the executability.
	 */
	private void addToogleCommandFields(FormContainer parent, final CommandModel model, final ResKey notExecutableReason) {
		BooleanField visibility =
			FormFactory.newBooleanField(parent.getName() + "_" + model.getLabel() + "_visible", Boolean.TRUE, false);
		parent.addMember(visibility);
		visibility.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean visible = ((Boolean) newValue).booleanValue();
				model.setVisible(visible);
			}
		});
		BooleanField executability =
			FormFactory.newBooleanField(parent.getName() + "_" + model.getLabel() + "_executable", Boolean.TRUE, false);
		parent.addMember(executability);
		executability.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean executable = ((Boolean) newValue).booleanValue();
				if (executable) {
					model.setExecutable();
				} else {
					model.setNotExecutable(notExecutableReason);
				}
			}
		});
	}

}
