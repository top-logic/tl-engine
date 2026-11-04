/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.view;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.VetoForwarder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.SelectionChannelBinding;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;

/**
 * {@link ReactControl} of a {@link BpmnEditorElement}: draws the BPMN diagram of a
 * {@link Collaboration} with bpmn-js in the client component <code>TLBpmnEditor</code>.
 *
 * <p>
 * The control publishes the diagram as BPMN XML ({@link #XML}), written by the
 * {@link BPMLExporter} from the model, and asks the client to draw it anew by raising
 * {@link #REVISION}. {@link #EDITABLE} switches between the bpmn-js modeler and viewer,
 * {@link #SELECTED} names the selected element by its BPMN ID ({@link Externalized#getExtId()}).
 * </p>
 *
 * <p>
 * The client reports a click with {@link #CMD_SELECT}, the BPMN ID in the argument {@link #ID}.
 * A changed diagram arrives like the value of a field: the command
 * {@link ReactFormFieldControl#CMD_VALUE_CHANGED} carries the XML of the whole diagram in
 * {@link FieldValueArguments#VALUE}, debounced by the client and sent before any action command.
 * </p>
 *
 * <p>
 * As a {@link StateHandler}, the control reports a changed diagram that is not saved: it blocks the
 * write of the input channel and tells the enclosing {@link DirtyChannel}. The changes can be
 * discarded, not saved - see {@link #canSave()}.
 * </p>
 */
public class BpmnEditorControl extends ReactControl implements StateHandler {

	/** Name of the client component. */
	public static final String REACT_MODULE = "TLBpmnEditor";

	/** State key of the BPMN XML of the shown diagram, <code>null</code> for an empty canvas. */
	public static final String XML = "xml";

	/**
	 * State key of the number of diagrams the control asked the client to draw; a new number makes
	 * the client draw {@link #XML} anew, also when the text has not changed.
	 */
	public static final String REVISION = "revision";

	/** State key of the switch between the bpmn-js modeler (<code>true</code>) and viewer. */
	public static final String EDITABLE = "editable";

	/** State key of the BPMN ID of the selected element, <code>null</code> for none. */
	public static final String SELECTED = "selected";

	/** Command the client sends when the user clicked an element, see {@link #ID}. */
	public static final String CMD_SELECT = "select";

	/**
	 * Argument of {@link #CMD_SELECT}: the BPMN ID of the clicked element; the ID of the canvas
	 * names the collaboration itself.
	 */
	public static final String ID = "id";

	private final ViewChannel _input;

	private final ViewChannel _selectionChannel;

	private final ViewChannel _diagramChannel;

	private final Predicate<Collaboration> _editable;

	private final DirtyChannel _dirtyChannel;

	private Collaboration _collaboration;

	private int _revision;

	/** The selected model element of the shown collaboration, <code>null</code> for none. */
	private Externalized _selected;

	/** The BPMN XML of the changed diagram, <code>null</code> while the diagram shows the model. */
	private String _changedXml;

	private BpmnSelectionBinding _selectionBinding;

