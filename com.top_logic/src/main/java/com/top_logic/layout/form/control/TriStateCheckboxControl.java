/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * {@link Control} displaying an checkbox with indeterminate state.
 * 
 * <p>
 * The appearance of the checkbox depends on the browser and may differ.
 * </p>
 * 
 * <p>
 * When the user (un-) checks the box, one of the following three actions will be performed:
 * <ul>
 * <li>Empty box: The box will be checked.</li>
 * <li>Box in indeterminate state: The box will be checked.</li>
 * <li>Checked box: The box will be unchecked.</li>
 * </ul>
 * </p>
 */
public abstract class TriStateCheckboxControl extends AbstractControlBase {
	
	/**
	 * {@link Enum} representing the state of this {@link TriStateCheckboxControl}.
	 */
	public static enum State {
		
		/**
		 * Checked checkbox.
		 */
		CHECKED,

		/**
		 * Empty checkbox.
		 */
		UNCHECKED,

		/**
		 * Indeterminate checkbox.
		 */
		INDETERMINATE,

	}

	private static final Map<String, ControlCommand> VALUE_CHANGED_MAP = createCommandMap(ValueChanged.INSTANCE);

	private boolean _isValid;

	/**
	 * Creates a {@link TriStateCheckboxControl}.
	 */
	public TriStateCheckboxControl() {
		super(VALUE_CHANGED_MAP);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Forces this control to update its UI.
	 */
	protected boolean invalidate() {
		return _isValid = false;
	}

	@Override
	protected boolean hasUpdates() {
		return !_isValid;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (!_isValid) {
			actions.add(new JSSnipplet(createCheckboxUpdate()));

			_isValid = true;
		}
	}

	/**
	 * Creates a script that updates the state of the checkbox.
	 */
	protected DynamicText createCheckboxUpdate() {
		return createCheckboxUpdate(currentState());
	}

	private DynamicText createCheckboxUpdate(State state) {
		switch (state) {
			case CHECKED:
				return checkboxUpdateScript(true, false);
			case INDETERMINATE:
				return checkboxUpdateScript(false, true);
			case UNCHECKED:
				return checkboxUpdateScript(false, false);

		}
		throw new UnreachableAssertion(state + " no covered.");
	}

	/**
	 * Creates an update script that sets the checkbox to the given state.
	 */
	protected final DynamicText checkboxUpdateScript(boolean checked, boolean indeterminate) {
		return new DynamicText() {
			
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("var checkbox = document.getElementById('");
				out.append(getInputId());
				out.append("');");
				out.append("checkbox.checked = " + String.valueOf(checked) + ";");
				out.append("checkbox.indeterminate = " + String.valueOf(indeterminate) + ";");
			}

		};
	}

	/**
	 * Computes the state for this control.
	 */
	protected abstract State currentState();

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		boolean indeterminate = false;
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute(ID_ATTR, getInputId());
			out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
			out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);

			switch (currentState()) {
				case CHECKED:
					out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
					break;
				case INDETERMINATE:
					// handled later
					indeterminate = true;
					break;
				case UNCHECKED:
					// empty checkbox.
					break;

			}

			writeOnChange(out);
			writeOnMouseDown(out);

			out.endEmptyTag();
		}
		out.endTag(SPAN);

		if (indeterminate) {
			/* Is necessary to set the initial state of the checkbox.
			 * 
			 * Although it is possible to check or uncheck the checkbox by the html boolean
			 * attribute checked of the input element, it is not possible to set the checkbox state
			 * to indeterminate. This is only possible using javascript by setting the indeterminate
			 * property of the dom node. */
			HTMLUtil.beginScriptAfterRendering(out);
			createCheckboxUpdate(State.INDETERMINATE).append(context, out);
			HTMLUtil.endScriptAfterRendering(out);
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "cTriState";
	}

	private void writeOnMouseDown(TagWriter out) throws IOException {
		out.beginAttribute(ONMOUSEDOWN_ATTR);
		out.append("event.stopPropagation();");
		out.endAttribute();
	}

	private CharSequence getInputId() {
		return getID() + "-input";
	}

	private void writeOnChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		out.append(FormConstants.SELECTION_PART_CONTROL_CLASS);
		out.append(".handleOnChange(this, ");
		writeIdJsString(out);
		out.append(", true);");
		out.append("return true;");
		out.endAttribute();
	}

	/**
	 * Updates the model of this control due to user interaction.
	 *
	 * @param select
	 *        Whether the user has selected or unselected this control.
	 */
	protected abstract void updateSelection(boolean select);


	private static class ValueChanged extends ControlCommand {

		public static final ControlCommand INSTANCE = new ValueChanged();

		public static final String COMMAND_ID = "valueChanged";

		private static final String VALUE_PARAM = "value";

		public ValueChanged() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			boolean isAllSelected = Utils.isTrue((Boolean) arguments.get(VALUE_PARAM));

			((TriStateCheckboxControl) control).updateSelection(isAllSelected);

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED;
		}
	}
}
