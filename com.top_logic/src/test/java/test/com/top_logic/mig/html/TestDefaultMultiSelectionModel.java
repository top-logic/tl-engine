/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * The class {@link TestDefaultMultiSelectionModel} tests {@link TestDefaultMultiSelectionModel}
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultMultiSelectionModel extends TestCase {

	static class Listener implements SelectionListener {

		Set<?> _formerlySelectedObjects;

		Set<?> _selectedObjects;

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
			assertNotNull(formerlySelectedObjects);
			assertNotNull(selectedObjects);
			_formerlySelectedObjects = formerlySelectedObjects;
			_selectedObjects = selectedObjects;
		}

		void reset() {
			_formerlySelectedObjects = null;
			_selectedObjects = null;
		}
	}

	private DefaultMultiSelectionModel _model;

	private Listener _l;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_model = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		_l = new Listener();
		_model.addSelectionListener(_l);
	}
	
	public void testSetSelection() {
		_model.setSelection(set(54, 56));
		assertEquals(set(), _l._formerlySelectedObjects);
		assertEquals(set(56, 54), _l._selectedObjects);
		_l.reset();

		_model.setSelection(set(54));
		assertEquals(set(56, 54), _l._formerlySelectedObjects);
		assertEquals(set(54), _l._selectedObjects);
		_l.reset();

		_model.setSelection(set(54, 52));
		assertEquals(set(54), _l._formerlySelectedObjects);
		assertEquals(set(54, 52), _l._selectedObjects);
		_l.reset();

		_model.setSelection(set(52, 54));
		assertNull(_l._formerlySelectedObjects);
		assertNull(_l._selectedObjects);
		_l.reset();

		_model.setSelection(set(1, 2, 3, 4));
		assertEquals(set(54, 52), _l._formerlySelectedObjects);
		assertEquals(set(1, 2, 3, 4), _l._selectedObjects);
		_l.reset();

	}

	public void testAddToSelection() {
		_model.addToSelection(set(54, 56));
		assertEquals(set(), _l._formerlySelectedObjects);
		assertEquals(set(54, 56), _l._selectedObjects);
		_l.reset();

		_model.addToSelection(set(58, 59));
		assertEquals(set(54, 56), _l._formerlySelectedObjects);
		assertEquals(set(54, 56, 58, 59), _l._selectedObjects);
		_l.reset();

		_model.addToSelection(set());
		assertNull(_l._formerlySelectedObjects);
		assertNull(_l._selectedObjects);
		_l.reset();

		_model.addToSelection(set(58, 60));
		assertEquals(set(54, 56, 58, 59), _l._formerlySelectedObjects);
		assertEquals(set(54, 56, 58, 59, 60), _l._selectedObjects);
		_l.reset();
	}

	public void testRemoveFromSelection() {
		_model.setSelection(set(54, 56, 58, 59, 60));
		assertEquals(set(), _l._formerlySelectedObjects);
		assertEquals(set(54, 56, 58, 59, 60), _l._selectedObjects);
		_l.reset();

		_model.removeFromSelection(set(58, 59));
		assertEquals(set(54, 56, 58, 59, 60), _l._formerlySelectedObjects);
		assertEquals(set(54, 56, 60), _l._selectedObjects);
		_l.reset();

		_model.removeFromSelection(set());
		assertNull(_l._formerlySelectedObjects);
		assertNull(_l._selectedObjects);
		_l.reset();

		_model.removeFromSelection(set(58, 60));
		assertEquals(set(54, 56, 60), _l._formerlySelectedObjects);
		assertEquals(set(54, 56), _l._selectedObjects);
		_l.reset();
	}

	public void testCannotChangeFixedSelection() {
		final Object fixedSelection = new Object();
		_model.setSelected(fixedSelection, true);
		_model.setDeselectionFilter(new Filter<Object>() {

			@Override
			public boolean accept(Object anObject) {
				return anObject != fixedSelection;
			}
		});

		Object newSelection = new Object();

		_model.setSelection(set(newSelection));
		assertEquals(set(fixedSelection, newSelection), _model.getSelection());

		_model.setSelected(fixedSelection, false);
		assertEquals(set(fixedSelection, newSelection), _model.getSelection());
	}

	public void testCannotClearFixedSelection() {
		final Object fixedSelection = new Object();
		_model.setSelected(fixedSelection, true);
		_model.setDeselectionFilter(new Filter<Object>() {

			@Override
			public boolean accept(Object anObject) {
				return anObject != fixedSelection;
			}
		});

		_model.clear();
		assertEquals(set(fixedSelection), _model.getSelection());
	}
}

