/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.display;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} displaying (read-only) a BPMN diagram.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMNDisplay extends AbstractControl {

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(new UISelection(), new HandleDiagramData(), new SetChangedState());

	private boolean _editMode;

	private Collaboration _model;

	private final SelectionModel _selectionModel;

	private StoreCallback _storeCallback;

	private boolean _changed;

	private List<BPMNSelectVetoListener> _vetoListeners = Collections.emptyList();

	/**
	 * Creates a {@link BPMNDisplay}.
	 *
	 * @param model
	 *        The diagram model to display.
	 * @param selection
	 *        The {@link SelectionModel} that holds the current selected objects in the given
	 *        collaboration.
	 */
	public BPMNDisplay(Collaboration model, SelectionModel selection) {
		super(COMMANDS);
		_model = model;
		_selectionModel = selection;
	}

	/**
	 * Whether the display supports modifying the diagram.
	 */
	public boolean getEditMode() {
		return _editMode;
	}

	/**
	 * @see #getEditMode()
	 */
	public void setEditMode(boolean editMode) {
		_editMode = editMode;
		resetChanged();
		requestRepaint();
	}

	/**
	 * Whether the graph has currently transient changes on the client.
	 */
	public boolean isChanged() {
		return _changed;
	}

	/**
	 * Resets the {@link #isChanged()} state to <code>false</code> (e.g. after a save operation).
	 */
	public void resetChanged() {
		setChanged(false);
	}

	void setChanged(boolean changed) {
		_changed = changed;
	}

	/**
	 * The {@link SelectionModel} in use.
	 */
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	public Collaboration getModel() {
		return _model;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected String getTypeCssClass() {
		return "cBPMN";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		resetChanged();

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.beginAttribute("data-bpml");
		if (_model != null) {
			try {
				XMLStreamWriter xout = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(out);
				new BPMLExporter(xout, false).exportBPML(_model);
			} catch (XMLStreamException ex) {
				throw new IOException(ex);
			}
		}
		out.endAttribute();
		out.endBeginTag();
		out.endTag(DIV);

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("services.bpe.BPMNDisplay.init(");
		out.writeJsString(getID());
		out.append(", ");
		out.writeJsLiteral(_editMode);
		out.append(", ");
		out.writeJsString(selectedGUIId());
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);
	}

	/**
	 * Adds the given {@link BPMNSelectVetoListener} to check for veto when the user changes the
	 * selection in the diagram.
	 * 
	 * @param listener
	 *        The {@link BPMNSelectVetoListener} to call before the GUI selection of the user is
	 *        applied.
	 */
	public void addSelectVetoListener(BPMNSelectVetoListener listener) {
		Objects.requireNonNull(listener, "Listener must not be null.");
		switch (_vetoListeners.size()) {
			case 0:
				_vetoListeners = Collections.singletonList(listener);
				break;
			case 1:
				_vetoListeners = new ArrayList<>(_vetoListeners);
				//$FALL-THROUGH$
			default:
				_vetoListeners.add(listener);
				break;
		}
	}

	/**
	 * Removes the given {@link BPMNSelectVetoListener} from the list of listeners.
	 * 
	 * @param listener
	 *        A formerly added {@link BPMNSelectVetoListener}.
	 * 
	 * @see #addSelectVetoListener(BPMNSelectVetoListener)
	 */
	public void removeSelectVetoListener(BPMNSelectVetoListener listener) {
		switch (_vetoListeners.size()) {
			case 0:
				break;
			case 1:
				if (_vetoListeners.get(0).equals(listener)) {
					_vetoListeners = Collections.emptyList();
				}
				break;
			default:
				_vetoListeners.remove(listener);
				break;
		}
	}

	/**
	 * Selects the element with the given GUI identifier.
	 * 
	 * @param selectionId
	 *        The GUI identifier of the element to select or <code>null</code>, when the selection
	 *        must be cleared. When there is no element with the given GUI ID, the selection is also
	 *        cleared.
	 */
	public void notifySelected(String selectionId) {
		Object selectedPart = BPEUtil.findPart(selectionId, _model);
		if (selectedPart != null && _selectionModel.isSelected(selectedPart)) {
			return;
		}
		try {
			for (BPMNSelectVetoListener vetoListener : _vetoListeners) {
				vetoListener.checkVeto(this, selectedPart);
			}
			if (selectedPart != null) {
				_selectionModel.setSelected(selectedPart, true);
			} else {
				_selectionModel.clear();
			}
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					notifySelected(selectionId);
					addSelectElementUpdate(selectionId);
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(getWindowScope());
			String newSelection = selectedGUIId();
			addSelectElementUpdate(newSelection);

		}
	}

	/**
	 * The GUI identifier for the selected element, or <code>null</code> when currently nothing is
	 * selected.
	 */
	@FrameworkInternal
	public String selectedGUIId() {
		Set<?> currentSelection = _selectionModel.getSelection();
		String newSelection;
		if (currentSelection.isEmpty()) {
			newSelection = null;
		} else {
			newSelection = ((Externalized) currentSelection.iterator().next()).getExtId();
		}
		return newSelection;
	}

	void addSelectElementUpdate(String elementId) {
		addUpdate(new JSSnipplet(new DynamicText() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("services.bpe.BPMNDisplay.selectElement(");
				TagUtil.writeJsString(out, getID());
				out.append(",");
				TagUtil.writeJsString(out, elementId);
				out.append(");");
			}
		}));
	}

	/**
	 * Requests the current diagram data from the UI.
	 *
	 * @param fun
	 *        The store callback to invoke, when the diagram data is available.
	 */
	public void storeDiagram(StoreCallback fun) {
		if (!isRepaintRequested()) {
			_storeCallback = fun;
			addUpdate(new JSSnipplet((context, out) -> {
				out.append("services.bpe.BPMNDisplay.storeDiagram(");
				TagUtil.writeJsString(out, getID());
				out.append(");");
			}));
		}
	}

	void handleDiagramData(DisplayContext context, String xml) {
		if (_storeCallback != null) {

			// TODO #23835: Workaround for buggy XML serializer in bpmn-js.
			if (!xml.contains("xmlns:bpmndi")) {
				xml = xml.replace(
					"xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"",
					"xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
					"xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
					"xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" " +
					"xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"");
			}

			_storeCallback.handleDiagramData(context, xml);
			_storeCallback = null;
		}
	}
	
	/**
	 * {@link ControlCommand} receiving selection events from the client.
	 */
	public static class SetChangedState extends ControlCommand {

		private static final String COMMAND_NAME = "setChangedState";

		/**
		 * Creates a {@link UISelection}.
		 */
		public SetChangedState() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SET_CHANGED_STATE_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			boolean changed = (Boolean) arguments.get("changed");
			((BPMNDisplay) control).setChanged(changed);
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	/**
	 * {@link ControlCommand} receiving selection events from the client.
	 */
	public static class HandleDiagramData extends ControlCommand {

		private static final String COMMAND_NAME = "handleDiagramData";

		/**
		 * Creates a {@link UISelection}.
		 */
		public HandleDiagramData() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SET_CHANGED_STATE_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			String xml = (String) arguments.get("xml");
			((BPMNDisplay) control).handleDiagramData(commandContext, xml);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			// Note: Diagram data is requested e.g. while the check-changed dialog is open.
			return true;
		}

	}

	/**
	 * {@link ControlCommand} receiving selection events from the client.
	 */
	public static class UISelection extends ControlCommand {

		private static final String COMMAND_NAME = "uiSelection";

		/**
		 * Creates a {@link UISelection}.
		 */
		public UISelection() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UI_SELECTION_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			String selectionId = (String) arguments.get("elementID");
			((BPMNDisplay) control).notifySelected(selectionId);
			return HandlerResult.DEFAULT_RESULT;
		}

	}
}
