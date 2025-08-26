/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SubtreeSelectionModel;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * {@link Control} that displays and modifies the selection state of a single object within a
 * {@link SelectionModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionPartControl extends AbstractControlBase implements SelectionListener {

	/**
	 * Commands of {@link SelectionPartControl}.
	 */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ControlCommand[] {
			ValueChanged.INSTANCE
		});

	/**
	 * Creates a {@link SelectionPartControl} without veto listeners.
	 */
	public static <T> Control createSelectionPartControl(SelectionModel<T> selectionModel, T part) {
		return createSelectionPartControl(selectionModel, part, Collections.emptyList());
	}

	/**
	 * Creates a {@link SelectionPartControl}.
	 * 
	 * @param selectionModel
	 *        The {@link SelectionModel} of which a part is displayed and observed.
	 * @param part
	 *        The element whose selection status is displayed. The control displays a checked box,
	 *        if the element is part of the {@link SelectionModel}, and an unchecked box, if the
	 *        element is not selected in the given {@link SelectionModel}.
	 */
	public static <T> Control createSelectionPartControl(SelectionModel<T> selectionModel, T part,
			Iterable<SelectionVetoListener> vetoListeners) {
		if (selectionModel instanceof SubtreeSelectionModel<T> subtreeSelection) {
			SubtreeSelectionPartControl<T> result = new SubtreeSelectionPartControl<>(subtreeSelection, part);
			vetoListeners.forEach(result::addSelectionVetoListener);
			return result;
		} else if (selectionModel.isMultiSelectionSupported()
			&& selectionModel instanceof TreeSelectionModel<?> treeSelection) {
			TreeSelectionPartControl result = new TreeSelectionPartControl(treeSelection, part);
			vetoListeners.forEach(result::addSelectionVetoListener);
			return result;
		} else {
			SelectionPartControl result = new SelectionPartControl(selectionModel, part);
			vetoListeners.forEach(result::addSelectionVetoListener);
			return result;
		}
	}

	private SelectionModel _selectionModel;

	private Object _selectionPart;

	private List<SelectionVetoListener> _vetoListeners = Collections.emptyList();

	private String _inputStyle;

	private boolean _selectionValid = true;

	/**
	 * Creates a {@link SelectionPartControl}.
	 * 
	 * @param selectionModel
	 *        The {@link SelectionModel} of which a part is displayed and observed.
	 * @param part
	 *        The element whose selection status is displayed. The control displays a checked box,
	 *        if the element is part of the {@link SelectionModel}, and an unchecked box, if the
	 *        element is not selected in the given {@link SelectionModel}.
	 */
	protected SelectionPartControl(SelectionModel selectionModel, Object part) {
		super(COMMANDS);
		_selectionModel = selectionModel;
		_selectionPart = part;
	}

	@Override
	public Object getModel() {
		return getSelectionPart();
	}

	/**
	 * The object whose selection state is controlled by this control.
	 */
	public Object getSelectionPart() {
		return _selectionPart;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		addSelectionModelListener(this);
	}

	@Override
	protected void internalDetach() {
		removeSelectionModelListener(this);

		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute(ID_ATTR, getInputId());

			if (isSingleSelection()) {
				out.writeAttribute(TYPE_ATTR, RADIO_TYPE_VALUE);
			} else {
				out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
			}

			if (isSelected()) {
				out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
			}

			if (isDisabled()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}

			out.beginCssClasses();
			writeInputCssClassesContent(out);
			out.endCssClasses();

			out.writeAttribute(STYLE_ATTR, getInputStyle());

			writeOnChange(context, out);

			out.endEmptyTag();
		}
		out.endTag(SPAN);
	}

	/**
	 * Writes the CSS classes of the input element.
	 */
	protected void writeInputCssClassesContent(TagWriter out) throws IOException {
		if (isSingleSelection()) {
			out.write(FormConstants.IS_RADIO_CSS_CLASS);
		} else {
			out.write(FormConstants.IS_CHECKBOX_CSS_CLASS);
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "tl-radio-checkbox-container";
	}

	private void writeOnChange(DisplayContext context, TagWriter out) throws IOException {
		if (context.getUserAgent().is_ie()) {
			// Workaround for IE bug: onchange events for select input
			// are fired too late. Updates are almost always lost. Therefore,
			// the onclick event is used to grab the input value. Operation
			// must always continue, because the browser otherwise stops processing
			// and the new value is not displayed in the client.
			out.beginAttribute(ONCLICK_ATTR);
			writeOnClickContent(out);
			out.endAttribute();
		} else {
			out.beginAttribute(ONCHANGE_ATTR);
			writeOnClickContent(out);
			out.endAttribute();
		}
	}

	private void writeOnClickContent(TagWriter out) throws IOException {
		out.append(FormConstants.SELECTION_PART_CONTROL_CLASS);
		out.append(".handleOnChange(this, ");
		writeIdJsString(out);
		out.append(", true);");
		out.append("return true;");
	}

	private String getInputId() {
		return getID() + "-input";
	}

	private String getInputStyle() {
		return _inputStyle;
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
			Boolean value = (Boolean) arguments.get(VALUE_PARAM);

			SelectionPartControl optionControl = (SelectionPartControl) control;

			optionControl.updateSelectionVeto(optionControl, Utils.isTrue(value));

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED;
		}
	}

	@Override
	public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
		if (event.getUpdatedObjects().contains(getSelectionPart())) {
			invalidateSelection();
		}
	}

	private void invalidateSelection() {
		_selectionValid = false;
	}

	@Override
	protected boolean hasUpdates() {
		return !_selectionValid;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (!_selectionValid) {
			actions.add(
				new PropertyUpdate(getInputId(), CHECKED_ATTR,
					new ConstantDisplayValue(Boolean.toString(isSelected()))));

			_selectionValid = true;
		}
	}

	/**
	 * @see SelectionModel#addSelectionListener(SelectionListener)
	 */
	public final void addSelectionModelListener(SelectionPartControl control) {
		_selectionModel.addSelectionListener(control);
	}

	/**
	 * @see SelectionModel#removeSelectionListener(SelectionListener)
	 */
	public final void removeSelectionModelListener(SelectionPartControl control) {
		_selectionModel.removeSelectionListener(control);
	}

	/**
	 * The {@link SelectionModel} .
	 */
	protected final SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	/**
	 * true, if a single option can be selected only, false otherwise
	 */
	public final boolean isSingleSelection() {
		return _selectionModel instanceof SingleSelectionModel;
	}

	/**
	 * Registers the given {@link SelectionVetoListener}.
	 */
	public void addSelectionVetoListener(SelectionVetoListener listener) {
		if (_vetoListeners == Collections.<SelectionVetoListener> emptyList()) {
			_vetoListeners = new ArrayList<>();
		}
		_vetoListeners.add(listener);
	}

	/**
	 * Removes the given {@link SelectionVetoListener}.
	 */
	public void removeSelectionVetoListener(SelectionVetoListener listener) {
		if (_vetoListeners.isEmpty()) {
			return;
		}
		_vetoListeners.remove(listener);
	}

	/**
	 * Returns the {@link SelectionVetoListener}s.
	 * 
	 * @return the {@link SelectionVetoListener}s.
	 */
	protected List<SelectionVetoListener> getVetoListeners() {
		return _vetoListeners;
	}

	/**
	 * true, if the selectable option is currently disabled, false otherwise.
	 */
	public boolean isDisabled() {
		return !getSelectionModel().isSelectable(getSelectionPart());
	}

	/**
	 * true, if the selectable option is currently checked, false otherwise.
	 */
	public boolean isSelected() {
		return getSelectionModel().isSelected(getSelectionPart());
	}

	/**
	 * Update of the {@link SelectionModel}.
	 */
	public void updateSelection(boolean selected) {
		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordSelection(getSelectionModel(), getSelectionPart(), selected,
				SelectionChangeKind.INCREMENTAL);
		}
		getSelectionModel().setSelected(getSelectionPart(), selected);
	}

	/**
	 * Implementation of {@link #updateSelection(boolean)} but checks control for veto.
	 * 
	 * @param control
	 *        The holder for the veto listeners.
	 */
	public void updateSelectionVeto(SelectionPartControl control, boolean selected) {
		try {
			for (SelectionVetoListener vetoListener : getVetoListeners()) {
				vetoListener.checkVeto(getSelectionModel(), getSelectionPart(), SelectionType.TOGGLE_SINGLE);
			}
			updateSelection(selected);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					updateSelection(selected);
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(control.getWindowScope());

		}

	}

}
