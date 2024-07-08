/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ToggleAllCommand extends ToggleCommand {

    public ToggleAllCommand(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        ConfigureLogEntriesComponent component = (ConfigureLogEntriesComponent) aComponent;
		FormContext theContext = component.getFormContext();

		for (Iterator<? extends FormMember> theGroups = theContext.getMembers(); theGroups.hasNext();) {
			this.setFields(component, (FormGroup) theGroups.next());
        }

        return HandlerResult.DEFAULT_RESULT;
    }

	static String createCommandId(boolean value) {
		return createCommandId("All", value);
	}
}
