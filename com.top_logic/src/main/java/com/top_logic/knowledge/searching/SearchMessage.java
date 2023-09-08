/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import com.top_logic.basic.StringServices;

/** 
 * A explaination from the search engine about the result of the search.
 * 
 * The message can be of different importance, therefore we defined
 * several levels of messages. They can reach from {@link #DEBUG} to 
 * {@link #FATAL} where the meening of DEBUG and FATAL has to be defined
 * by the engine and the using classes themselves.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchMessage {

    /** A message, which is of lowest interest. */
    public static final int DEBUG = 1;

    /** A notice to the user. */
    public static final int INFO  = 2;

    /** A none critical error, which is for a users information. */
    public static final int WARN  = 4;

    /** A critical error, which prevents the query from being executed. */
    public static final int ERROR = 8;

    /** A fatal error, which disabled the search engine from further working. */
    public static final int FATAL = 16;

    /** The level of this message. */
    private int level;

    /** The real message. */
    private String message;

    /** The search engine sending this message. */
    private SearchEngine source;

    /**
     * Constructor for a message.
     * 
     * A message has to contain all information, whereas the type of level 
     * is  defined by the constants of this class. The message has to be
     * a none empty string (also not <code>null</code>). The source, which
     * defines the search engine sending the message, can be 
     * <code>null</code>, because the search service itself can place messages
     * in the result.
     * 
     * @param    aLevel      The importance of this message (which has to be
     *                       one of the constants defined within this class).
     * @param    aMessage    The message.
     * @param    aSource     The search engine sending the message, this
     *                       may be <code>null</code>, because the search
     *                       service itself can place messages in the result.
     * @throws   IllegalArgumentException    If one of the parameters is empty
     *                                       or <code>null</code>.
     */
    public SearchMessage(int aLevel, String aMessage, SearchEngine aSource) 
                                            throws IllegalArgumentException {
        super();

        String theMessage = null;

        switch (aLevel) {
            case DEBUG:
            case INFO:
            case WARN:
            case ERROR:
            case FATAL: break;
            default:    theMessage = "Wrong type of message level (was " + 
                                     aLevel + ")!";
        }

        if (theMessage == null) {
            if (StringServices.isEmpty(aMessage)) {
                theMessage = "No message defined!";
            }
            else if (aSource == null) {
                theMessage = "No search engine defined!";
            }
        }

        if (theMessage != null) {
            throw new IllegalArgumentException(theMessage);
        }

        this.level   = aLevel;
        this.message = aMessage;
        this.source  = aSource;
    }

    /**
     * Return the string representation of this instance for debugging.
     * 
     * @return    The string representation for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                        + "level: " + this.getLevelAsString()
                        + "source: " + this.source
                        + ", value: " + this.message
                        + ']');
    }

    /**
     * The importance level of this message.
     * 
     * The value will be one of the constants from this class.
     * 
     * @return    The level.
     */
    public int getLevel() {
        return (this.level);
    }

    /**
     * The importance level of this message.
     * 
     * The value will be one of the names of the constants from this class.
     * 
     * @return    The level as string.
     */
    public String getLevelAsString() {
        String theLevel;

        switch (this.level) {
            case DEBUG: theLevel = "DEBUG";
                        break;
            case INFO:  theLevel = "INFO";
                        break;
            case WARN:  theLevel = "WARN";
                        break;
            case ERROR: theLevel = "ERROR";
                        break;
            case FATAL: theLevel = "FATAL";
                        break;
            default:    theLevel = "[unknown]";
        }

        return (theLevel);
    }

    /**
     * The message of this instance.
     * 
     * @return    The message (which is not <code>null</code> or empty)!
     */
    public String getMessage() {
        return (this.message);
    }

    /**
     * The search engine producing this message.
     * 
     * If the returned search engine is <code>null</code>, the message 
     * comes from the search service.
     * 
     * @return    The engine (can be <code>null</code>).
     */
    public SearchEngine getSearchEngine() {
        return (this.source);
    }

	/**
	 * Creates an {@link #ERROR} message.
	 * 
	 * @param message
	 *        Value of {@link #getMessage()}.
	 * @param engine
	 *        Value of {@link #getSearchEngine()}.
	 */
	public static SearchMessage error(String message, SearchEngine engine) {
		return new SearchMessage(ERROR, message, engine);
	}
}
