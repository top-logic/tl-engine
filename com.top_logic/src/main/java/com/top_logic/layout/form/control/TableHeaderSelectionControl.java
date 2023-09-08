/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * {@link Control} to de- and select all table rows.
 * 
 * <p>
 * The {@link Control} displays a tristate checkbox to indicate the following states:
 * 
 * <ul>
 * <li>Empty box: Selection is empty.</li>
 * <li>Box in indeterminate state (usually represented by a dash in the box): A real non empty
 * subset of rows are selected.</li>
 * <li>Checked box: All rows are selected.</li>
 * </ul>
 * </p>
 * <p>
 * The appearance of the checkbox depends on the browser and may differ.
 * </p>
 * <p>
 * When the user (un-) checks the box, one of the following three actions will be performed:
 * <ul>
 * <li>Empty box: The box will be checked, all rows are selected.</li>
 * <li>Box in indeterminate state: The box will be checked, all rows are selected</li>
 * <li>Checked box: The box will be unchecked, none of the rows are selected.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TableHeaderSelectionControl extends AbstractControlBase implements SelectionListener {

	private static final Map<String, ControlCommand> VALUE_CHANGED_MAP = createCommandMap(ValueChanged.INSTANCE);

	private SelectionModel _selectionModel;

	private Set<?> _options;

	private boolean _isValid;

	/**
	 * Creates a {@link TableHeaderSelectionControl}.
	 */
	public TableHeaderSelectionControl(SelectionModel selectionModel, Set<?> options) {
		super(VALUE_CHANGED_MAP);
		
		_selectionModel = selectionModel;
		_options = options;
	}

	Set<?> options() {
		return _options;
	}

	SelectionModel selectionModel() {
		return _selectionModel;
	}

	@Override
	public Object getModel() {
		return selectionModel();
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		selectionModel().addSelectionListener(this);
	}

	@Override
	protected void internalDetach() {
		selectionModel().removeSelectionListener(this);

		super.internalDetach();
	}

	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		_isValid = false;
	}

	@Override
	protected boolean hasUpdates() {
		return !_isValid;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (!_isValid) {
			actions.add(createCheckboxUpdate());

			_isValid = true;
		}
	}

	private ClientAction createCheckboxUpdate() {
		Set<?> selection = selectionModel().getSelection();

		if (selection.isEmpty()) {
			return createCheckboxUpdate(false, false);
		} else if (selection.containsAll(options())) {
			return createCheckboxUpdate(true, false);
		} else {
			return createCheckboxUpdate(false, true);
		}
	}

	private ClientAction createCheckboxUpdate(boolean checked, boolean indeterminate) {
		return new JSSnipplet(new DynamicText() {
			
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("var checkbox = document.getElementById('");
				out.append(getInputId());
				out.append("');");
				out.append("checkbox.checked = " + String.valueOf(checked) + ";");
				out.append("checkbox.indeterminate = " + String.valueOf(indeterminate) + ";");
			}

		});
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute(ID_ATTR, getInputId());
			out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
			out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);

			writeOnChange(out);
			writeOnMouseDown(out);

			out.endEmptyTag();
		}
		out.endTag(SPAN);

		/* 
		 * Is necessary to set the initial state of the checkbox.
		 * 
		 * Although it is possible to check or uncheck the checkbox by the html boolean attribute checked
		 * of the input element, it is not possible to set the checkbox state to
		 * indeterminate. This is only possible using javascript by setting the indeterminate
		 * property of the dom node. 
		 */
		getFrameScope().addClientAction(createCheckboxUpdate());
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
		out.append(".handleOnChange(this, true);");
		out.append("return true;");
		out.endAttribute();
	}

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

			updateSelection((TableHeaderSelectionControl) control, isAllSelected);

			return HandlerResult.DEFAULT_RESULT;
		}

		private void updateSelection(TableHeaderSelectionControl control, boolean selectAll) {
			SelectionModel selectionModel = control.selectionModel();
			if (selectAll) {
				Set<?> options = control.options();
				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordSelection(selectionModel, options, true, SelectionChangeKind.ABSOLUTE);
				}
				selectionModel.setSelection(options);
			} else {
				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordSelection(selectionModel, ScriptingRecorder.NO_SELECTION, false,
						SelectionChangeKind.ABSOLUTE);
				}
				selectionModel.clear();
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED;
		}
	}
}
