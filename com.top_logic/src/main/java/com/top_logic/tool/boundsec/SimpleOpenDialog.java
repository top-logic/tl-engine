/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Command handler for opening a dialog with a specific component and model.
 * 
 * The handler will look in the given arguments for the following values:
 * 
 * <ul>
 * <li>{@link #DIALOG_NAME}: Name of the dialog to be opened.</li>
 * <li>{@link #COMPONENT_NAME}: Business component within the opened dialog.</li>
 * <li>{@link #MODEL}: Model to be set into the business component.</li>
 * </ul>
 * 
 * @author <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class SimpleOpenDialog extends OpenModalDialogCommandHandler {

	/**
	 * Key for the arguments in
	 * {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}. Value is the
	 * {@link ComponentName} (or the serialised variant) of the dialog to open, or
	 * <code>null</code>.
	 */
    public static final String DIALOG_NAME = "dialog";
    
	/**
	 * Key for the arguments in
	 * {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}. Value is the
	 * {@link ComponentName} (or the serialised variant) of the business component within the dialog
	 * to set model to, or <code>null</code>.
	 */
    public static final String COMPONENT_NAME = "component";
    
    public static final String MODEL = "model";
    
    public static final String COMMAND_ID = "simpleDialog";

	public SimpleOpenDialog(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * @see com.top_logic.tool.boundsec.OpenModalDialogCommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        HandlerResult   theResult = super.handleCommand(aContext, aComponent, model, someArguments);

        if (theResult.isSuccess()) {
            LayoutComponent theDialog = this.getDialog(aComponent, someArguments);
            Object          theObject = someArguments.get(SimpleOpenDialog.MODEL);
			LayoutComponent theComp = theDialog.getComponentByName(getComponentName(someArguments));

			if (theComp == null) {
            	HandlerResult result = new HandlerResult();
				result.addErrorText("Fail to prepare display for " + theObject);
                
                return result;
            }
			theComp.setModel(theObject);
        }

        return theResult;
    }

    /**
     * The dialog (-name) to be opened will be taken out of the given arguments.
     * 
     * @see com.top_logic.tool.boundsec.OpenModalDialogCommandHandler#getDialog(com.top_logic.mig.html.layout.LayoutComponent, java.util.Map)
     */
    @Override
	protected LayoutComponent getDialog(LayoutComponent component, Map<String, Object> someArguments) {
		ComponentName dialogName = getDialogName(someArguments);
		if (dialogName == null) {
			return super.getDialog(component, someArguments);
		}
		LayoutComponent dialog = component.getDialog(dialogName);
        if (dialog == null) {
			throw new NullPointerException("Dialog with Name '" + dialogName + "' not found.");
        }
        return dialog;
    }

	/**
	 * Determines the {@link ComponentName} set for property {@link #DIALOG_NAME} from the given
	 * arguments.
	 * 
	 * @return May be <code>null</code>.
	 */
	public static ComponentName getDialogName(Map<String, Object> someArguments) {
		return componentName(someArguments, SimpleOpenDialog.DIALOG_NAME);
	}

	/**
	 * Determines the {@link ComponentName} set for property {@link #COMPONENT_NAME} from the given
	 * arguments.
	 * 
	 * @return May be <code>null</code>.
	 */
	public static ComponentName getComponentName(Map<String, Object> someArguments) {
		return componentName(someArguments, SimpleOpenDialog.COMPONENT_NAME);
	}

	private static ComponentName componentName(Map<String, Object> someArguments, String property) {
		Object dialogName = someArguments.get(property);
		if (dialogName == null) {
			return null;
		}
		if (dialogName instanceof ComponentName) {
			return (ComponentName) dialogName;
		} else {
			StringBuilder error = new StringBuilder();
			error.append("Illegal value of type '");
			error.append(dialogName.getClass().getName());
			error.append("' for property '");
			error.append(property);
			error.append("' (");
			error.append(dialogName);
			error.append("). Expected null or ");
			error.append(ComponentName.class.getName());
			error.append(".");
			throw new IllegalArgumentException(error.toString());
		}
	}
}
