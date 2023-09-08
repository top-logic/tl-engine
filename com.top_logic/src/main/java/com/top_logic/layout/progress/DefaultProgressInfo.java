/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * The DefaultProgressInfo is the default implementation of {@link ProgressInfo}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DefaultProgressInfo implements ProgressInfo {

	/** The resources to be used for I18N of messages. */
	private final Resources _resources;

    /** Saves the current finished count. */
	private volatile long _current;

    /** Saves the maximum count to be done. */
	private volatile long _expected;

    /** Flag indicating whether the progress has finished. */
	private volatile boolean _finished;

    /** Saves the messages of the progress. */
	private final StringBuffer _message;

	/** Stick Flag set by {@link #signalStop()} */
	private volatile boolean _shouldStop;

	/** last Time when {@link #getProgress()} was called */
	private volatile long _lastAlive;

	/** @see #getResult() */
	private volatile Object _result;

    /**
     * Creates a new instance of this class.
     */
    public DefaultProgressInfo() {
		_resources = Resources.getInstance();
		_message = new StringBuffer(1024);
        reset();
    }

    /**
     * Resets the progress info.
     */
    public void reset() {
		_current = 0;
		_expected = 1;
		_finished = false;
		_message.setLength(0);
		_shouldStop = false;
		_lastAlive = -1;
    }

    @Override
	public int getRefreshSeconds() {
        return 1;
    }

    @Override
	public float getProgress() {
		_lastAlive = System.currentTimeMillis();
		long expected = _expected;
		if (expected == 0) {
			return 0;
		}

		return (float) _current / (float) expected * 100f;
    }

    @Override
	public long getCurrent() {
		return _current;
    }

    @Override
	public long getExpected() {
		return _expected;
    }

    @Override
	public boolean isFinished() {
		return _finished;
    }

    @Override
	public String getMessage() {
		return _message.toString();
    }

    /**
     * Sets the current finished count.
     *
     * @param current
     *        the current finished count to set
     */
    public void setCurrent(long current) {
        internalSetCurrent(current);
    }

	private void internalSetCurrent(long current) {
		_current = current < 0 ? 0 : current;
	}

    /**
	 * Increments {@link #getCurrent()} and sets a new {@link #getMessage()}.
	 */
	public final void incCurrent(String aMessage) {
		internalIncCurrent(aMessage);
	}

	/**
	 * @param messageKey
	 *        The message resource key to use for {@link #incCurrent(String)}.
	 */
	public final void incCurrentKey(ResKey messageKey) {
		internalIncCurrent(getMessage(messageKey));
	}

	private void internalIncCurrent(String aMessage) {
		internalSetMessage(aMessage);
		internalSetCurrent(getCurrent() + 1);
	}

    /**
     * Sets the expected count.
     *
     * @param expected
     *        the expected count to set
     */
    public void setExpected(long expected) {
		_expected = expected < 0 ? 0 : expected;
    }

    /**
     * Sets whether the progress is finished.
     *
     * @param finished
     *        the finished state to set
     */
    public void setFinished(boolean finished) {
		_finished = finished;
    }

    /**
     * Sets the complete message of the progress.
     *
     * @param message
     *        the message to set
     */
    public void setMessage(String message) {
        internalSetMessage(message);
    }

	/**
	 * Sets a message from a resource key.
	 * 
	 * @param messageKey
	 *        The resource key to translate to a {@link #setMessage(String)} call.
	 */
	public final void setMessageKey(ResKey messageKey) {
		internalSetMessage(getMessage(messageKey));
	}

	private void internalSetMessage(String message) {
		_message.setLength(0);
		_message.append(message);
	}

    /**
     * Appends the given message in a new line (message = old message + "\n" + new message).
     *
     * @param aMessage
     *        the message to append
     */
	public void appendMessage(String aMessage) {
        internalAppendMessage(aMessage);
    }

	/**
	 * Appends a message created from a given resource key.
	 * 
	 * @param messageKey
	 *        The resource key to translate to a message for {@link #appendMessage(String)}.
	 */
	public final void appendMessageKey(ResKey messageKey) {
		internalAppendMessage(getMessage(messageKey));
	}

	private String getMessage(ResKey messageKey) {
		return _resources.decodeMessageFromKeyWithEncodedArguments(messageKey);
	}

	private void internalAppendMessage(String aMessage) {
		_message.append('\n').append(aMessage);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Will set {@link #_shouldStop}.
	 */
	@Override
	public boolean signalStop() {
		return _shouldStop = true;
	}

	/**
	 * Return true when {@link #signalStop()} was called.
	 * 
	 * In this case you should stop processing as soon as possible.
	 */
	@Override
	public boolean shouldStop() {
		if (_shouldStop) {
			return true;
		}
		if (DefaultProgressInfo.shouldStop(_lastAlive, this)) {
			signalStop();
		}
		return _shouldStop;
	}

	/**
	 * Check if anInfo is not alive anymore when comparing with lastAlive.
	 * 
	 * @param lastAlive
	 *        will not be considered when negative
	 */
	public static boolean shouldStop(long lastAlive, ProgressInfo anInfo) {
		if (lastAlive > 0) {
			long toLate = System.currentTimeMillis() - anInfo.getRefreshSeconds() * STOP_IF_NOT_ALIVE_FACTOR;
			return lastAlive < toLate;
		}
		return false;
	}

	/**
	 * The generic result of the long running operation.
	 * 
	 * <p>
	 * The result must only be accessed after {@link #isFinished()} becomes <code>true</code>.
	 * </p>
	 * 
	 * @return May be <code>null</code> in case this {@link ProgressInfo} was requested to
	 *         {@link #signalStop() stop}.
	 */
	public Object getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public void setResult(Object aResult) {
		_result = aResult;
	}

}
