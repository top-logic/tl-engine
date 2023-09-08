/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Queue;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.ArrayQueue;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.Action;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.ErrorHandlingHelper;
import com.top_logic.util.error.TopLogicException;

/**
 * Default implementation of {@link ValidationQueue} storing the {@link ToBeValidated} and
 * {@link Action} in two separate {@link Queue} and process them during
 * {@link #runValidation(DisplayContext)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultValidationQueue implements ValidationQueue, ActionQueue<Action> {

	/** Queue containing elements to validate */
	private Queue<ToBeValidated> _invalidQueue = new ArrayQueue<>();

	/** Queue of send {@link Action}s */
	private ConsolidatingQueue _actionQueue = new ConsolidatingQueue();

	/** {@link HandlerResult} used to store problems during {@link Action} processing or validating */
	private HandlerResult _result;

	/** whether the {@link ActionQueue} is currently processing {@link Action Actions} */
	private boolean _dispatching;

	/** whether the {@link ActionQueue} is forced to enqueue {@link Action Actions} */
	private boolean _queueingForced;

	/**
	 * whether the {@link ValidationQueue} was requested to
	 * {@link ValidationQueue#runValidation(DisplayContext)}
	 */
	private boolean _validating;

	@Override
	public void notifyInvalid(ToBeValidated o) {
		_invalidQueue.offer(o);
	}

	@Override
	public void enqueueAction(Action action) {
		_actionQueue.offer(action);
		executeAction();
	}

	private void notifyException(String message, RuntimeException ex) {
		if (_result == null) {
			_result = new HandlerResult();
			TopLogicException tlEx;
			if (ex instanceof TopLogicException) {
				tlEx = (TopLogicException) ex;
				logTLException(message, tlEx);
			} else {
				tlEx = new TopLogicException(I18NConstants.INTERNAL_ERROR, ex);
				tlEx.initSeverity(ErrorSeverity.SYSTEM_FAILURE);
				logError(message, ex);
			}
			_result.setException(tlEx);
		} else {
			_result.addErrorText(message + ex.getLocalizedMessage());
			logError(message, ex);
		}
	}

	private void logTLException(String message, TopLogicException tlEx) {
		if (ErrorHandlingHelper.isInternalError(tlEx)) {
			logError(message, tlEx);
		} else {
			Logger.info(message, DefaultValidationQueue.class);
		}
	}

	private void logError(String message, RuntimeException ex) {
		Logger.error(message, ex, DefaultValidationQueue.class);
	}


	@Override
	public HandlerResult runValidation(DisplayContext context) {
		if (_validating) {
			assert false : "Validation must not be done nested";
			return HandlerResult.DEFAULT_RESULT;
		}
		_validating = true;
		try {
			return actualValidation(context);
		} finally {
			_validating = false;
		}
	}

	private HandlerResult actualValidation(DisplayContext context) {
		assert _actionQueue.isEmpty() : "There are still some actions to process.";
		ToBeValidated nextInvalid = _invalidQueue.poll();
		int passes = 0;
		while (nextInvalid != null) {
			if (passes > 1024) {
				_actionQueue.clear();
				_invalidQueue.clear();
				String msg = "Unable to process all invalids (More than 1024)";
				notifyException(msg, new RuntimeException(msg));
				break;
			}
			passes++;
			if (passes > 1014) {
				Logger.warn("Excessive model validation: " + nextInvalid, DefaultValidationQueue.class);
			}
			try {
				nextInvalid.validate(context);
			} catch (RuntimeException ex) {
				notifyException("Exception during validating " + nextInvalid, ex);
			}
			
			// poll after dispatching as some actions may made some component invalid
			nextInvalid = _invalidQueue.poll();
		}
		return result();
	}

	/**
	 * Returns the {@link HandlerResult} containing potential problems
	 */
	private HandlerResult result() {
		if (_result == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		HandlerResult result = _result;
		_result = null;
		return result;
	}

	private void executeAction() {
		if (_queueingForced || _dispatching) {
			return;
		}
		_dispatching = true;
		try {
			actualExecute();
		} finally {
			_dispatching = false;
		}
	}

	private void actualExecute() {
		int passes = 0;
		Action nextAction = _actionQueue.poll();
		while (nextAction != null) {
			if (passes > 1024) {
				_actionQueue.clear();
				String msg = "Unable to process all actions (More than 1024)";
				notifyException(msg, new RuntimeException(msg));
				return;
			}
			passes++;
			if (passes > 1014) {
				Logger.warn("Excessive model actions: " + nextAction, DefaultValidationQueue.class);
			}
			try {
				nextAction.execute();
			} catch (RuntimeException ex) {
				notifyException("Exception during dispatch action " + nextAction, ex);
			}
			if (_queueingForced) {
				break;
			}
			nextAction = _actionQueue.poll();
		}
	}

	@Override
	public void forceQueueing() {
		_queueingForced = true;
	}

	@Override
	public void processActions() {
		_queueingForced = false;
		executeAction();
	}

	@FrameworkInternal
	public boolean hasPendingToBeValidated() {
		return !_invalidQueue.isEmpty();
	}
}
