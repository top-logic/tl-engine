/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.logEntry.LogEntryDisplayGroup;
import com.top_logic.event.logEntry.LogEntryFilter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ConfigureLogEntriesCommandHandler  extends CloseModalDialogCommandHandler {
    
	public static final String  COMMAND_ID  = "configureLogEntries";

	
	
	public ConfigureLogEntriesCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(DisplayContext,
     *      com.top_logic.mig.html.layout.LayoutComponent,
     *      Object, Map)
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        ConfigureLogEntriesComponent theComponent = (ConfigureLogEntriesComponent) aComponent;
        FormContext   theContext   = theComponent.getFormContext();
        
        LogEntryFilter theModel  = null;
        while (aComponent != null) {
            Object someModel = aComponent.getModel();
    
            if (someModel instanceof LogEntryFilter) {
				theModel = (LogEntryFilter) someModel;
                break;
            } else {
                aComponent = aComponent.getMaster();
            }
        }
         

        if (theModel != null && (theContext != null) && theContext.checkAll()) {
            List                theList        = new ArrayList();
            
            for (Iterator theIt = theComponent.getAllowedDisplayGroups().iterator(); theIt.hasNext(); ) {
                LogEntryDisplayGroup theGroup = (LogEntryDisplayGroup) theIt.next();
                FormGroup formGroup = (FormGroup)theContext.getMember(theComponent.convertToFieldname(theGroup.getKey()));

				Iterator<BooleanField> iter = theComponent.getBooleanFields(formGroup);
				while (iter.hasNext()) {
					BooleanField field = iter.next();
					if (field.getAsBoolean()) {
						theList.add(theComponent.convertFromFieldname(field.getName()));
					}
				}
            }    

			ComponentName componentName = aComponent.getName();
            LayoutComponent dialogParent = aComponent.getDialogParent();
			if (dialogParent != null) {
            	componentName = dialogParent.getName();
				if (dialogParent instanceof LogEntryFilterComponent) {
					((LogEntryFilterComponent) dialogParent).updateModel();
				}
				dialogParent.invalidate();
            }
			theModel.setConfiguredEventTypes(componentName, theList);
        }

		// Note: aComponent is modified above but saved to another variable before. Therefore, the
		// original component must be passed to the super implementation.
		return super.handleCommand(aContext, theComponent, model, someArguments);
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return (I18NConstants.SAVE_LOGENTRY_CONFIG);
    }
}
