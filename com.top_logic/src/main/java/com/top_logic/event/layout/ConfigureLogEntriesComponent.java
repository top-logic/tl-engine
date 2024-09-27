/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.logEntry.LogEntryDisplayGroup;
import com.top_logic.event.logEntry.LogEntryFilter;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ConfigureLogEntriesComponent  extends FormComponent {
    
	/**
	 * Configuration for the {@link ConfigureLogEntriesComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ConfigureLogEntriesCommandHandler.COMMAND_ID);
			// registry.registerCommandHandler(CancelHandler.COMMAND_ID);

			registry.registerButton(ToggleAllCommand.createCommandId(true));
			registry.registerButton(ToggleAllCommand.createCommandId(false));
		}

	}
    
    public ConfigureLogEntriesComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    public Set getAllowedDisplayGroups() {
        return ((LogEntryFilterComponent) getDialogParent()).getAllowedDisplayGroups();
    }
    
    @Override
	public FormContext createFormContext() {             
        Set                 theUserEntries = null;
        
        LogEntryFilter  theModel = (LogEntryFilter) getModel();
        if (theModel == null) {
            return null; 
        }

        theUserEntries = theModel.getEventTypes();

        if (theUserEntries == null) {
            theUserEntries = Collections.EMPTY_SET;
        }
        
		FormContext theContext = new FormContext("FormContext", getResPrefix());

        for (Iterator theIt = this.getAllowedDisplayGroups().iterator(); theIt.hasNext(); ) {
            LogEntryDisplayGroup theDisplayGroup = (LogEntryDisplayGroup) theIt.next();
            // convert the display group name to a form field name, e.g. replace "."
            FormGroup theFormGroup = new FormGroup(convertToFieldname(theDisplayGroup.getKey()), this.getResPrefix());

            for (Iterator theElements = theDisplayGroup.getEventTypes().iterator(); theElements.hasNext(); ) {
                String theEventType  = (String) theElements.next();
                String           theKey   = theDisplayGroup.getName() + "." + theEventType;
                boolean          theValue = theUserEntries.contains(theKey);
                BooleanField     theField = FormFactory.newBooleanField(convertToFieldname(theDisplayGroup.getKey(theEventType)), Boolean.valueOf(theValue), false);

                theFormGroup.addMember(theField);
            }
			addToggleCommand(theFormGroup, true);
			addToggleCommand(theFormGroup, false);

            theContext.addMember(theFormGroup);
        }    
        
        return theContext;   
    }

	private void addToggleCommand(FormGroup group, boolean value) {
		ResKey label =
			getResPrefix().append(ToggleCommand.RESOURCE_INFIX).key(value ? ToggleCommand.ON : ToggleCommand.OFF);
		String groupName = group.getName();
		CommandHandler command = ToggleCommand.newInstance(groupName, value);
		CommandField commandField = FormFactory.newCommandField(command.getID(), command, this);
		commandField.setLabel(label);
		group.addMember(commandField);
	}

    public String convertFromFieldname(String aFieldname) {
        return aFieldname.replaceAll("_", ".");
    }
    
    public String convertToFieldname(String aString) {
        return aString.replaceAll("\\.", "_");
    }
    
	public Iterator<BooleanField> getBooleanFields(FormGroup group) {
		return FilterUtil.filterIterator(BooleanField.class, group.getMembers());
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return true;
    }

    @Override
	protected void becomingInvisible() {
        super.becomingInvisible();
        removeFormContext();
    }

}
