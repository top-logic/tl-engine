/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.db2.HistoryCompaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog analyzing and starting a {@link HistoryCompaction} of the running application.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompactHistoryDialog extends AbstractFormPageDialog {

	/**
	 * Name of the field holding the point in time before which the history is compacted.
	 */
	private static final String BEFORE_DATE_FIELD = "beforeDate";

	/**
	 * Number of years the {@link #BEFORE_DATE_FIELD} is initialized back in time.
	 */
	private static final int DEFAULT_AGE_YEARS = 1;

	/**
	 * Width of the progress dialogs in pixels.
	 */
	private static final int PROGRESS_WIDTH = 600;

	/**
	 * Height of the progress dialogs in pixels.
	 */
	private static final int PROGRESS_HEIGHT = 400;

	private ComplexField _beforeDateField;

	/**
	 * Creates a {@link CompactHistoryDialog}.
	 *
	 * @param width
	 *        The width of the dialog.
	 * @param height
	 *        The height of the dialog.
	 */
	public CompactHistoryDialog(DisplayDimension width, DisplayDimension height) {
		super(I18NConstants.COMPACT_HISTORY_DIALOG_TITLE, I18NConstants.COMPACT_HISTORY_DIALOG_HEADER,
			I18NConstants.COMPACT_HISTORY_DIALOG_MESSAGE, width, height);
	}

	@Override
	protected IconControl createTitleIcon() {
		return null;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		_beforeDateField = FormFactory.newDateField(BEFORE_DATE_FIELD, defaultBeforeDate(), false);
		_beforeDateField.setMandatory(true);
		_beforeDateField.setLabel(I18NConstants.BEFORE_DATE);
		context.addMember(_beforeDateField);

		template(context, div(verticalBox(fieldBox(BEFORE_DATE_FIELD))));
	}

	private static Date defaultBeforeDate() {
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.add(Calendar.YEAR, -DEFAULT_AGE_YEARS);
		return calendar.getTime();
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return DefaultFormFieldControlProvider.INSTANCE.createControl(getFormContext());
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.ANALYZE_HISTORY, this::analyze));
		buttons.add(MessageBox.button(I18NConstants.COMPACT_HISTORY, this::compact));
		buttons.add(MessageBox.button(ButtonType.CLOSE, getDiscardClosure()));
	}

	private HandlerResult analyze(DisplayContext context) {
		HandlerResult problem = checkInput();
		if (problem != null) {
			return problem;
		}

		long beforeDate = beforeDate();
		return new ProgressDialog(I18NConstants.ANALYZE_HISTORY, px(PROGRESS_WIDTH), px(PROGRESS_HEIGHT)) {
			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				// The engine reports its progress and its result to the given log.
				HistoryCompactionOperation.analyze(beforeDate, log.asLog());
			}
		}.open(context);
	}

	private HandlerResult compact(DisplayContext context) {
		HandlerResult problem = checkInput();
		if (problem != null) {
			return problem;
		}

		ResKey violation = HistoryCompactionOperation.checkPreconditions();
		if (violation != null) {
			return MessageBox.newBuilder(MessageType.ERROR)
				.message(violation)
				.buttons(MessageBox.button(ButtonType.OK))
				.confirm(context);
		}

		long beforeDate = beforeDate();
		return MessageBox.newBuilder(MessageType.CONFIRM)
			.message(I18NConstants.CONFIRM_COMPACT)
			.buttons(MessageBox.button(ButtonType.OK, confirmContext -> startCompaction(confirmContext, beforeDate)),
				MessageBox.button(ButtonType.CANCEL))
			.confirm(context);
	}

	private HandlerResult startCompaction(DisplayContext context, long beforeDate) {
		return new CompactionProgressDialog(beforeDate).open(context);
	}

	/**
	 * Runs the compaction and restarts the persistency layer when its report has been read.
	 */
	private static final class CompactionProgressDialog extends ProgressDialog {

		private final long _beforeDate;

		CompactionProgressDialog(long beforeDate) {
			super(I18NConstants.COMPACT_HISTORY, px(PROGRESS_WIDTH), px(PROGRESS_HEIGHT));
			_beforeDate = beforeDate;
		}

		@Override
		protected void run(I18NLog log) throws AbortExecutionException {
			// The engine reports its progress and its result to the given log.
			HistoryCompactionOperation.compact(_beforeDate, log.asLog());

			log.info(I18NConstants.COMPACTION_FINISHED);
		}

		@Override
		protected void handleCompleted(DisplayContext context) {
			// Give the administrator the chance to read the report before the restart ends the
			// session displaying it.
			getDialogModel().addListener(DialogModel.CLOSED_PROPERTY,
				(sender, oldValue, newValue) -> restartPersistencyLayer());
		}

		private void restartPersistencyLayer() {
			Log log = new LogProtocol(CompactHistoryDialog.class);
			HistoryCompactionOperation.scheduleRestart(log);

			// Direct logout to prevent errors from being transported back to the UI.
			HttpSession session = DefaultDisplayContext.getDisplayContext().asRequest().getSession(false);
			if (session != null) {
				session.invalidate();
			}
		}
	}

	/**
	 * Checks the form input.
	 *
	 * @return The result reporting the problem, or <code>null</code> if the input is valid.
	 */
	private HandlerResult checkInput() {
		FormContext formContext = getFormContext();
		if (formContext.checkAll()) {
			return null;
		}
		return AbstractApplyCommandHandler.createErrorResult(formContext);
	}

	private long beforeDate() {
		return ((Date) _beforeDateField.getValue()).getTime();
	}

}