	/**
	 * Creates a {@link BpmnEditorControl}.
	 *
	 * @param context
	 *        The context of the view the control is part of.
	 * @param input
	 *        The channel holding the {@link Collaboration} to show.
	 * @param selectionChannel
	 *        The channel holding the selected model element, <code>null</code> for none.
	 * @param diagramChannel
	 *        The channel receiving the changed diagram as BPMN XML, <code>null</code> for none.
	 * @param editable
	 *        Decides for the shown collaboration (<code>null</code> for none) whether the user may
	 *        change the diagram.
	 */
	public BpmnEditorControl(ViewContext context, ViewChannel input, ViewChannel selectionChannel,
			ViewChannel diagramChannel, Predicate<Collaboration> editable) {
		super(context, null, REACT_MODULE);
		_input = input;
		_selectionChannel = selectionChannel;
		_diagramChannel = diagramChannel;
		_editable = editable;
		_dirtyChannel = context.getDirtyChannel();

		Registration inputRegistration = _input.addListener((sender, oldValue, newValue) -> showInput());
		addCleanupAction(inputRegistration::dispose);

		VetoListener inputVeto = new VetoListener() {
			@Override
			public List<StateHandler> checkVeto(ViewChannel sender, Object oldValue, Object newValue) {
				return checkDirty(sender);
			}

			@Override
			public List<StateHandler> checkDirty(ViewChannel sender) {
				return isDirty() ? List.of(BpmnEditorControl.this) : List.of();
			}
		};
		_input.addVetoListener(inputVeto);
		addCleanupAction(() -> _input.removeVetoListener(inputVeto));

		if (_dirtyChannel != null) {
			addCleanupAction(() -> _dirtyChannel.removeHandler(this));
		}

		if (_diagramChannel != null) {
			Registration diagramRegistration = _diagramChannel.addListener(this::handleDiagramValue);
			addCleanupAction(diagramRegistration::dispose);
		}

		// Draws the model anew when the shown collaboration or one of its parts is changed - e.g.
		// by the action chain saving the diagram.
		ChannelObjectObserver observer = new ChannelObjectObserver(List.of(_input),
			Set.of(TlBpeBpmlFactory.getExternalizedType()), this::handleModelChange);
		addAttachListener(() -> observer.attach(context.getModelScope()));
		addDetachListener(observer::detach);

		showInput();

		if (_selectionChannel != null) {
			_selectionBinding = new BpmnSelectionBinding(_selectionChannel);
			addCleanupAction(_selectionBinding::dispose);

			// Showing another collaboration drops the selection, so the unsaved changes blocking the
			// selection are reported when the input is asked, before the input is written.
			addCleanupAction(VetoForwarder.forward(_input, _selectionChannel));
		}
	}

	/**
	 * The shown collaboration, <code>null</code> for an empty canvas.
	 */
	public Collaboration getCollaboration() {
		return _collaboration;
	}

	private void showInput() {
		Object value = _input.get();
		_collaboration = value instanceof Collaboration collaboration ? collaboration : null;
		showModel();
	}

	private void handleModelChange(ModelChangeEvent event) {
		if (_collaboration == null || !_collaboration.tValid()) {
			return;
		}
		if (Stream.concat(event.getUpdated(), event.getCreated()).anyMatch(this::isPartOfCollaboration)) {
			showModel();
		}
	}

