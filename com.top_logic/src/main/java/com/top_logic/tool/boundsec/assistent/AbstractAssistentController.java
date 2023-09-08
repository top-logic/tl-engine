/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * This is the superclass of all assistant controllers.#
 * 
 * TODO SKR/KHA should this implement BoundChecker ?.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class AbstractAssistentController
		extends AbstractConfiguredInstance<AbstractAssistentController.Config> {
    
    /** Use this prefix for your first Step */
    protected static final String INIT_STEP_PREFIX   = "init"; 

    /** Use this prefix for your finish Step */
    protected static final String FINISH_STEP_PREFIX = "finished"; 
    
    /** Use this prefix for your closing Step */
    protected static final String END_STEP_PREFIX    = "end"; 

	protected AssistentComponent assistent;
    
	/**
	 * Configuration for the {@link AbstractAssistentController}
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<AbstractAssistentController> {

		// no configuration properties here

	}

	/**
	 * Creates a new {@link AbstractAssistentController} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to use to instantiate inner configurations.
	 * @param config
	 *        Configuration for the new {@link AbstractAssistentController}.
	 */
	public AbstractAssistentController(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Initializes this controller for the given {@link AssistentComponent}
	 * 
	 * @param assistant
	 *        must not be <code>null</code>.
	 */
	public void initAssistant(AssistentComponent assistant) {
		if (assistant == null) {
			throw new IllegalArgumentException("Assistant must not be null");
		}

		this.assistent = assistant;
	}
    
    /**
     * @param currentStepName the currently shown step.
     * @return true if next button must be named "finish". 
     */
    protected abstract boolean isFinishStep(ComponentName currentStepName);

    /**
     * @param currentStepName the currently shown step.
     * @return true if next button must be named "end" and no "back" and "cancel" is possible.
     */
    protected abstract boolean isCloseInfoStep(ComponentName currentStepName);
    
    /**
     * @param currentStepName the currently shown step.
     * @return true if next button called ("end" or "finish") shall close eventually opened
     *         dialog and make a finish call back on the assistant.
     */
    public abstract boolean isLastStep(ComponentName currentStepName);
    
    /**
     * @param currentStepName the currently shown step.
     * @return true if given step is the first one.
     */
    protected abstract boolean isFirstStep(ComponentName currentStepName);

    /**
     * Will be called, when assistant performs the "next" command.
     * All assistant steps are named by the associated StepInfo. 
     * If you don't want to jump, return the name of your own.
     * 
     * @param currentStepName the currently shown step.
     *        
     * @return the name of the step to show when performing the next command.
     * 
     * @throws IllegalArgumentException when currentStepName is unknown,
     *         or illegal.
     */
    public abstract ComponentName getNameOfNextStep(ComponentName currentStepName); 
    /*
        // Support AssistentComponent.SHOW_AGAIN flag
        Boolean showAgain = (Boolean) assistant.getData(AssistentComponent.SHOW_AGAIN);
        if (showAgain != null && showAgain.booleanValue()) {
            assistant.setData(AssistentComponent.SHOW_AGAIN, null);
            return currentStepName; 
        }

    */
    
    /**
     * Will be called, when assistant performs the "previous" command.
     * All assistant steps are named by the associated StepInfo. 
     * If you don't want to jump, return the name of your own.
     * 
     * @param currentStepName the currently shown step.
     * @return the name of the step to show when performing the previous command.
     */
    public ComponentName getNameOfPreviousStep(ComponentName currentStepName) {
        Stack stepStack = this.assistent.getStepStack();
        AssistentStepInfo theCurrent = (AssistentStepInfo) stepStack.pop();
        AssistentStepInfo theInfo = (AssistentStepInfo) stepStack.peek();
        stepStack.push(theCurrent);
        if(theInfo != null) {
			return AssistentStepInfo.getName(theInfo);
        }
        else {
            Logger.warn("unable to get name of previous step.", this);
            return currentStepName;
        }
    }

    /** is called when dialog was canceled. */
    public void dialogCanceled() {
        // empty here
    }

    /** is called when dialog was finished. (closed) */
    public void  dialogFinished() {
        // empty here
    }
    
    /**
     * Before the "next" command is performed this returned command will be performed.
     * 
     * @param currentStepName the step performing the command.
     * @return the commandHanlder to perform before the switch action. May be null.
     */
    public CommandHolder getAdditionaleCommandForNext(ComponentName currentStepName) {
        
        /* In case we are the last step, there should be a finish or end button.
         * This buttons are handled by the next command.
         * Here a handler is registered to close the dialog when clicked on the button
         * and make a finish call back to the assistant. 
         */
        if(this.isLastStep(currentStepName)) {
            CommandHandlerFactory theFactory = CommandHandlerFactory.getInstance();
            CommandHandler        theHandler = theFactory.getHandler(AssistentComponent.FINISH_COMMAND_KEY);

            return new CommandHolder(theHandler, this.assistent.getCurrentStep());
        }
        return null;
    }
    
    /**
     * Before the "previous" command is performed this returned command will be performed.
     * 
     * @param currentStepName the step performing the command.
     * @return the commandHanlder to perform before the switch action. May be null.
     */
    public CommandHolder getAdditionaleCommandForPrevious(ComponentName currentStepName) {
        return null;
    }

    /**
     * Before the "cancel" command is performed this returned command will be performed.
     * 
     * @param currentStepName the step performing the command.
     * @return the commandHanlder to perform before the switch action. May be null.
     */
    public CommandHolder getAdditionaleCommandForCancel(ComponentName currentStepName) {
        return null;
    }

    /**
	 * Returns a list of additional commands ({@link CommandHolder}) which are displayed behind the
	 * other buttons.
	 *
	 * @param currentStepName
	 *        the step performing the command.
	 * @return the {@link List} of {@link CommandHolder}s to add buttons for
	 */
    public List<CommandHolder> getAdditionalCommands(ComponentName currentStepName) {
        return new ArrayList<>();
    }

    /**
     * In some cases maybe you need to rename the next button to "finish", "end" or sth. else.
     * 
     * @return <code>null</code> to not rename the button.
     */
    public String getSpecialButtonCaptionKeyForForward(ComponentName currentStepName) {
        
        if(this.isFinishStep(currentStepName)) {
            return "assistent.finish";
        }
        else if(this.isCloseInfoStep(currentStepName)) {
            return "assistent.end";
        }
        return null;
    }
    
    /**
     * In some cases maybe you need to rename the back button to "denied"
     * 
     * @return <code>null</code> to not rename the button.
     */
    public String getSpecialButtonCaptionKeyForBackward(ComponentName currentStepName) {
        return null;
        
    }
    
    /**
     * return false to ensure this button type won't be rendered for the given step. 
     */
    public boolean showForwardButton(ComponentName currentStepName) {
        return true;
    }

    /**
     * return false to ensure this button type won't be rendered for the given step. 
     */
    public boolean showBackwardButton(ComponentName currentStepName) {
        return true;
    }
    
    /**
     * return false to ensure this button type won't be rendered for the given step. 
     */
    public boolean showCancelButton(ComponentName currentStepName) {
        return true;
    }

    /**
     * return true to ensure this button type will be rendered as disabled button. 
     */
    public boolean disableCancelButton(ComponentName currentStepName) {
        return this.isCloseInfoStep(currentStepName);
    }

    /**
     * return true to ensure this button type will be rendered as disabled button. 
     */
    public boolean disableBackwardButton(ComponentName currentStepName) {
        return this.isFirstStep(currentStepName);
    }
    
    /**
     * return true to ensure this button type will be rendered as disabled button. 
     */
    public boolean disableForwardButton(ComponentName currentStepName) {
        return false;
    }

    /**
     * Simple CommandHandler that copies all (validated) constraints to the dataMap.
     */
    public static class FormContextAjaxTransferCommandHandler extends AbstractCommandHandler {

		private static final String ID = "formContextAjaxTransferCommand";

		/**
		 * Configuration for {@link FormContextAjaxTransferCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        public static final FormContextAjaxTransferCommandHandler INSTANCE = 
			newInstance(FormContextAjaxTransferCommandHandler.class, ID);
        
		public FormContextAjaxTransferCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        /** 
         * Just copy all FormContext values into the AssistentComponent.
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            
            com.top_logic.layout.form.component.FormComponent  theForm        = (com.top_logic.layout.form.component.FormComponent)aComponent;
            com.top_logic.layout.form.model.FormContext        theFContext    = theForm.getFormContext();
            
            if (theFContext.checkAll()) {
	            AssistentComponent assiComponenet = AssistentComponent.getEnclosingAssistentComponent(aComponent);
	
	            assiComponenet.updateDataMap(theFContext);
	            assiComponenet.setData(AssistentComponent.SHOW_AGAIN, null);
	            return HandlerResult.DEFAULT_RESULT;
            }
            else {
				return AbstractApplyCommandHandler.createErrorResult(theFContext);
            }
        }
    }


	/**
	 * Called from the assistant component when the components becomes visible
	 */
	public void becomingVisible() {
        // empty here
	}

    /**
	 * sets the FormContexts of all inherited children of the assistant which are FormComponents to
	 * <code>null</code>
	 */
    protected final void resetFormComponents() {
    	resetFormComponents(assistent);		
    }

	/**
	 * This method sets the FormContexts of all inherited children of the given
	 * {@link LayoutContainer} which are FormComponents to <code>null</code>.
	 * 
	 * @param container must not be <code>null</code>
	 */
	protected void resetFormComponents(LayoutContainer container) {
		Iterator iter = container.getChildList().iterator();
    	while(iter.hasNext()){
    		Object child = iter.next();
            if(child instanceof com.top_logic.layout.form.component.FormComponent){
				((com.top_logic.layout.form.component.FormComponent) child).removeFormContext();
            }
            if (child instanceof LayoutContainer) {
            	resetFormComponents((LayoutContainer) child);
            }
    	}
	}

	/**
	 * called from the assistant component when the component becomes invisible
	 * Can be used for clean ups
	 */
	public void becomingInvisible() {
		// does nothing by default
	}
}
