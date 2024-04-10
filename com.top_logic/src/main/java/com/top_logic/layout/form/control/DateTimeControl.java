/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.form.FormConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link Control} that renders a {@link DateTimeField}.
 *
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 */
public class DateTimeControl extends AbstractFormFieldControl {

	/**
	 * Creates a new {@link DateTimeControl} with the given model.
	 */
	public DateTimeControl(FormField model) {
		super(model);
	}

	private static final String DATE_TIME_CLASS = "cDatetime";

	private static final String TIME_CSS_CLASS = "timePart";

	private Control _dayControl;

	private Control _timeControl;

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		DateTimeField dateTimeField = (DateTimeField) getModel();
		if (dateTimeField.getDayField().isImmutable()) {
			writeImmutable(context, out);
		} else {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			{
				{
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, FLEXIBLE_CSS_CLASS);
					out.endBeginTag();
					_dayControl.write(context, out);
					out.endTag(SPAN);
				}
				{
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, FIXED_RIGHT_CSS_CLASS + " " + TIME_CSS_CLASS);
					out.endBeginTag();
					_timeControl.write(context, out);
					out.endTag(SPAN);
				}

			}
			out.endTag(SPAN);
		}

	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		DateTimeField dateTimeField = (DateTimeField) getModel();
		_dayControl = DefaultFormFieldControlProvider.INSTANCE.createControl(dateTimeField.getDayField());
		_timeControl = DefaultFormFieldControlProvider.INSTANCE.createControl(dateTimeField.getTimeField());
	}

	@Override
	protected void internalDetach() {
		_dayControl = null;
		_timeControl = null;
		super.internalDetach();
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		DateTimeField dateTimeField = (DateTimeField) getModel();
		ComplexField dayField = dateTimeField.getDayField();
		ComplexField timeField = dateTimeField.getTimeField();
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.writeText(dayField.getAsString() + ", " + timeField.getAsString());
		out.endTag(SPAN);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		HTMLUtil.appendCSSClass(out, DATE_TIME_CLASS);
		super.writeControlClassesContent(out);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Do nothing, because the two controls - daycontrol and timecontrol - handle it themselves.
	}

	/**
	 * {@link ControlProvider} to instantiate a {@link DateTimeControl} for a {@link DateTimeField}.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class CP implements ControlProvider {

		/**
		 * Singleton {@link CP} instance.
		 */
		public static final CP INSTANCE = new CP();

		/** Singleton constructor. */
		public CP() {
		}

		@Override
		public Control createControl(Object model, String style) {
			if (model instanceof FormField) {
				FormField member = (FormField) model;
				return new DateTimeControl(member);
			}
			return DefaultFormFieldControlProvider.INSTANCE.createControl(model);
		}
	}

}
