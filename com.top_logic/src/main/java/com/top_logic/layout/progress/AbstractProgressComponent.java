/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.SimpleOpenDialog;
import com.top_logic.util.TLContext;

/**
 * Handling component for a {@link ProgressInfo}.
 * 
 * The component will provide two fields for the {@link ProgressInfo#getMessage() message} and the
 * {@link ProgressInfo#getProgress() progress}. When the {@link ProgressInfo} is finished, the
 * dialog this component lives in, will disappear.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link ProgressDialog}.
 */
@Deprecated
public abstract class AbstractProgressComponent extends FormComponent {

	/** Field for displaying the {@link ProgressInfo#getMessage()}. */
	public static final String MESSAGE_FIELD = "message";

	/** Field for displaying the {@link ProgressInfo#getProgress()}. */
	public static final String PROGRESS_FIELD = "progress";

	/** The control provider for both fields. */
	public static final ControlProvider PROVIDER = new DefaultFormFieldControlProvider() {

		@Override
		public Control visitStringField(StringField aMember, Void arg) {
			Control theControl = super.visitStringField(aMember, arg);

			if (MESSAGE_FIELD.equals(aMember.getName()) && theControl instanceof TextInputControl) {
				((TextInputControl) theControl).setMultiLine(true);
			}

			return theControl;
		}

		@Override
		public Control visitComplexField(ComplexField aMember, Void arg) {
			if (PROGRESS_FIELD.equals(aMember.getName())) {
				return new ProgressControl(aMember);
			}
			else {
				return super.visitComplexField(aMember, arg);
			}
		}
	};

