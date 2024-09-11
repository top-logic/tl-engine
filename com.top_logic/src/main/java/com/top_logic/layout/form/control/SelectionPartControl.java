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
import java.util.Set;

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
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;
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

	private SelectionPartModel _selectionPartModel;

	private int _tabIndex = -1;

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
	public SelectionPartControl(SelectionModel selectionModel, Object part) {
		this(new DefaultSelectionPartModel(selectionModel, part));
	}

	/**
	 * Create a new {@link SelectionPartControl}.
	 */
	public SelectionPartControl(SelectionPartModel selectionPartModel) {
		super(COMMANDS);
		_selectionPartModel = selectionPartModel;
	}

	@Override
	public SelectionPartModel getModel() {
		return _selectionPartModel;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_selectionPartModel.addSelectionModelListener(this);
	}

	@Override
	protected void internalDetach() {
		_selectionPartModel.removeSelectionModelListener(this);

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

			boolean single = isSingle();
			if (single) {
				out.writeAttribute(TYPE_ATTR, RADIO_TYPE_VALUE);
			} else {
				out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
			}

			if (isChecked()) {
				out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
			}

			if (isDisabled()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}

			if (single) {
				out.writeAttribute(CLASS_ATTR, FormConstants.IS_RADIO_CSS_CLASS);
			} else {
				out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);
			}
			out.writeAttribute(STYLE_ATTR, getInputStyle());
			if (hasTabIndex()) {
				out.writeAttribute(TABINDEX_ATTR, getTabIndex());
			}

			writeOnChange(context, out);

			out.endEmptyTag();
		}
		out.endTag(SPAN);
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
		out.append(".handleOnChange(this, true);");
		out.append("return true;");
	}

	private String getInputId() {
		return getID() + "-input";
	}

	private int getTabIndex() {
		return _tabIndex;
	}

	private boolean hasTabIndex() {
		return _tabIndex >= 0;
	}

	private String getInputStyle() {
		return _inputStyle;
	}

	private boolean isDisabled() {
		return _selectionPartModel.isDisabled();
	}

	private boolean isChecked() {
		return _selectionPartModel.isSelected();
	}

	private boolean isSingle() {
		return _selectionPartModel.isSingleSelection();
	}

	/**
	 * Registers the given {@link SelectionVetoListener}.
	 * 
	 * @param listener
	 *        Listener to check, before selection changes.
	 */
	public void addSelectionVetoListener(SelectionVetoListener listener) {
		_selectionPartModel.addSelectionVetoListener(listener);
	}

	/**
	 * Removes the given {@link SelectionVetoListener}.
	 * 
	 * @param listener
	 *        Listener to remove.
	 */
	public void removeSelectionVetoListener(SelectionVetoListener listener) {
		_selectionPartModel.removeSelectionVetoListener(listener);
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

			optionControl.getModel().updateSelectionVeto(optionControl, Utils.isTrue(value));

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED;
		}
	}

	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		if (_selectionPartModel.shallUpdateBox(formerlySelectedObjects, selectedObjects)) {
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
					new ConstantDisplayValue(Boolean.toString(isChecked()))));

			_selectionValid = true;
		}
	}

	/**
	 * Model of {@link SelectionPartControl}, that handles synchronization of displayed box (radio
	 * box or check box) with a {@link SelectionModel}.
	 */
	public static abstract class SelectionPartModel {

		private SelectionModel _selectionModel;

		private List<SelectionVetoListener> _vetoListeners = Collections.emptyList();

		/**
		 * Create a new {@link SelectionPartModel}.
		 */
		public SelectionPartModel(SelectionModel selectionModel) {
			_selectionModel = selectionModel;
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
		 * {@link SelectionModel} of this {@link SelectionPartModel}.
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
		 * true, if the selectable option is currently disabled, false otherwise.
		 */
		public abstract boolean isDisabled();

		/**
		 * true, if the selectable option is currently checked, false otherwise.
		 */
		public abstract boolean isSelected();

		/**
		 * true, if the box (check box or radio box) shall be updated, due to changes of the
		 *         selected options, false otherwise.
		 */
		public abstract boolean shallUpdateBox(Set<?> formerlySelectedObjects, Set<?> currentlySelectedOptions);

		/**
		 * Implementation of {@link #updateSelection(boolean)} but checks control for veto.
		 * 
		 * @param control
		 *        The holder for the veto listeners.
		 */
		public abstract void updateSelectionVeto(SelectionPartControl control, boolean selected);

		/**
		 * Update of the {@link SelectionModel}.
		 */
		public abstract void updateSelection(boolean selected);

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

	}
	
	private static class DefaultSelectionPartModel extends SelectionPartModel {

		private Object _selectionPart;

		public DefaultSelectionPartModel(SelectionModel selectionModel, Object selectionPart) {
			super(selectionModel);
			_selectionPart = selectionPart;
		}

		@Override
		public boolean isDisabled() {
			return !getSelectionModel().isSelectable(_selectionPart);
		}

		@Override
		public boolean isSelected() {
			return getSelectionModel().isSelected(_selectionPart);
		}

		@Override
		public boolean shallUpdateBox(Set<?> formerlySelectedObjects, Set<?> currentlySelectedOptions) {
			return formerlySelectedObjects.contains(_selectionPart) ^ currentlySelectedOptions.contains(_selectionPart);
		}

		@Override
		public void updateSelection(boolean selected) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(getSelectionModel(), _selectionPart, selected,
					SelectionChangeKind.INCREMENTAL);
			}
			getSelectionModel().setSelected(_selectionPart, selected);
		}

		@Override
		public void updateSelectionVeto(SelectionPartControl control, boolean selected) {
			try {
				for (SelectionVetoListener vetoListener : getVetoListeners()) {
					vetoListener.checkVeto(getSelectionModel(), _selectionPart, SelectionType.TOGGLE_SINGLE);
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
}
