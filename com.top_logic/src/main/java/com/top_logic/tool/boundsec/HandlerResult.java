/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.CommandContextDescription;
import com.top_logic.util.error.ContextDescription;
import com.top_logic.util.error.TopLogicException;

/**
 * The result object of a {@link com.top_logic.tool.boundsec.CommandHandler}.
 * 
 * Or the result of a command function at a (Bound-)Component.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class HandlerResult extends LazyTypedAnnotatable implements ContextDescription {

    /**
     * A successful (no errors or exception), immutable result for not closing a
     * dialog. you may use it for simple cases
     */
	public static final DefaultHandlerResult DEFAULT_RESULT = new DefaultHandlerResult();

	/**
	 * Property that signals whether a currently open dialog should be closed after the command has
	 * finished.
	 */
	public static final Property<Boolean> CLOSE_DIALOG =
		TypedAnnotatable.property(Boolean.class, "closeDialog", Boolean.FALSE);

	/**
	 * Generic array of all "processed" objects of the currently executing command.
	 * 
	 * @deprecated Should be replaced with a custom
	 *             {@link com.top_logic.basic.col.TypedAnnotatable.Property} to make communication
	 *             between commands explicit.
	 * 
	 * @see #getProcessed()
	 */
	@Deprecated
	public static final Property<List<Object>> PROCESSED = TypedAnnotatable.propertyList("processed");

    /**
	 * List of encoded error messages describing errors that occurred when processing command, e.g.
	 * a failed commit.
	 */
	private List<ResKey> _encodedErrors;

    /**
     * store an exception that occurred during processing.
     */
    private TopLogicException         exception      = null;

	private ContextDescription _description;

	private Command _errorContinuation = Command.DO_NOTHING;

	private ErrorSeverity _errorSeverity;

	private ResKey _errorTitle;

	private ResKey _errorMessage;

    /**
     * Default constructor.
     */
    public HandlerResult() {
		super();
    }

    /**
     * CTor to set closeDialog.
     */
    public HandlerResult(boolean aCloseDialog) {
		set(CLOSE_DIALOG, aCloseDialog);
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return The string representation of this instance.
     */
    @Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getName());
		result.append("(");
		result.append("success: ");
		result.append(this.isSuccess());
		List<ResKey> errors = getEncodedErrors();
		if (!errors.isEmpty()) {
			result.append(", errors: ");
			result.append(errors);
		}
		if (exception != null) {
			result.append(", exception: [");
			appendEx(result, exception);
			result.append("]");
		}
		result.append(")");
		return result.toString();
    }

	private void appendEx(StringBuilder result, Throwable ex) {
		if (ex instanceof I18NFailure) {
			result.append(((I18NFailure) ex).getErrorKey());
		} else {
			result.append(ex.getClass().getName());
			String message = ex.getMessage();
			if (message != null) {
				result.append(": ");
				result.append(message);
			}
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			result.append(", ");
			appendEx(result, cause);
		}
	}

    /**
     * Set the close dialog flag to tell component to close the command's
     * dialog.
     */
	public void setCloseDialog(boolean value) {
		set(CLOSE_DIALOG, value);
    }

    /**
	 * Create a new {@link #isSuspended() suspended} {@link HandlerResult}.
	 * 
	 * <p>
	 * A suspended command can be {@link #resume(DisplayContext, Map) resumed} later on. The general
	 * pattern is:
	 * </p>
	 * 
	 * <xmp>
	 * // In some CommandHandler:
	 * public HandlerResult handleCommand(..., Map<String, Object> arguments) {
	 *    ...
	 *    if (someCondition && !arguments.containsKey("isResumed")) {
	 *       HandlerResult suspended = HandlerResult.suspended();
	 *       
	 *       Command continuation = suspended.resumeContinuation(Collections.singletonMap("isResumed", someValue);
	 *       MessageBox.open(..., MessageBox.button(..., continuation));
	 *       
	 *       return suspended;
	 *    }
	 *    
	 *    // Normal execution.
	 *    ...
	 * }
	 * </xmp>
	 * 
	 * <p>
	 * Note: A command execution can only be suspended correctly, if the decision about the suspend
	 * is done by the first handler executed in a potential chain of handlers. If a suspend happens
	 * in the middle of a chain, changes done be handlers before the suspending handler cannot be
	 * undone. Therefore, there is no good reason, why an existing {@link HandlerResult} should be
	 * suspended (but only a fresh one can be created in state `suspended`).
	 * </p>
	 * 
	 * @since Ticket #14330
	 * 
	 * @see #isSuspended()
	 * @see #resume(DisplayContext, Map)
	 */
	public static HandlerResult suspended() {
		return new SuspendedResult();
	}

	/**
	 * Whether the current execution is/should be {@link #suspended() suspended}.
	 * 
	 * @since Ticket #14330
	 * 
	 * @see #suspended()
	 */
	public boolean isSuspended() {
		return false;
	}

	/**
	 * Called, after execution of a {@link #suspended() suspended} command has completed.
	 * 
	 * @since Ticket #14330
	 * 
	 * @param command
	 *        The outermost command that was executed.
	 * @param component
	 *        The context component, on which the command was invoked.
	 * @param arguments
	 *        The original arguments given to the command.
	 */
	@FrameworkInternal
	public void initContinuation(CommandHandler command, LayoutComponent component, Map<String, Object> arguments) {
		// Ignore.
	}

	/**
	 * Execute the given {@link Command} after the currently {@link #suspended() suspended}
	 * execution is {@link #resume(DisplayContext, Map) resumed}.
	 * 
	 * @since Ticket #14330
	 * 
	 * @param continuation
	 *        A continuation to be executed after {@link #resume(DisplayContext, Map)}.
	 */
	@FrameworkInternal
	public void appendContinuation(Command continuation) {
		throw notSuspended();
	}

	/**
	 * Creates a {@link Command} that {@link #resume(DisplayContext, Map) resumes} a
	 * {@link #suspended() suspended} execution without additional arguments.
	 * 
	 * @since Ticket #14330
	 * 
	 * @see #resume(DisplayContext, Map)
	 */
	public Command resumeContinuation() {
		return resumeContinuation(Collections.<String, Object> emptyMap());
	}

	/**
	 * Creates a {@link Command} that {@link #resume(DisplayContext, Map) resumes} a
	 * {@link #suspended() suspended} execution with some additional arguments.
	 *
	 * @since Ticket #14330
	 * 
	 * @see #resume(DisplayContext, Map)
	 */
	public Command resumeContinuation(final Map<String, Object> additionalArguments) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext resumeContext) {
				return resume(resumeContext, additionalArguments);
			}
		};
	}

	/**
	 * Same as {@link #resume(DisplayContext, Map)} without additional arguments.
	 */
	public final HandlerResult resume(DisplayContext resumeContext) {
		return resume(resumeContext, Collections.<String, Object> emptyMap());
	}

	/**
	 * Resume (execute again) the {@link #suspended() suspended} command of a previous operation.
	 *
	 * @since Ticket #14330
	 * 
	 * @param resumeContext
	 *        The new {@link DisplayContext} in which to resume the command execution.
	 * @param additionalArguments
	 *        Arguments to pass additionally to the resumed command. This can be used to alter the
	 *        behavior of the command in the resume phase.
	 * @return The new {@link HandlerResult} produced by the resumed command execution.
	 * 
	 * @see #suspended()
	 */
	public HandlerResult resume(DisplayContext resumeContext, Map<String, Object> additionalArguments) {
		throw notSuspended();
	}

	private IllegalStateException notSuspended() {
		return new IllegalStateException("Not suspended.");
	}

    /**
     * Ask if a Command Handlers work did not fail.
     * 
     * @return true for no errors and no exception, false otherwise
     */
    public boolean isSuccess() {
		return getEncodedErrors().isEmpty() && (getException() == null) && !isSuspended();
    }

    /**
	 * Sets/overwrites an exception that occurred during processing.
	 * 
	 * @see #error(ResKey, Throwable)
	 * 
	 * @param anException
	 *        may be null to clear
	 */
	public void setException(TopLogicException anException) {
        exception = anException;
    }

    /**
     * getter for exception
     * 
     * @return the stored exception, may be null
     */
	public TopLogicException getException() {
        return exception;
    }

    /**
     * <code>true</code>, if a dialog should be closed.
     */
    public boolean shallCloseDialog() {
		return get(CLOSE_DIALOG);
    }

    /**
	 * The list of I18N keys with optionally encoded arguments that describe the failures occurred.
	 * 
	 * @return Keys for an internationalized messages that describes the failures.
	 * 
	 * @see #addError(ResKey)
	 */
	public List<ResKey> getEncodedErrors() {
        if (_encodedErrors == null) {
			return Collections.emptyList();
        }

        return _encodedErrors;
    }

	/**
	 * Check, if there are error entries in this result.
	 * <p/>
	 * <em>Does not check for exceptions!</em>
	 * <p/>
	 * 
	 * @Deprecated Because it's too easy to call {@link #hasErrors()} and forgetting to check for
	 *             exceptions, too.
	 * 
	 * @return <code>true</code>, if this result contains errors.
	 */
	@Deprecated
    public boolean hasErrors() {
		return !getEncodedErrors().isEmpty();
    }

	/**
	 * Sets all encoded error messages at once.
	 * 
	 * @see #addError(ResKey)
	 * @see #getEncodedErrors()
	 */
	public void setEncodedErrors(List<ResKey> encodedErrors) {
        _encodedErrors = encodedErrors;
    }

	/**
	 * Adds an I18N key with optionally encoded arguments that describes the failure.
	 * 
	 * @param encodedMessage
	 *        Key for an internationalized message that describes the failure occurred.
	 * 
	 * @see #getEncodedErrors()
	 */
	public void addErrorMessage(ResKey encodedMessage) {
		internalAddError(encodedMessage);
    }

	/**
	 * Adds a plain message described by solely an I18N key.
	 * 
	 * @param errorKey
	 *        Key for an internationalized message that describes the failure occurred.
	 * 
	 * @see #getEncodedErrors()
	 */
	public void addError(ResKey errorKey) {
		addErrorMessage(errorKey);
	}

	/**
	 * Adds an error message described by an I18N key taking a single argument.
	 * 
	 * @param errorMessageKey
	 *        Key for an internationalized message that describes the failure occurred.
	 * @param argument
	 *        The single argument to the resource message.
	 * @see ResKey#message(ResKey, Object...)
	 * 
	 * @see #getEncodedErrors()
	 */
	public void addErrorMessage(ResKey errorMessageKey, Object argument) {
		internalAddError(ResKey.message(errorMessageKey, argument));
	}

	/**
	 * Adds an error message described by an I18N key with arguments.
	 * 
	 * @param errorMessageKey
	 *        Key for an internationalized message that describes the failure occurred.
	 * @param arguments
	 *        The arguments to the resource message.
	 * @see ResKey#message(ResKey, Object...)
	 * 
	 * @see #getEncodedErrors()
	 */
	public void addErrorMessage(ResKey errorMessageKey, Object... arguments) {
		internalAddError(ResKey.message(errorMessageKey, arguments));
	}

	/**
	 * Actually add the given encoded error message to the messages of this instance.
	 */
	protected void internalAddError(ResKey encodedMessage) {
		if (_encodedErrors == null) {
			_encodedErrors = new ArrayList<>();
		}
		_encodedErrors.add(encodedMessage);
	}
	
	/**
	 * Adds a plain (already internationalized) error message to be displayed directly to the user.
	 * 
	 * @param literalErrorText
	 *        The message to display directly without further internationalization.
	 */
	public void addErrorText(String literalErrorText) {
		addErrorMessage(ResKey.text(literalErrorText));
	}

    /**
	 * Return the list of processed objects.
	 * 
	 * @return The list of processed objects, never <code>null</code>.
	 * 
	 * @deprecated Replace with custom {@link com.top_logic.basic.col.TypedAnnotatable.Property} to
	 *             make communication between commands explicit.
	 */
	@Deprecated
    public List<?> getProcessed() {
		return get(PROCESSED);
    }

    /**
	 * Add the given object to the list of processed objects.
	 * 
	 * @param obj
	 *        The processed object.
	 * 
	 * @deprecated Replace with custom {@link com.top_logic.basic.col.TypedAnnotatable.Property} to
	 *             make communication between commands explicit.
	 */
	@Deprecated
	public void addProcessed(Object obj) {
		mkList(PROCESSED).add(obj);
    }

	/**
	 * Adds all given objects to the list of processed objects.
	 * 
	 * @param list
	 *        The processed objects.
	 * 
	 * @deprecated Replace with custom {@link com.top_logic.basic.col.TypedAnnotatable.Property} to
	 *             make communication between commands explicit.
	 */
	@Deprecated
	public void addAllProcessed(List<?> list) {
		mkList(PROCESSED).addAll(list);
	}

    /**
     * Appends all errors and processed objects. 
     * 
     * Note the Exception set within
     * the receiver is not replaced by the Exception of the appended
     * <code>aHandlerResult</code>, if there is any within
     * <code>aHandlerResult</code> otherwise the appended exception is kept!
     * If both exceptions are set, the Exception of the appender is reformulated
     * in an error message of <code>this</code>. Thus the intended logic of
     * the <code>isSuccessful()</code> is: after appending, the result of
     * <code>isSuccessful()</code> is <code>false</code> if
     * <code>this</code> or <code>aHandlerResult</code> would have returned
     * <code>false</code> before appending. Similarly
     * <code>shallCloseDialog</code> will return <code>true</code> if and
     * only if one of both calls would have resulted in <code>true</code>
     * before appending.
     * 
     * @param aHandlerResult a null value will be ignored.
     */
    public void appendResult(HandlerResult aHandlerResult) {
        if (aHandlerResult == null) {
            return;
        }
		for (ResKey encodedError : aHandlerResult.getEncodedErrors()) {
        	addErrorMessage(encodedError);
        }
		addAllProcessed(aHandlerResult.getProcessed());
		setCloseDialog(shallCloseDialog() || aHandlerResult.shallCloseDialog());
		TopLogicException theException = aHandlerResult.getException();
        if (exception == null) {
            exception = theException;
        }
        else if (theException != null) {
			addErrorText("appended.exception: " + theException.getMessage());
        }
    }

	/**
	 * Creates a {@link HandlerResult} with the given error key.
	 * 
	 * @see #addError(ResKey)
	 */
	public static HandlerResult error(ResKey errorKey) {
		HandlerResult result = new HandlerResult();
		result.addError(errorKey);
		return result;
	}

	/**
	 * Create a {@link HandlerResult} that contains an error.
	 * 
	 * @param errorKey
	 *        The error key as used in {@link TopLogicException#TopLogicException(Class, String)}.
	 * @param category
	 *        The error category as used in
	 *        {@link TopLogicException#TopLogicException(Class, String)}.
	 * @return A new {@link HandlerResult}.
	 * 
	 * @deprecated Use {@link #error(ResKey)}
	 */
	@Deprecated
	public static HandlerResult error(String errorKey, Class<?> category) {
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(ResPrefix.legacyString(TopLogicException.PREFIX + category.getName()).key(errorKey));
		return result;
	}

	/**
	 * Create a {@link HandlerResult} that contains an error.
	 * 
	 * @param errorKey
	 *        The error key as used in {@link TopLogicException#TopLogicException(Class, String)}.
	 * @param category
	 *        The error category as used in
	 *        {@link TopLogicException#TopLogicException(Class, String)}.
	 * @param ex
	 *        The exception as used in
	 *        {@link TopLogicException#TopLogicException(Class, String, Throwable)} .
	 * @return A new {@link HandlerResult}.
	 * 
	 * @deprecated Use {@link #error(ResKey)}
	 */
	@Deprecated
	public static HandlerResult error(String errorKey, Class<?> category, Throwable ex) {
		HandlerResult result = new HandlerResult();
		result.setException(new TopLogicException(category, errorKey, null, ex));
		return result;
	}

	/**
	 * Create a {@link HandlerResult} that contains an error and log an error.
	 * 
	 * @param logMessage
	 *        The message to use for logging.
	 * @param errorKey
	 *        The error key as used in {@link TopLogicException#TopLogicException(Class, String)}.
	 * @param category
	 *        The error category as used in
	 *        {@link TopLogicException#TopLogicException(Class, String)}.
	 * @param ex
	 *        The exception as used in
	 *        {@link TopLogicException#TopLogicException(Class, String, Throwable)} .
	 * @return A new {@link HandlerResult}.
	 * 
	 * @deprecated Use {@link #error(ResKey)}
	 */
	@Deprecated
	public static HandlerResult error(String logMessage, String errorKey, Class<?> category, Throwable ex) {
		if (logMessage != null) {
			Logger.error(logMessage, ex, category);
		}
		return error(errorKey, category, ex);
	}

	/**
	 * Create a {@link HandlerResult} that contains an error.
	 * 
	 * @param errorKey
	 *        The error key as used in
	 *        {@link TopLogicException#TopLogicException(ResKey,Throwable)}.
	 * @param ex
	 *        The exception as used in
	 *        {@link TopLogicException#TopLogicException(ResKey,Throwable)}.
	 * @return A new {@link HandlerResult}.
	 */
	public static HandlerResult error(ResKey errorKey, Throwable ex) {
		HandlerResult result = new HandlerResult();
		result.setException(new TopLogicException(errorKey, ex));
		return result;
	}

	/**
	 * A {@link HandlerResult} describing the errors of the given
	 * {@link BufferingProtocol}, or <code>null</code>, if no errors have yet
	 * been reported.
	 * 
	 * @param log
	 *        The {@link BufferingProtocol} to take error messages from.
	 * @return A new {@link HandlerResult}, or <code>null</code>, in case of no
	 *         errors.
	 *         
	 * @since TL_5_6_1
	 */
	public static HandlerResult getErrorResult(BufferingProtocol log) {
		if (log.hasErrors()) {
			HandlerResult errorResult = new HandlerResult();
			for (String errorMessage : log.getErrors()) {
				errorResult.addErrorText(errorMessage);
			}
			return errorResult;
		} else {
			return null;
		}
	}

	/**
	 * Initializes the description with a {@link CommandContextDescription}.
	 */
	public void init(LayoutComponent component, BoundCommand command) {
		if (hasDescription()) {
			return;
		}
		internalInit(new CommandContextDescription(component, command, getException()));
	}

	/**
	 * Initializes a custom {@link ContextDescription}.
	 */
	public void init(ContextDescription description) {
		if (hasDescription()) {
			return;
		}

		internalInit(description);
	}

	private void internalInit(ContextDescription description) {
		_description = description;
	}

	/**
	 * Whether a {@link #getContextDescription()} was set.
	 */
	public boolean hasDescription() {
		return _description != null;
	}

	/**
	 * Description of the command being executed, or <code>null</code>, if not
	 * {@link #hasDescription()}.
	 */
	public ContextDescription getContextDescription() {
		return _description;
	}

	/**
	 * An icon to represent a message to the user.
	 * 
	 * @see MessageType#getTypeImage()
	 * @see #setErrorSeverity(ErrorSeverity)
	 */
	public ErrorSeverity getErrorSeverity() {
		if (_errorSeverity != null) {
			return _errorSeverity;
		}

		if (getException() != null) {
			return getException().getSeverity();
		}

		return ErrorSeverity.ERROR;
	}

	/**
	 * @see #getErrorSeverity()
	 */
	public void setErrorSeverity(ErrorSeverity severity) {
		_errorSeverity = severity;
	}

	@Override
	public ResKey getErrorTitle() {
		if (_errorTitle != null) {
			return _errorTitle;
		}

		if (getException() != null) {
			return getException().getErrorKey();
		}

		if (_description != null) {
			return _description.getErrorTitle();
		}

		return com.top_logic.util.I18NConstants.INTERNAL_ERROR;
	}

	/**
	 * Sets a custom error title.
	 * 
	 * @see #getErrorTitle()
	 */
	public void setErrorTitle(ResKey errorTitle) {
		_errorTitle = errorTitle;
	}

	@Override
	public ResKey getErrorMessage() {
		if (_errorMessage != null) {
			return _errorMessage;
		}

		if (getException() != null) {
			return getException().getDetails();
		}

		if (_description != null) {
			return _description.getErrorMessage();
		}

		return ResKey.text(null);
	}
	
	/**
	 * Sets a custom error message.
	 * 
	 * @see #getErrorMessage()
	 */
	public void setErrorMessage(ResKey errorMessage) {
		_errorMessage = errorMessage;
	}

	/**
	 * Action to be executed after the result's error message is acknowledged.
	 */
	public Command errorContinuation() {
		return _errorContinuation;
	}

	/**
	 * Replaces the {@link #errorContinuation()} with the given action.
	 */
	public void setErrorContinuation(Command continuation) {
		_errorContinuation = continuation;
	}

	@Override
	public void logError() {
		if (hasDescription()) {
			getContextDescription().logError();
		}
	}

	/**
	 * Appends the given action to the {@link #errorContinuation()}.
	 */
	public void addErrorContinuation(final Command command) {
		final Command before = _errorContinuation;
		_errorContinuation = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				HandlerResult result = before.executeCommand(context);
				if (result.isSuccess()) {
					return command.executeCommand(context);
				}
				return result;
			}
		};
	}

}
