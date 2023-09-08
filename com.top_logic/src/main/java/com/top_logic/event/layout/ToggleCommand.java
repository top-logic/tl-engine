/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} switching all {@link BooleanField}s in a specified {@link FormGroup} on or
 * off.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ToggleCommand extends AJAXCommandHandler {

    private boolean value;
    String groupName;

    public static final String ON="On";
    public static final String OFF="Off";

    public static final String RESOURCE_INFIX = "toggleCommand.";

	public interface Config extends AJAXCommandHandler.Config {
		String getFormGroupName();

		void setFormGroupName(String value);

		boolean getValue();

		void setValue(boolean value);
	}

	public ToggleCommand(InstantiationContext context, Config config) {
		super(context, config);
		value = config.getValue();
		groupName = config.getFormGroupName();
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        ConfigureLogEntriesComponent component = (ConfigureLogEntriesComponent) aComponent;
		FormContext theContext = component.getFormContext();
        FormGroup     theGroup = (FormGroup)theContext.getMember(groupName);
		setFields(component, theGroup);
        return HandlerResult.DEFAULT_RESULT;
    }

	protected void setFields(ConfigureLogEntriesComponent component, FormGroup group) {
		Iterator<BooleanField> iter = component.getBooleanFields(group);
        while(iter.hasNext()){
			iter.next().setAsBoolean(value);
        }
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return value ? I18NConstants.SWITCH_ON : I18NConstants.SWITCH_OFF;
    }

	public static CommandHandler newInstance(String formGroupName, boolean value) {
		Config config = createConfig(ToggleCommand.class, createCommandId(formGroupName, value));
		config.setFormGroupName(formGroupName);
		config.setValue(value);
		return newInstance(config);
	}

	protected static String createCommandId(String formGroupName, boolean value) {
		return formGroupName + (value ? ON : OFF);
	}

}
