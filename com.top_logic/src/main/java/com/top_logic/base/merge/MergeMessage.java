/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * A MergeMessage describes and performs parts of imports and such.
 * 
 * A MergeMessage is always attached to a MergeNode, which represents
 * the basic dates the Messages will perfom on. Since MergeMessage
 * can <code>perform()</code> things such MergeMessage are called Tasks. 
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MergeMessage extends AbstractMergeNode {

    public static final String[] LEVEL_STRING = {
            "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

    /** Information. */
    public static final int DEBUG = 0;

    /** Information. */
    public static final int INFO = 1;

    /** Warning. */
    public static final int WARN = 2;

    /** Error, which can be managed. */
    public static final int ERROR = 3;

    /** Error, which cannot be managed. */
    public static final int FATAL = 4;

    /** 
     * USE with CTOR to clarify the flag
     * 
     * This is not correct english, native speakers raise your hand ... 
     */
    public static final boolean APPROVEABLE = true;

	/** The MergeMessage has been approved for performation by the user. */
	protected boolean approved;
	
	/** Error-Level of the message, one of DEBUG, INFO ,WARN ,ERROR, FATAL */
    protected int level;

	/** The message string of the MergeMessage, actually a translation key */
	protected ResKey message;

	/** Whether or not the mergeMessage is checkable or if it´s only a info message (of a mandatory action) */
	protected boolean approveable;

	/** 
     * Create a new MergeMessage for a given level and message and set the checkbox either changeable or immutable.
     * 
     * @param aLevel        one of DEBUG/INFO/WARN/ERROR/FATAL
     * @param aMessage      will be translated using Resources
     * @param isApproveable The user can choose if he wishes to perform this message.
	 * 
	 */
	public MergeMessage(int aLevel, ResKey aMessage, boolean isApproveable) {
		this(null, aLevel, aMessage, isApproveable);
	}

	public MergeMessage(AbstractMergeNode aParent, int aLevel, ResKey aMessage, boolean isApproveable) {
		super(aParent, aMessage);

		if ((aLevel != DEBUG) && (aLevel != INFO) 
         && (aLevel != WARN ) && (aLevel != ERROR) && (aLevel != FATAL)) {
			throw new IllegalArgumentException("Invalid type for a merge message!");
		}
        if (aMessage == null) {
            throw new IllegalArgumentException("Message for merge message is null or empty!");
        }

		this.level            = aLevel;
		this.message          = aMessage;
		this.approveable      = isApproveable;
        this.approved         = !isApproveable; // not Approveable messages are approved by default.
	}

    /**
     * Create an {@link #approveable} MergeMessage.
     */
	public MergeMessage(int aLevel, ResKey aMessage) {
		this(aLevel, aMessage,  APPROVEABLE);
    }

	@Override
	public List<? extends AbstractMergeNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean isInitialized() {
		// no lazy child creation.
		return true;
	}

    /**
     * Get a nice description fo debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + this.getLevelString() +
                    ", message: '" + this.message + ']');
    }
    
    /**
     * Return a String respresenting the level. 
     */
    public String getLevelString() {
       return LEVEL_STRING[this.level];
    }

    /**
     * Method invoked to execute the changed defined by this instance.
     * 
     * In this default implementation, this method will do nothing.
     * Be aware that perform will be called more than once in
     * case a message was propagated.
     * 
     * @throws    Exception if executing the merge operation fails.
     */
    public void perform(MergeTreeNode owner) throws Exception {
        // default do nothing
    }


    /**
     * the level of the message one of DEBUG, INFO ,WARN ,ERROR, FATAL
     */
    public int getLevel() {
        return (this.level);
    }

    /**
     * Will be translates using resources ...
     */
	public ResKey getMessage() {
        return (this.message);
    }

    /**
     * @see #approved
     */
	public boolean isApproved() {
		return approved;
	}

	/**
	 * @see #approved
	 */
	public void setApproved(boolean b) {
        approved = b;
	}

    /**
     * @see #approveable
     */
	public boolean isApproveable() {
		return approveable;
	}

    /**
     * Set approveable and apporved to the opposit values.
     * 
     * (Since a !approveable message is automatically approved)
     */
    public void setApproveable(boolean isApproveable) {
        approveable = isApproveable;
        approved    = !isApproveable;
    }
}
