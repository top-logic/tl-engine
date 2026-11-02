/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.operations;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.operations.DiagramOperations;

/**
 * Test case for {@link DiagramOperations#addToSelection(Iterable)}, the selection made by a
 * rectangle drawn over the diagram.
 */
public class TestDiagramAddToSelection extends TestCase {

	private SelectableBox _a;

	private SelectableBox _b;

	private Text _text;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_a = SelectableBox.create().setContent(Text.create().setValue("a"));
		_b = SelectableBox.create().setContent(Text.create().setValue("b"));
		_text = Text.create().setValue("text");
	}

	/**
	 * Tests that the elements are added to the elements selected so far, as an incremental change.
	 */
	public void testAddsToTheSelection() {
		Diagram diagram = diagram(true);
		diagram.getSelection().add(_a);
		_a.setSelected(true);
		diagram.setIncrementalSelection(false);

		diagram.addToSelection(List.of(_b));

		assertEquals(List.of(_a, _b), diagram.getSelection());
		assertTrue(_a.isSelected());
		assertTrue(_b.isSelected());
		assertTrue("Selected objects the diagram does not display are kept.",
			diagram.isIncrementalSelection());
	}

	/**
	 * Tests that an element already selected is not added a second time.
	 */
	public void testKeepsSelectedElementsOnce() {
		Diagram diagram = diagram(true);
		diagram.getSelection().add(_a);
		_a.setSelected(true);

		diagram.addToSelection(List.of(_a, _b));

		assertEquals(List.of(_a, _b), diagram.getSelection());
	}

	/**
	 * Tests that elements the user cannot select are skipped: a plain box, and an edge not marked
	 * selectable.
	 */
	public void testSkipsElementsThatCannotBeSelected() {
		Diagram diagram = diagram(true);
		GraphEdge fixedEdge = GraphEdge.create().setSelectable(false);
		GraphEdge selectableEdge = GraphEdge.create().setSelectable(true);

		diagram.addToSelection(List.of(_text, fixedEdge, selectableEdge));

		assertEquals(List.of(selectableEdge), diagram.getSelection());
		assertFalse(fixedEdge.isSelected());
		assertTrue(selectableEdge.isSelected());
	}

	/**
	 * Tests that a diagram showing one selected element at a time is left unchanged.
	 */
	public void testSingleSelectDiagramIsUnchanged() {
		Diagram diagram = diagram(false);
		diagram.getSelection().add(_a);
		_a.setSelected(true);

		diagram.addToSelection(List.of(_b));

		assertEquals(List.of(_a), diagram.getSelection());
		assertFalse(_b.isSelected());
	}

	/**
	 * Tests that a rectangle enclosing nothing new changes nothing, not even the kind of the last
	 * selection change.
	 */
	public void testNothingNewChangesNothing() {
		Diagram diagram = diagram(true);
		diagram.setIncrementalSelection(false);

		diagram.addToSelection(List.of(_text));

		assertEquals(List.of(), diagram.getSelection());
		assertFalse(diagram.isIncrementalSelection());
	}

	private Diagram diagram(boolean multiSelect) {
		return Diagram.create()
			.setRoot(VerticalLayout.create().addContent(_a).addContent(_b).addContent(_text))
			.setMultiSelect(multiSelect);
	}

}
