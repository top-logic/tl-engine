/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A component implementing the {@link FormMutator} interface is a helper for
 * filling a single {@link FormField} of a {@link FormComponent} with values, or
 * modifying its values.
 * 
 * <p>
 * A {@link FormMutator} is best opened in a dialog, or separate window. 
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormMutator {
	
	/**
	 * Called during the opening process of this mutator to associate it with
	 * its target {@link FormComponent} and {@link FormField}.
	 * 
	 * @param formHandler
	 *     Value that must be returned from future calls to {@link #getFormHandler()}.
	 * @param targetField
	 *     Value that must be returned from future calls to {@link #getTargetField()}.
	 */
	public void initContext(FormHandler formHandler, FormField targetField);

	/**
	 * The {@link FormHandler}, whose {@link #getTargetField() field} is
	 * modified by this mutator.
	 */
	public FormHandler getFormHandler();

	/**
	 * The filled or modified field of this mutator's
	 * {@link #getFormHandler() form}.
	 */
	public FormField getTargetField();

	
	/**
	 * Command that opens a {@link FormMutator} in a separate window.
	 * 
	 * @author <a href=mailto:twi@top-logic.com>twi</a>
	 */
	public static abstract class OpenPopup extends OpenWindowCommand {
	
		private FormHandler formHander;
		
		private FormField targetField;
	
	    private LayoutComponent popupParent;
	
		public OpenPopup(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		public void setPopupParent(LayoutComponent parent) {
	        this.popupParent = parent;
		}
		
		/**
		 * TODO BHU: Replace this fragile mechanism of transporting an additional
		 * parameter into a command handler with something better.
		 */
		public void setFieldArgument(FormField targetField) {
			this.targetField = targetField;
		}
	
		/**
		 * TODO BHU: Replace this fragile mechanism of transporting an additional
		 * parameter into a command handler with something better.
		 */
		public void setFormArgument(FormHandler formHandler) {
			this.formHander = formHandler;
		}
	
		@Override
		protected void initWindow(DisplayContext context, LayoutComponent opener, WindowComponent window, Map<String, Object> args) {
			LayoutComponent mutatorComponent = lookupMutatorComponent(window);
	        assert window.getChildCount() == 1 : 
		        "The component opened in a window is the only child of it.";

	        // TODO BHU: Necessary?
	        window.setOpener(popupParent);     
	        
		    ((FormMutator) mutatorComponent).initContext(formHander, targetField);
		    
			if (this.popupParent instanceof EditComponent && mutatorComponent instanceof ChannelListener) {
		       // register a ComponentModeListener to react on changes of the editmode of an EditComponent
		       EditComponent r = ((EditComponent)this.popupParent);
				r.editModeChannel().addListener((ChannelListener) mutatorComponent);
		   }	
	    }

	    protected abstract LayoutComponent lookupMutatorComponent(WindowComponent wrappingComponent);
	}

}
