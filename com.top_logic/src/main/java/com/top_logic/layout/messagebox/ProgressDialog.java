/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.tag.PageControl;
import com.top_logic.layout.form.tag.PageRenderer;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Dialog executing a background task and displaying its progress.
 * 
 * @see #run(I18NLog)
 * @see #open(DisplayContext)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ProgressDialog extends AbstractDialog implements HTMLFragment, DialogClosedListener {

	private final ProgressControl _progress = new ProgressControl();

	private final LogControl _logDisplay = new LogControl();

	private CommandModel _cancel;

	private CommandModel _ok;

	private ResKey _title;

	private int _stepCnt;

	private volatile State _state;

	private boolean _active;

	private volatile int _progressValue;

	/**
	 * The states of the background process managed by a {@link ProgressDialog}.
	 */
	public enum State {
		/**
		 * The background process is still running.
		 */
		RUNNING,

		/**
		 * The background process was canceled by the user.
		 */
		CANCELED,

		/**
		 * The background process has completed sucessfully.
		 */
		COMPLETED,

		/**
		 * The background process has finished with an error.
		 */
		FAILED;
	}

	/**
	 * Creates a {@link ProgressDialog}.
	 */
	public ProgressDialog(ResKey title, DisplayDimension width, DisplayDimension height) {
		this(DefaultDialogModel.dialogModel(ResKey.text(""), width, height, false));
		_title = title;
	}

	/**
	 * Creates a {@link ProgressDialog}.
	 */
	public ProgressDialog(DialogModel dialogModel) {
		super(dialogModel);
	}

	/**
	 * Number of total steps performed.
	 * 
	 * <p>
	 * Note: To use the {@link #setProgress(int)} API, you must override this method.
	 * </p>
	 */
	protected int getStepCnt() {
		return -1;
	}

	/**
	 * Performs the background task.
	 * 
	 * <p>
	 * This method is executed in a separate thread. Therefore, it must not interact with the user
	 * interface in any way. However the method is executed in the same sub-session as the calling
	 * thread. All actions are performed on behalf of the user owning the thread that opened the
	 * dialog.
	 * </p>
	 * 
	 * <p>
	 * All interaction with the user interface (except reporting progress messages to the given log
	 * and setting the current progress in {@link #setProgress(int)}) must be deferred until the
	 * operation has finished. Those operations can be placed in the
	 * {@link #handleCompleted(DisplayContext)} method.
	 * </p>
	 * 
	 * @param log
	 *        The log to write progress messages to. The message can have a {@link ResKey#tooltip()}
	 *        suffix that is used as tool-tip for the message.
	 * 
	 * @throws AbortExecutionException
	 *         If the user cancels the operation. Note: When catching exceptions inside this method,
	 *         make sure not to catch {@link AbortExecutionException}, otherwise the background task
	 *         cannot be canceled.
	 * 
	 * @see #setProgress(int)
	 * @see #handleCompleted(DisplayContext)
	 */
	protected abstract void run(I18NLog log) throws AbortExecutionException;

	/**
	 * Callback invoked after successful completion of the task executed by {@link #run(I18NLog)}.
	 * 
	 * <p>
	 * The method is again executed in the command thread of the current sub-session and can
	 * therefore interact with the users UI, e.g. triggering downloads, opening dialogs and so on.
	 * </p>
	 *
	 * @param context
	 *        The {@link DisplayContext} of the current UI session.
	 */
	protected void handleCompleted(DisplayContext context) {
		// Nothing to do.
	}

	/**
	 * Callback invoked if task executed by {@link #run(I18NLog)} failed.
	 * 
	 * <p>
	 * The method is again executed in the command thread of the current sub-session and can
	 * therefore interact with the users UI, e.g. triggering downloads, opening dialogs and so on.
	 * </p>
	 *
	 * @param context
	 *        The {@link DisplayContext} of the current UI session.
	 */
	protected void handleFailure(DisplayContext context) {
		setButtonType(_ok, ButtonType.CLOSE);
	}

	private void setButtonType(CommandModel button, ButtonType buttonType) {
		button.setLabel(Resources.getInstance().getString(buttonType.getButtonLabelKey()));
		button.setImage(buttonType.getButtonImage());
		button.setNotExecutableImage(buttonType.getDisabledButtonImage());
	}

	/**
	 * The button that becomes visible after the progress has finished.
	 */
	public CommandModel getOkButton() {
		return _ok;
	}

	@Override
	public HandlerResult open(WindowScope windowScope) {
		HandlerResult result = super.open(windowScope);
		
		_ok.setVisible(false);
		_cancel.setVisible(true);
	
		getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, this);

		_progressValue = 0;
		_stepCnt = getStepCnt();
		if (isAutoProgress()) {
			_progress.setMax(100);
		} else {
			_progress.setMax(_stepCnt);
		}
		_state = State.RUNNING;
		_active = true;
		SubSessionContext subSession = ThreadContextManager.getSubSession();
		SchedulerService.getInstance().execute(() -> runTask(subSession));
		scheduleUpdate();
	
		return result;
	}

	private boolean isAutoProgress() {
		return _stepCnt < 0;
	}

	@Override
	protected HTMLFragment createView() {
		PageControl pageControl = new PageControl(PageRenderer.getThemeInstance());
		pageControl.setTitleContent(new ResourceText(_title));
		pageControl.setSubtitleContent(_progress);
		pageControl.setIconBarContent(Icons.IN_PROGRESS);
		pageControl.setBodyContent(this);
		return pageControl;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		_logDisplay.write(context, out);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		_cancel = addCancel(buttons);
		_ok = addOk(buttons);
		getDialogModel().setDefaultCommand(_ok);

		// Handled explicitly to await progress.
		ScriptingRecorder.annotateAsDontRecord(_ok);
	}

	@Override
	public Command getDiscardClosure() {
		Command discardClosure = super.getDiscardClosure();

		return (context) -> {
			HandlerResult result = discardClosure.executeCommand(context);
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordAction(TypedConfiguration.newConfigItem(AwaitProgressDialog.class));
			}
			return result;
		};
	}

	/**
	 * The current state of the background process.
	 */
	public State getState() {
		return _state;
	}

	private void runTask(SubSessionContext subSession) {
		try {
			ThreadContextManager.inContext(subSession, this::runTaskInContext);

			// Task may have been canceled but it was not detected.
			if (_state == State.RUNNING) {
				_state = State.COMPLETED;
			}
		} catch (AbortExecutionException ex) {
			// Task was canceled.
		} catch (I18NRuntimeException ex) {
			_logDisplay.addMessage(Level.ERROR, ex.getErrorKey(), null);
			_state = State.FAILED;
		} catch (Throwable ex) {
			Logger.error("Background task failed.", ex, ProgressDialog.class);
			_logDisplay.addMessage(Level.ERROR, I18NConstants.ERROR_TASK_FAILED, ex);
			_state = State.FAILED;
		} finally {
			finish();
		}
	}

	private void runTaskInContext() {
		run(this::receiveLogMessage);
	}

	private void receiveLogMessage(Level level, ResKey message, Throwable ex) {
		if (_state != State.RUNNING) {
			throw new AbortExecutionException("Operation was canceled.", null);
		}
		_logDisplay.addMessage(level, message, ex);
	}

	/**
	 * The current progress on its way from 0 to {@link #getStepCnt()}.
	 * <p>
	 * Note: To use this API, {@link #getStepCnt()} must be overridden to return the total number of
	 * steps performed. By default, the progress bar automatically updates every second and jumps to
	 * 100% when the operation completes.
	 * </p>
	 */
	protected int getProgress() {
		return _progressValue;
	}

	/**
	 * Explicitly updates the progress bar by a certain amount.
	 * <p>
	 * Note: To use this API, {@link #getStepCnt()} must be overridden to return the total number of
	 * steps performed. By default, the progress bar automatically updates every second and jumps to
	 * 100% when the operation completes.
	 * </p>
	 * 
	 * @see #getStepCnt()
	 * 
	 * @throws AbortExecutionException
	 *         If the user canceled the operation. This exception must not be caught.
	 */
	protected void incProgress(int delta) {
		setProgress(getProgress() + delta);
	}

	/**
	 * Explicitly updates the progress bar to a certain step.
	 * 
	 * <p>
	 * Note: To use this API, {@link #getStepCnt()} must be overridden to return the total number of
	 * steps performed. By default, the progress bar automatically updates every second and jumps to
	 * 100% when the operation completes.
	 * </p>
	 *
	 * @param step
	 *        The step number in the range 0 to {@link #getStepCnt()}.
	 * 
	 * @see #getStepCnt()
	 * 
	 * @throws AbortExecutionException
	 *         If the user canceled the operation. This exception must not be caught.
	 */
	protected void setProgress(int step) {
		if (_state != State.RUNNING) {
			throw new AbortExecutionException("Operation was canceled.", null);
		}
		_progressValue = step;
	}

	@Override
	public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
		if (_state == State.RUNNING) {
			_state = State.CANCELED;
		}
	}

	private void handleTimer() {
		// Callback from the timer.
		if (_state == State.RUNNING) {
			if (isAutoProgress()) {
				_progress.setValue(_progress.getValue() + (_progress.getMax() - _progress.getValue()) / 10);
			} else {
				_progress.setValue(_progressValue);
			}
		}

		if (isActive()) {
			scheduleUpdate();
		} else {
			handleFinish();
		}
	}

	private void handleFinish() {
		if (_ok.isVisible()) {
			// Already finished (see await).
			return;
		}

		_ok.setVisible(true);
		_cancel.setVisible(false);
		if (_state == State.COMPLETED) {
			// Ensure that the progress bar always ends at 100%
			_progress.setValue(_progress.getMax());

			handleCompleted(DefaultDisplayContext.getDisplayContext());
		} else {
			handleFailure(DefaultDisplayContext.getDisplayContext());
		}
	}

	private synchronized boolean isActive() {
		return _active;
	}

	private synchronized void finish() {
		_active = false;
		notifyAll();
	}

	/**
	 * Waits until the background process has completed.
	 * 
	 * @param timeout
	 *        The maximum time in milliseconds to wait. The value <code>0</code> means infinite
	 *        wait.
	 * 
	 * @throws TopLogicException
	 *         If the background task does not finish within the specified timeout.
	 */
	public synchronized void await(long timeout) throws InterruptedException {
		long nextWait = timeout;
		while (_active) {
			long startTime = System.currentTimeMillis();
			wait(nextWait);
			if (nextWait > 0) {
				// No infinite wait.
				long waitedMillis = System.currentTimeMillis() - startTime;
				nextWait -= waitedMillis;
				if (nextWait <= 0) {
					throw new TopLogicException(I18NConstants.ERROR_PROGRESS_NOT_FINISHED_WITHIN_TIMEOUT);
				}
			}
		}

		// Must be done from the await action because the next UI progress update is still blocked
		// by the request lock. The script must only continue, after the UI has been updated.
		handleFinish();
	}

	private void scheduleUpdate() {
		uiExecutor().schedule(this::handleTimer, 1, TimeUnit.SECONDS);
	}

	private ScheduledExecutorService uiExecutor() {
		return MainLayout.getDefaultMainLayout().getWindowScope().getUIExecutor();
	}

}
