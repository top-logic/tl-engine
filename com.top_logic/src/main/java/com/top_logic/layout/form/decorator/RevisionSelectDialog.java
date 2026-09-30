/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateControl;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormPageDialog} to get a revision from a date selected by the user.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RevisionSelectDialog extends AbstractFormPageDialog {

	private static final String COMPARE_DATE_I18N_SUFFIX = "compareDate";

	private static final String NO_DATE_SELECTED_I18N_SUFFIX = "noDateSelected";

	/**
	 * Name of the {@link FormField} selecting the date.
	 */
	protected static final String DATE_FIELD_NAME = "dateField";

	/**
	 * Creates a new {@link RevisionSelectDialog}.
	 * 
	 * @param currentModel
	 *        The current model to select a comparison revision for.
	 * @param revCallback
	 *        The callback which is called with the selected revision.
	 * 
	 * @return The desired dialog.
	 * 
	 * @see #newRevisionSelectDialog(Object, RevisionCallback, DisplayDimension, DisplayDimension,
	 *      boolean)
	 */
	public static RevisionSelectDialog newRevisionSelectDialog(Object currentModel, RevisionCallback revCallback) {
		return newRevisionSelectDialog(
			currentModel,
			revCallback,
			DisplayDimension.dim(550, DisplayUnit.PIXEL),
			DisplayDimension.dim(350, DisplayUnit.PIXEL),
			true);
	}

	/**
	 * Creates a new {@link RevisionSelectDialog}.
	 * 
	 * @param currentModel
	 *        The current model to select a comparison revision for.
	 * @param revCallback
	 *        The callback which is called with the selected revision.
	 * @param width
	 *        Width of the dialog.
	 * @param height
	 *        Height of the dialog.
	 * @param suppressRecording
	 *        Whether actions like click of buttons or filling of formFields should not be recorded.
	 * @return The desired dialog.
	 */
	public static RevisionSelectDialog newRevisionSelectDialog(Object currentModel, RevisionCallback revCallback,
			DisplayDimension width, DisplayDimension height, boolean suppressRecording) {
		if (ComponentUtil.isHistoric(currentModel)) {
			return new HistoricRevisionSelectDialog(revCallback, width, height, suppressRecording);
		} else {
			return new CurrentRevisionSelectDialog(revCallback, width, height, suppressRecording);
		}
	}

	/**
	 * Appends a string displaying the {@link #DATE_FIELD_NAME}.
	 */
	protected static void appendDateField(StringBuilder out) {
		out.append("<span style='margin-right:5px;'><t:text key='" + COMPARE_DATE_I18N_SUFFIX + "' /></span>");
		out.append("<p:field name='");
		out.append(DATE_FIELD_NAME);
		out.append("' />");
	}

	final RevisionCallback _revCallback;

	final CommandModel _closeCommand;

	private final boolean _suppressRecording;

	/**
	 * Callback interface to create a command that shall be executed with the selected revision.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface RevisionCallback {

		/**
		 * @param rev
		 *        The revision selected by the user
		 * 
		 * @return The {@link Command} to execute when the user has selected the revision.
		 */
		Command commandForRevision(Revision rev);

	}

	/**
	 * Creates a new {@link RevisionSelectDialog}.
	 * 
	 * @param revCallback
	 *        see {@link #newRevisionSelectDialog(Object, RevisionCallback)}
	 * 
	 * @see #newRevisionSelectDialog(Object, RevisionCallback, DisplayDimension, DisplayDimension,
	 *      boolean)
	 */
	protected RevisionSelectDialog(RevisionCallback revCallback, DisplayDimension width, DisplayDimension height,
			boolean suppressRecording) {
		super(I18NConstants.SELECT_DIALOG, width, height);
		_revCallback = revCallback;
		_suppressRecording = suppressRecording;
		Command closeCommand = createCloseCommand();
		_closeCommand = MessageBox.button(ButtonType.OK, closeCommand);
		updateCloseCommandExecutability(false);
	}

	/**
	 * Creates the actual command to execute when the user has selected the {@link Revision}.
	 */
	protected abstract AbstractCloseCommand createCloseCommand();

	/**
	 * Command that executes the {@link RevisionSelectDialog#_revCallback callback command} and
	 * closes the dialog.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected static abstract class AbstractCloseCommand implements Command {

		protected final RevisionSelectDialog _dialog;

		public AbstractCloseCommand(RevisionSelectDialog dialog) {
			_dialog = dialog;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			Revision revision = getRevision();
			HandlerResult executeCommand =
				_dialog._revCallback.commandForRevision(revision).executeCommand(context);
			if (executeCommand.isSuccess()) {
				return _dialog.getDiscardClosure().executeCommand(context);
			}
			return executeCommand;
		}

		/**
		 * Determines the selected revision.
		 */
		protected abstract Revision getRevision();

	}

	@Override
	protected final void fillButtons(List<CommandModel> buttons) {
		internalFillButtons(buttons);
		if (_suppressRecording) {
			for (CommandModel button : buttons) {
				ScriptingRecorder.annotateAsDontRecord(button);
			}
		}
	}

	/**
	 * Actual implementation of {@link #fillButtons(List)}.
	 */
	protected void internalFillButtons(List<CommandModel> buttons) {
		buttons.add(_closeCommand);
		addCancel(buttons);
	}

	@Override
	protected IconControl createTitleIcon() {
		return null;
	}

	@Override
	protected HTMLFragment createBodyContent() {
		FormTemplate template =
			new FormTemplate(I18NConstants.SELECT_DIALOG, getControlProvider(), true, getTemplate());
		return new FormTemplateControl(getFormContext(), template);
	}

	/**
	 * The document describing how to diaplay this dialog.
	 */
	protected abstract Document getTemplate();

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.SELECT_DIALOG;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		if (_suppressRecording) {
			ScriptingRecorder.annotateAsDontRecord(context);
		}
		addDateField(context);
	}

	private void addDateField(FormContext context) {
		FormField newDateField = new DateTimeField(DATE_FIELD_NAME, new Date(), false);
		newDateField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				updateCloseCommandExecutability(newValue != null);
			}
		});
		newDateField.addListener(FormField.HAS_ERROR_PROPERTY, new HasErrorChanged() {

			@Override
			public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
				if (newError) {
					updateCloseCommandExecutability(false);
				} else {
					updateCloseCommandExecutability(sender.getValue() != null);
				}
				return Bubble.BUBBLE;
			}
		});
		updateCloseCommandExecutability(true);
		context.addMember(newDateField);
	}

	/**
	 * Returns the {@link #DATE_FIELD_NAME date field}.
	 */
	protected FormField getDateField(FormContext context) {
		return context.getField(DATE_FIELD_NAME);
	}

	void updateCloseCommandExecutability(boolean executable) {
		if (executable) {
			_closeCommand.setExecutable();
		} else {
			_closeCommand.setNotExecutable(I18NConstants.SELECT_DIALOG.key(NO_DATE_SELECTED_I18N_SUFFIX));
		}
	}

}

