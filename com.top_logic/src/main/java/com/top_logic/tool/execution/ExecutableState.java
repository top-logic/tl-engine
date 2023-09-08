/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Description of the executability of a command.
 * 
 * @see CommandHandler#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, java.util.Map)
 * @see ExecutabilityModel#getExecutability()
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public final class ExecutableState implements Comparable<ExecutableState> {

	/**
	 * The visibility and executability of a button.
	 * 
	 * @see ExecutableState#visibility()
	 */
	public enum CommandVisibility {

		/**
		 * The button is visible and executable.
		 */
		VISIBLE,

		/**
		 * The button is visible but in disabled state and cannot be executed.
		 */
		DISABLED,

		/**
		 * The button is not visible and cannot be executed.
		 */
		HIDDEN;

	}

    /**
	 * Internal visibility state marking a command as {@link #isExecutable() executable}.
	 * 
	 * @deprecated Use {@link #EXECUTABLE}, or {@link CommandVisibility#VISIBLE}
	 */
	@Deprecated
	public static final int VISIBILITY_VISIBLE = CommandVisibility.VISIBLE.ordinal();

	/**
	 * Internal visibility state marking a command as {@link #isDisabled() disabled}.
	 * 
	 * @deprecated Use {@link #createDisabledState(ResKey)} or {@link CommandVisibility#DISABLED}
	 */
	@Deprecated
	public static final int VISIBILITY_DISABLED = CommandVisibility.DISABLED.ordinal();

	/**
	 * Internal visibility state marking a command as {@link #isHidden() hidden}.
	 * 
	 * @deprecated Use {@link #NOT_EXEC_HIDDEN} or {@link CommandVisibility#HIDDEN}
	 */
	@Deprecated
	public static final int VISIBILITY_HIDDEN = CommandVisibility.HIDDEN.ordinal();
	
	/** Reason key for a hidden command. */
	public static final ResKey NOT_EXEC_HIDDEN_REASON = I18NConstants.ERROR_HIDDEN;

	/**
	 * Common {@link ExecutableState} for an {@link #isExecutable() executable} command.
	 */
	public static final ExecutableState EXECUTABLE = new ExecutableState(CommandVisibility.VISIBLE, ResKey.NONE);

	/**
	 * Common {@link ExecutableState} for a {@link #isHidden() hidden} command.
	 */
	public static final ExecutableState NOT_EXEC_HIDDEN =
		new ExecutableState(CommandVisibility.HIDDEN, NOT_EXEC_HIDDEN_REASON);

	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because of a
	 * missing model.
	 */
	public static final ExecutableState	NO_EXEC_NO_MODEL = createDisabledState(I18NConstants.ERROR_NO_MODEL);
	
	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because the
	 * current user is restricted.
	 */
	public static final ExecutableState	NO_EXEC_RESTRICTED_USER = createDisabledState(I18NConstants.ERROR_RESTRICTED_USER);

	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because the
	 * current user is restricted.
	 */
	public static final ExecutableState NO_EXEC_LICENSE_INVALID =
		createDisabledState(I18NConstants.ERROR_LICENSE_INVALID);

	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because the
	 * current model is historic.
	 */
	public static final ExecutableState NO_EXEC_HISTORIC = createDisabledState(I18NConstants.ERROR_HISTORIC_DATA);

	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because the
	 * current model is not historic.
	 */
	public static final ExecutableState NO_EXEC_CURRENT = createDisabledState(I18NConstants.ERROR_CURRENT_DATA);

	/**
	 * Disabled executable state that there are no current data for an historic object.
	 * 
	 * @see HasCurrentRevisionRule
	 */
	public static final ExecutableState NO_EXEC_NO_CURRENT_DATA =
		createDisabledState(I18NConstants.ERROR_NO_CURRENT_DATA);

	/**
	 * Common {@link ExecutableState} for a command {@link #isDisabled() disabled} because the
	 * current model is invalid.
	 */
	public static final ExecutableState NO_EXEC_INVALID = createDisabledState(I18NConstants.ERROR_INVALID_MODEL);

	/**
	 * {@link ExecutableState} disabling a command with "not supported" reason.
	 */
	public static final ExecutableState NO_EXEC_NOT_SUPPORTED =
		createDisabledState(I18NConstants.ERROR_MODEL_NOT_SUPPORTED);

	/**
	 * Disabled display that reports missing permissions as reason.
	 */
	public static final ExecutableState NO_EXEC_PERMISSION = createDisabledState(I18NConstants.ERROR_NO_PERMISSION);

    /**
     * @deprecated use a concrete reason for disabled commands
     */
    @Deprecated
	public static final ResKey NOT_EXEC_DISABLED_REASON = I18NConstants.ERROR_DISABLED;

	/**
	 * @deprecated use {@link #createDisabledState(ResKey)} and use concrete messeages
	 */
	@Deprecated
	public static final ExecutableState NOT_EXEC_DISABLED =
		ExecutableState.createDisabledState(NOT_EXEC_DISABLED_REASON);

	private static final Property<Boolean> ALL_VISIBLE = TypedAnnotatable.property(Boolean.class, "allVisible", Boolean.FALSE);

	private final CommandVisibility _visibility;

	private final ResKey _reasonKey;

	/**
	 * Creates an {@link ExecutableState}.
	 *
	 * @param visibility
	 *        See {@link #visibility()}.
	 * @param reasonKey
	 *        See {@link #getI18NReasonKey()}.
	 */
	public ExecutableState(CommandVisibility visibility, ResKey reasonKey) {
		_visibility = visibility;
		_reasonKey = reasonKey;

		if (reasonKey == null && !isExecutable()) {
			throw new IllegalArgumentException("No reason defined for denial of execution");
		}

	}

	/**
	 * Creates a {@link ExecutableState} from a plain resource key without arguments..
	 * 
	 * @param visibility
	 *        See {@link #getVisibility()}.
	 * @param reasonKey
	 *        See {@link #getI18NReasonKey()}.
	 * 
	 * @deprecated Use {@link #ExecutableState(CommandVisibility, ResKey)}
	 */
	@Deprecated
	public ExecutableState(int visibility, ResKey reasonKey) {
		this(fromOrdinal(visibility), reasonKey);
	}

	private static CommandVisibility fromOrdinal(int visibility) {
		if ((visibility < 0 || visibility > CommandVisibility.values().length)) {
			throw new IllegalArgumentException("Given visibility is invalid.");
	    }
		return CommandVisibility.values()[visibility];
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + "executable: " + isExecutable()
			+ ", visibility: " + _visibility + ", reason: '" + _reasonKey + "']";
	}

	/**
	 * Whether the command should be is executable.
	 */
	public final boolean isExecutable() {
		return visibility() == CommandVisibility.VISIBLE;
	}

	/**
	 * The internal visibility state.
	 * 
	 * <p>
	 * Note: the states are used to compare {@link ExecutableState}s
	 * </p>
	 * 
	 * @see #VISIBILITY_VISIBLE
	 * @see #VISIBILITY_DISABLED
	 * @see #VISIBILITY_HIDDEN
	 * 
	 * @deprecated Use {@link #visibility()}
	 */
	@Deprecated
	public final int getVisibility() {
		return visibility().ordinal();
	}

	/**
	 * The {@link CommandVisibility} of this {@link ExecutableState}.
	 * 
	 * <p>
	 * If the {@link CommandVisibility} is {@link CommandVisibility#DISABLED}, {@link #getI18NReasonKey()} gives a
	 * reason why.
	 * </p>
	 */
	public final CommandVisibility visibility() {
		if (allVisible()) {
			return _visibility == CommandVisibility.HIDDEN ? CommandVisibility.DISABLED : _visibility;
		}
		return (_visibility);
	}

	/**
	 * Whether the command should be displayed in disabled state.
	 * 
	 * @see #isExecutable()
	 */
	public final boolean isDisabled() {
		if (allVisible()) {
			return !isExecutable();
		}
		return _visibility == CommandVisibility.DISABLED;
	}

	/**
	 * Whether the command should be hidden.
	 * 
	 * @see #isVisible()
	 */
	public final boolean isHidden() {
		if (allVisible()) {
			return false;
		}
		return _visibility == CommandVisibility.HIDDEN;
	}
	
	/**
	 * Whether the command should be visible
	 */
	public final boolean isVisible() {
		return !isHidden();
	}

	/**
	 * The internationalized message explaining, why the command is disabled.
	 * 
	 * @see #getI18NReasonKey()
	 */
	public final String getReason() {
		return Resources.getInstance().getString(_reasonKey);
	}

	/**
	 * I18N key that describes why a command is not executable.
	 * 
	 * <p>
	 * Only relevant, if {@link #visibility()} is {@link CommandVisibility#DISABLED}.
	 * </p>
	 */
	public final ResKey getI18NReasonKey() {
		return _reasonKey;
	}

	/**
	 * This {@link ExecutableState} is less than another if the visibility is more restricted.
	 * 
	 * "Note: this class has a natural ordering that is inconsistent with equals."
	 * 
	 * @param other
	 *        The other {@link ExecutableState}
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExecutableState other) {
		return other.visibility().ordinal() - visibility().ordinal();
	}

    /** 
     * Create a new ExecutableState for some disabled case.
     * 
     * @param aI18nReasonKey Resource explaining why something is disabled.
     */
	public static ExecutableState createDisabledState(ResKey aI18nReasonKey) {
		return new ExecutableState(CommandVisibility.DISABLED, aI18nReasonKey);
    }

	/**
	 * Calculating the {@link ExecutableState} failed.
	 * <p>
	 * The error will be logged.
	 * </p>
	 * 
	 * @param exception
	 *        Is not allowed to be null, as that would make debugging such an error unnecessary
	 *        hard. Null will therefore cause an additional {@link NullPointerException} to be
	 *        logged. But the method will not fail, as failing during error handling has to be
	 *        avoided at all times.
	 */
	public static ExecutableState createErrorOccurredState(Throwable exception) {
		String causeMessage = getMessageNullsafe(exception);
		String message = "Calculating the ExecutableState failed. Cause: " + causeMessage;
		logError(new RuntimeException(message, exception));
		return createDisabledState(createErrorOccurredI18n(causeMessage));
	}

	private static ResKey createErrorOccurredI18n(String message) {
		return I18NConstants.ERROR_OCCURRED__MESSAGE.fill(message);
	}

	private static String getMessageNullsafe(Throwable exception) {
		if (exception == null) {
			String message = "The exception must not be null.";
			logError(new NullPointerException(message));
			return "Unknown failure.";
		}
		return exception.getMessage();
	}

	private static void logError(Throwable exception) {
		Logger.error(exception.getMessage(), exception, ExecutableState.class);
	}

	/**
	 * Utility to switch between {@link #EXECUTABLE} and {@link #NOT_EXEC_HIDDEN} depending on a
	 * condition.
	 */
	public static ExecutableState visibleIf(boolean condition) {
		return condition ? EXECUTABLE : NOT_EXEC_HIDDEN;
	}

	/**
	 * Combines this {@link ExecutableState} with the given one resulting in an
	 * {@link ExecutableState} with the strongest restriction.
	 */
	public ExecutableState combine(ExecutableState otherState) {
		if (otherState.compareTo(this) < 0) {
			return otherState;
		} else {
			return this;
		}
	}

	/**
	 * Whether the session property that even hidden commands are displayed is enabled.
	 */
	public static boolean allVisible() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return false;
		}
		return context.get(ALL_VISIBLE);
	}

	/**
	 * Enables the session property that even hidden commands are displayed.
	 */
	public static void setAllVisible(boolean value) {
		TLContext context = TLContext.getContext();
		if (context != null) {
			if (value) {
				context.set(ALL_VISIBLE, value);
			} else {
				context.reset(ALL_VISIBLE);
			}
		}
	}

}
