/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import com.top_logic.layout.progress.ProgressInfo;

/**
 * {@link ProgressInfo} that additionally collects log messages of different severity.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface LoggerProgressInfo extends ProgressInfo {

    /**
     * Reports an info message.
     *
     * @param aMessage
     *        The message to report.
     */
    public void info(String aMessage);

    /**
     * Reports an info message.
     *
     * @param aMessage
     *        The message to report.
     * @param aCaller
     *        The class reporting the message.
     */
    public void info(String aMessage, Class aCaller);

    /**
     * Reports an info message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     */
    public void info(String aMessage, Throwable anException);

    /**
     * Reports an info message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     * @param aCaller
     *        The class reporting the message.
     */
    public void info(String aMessage, Throwable anException, Class aCaller);

    /**
     * Reports a warning message.
     *
     * @param aMessage
     *        The message to report.
     */
    public void warn(String aMessage);

    /**
     * Reports a warning message.
     *
     * @param aMessage
     *        The message to report.
     * @param aCaller
     *        The class reporting the message.
     */
    public void warn(String aMessage, Class aCaller);

    /**
     * Reports a warning message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     */
    public void warn(String aMessage, Throwable anException);

    /**
     * Reports a warning message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     * @param aCaller
     *        The class reporting the message.
     */
    public void warn(String aMessage, Throwable anException, Class aCaller);

    /**
     * Reports an error message.
     *
     * @param aMessage
     *        The message to report.
     */
    public void error(String aMessage);

    /**
     * Reports an error message.
     *
     * @param aMessage
     *        The message to report.
     * @param aCaller
     *        The class reporting the message.
     */
    public void error(String aMessage, Class aCaller);

    /**
     * Reports an error message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     */
    public void error(String aMessage, Throwable anException);

    /**
     * Reports an error message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     * @param aCaller
     *        The class reporting the message.
     */
    public void error(String aMessage, Throwable anException, Class aCaller);

    /**
     * Reports a debug message.
     *
     * @param aMessage
     *        The message to report.
     */
    public void debug(String aMessage);

    /**
     * Reports a debug message.
     *
     * @param aMessage
     *        The message to report.
     * @param aCaller
     *        The class reporting the message.
     */
    public void debug(String aMessage, Class aCaller);

    /**
     * Reports a debug message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     */
    public void debug(String aMessage, Throwable anException);

    /**
     * Reports a debug message.
     *
     * @param aMessage
     *        The message to report.
     * @param anException
     *        The problem that caused the message.
     * @param aCaller
     *        The class reporting the message.
     */
    public void debug(String aMessage, Throwable anException, Class aCaller);

    /**
     * Marks the progress as finished and reports a final message.
     *
     * @param aMessage
     *        The final message.
     */
    public void setFinished(String aMessage);

    /**
     * Marks the progress as finished and reports a final message.
     *
     * @param aMessage
     *        The final message.
     * @param aCaller
     *        The class reporting the message.
     */
    public void setFinished(String aMessage, Class aCaller);

    /**
     * Marks the progress as finished and reports a final message.
     *
     * @param aMessage
     *        The final message.
     * @param anException
     *        The problem that caused the end of the progress.
     */
    public void setFinished(String aMessage, Throwable anException);

    /**
     * Marks the progress as finished and reports a final message.
     *
     * @param aMessage
     *        The final message.
     * @param anException
     *        The problem that caused the end of the progress.
     * @param aCaller
     *        The class reporting the message.
     */
    public void setFinished(String aMessage, Throwable anException, Class aCaller);

    /**
     * Resets the progress.
     *
     * @param expected
     *        The expected number of steps until the progress is finished.
     */
    public void reset(long expected);

    /**
     * Advances the progress by one step.
     */
    public void increaseProgress();

}
