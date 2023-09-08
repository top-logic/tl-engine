/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.shared.collection.map.MapUtilShared;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Simple implementation of assistant controller with default behavior.
 * Can be used for simple assistants, look at subclasses for examples
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public abstract class SimpleAssistentController extends AbstractAssistentController{

	private Map<ComponentName, SimpleStepInfo> allSteps;
    
	public SimpleAssistentController(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * This method registers an array of steps.
     * 
     * @param steps must NOT be <code>null</code>.
     */
    protected void registerSteps(SimpleStepInfo steps[]) {
         if (allSteps == null) {
			allSteps = MapUtilShared.newMap(steps.length);
         }
         for (int i = 0; i < steps.length; i++) {
             SimpleStepInfo info = steps[i];
             if (info != null) {
                 allSteps.put(info.getName(), info);
             }
             else {
                 Logger.error("no step info for step nr. " + i, this.getClass());
             }
         }
    }

    /**
	 * This method registers a new simple step.
	 * 
	 * @param aStep
	 *            A {@link SimpleStepInfo}. Must NOT be <code>null</code>.
	 */
    protected void registerStep(SimpleStepInfo aStep) {
         if (allSteps == null) {
			allSteps = MapUtilShared.newMap(16);
         }
    	 allSteps.put(aStep.getName(), aStep);
    }
    
    /**
     * creates and registers a step for the name, nextName and holder
     * @param aName name of the step
     * @param aNextName name of next step
     * @param aHolder commandHolder for additional command
     */
    protected void registerStep(ComponentName aName, ComponentName aNextName, CommandHolder aHolder){
        registerStep(aName,aNextName,null,aHolder);       
    }

    /**
     * creates and registers a step for the name, nextName, failtureName and holder
     * @param aName name of the step
     * @param aNextName name of the next step
     * @param aFailtureName name of step to display if step fails
     * @param aHolder commandHolder for additional command
     */
    protected void registerStep(ComponentName aName, ComponentName aNextName, ComponentName aFailtureName, CommandHolder aHolder){
        registerStep(new SimpleStepInfo(aName,aNextName,aFailtureName,aHolder));       
    }
    
    /**
     * Returns the registered SimpleStepInfo for given name.
     * 
     * @return null, if no such step exists.
     */
    protected SimpleStepInfo getStep(ComponentName aStepName){
        if (allSteps != null) {
			return allSteps.get(aStepName);
        }
        return null;
    }
    
    /**
     * true when at least on SimpleStepInfo was registered.
     */
    public boolean hasSteps() {
        return allSteps != null && !allSteps.isEmpty();
    }
    
    /**
     * Return the additional commands added by {@link #registerStep(ComponentName, ComponentName, CommandHolder) }.
     * 
     * Will call super if no CommandHolder was registered. In case of last step
     * you must care for the finishCommand yourself.
     */
    @Override
	public CommandHolder getAdditionaleCommandForNext(ComponentName currentStepName) {
    	SimpleStepInfo theStep   = this.getStep(currentStepName);
        CommandHolder  theHolder = null;

        if (theStep != null) {
            theHolder = theStep.getHolder();
        }
        else {
            Logger.warn("No step found for " + currentStepName, this);
        }

        if (theHolder == null) {
            theHolder = super.getAdditionaleCommandForNext(currentStepName);
    	}

        return (theHolder);
    }
    
    /**
     * Simple Data holder for connecting steps in assistants.
     * 
     * @author    <a href=mailto:fma@top-logic.com>fma</a>
     */
	public static class SimpleStepInfo {
		
		ComponentName name;

		ComponentName nextName;

		ComponentName failtureName;
        CommandHolder holder;
        boolean firstStep;
        boolean lastStep;
		boolean closeStep;
		boolean finishStep;
		boolean forwardButton;
		boolean backwardButton;
		boolean cancelButton;
    	
        public SimpleStepInfo(ComponentName aName, ComponentName aNextName, ComponentName aFailtureName, CommandHolder aHolder){
    	    this(aName, aNextName, aFailtureName, aHolder, false, false, false, false);
    	}
       
        public SimpleStepInfo(ComponentName aName, ComponentName aNextName, CommandHolder aHolder, boolean isFirstStep, boolean isLastStep, boolean isCloseStep, boolean isFinishStep){
        	this(aName, aNextName, null, aHolder, isFirstStep, isLastStep, isCloseStep, isFinishStep);
        }
        
        public SimpleStepInfo(ComponentName aName, ComponentName aNextName, ComponentName aFailtureName, CommandHolder aHolder, boolean isFirstStep, boolean isLastStep, boolean isCloseStep, boolean isFinishStep){
    	    name           = aName;
			nextName       = aNextName;
			holder         = aHolder;
			failtureName   = aFailtureName;
			firstStep      = isFirstStep; 
			lastStep       = isLastStep;
			closeStep      = isCloseStep;
			finishStep     = isFinishStep;
			forwardButton  = true;
			backwardButton = true;
			cancelButton   = true;
    	}
        
		public ComponentName getName() {
    	    return name;
    	}
    	
		public ComponentName getNextName() {
    	    return nextName;
    	}
    	
		public ComponentName getFailtureName() {
    	    return failtureName;
    	}       
    	
    	public CommandHolder getHolder(){ 
    	    return holder;
    	}

    	public boolean isCloseInfoStep() {
    		return this.closeStep;
    	}

    	public boolean isFinishStep() {
    		return this.finishStep;
    	}

    	public boolean isFirstStep() {
    		return this.firstStep;
    	}

    	public boolean isLastStep() {
    		return this.lastStep;
    	}

		/**
		 * Returns the closeStep.
		 */
		public boolean isCloseStep() {
			return this.closeStep;
		}

		/**
		 * @param isCloseStep The closeStep to set.
		 */
		public void setCloseStep(boolean isCloseStep) {
			this.closeStep = isCloseStep;
		}

		/**
		 * @param isFirstStep The firstStep to set.
		 */
		public void setFirstStep(boolean isFirstStep) {
			this.firstStep = isFirstStep;
		}

		/**
		 * @param isLastStep The lastStep to set.
		 */
		public void setLastStep(boolean isLastStep) {
			this.lastStep = isLastStep;
		}

		/**
		 * @param isFinishStep The finishStep to set.
		 */
		public void setFinishStep(boolean isFinishStep) {
			this.finishStep = isFinishStep;
		}

		/**
		 * Returns the forwardButton.
		 */
		public boolean hasForwardButton() {
			return this.forwardButton;
		}

		/**
		 * @param aForwardButton The forwardButton to set.
		 */
		public void setForwardButton(boolean aForwardButton) {
			this.forwardButton = aForwardButton;
		}

		/**
		 * Returns the backwardButton.
		 */
		public boolean hasBackwardButton() {
			return this.backwardButton;
		}

		/**
		 * @param aBackwardButton The backwardButton to set.
		 */
		public void setBackwardButton(boolean aBackwardButton) {
			this.backwardButton = aBackwardButton;
		}

		/**
		 * Returns the cancelButton.
		 */
		public boolean hasCancelButton() {
			return this.cancelButton;
		}

		/**
		 * @param aCancelButton The cancelButton to set.
		 */
		public void setCancelButton(boolean aCancelButton) {
			this.cancelButton = aCancelButton;
		}

    }

    /**
     * Return the name of the next step or a Failure Flag in case one is set. 
     * 
     * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#getNameOfNextStep(ComponentName)
     */
    @Override
	public ComponentName getNameOfNextStep(ComponentName currentStepName) {
        Boolean showAgain = (Boolean)assistent.getData(AssistentComponent.SHOW_AGAIN);
        if(showAgain!=null && showAgain.booleanValue()){
            assistent.setData(AssistentComponent.SHOW_AGAIN,null);
            return currentStepName;
        }

        Boolean showFailture = (Boolean)assistent.getData(AssistentComponent.SHOW_FAILTURE);
        if(showFailture!=null && showFailture.booleanValue()){
            assistent.setData(AssistentComponent.SHOW_FAILTURE,null);
            return getStep(currentStepName).getFailtureName();
        }
        return getStep(currentStepName).getNextName();
    }    
    
    /**
     * Resets all form components
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#dialogCanceled()
	 */
	@Override
	public void dialogCanceled() {
		resetFormComponents();
		super.dialogCanceled();
	}
	
	/**
	 * Resets all form components
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#dialogFinished()
	 */
	@Override
	public void dialogFinished() {
		resetFormComponents();
		super.dialogFinished();
	}
	
	/**
     * Reset the form components when becoming visible
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#becomingVisible()
	 */
	@Override
	public void becomingVisible() {
		resetFormComponents();
		super.becomingVisible();
	}
	
}
