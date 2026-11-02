/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.operations;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.graphic.flow.operations.DiagramOperations;

import de.haumacher.msgbuf.observer.Listener;
import de.haumacher.msgbuf.observer.Observable;

/**
 * Test case for {@link DiagramOperations#addToSelection(Iterable)}, the selection made by a
 * rectangle drawn over the diagram.
 */
public class TestDiagramAddToSelection extends TestCase {

	private SelectableBox _a;

	private SelectableBox _b;

	private SelectableBox _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_a = box("a");
		_b = box("b");
		_c = box("c");
	}

	/**
	 * Tests that the boxes are added to the boxes selected so far, as an incremental change.
	 */
	public void testAddsToTheSelection() {
		Diagram diagram = diagram(true);
		select(diagram, _a);
		diagram.setIncrementalSelection(false);

		diagram.addToSelection(List.of(_b, _c));

		assertEquals(List.of(_a, _b, _c), diagram.getSelection());
		assertTrue(_a.isSelected());
		assertTrue(_b.isSelected());
		assertTrue(_c.isSelected());
		assertTrue("Selected objects the diagram does not display are kept.",
			diagram.isIncrementalSelection());
	}

	/**
	 * Tests that the change is marked incremental before the selection changes, and that only the
	 * new boxes are added, none removed: the server takes every step as a change of the selection.
	 */
	public void testOnlyAddsTheNewBoxes() {
		Diagram diagram = diagram(true);
		select(diagram, _a);

		List<String> changes = new ArrayList<>();
		diagram.registerListener(new Listener() {
			@Override
			public void beforeSet(Observable obj, String property, Object value) {
				changes.add("set " + property);
			}

			@Override
			public void beforeAdd(Observable obj, String property, int index, Object element) {
				changes.add("add " + property);
			}

			@Override
			public void afterRemove(Observable obj, String property, int index, Object element) {
				changes.add("remove " + property);
			}
		});

		diagram.addToSelection(List.of(_a, _b, _c));

		assertEquals(
			List.of(
				"set " + Diagram.INCREMENTAL_SELECTION__PROP,
				"add " + Diagram.SELECTION__PROP,
				"add " + Diagram.SELECTION__PROP),
			changes);
	}

	/**
	 * Tests that a box already selected, or named twice, is selected once.
	 */
	public void testSelectsEachBoxOnce() {
		Diagram diagram = diagram(true);
		select(diagram, _a);

		diagram.addToSelection(List.of(_a, _b, _b));

		assertEquals(List.of(_a, _b), diagram.getSelection());
	}

	/**
	 * Tests that a diagram showing one selected box at a time is left unchanged.
	 */
	public void testSingleSelectDiagramIsUnchanged() {
		Diagram diagram = diagram(false);
		select(diagram, _a);

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
		select(diagram, _a);
		diagram.setIncrementalSelection(false);

		diagram.addToSelection(List.of(_a));

		assertEquals(List.of(_a), diagram.getSelection());
		assertFalse(diagram.isIncrementalSelection());
	}

	private static void select(Diagram diagram, SelectableBox box) {
		diagram.getSelection().add(box);
		box.setSelected(true);
	}

	private static SelectableBox box(String name) {
		return SelectableBox.create().setContent(Text.create().setValue(name));
	}

	private Diagram diagram(boolean multiSelect) {
		return Diagram.create()
			.setRoot(VerticalLayout.create().addContent(_a).addContent(_b).addContent(_c))
			.setMultiSelect(multiSelect);
	}

}