	private boolean isPartOfCollaboration(TLObject object) {
		for (TLObject current = object; current != null; current = current.tContainer()) {
			if (current == _collaboration) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Draws the shown collaboration as it is stored, dropping the changes of the diagram.
	 */
	private void showModel() {
		_changedXml = null;

		if (_selected != null && (_collaboration == null || !_selected.tValid()
			|| BPEUtil.findPart(_selected.getExtId(), _collaboration) != _selected)) {
			_selected = null;
		}

		Object token = beginUpdate();
		try {
			putState(XML, _collaboration == null ? null : exportDiagram(_collaboration));
			putState(REVISION, Integer.valueOf(++_revision));
			putState(EDITABLE, Boolean.valueOf(_editable.test(_collaboration)));
			putState(SELECTED, bpmnId(_selected));
		} finally {
			commitUpdate(token);
		}

		publishChanges();
		if (_selectionBinding != null) {
			_selectionBinding.refreshed();
		}
	}

	private static String exportDiagram(Collaboration collaboration) {
		StringWriter buffer = new StringWriter();
		try {
			XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
			new BPMLExporter(out, false).exportBPML(collaboration);
			out.flush();
		} catch (XMLStreamException ex) {
			throw new RuntimeException("Cannot export the diagram of workflow '" + collaboration.getName() + "'.",
				ex);
		}
		return buffer.toString();
	}

	/**
	 * Tells the diagram channel and the enclosing dirty-tracked scope about {@link #_changedXml}.
	 */
	private void publishChanges() {
		if (_diagramChannel != null) {
			_diagramChannel.set(_changedXml);
		}
		if (_dirtyChannel != null) {
			_dirtyChannel.updateState(this, isDirty());
		}
	}

	private void handleDiagramValue(ViewChannel sender, Object oldValue, Object newValue) {
		if (newValue == null && _changedXml != null) {
			// Cleared from outside: the changes are discarded.
			showModel();
		}
	}

	/**
	 * Receives the BPMN XML of the diagram after the user changed it.
	 */
	@ReactCommandHandler(ReactFormFieldControl.CMD_VALUE_CHANGED)
	void handleDiagramChanged(Map<String, Object> arguments) {
		Object xml = arguments.get(FieldValueArguments.VALUE);
		if (_collaboration == null || !(xml instanceof String text)) {
			return;
		}
		_changedXml = text;
		publishChanges();
	}

	/**
	 * Receives the element the user clicked and writes its model element to the selection channel.
	 */
	@ReactCommandHandler(CMD_SELECT)
	void handleSelect(Map<String, Object> arguments) {
		Object id = arguments.get(ID);
		Object part = id instanceof String extId && _collaboration != null
			? BPEUtil.findPart(extId, _collaboration)
			: null;
		Externalized before = _selected;
		Externalized selected = part instanceof Externalized externalized ? externalized : null;
		// The client shows the clicked element already.
		updateStateSilently(() -> putState(SELECTED, bpmnId(selected)));
		_selected = selected;
		if (_selectionBinding == null) {
			return;
		}
		try {
			_selectionBinding.selectionChanged(selected);
		} catch (ChannelVetoException ex) {
			// Unsaved changes of whoever shows the selection block the change: the diagram shows the
			// selection it showed before.
			_selected = before;
			putState(SELECTED, bpmnId(before));
			throw ex;
		}
	}

	/**
	 * Shows the given model element selected, <code>null</code> or an element of another
	 * collaboration for no selection.
	 */
	void displaySelection(Object part) {
		Externalized selected = part instanceof Externalized externalized
			&& _collaboration != null
			&& BPEUtil.findPart(externalized.getExtId(), _collaboration) == externalized ? externalized : null;
		_selected = selected;
		putState(SELECTED, bpmnId(selected));
	}

	/**
	 * Whether the given object is a model element of the shown collaboration.
	 */
	boolean hasElement(Object part) {
		return part instanceof Externalized externalized
			&& _collaboration != null
			&& BPEUtil.findPart(externalized.getExtId(), _collaboration) == externalized;
	}

	private static String bpmnId(Externalized part) {
		return part == null ? null : part.getExtId();
	}

	// -- StateHandler --

	@Override
	public boolean isDirty() {
		return _changedXml != null;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

	/**
	 * Always <code>false</code>: the editor does not save its changes itself, a view saves them
	 * through an action chain. The question about unsaved changes therefore offers to discard them
	 * only.
	 */
	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public void executeSave() {
		throw new UnsupportedOperationException(
			"The diagram editor does not save, a view saves the diagram through an action chain.");
	}

	@Override
	public void executeDiscard() {
		if (isDirty()) {
			showModel();
		}
	}

	@Override
	public String getDescription() {
		return MetaLabelProvider.INSTANCE.getLabel(_collaboration);
	}

	/**
	 * {@link SelectionChannelBinding} between the selected element of the diagram and the selection
	 * channel.
	 */
	private class BpmnSelectionBinding extends SelectionChannelBinding {

		BpmnSelectionBinding(ViewChannel channel) {
			super(channel);
			attach();
		}

		void selectionChanged(Externalized selected) {
			selectionChanged(selected == null ? Set.of() : Set.<Object> of(selected));
		}

		@Override
		protected Set<Object> getSelectedKeys() {
			return _selected == null ? Set.of() : Set.<Object> of(_selected);
		}

		@Override
		protected void displaySelection(Set<?> keys) {
			BpmnEditorControl.this.displaySelection(keys.isEmpty() ? null : keys.iterator().next());
		}

		@Override
		protected boolean canDisplaySeveral() {
			return false;
		}

		@Override
		protected boolean hasElement(Object key) {
			return BpmnEditorControl.this.hasElement(key);
		}

		@Override
		protected void detach() {
			// The selection of the diagram is held by the control, no listener to give up.
		}
	}

}
