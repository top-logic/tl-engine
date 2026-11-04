/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.model.CommittedModelChanges;
import test.com.top_logic.layout.view.model.CommittedModelChanges.ScopedReactContext;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.bpe.bpml.BPMLDefinitions;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.view.BpmnEditorControl;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Test of {@link BpmnEditorControl}: the selection, the changed diagram and its unsaved changes.
 */
@SuppressWarnings("javadoc")
public class TestBpmnEditorControl extends BasicTestCase {

	/** The name of a manual task of the default diagram of a new workflow. */
	private static final String TASK_NAME = "Create";

	private GlobalModelEventForwarder _scope;

	private DefaultViewChannel _input;

	private DefaultViewChannel _selection;

	private DefaultViewChannel _diagram;

	private Collaboration _collaboration;

	private ManualTask _task;

	private TestedEditor _editor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_collaboration = inTx(() -> BPMLDefinitions.createCollaboration("Editor test"));
		_task = findTask(_collaboration, TASK_NAME);
		assertNotNull(_task);

		_scope = CommittedModelChanges.modelScope();
		ViewContext context = new DefaultViewContext(new ScopedReactContext(_scope));
		_input = new DefaultViewChannel("workflow");
		_selection = new DefaultViewChannel("selection");
		_diagram = new DefaultViewChannel("diagram");
		_input.set(_collaboration);

		_editor = new TestedEditor(context, _input, _selection, _diagram);
		_editor.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_editor.detach();
		_editor.cleanupTree();
		super.tearDown();
	}

	public void testInitialState() {
		assertNotNull(_editor.state(BpmnEditorControl.XML));
		assertEquals(Boolean.FALSE, _editor.state(BpmnEditorControl.EDITABLE));
		assertNull(_editor.state(BpmnEditorControl.SELECTED));
		assertNull(_diagram.get());
		assertFalse(_editor.isDirty());
		assertFalse(_editor.hasErrors());
		assertFalse("The editor does not save itself.", _editor.canSave());
	}

	public void testSelectWritesSelectionChannel() {
		_editor.executeCommand(BpmnEditorControl.CMD_SELECT, Map.of(BpmnEditorControl.ID, _task.getExtId()));
		assertSame(_task, _selection.get());
		assertEquals(_task.getExtId(), _editor.state(BpmnEditorControl.SELECTED));
	}

	public void testSelectionChannelSelectsElement() {
		Lane lane = firstLane(_collaboration);
		_selection.set(lane);
		assertEquals(lane.getExtId(), _editor.state(BpmnEditorControl.SELECTED));

		_selection.set("no element of the workflow");
		assertNull(_editor.state(BpmnEditorControl.SELECTED));
	}

	public void testChangedDiagramIsDirty() {
		String xml = "<changed/>";
		changeDiagram(xml);
		assertEquals(xml, _diagram.get());
		assertTrue(_editor.isDirty());
	}

	public void testClearingDiagramDiscards() {
		int revision = revision();
		changeDiagram("<changed/>");

		_diagram.set(null);
		assertNull(_diagram.get());
		assertFalse(_editor.isDirty());
		assertTrue("The model is drawn anew.", revision() > revision);
	}

	public void testSaveResetsDiagram() throws IOException {
		String renamed = xml(_collaboration).replace("name=\"" + TASK_NAME + "\"", "name=\"Renamed\"");
		changeDiagram(renamed);
		int revision = revision();

		inTx(() -> {
			BPMLDefinitions.updateFromDiagram(_collaboration, _diagram.get().toString());
			return null;
		});
		_scope.synthesizeModelEvents();

		assertEquals("Renamed", _task.getName());
		assertNull("The saved diagram is no change any more.", _diagram.get());
		assertFalse(_editor.isDirty());
		assertTrue("The saved model is drawn.", revision() > revision);
		assertTrue(_editor.state(BpmnEditorControl.XML).toString().contains("Renamed"));
	}

	public void testInputWriteVetoedWhileDirty() {
		Collaboration other = inTx(() -> BPMLDefinitions.createCollaboration("Other"));
		changeDiagram("<changed/>");

		try {
			_input.set(other);
			fail("Unsaved changes must block another workflow.");
		} catch (ChannelVetoException ex) {
			assertTrue(ex.getDirtyHandlers().contains(_editor));
		}
		assertSame(_collaboration, _input.get());

		_editor.executeDiscard();
		_input.set(other);
		assertSame(other, _editor.getCollaboration());
	}

	public void testOtherWorkflowClearsSelection() {
		Collaboration other = inTx(() -> BPMLDefinitions.createCollaboration("Other"));
		_editor.executeCommand(BpmnEditorControl.CMD_SELECT, Map.of(BpmnEditorControl.ID, _task.getExtId()));
		assertSame(_task, _selection.get());

		_input.set(other);
		assertNull(_selection.get());
		assertNull(_editor.state(BpmnEditorControl.SELECTED));
	}

	private void changeDiagram(String xml) {
		_editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, xml));
	}

	private int revision() {
		return ((Number) _editor.state(BpmnEditorControl.REVISION)).intValue();
	}

	private static String xml(Collaboration collaboration) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			BPMLDefinitions.exportXml(collaboration, buffer, false);
			return buffer.toString(StringServices.UTF8);
		}
	}

	private static ManualTask findTask(Collaboration collaboration, String name) {
		for (Process process : collaboration.getProcesses()) {
			for (Node node : process.getNodes()) {
				if (node instanceof ManualTask task && name.equals(task.getName())) {
					return task;
				}
			}
		}
		return null;
	}

	private static Lane firstLane(Collaboration collaboration) {
		for (Process process : collaboration.getProcesses()) {
			for (Lane lane : process.getLanes()) {
				return lane;
			}
		}
		throw new AssertionError("No lane.");
	}

	private static <T> T inTx(Supplier<T> operation) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			T result = operation.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * {@link BpmnEditorControl} giving the test access to its state.
	 */
	private static class TestedEditor extends BpmnEditorControl {

		TestedEditor(ViewContext context, DefaultViewChannel input, DefaultViewChannel selection,
				DefaultViewChannel diagram) {
			super(context, input, selection, diagram, collaboration -> false);
		}

		Object state(String key) {
			return getState(key);
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestBpmnEditorControl.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}

}
