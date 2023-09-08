/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * TODO TBE this class
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PublishableNewQueryComponent extends NewQueryComponent {
	
	/**
	 * Configuration for the {@link PublishableNewQueryComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends NewQueryComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@StringDefault(PublishableNewQueryCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			NewQueryComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	// C'tor
	public PublishableNewQueryComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
	    super(context, someAttrs);
    }

	@Override
	public FormContext createFormContext() {
		FormContext theContext = super.createFormContext();
		theContext = PublishableFieldSupport.createFormContext(this, theContext, this.getResPrefix());	    
		return theContext;
	}
	
    @Override
	public boolean isExpanded() {
		return allow(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
    }

    public static class PublishableNewQueryCommandHandler extends NewQueryCommandHandler {
    	
        public static final ExecutableState INVALID_FORM_CONTEXT 
            = ExecutableState.createDisabledState(I18NConstants.ERROR_INVALID_INPUT);

    	/** The ID of this command handler. */
        public static final String COMMAND_ID = "publishStoredQuery";
        
        
        
        public PublishableNewQueryCommandHandler(InstantiationContext context, Config config) {
        	super(context, config);
        }
        
        @Override
		protected StoredQuery createNewQuery(FormContainer context, LayoutComponent component) throws Exception {
			StoredQuery theQuery = super.createNewQuery(context, component);
			if (theQuery != null) {
				publish(theQuery, context);
			}
			return theQuery;
		}
        
        /**
         * Should not be needed as {@link OpenDialogOnValidFormContextHandler} should check, first.
         */
        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return DialogParentHasValidFormContext.INSTANCE;
        }
        
        static class DialogParentHasValidFormContext implements ExecutabilityRule {
            
            public static final DialogParentHasValidFormContext INSTANCE = new DialogParentHasValidFormContext();
            
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
                FormComponent parentForm = (FormComponent) aComponent.getDialogParent();
                if (parentForm.getFormContext().checkAll()) {
                    return ExecutableState.EXECUTABLE;
                }
                return INVALID_FORM_CONTEXT;
            }
        }

    }
}
