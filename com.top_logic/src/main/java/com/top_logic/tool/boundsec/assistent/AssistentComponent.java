/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.gui.layout.ButtonBar;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutList;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.assistent.commandhandler.BackwardAssistentCommandHandler;
import com.top_logic.tool.boundsec.assistent.commandhandler.CancelAssistentCommandHandler;
import com.top_logic.tool.boundsec.assistent.commandhandler.FinishAssistentCommandHandler;
import com.top_logic.tool.boundsec.assistent.commandhandler.ForwardAssistentCommandHandler;
import com.top_logic.tool.boundsec.assistent.commandhandler.ShowAssistentCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * Implementation of a assistant (wizard). All steps are based on LayoutComponent.
 * 
 * Access to the DataMap is not synchronized, as this concurrent access is
 * not expected. 
 * 
 * TODO FSC/KHA fix erode executability rules by some Kind of 
 *     {@link AbstractAssistentController} based {@link ExecutabilityRule}.
 * 
 * <p> <b>DEPRECATED: use BoundAssistentComponent instead because this component
 *             doesn't care about security
 * </b></p>
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class AssistentComponent extends LayoutList implements Selectable {

	public interface Config extends LayoutList.Config, Selectable.SelectableConfig {
		@Name(XML_ATTR_FORWARD_COMMAND_NAME)
		@StringDefault(FORWARD_COMMAND_KEY)
		String getForwardCommand();

		@Name(XML_ATTR_BACK_COMMAND_NAME)
		@StringDefault(BACKWARD_COMMAND_KEY)
		String getBackCommand();

		@Name(XML_ATTR_CANCEL_COMMAND_NAME)
		@StringDefault(CANCEL_COMMAND_KEY)
		String getCancelCommand();

		@Name(XML_ATTR_SHOW_COMMAND_NAME)
		@StringDefault(SHOW_COMMAND_KEY)
		String getShowCommand();

		@Name(AssistentComponent.XML_ATTR_CONTROLLER_CLASS)
		@Mandatory
		PolymorphicConfiguration<AbstractAssistentController> getController();

		void setController(PolymorphicConfiguration<AbstractAssistentController> aController);

	}

	/** Boolean flag in Data Map that indicates that step had a failure.
     *
     * You can use this to make your next step be a special error step. 
     */
    public static final String SHOW_FAILTURE = "showFailture";

    /** Boolean flag in Data Map that indicates that step should be shown again.
     *
     *  (Normally as a result of a failed Constraint-validation) 
     */
    public static final String SHOW_AGAIN   = "showAgain";

	private static final String XML_ATTR_CONTROLLER_CLASS = "controller";
    private static final String XML_ATTR_FORWARD_COMMAND_NAME = "forwardCommand";
    private static final String XML_ATTR_BACK_COMMAND_NAME = "backCommand";
    private static final String XML_ATTR_CANCEL_COMMAND_NAME = "cancelCommand";
    private static final String XML_ATTR_SHOW_COMMAND_NAME = "showCommand";
        
    private CommandHandler forwardHandler;
    private CommandHandler backwardHandler;
    private CommandHandler cancelHandler;
    private CommandHandler showHandler;

    /** the steps as stepInfos */
    private Stack stepStack; 

    /** map for communication between steps. */
    private Map<String, Object> dataMap; 
    
    /** All StepInfos are stored here. */
	private List<AssistentStepInfo> _stepConfigs;
    
    /** The selected step. */
    private DefaultDeckPaneModel model;
    
    /** The controller of this assistant. */
    private AbstractAssistentController controller;
    
    /** key for CommandHandlerFactory */
    public static final String FORWARD_COMMAND_KEY  = ForwardAssistentCommandHandler.COMMAND;

    /** key for CommandHandlerFactory */
    public static final String BACKWARD_COMMAND_KEY = BackwardAssistentCommandHandler.COMMAND;

    /** key for CommandHandlerFactory */
    private static final String CANCEL_COMMAND_KEY  = CancelAssistentCommandHandler.COMMAND;

    /** key for CommandHandlerFactory */
    public static final String FINISH_COMMAND_KEY  = FinishAssistentCommandHandler.COMMAND;

    /** key for CommandHandlerFactory */
    public static final String SHOW_COMMAND_KEY    = ShowAssistentCommandHandler.COMMAND;
    
    private boolean modelValid;

    private String forwardCommandName;
    private String backCommandName;
    private String cancelCommandName;
    private String showCommandName;
    
	public AssistentComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		this.forwardCommandName = config.getForwardCommand();
		this.backCommandName = config.getBackCommand();
		this.cancelCommandName = config.getCancelCommand();
		this.showCommandName = config.getShowCommand();
		initAssistent(context, config);
    }
    
    /**
	 * The {@link AssistentComponent} in which the given component is displayed, or
	 * <code>null</code>, if the given component is not part of an assistant.
	 */
    public static AssistentComponent getEnclosingAssistentComponent(LayoutComponent aComponent) {
		LayoutComponent theComponent = aComponent;
		while (theComponent != null && !(theComponent instanceof AssistentComponent)) {
            theComponent = theComponent.getParent();
        }
        return (AssistentComponent)theComponent;
    }

    /**
     * If a component is used inn assistant step, this method will return the controller of the 
     * AisstentComponent.
     * 
     * @param aComponent a component used in a assistant step.
     * @return the controller of the assistentComponent.
     */
    public static AbstractAssistentController getAssistentController(LayoutComponent aComponent) {
        AssistentComponent theAssistent = getEnclosingAssistentComponent(aComponent);
        if(theAssistent != null) {
            return theAssistent.getController();
        }
        else {
            return null;
        }
    }
    
	/** Tests if step is a major step. */
    public static boolean isMajorStep(AssistentStepInfo assistentStepInfo) {
		return assistentStepInfo.getStepKey() != null;
	}

	private void initAssistent(InstantiationContext context, Config config) {
        this.model = new DefaultDeckPaneModel();
		this.controller = context.getInstance(config.getController());
		controller.initAssistant(this);
    }

    public AssistentStepInfo getCurrentStepInfo() {
        return this.getStepInfo(this.getSelectedIndex());
    }
    
	/**
	 * The name of {@link #getCurrentStepInfo()}.
	 */
	public final ComponentName getCurrentStepInfoName() {
		return AssistentStepInfo.getName(getCurrentStepInfo());
	}

    public AssistentStepInfo getStepInfo(int aPos) {
		if (aPos > this._stepConfigs.size() || aPos < 0) {
            return null;
        }
		return (AssistentStepInfo) this._stepConfigs.get(aPos);
    }

    public LayoutComponent getCurrentStep() {
        return this.getCurrentChild();
    }

    public AbstractAssistentController getController() {
        return this.controller;
    }
    
    public Stack getStepStack() {
        return this.stepStack;
    }

    public BoundCommandGroup getDefaultCommandGroup() {
        return SimpleBoundCommandGroup.READ;
    }
    
    public void setSelectedIndex(int anIndex) {
        
        int theIndex = this.getSelectedIndex();

// KBU: Finish and restart needs this in the showAssistent step
//        if (anIndex == theIndex) {
//            return; // nothing has happened ...
//        }
          
        /* current step */
        LayoutComponent theComponent;

        if (theIndex >= 0) {
			theComponent = getChild(theIndex);
            theComponent.setVisible(false);
        }
        else {
            // Assistant not initialized yet, no need to deactivate a component.
        }

        /* new step */
		theComponent = getChild(anIndex);
        if (isVisible()) {
            theComponent.setVisible(true);
        }

        this.model.setSelectedIndex(anIndex);
    }
    
    /** 
     * This method returns which step is currently shown.
     */
    public final int getSelectedIndex() {
    	return this.model.getSelectedIndex();
    }

    @Override
	public boolean validateModel(DisplayContext aContext) {
		boolean nonLocalChange = super.validateModel(aContext);
        this.registerSpecialCommands();
        modelValid=true;
		return nonLocalChange;
    }

    /**
     * special commands etc.
     * 
     * TODO KHA this is called for every redraw, can this be optimized ?
     */
    protected final void registerSpecialCommands() {
		ComponentName currentStepName = this.getCurrentStepInfoName();
		if (currentStepName == null) {
			return;
		}
        clearCommands();


        CommandHolder next = this.controller.getAdditionaleCommandForNext(currentStepName);
        CommandHolder prev = this.controller.getAdditionaleCommandForPrevious(currentStepName);
        CommandHolder canc = this.controller.getAdditionaleCommandForCancel(currentStepName);
        List<CommandHolder> commands = this.controller.getAdditionalCommands(currentStepName);

        /* create all the buttons */
        reinitButton(canc, this.getCancelHandler());
        reinitButton(prev, this.getBackHandler());
        if (this.controller.isLastStep(currentStepName)) {
        	reinitButton(next, this.getShowHandler());        	
        }
        else {
        	reinitButton(next, this.getForwardHandler());
        }

		List<CommandModel> buttons = new ArrayList<>(commands.size());
		buttons.addAll(createButtonCommandModels());
		for (Iterator<CommandHolder> it = commands.iterator(); it.hasNext();) {
			CommandHolder holder = it.next();

			CommandModel model = modelForCommand(holder.getHandler(), holder.getTargetComponent());
			model.setShowProgress();
			buttons.add(model);
		}

        ButtonBar theBC = getButtonBar();
		theBC.setTransientButtons(buttons);
    }

    @Override
	protected CommandModel modelForCommand(CommandHandler command, Map<String, Object> arguments,
			LayoutComponent targetComponent) {
		CommandModel theModel = super.modelForCommand(command, arguments, targetComponent);

        if (command instanceof CommandChain) {
			theModel.setShowProgress();
        }
        else if (command instanceof ForwardAssistentCommandHandler) {
			theModel.setShowProgress();
        }

        return theModel;
    }

    @Override
	public CommandHandler getCancelCommand() {
		CommandHandler configuredCommand = super.getCancelCommand();
		if (configuredCommand != null) {
			return configuredCommand;
		}
		ComponentName theStep = AssistentStepInfo.getName(getCurrentStepInfo());
        if (this.controller.isLastStep(theStep) ) {
			return this.getCommandChain(this.controller.getAdditionaleCommandForNext(theStep), this.getShowHandler());
        }
		return this.getCommandChain(this.controller.getAdditionaleCommandForCancel(theStep), this.getCancelHandler());
    }
    
    @Override
	public CommandHandler getDefaultCommand() {
		CommandHandler configuredCommand = super.getDefaultCommand();
		if (configuredCommand != null) {
			return configuredCommand;
		}
		ComponentName theStep = getCurrentStepInfoName();
        if (this.controller.isLastStep(theStep) || this.controller.isLastStep(theStep) ) {
			return this.getCommandChain(this.controller.getAdditionaleCommandForNext(theStep), this.getShowHandler());
        }
        
		return this.getCommandChain(this.controller.getAdditionaleCommandForNext(theStep), this.getForwardHandler());
    }
    
	private CommandHandler getCommandChain(CommandHolder anAdditionalCommand, CommandHandler defaultCommand) {
		CommandHandler theCommand = defaultCommand;
        if(anAdditionalCommand != null && theCommand != null) {
            LayoutComponent theTarget = anAdditionalCommand.getTargetComponent();
            if (theTarget != null) {
            	List<CommandHolder> commandHolderList = new ArrayList<>(2);
            	commandHolderList.add(anAdditionalCommand);
            	commandHolderList.add(new CommandHolder(defaultCommand, this.getCurrentStep()));
                theCommand = theTarget.getCommandById(CommandChain.generateID(commandHolderList));
            }
            else {
                return theCommand;
            }
        }
        return theCommand;
    }
    
    private void reinitButton(CommandHolder anAdditionalCommand, final CommandHandler defaultCommand) {
		CommandHandler theCommand = defaultCommand;
        if(anAdditionalCommand != null) {
            LayoutComponent theTarget = anAdditionalCommand.getTargetComponent();

            List<CommandHolder> theCommands = new ArrayList<>(2);
            theCommands.add(anAdditionalCommand);
            if (defaultCommand != null) {
            	theCommands.add(new CommandHolder(defaultCommand, this));
            }

			CommandChain theCommandChain =
				CommandChain.newInstance(CommandChain.generateID(theCommands), getDefaultCommandGroup(), theCommands,
					theTarget);

            // TODO every time you show the same step, a new instance will be registered. 
            // (Also results in multiple javascript functions. KHA I'm not sure about this ...)
            if (theTarget != this) {
                // Otherwise 
                theTarget.registerCommand(theCommandChain);
            }
            theCommand = theCommandChain; 
        }
        else {
        	if (defaultCommand != null) {
        		/*
				 * Register the default command on the current step to make the
				 * client side representation of this AssistentComponent
				 * unnecessary. This is needed for DIV-Layout since there is no
				 * representation of the Frame this AssistentComponent writes.
				 */
        		CommandHolder theHolder = new CommandHolder(defaultCommand,this);
        		LayoutComponent theTarget = getCurrentStep();
				CommandChain theCommandChain = CommandChain.newInstance(
					CommandChain.generateID(Collections.singletonList(theHolder)),
					SimpleBoundCommandGroup.READ,
					Collections.singletonList(theHolder),
					theTarget,
					new ExecutabilityRule() {
						@Override
						public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
							return defaultCommand.isExecutable(AssistentComponent.this, model, someValues);
						}
					},
					defaultCommand.getResourceKey(theTarget)
				);
				theTarget.registerCommand(theCommandChain);
				theCommand = theCommandChain;
        	}
        }

        /* rename the buttons 
        boolean forwardButton  = defaultCommand instanceof ForwardAssistentCommandHandler;
        boolean backwardButton = defaultCommand instanceof BackwardAssistentCommandHandler;
        String  buttonResPrefix= "assistent";

        String fwdCaption = this.controller.getSpecialButtonCaptionKeyForForward(this.getCurrentStepInfo().getName());
        String backwardCaption = this.controller.getSpecialButtonCaptionKeyForBackward(this.getCurrentStepInfo().getName());
        if(fwdCaption != null && forwardButton) {
            theButton.updateI18N(fwdCaption);
        }    
        else if(backwardCaption != null && backwardButton) {
            theButton.updateI18N(backwardCaption);
        }else
        {
            String theId;
            if (defaultCommand != null) {
                theId = defaultCommand.getID();
            } else {
                theId = "bla";   
            }
            theButton.updateI18N(buttonResPrefix + "." + theId);
        }
        */

        //TODO this is to much... we only need the button in the ButtonComponent, not the js script in assistentComponent
		if (theCommand != null) {
			if (!this.registerButtonCommand(theCommand)) {
				Logger.error("Failed to registerButtonCommand:" + theCommand, this);
			}
        }
    }

    protected synchronized CommandHandler getBackHandler() {

        if(this.backwardHandler == null) {
            CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
            this.backwardHandler = theFac.getHandler(backCommandName);
        }
        return this.backwardHandler;
    }

    protected synchronized CommandHandler getForwardHandler() {

        if(this.forwardHandler == null) {
            CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
            this.forwardHandler = theFac.getHandler(forwardCommandName);
        }
        return this.forwardHandler;
    }

    protected synchronized CommandHandler getCancelHandler() {
        
        if(this.cancelHandler == null) {
            CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
            this.cancelHandler = theFac.getHandler(cancelCommandName);
        }
        return this.cancelHandler;
    }

    protected synchronized CommandHandler getShowHandler() {
        
        if(this.showHandler == null) {
            CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
            this.showHandler = theFac.getHandler(showCommandName);
        }
        return this.showHandler;
    }

    @Override
	protected void onRemove(int index, LayoutComponent removed) {
		super.onRemove(index, removed);

        List selectableObjects = this.model.getSelectableCards();
		for (int n = 0, size = selectableObjects.size(); n < size; n++) {
			Card currentCard = (Card) selectableObjects.get(n);
			if (currentCard.getContent() == removed) {
				_stepConfigs.remove(n);
        		this.model.removeSelectableCard(currentCard);
				break;
        	}
        }
    }

	@Override
	protected void onAdd(int index, LayoutComponent newChild) {
		super.onAdd(index, newChild);
		
		AssistentStepInfo assistantInfo = getAssistantInfo(newChild);
		_stepConfigs.add(index, assistantInfo);
		this.model.addSelectableCard(index, new AssistantCard(newChild, assistantInfo));
	}

	private AssistentStepInfo getAssistantInfo(LayoutComponent newChild) {
		AssistentStepInfo assistantInfo = newChild.getConfig().getAssistantInfo();
		if (assistantInfo == null) {
			throw new TopLogicException(noAssistantInfoFor(newChild));
		}
		return assistantInfo;
	}

	private ResKey noAssistantInfoFor(LayoutComponent child) {
		return I18NConstants.MISSING_ASSISTANT_INFO__CHILD__ASSISTANT.fill(child.toString(),
			AssistentComponent.this.toString());
	}

	@Override
	protected void onSet(List<LayoutComponent> oldChildren) {
		super.onSet(oldChildren);
		_stepConfigs = getChildList().stream()
			.map(this::getAssistantInfo)
			.collect(Collectors.toList());
	}

    @Override
	public boolean containsComponent(LayoutComponent aComponent, boolean visibleOnly) {
		for (LayoutComponent child : getChildList()) {
			if (aComponent == child) {
                return true && visibleOnly;
            }
        }
        
        return false;
    }

    @Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		LayoutComponent currentStep = this.getCurrentStep();
		return currentStep == null ? Collections.<LayoutComponent> emptyList() : Collections.singletonList(currentStep);
    }

    @Override
	protected void setChildVisibility(boolean visible) {
    	LayoutComponent currentStep = this.getCurrentStep();
		if (currentStep != null) {
			currentStep.setVisible(visible);
		}
    }
    
    @Override
	protected void becomingVisible() {
        super.becomingVisible();

        //TODO dbu: Ticket #3878 please verify that this is a correct fix.
        modelValid = false;
        this.resetStackAndSteps();
        this.resetData();
        
        getController().becomingVisible();
		setSelected(_stepConfigs);
    }
    
    /** Callback to controller
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent#becomingInvisible()
	 */
	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		getController().becomingInvisible();
	}
    
    /** show the first visible step */
    private void resetStackAndSteps() {
        this.stepStack = new Stack();
		setSelectedIndex(getInitialIndex());
        this.getStepStack().push(this.getCurrentStepInfo());
    }

	/**
	 * The index of the initial step.
	 */
	protected int getInitialIndex() {
		return 0;
	}
    
    /** user canceled dialog */
    public void dialogCanceled() {
        // Allow Controller to cleanup some Objects, first
        this.getController().dialogCanceled();
        this.resetData();
        this.resetStackAndSteps();
        this.invalidate();
    }
    
    /** dialog finished successfully, notify controller, then cleanup */
    public void dialogFinished() {
        // Allow Controller to cleanup some Objects, first
        this.getController().dialogFinished();
        this.resetData();
        this.resetStackAndSteps();
    }
    
    @Override
	public boolean makeVisible(LayoutComponent aChild) {
		if (getChildList().indexOf(aChild) < 0) {
    		return false;   // not my child                
    	}
        
        boolean superVisible = makeVisible();
        
        if (superVisible) { // No need to this if super failed me
           
            showStep(aChild);
        }

        return superVisible;   // This should do it.
    }

	/**
	 * This method switches this assistant to the given step
	 * 
	 * @param aStep
	 *            the step to make visible
	 * @throws IllegalArgumentException
	 *             if the given step is not a step of this assistant
	 */
	public void showStep(LayoutComponent aStep) {
		int index = getChildList().indexOf(aStep);
		if (index >= 0) {
			setSelectedIndex(index);
			invalidate();
		} else {
			throw new IllegalArgumentException("The given step '" + aStep + "' is not a step of this assistant.");
		}
	}
    
    @Override
	public boolean isOuterFrameset() {
        return true;
    }
    
    @Override
	public boolean isCompleteRenderer() {
        return true;
    }
    
    private LayoutComponent getCurrentChild() {
		int selectedIndex = this.getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		return getChild(selectedIndex);
    }
    
    /**
     * the step specified by given name.
     */
    public LayoutComponent getStepByStepName(ComponentName aStepName) {
        AssistentStepInfo theStepInfo;
		int count = this._stepConfigs.size();

        if (aStepName != null) {
            for (int thePos = 0; thePos < count; thePos++) {
				theStepInfo = this._stepConfigs.get(thePos);
    
				if ((theStepInfo != null) && aStepName.equals(AssistentStepInfo.getName(theStepInfo))) {
					return getChild(thePos);
                }
            }
        }

        return null;
    }
    
    private synchronized Map<String, Object> getDataMap() {
        
        if(this.dataMap == null) {
            this.dataMap = new HashMap<>();
        }
        return this.dataMap;
    }
    
    /**
     * Sets the value for dataKey to data.
     * 
     * @param dataKey   Arbitrary key for the internal map.
     * @param data      if null, dataKey is removed from the map
     */
    public void setData(String dataKey, Object data) {
        Map<String, Object> theMap = this.getDataMap();
        if(data==null) {
            theMap.remove(dataKey);  
        } else {
           theMap.put(dataKey, data);
        }
    }
    
    public Object getData(String dataKey) {
        return this.getDataMap().get(dataKey);
    }
    
    public Set<String> getDataKeys() {
        return this.getDataMap().keySet();
    }

    public void resetData() {
        this.getDataMap().clear();        
    }
    
    /**
     * @param dataKey the key of the data
     * @return an HTML representation of the data belonging to the given key
     * An empty String is returned when no data for the key exists.
     */
    public String getDataAsHTML(String dataKey) {
    	Object obj = getData(dataKey);
    	if(obj!=null){
    		return TagUtil.encodeXML(obj.toString());
    	}
    	return "";
    }
    
    @Override
	public void invalidate() {
    	super.invalidate();
    	modelValid=false;
    }
    
    
    @Override
	public boolean isModelValid() {
		return this.modelValid && super.isModelValid();
    }
    
    
    /**
	 * This method returns the {@link DefaultDeckPaneModel} of this {@link AssistentComponent}.
	 */
    public final DefaultDeckPaneModel getDeckPaneModel() {
    	return this.model;
    }
    
    @Override
	protected void componentsResolved(InstantiationContext context) {
		for (int index = 0, size = getChildCount(); index < size; index++) {
			AssistantCard currentCard = new AssistantCard(getChild(index), getStepInfo(index));
			this.model.addSelectableCard(currentCard);
		}
    	super.componentsResolved(context);
    }
    
    /**
	 * The class {@link AssistantCard} is the {@link Card} implementation which is used to add cards
	 * to the model.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
    private static class AssistantCard implements Card {

		private final AssistentStepInfo info;
		private final LayoutComponent content;

		public AssistantCard(LayoutComponent content, AssistentStepInfo info) {
			this.content = content;
			this.info = info;
		}

		@Override
		public AssistentStepInfo getCardInfo() {
			return info;
		}

		@Override
		public Object getContent() {
			return content;
		}

		@Override
		public void writeCardInfo(DisplayContext context, Appendable out) throws IOException {
			out.append(getName());
		}

		@Override
		public String getName() {
			return ComponentNameFormat.INSTANCE.getSpecification(AssistentStepInfo.getName(info));
		}

	}

    /** 
     * Recursivly put all {@link FormField#getValue()} from aCtxt into {@link #setData(String, Object)}.
     * 
     * @param    aCtxt    The form context to transfer the data from.
     */
    public void updateDataMap(FormContainer aCtxt) {
        Iterator<? extends FormMember> members = aCtxt.getMembers();
        while (members.hasNext()) {
            FormMember theMember = members.next();
            if (theMember instanceof SelectField && !((SelectField) theMember).isMultiple()) {
                Object theValue = CollectionUtil.getSingleValueFrom(((FormField)theMember).getValue());
                this.setData(theMember.getName(), theValue);
            }
            else if (theMember instanceof FormField) {
                this.setData(theMember.getName(), ((FormField)theMember).getValue());
            }
            else if (theMember instanceof FormContainer) {
                // TODO this will override Members with the same name via the SubContainer ...
                updateDataMap((FormContainer) theMember);
            }
        }    
    }

}