	/**
	 * Configuration for the {@link AbstractProgressComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

	}

	/**
	 * Creates a {@link AbstractProgressComponent}.
	 */
	public AbstractProgressComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
		super(context, someAtts);
	}

	/**
	 * This method will be called, when {@link ProgressInfo#isFinished()} returns <code>true</code>.
	 * 
	 * The method can be used to update the dialog opener.
	 */
	public abstract void finishParent();

	@Override
	public FormContext createFormContext() {
		FormContext theContext = new FormContext(this);
		ProgressInfo theInfo = getInfo();
		String message = StringServices.EMPTY_STRING;
		double progressValue = 0.0d;
		if (theInfo != null) {
			message = StringServices.isEmpty(theInfo.getMessage()) ? HTMLConstants.NBSP : theInfo.getMessage();
			progressValue = theInfo.getProgress();
		}
		StringField theString = FormFactory.newStringField(AbstractProgressComponent.MESSAGE_FIELD, message, true);
		DecimalFormat progressFormat = new DecimalFormat("##0", new DecimalFormatSymbols(TLContext.getLocale()));
		ComplexField theComplex =
			FormFactory.newComplexField(AbstractProgressComponent.PROGRESS_FIELD, progressFormat, progressValue, true);

		theString.setControlProvider(AbstractProgressComponent.PROVIDER);
		theComplex.setControlProvider(AbstractProgressComponent.PROVIDER);

		theContext.addMember(theString);
		theContext.addMember(theComplex);

		return theContext;
	}

	/**
	 * Refresh the information in the form fields by the values from {@link #getInfo()}.
	 * 
	 * This method will take the {@link ProgressInfo#getMessage()}, the
	 * {@link ProgressInfo#getProgress()} and return the result of {@link ProgressInfo#isFinished()}
	 * .
	 * 
	 * @return <code>true</code> if progress is finished.
	 */
	public boolean refresh() {
		FormContext theContext = this.getFormContext();
		ProgressInfo theInfo = this.getInfo();

		if (theInfo.isFinished()) {
			this.setValue(theContext.getMember(AbstractProgressComponent.MESSAGE_FIELD), null);
			this.setValue(theContext.getMember(AbstractProgressComponent.PROGRESS_FIELD), 0);

			return true;
		}
		else {
			String message = StringServices.isEmpty(theInfo.getMessage()) ? HTMLConstants.NBSP : theInfo.getMessage();
			this.setValue(theContext.getMember(AbstractProgressComponent.MESSAGE_FIELD), message);
			this.setValue(theContext.getMember(AbstractProgressComponent.PROGRESS_FIELD), theInfo.getProgress());

			return false;
		}
	}

	/**
	 * Return the held {@link ProgressInfo}.
	 * 
	 * @return The requested progress info, not <code>null</code> while dialog is visible.
	 */
	public ProgressInfo getInfo() {
		return (ProgressInfo) getModel();
	}

	/**
	 * Set the given value to the given form member, if it is a {@link FormField}.
	 * 
	 * @param aMember
	 *        The form member to set the value to, may be <code>null</code>.
	 * @param aValue
	 *        The value to be set to the field, may be <code>null</code>.
	 */
	protected void setValue(FormMember aMember, Object aValue) {
		if (aMember instanceof FormField) {
			((FormField) aMember).initializeField(aValue);
		}
	}

	/**
	 * Set a new progress info to this component.
	 * 
	 * @param anInfo
	 *        The info to be used, must not be <code>null</code>.
	 */
	public void setInfo(ProgressInfo anInfo) {
		setModel(anInfo);
	}

	/**
	 * Command handler for starting the separate thread and providing the {@link ProgressInfo} being
	 * displayed.
	 * 
	 * If {@link #startThread(DisplayContext, LayoutComponent, Object, Map)} doesn't return a
	 * {@link ProgressInfo} the dialog will not be displayed.
	 * 
	 * @deprecated Use {@link ProgressDialog}.
	 */
	@Deprecated
	public static abstract class AbstractProgressCommandHandler extends AbstractCommandHandler {

		private static final class DialogClosedListenerImplementation implements DialogClosedListener {

			private final AtomicReference<ScheduledFuture<?>> _cancelProgress;

			DialogClosedListenerImplementation(AtomicReference<ScheduledFuture<?>> cancelProgress) {
				_cancelProgress = cancelProgress;
			}

			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					ScheduledFuture<?> scheduledFuture = _cancelProgress.getAndSet(null);
					if (scheduledFuture != null) {
						scheduledFuture.cancel(false);
					}
				}
			}
		}

		private static final class RefreshComponent implements Runnable {

			private final AbstractProgressComponent _progressComponent;

			private final AtomicReference<ScheduledFuture<?>> _cancelProgress;

			RefreshComponent(AbstractProgressComponent progressComponent,
					AtomicReference<ScheduledFuture<?>> cancelProgress) {
				_progressComponent = progressComponent;
				_cancelProgress = cancelProgress;
			}

			@Override
			public void run() {
				if (!_progressComponent.refresh()) {
					// progress not yet finished.
					return;
				}
				ScheduledFuture<?> scheduledFuture = _cancelProgress.getAndSet(null);
				if (scheduledFuture != null) {
					scheduledFuture.cancel(false);
				}
				try {
					_progressComponent.finishParent();
				} catch (ThreadDeath ex) {
					throw ex;
				} catch (Throwable ex) {
					InfoService.logError(I18NConstants.PROGRESS_FAILED, "Progress failed in '"
						+ _progressComponent.getName() + "': " + _progressComponent.getLocation(), ex,
						RefreshComponent.class);
				} finally {
					_progressComponent.closeDialog();
				}

			}
		}

		/**
		 * Creates a {@link AbstractProgressCommandHandler}.
		 */
		public AbstractProgressCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		// Abstract methods

		/**
		 * Start the thread doing the work and return a {@link ProgressInfo} to monitor the state of
		 * that thread.
		 * 
		 * @param aComponent
		 *        The business component handed over to the
		 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
		 * 
		 * @return The progress info for monitoring, may be <code>null</code>.
		 */
		protected abstract ProgressInfo startThread(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments);

		/** Has to return the name of the progress component. */
		protected abstract ComponentName getProgressComponent();

		// Overridden methods from AbstractCommandHandler

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent callerComponent,
				Object model, Map<String, Object> someArguments) {
			ProgressInfo theInfo = this.startThread(aContext, callerComponent, model, someArguments);

			if (theInfo != null) {
				AbstractProgressComponent progressComponent = (AbstractProgressComponent) callerComponent
					.getMainLayout().getComponentByName(getProgressComponent());

				progressComponent.setInfo(theInfo);

				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordAwaitProgress(progressComponent);
				}

				LayoutComponent dialogTop = progressComponent.getDialogTopLayout();
				SimpleOpenDialog.openDialog(dialogTop, new ResourceText(I18NConstants.PROGRESS));
				DialogComponent dialog = dialogTop.getDialogSupport().getOpenedDialogs().get(dialogTop);
				refreshProgress(progressComponent, dialog, theInfo.getRefreshSeconds());
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private void refreshProgress(final AbstractProgressComponent progressComponent, DialogModel dialog,
				int refreshSeconds) {
			AtomicReference<ScheduledFuture<?>> cancelProgress = new AtomicReference<>();
			/* Ensure that progress is stopped when dialog is closed server side, e.g. during
			 * scripted test, when no JavaScript is executed. */
			dialog.addListener(DialogModel.CLOSED_PROPERTY, new DialogClosedListenerImplementation(cancelProgress));

			ScheduledExecutorService uiExecutor = progressComponent.getMainLayout().getWindowScope().getUIExecutor();
			RefreshComponent refreshComponent = new RefreshComponent(progressComponent, cancelProgress);
			ScheduledFuture<?> scheduledRefreshProgress =
				uiExecutor.scheduleWithFixedDelay(refreshComponent, refreshSeconds, refreshSeconds, TimeUnit.SECONDS);
			cancelProgress.set(scheduledRefreshProgress);
		}

	}
}
