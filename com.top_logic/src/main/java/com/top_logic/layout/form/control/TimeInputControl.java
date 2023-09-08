/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control which renders the time aspect of a {@link Date} and the possibility to open a clock to
 * change the time.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TimeInputControl extends TextInputControl {

	/**
	 * Commands registered at this control.
	 * 
	 * @see #getCommand(String)
	 */
	public static final Map<String, ControlCommand> TIME_INPUT_COMMANDS = createCommandMap(TextInputControl.COMMANDS,
		new ControlCommand[] {
			ValueChangedParseToFormat.INSTANCE,
			ShowClock.INSTANCE
		});

	private final static String IMAGE_ID_SUFFIX = "-commandImage";

	/**
	 * Use a custom default value, to fit a regular date into the input field of the tag (e.g. 09:03
	 * PM).
	 */
	public static final int DEFAULT_COLUMN_SIZE = 3;

	/**
	 * Creates a TimeInputControl and sets the columms to DEFAULT_COLUMN_SIZE.
	 * 
	 * @param model
	 *        Server-side representation for this input field.
	 */
	public TimeInputControl(FormField model) {
		super(model, TIME_INPUT_COMMANDS);

		setColumns(DEFAULT_COLUMN_SIZE);
	}

	@Override
	protected void writeEditableContents(DisplayContext context, TagWriter out) throws IOException {
		super.writeEditableContents(context, out);

		writeFixedRight(context, out);
	}

	private void writeFixedRight(DisplayContext context, TagWriter out)
			throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
		out.endBeginTag();

		ButtonWriter buttonWriter = new ButtonWriter(this, Icons.CLOCK, ShowClock.INSTANCE);
		buttonWriter.setID(getImageId());
		buttonWriter.setTooltip(I18NConstants.OPEN_CLOCK__LABEL.fill(getModel().getLabel()));
		buttonWriter.setAutocomplete("off");
		
		if (getFieldModel().isActive()) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}

		out.endTag(SPAN);
	}

	/**
	 * the client side id of the image which opens the calendar.
	 */
	private String getImageId() {
		return getID() + IMAGE_ID_SUFFIX;
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Date popup image must be disabled in addition to disabling the input element.
		requestRepaint();
	}

	@Override
	protected String getTypeCssClass() {
		return "cTimeInput";
	}

	@Override
	protected String getJsClass() {
		return FormConstants.CLOCK_HANDLER_CLASS;
	}

	String getValueTime() {
		FormField fieldModel = getFieldModel();
		SimpleDateFormat sdFormat = getFormat();

		Date value = (Date) fieldModel.getValue();

		if (value == null) {
			value = new Date();
		}
		return sdFormat.format(value);
	}

	String getDefaultTime() {
		Date defaultValue = new Date();

		SimpleDateFormat sdFormat = getFormat();
		return sdFormat.format(defaultValue);
	}

	SimpleDateFormat getFormat() {
		SimpleDateFormat sdFormat;
		// 'hh:mm a' for 12-hours-clock, 'HH:mm' for 24-hours-clock
		sdFormat = CalendarUtil.newSimpleDateFormat("HH:mm");
		sdFormat.setTimeZone(ThreadContext.getTimeZone());

		return sdFormat;
	}

	void openClock() {
		addUpdate(new JSSnipplet(new AbstractDisplayValue() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append(FormConstants.CLOCK_HANDLER_CLASS);
				out.append(".initializeAndOpenTimepicker(");
				out.append("this, ");
				((TagWriter) out).writeJsString(getFormat().toPattern());
				out.append(", ");
				((TagWriter) out).writeJsString(getInputId());
				out.append(", ");
				((TagWriter) out).writeJsString(getID());
				out.append(", ");
				((TagWriter) out).writeJsString(getValueTime());
				out.append(", ");
				((TagWriter) out).writeJsString(getDefaultTime());

				out.append(");");
			}
		}));
	}

	/**
	 * Command to open the clock.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	protected static class ShowClock extends ControlCommand {

		/**
		 * ID of the command.
		 */
		protected static final String COMMAND_ID = "showClock";

		/**
		 * Single instance of the {@link ShowClock}.
		 */
		public static final ControlCommand INSTANCE = new ShowClock((COMMAND_ID));

		private ShowClock(String aCommand) {
			super(aCommand);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_CLOCK_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			TimeInputControl timeInputCtrl = (TimeInputControl) control;

			timeInputCtrl.openClock();

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command to parse a value and a format to a Date object. Needs as argument in execute() a
	 * value.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	protected static class ValueChangedParseToFormat extends ControlCommand {

		/**
		 * ID of the command.
		 */
		protected static final String COMMAND_ID = "valueChangedParseToFormat";

		/**
		 * Single instance of the {@link ValueChangedParseToFormat}.
		 */
		public static final ControlCommand INSTANCE = new ValueChangedParseToFormat();

		/**
		 * Parses the value of the clock to a Date-object for the server.
		 */
		public ValueChangedParseToFormat() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final TimeInputControl timeInputCtrl = (TimeInputControl) control;
			if (timeInputCtrl.isViewDisabled()) {
				Logger.warn(
					"Received '" + getID() + "' for a client side disabled control. Model: "
						+ timeInputCtrl.getModel(),
					AbstractFormFieldControlBase.class);
				timeInputCtrl.requestRepaint();
				return HandlerResult.DEFAULT_RESULT;
			}

			final Object newValue = arguments.get("value");

			Date date = null;
			try {
				date = timeInputCtrl.getFormat().parse((String) newValue);
			} catch (ParseException ex) {
				return HandlerResult.error(I18NConstants.ILLEGAL_CLOCK_TIME_FORMAT.fill(newValue), ex);
			}

			AbstractFormField fieldModel = (AbstractFormField) timeInputCtrl.getFieldModel();
			updateField(commandContext, fieldModel, date);

			return HandlerResult.DEFAULT_RESULT;
		}

		private void updateField(DisplayContext commandContext, AbstractFormField target, Date date) {
			try {
				FormFieldInternals.setValue(target, date);
			} catch (VetoException ex) {
				ex.setContinuationCommand(createUpdateContinuation(target, date));
				ex.process(commandContext.getWindowScope());
			}
		}

		private static Command createUpdateContinuation(AbstractFormField target, Date date) {
			return new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					target.setValue(date);
					return HandlerResult.DEFAULT_RESULT;
				}
			};
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED_CLOCK;
		}
	}
}