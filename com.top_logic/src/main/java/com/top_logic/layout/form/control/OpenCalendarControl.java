/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.DisplayDimension.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormFieldControl} opening a date chooser.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenCalendarControl extends AbstractFormFieldControl {

	/**
	 * Commands registered at this control.
	 * 
	 * @see #getCommand(String)
	 */
	public static final Map<String, ControlCommand> OPEN_CALENDAR_COMMANDS =
		createCommandMap(AbstractFormFieldControl.COMMANDS, ToggleCalendarVisibilityCommand.INSTANCE);

	/**
	 * {@link ControlProvider} creating an {@link OpenCalendarControl} for the model.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class OpenCalendar implements ControlProvider {

		/** Singleton {@link OpenCalendar} instance. */
		public static final OpenCalendar INSTANCE = new OpenCalendar();

		/**
		 * Creates a new {@link OpenCalendar}.
		 */
		protected OpenCalendar() {
			// singleton instance
		}

		@Override
		public Control createControl(Object model, String style) {
			return new OpenCalendarControl((FormField) model);
		}

	}

	/**
	 * This command opens a new calendar dialog.
	 * 
	 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
	 */
	public static final class ToggleCalendarVisibilityCommand extends ControlCommand {

		private static final String COMMAND_NAME = "toggleCalendarVisibility";

		final static ToggleCalendarVisibilityCommand INSTANCE = new ToggleCalendarVisibilityCommand();

		private ToggleCalendarVisibilityCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			OpenCalendarControl dateControl = (OpenCalendarControl) control;

			if (dateControl.hasCalendarPopup()) {
				closeCalendarDialog(dateControl);
			} else {
				openCalendarDialog(commandContext, dateControl);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private void closeCalendarDialog(OpenCalendarControl dateControl) {
			dateControl.getCalendarPopup().getPopupDialogModel().setClosed();
		}

		/**
		 * Opens calendar dialog for given {@link OpenCalendarControl}.
		 * 
		 * @param commandContext
		 *        The display context
		 * @param dateControl
		 *        The control owning the popup
		 */
		private void openCalendarDialog(DisplayContext commandContext, OpenCalendarControl dateControl) {
			// Create dialog model
			PopupDialogModel dialogModel = new DefaultPopupDialogModel(
				null,
				new DefaultLayoutData(dim(240, DisplayUnit.PIXEL), 100, dim(160, DisplayUnit.PIXEL), 100,
					Scrolling.AUTO),
				1);

			dialogModel.addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, new DialogClosedListener() {

				@Override
				public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
					dateControl.clearCalendarPopup();
					dialogModel.removeListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
				}
			});

			// Create Controls and setup
			String placementID = dateControl.getID() + OpenCalendarControl.IMAGE_ID_SUFFIX;
			PopupDialogControl calendarDialog =
				new PopupDialogControl(dateControl.getFrameScope(), dialogModel, placementID);

			/* TimeZone can not be taken from the user. The same TimeZone as in the format must be
			 * used to be consistent with the textual representation of the seleted date. */
			TimeZone timeZone = dateControl.getTimeZone();
			Locale currentLocale = commandContext.getSubSessionContext().getCurrentLocale();
			CalendarControl calendarControl =
				new CalendarControl(dialogModel, dateControl.getFieldModel(), timeZone, currentLocale);
			calendarDialog.setContent(calendarControl);

			dateControl.getFrameScope().getWindowScope().openPopupDialog(calendarDialog);

			dateControl.setCalendarPopup(calendarDialog);
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.form.control.I18NConstants.TOGGLE_CALENDAR_VISIBILITY;
		}
	}

	final static String IMAGE_ID_SUFFIX = "-commandImage";

	private PopupDialogControl _calendarPopup;

	/**
	 * Creates a {@link OpenCalendarControl}.
	 */
	public OpenCalendarControl(FormField model) {
		super(model, OPEN_CALENDAR_COMMANDS);
	}

	TimeZone getTimeZone() {
		FormField fieldModel = getFieldModel();
		if (fieldModel instanceof ComplexField) {
			ComplexField model = (ComplexField) fieldModel;
			Format format = model.getFormat();
			if (format instanceof DateFormat) {
				return ((DateFormat) format).getTimeZone();
			}
		}
		return TimeZones.systemTimeZone();
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		ButtonWriter buttonWriter = new ButtonWriter(this, Icons.CALENDAR, ToggleCalendarVisibilityCommand.INSTANCE);
		buttonWriter.setID(getImageId());
		buttonWriter.setTooltip(I18NConstants.OPEN_CALENDAR__LABEL.fill(getModel().getLabel()));
		buttonWriter.setAutocomplete("off");
		
		if (getFieldModel().isDisabled()) {
			buttonWriter.writeDisabledButton(context, out);
		} else {
			buttonWriter.writeButton(context, out);
		}

		out.endTag(SPAN);
	}

	@Override
	protected String getTypeCssClass() {
		return "cOpenCalendar";
	}

	/**
	 * the client side id of the image which opens the calendar.
	 */
	private String getImageId() {
		return getID() + IMAGE_ID_SUFFIX;
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		writeInvisible(context, out);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!sender.isImmutable()) {
			addDisabledUpdate(newValue.booleanValue());
			requestRepaint();
		}
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// Value is not displayed directly, no update necessary.
	}

	/* package protected */boolean hasCalendarPopup() {
		return _calendarPopup != null;
	}

	/* package protected */void setCalendarPopup(PopupDialogControl calendarPopup) {
		_calendarPopup = calendarPopup;
	}

	/* package protected */PopupDialogControl getCalendarPopup() {
		return _calendarPopup;
	}

	/* package protected */void clearCalendarPopup() {
		_calendarPopup = null;
	}
}
