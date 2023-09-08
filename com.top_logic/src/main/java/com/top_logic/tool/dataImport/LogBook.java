/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * The LogBook holds the log entries of a progress.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class LogBook {

    /** The I18N key suffix for counter logging if errors occurred. */
    private static final String ERROR_SUFFIX = "_error";

    /** The I18N key suffix for counter logging if duplicates occurred. */
    private static final String DUPLICATE_SUFFIX = "_dup";

    /** The resource prefix to use for internationalized log entries. */
	private ResPrefix resPrefix;

    /** Holds the progress info. */
    private DefaultProgressInfo progress;

    /** Holds the progress result. */
    private ProgressResult result;

    /** Flag whether messages should be added to result. */
    private boolean logToResult;

    /** Flag whether messages should be shown in progress component GUI. */
    private boolean logToGUI;

    /** Flag whether messages should be logged to default logger. */
    private boolean logToDefaultLogger;

    /** Holds the caller for default logger. */
    private Object caller;

    /** Holds the instance of the resources. */
    private Resources resources;

    // I18Ned Strings for message types
    private String info, warn, error, fatal;

    /** Stores how often a message of a type was logged. */
    private Map requests = new HashMap();

    /** The maximum amount of messages of a type which will be logged. */
    private int maxMessageCount = Integer.MAX_VALUE;



    /**
     * Creates a new instance of this class.
     */
    public LogBook() {
		this(ResPrefix.GLOBAL, new DefaultProgressInfo(), new ProgressResult());
    }

    /**
     * Creates a new instance of this class.
     */
	public LogBook(ResPrefix resPrefix) {
        this(resPrefix, new DefaultProgressInfo(), new ProgressResult());
    }

    /**
     * Creates a new instance of this class.
     */
	public LogBook(ResPrefix resPrefix, DefaultProgressInfo progress) {
        this(resPrefix, progress, new ProgressResult());
    }

    /**
     * Creates a new instance of this class.
     */
	public LogBook(ResPrefix resPrefix, ProgressResult result) {
        this(resPrefix, new DefaultProgressInfo(), result);
    }

    /**
     * Creates a new instance of this class.
     */
	public LogBook(ResPrefix resPrefix, DefaultProgressInfo progress, ProgressResult result) {
        this.resPrefix = resPrefix;
        this.progress = progress;
        this.result = result;
        this.caller = this;
        this.logToResult = true;
        this.logToGUI = true;
        this.logToDefaultLogger = true;
        initResources();
    }

    /**
     * Creates a new instance of this class.
     */
	public LogBook(ResPrefix resPrefix, DefaultProgressInfo progress, Object aCaller) {
        this(resPrefix, progress, new ProgressResult());
        setCaller(aCaller);
    }



    /**
     * Gets the resources instance for the current user.
     */
    private void initResources() {
        resources = Resources.getInstance();
		info = resources.getString(resPrefix.key("info"), StringServices.EMPTY_STRING);
        if (!StringServices.isEmpty(info)) info += " ";
		warn = resources.getString(resPrefix.key("warn"), StringServices.EMPTY_STRING);
        if (!StringServices.isEmpty(warn)) warn += " ";
		error = resources.getString(resPrefix.key("error"), StringServices.EMPTY_STRING);
        if (!StringServices.isEmpty(error)) error += " ";
		fatal = resources.getString(resPrefix.key("fatal"), StringServices.EMPTY_STRING);
        if (!StringServices.isEmpty(fatal)) fatal += " ";
    }



    /**
     * Sets the logToResult flag.
     *
     * @param logToResult
     *        the flag to set
     */
    public void setLogToResult(boolean logToResult) {
        this.logToResult = logToResult;
    }

    /**
     * Sets the logToGUI flag.
     *
     * @param logToGUI
     *        the flag to set
     */
    public void setLogToGUI(boolean logToGUI) {
        this.logToGUI = logToGUI;
    }

    /**
     * Sets the logToDefaultLogger flag.
     *
     * @param logToDefaultLogger
     *        the flag to set
     */
    public void setLogToDefaultLogger(boolean logToDefaultLogger) {
        this.logToDefaultLogger = logToDefaultLogger;
    }

    /**
     * This method sets the caller for the default logger.
     *
     * @param caller
     *        the caller to set
     */
    public void setCaller(Object caller) {
        this.caller = caller != null ? caller : this;
    }

    /**
     * This method sets the maxMessageCount. Also resets the message counter.
     *
     * @param maxMessageCount
     *        The maxMessageCount to set.
     */
    public void setMaxMessageCount(int maxMessageCount) {
        this.maxMessageCount = maxMessageCount;
        requests.clear();
    }

    /**
     * Gets the ProgressResult of the progress.
     *
     * @return the ProgressResult of the progress
     */
    public ProgressResult getResult() {
        return this.result;
    }

    /**
     * Gets the DefaultProgressInfo of the progress.
     *
     * @return the DefaultProgressInfo of the progress
     */
    public DefaultProgressInfo getProgress() {
        return this.progress;
    }

    /**
     * Gets the resource prefix for internationalization.
     *
     * @return the resource prefix
     */
	public ResPrefix getResPrefix() {
        return this.resPrefix;
    }

    /**
     * Gets the current Resources instance to avoid many Resources.getInstance() calls.
     *
     * @return current Resources instance
     */
    public Resources getResources() {
        return this.resources;
    }

    /**
     * Gets the translated message of the given key.
     *
     * @param aKey
     *        the i18n key (without prefix)
     * @return the translated message of the given key
     */
    public String getResString(String aKey) {
		return resources.getString(resPrefix.key(aKey));
    }

    /**
     * Gets the translated message of the given key.
     *
     * @param aKey
     *        the i18n key (without prefix)
     * @param aDefaultValue
     *        the default value if the key was not found
     * @return the translated message of the given key
     */
    public String getResString(String aKey, String aDefaultValue) {
		return resources.getString(resPrefix.key(aKey), aDefaultValue);
    }



    /**
     * Checks how many the message with the given key gets logged and suppresses the
     * message, if it was logged too often.
     *
     * @param aI18NKey
     *        the i18n key (without prefix) of the message to log
     * @param aMessage
     *        the already translated message which will be altered or cleared if necessary
     * @return the given message if it was not logged too often, an altered message which
     *         the information that this type of message will not be logged further, if the
     *         limit is reached or <code>null</code>, if the limit was exceeded
     */
    private String checkFrequency(String aI18NKey, String aMessage) {
        int count = Utils.getintValue(requests.get(aI18NKey));
        requests.put(aI18NKey, Integer.valueOf((++count)));
        if (count == maxMessageCount) {
			return aMessage + "\n" + resources.getString(resPrefix.key("noFurtherMessages"));
        }
        return count < maxMessageCount ? aMessage : null;
    }



    /**
     * Logs an info message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     */
    public void info(String aI18NKey) {
		String theMessage = resources.getString(resPrefix.key(aI18NKey));
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, caller);
        }
    }

    /**
     * Logs an info message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     */
    public void info(String aI18NKey, Throwable aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage());
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, aValue, caller);
        }
    }

    /**
     * Logs an info message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void info(String aI18NKey, Throwable aValue, Object aValue2) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage(), aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, aValue, caller);
        }
    }

    /**
     * Logs an info message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log. May be <code>null</code>;
     *        in that case the given value's string representation will be used as message.
     *        This can be used for logging already translated messages.
     * @param aValue
     *        the object to insert into the I18Ned message or to use as message if aI18NKey
     *        is <code>null</code>
     */
    public void info(String aI18NKey, Object aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? StringServices.toString(aValue)
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, caller);
        }
    }

    /**
     * Logs an info message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void info(String aI18NKey, Object aValue1, Object aValue2) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, caller);
        }
    }

    /**
     * Logs an info message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     * @param aValue3
     *        the third object to insert into the I18Ned message
     */
    public void info(String aI18NKey, Object aValue1, Object aValue2, Object aValue3) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2, aValue3);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, caller);
        }
    }

    /**
     * Logs an info message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param values
     *        the objects to insert into the I18Ned message
     */
    public void info(String aI18NKey, Object[] values) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), values);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addInfo(theMessage);
            if (logToGUI) progress.appendMessage(info + theMessage);
            if (logToDefaultLogger) Logger.info(theMessage, caller);
        }
    }


    /**
     * Logs an warning message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     */
    public void warning(String aI18NKey) {
		String theMessage = resources.getString(resPrefix.key(aI18NKey));
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, caller);
        }
    }

    /**
     * Logs a warning message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     */
    public void warning(String aI18NKey, Throwable aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage());
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, aValue, caller);
        }
    }

    /**
     * Logs a warning message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void warning(String aI18NKey, Throwable aValue, Object aValue2) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage(), aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, aValue, caller);
        }
    }

    /**
     * Logs a warning message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log. May be <code>null</code>;
     *        in that case the given value's string representation will be used as message.
     *        This can be used for logging already translated messages.
     * @param aValue
     *        the object to insert into the I18Ned message or to use as message if aI18NKey
     *        is <code>null</code>
     */
    public void warning(String aI18NKey, Object aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? StringServices.toString(aValue)
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, caller);
        }
    }

    /**
     * Logs a warning message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void warning(String aI18NKey, Object aValue1, Object aValue2) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, caller);
        }
    }

    /**
     * Logs a warning message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     * @param aValue3
     *        the third object to insert into the I18Ned message
     */
    public void warning(String aI18NKey, Object aValue1, Object aValue2, Object aValue3) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2, aValue3);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, caller);
        }
    }

    /**
     * Logs a warning message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param values
     *        the objects to insert into the I18Ned message
     */
    public void warning(String aI18NKey, Object[] values) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), values);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addWarning(theMessage);
            if (logToGUI) progress.appendMessage(warn + theMessage);
            if (logToDefaultLogger) Logger.warn(theMessage, caller);
        }
    }


    /**
     * Logs an error message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     */
    public void error(String aI18NKey) {
		String theMessage = resources.getString(resPrefix.key(aI18NKey));
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, caller);
        }
    }

    /**
     * Logs an error message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     */
    public void error(String aI18NKey, Throwable aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage());
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, aValue, caller);
        }
    }

    /**
     * Logs an error message with an exception to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void error(String aI18NKey, Throwable aValue, Object aValue2) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage(), aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, aValue, caller);
        }
    }

    /**
     * Logs an error message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log. May be <code>null</code>;
     *        in that case the given value's string representation will be used as message.
     *        This can be used for logging untranslated messages.
     * @param aValue
     *        the object to insert into the I18Ned message or to use as message if aI18NKey
     *        is <code>null</code>
     */
    public void error(String aI18NKey, Object aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? StringServices.toString(aValue)
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, caller);
        }
    }

    /**
     * Logs an error message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void error(String aI18NKey, Object aValue1, Object aValue2) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, caller);
        }
    }

    /**
     * Logs an error message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     * @param aValue3
     *        the third object to insert into the I18Ned message
     */
    public void error(String aI18NKey, Object aValue1, Object aValue2, Object aValue3) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2, aValue3);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, caller);
        }
    }

    /**
     * Logs an error message to logger, result and GUI.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param values
     *        the objects to insert into the I18Ned message
     */
    public void error(String aI18NKey, Object[] values) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), values);
        theMessage = checkFrequency(aI18NKey, theMessage);
        if (theMessage != null) {
            if (logToResult) result.addError(theMessage);
            if (logToGUI) progress.appendMessage(error + theMessage);
            if (logToDefaultLogger) Logger.error(theMessage, caller);
        }
    }



    /**
     * Logs an fatal error message to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     */
    public void fatal(String aI18NKey) {
		String theMessage = resources.getString(resPrefix.key(aI18NKey));
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, caller);
        result.setSuccess(false);
    }

    /**
     * Logs an fatal error message with an exception to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     */
    public void fatal(String aI18NKey, Throwable aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, aValue, caller);
        result.setSuccess(false);
    }

    /**
     * Logs an fatal error message to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log. May be <code>null</code>;
     *        in that case the given value's string representation will be used as message.
     *        This can be used for logging untranslated messages.
     * @param aValue
     *        the object to insert into the I18Ned message or to use as message if aI18NKey
     *        is <code>null</code>
     */
    public void fatal(String aI18NKey, Object aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? StringServices.toString(aValue)
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, caller);
        result.setSuccess(false);
    }

    /**
     * Logs an fatal error message to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void fatal(String aI18NKey, Object aValue1, Object aValue2) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2);
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, caller);
        result.setSuccess(false);
    }

    /**
     * Logs an fatal error message to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     * @param aValue3
     *        the third object to insert into the I18Ned message
     */
    public void fatal(String aI18NKey, Object aValue1, Object aValue2, Object aValue3) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2, aValue3);
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, caller);
        result.setSuccess(false);
    }

    /**
     * Logs an fatal error message to logger, result and GUI.
     * In addition, the result success is set to false.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param values
     *        the objects to insert into the I18Ned message
     */
    public void fatal(String aI18NKey, Object[] values) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), values);
        if (logToResult) result.addError(theMessage);
        if (logToGUI) progress.appendMessage(fatal + theMessage);
        if (logToDefaultLogger) Logger.fatal(theMessage, caller);
        result.setSuccess(false);
    }



    /**
     * Logs a message to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     */
    public void log(String aI18NKey) {
		String theMessage = resources.getString(resPrefix.key(aI18NKey));
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, caller);
    }

    /**
     * Logs a message with an exception to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     */
    public void log(String aI18NKey, Throwable aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage());
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, aValue, caller);
    }

    /**
     * Logs a message with an exception to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log
     * @param aValue
     *        the exception to add to the message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void log(String aI18NKey, Throwable aValue, Object aValue2) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? aValue.getMessage()
			: resources.getMessage(resPrefix.key(aI18NKey), aValue.getMessage(), aValue2);
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, aValue, caller);
    }

    /**
     * Logs a message to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log. May be <code>null</code>;
     *        in that case the given value's string representation will be used as message.
     *        This can be used for logging already translated messages.
     * @param aValue
     *        the object to insert into the I18Ned message or to use as message if aI18NKey
     *        is <code>null</code>
     */
    public void log(String aI18NKey, Object aValue) {
		String theMessage = StringServices.isEmpty(aI18NKey) ? StringServices.toString(aValue)
			: resources.getMessage(resPrefix.key(aI18NKey), aValue);
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, caller);
    }

    /**
     * Logs a message to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     */
    public void log(String aI18NKey, Object aValue1, Object aValue2) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2);
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, caller);
    }

    /**
     * Logs a message to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param aValue1
     *        the first object to insert into the I18Ned message
     * @param aValue2
     *        the second object to insert into the I18Ned message
     * @param aValue3
     *        the third object to insert into the I18Ned message
     */
    public void log(String aI18NKey, Object aValue1, Object aValue2, Object aValue3) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), aValue1, aValue2, aValue3);
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, caller);
    }

    /**
     * Logs a message to logger and GUI, but not to result.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the message to log.
     *        Must not be <code>null</code>.
     * @param values
     *        the objects to insert into the I18Ned message
     */
    public void log(String aI18NKey, Object[] values) {
		String theMessage = resources.getMessage(resPrefix.key(aI18NKey), values);
        if (logToGUI) progress.appendMessage(theMessage);
        if (logToDefaultLogger) Logger.info(theMessage, caller);
    }

    /**
     * Logs current memory usage of the Java VM.
     */
    public void logMemory() {
        Runtime rt = Runtime.getRuntime();
        StringBuffer sb = new StringBuffer();
        sb.append("Memory usage:\n");
        sb.append("Used Memory in VM: " + toMB(rt.totalMemory() - rt.freeMemory()) + " MB\n");
        sb.append("Free Memory in VM: " + toMB(rt.freeMemory()) + " MB\n");
        sb.append("Current VM Size: " + toMB(rt.totalMemory()) + " MB\n");
        sb.append("Still available to VM: " + toMB(rt.maxMemory() - rt.totalMemory()) + " MB\n");
        sb.append("Max VM Size: " + toMB(rt.maxMemory()) + " MB\n");
        boolean logToGUIState = logToGUI;
        logToGUI = false;
        log(null, sb.toString());
        logToGUI = logToGUIState;
    }

    /**
     * Converts bytes to megabytes.
     *
     * @param bytes
     *        the byte value to convert
     * @return the given value as megabyte value
     */
    private long toMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    /**
     * Logs a counter and error counter if errors occurred.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the counter message to log;
     * @param aStepName
     *        the already translated step name
     * @param aCount
     *        the value of the counter
     * @param aErrorCount
     *        the value of an error counter, which will extend the message if it is greater
     *        than 0
     */
    public void logCounter(String aI18NKey, String aStepName, int aCount, int aErrorCount) {
        String theKey = aErrorCount > 0 ? aI18NKey + ERROR_SUFFIX : aI18NKey;
        info(theKey, aStepName, String.valueOf(aCount), String.valueOf(aErrorCount));
    }

    /**
     * Logs a counter, an error counter and a duplicate counter.
     *
     * @param aI18NKey
     *        the I18N key (without prefix) of the counter message to log;
     * @param aStepName
     *        the already translated step name
     * @param aCount
     *        the value of the counter
     * @param aErrorCount
     *        the value of an error counter, which will extend the message if it is greater
     *        than 0
     * @param aDuplicateCount
     *        the value of an duplicate counter, which will extend the message if it is
     *        greater than 0
     */
    public void logCounter(String aI18NKey, String aStepName, int aCount, int aErrorCount, int aDuplicateCount) {
        if (aDuplicateCount == 0) {
            logCounter(aI18NKey, aStepName, aCount, aErrorCount);
            return;
        }
        String theKey = aErrorCount > 0 ? aI18NKey + DUPLICATE_SUFFIX + ERROR_SUFFIX : aI18NKey + DUPLICATE_SUFFIX;
		String theMessage = resources.getMessage(resPrefix.key(theKey), new Object[] {
		aStepName, String.valueOf(aCount), String.valueOf(aErrorCount), String.valueOf(aDuplicateCount)});
        info(null, theMessage);
    }

}
