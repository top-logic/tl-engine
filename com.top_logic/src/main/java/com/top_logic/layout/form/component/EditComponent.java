/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.Map;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.component.edit.CanLock;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.ChangeCheckDialogCloser;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InEditModeExecutable;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.error.TopLogicException;


/**
 * {@link FormComponent} allowing to switch the display between edit and view mode.
 * 
 * <p>
 * In view mode, the content is displayed in a way optimized for reading (text only without input
 * fields). In edit mode, an input field is shown for each piece of information that can be edited
 * by the user. The default mode is view mode. If an {@link Config#getApplyCommand() apply command}
 * is registered, the user can switch the component into edit-mode by clicking on the edit button.
 * This apply command is invoked, if the user clicks the "apply" or "save" buttons.
 * </p>
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class EditComponent extends FormComponent implements Editor, CanLock {

	/**
	 * Configuration options for {@link EditComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends FormComponent.Config, Editor.Config, CanLock.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "editor";

		/**
		 * @see #getAllowRefresh()
		 */
		String ALLOW_REFRESH_ATTRIBUTE = "allowRefresh";

		@Override
		@ClassDefault(EditComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		/**
		 * Whether the {@link FormContext} is reset whenever F5 is pressed.
		 */
		@Name(ALLOW_REFRESH_ATTRIBUTE)
		@BooleanDefault(true)
		boolean getAllowRefresh();

		/**
		 * ID of the delete {@link CommandHandler}.
		 * 
		 * <p>
		 * The delete command deletes the currently shown model.
		 * </p>
		 */
		@Name("deleteCommand")
		@Nullable
		String getDeleteCommand();

		/**
		 * Whether to keep the edit mode even if the component becomes invisible.
		 * 
		 * <p>
		 * Normally, an {@link EditComponent} switches back to view mode, if it becomes invisible.
		 * </p>
		 */
		@Name("keepInvisibleEditMode")
		boolean getKeepInvisibleEditMode();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			Editor.Config.super.modifyIntrinsicCommands(registry);

			String theDelete = getDeleteCommand();
			if (!StringServices.isEmpty(theDelete)) {
				registry.registerButton(theDelete);
			}
		}

	}

	/** I18N of data appliance error */
	public static final String DATA_APPLIANCE_ERROR =
		"error_code_com.top_logic.layout.form.component.EditComponent.saveError";

	/**
	 * @see #channels()
	 */
	protected static final Map<String, ChannelSPI> CHANNELS =
		channels(LayoutComponent.MODEL_CHANNEL, EditMode.EDIT_MODE_SPI);

	private static final ComponentChannel.ChannelValueFilter EDIT_MODE_VETO =
		new ComponentChannel.ChannelValueFilter() {

			@Override
			public boolean accept(ComponentChannel sender, Object oldValue, Object newValue) {
				EditComponent editor = (EditComponent) sender.getComponent();
				return editor.checkComponentModeChange(((Boolean) newValue).booleanValue());
			}
		};

    private boolean allowRefresh;

	/***
	 * When component is opened as dialog, this variable defines, if the dialog
	 * will automatically close on clicking the save button.
	 */
	private final LockHandler _lockHandler;
	
	/**
	 * If the Component has to be set to editMode after model set.
	 * 
	 * @see #afterModelSet(Object, Object)
	 */
	private boolean _switchToEdit;

    /**
     * Constructor which gets the configuration via attributes.
     * 
     * @param    someAttrs       Attributes to configure this component.
     */
    public EditComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);

		this.allowRefresh = someAttrs.getAllowRefresh();

		_lockHandler = CanLock.createLockHandler(context, someAttrs);
    }

	/**
	 * This component's {@link LockHandler}, see {@link Config#getLockHandler()}.
	 */
	@Override
	public LockHandler getLockHandler() {
		return _lockHandler;
	}

	/**
	 * Overridden to set {@link #_switchToEdit} false if editMode is false.
	 * 
	 * @see com.top_logic.layout.form.component.edit.EditMode#setEditMode(boolean)
	 */
	@Override
	public void setEditMode(boolean newValue) {
		Editor.super.setEditMode(newValue);
		if (!newValue) {
			_switchToEdit = false;
		}
	}

    /**
     * Leave Edit mode when changing tabber.
     * @see com.top_logic.layout.basic.component.ControlComponent#becomingInvisible()
     */
    @Override
	protected void becomingInvisible() {
        super.becomingInvisible();
        if(!isInViewMode()){
			if (!config().getKeepInvisibleEditMode()) {
				setViewMode();
			}
        }
		_switchToEdit = false;
    }

    /**
     * Return the name of the {@link com.top_logic.tool.boundsec.CommandHandler} performing
     * a delete operation.
     * 
     * This handler will be active, when the component is in the edit mode. If the component
     * didn't support deletion of objects, this method must return <code>null</code>.
     * 
     * @return    The name of the delete command, <code>null</code> 
     *            when there is no delete command
     */
	protected final String getDeleteCommandHandlerName() {
		return config().getDeleteCommand();
	}

	/**
	 * Setup the immutable state of the new {@link FormContext} depending on the
	 * component state of this component.
	 * 
	 * Depending on the current state of the component (is in view, edit or
	 * create mode) the entries in the form context will be activated or
	 * deactivated.
	 * 
	 * @see #isInEditMode()
	 */
    @Override
    protected void setupFormContext(FormContext newFormContext) {
    	super.setupFormContext(newFormContext);
    	initByComponentMode(newFormContext);
    }

	private void initByComponentMode(FormContext formContext) {
		formContext.setImmutable(!this.isInEditMode());
	}
    
    @Override
    protected void registerDialogCloseCommand() {
    	// This command handler is responsible for handling the change management  
    	// when closing the dialog in edit mode via the "X" button of the dialog.
    	this.registerCommandHandler(ChangeCheckDialogCloser.HANDLER_NAME, false);

		super.registerDialogCloseCommand();
    }
    
    @Override
    protected String getDefaultCloseDialogHandlerName() {
    	return CloseDialogInViewCommandHandler.COMMAND_ID;
    }

	/**
	 * Hook called before the actual mode change happens.
	 * 
	 * <p>
	 * By default, the token context for the current model is acquired/released. If this fails, the
	 * mode change is denied.
	 * </p>
	 * 
	 * @param editMode
	 *        The pending mode to be selected.
	 * @throws TopLogicException
	 *         To cancel the pending mode change.
	 */
	protected boolean checkComponentModeChange(boolean editMode) throws TopLogicException {
		if (editMode) {
			CommandHandler handler = getEditCommandHandler();

			// Note: If the handler is null, the component only acts as slave of another edit mode
			// handler.
			if (handler != null && !allow(handler)) {
				return false;
			}
		}

		updateLock(editMode);
		return true;
	}

	/**
	 * Updates the lock state on response to a mode switch.
	 *
	 * @param editMode
	 *        The new mode.
	 */
	protected void updateLock(boolean editMode) {
		LockHandler lockHandler = getLockHandler();
		if (editMode) {
			// Could possibly fail with TopLogicException
			acquireTokenContext();

			// Since acquire triggers a refetch, the model could become invalid just after locking
			// succeeded, see Ticket #7527.
			if (!ComponentUtil.isValid(getModel())) {
				lockHandler.releaseLock();
				throw LayoutUtils.createErrorSelectedObjectDeleted();
			}
		} else {
			if (lockHandler.getLock() != null) {
				lockHandler.releaseLock();
			}
		}
	}

	/**
	 * Notifies about a change of the component mode, see {@link #isInEditMode()},
	 * {@link #isInViewMode()}.
	 * 
	 * @param editMode
	 *        The new component mode.
	 */
	@Override
	public void handleComponentModeChange(boolean editMode) {
		// Even when switching from edit mode back to view mode, the
		// contents of the form must be renewed, because changes to the form
		// context may have happened during edit mode.
		//
		// if (aMode == EDIT_MODE) {
		// // when switching into edit mode discard context
		// // in order to force creation of new context.
		// // This ensures, that the context contains the most recent data
		// this.setFormContext(null);
		// }

		// Reset the form context.
		this.removeFormContext();
		this.invalidate();
	}

    /**
     * Returns the cancel command. 
     */
    @Override
	public final CommandHandler getCancelCommand() {
		CommandHandler configuredCommand = super.getCancelCommand();
		if (configuredCommand != null) {
			return configuredCommand;
		}
		return Editor.super.getCancelCommandHandler();
    }

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public Command getDiscardClosure() {
		return Editor.super.getDiscardClosure();
	}

    @Override
	public CommandHandler getDefaultCommand() {
		CommandHandler configuredCommand = super.getDefaultCommand();
		if (configuredCommand != null) {
			return configuredCommand;
		}
        if (this.isInEditMode()) {
			return getSaveCommandHandler();
		} else {
			return getEditCommandHandler();
        }
    }
    
    /**
     * This assumes that all Dialogs will Create new Objects.
     * 
     * ... which may not be true in all cases.
     * 
     * @param    aCommand    The command to be checked, must not be <code>null</code>.
     * @return   <code>true</code>, if the given command is OpenModalDialogCommandHandler
     * @deprecated use {@link ExecutabilityRule}
     */
    @Deprecated
	protected final boolean isCreateCommand(BoundCommand aCommand) {
        return (aCommand instanceof OpenModalDialogCommandHandler);
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		boolean wasInEdit;
		if (getEditCommandHandler() != null) {
			/* If the component is in edit mode we have to check if it is possible to stay in the
			 * edit mode with the new model. We have to do three things: */
			// 1. Remember if the component is in edit mode.
			// 2. Set the component to the view mode.
			// 3. Check the new model with the SwitchToEdit-ExecutableRule.
			wasInEdit = this.isInEditMode();

			if (wasInEdit) {
				this.setViewMode();
			}
		} else {
			// Note: The component acts as edit-mode slave. No switch must be happen with the new
			// model.
			wasInEdit = false;
		}

		super.afterModelSet(oldModel, newModel);

		if (wasInEdit) {
			// Even if the new model is null, try to re-switch to edit mode, because the current new
			// model could be replaced later on with another non-null model.
			_switchToEdit = true;
		}

	}

	/**
	 * Overridden to return valid only if no switch to edit is needed.
	 * 
	 * @return if model is valid
	 * 
	 * @see com.top_logic.layout.form.component.FormComponent#isModelValid()
	 */
	@Override
	public boolean isModelValid() {
		return super.isModelValid() && !_switchToEdit;
	}

	/**
	 * Overridden to validate model before switching to edit mode.
	 * 
	 * @param context
	 * 		  The DisplayContext		
	 * 
	 * @see com.top_logic.layout.form.component.FormComponent#validateModel(com.top_logic.layout.DisplayContext)
	 */
	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);
		if (_switchToEdit) {
			reswitchToEdit();
			_switchToEdit = false;
			result = true;
		}
		return result;
	}

	private void reswitchToEdit() {
		CommandHandler switchCommand = getEditCommandHandler();
		if (switchCommand == null) {
			return;
		}
		ExecutableState executableState =
			switchCommand.isExecutable(this, Collections.<String, Object> emptyMap());
		if (!ExecutableState.EXECUTABLE.equals(executableState)) {
			return;
		}
		try {
			setEditMode();
		} catch (TopLogicException ex) {
			// Mode switch not possible due to token conflict.
			HandlerResult error = HandlerResult.error(ex.getDetails());
			error.setErrorTitle(I18NConstants.CANNOT_KEEP_EDIT_MODE);
			error.setErrorSeverity(ErrorSeverity.INFO);

			// Do not stop processing, only inform the user.
			openErrorDialog(error);
		}
	}

    /**
     * Overwritten to force recreation of form context in case of a refresh
     * 
     * @see com.top_logic.mig.html.layout.LayoutComponent#receiveGlobalRefreshEvent(java.lang.Object, java.lang.Object)
     */
    @Override
	protected boolean receiveGlobalRefreshEvent(Object aModel, Object changedBy) {
        if (this.allowRefresh() && this.isInViewMode()) {
			removeFormContext();
            return true;
        } else {
            return false;
        }
    }

	/**
	 * @see Config#getAllowRefresh()
	 */
    protected boolean allowRefresh() {
        return this.allowRefresh;
    }

	@Override
	public Command getApplyClosure() {
		return Editor.super.getApplyClosure();
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		Editor.super.linkChannels(log);

		editModeChannel().addListener(EDIT_MODE_LISTENER);
		editModeChannel().addVetoListener(EDIT_MODE_VETO);
	}

	@Override
	protected void notifyRemoveFromMainLayout() {
		super.notifyRemoveFromMainLayout();
		// Ensure that no lock remains required when this component is removed.
		LockHandler lockHandler = getLockHandler();
		if (lockHandler.getLock() != null) {
			lockHandler.releaseLock();
		}
	}

	/**
	 * Aborts the current edit operation, drops all changes and switches the component back to view
	 * mode.
	 */
	@Label("Cancel")
    public static class CancelCommand extends AbstractCommandHandler {
    	
        public static final String COMMAND_ID = "nothing";

		/**
		 * Configuration options for {@link EditComponent.CancelCommand}.
		 */
		public interface Config extends AbstractCommandHandler.Config {
			@Override
			@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
			CommandGroupReference getGroup();
		}

		/**
		 * Creates a {@link EditComponent.CancelCommand} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public CancelCommand(InstantiationContext context, Config config) {
    		super(context, config);
		}
    	
    	@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			if (component instanceof EditMode) {
				((EditMode) component).setViewMode();
    		}
    		return HandlerResult.DEFAULT_RESULT;
    	}
    	
		@Override
		protected ExecutabilityRule intrinsicExecutability() {
			return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new OnlyWithCanonicalModel(this));
		}

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
        	return InEditModeExecutable.INSTANCE;
        }
        
        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.CANCEL;
        }
    }

    /**
     * The DiscardCommand is the default "discard" command. It discards all changes made in
     * edit mode, like the cancel command, but stays in edit mode.
     */
    public static class DiscardCommand extends AbstractCommandHandler {

        public static final String COMMAND_ID = "discard";

		/**
		 * Configuration options for {@link EditComponent.DiscardCommand}.
		 */
		public interface Config extends AbstractCommandHandler.Config {
			@Override
			@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
			CommandGroupReference getGroup();
		}

		public DiscardCommand(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			if (component instanceof EditMode) {
				EditMode editor = ((EditMode) component);

				if (editor.isInEditMode()) {
					try {
						editor.setViewMode();
						editor.setEditMode();
					} catch (TopLogicException ex) {
						editor.setViewMode();
						throw new TopLogicException(I18NConstants.CANNOT_KEEP_EDIT_MODE, ex);
					}
				}
			}
            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		protected ExecutabilityRule intrinsicExecutability() {
			return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new OnlyWithCanonicalModel(this));
		}

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return InEditModeExecutable.INSTANCE;
        }

        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.DISCARD;
        }
    }

    /**
     * Close command handler which is visible only in view mode. 
     * 
     * This one may be used, when the edit component is displayed in a dialog.
     */
    public static class CloseDialogInViewCommandHandler extends AbstractCommandHandler {

        public static final String COMMAND_ID = "closeDialogInView";

		/**
		 * Configuration options for {@link EditComponent.CloseDialogInViewCommandHandler}.
		 */
		public interface Config extends AbstractCommandHandler.Config {
			@Override
			@FormattedDefault(TARGET_NULL)
			ModelSpec getTarget();

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
			CommandGroupReference getGroup();
		}

        /** 
         * Creates a {@link CloseDialogInViewCommandHandler}.
         */
        public CloseDialogInViewCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            LayoutComponent theParent = aComponent.getDialogParent();
            
            if (theParent != null) {
                aComponent.closeDialog();
            }

            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return InViewModeExecutable.INSTANCE;
        }
    }
}
