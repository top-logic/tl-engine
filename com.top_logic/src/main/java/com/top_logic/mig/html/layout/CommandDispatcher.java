/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder.Event;
import com.top_logic.layout.scripting.runtime.action.ExpectedFailureActionOp;
import com.top_logic.layout.scripting.runtime.action.ExpectedFailureActionOp.ExpectedFailureAction;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The CommandDispatcher executes {@link CommandHandler}s after checking their executability
 *
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class CommandDispatcher {

    /** convenience state for not allowed commands */
	public static final ExecutableState EXEC_STATE_NOT_IN_STATE =
		ExecutableState.createDisabledState(I18NConstants.ERROR_NOT_IN_STATE);

	/**
	 * Command argument that prevents a user confirmation even for commands that have the
	 * {@link com.top_logic.tool.boundsec.CommandHandler.Config#getConfirm()} option set.
	 */
	private static final TypedAnnotatable.Property<Boolean> COMMAND_APPROVED =
		TypedAnnotatable.property(Boolean.class, "commandApproved", Boolean.FALSE);

	/** singleton instance */
	private static CommandDispatcher instance;

	/**
	 * singleton, private access
	 */
	private CommandDispatcher() {
	}

	/**
	 * Executes the given command.
	 * 
	 * @param command
	 *        The command to execute.
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param component
	 *        The component, on which the command should be executed.
	 * @param someArguments
	 *        The command arguments. See
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @return The result of the command.
	 */
	public final HandlerResult dispatchCommand(CommandHandler command, DisplayContext context,
			LayoutComponent component, Map<String, Object> someArguments) {
		if (command.needsConfirm() && !context.get(COMMAND_APPROVED)) {
			LayoutData layout =
				DefaultLayoutData.newLayoutData(DisplayDimension.px(400), DisplayDimension.px(150));
			ResKey message = CommandHandlerUtil.getConfirmKey(command, component, someArguments);

			CommandModel ok = MessageBox.button(ButtonType.OK,
				approveContext -> dispatchCommand(command, approved(approveContext), component, someArguments));
			return MessageBox
				.newBuilder(MessageType.CONFIRM)
				.layout(layout)
				.message(message)
				.buttons(
					ScriptingRecorder.annotateAsDontRecord(ok),
					ScriptingRecorder.annotateAsDontRecord(MessageBox.button(ButtonType.CANCEL)))
				.confirm(context.getWindowScope());
		} else {
			// Do not record synthetic argument.
			context.reset(COMMAND_APPROVED);
		}

		HandlerResult result;
		if (ScriptingRecorder.isRecordingActive()) {
			Event event = ScriptingRecorder.recordCommand(context, command, component, someArguments);

			// Do actual dispatch
			result = internalDispatch(command, context, component, someArguments);

			if (!result.isSuccess() && !result.isSuspended()) {
				if (event != null) {
					event.setAction(wrapWithExpectedFailure(context, result, event.getAction()));
				}

			}
		} else {
			// Do actual dispatch
			result = internalDispatch(command, context, component, someArguments);
		}
		return result;
	}

	private HandlerResult internalDispatch(CommandHandler command, DisplayContext context,
			LayoutComponent component, Map<String, Object> someArguments) {
		HandlerResult result = internalDispatchCommand(command, context, component, someArguments);
		result.init(component, command);
		if (result.isSuspended()) {
			result.initContinuation(command, component, someArguments);
		}
		return result;
	}

	/**
	 * as {@link #dispatchCommand(CommandHandler, DisplayContext, LayoutComponent, Map)} with empty
	 * arguments
	 * 
	 * @see #dispatchCommand(CommandHandler, DisplayContext, LayoutComponent, Map)
	 */
	public final HandlerResult dispatchCommand(CommandHandler command, DisplayContext context,
			LayoutComponent component) {
		return dispatchCommand(command, context, component, CommandHandler.NO_ARGS);
	}

	/**
	 * Execute the command (BoundCommand or CommandHandler).
	 */
	protected HandlerResult internalDispatchCommand(CommandHandler aCommand, DisplayContext aContext,
			LayoutComponent aComponent, Map<String, Object> someArguments) {
		assert aCommand != null;

        // Cache is used, if read commands shall be handled
		boolean useSecurityRequestCache = BoundComponent.useSecurityRequestCache(aCommand);
		
		boolean installed;
		if (useSecurityRequestCache) {
			// Check, if the cache has been installed at previous calls
			installed = BoundComponent.installRequestCacheIfNotExisting();
        } else {
        	// Uninstall cache, because write commands shall be handled
			BoundComponent.uninstallRequestCache();
			installed = false;
        }

        try {
			ExecutableState theState = resolveExecutableState(aCommand, aComponent, someArguments);
        	if ( ! theState.isExecutable()) {
        	    HandlerResult theResult = new HandlerResult();
        	    theResult.addErrorMessage(theState.getI18NReasonKey());
        	    return theResult;
        	}
    
        	HandlerResult theResult;
			if (someArguments.get(DirtyHandling.SKIP_DIRTY_HANDLING) != null
				|| !DirtyHandling.getInstance().checkDirty(aContext, aCommand, aComponent, someArguments)) {
				if (PerformanceMonitor.isEnabled()) {
					ProcessingInfo processingInfo = aContext.getProcessingInfo();
					processingInfo.setCommandID(aCommand.getID());
					ResKey i18nKey = aCommand.getResourceKey(aComponent);
					if (i18nKey == null) {
						i18nKey = ResKey.text(aCommand.getID());
					}
					processingInfo.setCommandName(i18nKey);
					processingInfo.setProcessingKind(ProcessingKind.COMMAND_EXECUTION);
					processingInfo.setComponentName(aComponent.getName());
				}

				theResult = aComponent.dispatchCommand(aContext, aCommand, someArguments);
        	} else {
        		return HandlerResult.DEFAULT_RESULT;
        	}
    
        	/**
        	 * Post-process the result
        	 */
        	if (theResult != null) {
        	    if (theResult.shallCloseDialog()) {
        	        closeDialog(aComponent, aContext, someArguments);
        	        theResult.setCloseDialog(false);
        	    }
        	}
    
        	return theResult;
		} catch (TopLogicException ex) {
			HandlerResult error = new HandlerResult();
			error.setException(ex);
			return error;
		} catch (Throwable ex) {
			HandlerResult error = new HandlerResult();
			TopLogicException tlEx = new TopLogicException(com.top_logic.util.I18NConstants.INTERNAL_ERROR, ex);
			tlEx.initSeverity(ErrorSeverity.SYSTEM_FAILURE);
			error.setException(tlEx);
			return error;
        }
        finally {
			if (installed) {
            	BoundComponent.uninstallRequestCache();
            }
        }
    }

	/**
	 * Marks the command execution as approved by the user.
	 * 
	 * <p>
	 * When passing a command with approved arguments to the
	 * {@link #dispatchCommand(CommandHandler, DisplayContext, LayoutComponent, Map)} method, no
	 * user confirmation is requested, even if the command has the
	 * {@link com.top_logic.tool.boundsec.CommandHandler.Config#getConfirm()} option set.
	 * </p>
	 * 
	 * @param context
	 *        Command context
	 * @return Modified command context to pass to
	 *         {@link #dispatchCommand(CommandHandler, DisplayContext, LayoutComponent, Map)}.
	 */
	public static DisplayContext approved(DisplayContext context) {
		context.set(COMMAND_APPROVED, Boolean.TRUE);
		return context;
	}

	private ApplicationAction wrapWithExpectedFailure(DisplayContext context, HandlerResult result,
			ApplicationAction commandAction) {
		ExpectedFailureAction failureExpectation = TypedConfiguration.newConfigItem(ExpectedFailureAction.class);
		failureExpectation.setImplementationClass(ExpectedFailureActionOp.class);
		failureExpectation.setFailureAction(commandAction);
	
		ResKey errorKey = getErrorKey(result);
		if (errorKey != null) {
			String failureMessage = context.getResources().getString(errorKey);
			failureExpectation.setExpectedFailureMessage(failureMessage);
		}
		return failureExpectation;
	}

	private ResKey getErrorKey(HandlerResult result) {
		TopLogicException problem = result.getException();
		if (problem != null) {
			return getErrorKey(problem);
		}
	
		if (result.getEncodedErrors().size() > 0) {
			return result.getEncodedErrors().get(0);
		}
	
		return null;
	}

	private ResKey getErrorKey(TopLogicException problem) {
		ResKey errorKey;
		errorKey = problem.getErrorKey();
	
		// Find error key representing maximum detail.
		Throwable failure = problem;
		while (failure.getCause() != null) {
			failure = failure.getCause();
			if (failure instanceof I18NRuntimeException) {
				errorKey = ((I18NRuntimeException) failure).getErrorKey();
			}
		}
		return errorKey;
	}

	/**
     * Get the single instance of {@link CommandDispatcher}
     */
    public static synchronized CommandDispatcher getInstance() {
    	if (instance == null) {
    		instance = new CommandDispatcher();
      	}
    	return instance;
    }

    /**
     * Checks the security.
     * In some cases the object to check the security on depends on the given parameters.
     * this means, the command itself must resolve this object
     */
	private static boolean checkSecurity(CommandHandler aCommand, BoundChecker aChecker, Object model,
			Map<String, Object> someArguments) {
		return aCommand.checkSecurity((LayoutComponent) aChecker, model, someArguments);
    }

    /**
     * This method does all the checks needed to decide if a command is executable.
     * This involves the check of business rules, view state and security.
     *
     * @param aCommand       the command to be checked, must not be <code>null</code>
     * @param aChecker       the component used as checker to ask for security.
     * @param someArguments  a map of parameters
     * @return an {@link ExecutableState} containing information about the executability of the command
     */
    public static final ExecutableState resolveExecutableState(CommandHandler aCommand, LayoutComponent aChecker, Map<String, Object> someArguments) {
		if (!ComponentUtil.isValid(aChecker.getModel())) {
			return ExecutableState.NO_EXEC_INVALID;
		}

		Object model = CommandHandlerUtil.getTargetModel(aCommand, aChecker, someArguments);
		ExecutableState commandState = aCommand.isExecutable(aChecker, model, someArguments);
		if (!commandState.isExecutable()) {
			return commandState;
		}

        // system commands may bypass the security check 
        // check business rules
		boolean isSystem = aCommand.getCommandGroup().isSystemGroup();
		if (isSystem) {
			return ExecutableState.EXECUTABLE;
        }

		ExecutableState approvalState =
			CommandApprovalService.getInstance().isExecutable(aChecker, aCommand.getCommandGroup(), aCommand.getID(),
				model, someArguments);
		if (!approvalState.isExecutable()) {
			return approvalState;
		}

        if (aChecker instanceof BoundChecker) {
        	Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
			if (!SimpleBoundCommandGroup.isAllowedCommandGroup(currentPerson, aCommand.getCommandGroup())) {
				return ExecutableState.NO_EXEC_RESTRICTED_USER;
        	}
        	
			if (!checkSecurity(aCommand, (BoundChecker) aChecker, model, someArguments)) {
                return ExecutableState.NO_EXEC_PERMISSION;
            }
        }

		return ExecutableState.EXECUTABLE;
    }

    /**
     * Closes a dialog.
     * If closing fails, no exception is propagated.
     */
    public static void closeDialog(LayoutComponent aDialog, DisplayContext aContext, Map<String, Object> someArguments) {
        if (aDialog.openedAsDialog()) {
            CommandHandlerFactory theFactory      = CommandHandlerFactory.getInstance();
            CommandHandler        theCloseHandler = theFactory.getHandler(CloseModalDialogCommandHandler.HANDLER_NAME);

			CommandHandlerUtil.handleCommand(theCloseHandler, aContext, aDialog, someArguments);
        }
    }
}
